package io.github.edadma.typesetter

class Font(
    val family: String,
    val size: Double,
    //                 extents: FontExtents,
    val space: Double,
    val style: Set[String],
    val fontFace: Any,
    val baseline: Option[Double],
    val ligatures: Set[String],
)
