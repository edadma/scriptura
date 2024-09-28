package io.github.edadma.typesetter

class CharBox(ts: Typesetter, val text: String, val font: Any, val color: Color) extends Box:

  val TextExtents(_, yBearing, width, height, xAdvance, _) = ts.getTextExtents(text)

  val ascent: Double = -yBearing // Ascent is the negative yBearing
  val descent: Double = height - ascent // Descent is height minus ascent

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    boundingBox(t, x, y)
    if text.nonEmpty then
      t.setColor(color)
      t.drawString(text, x, y)

  override def toString: String = s"CharBox(ascent=$ascent, descent=$descent, width=$width, \"$text\")"
