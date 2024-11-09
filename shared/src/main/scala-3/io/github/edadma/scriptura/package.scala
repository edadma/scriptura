package io.github.edadma.scriptura

def problem(msg: String): Nothing =
  Console.err.println(msg)
  sys.exit(1)
