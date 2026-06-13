package io.github.edadma.scriptura

import java.io.File

case class Config(
    input: Option[File] = None,
    output: String = null,
    typ: String = null,
    paper: Option[String] = None,
    resolution: String = "hd",
    size: Double = 14,
    multi: Boolean = false,
    usfx: Boolean = false,
    gui: Boolean = false,
)

@main
def run(args: String*): Unit =
  import scopt.OParser
  val builder = OParser.builder[Config]
  val parser = {
    import builder.*
    OParser.sequence(
      programName("scriptura"),
      head("Scriptura", "v0.0.1"),
      arg[File]("<input file>")
        .optional()
        .action((x, c) => c.copy(input = Some(x)))
        .text("input file (defaults to standard input)"),
      help("help").text("prints this usage text"),
      opt[Unit]('m', "multi")
        .action((_, c) => c.copy(multi = true))
        .text("multi"),
      opt[String]('o', "output")
        .valueName("<output file>")
        .action((x, c) => c.copy(output = x))
        .text("output file (defaults to <input>.<type>)"),
      opt[String]('p', "paper")
        .valueName("<a4 | letter>")
        .action((x, c) => c.copy(paper = Some(x)))
        .validate({
          case "a4" | "letter" => success
          case _               => failure("only 'a4' or 'letter' are allowed as paper types")
        })
        .text("paper size (defaults to letter)"),
      opt[String]('r', "resolution")
        .valueName("<sd | hd | fhd>")
        .action((x, c) => c.copy(resolution = x))
        .validate({
          case "sd" | "hd" | "fhd" => success
          case _                   => failure("only 'sd' | 'hd' | 'fhd' are allowed as resolutions")
        })
        .text("resolution (defaults to hd)"),
      opt[Int]('s', "size")
        .valueName("<inches>")
        .action((x, c) => c.copy(size = x))
        .validate(s =>
          if 0 < s then success
          else failure("only positive values are allowed as screen sizes"),
        )
        .text("screen size in inches for PNG file output (defaults to 13)"),
      opt[String]('t', "type")
        .valueName("<pdf | png>")
        .action((x, c) => c.copy(typ = x))
        .validate({
          case "png" | "pdf" => success
          case _             => failure("only 'png' or 'pdf' are allowed as output file types")
        })
        .text("output file type (defaults to pdf, or png for multi mode)"),
      opt[Unit]('u', "usfx")
        .action((_, c) => c.copy(usfx = true))
        .text("USFX"),
      opt[Unit]('g', "gui")
        .action((_, c) => c.copy(gui = true))
        .text("open the preview GUI (seeded with the input file, if given)"),
      version("version").text("prints the current version"),
    )
  }

  def config: PartialFunction[Config, Unit] = {
    case c @ Config(None, null, _, _, _, _, _, _, _)       => config(c.copy(output = "out"))
    case c @ Config(Some(file), null, _, _, _, _, _, _, _) => config(c.copy(output = file.toString))
    case c @ Config(_, _, null, _, _, _, false, _, _)      => config(c.copy(typ = "pdf"))
    case c @ Config(_, _, null, _, _, _, true, _, _)       => config(c.copy(typ = "png"))
    case c                                                 => app(c)
  }

  OParser.parse(parser, args, Config()) match {
    case Some(c) if c.gui => scripturaGui(c)
    case Some(c)          => config(c)
    case _                =>
  }
