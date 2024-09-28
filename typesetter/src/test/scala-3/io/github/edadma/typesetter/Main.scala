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

        // Pass the Graphics2D to your custom Graphics2DTypesetter
        val t = new Graphics2DTypesetter(new TestDocument, g)

        // Example CharBox rendering
        val charBox = new CharBox(t, "Testing", null, Color("black"))

        // Draw the CharBox
        charBox.draw(t, 10, 10 + charBox.ascent)
      }
    }

    override def closeOperation(): Unit = dispose()
