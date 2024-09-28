package io.github.edadma.typesetter

trait Box:
  def ascent: Double
  def descent: Double
  def height: Double // ascent + descent
  def width: Double
  def xAdvance: Double

  def draw(t: Typesetter, x: Double, y: Double): Unit

  def boundingBox(t: Typesetter, x: Double, y: Double): Unit =
    t.setColor(Color("red"))
    t.drawRect(x, y - ascent, width, height)