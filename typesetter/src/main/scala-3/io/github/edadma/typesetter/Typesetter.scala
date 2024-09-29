package io.github.edadma.typesetter

import scala.compiletime.uninitialized

abstract class Typesetter:
  val doc: Document

  var currentFontSize: Double = uninitialized
  var currentFontXHeight: Double = uninitialized
  var currentDPI: Double = uninitialized

  UnitConverter.t = this

//  def setFont(font: Any): Unit

  def setColor(color: Color): Unit

  def drawString(text: String, x: Double, y: Double): Unit

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit

  def drawRect(x: Double, y: Double, width: Double, height: Double): Unit

  def fillRect(x: Double, y: Double, width: Double, height: Double): Unit

  def loadFont(path: String, size: Float): Any

  def getTextExtents(text: String): TextExtents

  doc.setTypesetter(this)

case class TextExtents(
    xBearing: Double,
    yBearing: Double,
    width: Double,
    height: Double,
    xAdvance: Double,
    yAdvance: Double,
)
