package io.github.edadma.texish

import io.github.edadma.char_reader.CharReader
import scala.collection.mutable

import pprint.pprintln

@main def run(): Unit =
  val config =
    Map(
      "today" -> "MMMM d, y",
      "include" -> ".",
      "rounding" -> "HALF_EVEN",
    )
  val actives =
    List(
      new Active("<") {
        def apply(pos: CharReader, r: Renderer): String = {
          "lt"
        }
      },
    )
  val commands =
    List(
      new Command("verses", 1):
        def apply(
            pos: CharReader,
            renderer: Renderer,
            args: List[Any],
            optional: Map[String, Any],
            context: Any,
        ): Any =
          args match
            case List(a: String) => println(s"verses: $a")
            case List(a)         => problem(pos, s"expected arguments <string>: $a")
            case _               => problem(pos, "expected arguments <string>"),
    )
  val parser = new Parser(commands, actives, blanks = true)
  val scopes = mutable.Stack[Map[String, Any]](Map.empty)
  val renderer =
    new Renderer(parser, config, null):
      def output(v: Any): Unit = println(display(v))

      def get(name: String): Any = scopes.top.getOrElse(name, UNDEFINED)

      def set(name: String, value: Any): Unit = scopes(0) += (name -> value)

      def enterScope(): Unit = scopes push scopes.top

      def exitScope(): Unit = scopes.pop
  val src =
    """
    |\set v 123 \v
    """.trim.stripMargin
  val ast = parser.parse(src)

//  pprintln(ast)
  renderer.render(ast)
