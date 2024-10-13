package io.github.edadma.scriptura

import io.github.edadma.texish.{Parser, Renderer}
import io.github.edadma.typesetter.{Typesetter, HorizontalMode, VerticalMode, Box}

import pprint.pprintln

class ScripturaRenderer(val typesetter: Typesetter, val config: Map[String, Any]) extends Renderer:
  val context: Any = typesetter
  var newlineCount: Int = 0

  override def output(v: Any): Unit =
    pprintln(v)
    v match
      case s: Seq[Any]                                                               => s foreach output
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
      case b: Box          => typesetter add b
      case f: (() => Unit) => f()

  override def set(name: String, value: Any): Unit =
    value match
      case n: BigDecimal => typesetter.set(name, n.toDouble)

  override def get(name: String): Any = typesetter.get(name)

  override def enterScope(): Unit = typesetter.enter()

  override def exitScope(): Unit = typesetter.exit()