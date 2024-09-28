package io.github.edadma.typesetter

import scala.collection.mutable.ListBuffer

class HBoxBuilder:

  private var boxes = new ListBuffer[Box]

  // Add a box to the builder
  def addBox(box: Box): HBoxBuilder =
    boxes += box
    this // Return the builder for chaining

  // Add a flexible GlueBox
  def addGlue(naturalWidth: Double, stretch: Double = 0, shrink: Double = 0): HBoxBuilder =
    boxes += new GlueBox(naturalWidth, stretch, shrink)
    this

  // Produce an HBox of a specific width
  def buildTo(width: Double): HBox =
    val totalNaturalWidth = boxes.collect { case b: Box if !b.isInstanceOf[GlueBox] => b.width }.sum
    val glueBoxes = boxes.collect { case glue: GlueBox => glue }
    val totalGlueNaturalWidth = glueBoxes.map(_.naturalWidth).sum
    val totalWidth = totalNaturalWidth + totalGlueNaturalWidth
    val remainingWidth = width - totalWidth

    if remainingWidth > 0 then
      // Distribute extra space (stretch)
      val totalStretch = glueBoxes.map(_.stretch).sum
      val finalBoxes = boxes.map {
        case glue: GlueBox if totalStretch > 0 =>
          val extra = remainingWidth * (glue.stretch / totalStretch)
          new HSkipBox(glue.naturalWidth + extra)
        case glue: GlueBox => new HSkipBox(glue.naturalWidth) // No stretch applied
        case box           => box
      }

      // Ensure the total width is exactly the requested width
      val finalTotalWidth = finalBoxes.map(_.width).sum
      val adjustment = width - finalTotalWidth // Calculate how much we need to adjust
      if finalBoxes.nonEmpty then
        // Adjust the last box to match the exact width
        finalBoxes.last match
          case hskip: HSkipBox =>
            val adjustedBox = new HSkipBox(hskip.width + adjustment)
            val adjustedFinalBoxes = finalBoxes.dropRight(1) :+ adjustedBox
            new HBox(adjustedFinalBoxes.toList)
          case _ => new HBox(finalBoxes.toList) // If no skip box, just use the final boxes
      else new HBox(finalBoxes.toList)
    else if remainingWidth < 0 then
      // Distribute compression (shrink)
      val totalShrink = glueBoxes.map(_.shrink).sum
      val finalBoxes = boxes.map {
        case glue: GlueBox if totalShrink > 0 =>
          val shrinkAmount = remainingWidth * (glue.shrink / totalShrink)
          new HSkipBox((glue.naturalWidth + shrinkAmount).max(0)) // Prevent negative width
        case glue: GlueBox => new HSkipBox(glue.naturalWidth) // No shrink applied
        case box           => box
      }

      // Ensure the total width is exactly the requested width
      val finalTotalWidth = finalBoxes.map(_.width).sum
      val adjustment = width - finalTotalWidth
      if finalBoxes.nonEmpty then
        // Adjust the last box to match the exact width
        finalBoxes.last match
          case hskip: HSkipBox =>
            val adjustedBox = new HSkipBox(hskip.width + adjustment)
            val adjustedFinalBoxes = finalBoxes.dropRight(1) :+ adjustedBox
            new HBox(adjustedFinalBoxes.toList)
          case _ => new HBox(finalBoxes.toList)
      else new HBox(finalBoxes.toList)
    else
      // Perfect fit, no adjustment needed
      new HBox(boxes.toList)

  // Produce an HBox with just the boxes added
  def build(): HBox =
    new HBox(boxes.toList)
