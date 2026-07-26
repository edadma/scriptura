package io.github.edadma.scriptura

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

/** Exercises the preview's typesetting path — the bridge from editor source to the page surfaces the
  * GUI blits — without opening a window. With no display scale applied (the default device-pixel
  * ratio is 1.0) the logical page size equals the screen-resolution pixel size.
  */
class TypesetGuiTests extends AnyFreeSpec with Matchers:

  // Nothing here needs a texish tree — the core faces and the base/document packages are inside the
  // texish artifact — but the app points the engine at one when there is one to point at, so do the
  // same before any test typesets and exercise the path the app actually takes.
  offerBundledFonts()

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

  // The format that ships with the engine is compiled into the texish artifact, so it resolves with
  // nothing configured — no TEXISHHOME, no texish home, no package folder on disk. This is the
  // round-trip on Native, and it is what lets scriptura run on a machine that has no texish tree.
  "\\use{document} resolves the shipped format with nothing configured" in {
    val restore = io.github.edadma.texish.Typesetter.home

    io.github.edadma.texish.Typesetter.home = ""
    try
      val (pages, log, ok) = typeset("\\use{document}\n\\title{T}\\maketitle\nHello.\n\n")

      withClue(s"log: $log") { ok shouldBe true }
      pages.length should be >= 1
    finally io.github.edadma.texish.Typesetter.home = restore
  }

  // The families beyond the core are opt-in per typesetter, so the preview has to ask for them. If it stops
  // asking, a document that sets Hebrew or Chinese does not fail loudly — it fails only for the person who
  // wrote it, at the point they try, which is exactly the kind of regression nobody notices for months.
  // Skipped where this machine has no texish font tree to offer; there is nothing to load and nothing to check.
  "the preview offers the bundled families, not only the core" in {
    assume(io.github.edadma.texish.Typesetter.home.nonEmpty, "no texish font tree on this machine")

    val (pages, log, ok) = typeset("\\font{hebrew}{12}\nבְּרֵאשִׁית\n\n")

    withClue(s"log: $log") { ok shouldBe true }
    pages.length shouldBe 1
  }

  // Only `base` and `document` are compiled into the texish artifact; every other package — diagram, plot,
  // book, usfm, … — lives in a packages/ folder on disk. Loading one exercises the other half of what the
  // texish home is for, and it is a separate code path from the font search: pointing the engine at a tree
  // used to configure the fonts and leave \use unable to find anything, which reads as a broken install.
  // Skipped where this machine has no texish tree to offer; there is nothing to resolve and nothing to check.
  "a package that is not compiled into the engine resolves in the preview" in {
    assume(io.github.edadma.texish.Typesetter.home.nonEmpty, "no texish tree on this machine")

    val (pages, log, ok) = typeset("\\use{document}\n\\use{diagram}\nHello.\n\n")

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
