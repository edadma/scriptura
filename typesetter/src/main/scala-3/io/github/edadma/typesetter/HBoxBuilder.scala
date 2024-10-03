package io.github.edadma.typesetter

class HBoxBuilder extends Builder:

  def buildTo(width: Double): HBox = HBox(buildTo(width, boxes, _.width, HSpaceBox(_)))

  def build: HBox = HBox(boxes.toList)

  def width: Double = size(_.width)
