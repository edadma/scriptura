package io.github.edadma.typesetter

import scala.annotation.tailrec

class ParagraphMode(protected val t: Typesetter, pageMode: Builder) extends HorizontalMode:
  def result: Box = ???

  override def done(): Unit =
    while boxes.nonEmpty do
      val hbox = new HBoxBuilder(t, t.getNumber("hsize"))

      @tailrec
      def line(): Unit =
        if boxes.nonEmpty then
          if hbox.size + boxes.head.width <= t.getNumber("hsize") then
            hbox add boxes.remove(0)
            line()
          else
            boxes.head match
              case b: CharBox =>
                b.text.indexOf('-') match
                  case -1 =>
                    Hyphenation(b.text) match
                      case None =>
                      case Some(hyphenation) =>
                        var lastBefore: CharBox = null
                        var lastAfter: String = null

                        @tailrec
                        def longest(): Unit =
                          if hyphenation.hasNext then
                            val (before, after) = hyphenation.next
                            val beforeHyphen = b.newCharBox(before)

                            if hbox.size + beforeHyphen.width <= t.getNumber("hsize") then
                              lastBefore = beforeHyphen
                              lastAfter = after
                              longest()

                        longest()

                        if lastBefore ne null then
                          hbox add lastBefore
                          boxes.remove(0)
                          boxes.insert(0, b.newCharBox(lastAfter))
                    end match
                  case idx =>
                    val beforeHyphen = b.newCharBox(b.text.substring(0, idx + 1))

                    if hbox.size + beforeHyphen.width <= t.getNumber("hsize") then
                      hbox add beforeHyphen
                      boxes.remove(0)
                      boxes.insert(0, b.newCharBox(b.text.substring(idx + 1)))
              case _ =>
            end match

      line()

      if hbox.last.isSpace then hbox.removeLast()
      if boxes.nonEmpty && boxes.head.isSpace then boxes.remove(0)
      if boxes.isEmpty then hbox add new HSpaceBox(2)

      val newLine = hbox.result

      pageMode add newLine
    end while

    t.indentParagraph = true
    pop
  end done
