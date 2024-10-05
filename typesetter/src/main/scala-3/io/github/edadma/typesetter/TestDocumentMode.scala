package io.github.edadma.typesetter

class TestDocumentMode(protected val t: Typesetter) extends DocumentMode:
  def add(box: Box): Mode =
    t.document add box
    this

  override def done(): Unit = pop
