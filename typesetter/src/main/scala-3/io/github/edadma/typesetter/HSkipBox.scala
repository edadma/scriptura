package io.github.edadma.typesetter

class HSkipBox(val width: Double) extends Box:

  val height: Double = 0
  val ascent: Double = 0
  val descent: Double = 0
  val xAdvance: Double = width

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    box(t, x, y)

  override def toString: String = s"HSkipBox(width=$width)"
