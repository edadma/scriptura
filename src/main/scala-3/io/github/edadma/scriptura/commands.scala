package io.github.edadma.scriptura

import io.github.edadma.typesetter.Typesetter
import io.github.edadma.texish.{AST, Active, Command, Parser, Renderer, problem}
import io.github.edadma.char_reader.CharReader

val commands =
  List(
    new Command("hbox", 1, false):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(a: AST) =>
            context.asInstanceOf[Typesetter].hbox()
            renderer.render(a)

            val r = context.asInstanceOf[Typesetter].result

            context.asInstanceOf[Typesetter].pop()
          case List(a) => problem(pos, s"expected arguments <text>: $a")
          case _       => problem(pos, "expected arguments <text>"),
  )
