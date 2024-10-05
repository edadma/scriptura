package io.github.edadma.typesetter

import io.github.edadma.typesetter.TestDocument

import java.awt.RenderingHints
import scala.swing.*
import scala.swing.event.*

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

//        val b1 = new HBoxBuilder(t, 700)

        t.hbox
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

        val b2 = new HBoxBuilder(t, 700)

        t.addFil()
          .add(
            CharBox(
              t,
              "line 2",
            ),
          )
          .addFil()

//        val b3 = new HBoxBuilder

        t.addFil()
          .add(
            ImageBox(
              t,
              "866-536x354.jpg",
            ),
          )
          .addFil()

        val line1 = b1.result
        val line2 = b2.result
        val line3 = b3.result
//        val vb = new VBoxBuilder
        t.add(line1).add(line3).add(line2).build

        t.end()
        t.document.pages.head.draw(t, 10, 10 + t.document.pages.head.ascent)
      }
    }

    override def closeOperation(): Unit = dispose()
