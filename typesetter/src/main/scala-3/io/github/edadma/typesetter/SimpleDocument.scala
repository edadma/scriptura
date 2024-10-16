package io.github.edadma.typesetter

class SimpleDocument extends Document:
  def init(): Unit = ()

  def layout(b: Box): Box = b

  infix def add(box: Box): Unit =
    t.createPageTarget(t.getNumber("pagewidth"), t.getNumber("pageheight"))
    pages += t.renderToTarget(
      layout(box),
      t.getNumber("hoffset"),
      t.getNumber("voffset"),
    )
    t.ejectPageTarget()
    page += 1
