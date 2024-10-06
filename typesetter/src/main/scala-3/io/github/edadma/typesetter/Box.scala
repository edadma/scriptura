package io.github.edadma.typesetter

trait Box:
  def ascent: Double
  def descent: Double
  def height: Double // ascent + descent
  def width: Double
  def xAdvance: Double
  def isSpace: Boolean

  def draw(t: Typesetter, x: Double, y: Double): Unit

  def box(t: Typesetter, x: Double, y: Double, color: String = "red"): Unit =
    if t.debug then
      t.setColor(Color("blue"))
      t.drawLine(x, y, x + width, y)
      t.setColor(Color(color))
      t.drawRect(x, y - ascent, width, height)
end Box
