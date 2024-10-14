package io.github.edadma.typesetter

class TestDocument extends Document:
  def init(): Unit = ()
//    t.set("pagewidth", 400)

  def page(b: Box): Box = b
