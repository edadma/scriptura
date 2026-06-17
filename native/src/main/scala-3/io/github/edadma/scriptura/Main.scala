package io.github.edadma.scriptura

import java.io.File

import scala.scalanative.unsafe.{Zone, toCString}
import scala.scalanative.posix.stdlib.setenv

/** Point TEXISHHOME at the texish checkout, so texish's package resolver finds the formats that ship
  * with the engine (`\use{document}`, `\use{logos}`, …) under `$TEXISHHOME/packages` no matter where
  * the app was launched from. Set in the running process's environment, so the engine reads it when a
  * document's `\use` runs. */
private[scriptura] def setTexishHome(): Unit =
  val texishHome = s"${System.getProperty("user.home")}/dev/texish"
  Zone(setenv(toCString("TEXISHHOME"), toCString(texishHome), 1))

/** Scriptura is a GUI front-end to the texish typesetting engine. It opens a preview window: edit a
  * document's source on the left and see the typeset pages on the right. The headless renderer is the
  * texish CLI; this app is only the editor and live preview.
  *
  * An optional file path on the command line seeds the editor with that document; otherwise it opens
  * with a short sample.
  */
@main def run(args: String*): Unit =
  setTexishHome()
  scripturaGui(args.headOption.map(new File(_)))
