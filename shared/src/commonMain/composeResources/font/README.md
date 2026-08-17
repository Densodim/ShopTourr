# Bundled fonts

| file | family | source |
| --- | --- | --- |
| `jetbrains_mono_regular.ttf` | JetBrains Mono Regular (variable, default instance) | Google Fonts CDN, `fonts.gstatic.com/s/jetbrainsmono/v24` |

Licensed under the SIL Open Font License 1.1 (<https://scripts.sil.org/OFL>),
which permits embedding in an application. Copyright: JetBrains s.r.o.
The full OFL text is not checked in yet; add `OFL.txt` here before shipping a
store build so the licence travels with the fonts.

## Why Instrument Serif is not here

The mock asks for Instrument Serif on headings and amounts, but that family
ships Latin only — no U+0400–04FF. This app is Russian-first, so bundling it
would leave every Russian heading on a fallback face while Latin words in the
same line used Instrument Serif; "Привет, Dima" rendered in two different
fonts.

The mock has the same gap: its stack is `'Instrument Serif', Georgia, serif`,
so the serif seen on Russian headings there is Georgia, not Instrument Serif.
The platform serif (`FontFamily.Serif`) is therefore the closer match, and the
theme keeps it.

To bundle a real display serif, pick one with Cyrillic — Playfair Display,
Prata and Lora are the nearest in character and all cover it.
