package io.github.edadma.typesetter

class HBoxBuilder extends Buildable:

  // Produce an HBox of a specific width
  def buildTo(width: Double): HBox = HBox(buildTo(width, boxes, _.width, HSkipBox(_)))

  // Produce an HBox with just the boxes added
  def build: HBox = HBox(boxes.toList)
