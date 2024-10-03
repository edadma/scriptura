package io.github.edadma.typesetter

class VBoxBuilder extends Builder:

  def buildTo(height: Double): VBox = VBox(buildTo(height, boxes, _.height, VSpaceBox(_)))

  def build: VBox = VBox(boxes.toList)

  def height: Double = size(_.height)
