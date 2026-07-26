package io.github.edadma.scriptura

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

/** Exercises the preview's typesetting path — the bridge from editor source to the page surfaces the
  * GUI blits — without opening a window. With no display scale applied (the default device-pixel
  * ratio is 1.0) the logical page size equals the screen-resolution pixel size.
  */
class TypesetGuiTests extends AnyFreeSpec with Matchers:

  // The bundled fonts ship with the engine, not with this app, so the typesetter can only open them
  // once TEXISHHOME points at the texish checkout. The app does this at startup; do it here too,
  // before any test typesets, so no test depends on having been run after another one that set it.
  setTexishHome()

  "typesetting a short document yields one letter page sized at the screen resolution" in {
    val (pages, _, _) = typeset("Hello world\n\n")

    pages.length shouldBe 1
    // letter is 612x792pt; at the 96dpi screen resolution that is 816x1056 logical pixels
    pages.head.w shouldBe 816.0
    pages.head.h shouldBe 1056.0
  }

  "typesetting a multi-page document ships one page surface per page" in {
    val (pages, _, _) = typeset("first\n\n\\vfill\\eject second\n\n\\vfill\\eject third\n\n")

    pages.length shouldBe 3
    pages.foreach { p =>
      p.w should be > 0.0
      p.h should be > 0.0
    }
  }

  // The app sets TEXISHHOME in the running process so texish's \use resolver finds the formats that
  // ship with the engine under $TEXISHHOME/packages. This checks the whole round-trip on Native: the
  // programmatic setenv is visible to the engine, and \use{document} (which itself \use{logos}) loads.
  "with TEXISHHOME set, \\use{document} resolves the shipped format" in {
    setTexishHome()
    val (pages, log, ok) = typeset("\\use{document}\n\\title{T}\\maketitle\nHello.\n\n")

    withClue(s"log: $log") { ok shouldBe true }
    pages.length should be >= 1
  }

  "zoom scales the page, because it scales the resolution the engine lays out at" in {
    val (base, _, _)  = typeset("Hello world\n\n")
    val (big, _, _)   = typeset("Hello world\n\n", zoom = 2.0)
    val (small, _, _) = typeset("Hello world\n\n", zoom = 0.5)

    big.head.w shouldBe (base.head.w * 2 +- 1.0)
    big.head.h shouldBe (base.head.h * 2 +- 1.0)
    small.head.w shouldBe (base.head.w * 0.5 +- 1.0)
    small.head.h shouldBe (base.head.h * 0.5 +- 1.0)
  }

  "every offered zoom level is a positive factor, and the default is one of them" in {
    ZoomLevels.map(_._1) should contain(DefaultZoom)
    DefaultZoom.toDouble shouldBe 1.0
    ZoomLevels.foreach { case (factor, label) =>
      withClue(s"level '$label': ") { factor.toDoubleOption.getOrElse(0.0) should be > 0.0 }
    }
  }

  // --- jumping to a diagnostic ---------------------------------------------

  "the source position is read out of a texish diagnostic" in {
    val log = "Unknown command: \\TeX (line 7, column 118):\nThis document is typeset by \\TeX.\n     ^"

    errorPosition(log) shouldBe Some((7, 118))
  }

  "a log with no position, or no message at all, offers nothing to jump to" in {
    errorPosition("") shouldBe None
    errorPosition("wrote out.pdf") shouldBe None
  }

  "a line and column resolve to the offset of that character" in {
    val src = "alpha\nbravo\ncharlie\n"

    offsetOf(src, 1, 1) shouldBe 0             // the very start
    offsetOf(src, 2, 1) shouldBe 6             // just past "alpha\n"
    offsetOf(src, 3, 4) shouldBe 12 + 3        // the 'r' of charlie
    src.charAt(offsetOf(src, 3, 4)) shouldBe 'r'
  }

  "a position past the end of its line or of the text is clamped, not thrown" in {
    val src = "alpha\nbravo\n"

    offsetOf(src, 1, 999) shouldBe 5            // clamped to the end of line one, not into line two
    offsetOf(src, 99, 1) should be <= src.length
    offsetOf("", 5, 5) shouldBe 0
    offsetOf(src, 0, 0) shouldBe 0              // a nonsensical position still lands somewhere real
  }

  // --- page navigation -------------------------------------------------------

  "each page's top is its predecessors' heights plus the borders and gaps between them" in {
    val tops = pageTops(Seq(100.0, 200.0, 50.0))

    tops.length shouldBe 3
    tops(0) shouldBe 8.0                        // the column's padding
    tops(1) shouldBe 8.0 + 100 + 2 + 12         // page, its two borders, the gap
    tops(2) shouldBe 8.0 + 100 + 2 + 12 + 200 + 2 + 12
  }

  "no pages means no offsets" in {
    pageTops(Nil) shouldBe empty
  }

  "the page being shown is the last one whose top has reached the viewport" in {
    val tops = pageTops(Seq(100.0, 100.0, 100.0))

    pageAt(tops, 0.0) shouldBe 0
    pageAt(tops, tops(1) - 5) shouldBe 0        // still showing page one
    pageAt(tops, tops(1)) shouldBe 1            // exactly at page two's top
    pageAt(tops, tops(2) + 40) shouldBe 2       // scrolled into page three
    pageAt(Nil, 500.0) shouldBe 0               // nothing to be on
  }
