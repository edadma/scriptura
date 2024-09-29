package io.github.edadma.typesetter

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class HBoxBuilder:

  private val boxes = new ArrayBuffer[Box]

  // Add a box to the builder
  def addBox(box: Box): HBoxBuilder =
    boxes += box
    this // Return the builder for chaining

  // Add a flexible GlueBox
  def addGlue(naturalWidth: Double, stretch: Double = 0, shrink: Double = 0): HBoxBuilder =
    addBox(new OrdinaryGlueBox(naturalWidth, stretch, shrink))

  def addFil: HBoxBuilder = addBox(new FilGlueBox())

  // Produce an HBox of a specific width
  def buildTo(width: Double): HBox = {
    // Step 1: Calculate the natural width of all boxes
    val naturalWidth = boxes.map(_.width).sum
    var delta = width - naturalWidth

    // Step 2: Collect all GlueBoxes with their indices
    val glueBoxesWithIndices = boxes.zipWithIndex.collect { case (g: GlueBox, idx) =>
      (g, idx)
    }

    // If there are no GlueBoxes, return the boxes as-is (or handle accordingly)
    if (glueBoxesWithIndices.isEmpty) {
      if (delta != 0) {
        println("Warning: No glue available to adjust the width.")
      }
      return HBox(boxes.toList)
    }

    // Step 3: Determine the maximum glue order present
    val maxOrder = glueBoxesWithIndices.map(_._1.typ.order).max

    // Function to distribute space (stretch or shrink)
    def distributeSpace(
        remaining: Double,
        glueBoxes: scala.collection.Seq[(GlueBox, Int)],
        totalFlex: Double,
        adjust: (GlueBox, Double) => Double,
    ): Double = {
      if (totalFlex == 0) remaining
      else {
        val flexPerUnit = remaining / totalFlex
        glueBoxes.foldLeft(remaining) { case (rem, (g, idx)) =>
          val adjustment = adjust(g, flexPerUnit)
          val newWidth = g.naturalWidth + adjustment
          boxes(idx) = new HSkipBox(newWidth)
          rem - math.abs(adjustment)
        }
      }
    }

    // Step 4: Distribute the delta
    var remaining = delta
    var currentOrder = maxOrder

    while ((remaining > 1e-6 || remaining < -1e-6) && currentOrder >= 0) {
      val currentGlueBoxes = glueBoxesWithIndices.filter(_._1.typ.order == currentOrder)

      if (delta > 0) {
        // Stretching
        val totalStretch = currentGlueBoxes.map(_._1.stretch).sum
        if (totalStretch > 0) {
          val allocated = distributeSpace(
            remaining,
            currentGlueBoxes,
            totalStretch,
            (g, flexPerUnit) => g.stretch * flexPerUnit,
          )
          remaining = delta - (delta - allocated)
        }
      } else {
        // Shrinking
        val totalShrink = currentGlueBoxes.map(_._1.shrink).sum
        if (totalShrink > 0) {
          val allocated = distributeSpace(
            -remaining,
            currentGlueBoxes,
            totalShrink,
            (g, flexPerUnit) => -g.shrink * flexPerUnit,
          )
          remaining = delta + (delta + allocated)
        }
      }

      currentOrder -= 1
    }

    // Step 5: Replace remaining GlueBoxes with their natural widths if any
    glueBoxesWithIndices.foreach { case (g, idx) =>
      if (!boxes(idx).isInstanceOf[HSkipBox]) {
        boxes(idx) = new HSkipBox(g.naturalWidth)
      }
    }

    // Step 6: Verify the final width (optional)
    val finalWidth = boxes.map(_.width).sum
    if (math.abs(finalWidth - width) > 1e-3) {
      println(s"Warning: Final width ($finalWidth) does not match target width ($width).")
    }

    // Return the adjusted boxes as a List
    HBox(boxes.toList)
  }

  // Produce an HBox with just the boxes added
  def build: HBox =
    new HBox(boxes.toList)
