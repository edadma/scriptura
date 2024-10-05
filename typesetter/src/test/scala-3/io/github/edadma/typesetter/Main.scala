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

//        val b1 = new HBoxBuilder

        t.setDocument(new TestDocument)
        t.addFil()
          .add(CharBox(t, "Hello"))
          .addGlue(t.currentFont.space, 1)
          .add(
            CharBox(
              t,
              "Scriptura",
            ),
          )
          .addFil()

//        val b2 = new HBoxBuilder

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

//        val line1 = b1.buildTo(700)
//        val line2 = b2.buildTo(700)
//        val line3 = b3.buildTo(700)
//        val vb = new VBoxBuilder
//        val vbox = vb.add(line1).add(line3).add(line2).build

        t.end()
        println(t.document.pages.length)
        t.document.pages.head.draw(t, 10, 10 + t.document.pages.head.ascent)
      }
    }

    override def closeOperation(): Unit = dispose()
