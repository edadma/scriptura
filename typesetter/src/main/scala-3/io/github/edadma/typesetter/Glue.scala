package io.github.edadma.typesetter

class Glue(val naturalSize: Double, val stretch: Double = 0, val shrink: Double = 0, val order: Int = 0)
    extends SpaceBox:

  val descent: Double = naturalSize
  val height: Double = naturalSize
  val width: Double = naturalSize // Initially, it's the natural width
  val xAdvance: Double = naturalSize // Same as width initially

  def -(amount: Double): Glue = Glue(naturalSize - amount, stretch, shrink, order)
  def +(amount: Double): Glue = Glue(naturalSize + amount, stretch, shrink, order)
  def *(amount: Double): Glue = Glue(naturalSize * amount, stretch, shrink, order)

  override def toString: String = s"$Glue(naturalSize=$naturalSize, stretch=$stretch, shrink=$shrink, order=$order)"
end Glue

val FilGlue = Glue(0, 1, 0, 1)

val ZeroGlue = Glue(0, 0, 0, 0)
