package io.github.edadma.typesetter

class VBoxBuilder(protected val t: Typesetter, protected val toSize: Double | Null = null) extends ListBoxBuilder:

  protected val measure: Box => Double = _.height
  protected val skip: Double => Box = VSpaceBox(_)
  protected val wrap: Seq[Box] => Box = VBox(_)

  protected[typesetter] var firstParagraph: Boolean = true

  override infix def add(box: Box): Unit =
    if box.typ == Type.Start then paragraph add box
    else
      if nonEmpty && !last.isSpace then
        val baselineskip = t.getGlue("baselineskip") - last.descent - box.ascent
        val skip =
          if baselineskip.naturalSize <= t.getNumber("lineskiplimit") then t.getGlue("lineskip") else baselineskip

        add(skip)
//        firstLine = false

      super.add(box)

  def paragraph: ParagraphMode =
    val paragraphMode = new ParagraphMode(t, this)

    t.modeStack push paragraphMode

    if t.indentParagraph && !firstParagraph then paragraphMode add HSpaceBox(t.getNumber("parindent"))
    else firstParagraph = false

    paragraphMode
