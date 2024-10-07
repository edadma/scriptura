package io.github.edadma.scriptura

import scala.swing.*
import io.github.edadma.typesetter.{Graphics2DTypesetter, ImageBox, TestDocument}
import io.github.edadma.texish.{Parser, Renderer}
import pprint.pprintln

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
            // debug = true
        val p = new Parser(Nil, Nil, blanks = true)
        val r = new Renderer(p, Map.empty, null) {
          var newlineCount: Int = 0

          override def output(v: Any): Unit =
            v match
              case s: Seq[Any]               => s.foreach(output)
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
          |
          |Lorem ipsum dolor sit amet
          |""".trim.stripMargin
        val ast = p.parse(src)

        pprintln(ast)
        r.render(ast)
        t.end()
        t.document.pages.head.draw(t, 10, 10 + t.document.pages.head.ascent)
      }
    }

    override def closeOperation(): Unit = dispose()
