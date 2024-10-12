package io.github.edadma.scriptura

import scala.swing.*
import scala.swing.event.*
import java.awt.image.BufferedImage
import javax.swing.{BorderFactory, ImageIcon}
import java.awt.{Color, Toolkit}
import io.github.edadma.typesetter.{Graphics2DTypesetter, TestDocument}
import io.github.edadma.texish.{Parser, Renderer}
import pprint.pprintln

class MultiPagePanel extends BoxPanel(Orientation.Vertical):
  def setImages(images: List[BufferedImage]): Unit =
    contents.clear()

    for (img <- images)
      val label = new Label:
        icon = new ImageIcon(img)
        border = BorderFactory.createLineBorder(Color.BLACK, 2) // Adds a border around each page

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
      val doc = new TestDocument
      val t =
        new Graphics2DTypesetter(doc):
          set("hsize", 600)
//          debug = true
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

      val ast = p.parse(inputArea.text)

      r.render(ast)
      t.end()
      multiPagePanel.setImages(doc.pages.toList.asInstanceOf[List[BufferedImage]])
    }

    // Set up the main frame
    contents = splitPane
    size = new Dimension(screenSize.width, screenSize.height)

    override def closeOperation(): Unit = dispose()

/*
asdf

zxcv

sdfg

wert

rtuy

asdf

zxcv

sdfg

wert

rtuy

asdf

zxcv

sdfg

wert

rtuy

rtuy

rtuy

rtuy

rtuy

rtuy

xcvb

sdfg

dfgh

wert
 */
