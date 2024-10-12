package io.github.edadma.typesetter

import scala.collection.mutable.ArrayBuffer
import scala.compiletime.uninitialized

abstract class Document extends Mode:
  private[typesetter] var ts: Typesetter = uninitialized

  def t: Typesetter = ts

  def init(): Unit

  def add(box: Box): Unit

  override def done(): Unit = pop

  def result: Box = ???
