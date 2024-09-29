package io.github.edadma.typesetter

abstract class GlueBox(val naturalWidth: Double, val stretch: Double, val shrink: Double) extends Box:
  val order: Int
  val name: String

  val ascent: Double = 0
  val descent: Double = 0
  val height: Double = 0
  val width: Double = naturalWidth // Initially, it's the natural width
  val xAdvance: Double = naturalWidth // Same as width initially

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    // No drawing needed for glue
    ()

  override def toString: String = s"${name}Glue(naturalWidth=$naturalWidth, stretch=$stretch, shrink=$shrink)"

class OrdinaryGlueBox(naturalWidth: Double, stretch: Double = 0, shrink: Double = 0)
    extends GlueBox(naturalWidth, stretch, shrink):
  val order: Int = 0
  val name: String = "Ordinary"

class FilGlueBox(naturalWidth: Double = 0, stretch: Double = 1, shrink: Double = 1)
    extends GlueBox(naturalWidth, stretch, shrink):
  val order: Int = 1
  val name: String = "Fil"

class FillGlueBox(naturalWidth: Double = 0, stretch: Double = 1, shrink: Double = 1)
    extends GlueBox(naturalWidth, stretch, shrink):
  val order: Int = 2
  val name: String = "Fill"

class FilllGlueBox(naturalWidth: Double = 0, stretch: Double = 1, shrink: Double = 1)
    extends GlueBox(naturalWidth, stretch, shrink):
  val order: Int = 3
  val name: String = "Filll"
