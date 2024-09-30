package io.github.edadma.typesetter

import pprint.pprintln

class PageMode(protected val t: Typesetter) extends Mode:
  infix def add(box: Box): Unit = {}

  def result: Box = ZeroGlue

//class PageMode(protected val t: Typesetter, book: Mode, val pageFactory: PageFactory) extends Mode:
//  protected[typesetter] var firstParagraph: Boolean = true
//
//  def newPage: PageBox = pageFactory(t, t.pageWidth, t.pageHeight)
//
//  protected[compositor] var page: PageBox = newPage
//
//  def add(box: Box): Unit =
//    if box.typ == Type.Start then start add box
//    else addLine(box)
//
//  infix def addLine(box: Box): Unit =
//    if page.pointLength + box.height > t.pageHeight then
//      page.lastOption foreach { // todo: only internally generated interline spacing should be removed
//        case _: VSpaceBox => page.remove(page.length - 1)
//        case _            =>
//      }
//      book.add(page)
//      page = newPage
//    end if
//
//    // todo: only internally generated interline spacing should be removed
//    if page.nonEmpty || !box.isInstanceOf[VSpaceBox] then page add box
//
//  def result: PageBox = page
//
//  def start: ParagraphMode =
//    val paragraphMode = new ParagraphMode(t, this)
//
//    t.modeStack push paragraphMode
//
//    if t.indentParagraph && !firstParagraph then paragraphMode add new HSpaceBox(0, t.parindent, 0)
//    else firstParagraph = false
//
//    paragraphMode
