package io.github.edadma.scriptura

import io.github.edadma.typesetter.{FilGlue, FillGlue, ImageBox, InfGlue, RuleBox, Typesetter}
import io.github.edadma.texish.{AST, Active, Command, Parser, Renderer, problem}
import io.github.edadma.char_reader.CharReader

val commands =
  List(
    new Command("typeface", 1, true):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(family: String) =>
            () => context.asInstanceOf[Typesetter].typeface(family)
          case _ => problem(pos, "expected arguments <family>")
    ,
    new Command("font", 3, true):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(family: String, size: Number, style: String) =>
            () => context.asInstanceOf[Typesetter].selectFont(family, size.doubleValue, style.split("\\s+").toSet)
          case _ => problem(pos, "expected arguments <family> <size> <style>")
    ,
    new Command("newpage", 0):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        () => context.asInstanceOf[Typesetter].newpage()
    ,
    new Command("noindent", 0):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        () => context.asInstanceOf[Typesetter].noindent()
    ,
    new Command("indent", 0):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        () => context.asInstanceOf[Typesetter].indent()
    ,
    new Command("image", 1):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args.head match {
          case p: String => new ImageBox(context.asInstanceOf[Typesetter], p)
          case a         => problem(pos, s"expected a path: $a")
        }
    ,
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
    new Command("rule", 2, true):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(width: BigDecimal, thickness: BigDecimal) =>
            RuleBox(
              context.asInstanceOf[Typesetter],
              width.toDouble,
              thickness.toDouble,
              shift = if optional contains "shift" then optional("shift").asInstanceOf[Number].doubleValue else 0,
            )
          case _ => problem(pos, "expected arguments <width> <thickness>")
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
    new Command("hss", 0, false):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        InfGlue
    ,
    new Command("vss", 0, false):
      def apply(
          pos: CharReader,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        InfGlue
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
