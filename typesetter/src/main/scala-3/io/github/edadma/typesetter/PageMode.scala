package io.github.edadma.typesetter

class PageMode(protected val t: Typesetter, document: Mode) extends Mode:
  protected[typesetter] var firstParagraph: Boolean = true

  private val pageHeight = t.getNumber("vsize")

  protected[typesetter] var page: VBoxBuilder = new VBoxBuilder(t, pageHeight)

  infix def add(box: Box): Unit =
    if box.typ == Type.Start then start add box
    else addLine(box)

  infix def addLine(box: Box): Unit =
    if page.size + box.height > pageHeight then
      page.lastOption foreach { // todo: only internally generated interline spacing should be removed
        case _: VSpaceBox => page.removeLast()
        case _            =>
      }
      document add page.result
      page = new VBoxBuilder(t, pageHeight)
    end if

    // todo: only internally generated interline spacing should be removed
    if page.nonEmpty || !box.isInstanceOf[VSpaceBox] then page add box

  def result: Box = page.result

  def start: ParagraphMode =
    val paragraphMode = new ParagraphMode(t, this)

    t.modeStack push paragraphMode

    if t.indentParagraph && !firstParagraph then paragraphMode add HSpaceBox(t.getNumber("parindent"))
    else firstParagraph = false

    paragraphMode
