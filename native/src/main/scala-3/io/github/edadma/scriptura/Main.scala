package io.github.edadma.scriptura

import java.io.File

import io.github.edadma.texish.{Install, Typesetter}

/** Point texish at the `fonts/` and `packages/` an installation ships, so a document can set Hebrew or draw a
  * diagram without the user configuring anything.
  *
  * Two places are tried. First the installation: `Install.configure()` locates this executable and looks for a
  * texish tree beside it, which is how a packaged scriptura finds the texish package it depends on — a package
  * manager links both programs into one prefix and links texish's data alongside them, so the tree is there even
  * though it belongs to a different package. Then, failing that, a texish source tree where this machine keeps
  * one, which is the case when scriptura is being run from a build.
  *
  * Nothing depends on either succeeding: the core faces and the `base` and `document` packages are compiled into
  * the texish artifact, so the preview renders an ordinary document with no tree at all. What a tree adds is the
  * wider font families and the packages beyond those two — and where there is none, a document naming one is told
  * which file was missing rather than left to guess.
  *
  * Set before any typesetter is constructed — texish reads it when one is built. Asking for the wider font
  * families is separate and happens per typesetter (see typeset and typesetPdf in gui.scala).
  */
private[scriptura] def offerBundledFonts(): Unit =
  Install.configure()

  if Typesetter.home.isEmpty then
    val checkout = new File(System.getProperty("user.home"), "dev/texish")

    if new File(checkout, "fonts").isDirectory then Typesetter.home = checkout.getPath

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
