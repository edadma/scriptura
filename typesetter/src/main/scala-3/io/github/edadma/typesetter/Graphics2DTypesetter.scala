package io.github.edadma.typesetter

import java.awt.Graphics2D

class Graphics2DTypesetter(val doc: Document, g: Graphics2D) extends Typesetter:
//  def setFont(font: java.awt.Font): Unit = g.setFont(font)

  def setColor(color: Color): Unit =
    g.setColor(new java.awt.Color(color.redInt, color.greenInt, color.blueInt, color.alphaInt))

  def drawString(text: String, x: Double, y: Double): Unit = g.drawString(text, x.toFloat, y.toFloat)
  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit =
    g.drawLine(x1.toInt, y1.toInt, x2.toInt, y2.toInt)
  def drawRect(x: Double, y: Double, width: Double, height: Double): Unit =
    g.drawRect(x.toInt, y.toInt, width.toInt, height.toInt)
  def fillRect(x: Double, y: Double, width: Double, height: Double): Unit =
    g.fillRect(x.toInt, y.toInt, width.toInt, height.toInt)

  def loadFont(path: String, size: Float): java.awt.Font = {
    val fontFile = new java.io.File(path)
    val font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontFile).deriveFont(size)
    font
  }
