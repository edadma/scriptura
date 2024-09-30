package io.github.edadma.typesetter

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.language.postfixOps

abstract class Typesetter:

  val doc: Document

  var currentFontXHeight: Double = uninitialized
  var currentDPI: Double = uninitialized
  var currentFont: Font = uninitialized
  var currentColor: Color = Color("grey")

  UnitConverter.t = this

  case class Typeface(
      fonts: mutable.HashMap[Set[String], Any], // todo: find a nicer target independent type other than Any
      baseline: Option[Double],
      ligatures: Set[String],
  )

  protected val typefaces = new mutable.HashMap[String, Typeface]

  def setFont(font: Any, size: Double): Unit

  def setColor(color: Color): Unit

  def drawString(text: String, x: Double, y: Double): Unit

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit

  def drawRect(x: Double, y: Double, width: Double, height: Double): Unit

  def fillRect(x: Double, y: Double, width: Double, height: Double): Unit

  def loadFont(path: String): Any

  def getTextExtents(text: String): TextExtents

  def makeFont(font: Any, size: Double): Any

  doc.setTypesetter(this)

  def setFont(f: Font): Unit = setFont(f.fontFace, f.size)

  loadTypeface(
    "noto",
    "fonts/NotoSerif/NotoSerif",
    "\uFB03\uFB04\uFB01\uFB02",
    Set(),
    "Regular",
    "Bold",
    "Italic",
    ("Bold", "Italic"),
  )
  loadTypeface(
    "noto",
    "fonts/NotoSans/NotoSans",
    "\uFB03\uFB04\uFB01\uFB02\uFB00",
    Set("sans"),
    "Black",
    ("Black", "Italic"),
    "Bold",
    ("Bold", "Italic"),
    "ExtraBold",
    ("ExtraBold", "Italic"),
    "ExtraLight",
    ("ExtraLight", "Italic"),
    "Italic",
    "Light",
    ("Light", "Italic"),
    "Medium",
    ("Medium", "Italic"),
    "Regular",
    "SemiBold",
    ("SemiBold", "Italic"),
    "Thin",
    ("Thin", "Italic"),
  )
  loadTypeface(
    "gentium",
    "fonts/GentiumPlus-6.200/GentiumPlus",
    "\uFB03\uFB04\uFB01\uFB02\uFB00",
    Set(),
    "Regular",
    "Bold",
    "Italic",
    ("Bold", "Italic"),
  )
  loadTypeface(
    "gentiumbook",
    "fonts/GentiumBookPlus/GentiumBookPlus",
    "\uFB03\uFB04\uFB01\uFB02\uFB00",
    Set(),
    "Regular",
    "Bold",
    "Italic",
    ("Bold", "Italic"),
  )
  overrideBaseline("gentium", 0.8)
  loadTypeface("pt", "fonts/PTSansNarrow/PTSansNarrow", "\uFB01\uFB02", Set(), "Regular", "Bold")
  loadTypeface(
    "mono",
    "fonts/JetBrainsMono/static/JetBrainsMono",
    "",
    Set(),
    "Bold",
    ("Bold", "Italic"),
    "ExtraBold",
    ("ExtraBold", "Italic"),
    "ExtraLight",
    ("ExtraLight", "Italic"),
    "Italic",
    "Light",
    ("Light", "Italic"),
    "Medium",
    ("Medium", "Italic"),
    "Regular",
    "SemiBold",
    ("SemiBold", "Italic"),
    "Thin",
    ("Thin", "Italic"),
  )
  loadTypeface(
    "alegreya",
    "fonts/Alegreya/static/Alegreya",
    "\uFB01\uFB02",
    Set(),
    "Black",
    ("Black", "Italic"),
    "Bold",
    ("Bold", "Italic"),
    "ExtraBold",
    ("ExtraBold", "Italic"),
    "Italic",
    "Medium",
    ("Medium", "Italic"),
    "Regular",
    "SemiBold",
    ("SemiBold", "Italic"),
  )
  loadTypeface(
    "alegreya",
    "fonts/AlegreyaSC/AlegreyaSC",
    "\uFB01\uFB02",
    Set("smallcaps"),
    "Black",
    ("Black", "Italic"),
    "Bold",
    ("Bold", "Italic"),
    "ExtraBold",
    ("ExtraBold", "Italic"),
    "Italic",
    "Medium",
    ("Medium", "Italic"),
    "Regular",
  )

  currentFont = makeFont("gentium", 50, Set("regular"))

  def loadFont(typeface: String, path: String, ligatures: Set[String], styleSet: Set[String]): Unit =
    val font = loadFont(path)

    typefaces get typeface match
      case None => typefaces(typeface) = Typeface(mutable.HashMap(styleSet -> font), None, ligatures)
      case Some(Typeface(fonts, _, ligatures)) =>
        if fonts contains styleSet then
          sys.error(s"font for typeface '$typeface' with style '${styleSet.mkString(", ")}' has already been loaded")
        else fonts(styleSet) = font
  end loadFont

  def loadTypeface(
      typeface: String,
      basepath: String,
      ligatures: String,
      every: Set[String],
      styles: (Product | String)*,
  ): Unit =
    for style <- styles do
      val (styleName, styleSet) =
        style match
          case s: String  => (s, if s.toLowerCase == "regular" then Set.empty else Set(s.toLowerCase))
          case p: Product => (p.productIterator.mkString, p.productIterator.map(_.toString.toLowerCase).toSet)

      loadFont(
        typeface,
        s"$basepath-$styleName.ttf",
        ligatures map (_.toString) toSet,
        styleSet ++ every.map(_.toLowerCase),
      )
  end loadTypeface

  def overrideBaseline(typeface: String, baseline: Double): Unit =
    typefaces get typeface match
      case None                                => sys.error(s"typeface '$typeface' not found")
      case Some(Typeface(fonts, _, ligatures)) => typefaces(typeface) = Typeface(fonts, Some(baseline), ligatures)

  def makeFont(typeface: String, size: Double, styleSet: Set[String]): Font =
    typefaces get typeface match
      case None => sys.error(s"font for typeface '$typeface' not found")
      case Some(Typeface(fonts, baseline, ligatures)) =>
        val font =
          fonts.getOrElse(
            styleSet map (_.toLowerCase) filterNot (_ == "regular"),
            sys.error(
              s"font for typeface '$typeface' with style '${styleSet.mkString(", ")}' has not been loaded",
            ),
          )

        Font(typeface, size, 0, styleSet, makeFont(font, size), baseline, ligatures)

end Typesetter

case class TextExtents(
    xBearing: Double,
    yBearing: Double,
    width: Double,
    height: Double,
    xAdvance: Double,
    yAdvance: Double,
)
