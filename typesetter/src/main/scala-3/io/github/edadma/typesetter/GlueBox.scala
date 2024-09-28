package io.github.edadma.typesetter

class GlueBox extends Box:

  val ascent: Double = 0
  val descent: Double = 0
  val height: Double = 0
  val width: Double = 0
  val xAdvance: Double = 0

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    // No drawing needed for glue
    ()

  override def toString: String = "GlueBox()"
