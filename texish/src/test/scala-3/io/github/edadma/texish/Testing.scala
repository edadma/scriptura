package io.github.edadma.texish

import java.io.{ByteArrayOutputStream, PrintStream}
import scala.collection.mutable

trait Testing:
  def test(src: String): String =
    val config =
      Map(
        "today" -> "MMMM d, y",
        "include" -> ".",
        "rounding" -> "HALF_EVEN",
      )
    val parser = new Parser(Nil, Nil, blanks = true)
    val scopes = new mutable.Stack[Map[String, Any]]
    val bytes = new ByteArrayOutputStream
    val renderer =
      new Renderer(parser, config, null):
        def output(v: Any): Unit = new PrintStream(bytes, false).print(display(v))

        def get(name: String): Any = scopes.top.getOrElse(name, UNDEFINED)

        def set(name: String, value: Any): Unit = scopes(0) += (name -> value)

        def enterScope(): Unit = scopes push scopes.top

        def exitScope(): Unit = scopes.pop
    val ast = parser.parse(src)

    renderer.render(ast)
    bytes.toString
