package io.github.edadma.typesetter

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

class ParagraphMode(protected val t: Typesetter, pageMode: PageMode) extends HorizontalMode:
  val boxes = new ArrayBuffer[Box]

  protected def addBox(box: Box): Unit = boxes += box

  protected def nonEmpty: Boolean = boxes.nonEmpty

  protected def last: Box = boxes.last

  protected def length: Int = boxes.length

  protected def update(index: Int, elem: Box): Unit = boxes.update(index, elem)

  def result: Box = ???

  override def done(): Unit =
    var firstLine = true

    while boxes.nonEmpty do
      val hbox = new HBoxBuilder

      @tailrec
      def line(): Unit =
        if boxes.nonEmpty then
          if hbox.size(_.width) + boxes.head.width <= pageMode.result.lineWidth then
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

                            if hbox.width + beforeHyphen.width <= pageMode.result.lineWidth then
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

                    if hbox.size(_.width) + beforeHyphen.width <= pageMode.result.lineWidth then
                      hbox add beforeHyphen
                      boxes.remove(0)
                      boxes.insert(0, b.newCharBox(b.text.substring(idx + 1)))
              case _ =>
            end match

      line()

      if hbox.last.isSpace then hbox.removeLast()
      if boxes.nonEmpty && boxes.head.isSpace then boxes.remove(0)
      if boxes.isEmpty then hbox add new HSpaceBox(2)

      // hbox.setToWidth(pageMode.result.lineWidth)

      if pageMode.result.nonEmpty && !pageMode.result.last.isSpace then
        val last = pageMode.result.last
        val baselines = List(last.baselineHeight, hbox.baselineHeight) filterNot (_.isEmpty) map (_.get)
        val baseline = if baselines.nonEmpty then baselines.sum / baselines.length else 0
        val skip = baseline - last.descent - hbox.ascent

        if skip > 0 then pageMode addLine new VSpaceBox(0, skip, if firstLine then 0.05 else 0)
        if firstLine then firstLine = false

      pageMode addLine hbox
    end while

    comp.indentParagraph = true
    pop
  end done
