package io.github.edadma.scriptura

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

/** Exercises the preview's typesetting path — the bridge from editor source to the page surfaces the
  * GUI blits — without opening a window. With no display scale applied (the default device-pixel
  * ratio is 1.0) the logical page size equals the screen-resolution pixel size.
  */
class TypesetGuiTests extends AnyFreeSpec with Matchers:

  "typesetting a short document yields one letter page sized at the screen resolution" in {
    val (pages, _) = typeset("Hello world\n\n")

    pages.length shouldBe 1
    // letter is 612x792pt; at the 96dpi screen resolution that is 816x1056 logical pixels
    pages.head.w shouldBe 816.0
    pages.head.h shouldBe 1056.0
  }

  "typesetting a multi-page document ships one page surface per page" in {
    val (pages, _) = typeset("first\n\n\\vfill\\eject second\n\n\\vfill\\eject third\n\n")

    pages.length shouldBe 3
    pages.foreach { p =>
      p.w should be > 0.0
      p.h should be > 0.0
    }
  }
