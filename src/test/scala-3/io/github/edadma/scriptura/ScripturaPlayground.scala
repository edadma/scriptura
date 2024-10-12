package io.github.edadma.scriptura

import scala.swing.*
import scala.swing.event.*
import java.awt.image.BufferedImage
import javax.swing.{BorderFactory, ImageIcon}
import java.awt.{Color, Toolkit}
import io.github.edadma.typesetter.{Graphics2DTypesetter, TestDocument, HorizontalMode}
import pprint.pprintln

class MultiPagePanel extends BoxPanel(Orientation.Vertical):
  def setImages(images: List[BufferedImage]): Unit =
    contents.clear()

    for (img <- images)
      val label = new Label:
        icon = new ImageIcon(img)
        border = BorderFactory.createLineBorder(Color.BLACK, 1) // Adds a border around each page

      val wrapper = new BoxPanel(Orientation.Vertical) {
        contents += label
        border = BorderFactory.createEmptyBorder(10, 10, 0, 10) // Adds margin around each page
      }

      contents += wrapper

    revalidate()
    repaint()

object ScripturaPlayground extends SimpleSwingApplication:
  val screenSize = Toolkit.getDefaultToolkit.getScreenSize

  def top: Frame = new MainFrame:
    title = "Scriptura Playground"

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
      border = Swing.EmptyBorder(10, 10, 10, 10)

      contents += new ScrollPane(inputArea)
      contents += new ScrollPane(errorOutput)
      contents += new FlowPanel(FlowPanel.Alignment.Center)(runButton)
    }

    // Main split pane
    val splitPane = new SplitPane(Orientation.Vertical, leftPanel, outputScrollPane) {
      oneTouchExpandable = true
      continuousLayout = true
      dividerLocation = screenSize.width / 2
    }

    // Event handling for the Run button
    listenTo(runButton)
    reactions += { case ButtonClicked(`runButton`) =>
      try {
        val doc = new TestDocument(5)
        val t =
          new Graphics2DTypesetter(doc):
            set("hsize", 600)
        //          debug = true
        val p = new ScripturaParser
        val r = new ScripturaRenderer(t, Map.empty)
        val ast = p.parse(inputArea.text)

        r.render(ast)
        t.end()
        multiPagePanel.setImages(doc.pages.toList.asInstanceOf[List[BufferedImage]])
        errorOutput.text = ""
      } catch
        case error: Throwable =>
          errorOutput.text = error.toString
          multiPagePanel.setImages(Nil)
    }

    // Set up the main frame
    contents = splitPane
    size = new Dimension(screenSize.width, screenSize.height)

    override def closeOperation(): Unit = dispose()
