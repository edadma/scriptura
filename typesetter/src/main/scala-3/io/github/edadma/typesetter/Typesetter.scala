package io.github.edadma.typesetter

import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.language.postfixOps

abstract class Typesetter:

  val document: Document

  var debug: Boolean = false
  var currentFontXHeight: Double = uninitialized
  var currentDPI: Double = uninitialized
  var currentFont: Font = uninitialized
  var currentColor: Color = Color("grey")
  val converter = UnitConverter(this)

  case class Typeface(
      fonts: mutable.HashMap[Set[String], Any], // todo: find a nicer target independent type other than Any
      baseline: Option[Double],
      ligatures: Set[String],
  )

  protected val typefaces = new mutable.HashMap[String, Typeface]
  protected[typesetter] val scopes = new mutable.Stack[Map[String, Any]]
  protected[typesetter] val modeStack = new mutable.Stack[Mode]
  var indentParagraph: Boolean = true // todo: this should go into page mode maybe

  scopes push Map.empty
  modeStack push document
  document.ts = this
  document.init()

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
  set(defaultParameters)
//  modeStack push new PageMode(this, modeStack.top)
  modeStack push new VBoxBuilder(this)

  def setFont(font: Any /*, size: Double*/ ): Unit

  def setColor(color: Color): Unit

  def drawString(text: String, x: Double, y: Double): Unit

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit

  def drawRect(x: Double, y: Double, width: Double, height: Double): Unit

  def fillRect(x: Double, y: Double, width: Double, height: Double): Unit

  def loadFont(path: String): Any

  def getTextExtents(text: String): TextExtents

  def makeFont(font: Any, size: Double): Any

  def charWidth(font: Any, c: Char): Double

  def loadImage(path: String): (Any, Int, Int)

  def drawImage(image: Any, x: Double, y: Double): Unit

  def setFont(f: Font): Unit = setFont(f.renderFont /*, f.size*/ )

  def in: Double = currentDPI

  def pt: Double = in / 72

  def cm: Double = in / 2.54

  def get(name: String): Any = scopes.top.getOrElse(name, UNDEFINED)

  def getGlue(name: String): Glue = get(name).asInstanceOf[Glue]

  def getNumber(name: String): Double = get(name).asInstanceOf[Double]

  def set(name: String, value: Double | Glue): Unit = scopes(0) += (name -> value)

  def set(pairs: Seq[(String, Any)]): Unit = scopes(0) ++= pairs

  def loadFont(typeface: String, path: String, ligatures: Set[String], styleSet: Set[String]): Unit =
    val font = loadFont(path)

    typefaces get typeface match
      case None => typefaces(typeface) = Typeface(mutable.HashMap(styleSet -> font), None, ligatures)
      case Some(Typeface(fonts, _, _)) =>
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
        val derivedFont = makeFont(font, size)

        Font(typeface, size, charWidth(derivedFont, ' '), styleSet, derivedFont, baseline, ligatures)

  infix def add(text: String): Typesetter =
    if modeStack.top.isInstanceOf[VBoxBuilder] then start()
    add(charBox(text))

//  def textBox(text: String): CharBox =
//    val rep =
//      if representations then Ligatures.replace(text, Ligatures.REPRESENTATIONS, currentFont.ligatures) else text
//
//    charBox(if ligatures then Ligatures(rep, currentFont.ligatures) else rep)
//    charBox(text)

  // todo: charBox should do what textBox did in "compositor" (dealing with ligatures of all kinds)
  def charBox(s: String): CharBox = new CharBox(this, s)

  infix def add(box: Box): Typesetter =
    modeStack.top add box
    this

  infix def addGlue(naturalWidth: Double, stretch: Double = 0, shrink: Double = 0): Typesetter =
    add(Glue(naturalWidth, stretch, shrink))

  infix def addFil(): Typesetter = add(FilGlue)

  def hbox(toSize: Double | Null = null): Typesetter =
    modeStack push new HBoxBuilder(this, toSize)
    this

  def done(): Unit = modeStack.top.done()

  def paragraph(): Unit =
    modeStack.top match
      case p: ParagraphMode => p.done()
      case _                =>

  def end(): Unit = while modeStack.nonEmpty do done()

  def start(): Unit =
    paragraph()

    modeStack.top match
      case p: VBoxBuilder => p.paragraph
      case _              =>

  private def defaultParameters =
    List(
      "baselineskip" -> Glue(currentFont.size * 1.2 * pt),
      "lineskip" -> Glue(1),
      "lineskiplimit" -> 0.0,
      "spaceskip" -> Glue(currentFont.space, 1),
      "xspaceskip" -> Glue(currentFont.space * 1.5, 1),
      "hsize" -> 800.0,
      "vsize" -> 500.0,
      "parindent" -> in / 2,
      "parfillskip" -> FilGlue,
      "leftskip" -> ZeroGlue,
      "rightskip" -> ZeroGlue,
      "parskip" -> FilGlue,
      "hangindent" -> 0.0,
      "hangafter" -> 1.0,

      //
      "imageScaling" -> 1.0,
    )

end Typesetter

case class TextExtents(
    xBearing: Double,
    yBearing: Double,
    width: Double,
    height: Double,
    xAdvance: Double,
    yAdvance: Double,
)
