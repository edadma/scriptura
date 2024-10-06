package io.github.edadma.typesetter

class VSpaceBox(val height: Double) extends SpaceBox:

  val descent: Double = height
  val width: Double = 0
  val xAdvance: Double = 0

  override def toString: String = s"VSpaceBox(height=$height)"
