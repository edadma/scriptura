package io.github.edadma.typesetter

import scala.collection.mutable.ArrayBuffer

class SimpleDocumentMode(protected val t: Typesetter) extends DocumentMode:
  def add(box: Box): Unit = ()

  override def done(): Unit = ()
