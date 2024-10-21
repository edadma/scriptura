package io.github.edadma.typesetter

class RuleBox(t: Typesetter, val width: Double, thickness: Double, color: Color, shift: Double) extends NoGlueBox:
  def this(t: Typesetter, width: Double, thickness: Double, shift: Double = 0) =
    this(t, width, thickness, t.currentColor, shift)
  require(width >= 0, "rule width is non-negative")
  require(thickness >= 0, "rule thickness is non-negative")

  val (ascent, descent) =
    if shift >= 0 then (thickness + shift, 0d)
    else if thickness + shift < 0 then (0d, thickness - shift)
    else (thickness + shift, -shift)

  val xAdvance: Double = width

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    t.setColor(color)
    t.fillRect(x, y + ascent, width, thickness)
