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
          new Graphics2DTypesetter(new TestDocument, g):
            set("hsize", 600)
            debug = true

        t.hbox(t.getNumber("hsize"))
          .addFil()
          .add("Hello")
          .add(" ")
          .add("Scriptura!")
          .add(" ")
          .add("Cool")
          .addFil()
          .done()

        t.hbox(t.getNumber("hsize"))
          .addFil()
          .add(
            ImageBox(
              t,
              "866-536x354.jpg",
            ),
          )
          .addFil()
          .done()

        t.hbox(t.getNumber("hsize"))
          .addFil()
          .add("text")
          .addFil()
          .done()

        t.end()
        t.document.pages.head.draw(t, 10, 10 + t.document.pages.head.ascent)
        println("---------------")
      }
    }

    override def closeOperation(): Unit = dispose()
