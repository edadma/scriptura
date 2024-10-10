package io.github.edadma.typesetter

import scala.swing.*

import pprint.pprintln

object Main extends SimpleSwingApplication:
  def top: Frame = new MainFrame:
    title = "Simple Swing Example"

    contents = new Panel {
      preferredSize = new Dimension(800, 500)

      override def paintComponent(g: Graphics2D): Unit = {
        super.paintComponent(g)

        val t =
          new Graphics2DTypesetter(new TestDocument):
            set("hsize", 600)
            debug = true

//        t.hbox(t.getNumber("hsize"))
//          .addFil()
//          .add("Hello")
//          .add(" ")
//          .add("Scriptura!")
//          .add(" ")
//          .add("Cool")
//          .addFil()
//          .done()

//        t.hbox(t.getNumber("hsize"))
//          .addFil()
//          .add(
//            ImageBox(
//              t,
//              "866-536x354.jpg",
//            ),
//          )
//          .addFil()
//          .done()

//        t.hbox(t.getNumber("hsize"))
//          .addFil()
//          .add("one")
//          .addFil()
//          .done()
//
//        t.hbox(t.getNumber("hsize"))
//          .addFil()
//          .add("two")
//          .addFil()
//          .done()

//        t.hbox(t.getNumber("hsize"))
//          .addFil()
//          .add("three")
//          .addFil()
//          .done()

        Seq(
          "asdf1",
          " ",
          "asdf2",
//          " ",
//          "asdf3",
//          " ",
//          "asdf4",
//          " ",
//          "asdf5",
//          " ",
//          "asdf6",
//          " ",
//          "asdf7",
//          " ",
//          "asdf8",
//          " ",
//          "asdf9",
//          " ",
//          "asdf10",
//          " ",
//          "asdf11",
//          " ",
//          "asdf12",
//          " ",
//          "asdf13",
//          " ",
//          "asdf14",
//          " ",
//          "asdf15",
//          " ",
//          "asdf16",
//          " ",
//          "asdf17",
//          " ",
//          "asdf18",
//          " ",
//          "asdf19",
//          " ",
//          "asdf20",
//          " ",
//          "asdf21",
//          " ",
//          "asdf22",
//          " ",
//          "asdf23",
//          " ",
//          "asdf24",
//          " ",
        ).foreach(t.add)
        t.end()
        t.document.pages.head.draw(t, 10, 10 + t.document.pages.head.ascent)
        g.drawImage(t.page, null, 0, 0)
      }
    }

    override def closeOperation(): Unit = dispose()
