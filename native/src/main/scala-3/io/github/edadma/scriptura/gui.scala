package io.github.edadma.scriptura

import io.github.edadma.suit.*
import io.github.edadma.suit.dsl.*
import io.github.edadma.suit.widgets.*
import io.github.edadma.libcairo.Surface
import io.github.edadma.texish.{CairoImageTypesetter, CairoPDFTypesetter, Color as TexColor}
import io.github.edadma.texish.parser.{Processor, TypesetterHandler, registerTypesettingPrimitives}

import java.io.{ByteArrayOutputStream, File, FileOutputStream}
import java.nio.file.{Files, Paths}

import scala.scalanative.unsafe.*

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

/** The zoom levels the preview offers, as `(factor, label)`. The factor is carried as its own text so
  * the value round-trips through the `Select` exactly, with no float formatting to agree on. 100% is
  * the page at its true size: one point becomes `ScreenDpi / 72` logical pixels.
  */
private[scriptura] val ZoomLevels: Seq[(String, String)] =
  Seq("0.5" -> "50%", "0.75" -> "75%", "1" -> "100%", "1.25" -> "125%", "1.5" -> "150%", "2" -> "200%")

private[scriptura] val DefaultZoom = "1"

/** How long the editor must be still before auto-render typesets it. Long enough that a burst of
  * typing collapses into one render, short enough to still feel live.
  */
private val AutoRenderDelayMs = 250

/** The preview column's geometry, which the page offsets below are computed from: the padding around
  * the whole column, the gap between pages, and each page's border.
  */
private val PreviewPad      = 8.0
private val PageGap         = 12.0
private val PageBorderWidth = 1.0

/** The source position a texish diagnostic points at, as a 1-based `(line, column)`. The engine
  * prints it in the message itself — "Unknown command: \TeX (line 7, column 118)" — so the first
  * such position in the log is the one to jump to. `None` when the log carries no position (an
  * ordinary message, or a failure that happened outside the document).
  */
private[scriptura] def errorPosition(log: String): Option[(Int, Int)] =
  """\(line (\d+), column (\d+)\)""".r.findFirstMatchIn(log).map(m => (m.group(1).toInt, m.group(2).toInt))

/** The character offset of a 1-based `(line, column)` in `text`, clamped to the text — a position
  * from a stale diagnostic may point past an edited source, and that should land at the end rather
  * than throw. The column is clamped within its own line, so it cannot spill onto the next one.
  */
private[scriptura] def offsetOf(text: String, line: Int, column: Int): Int =
  val lines = text.linesWithSeparators.toVector

  if lines.isEmpty then 0
  else
    val li      = math.max(0, math.min(line - 1, lines.length - 1))
    val base    = lines.take(li).map(_.length).sum
    val lineLen = lines(li).stripLineEnd.length
    math.max(0, math.min(text.length, base + math.max(0, math.min(column - 1, lineLen))))

/** The y offset of every page's top within the preview column, from the page heights. The column
  * pads its start, and each page adds its own border above and below plus the gap to the next.
  */
private[scriptura] def pageTops(heights: Seq[Double]): Vector[Double] =
  heights.scanLeft(PreviewPad)((y, h) => y + h + 2 * PageBorderWidth + PageGap).toVector.init

/** Which page a scroll offset is showing: the last one whose top has reached the top of the
  * viewport. The tolerance absorbs the fractional offsets a wheel leaves behind.
  */
private[scriptura] def pageAt(tops: Seq[Double], offset: Double): Int =
  if tops.isEmpty then 0 else math.max(0, tops.lastIndexWhere(_ <= offset + 1.0))

/** Typeset the editor's source into a list of page surfaces, capturing whatever the engine prints
  * (and any failure's stack trace) as the message log. The returned surfaces belong to the caller.
  * The boolean is whether typesetting succeeded: on failure the page list is empty and the caller
  * keeps showing its last good render rather than blanking the preview.
  *
  * `pageColor` paints the page and `ink` is the default pen, so a dark-scheme preview passes a dark
  * page with light ink; any author-specified `\color` survives unchanged. The defaults are the
  * white paper and black ink that print output expects.
  *
  * `zoom` scales the device resolution rather than the finished image, so the engine lays the page
  * out afresh at the larger size and the glyphs are drawn — not magnified — at every level.
  */
private[scriptura] def typeset(
    source:    String,
    pageColor: TexColor = TexColor("white"),
    ink:       TexColor = TexColor("black"),
    baseDir:   String = ".",
    zoom:      Double = 1.0,
): (Vector[Page], String, Boolean) =
  val scale = { val s = DevicePixelRatio.scaleX; if s <= 0 then 1.0 else s }
  val dpi   = ScreenDpi * scale * zoom
  val out   = new ByteArrayOutputStream

  // Hyphenation is the document's choice, not the previewer's: a source selects a language with
  // \usehyphenation{en-us} (or \loadhyphenation for an unbundled one), exactly as it does under the
  // texish CLI. Forcing a language here would make the preview disagree with the rendered output.
  try
    val t       = new CairoImageTypesetter(dpi)
    t.backgroundColor = pageColor
    t.currentColor    = ink
    val handler = new TypesetterHandler(t)
    val proc    = new Processor(handler)

    registerTypesettingPrimitives(proc, handler)
    proc.setBaseDir(baseDir)

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

/** Typeset the source to a print-quality PDF at `path`: a white page with black ink — the colours
  * print output expects, independent of the screen theme. Returns the engine's captured output and
  * whether it succeeded, so the caller can surface warnings or a failure in the log.
  */
private[scriptura] def typesetPdf(source: String, path: String, baseDir: String = "."): (String, Boolean) =
  val out = new ByteArrayOutputStream

  try
    val t       = new CairoPDFTypesetter(path)
    val handler = new TypesetterHandler(t)
    val proc    = new Processor(handler)

    registerTypesettingPrimitives(proc, handler)
    proc.setBaseDir(baseDir)

    Console.withOut(out) {
      proc.process(source)
      t.end()
    }
    t.destroy()
    (out.toString, true)
  catch
    case err: Throwable =>
      (out.toString + Option(err.getMessage).getOrElse(err.toString), false)

/** A C `system` binding, used to launch the platform's file opener. */
@extern
private object Libc:
  def system(command: CString): CInt = extern

/** The CUPS print destinations (`lpstat -e`), one per line, or empty if none are configured or the
  * query fails. Output is captured through a temp file since the launcher cannot pipe it back.
  */
private def listPrinters(): List[String] =
  val tmp = Paths.get(System.getProperty("java.io.tmpdir"), "scriptura-printers.txt").toString
  Zone(Libc.system(toCString(s"lpstat -e > '$tmp' 2>/dev/null")))
  try
    val src = scala.io.Source.fromFile(tmp)
    try src.getLines().map(_.trim).filter(_.nonEmpty).toList
    finally src.close()
  catch case _: Throwable => Nil

/** Send a file to a named CUPS destination via `lp`; returns the launcher exit status (0 on success).
  * A named queue prints over the network the same as a local one — CUPS hides the difference. */
private def lpPrint(path: String, printer: String): Int =
  Zone(Libc.system(toCString(s"lp -d '$printer' '$path'")))

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
    // The active theme lives in state so the Theme menu can switch it; the window opens dark.
    val (theme, setTheme, _)             = useState[Theme](Theme.dark)
    val muted                            = Color.lerp(theme.surfaceText, theme.surface, 0.4)

    // The preview follows the theme. A dark theme renders the page itself dark with light ink (the
    // engine paints `docPage` and uses `docInk` as the default pen, so author-set colours survive),
    // and floats it on a dark backdrop; a light theme keeps black-on-white paper on a neutral grey.
    val docPage    = if theme.isDark then TexColor("#24282c") else TexColor("white")
    val docInk     = if theme.isDark then TexColor("#e9ecef") else TexColor("black")
    val backdrop   = if theme.isDark then theme.background else Color.rgb(0x9aa0a6)
    val pageBorder = if theme.isDark then theme.border else Color.rgb(0x333333)
    val (source, setSource, _)           = useState(init.text)
    val (savedText, setSavedText, _)     = useState(init.text)
    val (currentFile, setCurrentFile, _) = useState(init.file)
    val (pages, setPages, _)             = useState(Vector.empty[Page])
    val (logText, setLog, _)             = useState("")
    val (autoRender, setAutoRender, _)   = useState(true)
    val (hasError, setError, _)          = useState(false)
    val (zoomKey, setZoomKey, _)         = useState(DefaultZoom)

    val zoom = zoomKey.toDoubleOption.getOrElse(1.0)

    // The editor's viewport and a pending caret placement, so a diagnostic can be jumped to: the
    // request moves the caret, and the widget reports back where it landed so the viewport can
    // bring it into view. The token makes a repeat jump to the same spot take effect again.
    val editorRef                          = useRef[RenderObject | Null](null)
    val (caretReq, setCaretReq, _)         = useState(Option.empty[CaretRequest])
    val (jumpCount, setJumpCount, _)       = useState(0L)

    // The preview's viewport and the page it is showing, tracked from its scroll offset so the
    // indicator follows a wheel or a bar drag as well as the page buttons.
    val previewRef                         = useRef[RenderObject | Null](null)
    val (currentPage, setCurrentPage, _)   = useState(0)

    // Modal state: the Save-As / Open path prompt (sharing one path field) and the unsaved-changes
    // exit confirmation.
    val (showSaveAs, setShowSaveAs, _)   = useState(false)
    val (showOpen, setShowOpen, _)       = useState(false)
    val (showDiscard, setShowDiscard, _) = useState(false)
    val (pathInput, setPathInput, _)     = useState("")

    // Print: the chosen CUPS destination (remembered after the first pick, so Print is one click
    // thereafter), the printer-picker modal and the destinations it lists, and the rendered PDF the
    // pick will send.
    val (printer, setPrinter, _)         = useState(Option.empty[String])
    val (showPrint, setShowPrint, _)     = useState(false)
    val (printers, setPrinters, _)       = useState(List.empty[String])
    val printPdfRef                      = useRef("")

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
      // Resolve a document's relative \use and \include against its own directory (else the cwd).
      val baseDir       = currentFile.flatMap(f => Option(f.getAbsoluteFile.getParent)).getOrElse(".")
      val (ps, log, ok) = typeset(text, docPage, docInk, baseDir, zoom)
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

    // Print: typeset a print-quality PDF (white page, black ink, independent of the screen theme)
    // into a temp file named after the document, ready for a destination to be chosen. Returns
    // whether it succeeded; a failure (or engine warnings) goes to the log.
    def renderPdfForPrint(): Boolean =
      val base = currentFile.map(_.getName.replaceFirst("\\.[^.]*$", "")).getOrElse("document")
      val pdf  = Paths.get(System.getProperty("java.io.tmpdir"), s"$base.pdf").toString
      val baseDir    = currentFile.flatMap(f => Option(f.getAbsoluteFile.getParent)).getOrElse(".")
      val (elog, ok) = typesetPdf(source, pdf, baseDir)
      if !ok then { setLog(s"Print failed:\n$elog"); false }
      else { printPdfRef.current = pdf; if elog.nonEmpty then setLog(elog); true }

    // Send the rendered PDF to a destination, remembering it as the printer for next time.
    def sendTo(p: String): Unit =
      val rc   = lpPrint(printPdfRef.current, p)
      val name = p.replace('_', ' ')
      setPrinter(Some(p))
      setLog(if rc == 0 then s"Sent to $name." else s"Could not print to $name (lp status $rc).")

    def openPrinterPicker(): Unit =
      val ps = listPrinters()
      if ps.isEmpty then setLog("No printers found. Add one in System Settings ▸ Printers & Scanners.")
      else { setPrinters(ps); setShowPrint(true) }

    // The Print button: render, then send straight to the remembered printer, or raise the picker
    // the first time (or whenever none is remembered).
    def doPrint(): Unit =
      if renderPdfForPrint() then
        printer match
          case Some(p) => sendTo(p)
          case None    => openPrinterPicker()

    // File ▸ Print to… always raises the picker, so the destination can be changed.
    def doChoosePrinter(): Unit =
      if renderPdfForPrint() then openPrinterPicker()

    // --- jumping to a diagnostic ---------------------------------------------

    // Where in the source the current log points, if anywhere — also what decides whether the log
    // pane offers itself as a target to click.
    val errorOffset: Option[Int] =
      errorPosition(logText).map((line, col) => offsetOf(source, line, col))

    /** Put the caret on the position the log names. The editor answers through `onCaretAt` below,
      * which is what actually scrolls it into view — only the editor knows which wrapped row an
      * offset falls on.
      */
    def jumpToError(): Unit =
      errorOffset.foreach { off =>
        setJumpCount(jumpCount + 1)
        setCaretReq(Some(CaretRequest(off, jumpCount + 1)))
      }

    /** Scroll the editor so a caret at `top` (of height `h`) is inside the viewport, moving only
      * when it is actually out of view, and leaving a line of margin so it does not sit against
      * the edge.
      */
    def revealCaret(top: Double, h: Double): Unit =
      editorRef.current match
        case r: RenderScroll =>
          val margin = h * 2
          val view   = r.size.height
          if top - margin < r.scrollOffset then r.scrollOffset = top - margin
          else if top + h + margin > r.scrollOffset + view then r.scrollOffset = top + h + margin - view
        case _ => ()

    // --- page navigation -----------------------------------------------------

    val tops     = pageTops(pages.map(_.h))
    val pageCount = pages.length

    /** Scroll the preview to a page's top, clamped to the pages that exist. The scroll reports back
      * through `onScroll`, which is what updates the indicator — so the two cannot disagree.
      */
    def goToPage(i: Int): Unit =
      if tops.nonEmpty then
        val target = tops(math.max(0, math.min(i, tops.length - 1)))
        previewRef.current match
          case r: RenderScroll => r.scrollByY(target - r.offsetY): Unit
          case _               => ()

    // typeset once on mount so the preview is populated from the start
    useEffect(() => { run(); () => () }, Array())

    // With auto-render on, re-typeset as the source changes — and immediately when the toggle is
    // switched on; with it off, only Run renders. The source is debounced first: typesetting is a
    // whole-document job, so driving it from every keystroke makes a document of any size stutter
    // under the typing. Settling for a beat means one render per pause instead of one per key.
    val debouncedSource = useDebouncedValue(source, AutoRenderDelayMs)
    useEffect(
      () => { if autoRender then renderSource(debouncedSource); () => () },
      Array(debouncedSource, autoRender),
    )

    // When the theme flips between light and dark, re-typeset so the page colours follow it at once
    // — regardless of auto-render. The first mount is skipped; the initial render already ran.
    val themedOnce = useRef(false)
    useEffect(
      () => { if themedOnce.current then renderSource(source) else themedOnce.current = true; () => () },
      Array(theme.isDark),
    )

    // Changing the zoom re-typesets at the new resolution — regardless of auto-render, since the
    // pages on screen are the wrong size until it does. The first mount is skipped, as above.
    val zoomedOnce = useRef(false)
    useEffect(
      () => { if zoomedOnce.current then renderSource(source) else zoomedOnce.current = true; () => () },
      Array(zoomKey),
    )

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
      if pages.isEmpty then Seq(text("No pages — press Run.", color = theme.surfaceText))
      else
        pages.map(p =>
          box(border = pageBorder, borderWidth = PageBorderWidth)(
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

    // The menu bar across the top of the editor pane — the File actions and the Theme switcher.
    // The document's name and saved state live in the window title (a leading * means unsaved).
    val menuBarNode: VNode =
      menuBar(
        menu("File")(close =>
          Seq(
            MenuItem("Open…", () => { requestDiscard(() => openPathModal(setShowOpen)); close() }),
            MenuItem("Save", () => { doSave(); close() }),
            MenuItem("Save As…", () => { openPathModal(setShowSaveAs); close() }),
            MenuItem("Print to…", () => { doChoosePrinter(); close() }),
          ),
        ),
        menu("Theme")(close =>
          Seq(
            MenuItem("Dark", () => { setTheme(Theme.dark); close() }),
            MenuItem("Light", () => { setTheme(Theme.light); close() }),
            MenuItem("Violet Dark", () => { setTheme(Theme.violetDark); close() }),
            MenuItem("Violet Light", () => { setTheme(Theme.violetLight); close() }),
          ),
        ),
      )

    // The toolbar sits just below the menu bar: Run typesets on demand, the toggle drives live
    // rendering.
    val controls: VNode =
      row(crossAxisAlignment = CrossAxisAlignment.Center, spacing = 10)(
        Seq
          .concat(
            Seq(
              Button("Run", () => run()),
              Button("Print", () => doPrint()),
              Switch(autoRender, setAutoRender),
              text("Auto-render", color = theme.surfaceText),
              text("Zoom", color = theme.surfaceText),
              Select(ZoomLevels, zoomKey, setZoomKey, width = 110),
            ),
            // Page navigation, shown only when there is more than one page to move between.
            if pageCount > 1 then
              Seq(
                Button("◀", () => goToPage(currentPage - 1)),
                text(s"Page ${currentPage + 1} of $pageCount", color = theme.surfaceText),
                Button("▶", () => goToPage(currentPage + 1)),
              )
            else Nil,
          )*,
      )

    // A path-prompt modal, shared in shape by Save As and Open (they differ only in title/action).
    def pathModal(title: String, actionLabel: String, action: () => Unit, open: Boolean, close: () => Unit): VNode =
      Dialog(open = open, onClose = close, width = 540)(
        col(crossAxisAlignment = CrossAxisAlignment.Stretch, mainAxisSize = MainAxisSize.Min, spacing = 12)(
          text(title, color = theme.surfaceText, weight = FontWeight.SemiBold),
          text("File path", color = muted),
          TextField(pathInput, setPathInput, onSubmit = () => action()),
          row(mainAxisAlignment = MainAxisAlignment.End, spacing = 8)(
            Button("Cancel", close),
            Button(actionLabel, action),
          ),
        ),
      )

    // The unsaved-changes guard, shared by window-close and Open. "Save & Continue" saves straight
    // to the current file when there is one (no prompt) and continues; for a never-saved document
    // it opens the Save dialog to name the file first, then continues once that save completes.
    def saveThenContinue(): Unit =
      setShowDiscard(false)
      currentFile match
        case Some(f) => writeTo(f); discardAction.current()
        case None    => { pendingContinue.current = true; openPathModal(setShowSaveAs) }

    val discardModal: VNode =
      Dialog(open = showDiscard, onClose = () => setShowDiscard(false), width = 460)(
        col(crossAxisAlignment = CrossAxisAlignment.Stretch, mainAxisSize = MainAxisSize.Min, spacing = 12)(
          text("Unsaved changes", color = theme.surfaceText, weight = FontWeight.SemiBold),
          text("The document has unsaved changes. Continue and discard them?", color = theme.surfaceText, maxLines = 0),
          row(mainAxisAlignment = MainAxisAlignment.End, spacing = 8)(
            Seq(
              Button("Save & Continue", () => saveThenContinue()),
              Button("Discard & Continue", () => { setShowDiscard(false); discardAction.current() }),
              Button("Cancel", () => setShowDiscard(false)),
            )*,
          ),
        ),
      )

    // The printer picker: one button per CUPS destination, choosing one prints to it (and remembers
    // it as the printer). The PDF is already rendered; the buttons only dispatch it.
    val printModal: VNode =
      Dialog(open = showPrint, onClose = () => setShowPrint(false), width = 460)(
        col(crossAxisAlignment = CrossAxisAlignment.Stretch, mainAxisSize = MainAxisSize.Min, spacing = 10)(
          (Seq[VNode](text("Choose a printer", color = theme.surfaceText, weight = FontWeight.SemiBold))
            ++ printers.map(p => Button(p.replace('_', ' '), () => { setShowPrint(false); sendTo(p) }))
            ++ Seq[VNode](
              row(mainAxisAlignment = MainAxisAlignment.End, spacing = 8)(Button("Cancel", () => setShowPrint(false))),
            ))*,
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
                // The menu bar and the toolbar form one tight header block, set close together and
                // held apart from the editor by the column's usual gap.
                col(crossAxisAlignment = CrossAxisAlignment.Stretch, mainAxisSize = MainAxisSize.Min, spacing = 2)(
                  menuBarNode,
                  controls,
                ),
                box(flex = 1, clip = true)(
                  scrollArea(Axis.Vertical, ref = editorRef)(
                    col(crossAxisAlignment = CrossAxisAlignment.Stretch, mainAxisSize = MainAxisSize.Min)(
                      TextArea(source, setSource, caretRequest = caretReq, onCaretAt = revealCaret),
                    ),
                  ),
                ),
                // The log pane. When the message carries a source position, the pane becomes a
                // target: clicking it puts the caret on the offending character, so a diagnostic
                // is one click from the place that caused it.
                sizedBox(height = 140)(
                  box(
                    bg          = theme.surface,
                    border      = if errorOffset.isDefined then theme.accent else theme.border,
                    borderWidth = 1,
                    radius      = theme.radius,
                    clip        = true,
                    padding     = EdgeInsets.all(8),
                    cursor      = if errorOffset.isDefined then Cursor.Pointer else Cursor.Default,
                    onMouseDown = _ => jumpToError(),
                  )(
                    scrollArea(Axis.Vertical)(
                      col(crossAxisAlignment = CrossAxisAlignment.Stretch, mainAxisSize = MainAxisSize.Min, spacing = 4)(
                        Seq
                          .concat(
                            Seq(
                              text(
                                if logText.isEmpty then "(no messages)" else logText,
                                color    = theme.surfaceText,
                                maxLines = 0,
                              ),
                            ),
                            errorPosition(logText).map((line, col) =>
                              text(s"Click to go to line $line, column $col.", color = muted),
                            ),
                          )*,
                      ),
                    ),
                  ),
                ),
              ),
            ),
            // preview pane: the typeset pages on a neutral backdrop, scrolling both ways so a page
            // keeps its true size; a danger badge floats over the last good render on an error.
            box(flex = 1, clip = true, bg = backdrop)(
              stack(Alignment.topLeft)(
                (scrollArea(
                  both     = true,
                  ref      = previewRef,
                  // Only on a real change: the offset ticks continuously through a scroll, and
                  // setting the same page each tick would re-render the window for nothing.
                  onScroll = off => {
                    val p = pageAt(tops, off.y)
                    if p != currentPage then setCurrentPage(p)
                  },
                )(
                  padding(EdgeInsets.all(PreviewPad))(
                    col(
                      crossAxisAlignment = CrossAxisAlignment.Start,
                      mainAxisSize       = MainAxisSize.Min,
                      spacing            = PageGap,
                    )(
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
          printModal,
        ),
      ),
    )
  }

/** A small starter document so the preview has something to show before the user types anything. */
private val SampleSource: String =
  """// \TeX and the other logo macros are defined in the engine's base package.
    |\use{base}
    |
    |Welcome to Scriptura.
    |
    |Edit the source on the left and press Run to typeset the document. Each page is rendered to a Cairo image surface and shown on the right.
    |
    |\vskip 12pt
    |
    |The typesetter breaks paragraphs into lines and lines into pages, justifying the text and balancing the page just as \TeX does.
    |
    |\vfill
    |""".stripMargin

/** Launch the native preview window, seeding the editor with the input file's contents when one was
  * given on the command line, or the sample document otherwise.
  */
def scripturaGui(input: Option[File]): Unit =
  val init =
    input match
      case Some(file) if Files.exists(file.toPath) =>
        Init(new String(Files.readAllBytes(file.toPath), "UTF-8"), Some(file))
      case _ => Init(SampleSource, None)

  Suit.run("Scriptura", 1600, 1000)(App(init))
