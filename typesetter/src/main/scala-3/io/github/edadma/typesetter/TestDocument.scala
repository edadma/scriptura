package io.github.edadma.typesetter

import scala.collection.mutable.ArrayBuffer

class TestDocument extends Document:
  val pages = new ArrayBuffer[Any]

  def init(): Unit = ()

  def nonEmpty: Boolean = ???

  override def done(): Unit = pop

  def add(box: Box): Unit = pages += t.render(box, 5, 5)
