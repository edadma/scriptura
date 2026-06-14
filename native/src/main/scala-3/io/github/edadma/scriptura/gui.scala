package io.github.edadma.scriptura

import io.github.edadma.suit.*
import io.github.edadma.suit.dsl.*
import io.github.edadma.suit.widgets.*
import io.github.edadma.libcairo.Surface
import io.github.edadma.typesetter.{CairoImageTypesetter, Hyphenation}
import io.github.edadma.typesetter.parser.{Processor, TypesetterHandler, registerTypesettingPrimitives}

import java.io.{ByteArrayOutputStream, File, FileOutputStream}
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
    // Show just the engine's diagnostic (it already carries the line/column and a caret), not a
    // Java stack trace — a half-typed command is an ordinary, expected state, not a crash.
    case err: Throwable =>
      val message = Option(err.getMessage).getOrElse(err.toString)
      (Vector.empty, out.toString + message, false)

/** What the editor opens with: the initial text and the file it came from (`None` for the built-in
  * sample or a brand-new document). */
private case class Init(text: String, file: Option[File])

/** The preview editor. The source lives in component state and drives the whole window: pressing
  * Run typesets it into page surfaces, which the right pane blits. An effect keyed on the page
  * list re-blits each new page and, on its way out, destroys the surfaces of the run it replaced —
  * so the engine's pages never outlive the widgets that show them. The toolbar runs the typesetter,
  * toggles live rendering, and loads/saves the document; an unsaved-changes guard confirms before
  * the window closes.
  */
private val App: Component[Init] =
  component[Init] { init =>
    val theme                            = Theme.violetLight
    val muted                            = Color.lerp(theme.surfaceText, theme.surface, 0.4)
    val (source, setSource, _)           = useState(init.text)
    val (savedText, setSavedText, _)     = useState(init.text)
    val (currentFile, setCurrentFile, _) = useState(init.file)
    val (pages, setPages, _)             = useState(Vector.empty[Page])
    val (logText, setLog, _)             = useState("")
    val (autoRender, setAutoRender, _)   = useState(true)
    val (hasError, setError, _)          = useState(false)

    // Modal state: the Save-As / Open path prompt (sharing one path field) and the unsaved-changes
    // exit confirmation.
    val (showSaveAs, setShowSaveAs, _)   = useState(false)
    val (showOpen, setShowOpen, _)       = useState(false)
    val (showDiscard, setShowDiscard, _) = useState(false)
    val (pathInput, setPathInput, _)     = useState("")

    // The document is dirty when the editor text differs from what was last saved or loaded.
    val dirty    = source != savedText
    val fileName = currentFile.map(_.getName).getOrElse("Untitled")

    // The close handler is installed once but consulted at close time, so it reads the live dirty
    // flag and the runtime's "proceed" thunk through refs kept current each render.
    val dirtyRef = useRef(dirty)
    dirtyRef.current = dirty
    // What to run once the user agrees to discard unsaved changes — quit (window close) or load
    // another file (Open). `requestDiscard` gates an action behind the confirmation when dirty.
    val discardAction = useRef[() => Unit](() => ())
    def requestDiscard(action: () => Unit): Unit =
      if dirty then { discardAction.current = action; setShowDiscard(true) }
      else action()
    // Set while a Save As is the "Save" step of the discard guard, so the guarded action runs once
    // the save completes (and is cleared if that Save As is cancelled).
    val pendingContinue = useRef(false)

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

    // --- file operations -----------------------------------------------------

    def writeTo(file: File): Unit =
      val fos = new FileOutputStream(file)
      try fos.write(source.getBytes("UTF-8"))
      finally fos.close()
      setSavedText(source)
      setCurrentFile(Some(file))

    // Save writes straight to the current file; with no file yet it falls back to Save As.
    def doSave(): Unit =
      currentFile match
        case Some(f) => writeTo(f)
        case None    => { setPathInput(""); setShowSaveAs(true) }

    def doSaveAs(): Unit =
      val p = pathInput.trim
      if p.nonEmpty then
        writeTo(new File(p))
        setShowSaveAs(false)
        // If this save was the "Save" step of a discard guard, continue the guarded action now.
        if pendingContinue.current then { pendingContinue.current = false; discardAction.current() }

    def doOpen(): Unit =
      val p = pathInput.trim
      if p.nonEmpty then
        val f = new File(p)
        if Files.exists(f.toPath) then
          val text = new String(Files.readAllBytes(f.toPath), "UTF-8")
          setSource(text)
          setSavedText(text)
          setCurrentFile(Some(f))
          setShowOpen(false)

    def openPathModal(setShow: Boolean => Unit): Unit =
      setPathInput(currentFile.map(_.getPath).getOrElse(""))
      setShow(true)

    // typeset once on mount so the preview is populated from the start
    useEffect(() => { run(); () => () }, Array())

    // With auto-render on, re-typeset whenever the source changes — and immediately when the
    // toggle is switched on — so the preview tracks every keystroke; with it off, only Run renders.
    useEffect(() => { if autoRender then renderSource(source); () => () }, Array(source, autoRender))

    // re-blit the current pages; the cleanup (run when the page list changes or the app unmounts)
    // frees the surfaces of the run being replaced, after its widgets have already been removed
    useEffect(() => { pages.foreach(_.handle.repaint()); () => pages.foreach(_.surf.destroy()) }, Array(pages))

    // Guard the window close: with unsaved changes, raise the confirmation modal and stash the
    // runtime's proceed thunk to run only if the user confirms; otherwise let the close through.
    useEffect(
      () => {
        WindowControl.onCloseRequest = proceed =>
          if dirtyRef.current then { discardAction.current = proceed; setShowDiscard(true) }
          else proceed()
        () => { WindowControl.onCloseRequest = p => p() }
      },
      Array(),
    )

    // The title bar shows the document name, with a leading * while there are unsaved edits.
    useEffect(
      () => { WindowControl.setTitle(s"${if dirty then "*" else ""}$fileName — Scriptura"); () => () },
      Array(fileName, dirty),
    )

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

    // The toolbar at the top of the editor pane — run, live-render, and the file actions. The
    // document's name and saved state live in the window title (a leading * means unsaved).
    val toolbar: VNode =
      row(crossAxisAlignment = CrossAxisAlignment.Center, spacing = 10)(
        Button("Run", () => run()),
        Switch(autoRender, setAutoRender),
        text("Auto-render", color = theme.surfaceText),
        Button("Open", () => requestDiscard(() => openPathModal(setShowOpen))),
        Button("Save", () => doSave()),
        Button("Save As", () => openPathModal(setShowSaveAs)),
      )

    // A path-prompt modal, shared in shape by Save As and Open (they differ only in title/action).
    def pathModal(title: String, actionLabel: String, action: () => Unit, open: Boolean, close: () => Unit): VNode =
      Dialog(open = open, onClose = close, width = 540)(
        col(crossAxisAlignment = CrossAxisAlignment.Stretch, mainAxisSize = MainAxisSize.Min, spacing = 12)(
          text(title, color = theme.surfaceText, weight = FontWeight.SemiBold),
          text("File path", color = muted),
          TextField(pathInput, setPathInput),
          row(mainAxisAlignment = MainAxisAlignment.End, spacing = 8)(
            Button("Cancel", close),
            Button(actionLabel, action),
          ),
        ),
      )

    // The unsaved-changes guard, shared by window-close and Open. "Save & Continue" opens the Save
    // dialog (so saving is visible and the location is the user's choice); the guarded action runs
    // once that save completes.
    val discardModal: VNode =
      Dialog(open = showDiscard, onClose = () => setShowDiscard(false), width = 460)(
        col(crossAxisAlignment = CrossAxisAlignment.Stretch, mainAxisSize = MainAxisSize.Min, spacing = 12)(
          text("Unsaved changes", color = theme.surfaceText, weight = FontWeight.SemiBold),
          text("The document has unsaved changes. Continue and discard them?", color = theme.surfaceText, maxLines = 0),
          row(mainAxisAlignment = MainAxisAlignment.End, spacing = 8)(
            Seq(
              Button("Save & Continue", () => { setShowDiscard(false); pendingContinue.current = true; openPathModal(setShowSaveAs) }),
              Button("Discard & Continue", () => { setShowDiscard(false); discardAction.current() }),
              Button("Cancel", () => setShowDiscard(false)),
            )*,
          ),
        ),
      )

    ThemeProvider(theme)(
      box(bg = theme.background, padding = EdgeInsets.all(8))(
        // The splitter fills the window; the modals sit alongside it in the stack, taking no space
        // until opened (a closed Dialog renders nothing), then portal over the whole window.
        stack(Alignment.topLeft)(
          splitter(axis = Axis.Horizontal, initial = 0.4)(
            padding(EdgeInsets(top = 0, right = 8, bottom = 0, left = 0))(
              col(crossAxisAlignment = CrossAxisAlignment.Stretch, spacing = 8)(
                toolbar,
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
              ),
            ),
            // preview pane: the typeset pages on a neutral backdrop, scrolling both ways so a page
            // keeps its true size; a danger badge floats over the last good render on an error.
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
          pathModal("Save As", "Save", () => doSaveAs(), showSaveAs, () => { setShowSaveAs(false); pendingContinue.current = false }),
          pathModal("Open file", "Open", () => doOpen(), showOpen, () => setShowOpen(false)),
          discardModal,
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
  val init =
    c.input match
      case Some(file) if Files.exists(file.toPath) =>
        Init(new String(Files.readAllBytes(file.toPath), "UTF-8"), Some(file))
      case _ => Init(SampleSource, None)

  Suit.run("Scriptura", 1600, 1000)(App(init))
