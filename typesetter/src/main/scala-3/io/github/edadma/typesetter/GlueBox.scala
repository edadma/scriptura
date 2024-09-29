package io.github.edadma.typesetter

abstract class GlueBox(val naturalSize: Double, val stretch: Double, val shrink: Double) extends Box:
  val order: Int
  val name: String

  val ascent: Double = 0
  val descent: Double = 0
  val height: Double = naturalSize
  val width: Double = naturalSize // Initially, it's the natural width
  val xAdvance: Double = naturalSize // Same as width initially

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    // No drawing needed for glue
    ()

  override def toString: String = s"${name}Glue(naturalSize=$naturalSize, stretch=$stretch, shrink=$shrink)"

class OrdinaryGlueBox(naturalSize: Double, stretch: Double = 0, shrink: Double = 0)
    extends GlueBox(naturalSize, stretch, shrink):
  val order: Int = 0
  val name: String = "Ordinary"

  def add(space: Double): OrdinaryGlueBox = OrdinaryGlueBox(naturalSize + space, stretch, shrink)
  def subtract(space: Double): OrdinaryGlueBox = OrdinaryGlueBox(naturalSize - space, stretch, shrink)

class FilGlueBox(naturalSize: Double = 0, stretch: Double = 1, shrink: Double = 1)
    extends GlueBox(naturalSize, stretch, shrink):
  val order: Int = 1
  val name: String = "Fil"

class FillGlueBox(naturalSize: Double = 0, stretch: Double = 1, shrink: Double = 1)
    extends GlueBox(naturalSize, stretch, shrink):
  val order: Int = 2
  val name: String = "Fill"

class FilllGlueBox(naturalSize: Double = 0, stretch: Double = 1, shrink: Double = 1)
    extends GlueBox(naturalSize, stretch, shrink):
  val order: Int = 3
  val name: String = "Filll"
