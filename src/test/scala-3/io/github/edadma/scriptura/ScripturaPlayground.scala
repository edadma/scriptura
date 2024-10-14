package io.github.edadma.scriptura

import scala.swing.*
import scala.swing.event.*
import java.awt.image.BufferedImage
import javax.swing.{BorderFactory, ImageIcon, KeyStroke}
import java.awt.{Color, Toolkit}
import java.io.{PrintWriter, StringWriter, ByteArrayOutputStream, PrintStream}
import javax.swing.undo.{UndoManager, AbstractUndoableEdit}
import java.awt.event.{InputEvent, KeyEvent, ActionEvent}

import scala.Console.withOut

import io.github.edadma.typesetter.{Graphics2DTypesetter, HorizontalMode, TestDocument, SimpleDocument}

import pprint.pprintln

class MultiPagePanel extends BoxPanel(Orientation.Vertical):
  def setImages(images: List[BufferedImage]): Unit =
    contents.clear()

    for (img <- images)
      val label = new Label:
        icon = new ImageIcon(img)
        border = BorderFactory.createLineBorder(Color.BLACK, 1)

      contents +=
        new BoxPanel(Orientation.Vertical):
          contents += label
          border = BorderFactory.createEmptyBorder(10, 10, 0, 10)

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

    val undoManager = new UndoManager()

    inputArea.peer.getDocument.addUndoableEditListener { event =>
      undoManager.addEdit(event.getEdit)
    }

    val undoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK)
    inputArea.peer.getInputMap.put(undoKey, "Undo")
    inputArea.peer.getActionMap.put(
      "Undo",
      new javax.swing.AbstractAction {
        override def actionPerformed(e: ActionEvent): Unit = {
          if (undoManager.canUndo) undoManager.undo()
        }
      },
    )

    val redoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)
    inputArea.peer.getInputMap.put(redoKey, "Redo")
    inputArea.peer.getActionMap.put(
      "Redo",
      new javax.swing.AbstractAction {
        override def actionPerformed(e: ActionEvent): Unit = {
          if (undoManager.canRedo) undoManager.redo()
        }
      },
    )

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

    def captureStdOut(block: => Unit): String =
      val outputStream = new ByteArrayOutputStream()

      withOut(outputStream) {
        block // Execute the block with stdout redirected
      }

      outputStream.toString

    // Event handling for the Run button
    listenTo(runButton)
    reactions += { case ButtonClicked(`runButton`) =>
      try {
        val t = new Graphics2DTypesetter {
//          debug = true
//          ligatures = false
          setDocument(new TestDocument)
        }
        val p = new ScripturaParser
        val r = new ScripturaRenderer(t, Map.empty)
        val ast = p.parse(inputArea.text)

        errorOutput.text = captureStdOut {
          r.render(ast)
          t.end()
        }

        multiPagePanel.setImages(
          t.getDocument.pages.toList.asInstanceOf[List[BufferedImage]],
        )
      } catch
        case error: Throwable =>
          val sw = new StringWriter
          val pw = new PrintWriter(sw)

          error.printStackTrace(pw)
          errorOutput.text = sw.toString
          multiPagePanel.setImages(Nil)
    }

    // Set up the main frame
    contents = splitPane
    size = new Dimension(screenSize.width, screenSize.height)

    override def closeOperation(): Unit = dispose()

/*
\vfil
\hbox to:\hsize{\hfil asdf \hfil}
\vfil
 */
