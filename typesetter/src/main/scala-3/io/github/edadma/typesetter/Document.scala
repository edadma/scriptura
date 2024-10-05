package io.github.edadma.typesetter

import scala.collection.mutable.ArrayBuffer
import scala.compiletime.uninitialized

abstract class Document:
  private[typesetter] var t: Typesetter = uninitialized
  var pages = new ArrayBuffer[Box]

  infix def add(box: Box): Unit = pages += box

  def init(): Unit
