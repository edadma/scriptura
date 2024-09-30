package io.github.edadma.typesetter

class VSpaceBox(val height: Double) extends SpaceBox:

  val descent: Double = height
  val width: Double = 0
  val xAdvance: Double = 0

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    // No drawing needed for a rigid box (invisible)
    ()

  override def toString: String = s"VSpaceBox(height=$height)"
