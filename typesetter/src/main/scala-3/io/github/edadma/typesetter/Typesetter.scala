package io.github.edadma.typesetter

import scala.collection.mutable
import scala.compiletime.uninitialized

abstract class Typesetter:
  type GenericFont

  case class Font(
      family: String,
      size: Double,
      //                 extents: FontExtents,
      space: Double,
      style: Set[String],
      fontFace: GenericFont,
      baseline: Option[Double],
      ligatures: Set[String],
  )

  val doc: Document

  var currentFontSize: Double = uninitialized
  var currentFontXHeight: Double = uninitialized
  var currentDPI: Double = uninitialized

  UnitConverter.t = this

  case class Typeface(
      fonts: mutable.HashMap[Set[String], GenericFont],
      baseline: Option[Double],
      ligatures: Set[String],
  )

  protected val typefaces = new mutable.HashMap[String, Typeface]

  //  def setFont(font: Any): Unit

  def setColor(color: Color): Unit

  def drawString(text: String, x: Double, y: Double): Unit

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit

  def drawRect(x: Double, y: Double, width: Double, height: Double): Unit

  def fillRect(x: Double, y: Double, width: Double, height: Double): Unit

  def loadFont(path: String): GenericFont

  def getTextExtents(text: String): TextExtents

  doc.setTypesetter(this)

  def loadFont(typeface: String, path: String, ligatures: Set[String], styleSet: Set[String]): Unit =
    val face: GenericFont = loadFont(path) /*fontFaceCreateForFTFace(
      freetype
        .newFace(path, 0)
        .getOrElse(sys.error(s"error loading face: $path"))
        .faceptr,
      0,
    )*/

    typefaces get typeface match
      case None => typefaces(typeface) = Typeface(mutable.HashMap(styleSet -> face), None, ligatures)
      case Some(Typeface(fonts, _, ligatures)) =>
        if fonts contains styleSet then
          sys.error(s"font for typeface '$typeface' with style '${styleSet.mkString(", ")}' has already been loaded")
        else fonts(styleSet) = face
  end loadFont

end Typesetter

case class TextExtents(
    xBearing: Double,
    yBearing: Double,
    width: Double,
    height: Double,
    xAdvance: Double,
    yAdvance: Double,
)
