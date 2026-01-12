package io.github.edadma.scriptura

import io.github.edadma.char_reader.CharReader
import io.github.edadma.texish.{Handler, Value}
import io.github.edadma.typesetter.{Box, HorizontalMode, Typesetter, VerticalMode}

class ScripturaHandler(val typesetter: Typesetter) extends Handler:
  private var newlineCount: Int = 0
  private var suppressed: Boolean = false

  def text(s: String): Unit =
    if !suppressed then
      if newlineCount == 1 then typesetter add " "
      typesetter.start add s
      newlineCount = 0

  def space(): Unit =
    if !suppressed then
      // Add space unless in vertical mode (halign cells are not HorizontalMode but accept spaces)
      if !typesetter.mode.isInstanceOf[VerticalMode] && newlineCount == 0 then
        typesetter add " "

  def newline(): Unit =
    if !suppressed then
      typesetter.mode match
        case _: HorizontalMode if newlineCount == 0 =>
          newlineCount += 1
        case _: HorizontalMode if newlineCount == 1 =>
          newlineCount += 1
          typesetter.paragraph()
        case _ =>
          // ignore newlines in vertical mode or after paragraph

  def get(name: String): Value =
    typesetter.get(name) match
      case None              => Value.Undefined
      case Some(s: String)   => Value.Text(s)
      case Some(n: Number)   => Value.Num(BigDecimal(n.doubleValue))
      case Some(b: Boolean)  => Value.Bool(b)
      case Some(v: Value)    => v  // Return stored Value directly
      case Some(v)           => Value.Text(v.toString)

  def set(name: String, value: Value): Unit =
    val v = value match
      case Value.Text(s)   => s
      case Value.Num(n)    => n.toDouble
      case Value.Bool(b)   => b
      case Value.Nil       => null
      case Value.Undefined => null
      case other           => other  // Store Value directly (Macro, Seq, Map, etc.)
    typesetter.set(name, v)

  def enterScope(): Unit = typesetter.enter()

  def exitScope(): Unit = typesetter.exit()

  def suppressOutput(suppress: Boolean): Unit = suppressed = suppress

  def command(name: String, args: Seq[Value], pos: CharReader): Value =
    // All scriptura commands are registered as Primitives
    // Unknown commands are errors
    error(s"Unknown command: \\$name", pos)

  // Helper to add a Box directly
  def addBox(box: Box): Unit =
    if !suppressed then
      if newlineCount == 1 then typesetter add " "
      typesetter add box
      newlineCount = 0

  // Helper to output a numeric value
  def outputNumber(d: Double): Unit =
    val s = if d % 1 == 0 then d.toInt.toString else d.toString
    text(s)

  // Reset newline count (useful after certain operations)
  def resetNewlineCount(): Unit = newlineCount = 0
