package io.github.edadma.typesetter

class PageMode(t: Typesetter, document: Mode) extends VBoxBuilder(t):
  private lazy val pageHeight = t.getNumber("vsize")

  override infix def add(box: Box): Unit =
    if size + box.height > pageHeight then
      lastOption foreach { // todo: only internally generated interline spacing should be removed
        case _: VSpaceBox => removeLast()
        case _            =>
      }
      document add wrap(buildTo(pageHeight))
      clear()
    end if

    // todo: only internally generated interline spacing should be removed
    if nonEmpty || !box.isInstanceOf[VSpaceBox] then super.add(box)
