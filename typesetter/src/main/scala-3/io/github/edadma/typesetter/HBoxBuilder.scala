package io.github.edadma.typesetter

class HBoxBuilder extends Buildable:

  def buildTo(width: Double): HBox = HBox(buildTo(width, boxes, _.width, HSpaceBox(_)))

  def build: HBox = HBox(boxes.toList)
