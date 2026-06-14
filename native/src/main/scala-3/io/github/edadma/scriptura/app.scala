package io.github.edadma.scriptura

import io.github.edadma.texish.{CairoPDFTypesetter, Typesetter, standardPrelude}
import io.github.edadma.texish.parser.{Processor, TypesetterHandler, registerTypesettingPrimitives}

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

    val t: Typesetter =
      args match
        case Config(_, _, "pdf", Some("a4"), _, _, _, _, _) =>
          new CairoPDFTypesetter(output.toString) {
            set("paperwidth", 210 * mm)
            set("paperheight", 297 * mm)
          }
        case Config(_, _, "pdf", Some("letter"), _, _, _, _, _) =>
          new CairoPDFTypesetter(output.toString) {
            set("paperwidth", 8.5 * in)
            set("paperheight", 11 * in)
          }
        case Config(_, _, "pdf", None, _, _, _, _, _) =>
          new CairoPDFTypesetter(output.toString)

//        case Config(_, _, "png", _, resolution, size, _, _) =>
//          val (width, height) =
//            resolution match
//              case "sd"  => (720, 480)
//              case "hd"  => (1280, 720)
//              case "fhd" => (1920, 1080)
//
//          Compositor.png(output.toString, width, height, ppi(width, height, size), simplePageFactory())
        case _ => sys.error("error")

    if args.usfx then () // USFX.fromString(doc, in)
    else
      val handler = new TypesetterHandler(t)
      val proc = new Processor(handler)
      registerTypesettingPrimitives(proc, handler)
      proc.process(standardPrelude) // standard macro definitions (\TeX, \TeXish, …) before the document
      proc.process(input)
    end if

    t.end()
    t.destroy()
  end process
