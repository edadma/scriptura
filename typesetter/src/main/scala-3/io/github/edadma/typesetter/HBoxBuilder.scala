package io.github.edadma.typesetter

import scala.collection.mutable.ListBuffer

class HBoxBuilder:

  private var boxes = new ListBuffer[Box]

  // Add a box to the builder
  def addBox(box: Box): HBoxBuilder =
    boxes += box
    this // Return the builder for chaining

  // Produce an HBox of a specific width
  def buildTo(width: Double): HBox =
    val finalBoxes = boxes

    new HBox(finalBoxes.toList)

  // Produce an HBox with just the boxes added
  def build(): HBox =
    new HBox(boxes.toList)
