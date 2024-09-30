package io.github.edadma.typesetter

abstract class HorizontalMode extends Mode:
  protected def addBox(box: Box): Unit

  protected def nonEmpty: Boolean

  protected def last: Box

  protected def length: Int

  protected def update(index: Int, elem: Box): Unit

  infix def add(box: Box): Unit =
    if nonEmpty then
      (last, box) match
        case (_: SpaceBox, _)                 => addBox(box)
        case (_, b: CharBox) if b.text == "," => addBox(box)
        case (l: CharBox, b: CharBox)
            if b.text.nonEmpty && !l.text.endsWith(",") && !l.text
              .endsWith(")") && !b.text.startsWith("(") && (!l.text
              .exists(_.isLetterOrDigit) || !b.text.exists(_.isLetterOrDigit)) =>
          update(length - 1, l.newCharBox(l.text ++ b.text))
        case (b: CharBox, _)
            if b.text.nonEmpty &&
              !(b.text.last == '.' && Abbreviation(b.text.dropRight(1))) &&
              ".!?:;".contains(b.text.last) =>
          addBox(new HSpaceBox(0, comp.currentFont.space * 1.5))
          addBox(box)
        case _ =>
          addBox(new HSpaceBox(0, comp.currentFont.space))
          addBox(box)
    else addBox(box)
  end add
