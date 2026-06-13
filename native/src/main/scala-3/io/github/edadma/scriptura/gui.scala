package io.github.edadma.scriptura

import io.github.edadma.suit.*
import io.github.edadma.suit.dsl.*
import io.github.edadma.suit.widgets.*
import io.github.edadma.libcairo.Surface
import io.github.edadma.typesetter.{CairoImageTypesetter, Hyphenation}
import io.github.edadma.typesetter.texish.{Processor, TypesetterHandler, registerTypesettingPrimitives}

import java.io.{ByteArrayOutputStream, PrintWriter, StringWriter}
import java.nio.file.Files

/** One typeset page held between renders: the Cairo surface the engine drew (owned here, so the
  * effect that installs the page is also responsible for freeing it), the same surface presented
  * to suit as a [[RasterImage]], the handle that asks suit to re-blit it, and the logical size to
  * show it at — the surface's device pixels divided by the display scale, so it lands one-to-one
  * and stays sharp on a HiDPI screen.
  */
private[scriptura] final class Page(
    val surf:   Surface,
    val image:  RasterImage,
    val handle: SurfaceHandle,
    val w:      Double,
    val h:      Double,
)

/** The on-screen resolution the preview is rendered at, in dots per inch. Multiplied by the
  * display's device-pixel ratio so the backing surface is sized in device pixels; the logical
  * width handed to the `surface` widget divides it back out, keeping the page crisp at 2×.
  */
private val ScreenDpi = 96.0

/** Typeset the editor's source into a list of page surfaces, capturing whatever the engine prints
  * (and any failure's stack trace) as the message log. The returned surfaces belong to the caller.
  * The boolean is whether typesetting succeeded: on failure the page list is empty and the caller
  * keeps showing its last good render rather than blanking the preview.
  */
private[scriptura] def typeset(source: String): (Vector[Page], String, Boolean) =
  val scale = { val s = DevicePixelRatio.scaleX; if s <= 0 then 1.0 else s }
  val dpi   = ScreenDpi * scale
  val out   = new ByteArrayOutputStream

  // TeX loads hyphenation patterns from its format; do the same so long words break across lines
  // instead of stretching a paragraph's spaces. Idempotent — the en-US patterns parse once.
  Hyphenation.enableEnglish()

  try
    val t       = new CairoImageTypesetter(dpi)
    val handler = new TypesetterHandler(t)
    val proc    = new Processor(handler)

    registerTypesettingPrimitives(proc, handler)

    Console.withOut(out) {
      proc.process(source)
      t.end()
    }

    val surfaces = t.getDocument.printedPages.toList.map(_.asInstanceOf[Surface])

    // frees the final page's drawing context and the measurement scratch; the collected page
    // surfaces survive for the caller to display and later destroy
    t.destroy()

    val pages =
      surfaces.iterator
        .map(s => Page(s, CairoBitmap.wrap(s), new SurfaceHandle, s.getWidth / scale, s.getHeight / scale))
        .toVector

    (pages, out.toString, true)
  catch
    case err: Throwable =>
      val sw = new StringWriter
      err.printStackTrace(new PrintWriter(sw))
      (Vector.empty, out.toString + sw.toString, false)

/** The preview editor. The source lives in component state and drives the whole window: pressing
  * Run typesets it into page surfaces, which the right pane blits. An effect keyed on the page
  * list re-blits each new page and, on its way out, destroys the surfaces of the run it replaced —
  * so the engine's pages never outlive the widgets that show them.
  */
private val App: Component[String] =
  component[String] { initial =>
    val theme                          = Theme.violetLight
    val (source, setSource, _)         = useState(initial)
    val (pages, setPages, _)           = useState(Vector.empty[Page])
    val (logText, setLog, _)           = useState("")
    val (autoRender, setAutoRender, _) = useState(false)
    val (hasError, setError, _)        = useState(false)

    // Typeset `text` into pages + log. `run` renders the current editor text on demand (the Run
    // button); the auto-render effect below renders the latest text whenever it changes. A failed
    // typeset (common while a command is half-typed) keeps the last good pages on screen and just
    // raises the error flag — an overlaid badge — rather than blanking the preview, which is jarring.
    def renderSource(text: String): Unit =
      val (ps, log, ok) = typeset(text)
      setLog(log)
      if ok then
        setPages(ps)
        setError(false)
      else setError(true)

    def run(): Unit = renderSource(source)

    // typeset once on mount so the preview is populated from the start
    useEffect(() => { run(); () => () }, Array())

    // With auto-render on, re-typeset whenever the source changes — and immediately when the
    // toggle is switched on — so the preview tracks every keystroke; with it off, only Run renders.
    useEffect(() => { if autoRender then renderSource(source); () => () }, Array(source, autoRender))

    // re-blit the current pages; the cleanup (run when the page list changes or the app unmounts)
    // frees the surfaces of the run being replaced, after its widgets have already been removed
    useEffect(() => { pages.foreach(_.handle.repaint()); () => pages.foreach(_.surf.destroy()) }, Array(pages))

    val previewPages: Seq[VNode] =
      if pages.isEmpty then Seq(text("No pages — press Run.", color = Color.rgb(0x222222)))
      else
        pages.map(p =>
          box(border = Color.rgb(0x333333), borderWidth = 1)(
            surface(p.image, p.handle, width = p.w, height = p.h),
          ),
        )

    // When the latest typeset failed, a danger badge floats in the preview's top-right corner over
    // the last good render — a quiet signal that the preview is stale, instead of blanking it.
    val errorOverlay: Seq[VNode] =
      if !hasError then Seq.empty
      else
        Seq(
          align(Alignment.topRight)(
            padding(EdgeInsets.all(12))(
              box(
                bg      = theme.danger,
                radius  = 6,
                padding = EdgeInsets.symmetric(horizontal = 10, vertical = 6),
                shadow  = Shadow(color = Color.rgb(0x000000).withAlpha(80), offset = Offset(0, 1), blur = 4),
              )(
                text("⚠ Typeset error — see log", color = theme.onPrimary),
              ),
            ),
          ),
        )

    ThemeProvider(theme)(
      box(bg = theme.background, padding = EdgeInsets.all(8))(
        splitter(axis = Axis.Horizontal, initial = 0.4)(
          // editor pane: a scrolling source editor, a message log, and the Run button. A right
          // inset keeps the editors off the splitter gutter so they read as their own panel.
          padding(EdgeInsets(top = 0, right = 8, bottom = 0, left = 0))(
          col(crossAxisAlignment = CrossAxisAlignment.Stretch, spacing = 8)(
            box(flex = 1, clip = true)(
              scrollArea(Axis.Vertical)(
                col(crossAxisAlignment = CrossAxisAlignment.Stretch, mainAxisSize = MainAxisSize.Min)(
                  TextArea(source, setSource),
                ),
              ),
            ),
            sizedBox(height = 140)(
              box(
                bg          = theme.surface,
                border      = theme.border,
                borderWidth = 1,
                radius      = theme.radius,
                clip        = true,
                padding     = EdgeInsets.all(8),
              )(
                scrollArea(Axis.Vertical)(
                  text(if logText.isEmpty then "(no messages)" else logText, color = theme.surfaceText, maxLines = 0),
                ),
              ),
            ),
            row(mainAxisAlignment = MainAxisAlignment.Center, crossAxisAlignment = CrossAxisAlignment.Center, spacing = 12)(
              Button("Run", () => run()),
              Switch(autoRender, setAutoRender),
              text("Auto-render", color = theme.surfaceText),
            ),
          ),
          ),
          // preview pane: the typeset pages on a neutral backdrop. The pages keep their true pixel
          // size, so the viewport scrolls both ways — vertically through the pages, horizontally
          // when a page is wider than the pane — rather than scaling them to fit. A small inset
          // frames the pages without wasting space, so the document reads close to edge-to-edge.
          box(flex = 1, clip = true, bg = Color.rgb(0x9aa0a6))(
            stack(Alignment.topLeft)(
              (scrollArea(both = true)(
                padding(EdgeInsets.all(8))(
                  col(crossAxisAlignment = CrossAxisAlignment.Start, mainAxisSize = MainAxisSize.Min, spacing = 12)(
                    previewPages*,
                  ),
                ),
              ) +: errorOverlay)*,
            ),
          ),
        ),
      ),
    )
  }

/** A small starter document so the preview has something to show before the user types anything. */
private val SampleSource: String =
  """Welcome to Scriptura.
    |
    |Edit the source on the left and press Run to typeset the document. Each page is rendered to a Cairo image surface and shown on the right.
    |
    |\vskip 12pt
    |
    |The typesetter breaks paragraphs into lines and lines into pages, justifying the text and balancing the page just as TeX does.
    |
    |\vfill
    |""".stripMargin

/** Launch the native preview window, seeding the editor with the input file's contents when one was
  * given on the command line, or the sample document otherwise.
  */
def scripturaGui(c: Config): Unit =
  val initial =
    c.input match
      case Some(file) if Files.exists(file.toPath) => new String(Files.readAllBytes(file.toPath))
      case _                                       => SampleSource

  Suit.run("Scriptura", 1600, 1000)(App(initial))
