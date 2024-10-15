package io.github.edadma.typesetter

class TestDocument extends SimpleDocument:
  override def init(): Unit =
    t.set(
      Seq("pagewidth" -> 400, "pageheight" -> 400, "hsize" -> 300, "vsize" -> 300, "hoffset" -> 50, "voffset" -> 50),
    )
