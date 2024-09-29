package io.github.edadma.typesetter

class VBoxBuilder extends ListBoxBuilder:

  // Produce an HBox of a specific width
  def buildTo(height: Double): VBox = VBox(buildTo(height, boxes, _.height, VSkipBox(_)))

  // Produce an HBox with just the boxes added
  def build: VBox = VBox(boxes.toList)
