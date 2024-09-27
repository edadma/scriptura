package io.github.edadma.typesetter

trait Box:
  def ascent: Double
  def descent: Double
  def height: Double = ascent + descent
  def width: Double
//  def hshift: Double
//  def vshift: Double

  def draw(t: Typesetter, x: Double, y: Double): Unit
