package io.github.edadma.typesetter

class TestDocumentMode(protected val t: Typesetter) extends DocumentMode:
  def add(box: Box): Unit =
    println("added page")
    t.document add box

  override def done(): Unit = ()
