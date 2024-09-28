package io.github.edadma.typesetter

import io.github.edadma.typesetter.TestDocument

import java.awt.RenderingHints
import scala.swing.*
import scala.swing.event.*

object Main extends SimpleSwingApplication:
  def top: Frame = new MainFrame:
    title = "Simple Swing Example"

    contents = new Panel {
      preferredSize = new Dimension(400, 400)

      override def paintComponent(g: Graphics2D): Unit = {
        super.paintComponent(g)

        val t = new Graphics2DTypesetter(new TestDocument, g)
        val builder = new HBoxBuilder

        builder
          .addBox(new CharBox(t, "Hello", null, Color("black")))
          .addGlue()
          .addBox(
            new CharBox(
              t,
              "World",
              null,
              Color("black"),
            ),
          )

        val line = builder.buildTo(270)

        line.draw(t, 10, 10 + line.ascent)
        println(line.width)
      }
    }

    override def closeOperation(): Unit = dispose()
