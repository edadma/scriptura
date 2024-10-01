package io.github.edadma.typesetter

class ImageBox(t: Typesetter, path: String, scaledWidth: Option[Double] = None, scaledHeight: Option[Double] = None)
    extends ContentBox:
  private val (image, imageWidth, imageHeight) = t.loadImage(path)
  private val imageScaling = t.getNumber("imageScaling")
  val typ: Type = Type.Horizontal
  val width: Double = scaledWidth getOrElse imageWidth * imageScaling
  val ascent: Double = scaledHeight getOrElse imageHeight * imageScaling
  val descent: Double = 0
  val height: Double = ascent
  val xAdvance: Double = width

  def draw(t: Typesetter, x: Double, y: Double): Unit =
    t.drawImage(image, x / imageScaling, (y - ascent) / imageScaling)