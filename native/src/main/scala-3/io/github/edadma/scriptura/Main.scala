package io.github.edadma.scriptura

import java.io.File

import io.github.edadma.texish.Typesetter

/** Offer texish the wider bundled font set if a texish source tree is sitting where this machine keeps
  * one. Nothing depends on it: the Latin Modern core and the standard packages are compiled into the
  * texish artifact, so the engine renders — and `\use{document}` resolves — with no configuration at
  * all. This only adds the families that are too large to ship inside the artifact: the complex-script
  * faces, the CJK cuts, the alternative text families. Pointed at nothing, texish simply does without
  * them and a document naming one gets a clear "typeface not found".
  *
  * Set before any typesetter is constructed — texish registers its bundled faces in the constructor. */
private[scriptura] def offerBundledFonts(): Unit =
  val checkout = new File(System.getProperty("user.home"), "dev/texish")

  if new File(checkout, "fonts").isDirectory then Typesetter.fontsDir = checkout.getPath

/** Scriptura is a GUI front-end to the texish typesetting engine. It opens a preview window: edit a
  * document's source on the left and see the typeset pages on the right. The headless renderer is the
  * texish CLI; this app is only the editor and live preview.
  *
  * An optional file path on the command line seeds the editor with that document; otherwise it opens
  * with a short sample.
  */
@main def run(args: String*): Unit =
  offerBundledFonts()
  scripturaGui(args.headOption.map(new File(_)))
