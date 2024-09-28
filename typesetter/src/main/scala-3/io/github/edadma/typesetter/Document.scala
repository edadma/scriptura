package io.github.edadma.typesetter

import scala.compiletime.uninitialized

abstract class Document:
  protected var t: Typesetter = uninitialized

  def setTypesetter(typesetter: Typesetter): Unit = t = typesetter
