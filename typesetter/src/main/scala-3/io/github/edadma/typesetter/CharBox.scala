package io.github.edadma.typesetter

class CharBox(ts: Typesetter, val text: String, val font: Any, val color: Color) extends Box:

  val TextExtents(_, yBearing, _, heightVal, xAdvance, _) = ts.getTextExtents(text)

  val ascent: Double = -yBearing // Ascent is the negative yBearing
  val descent: Double = heightVal - ascent // Descent is height minus ascent
  val width: Double = xAdvance // Horizontal advance of the text
  override val height: Double = heightVal

  def draw(ts: Typesetter, x: Double, y: Double): Unit =
    if text.nonEmpty then
      ts.setColor(color)
      ts.drawString(text, x, y)

  override def toString: String = s"CharBox(ascent=$ascent, descent=$descent, width=$width, \"$text\")"
