package io.github.edadma.scriptura

import io.github.edadma.texish.{CairoImageTypesetter, CairoPDFTypesetter, Typesetter}
import io.github.edadma.texish.parser.{Processor, TypesetterHandler, registerTypesettingPrimitives}
import io.github.edadma.libcairo.Surface

import java.nio.file.{Files, Paths}

def app(args: Config): Unit =
  val input =
    args match
      case Config(None, _, _, _, _, _, _, _, _) => scala.io.Source.stdin.mkString
      case Config(Some(file), _, _, _, _, _, _, _, _) =>
        val path = file.toPath.normalize.toAbsolutePath

        if !Files.exists(path) then problem(s"file '$path' not found")
        else if !Files.isReadable(path) then problem(s"file '$path' is not readable")
        else if !Files.isRegularFile(path) then problem(s"file '$path' is not a file")
        else new String(Files.readAllBytes(file.toPath))

  if args.multi then
    input.split("---.*\n").filterNot(_.matches("\\s*")).zipWithIndex.foreach { case (in, idx) =>
      process(in, f"${idx + 1}% 3d".replace(' ', '_'))
    }
  else process(input, "")

  def process(input: String, suffix: String): Unit =
    val output = Paths.get(s"${args.output}$suffix.${args.typ}").normalize.toAbsolutePath

    if !Files.isWritable(output.getParent) then problem(s"'$output' is not writable")

    // Page size in points (1in = 72pt, 1mm = 72/25.4pt): A4 210x297mm, else US Letter 8.5x11in.
    val (paperW, paperH) =
      if args.paper.contains("a4") then (210 * 72 / 25.4, 297 * 72 / 25.4)
      else (8.5 * 72, 11.0 * 72)

    // Feed the document through a fresh processor and flush the typesetter; the engine ships only primitives,
    // so a document pulls in any higher-level macros (sectioning, lists, logos) by including a format itself.
    def typeset(t: Typesetter): Unit =
      if args.usfx then () // USFX.fromString(doc, in)
      else
        val handler = new TypesetterHandler(t)
        val proc    = new Processor(handler)
        registerTypesettingPrimitives(proc, handler)
        proc.process(input)
      end if
      t.end()

    args.typ match
      case "png" =>
        // One ARGB32 surface per shipped page, rasterised at the chosen device resolution.
        val dpi = args.resolution match
          case "sd"  => 96.0
          case "fhd" => 300.0
          case _     => 150.0 // hd
        val t = new CairoImageTypesetter(dpi)
        t.set("paperwidth", paperW)
        t.set("paperheight", paperH)
        typeset(t)

        val pages = t.getDocument.printedPages
        val base  = output.toString.stripSuffix(".png")
        pages.zipWithIndex.foreach { case (page, i) =>
          val name    = if pages.length == 1 then s"$base.png" else s"${base}_${i + 1}.png"
          val surface = page.asInstanceOf[Surface]
          surface.writeToPNG(name)
          surface.destroy()
          println(s"wrote $name")
        }
        t.destroy()

      case _ => // pdf
        val t = new CairoPDFTypesetter(output.toString)
        t.set("paperwidth", paperW)
        t.set("paperheight", paperH)
        typeset(t)
        t.destroy()
  end process
