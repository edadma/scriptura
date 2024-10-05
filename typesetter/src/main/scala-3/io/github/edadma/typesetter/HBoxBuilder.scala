package io.github.edadma.typesetter

class HBoxBuilder(protected val t: Typesetter, val toSize: Double | Null = null) extends Builder:

  protected val measure: Box => Double = _.width
  protected val skip: Double => Box = HSpaceBox(_)
  protected val wrap: Seq[Box] => Box = HBox(_)
