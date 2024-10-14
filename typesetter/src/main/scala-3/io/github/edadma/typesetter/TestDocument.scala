package io.github.edadma.typesetter

import scala.collection.mutable.ArrayBuffer

class TestDocument(inset: Int = 0) extends Document:
  val pages = new ArrayBuffer[Any]

  def init(): Unit = ()

  def nonEmpty: Boolean = ???

  override def done(): Unit = pop

  def add(box: Box): Unit =
    val pagebox = box

    pages += t.render(pagebox, t.getNumber("hsize"), t.getNumber("vsize"))
