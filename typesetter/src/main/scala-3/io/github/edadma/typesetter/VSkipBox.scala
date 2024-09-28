package io.github.edadma.typesetter

class VSkipBox(val height: Double) extends Box:

  val ascent: Double = 0
  val descent: Double = height
  val width: Double = 0
  val xAdvance: Double = 0

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    // No drawing needed for a rigid box (invisible)
    ()

  override def toString: String = s"VSkipBox(height=$height)"
