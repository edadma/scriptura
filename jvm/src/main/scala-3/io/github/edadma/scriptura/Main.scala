package io.github.edadma.scriptura

import io.github.edadma.texish.Processor
import io.github.edadma.typesetter.Graphics2DTypesetter

import java.io.File
import javax.imageio.ImageIO
import scala.io.Source

@main def run(args: String*): Unit =
  if args.isEmpty then
    println("Usage: run <script-file>")
    System.exit(1)

  val scriptFile = args.head
  val source = Source.fromFile(scriptFile)
  val scriptContent = source.mkString
  source.close()

  val t = new Graphics2DTypesetter
  val handler = new ScripturaHandler(t)
  val proc = new Processor(handler)
  registerScripturaPrimitives(proc, handler)

  proc.process(scriptContent)
  t.end()

  // Write output pages
  val pages = t.getDocument.printedPages.toList
  pages.zipWithIndex.foreach { case (page, i) =>
    val outFile = new File(s"output-page-${i + 1}.png")
    ImageIO.write(page.asInstanceOf[java.awt.image.BufferedImage], "PNG", outFile)
    println(s"Wrote ${outFile.getName}")
  }
