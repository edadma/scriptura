package io.github.edadma.typesetter

import scala.collection.mutable.ArrayBuffer

trait Builder extends Mode:
  protected val boxes = new ArrayBuffer[Box]

  def init(): Unit = ()

  def clear(): Unit = boxes.clear()

  def last: Box = boxes.last

  def lastOption: Option[Box] = boxes.lastOption

  def removeLast(): Box = boxes.remove(boxes.length - 1)

  def length: Int = boxes.length

  def update(index: Int, elem: Box): Unit = boxes.update(index, elem)

  def nonEmpty: Boolean = boxes.nonEmpty

  def isEmpty: Boolean = boxes.isEmpty

  infix def add(b: Box): Unit = boxes += b
