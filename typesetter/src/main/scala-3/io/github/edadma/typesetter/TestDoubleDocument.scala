package io.github.edadma.typesetter

class TestDoubleDocument extends Document:
  def init(): Unit = {}

  def page(b: Box): Box = b

  override infix def add(box: Box): Unit = ()
