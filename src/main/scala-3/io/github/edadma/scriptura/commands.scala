package io.github.edadma.scriptura

import io.github.edadma.typesetter.{FilGlue, FillGlue, ImageBox, InfGlue, RuleBox, Typesetter, Glue}
import io.github.edadma.texish.{AST, Active, Command, Parser, Renderer, problem}
import io.github.edadma.char_reader.CharReader

val commands =
  List(
    new Command("typeface", 1, true):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(typeface: String) =>
            context.asInstanceOf[Typesetter].typeface(typeface)
            ()
          case _ => problem(pos, "expected arguments <family>")
    ,
    new Command("font", 3, true):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(typeface: String, size: Number, style: String) =>
            context.asInstanceOf[Typesetter].selectFont(typeface, size.doubleValue, style.split("\\s+").toSet)
            ()
          case _ => problem(pos, "expected arguments <typeface> <size> <style>")
    ,
    new Command("newpage", 0):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        context.asInstanceOf[Typesetter].newpage()
    ,
    new Command("noindent", 0):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        context.asInstanceOf[Typesetter].noindent()
    ,
    new Command("indent", 0):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        context.asInstanceOf[Typesetter].indent()
    ,
    new Command("image", 1):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args.head match {
          case p: String =>
            context.asInstanceOf[Typesetter].image(p)
            ()
          case a => problem(pos, s"expected a path: $a")
        }
    ,
    new Command("vskip", 1, true):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(d: Number) =>
            context.asInstanceOf[Typesetter].glue(d.doubleValue, 0, 0)
            ()
          case _ => problem(pos, "expected arguments <dimen>")
    ,
    new Command("hbox", 1, false):
      def apply(
          pos: CharReader,
          parser: Parser,
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

//              val r = context.asInstanceOf[Typesetter].result
//
//              context.asInstanceOf[Typesetter].pop()
            context.asInstanceOf[Typesetter].done()
          case List(a) => problem(pos, s"expected arguments <text>: $a")
          case _       => problem(pos, "expected arguments <text>")
    ,
    new Command("bold", 1, false):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(a: AST) =>
            context.asInstanceOf[Typesetter].bold()
            renderer.render(a)
            context.asInstanceOf[Typesetter].nobold()
            ()
          case List(a) => problem(pos, s"expected arguments <text>: $a")
          case _       => problem(pos, "expected arguments <text>")
    ,
    new Command("italic", 1, false):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(a: AST) =>
            context.asInstanceOf[Typesetter].italic()
            renderer.render(a)
            context.asInstanceOf[Typesetter].noitalic()
            ()
          case List(a) => problem(pos, s"expected arguments <text>: $a")
          case _       => problem(pos, "expected arguments <text>")
    ,
    new Command("smallcaps", 1, false):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        args match
          case List(a: AST) =>
            context.asInstanceOf[Typesetter].smallcaps()
            renderer.render(a)
            context.asInstanceOf[Typesetter].nosmallcaps()
            ()
          case List(a) => problem(pos, s"expected arguments <text>: $a")
          case _       => problem(pos, "expected arguments <text>")
    ,
    new Command("hrule", 0, true):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        val t = context.asInstanceOf[Typesetter]
        val width = optional.getOrElse("width", t getNumber "hsize").asInstanceOf[Number].doubleValue
        val ascent = optional.getOrElse("ascent", 3).asInstanceOf[Number].doubleValue
        val descent = optional.getOrElse("descent", 0).asInstanceOf[Number].doubleValue

        t add RuleBox(t, width, ascent, descent)
        ()
    ,
    new Command("hfil", 0, false):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        context.asInstanceOf[Typesetter].fil
        ()
    ,
    new Command("vfil", 0, false):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        context.asInstanceOf[Typesetter].fil
        ()
    ,
    new Command("hss", 0, false):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        context.asInstanceOf[Typesetter].add(InfGlue)
        ()
    ,
    new Command("vss", 0, false):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        context.asInstanceOf[Typesetter].add(InfGlue)
        ()
    ,
    new Command("hfill", 0, false):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        context.asInstanceOf[Typesetter].fill
        ()
    ,
    new Command("vfill", 0, false):
      def apply(
          pos: CharReader,
          parser: Parser,
          renderer: Renderer,
          args: List[Any],
          optional: Map[String, Any],
          context: Any,
      ): Any =
        context.asInstanceOf[Typesetter].fill
        (),
  )
