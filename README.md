# Scriptura

A GUI front-end to the [texish](../texish) typesetting engine: edit a document's
source on the left, see the typeset pages on the right. The headless renderer is
the texish CLI; this app is the editor and live preview.

## Running

Scriptura does not bundle fonts or packages — those ship with the engine. Point
`TEXISHHOME` at a texish checkout so the engine finds its `fonts/` and
`packages/` no matter which directory the app was launched from:

    export TEXISHHOME=/path/to/texish
    sbt scripturaNative/nativeLink
    ./native/target/scala-3.8.4/scriptura scripts/test.script

The native app sets `TEXISHHOME` itself, defaulting to `$HOME/dev/texish`.

The Zoom control sets the preview's scale, from 50% to 200%. 100% is the page at
its true size. Zoom changes the resolution the engine lays the page out at and
re-typesets, rather than magnifying the finished image, so type stays sharp at
every level; it does not affect what Print produces.

Hyphenation is the document's choice, not the previewer's: a source turns it on
with `\usehyphenation{en-us}` (bundled: `en-us`, `es`, `fr`, `it`, `pt`), or
`\loadhyphenation{tag}{path}` for a language that is not bundled. The preview
applies no language of its own, so what you see matches what the CLI renders.

Note that texish comments are `//` — a `%` is an ordinary character and will be
typeset into the document.

## Sample documents

`scripts/` holds sample and smoke-test documents. Render one headlessly with the
texish CLI:

    texish scripts/math.script -o math.pdf

## License

- **Scriptura**: ISC License

Fonts are distributed with texish under their respective licenses (Google Fonts
and Computer Modern under the SIL Open Font License).
