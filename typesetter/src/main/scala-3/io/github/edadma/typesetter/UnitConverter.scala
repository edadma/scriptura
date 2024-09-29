package io.github.edadma.typesetter

import scala.compiletime.uninitialized

object UnitConverter:
  var t: Typesetter = uninitialized

  def pointsToPixels(points: Double, dpi: Double): Double = (points / 72.0) * dpi

  def cmToPoints(cm: Double): Double = (cm / 2.54) * 72.0

  def inchesToPoints(inches: Double): Double = inches * 72.0

  def mmToPoints(mm: Double): Double = (mm / 25.4) * 72.0

  def toPoints(value: Double, unit: String): Double = unit match
    case "pt" => value // Points
    case "pc" => value * 12 // Picas (1 pica = 12 points)
    case "in" => value * 72 // Inches (1 inch = 72 points)
    case "cm" => value * 28.3465 // Centimeters (1 cm = 28.3465 points)
    case "mm" => value * 2.83465 // Millimeters (1 mm = 2.83465 points)
    case "em" => value * t.currentFontSize // Em units (based on current font size)
    case "ex" => value * t.currentFontXHeight // Ex units (based on current font x-height)
    case "px" => value * (72.0 / t.currentDPI) // Pixels (dependent on screen DPI)
    case _    => throw new IllegalArgumentException(s"Unknown unit: $unit")

  def toPicas(points: Double): String =
    val picas = points / 12
    val remainingPoints = points % 12
    f"${picas}p${remainingPoints}"
