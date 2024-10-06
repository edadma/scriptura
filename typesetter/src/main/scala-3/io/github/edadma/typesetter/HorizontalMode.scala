package io.github.edadma.typesetter

trait HorizontalMode extends Builder:
  override infix def add(box: Box): Unit =
    if nonEmpty then
      (last, box) match
        case (_: SpaceBox, _)                 => super.add(box)
        case (_, b: CharBox) if b.text == "," => super.add(box)
        case (l: CharBox, b: CharBox)
            if b.text.nonEmpty && !l.text.endsWith(",") && !l.text
              .endsWith(")") && !b.text.startsWith("(") && (!l.text
              .exists(_.isLetterOrDigit) || !b.text.exists(_.isLetterOrDigit)) =>
          update(length - 1, l.newCharBox(l.text ++ b.text))
        case (b: CharBox, _)
            if b.text.nonEmpty &&
              !(b.text.last == '.' && Abbreviation(b.text.dropRight(1))) &&
              ".!?:;".contains(b.text.last) =>
          super.add(t.getGlue("xspaceskip"))
          super.add(box)
        case _ =>
          super.add(t.getGlue("spaceskip"))
          super.add(box)
    else super.add(box)
