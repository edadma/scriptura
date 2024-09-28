package io.github.edadma.typesetter

import scala.collection.mutable.ListBuffer

class HBoxBuilder:

  private var boxes = new ListBuffer[Box]

  // Add a box to the builder
  infix def addBox(box: Box): HBoxBuilder =
    boxes += box
    this // Return the builder for chaining

  // Add a flexible GlueBox
  def addGlue(): HBoxBuilder =
    boxes += new GlueBox()
    this

  // Produce an HBox of a specific width
  def buildTo(width: Double): HBox =
    val totalWidth = boxes.collect { case b: Box if !b.isInstanceOf[GlueBox] => b.xAdvance }.sum
    val glueCount = boxes.count(_.isInstanceOf[GlueBox])

    if glueCount == 0 then
      // No glue present, just return the boxes as is
      return new HBox(boxes.toList)

    // Calculate the width for each GlueBox
    val remainingWidth = (width - totalWidth).max(0)
    val glueWidth = remainingWidth / glueCount

    // Replace GlueBoxes with rigid HSkipBox's
    val finalBoxes = boxes.map {
      case glue: GlueBox => new HSkipBox(glueWidth)
      case box           => box
    }

    new HBox(finalBoxes.toList)

  // Produce an HBox with just the boxes added
  def build: HBox =
    new HBox(boxes.toList)
