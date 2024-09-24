package io.github.edadma.scriptura

import scala.swing.*
import scala.swing.event.*

object Main extends SimpleSwingApplication:
  def top: Frame = new MainFrame:
    title = "Simple Swing Example"

    val label = new Label("Hello, Scala 3 with Swing!")
    val button = new Button("Click Me")
    val textField: TextField = new TextField { columns = 20 }

    contents = new BoxPanel(Orientation.Vertical) {
      contents += label
      contents += button
      contents += textField
      border = Swing.EmptyBorder(15, 15, 15, 15)
    }

    listenTo(button)
    reactions += { case ButtonClicked(_) =>
      label.text = s"Hello, ${textField.text}!"
    }

    size = new Dimension(300, 200)

    override def closeOperation(): Unit = dispose()
