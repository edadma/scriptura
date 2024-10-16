package io.github.edadma.typesetter

class TestFoldedDocument extends TestDocument:
  val folds = 2

  override infix def add(box: Box): Unit =
    val fold = page % folds
    val width = t.getNumber("paperwidth") / folds

    if fold == 0 then
      t.createPageTarget(t.getNumber("paperwidth"), t.getNumber("paperheight"))
      eject = true

    pages += t.renderToTarget(
      layout(box),
      fold * width + t.getNumber("hoffset"),
      t.getNumber("voffset"),
    )

    if fold == folds - 1 then
      t.ejectPageTarget()
      eject = false

    page += 1
