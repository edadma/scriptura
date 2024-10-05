package io.github.edadma.typesetter

abstract class Document:
  protected val t: Typesetter
  var pages = new ArrayBufer[Box]

  infix def add(box: Box): Unit = pages += box

  def init(): Unit
