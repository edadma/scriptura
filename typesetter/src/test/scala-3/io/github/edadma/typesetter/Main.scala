package io.github.edadma.typesetter

import scala.swing.*

object Main extends SimpleSwingApplication:
  def top: Frame = new MainFrame:
    title = "Simple Swing Example"

    contents = new Panel {
      preferredSize = new Dimension(800, 500)

      override def paintComponent(g: Graphics2D): Unit = {
        super.paintComponent(g)

        val t =
          new Graphics2DTypesetter(g):
            set("hsize", 700)
            setDocument(new TestDocument)

        t.hbox(700)
          .addFil()
          .add(CharBox(t, "Hello"))
          .addGlue(t.currentFont.space, 1)
          .add(
            CharBox(
              t,
              "Scriptura",
            ),
          )
          .addFil()
          .done()

        t.hbox(700)
          .addFil()
          .add(
            ImageBox(
              t,
              "866-536x354.jpg",
            ),
          )
          .addFil()
          .done()

        t.hbox(700)
          .addFil()
          .add(
            CharBox(
              t,
              "line 2",
            ),
          )
          .addFil()
          .done()

        t.end()
        t.document.pages.head.draw(t, 10, 10 + t.document.pages.head.ascent)
      }
    }

    override def closeOperation(): Unit = dispose()
