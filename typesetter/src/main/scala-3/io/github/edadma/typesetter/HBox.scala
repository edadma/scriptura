package io.github.edadma.typesetter

class HBox(val boxes: List[Box]) extends Box:

  val width: Double = boxes.map(_.width).sum
  val height: Double = if boxes.isEmpty then 0 else boxes.map(_.height).max
  val ascent: Double = if boxes.isEmpty then 0 else boxes.map(_.ascent).max
  val descent: Double = height - ascent // ascent + descent = height
  val xAdvance: Double = boxes.map(_.xAdvance).sum

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    box(t, x, y)
    var currentX = x
    for box <- boxes do
      box.draw(t, currentX, y)
      currentX += box.width

  override def toString: String =
    s"HBox(width=$width, height=$height, ascent=$ascent, descent=$descent, boxes=$boxes)"
