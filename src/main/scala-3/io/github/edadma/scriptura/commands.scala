package io.github.edadma.scriptura

import io.github.edadma.typesetter.{FilGlue, FillGlue, Typesetter}
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
            context
              .asInstanceOf[Typesetter]
              .hbox(if optional contains "to" then optional("to").asInstanceOf[Number].doubleValue else null)
            renderer.render(a)

            val r = context.asInstanceOf[Typesetter].result

            context.asInstanceOf[Typesetter].pop()
            r
          case List(a) => problem(pos, s"expected arguments <text>: $a")
          case _       => problem(pos, "expected arguments <text>"),
    new Command("bold", 1, false):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(a: AST) =>
            () =>
              context.asInstanceOf[Typesetter].bold()
              renderer.render(a)
              context.asInstanceOf[Typesetter].nobold()
          case List(a) => problem(pos, s"expected arguments <text>: $a")
          case _       => problem(pos, "expected arguments <text>"),
    new Command("italic", 1, false):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(a: AST) =>
            () =>
              context.asInstanceOf[Typesetter].italic()
              renderer.render(a)
              context.asInstanceOf[Typesetter].noitalic()
          case List(a) => problem(pos, s"expected arguments <text>: $a")
          case _       => problem(pos, "expected arguments <text>"),
    new Command("smallcaps", 1, false):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(a: AST) =>
            () =>
              context.asInstanceOf[Typesetter].smallcaps()
              renderer.render(a)
              context.asInstanceOf[Typesetter].nosmallcaps()
          case List(a) => problem(pos, s"expected arguments <text>: $a")
          case _       => problem(pos, "expected arguments <text>")
    ,
    new Command("hfil", 0, false):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        FilGlue
    ,
    new Command("vfil", 0, false):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        FilGlue
    ,
    new Command("hfill", 0, false):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        FillGlue
    ,
    new Command("vfill", 0, false):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        FillGlue,
  )
//\vfil \hbox {\hfil asdf \hfil} \vfil
