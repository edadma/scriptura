package io.github.edadma.scriptura

import io.github.edadma.char_reader.CharReader
import io.github.edadma.texish.{Active, Parser, Renderer}
import io.github.edadma.typesetter.Typesetter

class ScripturaParser extends Parser(
      commands,
      List(
        new Active("#") {
          def apply(pos: CharReader, r: Renderer, context: Any): Any =
            context.asInstanceOf[Typesetter].op("placeholder")
        },
        new Active("&") {
          def apply(pos: CharReader, r: Renderer, context: Any): Any =
            context.asInstanceOf[Typesetter].op("newColumn")
        },
      ),
      blanks = true,
    )
