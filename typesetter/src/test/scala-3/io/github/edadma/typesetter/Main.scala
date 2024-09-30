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

        val t = new Graphics2DTypesetter(new TestDocument, g)
        val b1 = new HBoxBuilder

        b1.addFil()
          .add(CharBox(t, "Hello"))
          .addGlue(t.currentFont.space, 1)
          .add(
            CharBox(
              t,
              "Scriptura",
            ),
          )
          .addFil()
        val b2 = new HBoxBuilder

        b2.addFil()
          .add(
            CharBox(
              t,
              "line 2",
            ),
          )
          .addFil()

        val line1 = b1.buildTo(700)
        val line2 = b2.buildTo(700)
        val vb = new VBoxBuilder
        val vbox = vb.add(line1).add(line2).build

        vbox.draw(t, 10, 10 + vbox.ascent)
      }
    }

    override def closeOperation(): Unit = dispose()
