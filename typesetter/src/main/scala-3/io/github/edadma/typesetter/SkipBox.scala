package io.github.edadma.typesetter

trait SkipBox extends Box:
  val ascent: Double = 0
  val descent: Double = 0

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    box(t, x, y, "lightgreen")
