package io.github.edadma.scriptura

import io.github.edadma.texish.{Parser, Renderer}
import io.github.edadma.typesetter.{Typesetter, HorizontalMode, VerticalMode, Box}

import pprint.pprintln

class ScripturaRenderer(val typesetter: Typesetter, val config: Map[String, Any], val parser: Parser) extends Renderer:
  val context: Any = typesetter
  var newlineCount: Int = 0

  def output(v: Any): Unit =
    v match
//      case s: Seq[Any]                                                               => s foreach output
      case "\n" if newlineCount == 0 && typesetter.mode.isInstanceOf[HorizontalMode] => newlineCount += 1
      case "\n" if newlineCount == 1 =>
        newlineCount += 1
        typesetter.paragraph()
      case "\n"                                                                  =>
      case " " if newlineCount > 0 || typesetter.mode.isInstanceOf[VerticalMode] =>
      case s: String =>
        if newlineCount == 1 then typesetter add " "
        typesetter.start()
        typesetter add s
        newlineCount = 0
      case b: Box => typesetter add b
//      case f: Function0[?] => f()
      case d: Double =>
        output(
          if (d % 1 == 0) d.toInt.toString
          else d.toString,
        )

  def group(vals: Seq[Any]): Any = vals.mkString

  def set(name: String, value: Any): Unit =
    value match
      case n: BigDecimal => typesetter.set(name, n.toDouble)

  def get(name: String): Any = typesetter.get(name)

  def enterScope(): Unit = typesetter.enter()

  def exitScope(): Unit = typesetter.exit()
