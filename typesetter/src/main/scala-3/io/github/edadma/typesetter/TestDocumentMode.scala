package io.github.edadma.typesetter

class TestDocumentMode(protected val t: Typesetter) extends DocumentMode:
  def add(box: Box): Unit = t.document add box

  override def done(): Unit = ()
