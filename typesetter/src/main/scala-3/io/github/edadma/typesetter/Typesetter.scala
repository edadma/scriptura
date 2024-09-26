package io.github.edadma.typesetter

abstract class Typesetter(layout: LayoutManager):
  def setFont(font: Any): Unit

  def setColor(color: Color): Unit

  def drawString(text: String, x: Double, y: Double): Unit

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit

  def drawRect(x: Double, y: Double, width: Double, height: Double): Unit

  def fillRect(x: Double, y: Double, width: Double, height: Double): Unit

  def loadFont(path: String, size: Float): Any
