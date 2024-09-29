package io.github.edadma.typesetter

class VBox(val boxes: List[Box]) extends Box:

  val width: Double = if boxes.isEmpty then 0 else boxes.map(_.width).max
  val height: Double = boxes.map(_.height).sum
  val descent: Double = if boxes.isEmpty then 0 else boxes.last.descent
  val ascent: Double = height - descent
  val xAdvance: Double = width

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    box(t, x, y)

    var currentY = if boxes.isEmpty then y else y - ascent + boxes.head.ascent

    for box <- boxes do
      box.draw(t, x, currentY)
      currentY += box.height

  override def toString: String =
    s"VBox(width=$width, height=$height, boxes=$boxes)"
