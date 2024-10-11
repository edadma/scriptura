package io.github.edadma.typesetter

class PageMode(t: Typesetter) extends VBoxBuilder(t):
  private lazy val pageHeight = t.getNumber("vsize")

  override infix def add(box: Box): Unit =
    if size + box.height > pageHeight then
      lastOption foreach { // todo: only internally generated interline spacing should be removed
        case _: VSpaceBox => removeLast()
        case _            =>
      }
      t.modeStack(1) add result
      clear()
    end if

    super.add(box)

  override def result: Box = wrap(buildTo(pageHeight))
