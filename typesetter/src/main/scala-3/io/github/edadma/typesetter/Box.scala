package io.github.edadma.typesetter

trait Box {
  def ascent: Double
  def descent: Double
  def height: Double = ascent + descent
  def width: Double
  def xshift: Double
  def yshift: Double

  def draw(t: Typesetter): Unit
}
