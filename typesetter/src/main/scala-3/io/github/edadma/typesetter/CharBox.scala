package io.github.edadma.typesetter

class CharBox(t: Typesetter, val text: String, val color: Color) extends Box:

  val TextExtents(_, yBearing, width, height, xAdvance, _) = t.getTextExtents(text)
  private val font = t.currentFont

  val ascent: Double = -yBearing // Ascent is the negative yBearing
  val descent: Double = height - ascent // Descent is height minus ascent

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    box(t, x, y)
    if text.nonEmpty then
      t.setFont(font)
      t.setColor(color)
      t.drawString(text, x, y)

  override def toString: String = s"CharBox(ascent=$ascent, descent=$descent, width=$width, \"$text\")"
