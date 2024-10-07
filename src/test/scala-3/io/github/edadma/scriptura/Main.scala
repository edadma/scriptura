package io.github.edadma.scriptura

import scala.swing.*
import io.github.edadma.typesetter.{Graphics2DTypesetter, ImageBox, TestDocument}
import io.github.edadma.texish.{Parser, Renderer}

object Main extends SimpleSwingApplication:
  def top: Frame = new MainFrame:
    title = "Simple Swing Example"

    contents = new Panel {
      preferredSize = new Dimension(800, 500)

      override def paintComponent(g: Graphics2D): Unit = {
        super.paintComponent(g)

        val t =
          new Graphics2DTypesetter(new TestDocument, g):
            set("hsize", 600)
        //            debug = true

//        t.hbox(t.getNumber("hsize"))
//          .addFil()
//          .add("Hello")
//          .add(" ")
//          .add("Scriptura!")
//          .add(" ")
//          .add("Cool")
//          .addFil()
//          .done()
//
//        t.hbox(t.getNumber("hsize"))
//          .addFil()
//          .add(
//            ImageBox(
//              t,
//              "866-536x354.jpg",
//            ),
//          )
//          .addFil()
//          .done()
//
//        //        t.hbox(t.getNumber("hsize"))
//        //          .addFil()
//        //          .add("one")
//        //          .addFil()
//        //          .done()
//        //
//        //        t.hbox(t.getNumber("hsize"))
//        //          .addFil()
//        //          .add("two")
//        //          .addFil()
//        //          .done()
//
//        t.hbox(t.getNumber("hsize"))
//          .addFil()
//          .add("three")
//          .addFil()
//          .done()
//
//        t.end()

        val p = new Parser(Nil, Nil)
        val r = new Renderer(p, Map.empty, null) {
          var newlineCount: Int = 0

          override def output(v: Any): Unit =
            v match
              case "\n" if newlineCount == 0 => newlineCount += 1
              case "\n" =>
                t.paragraph()
                newlineCount = 0
              case v =>
                t.add(v.toString)
                newlineCount = 0

          override def set(name: String, value: Any): Unit =
            value match
              case n: BigDecimal => t.set(name, n.toDouble)

          override def get(name: String): Any = t.get(name)

          override def enterScope(): Unit = t.enter()

          override def exitScope(): Unit = t.exit()
        }

        val src =
          """
          |Lorem ipsum dolor sit am
          |
          |Lorem ipsum dolor sit amet
          |""".trim.stripMargin
        val ast = p.parse(src)

//        pprintln(ast)
        r.render(ast)
        t.end()
        t.document.pages.head.draw(t, 10, 10 + t.document.pages.head.ascent)
      }
    }

    override def closeOperation(): Unit = dispose()
