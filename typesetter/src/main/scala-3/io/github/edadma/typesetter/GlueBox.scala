package io.github.edadma.typesetter

class GlueBox(val naturalWidth: Double, val stretch: Double = 0, val shrink: Double = 0) extends Box:

  val ascent: Double = 0
  val descent: Double = 0
  val height: Double = 0
  val width: Double = naturalWidth // Initially, it's the natural width
  val xAdvance: Double = naturalWidth // Same as width initially

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    // No drawing needed for glue
    ()

  override def toString: String = s"GlueBox(naturalWidth=$naturalWidth, stretch=$stretch, shrink=$shrink)"
