package io.github.edadma.typesetter

import scala.collection.mutable.ArrayBuffer

abstract class DocumentMode extends Mode:
  protected val t: Typesetter

  def result: Box = ???
