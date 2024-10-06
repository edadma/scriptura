package io.github.edadma.typesetter

class VBoxBuilder(protected val t: Typesetter, protected val toSize: Double | Null = null) extends ListBoxBuilder:

  protected val measure: Box => Double = _.height
  protected val skip: Double => Box = VSpaceBox(_)
  protected val wrap: Seq[Box] => Box = VBox(_)
