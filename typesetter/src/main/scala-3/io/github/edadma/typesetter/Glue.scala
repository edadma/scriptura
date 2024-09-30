package io.github.edadma.typesetter

class Glue(val naturalSize: Double, val stretch: Double = 0, val shrink: Double = 0, val order: Int = 0) extends Box:

  val name: String =
    order match
      case 0 => "Ordinary"
      case 1 => "Fil"
      case 2 => "Fill"
      case 3 => "Filll"

  val ascent: Double = 0
  val descent: Double = 0
  val height: Double = naturalSize
  val width: Double = naturalSize // Initially, it's the natural width
  val xAdvance: Double = naturalSize // Same as width initially

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    // No drawing needed for glue
    ()

  override def toString: String = s"${name}Glue(naturalSize=$naturalSize, stretch=$stretch, shrink=$shrink)"
end Glue

val FilGlue = Glue(0, 1, 0, 1)

val ZeroGlue = Glue(0, 0, 0, 0)
