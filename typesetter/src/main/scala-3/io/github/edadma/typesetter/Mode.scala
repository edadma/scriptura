package io.github.edadma.typesetter

trait Mode:
  protected val t: Typesetter

  infix def add(box: Box): Unit

  def result: Box

  def done(): Unit =
    pop
    t.add(result)

  def pop: Mode = t.modeStack.pop

  def top: Mode = t.modeStack.top
