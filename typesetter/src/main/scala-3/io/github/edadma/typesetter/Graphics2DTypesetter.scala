package io.github.edadma.typesetter

import java.awt.{Font => JFont, Graphics2D, RenderingHints, Toolkit}
import java.awt.font.TextLayout
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class Graphics2DTypesetter(val document: Document, g: Graphics2D) extends Typesetter:
//  def setFont(font: java.awt.Font): Unit = g.setFont(font)

  g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
  g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

  // JFont.createFont(JFont.TRUETYPE_FONT, new File("fonts/Roboto-Regular.ttf")).deriveFont(24f)
  // loadFont("fonts/MonteCarlo-Regular.ttf").deriveFont(50f)
  // Font.createFont(Font.TRUETYPE_FONT, new File("fonts/cm/cmunrm.ttf")).deriveFont(30f)
  //     loadFont("fonts/Roboto-Regular.ttf").deriveFont(24f)

  private val frc = g.getFontRenderContext

  def getDPI: Double = Toolkit.getDefaultToolkit.getScreenResolution

  def setFont(font: Any /*, size: Double*/ ): Unit = g.setFont(font.asInstanceOf[JFont] /*.deriveFont(size.toFloat)*/ )

  def setColor(color: Color): Unit =
    g.setColor(new java.awt.Color(color.redInt, color.greenInt, color.blueInt, color.alphaInt))

  def drawString(text: String, x: Double, y: Double): Unit = g.drawString(text, x.toFloat, y.toFloat)
  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit =
    g.drawLine(x1.toInt, y1.toInt, x2.toInt, y2.toInt)
  def drawRect(x: Double, y: Double, width: Double, height: Double): Unit =
    g.drawRect(x.toInt, y.toInt, width.toInt, height.toInt)
  def fillRect(x: Double, y: Double, width: Double, height: Double): Unit =
    g.fillRect(x.toInt, y.toInt, width.toInt, height.toInt)

  def loadFont(path: String): JFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new java.io.File(path))

  def getTextExtents(text: String): TextExtents =
    val layout = new TextLayout(text, currentFont.renderFont.asInstanceOf[JFont], frc)
    val bounds = layout.getBounds

    val ascent = -bounds.getY
//    val descent = layout.getDescent
    val width = bounds.getWidth
    val height = bounds.getHeight
    val advance = layout.getAdvance

    TextExtents(
      xBearing = bounds.getX,
      yBearing = -ascent, // In Graphics2D, the ascent is negative yBearing (above the baseline)
      width = width,
      height = height,
      xAdvance = advance,
      yAdvance = 0, // In horizontal typesetting, yAdvance is 0
    )

  def makeFont(font: Any, size: Double): Any = font.asInstanceOf[JFont].deriveFont(size.toFloat)

  def charWidth(font: Any, c: Char): Double =
    setFont(font)
    g.getFontMetrics.charWidth(c)

  def loadImage(path: String): (Any, Int, Int) =
    val image = ImageIO.read(new File(path))

    (image, image.getWidth, image.getHeight)

  def drawImage(image: Any, x: Double, y: Double): Unit =
    g.drawImage(image.asInstanceOf[BufferedImage], x.toInt, y.toInt, null)

//    val glyphs = font.createGlyphVector(frc, text)
//    val lb = glyphs.getLogicalBounds
//    val vb = glyphs.getVisualBounds
//
//    println(lb.getX)
//    println(lb.getWidth)
//    println(vb.getWidth)
//    TextExtents(lb.getX, vb.getY, vb.getWidth, vb.getHeight, lb.getWidth, 0)
