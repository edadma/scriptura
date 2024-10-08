package io.github.edadma.scriptura

import scala.swing.*
import scala.swing.event.*
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import io.github.edadma.typesetter.{Graphics2DTypesetter, ImageBox, TestDocument}
import io.github.edadma.texish.{Parser, Renderer}

import pprint.pprintln

class MultiPagePanel extends BoxPanel(Orientation.Vertical):
  def setImages(images: List[BufferedImage]): Unit =
    contents.clear()

    for (img <- images)
      contents +=
        new Label:
          icon = new ImageIcon(img)

    revalidate()
    repaint()

object ScripturaPlayground extends SimpleSwingApplication:

  def top: Frame = new MainFrame:
    title = "Scriptura Playground"

    val src: String = ""

    // Left panel components
    val inputArea = new TextArea {
      rows = 20
      lineWrap = true
      wordWrap = true
    }
    val errorOutput = new TextArea {
      rows = 5
      editable = false
    }
    val runButton = new Button("Run")

    val multiPagePanel = new MultiPagePanel
    val outputScrollPane = new ScrollPane(multiPagePanel)

    // Adding components to the left panel
    val leftPanel = new BoxPanel(Orientation.Vertical) {
      contents += new ScrollPane(inputArea)
      contents += new ScrollPane(errorOutput)
      contents += new FlowPanel(FlowPanel.Alignment.Center)(runButton)
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }

    // Main split pane
    val splitPane = new SplitPane(Orientation.Horizontal, leftPanel, outputScrollPane) {
      oneTouchExpandable = true
      continuousLayout = true
      dividerLocation = 300
    }

    // Event handling for the Run button
    listenTo(runButton)
    reactions += { case ButtonClicked(`runButton`) =>
      val inputText = inputArea.text
    // Add your typesetting logic here, using the inputText
    // Example: errorOutput.text = "Error: Could not typeset" // for error messages
    }

    // Set up the main frame
    contents = splitPane
    size = new Dimension(800, 600)

    override def closeOperation(): Unit = dispose()

/*
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
              case " " if newlineCount > 0   =>
              case "\n" if newlineCount == 1 =>
                newlineCount += 1
                t.paragraph()
              case "\n" =>
              case s: String =>
                if newlineCount == 1 then t add " "
                t add s
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
          |asdf qwer sdfg asdf qwer sdfg asdf.
          |qwer sdfg asdf qwer sdfg asdf qwer sdfg asdf qwer sdfg
          |
          |wert kljh eryt wert kljh eryt
          |wert kljh eryt wert kljh eryt wert kljh eryt wert kljh eryt
          """.trim.stripMargin
        val ast = p.parse(src)

        pprintln(ast)
        r.render(ast)
        t.end()
        t.document.pages.head.draw(t, 10, 10 + t.document.pages.head.ascent)
      }
 */
