package io.github.edadma.typesetter

class HSkipBox(val width: Double) extends SkipBox:

  val height: Double = 0
  val xAdvance: Double = width

  override def toString: String = s"HSkipBox(width=$width)"
