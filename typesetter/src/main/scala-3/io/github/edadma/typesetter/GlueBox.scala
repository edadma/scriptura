package io.github.edadma.typesetter

enum GlueType(val order: Int, val name: String):
  case Ordinary extends GlueType(0, "Ordinary")
  case Fil extends GlueType(1, "Fil")
  case Fill extends GlueType(2, "Fill")
  case Filll extends GlueType(3, "Filll")

abstract class GlueBox(val naturalWidth: Double, val stretch: Double, val shrink: Double) extends Box:

  val typ: GlueType

  val ascent: Double = 0
  val descent: Double = 0
  val height: Double = 0
  val width: Double = naturalWidth // Initially, it's the natural width
  val xAdvance: Double = naturalWidth // Same as width initially

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    // No drawing needed for glue
    ()

  override def toString: String = s"${typ.name}Box(naturalWidth=$naturalWidth, stretch=$stretch, shrink=$shrink)"

class OrdinaryGlueBox(naturalWidth: Double, stretch: Double = 0, shrink: Double = 0)
    extends GlueBox(naturalWidth, stretch, shrink):
  val typ: GlueType = GlueType.Ordinary

class FilGlueBox(naturalWidth: Double = 0) extends GlueBox(naturalWidth, 1, 1):
  val typ: GlueType = GlueType.Fil

class FillGlueBox(naturalWidth: Double = 0) extends GlueBox(naturalWidth, 1, 1):
  val typ: GlueType = GlueType.Fill

class FilllGlueBox(naturalWidth: Double = 0) extends GlueBox(naturalWidth, 1, 1):
  val typ: GlueType = GlueType.Filll
