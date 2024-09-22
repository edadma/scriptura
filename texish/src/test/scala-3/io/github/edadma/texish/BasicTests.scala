package io.github.edadma.texish

import org.scalatest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class BasicTests extends AnyFreeSpec with Matchers with Testing:
  "characters" in {
    test("asdf") shouldBe "asdf"
  }

  "simple text" in {
    test("asdf zxcv") shouldBe
      """
        |["asdf",  , "zxcv"]
      """.trim.stripMargin
  }
