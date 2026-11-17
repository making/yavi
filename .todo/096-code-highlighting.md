# Highlight the code blocks in the documentation

Difficulty: Medium

## Goal

Fenced Java, XML, Kotlin, JSON and property examples are rendered as monochrome text.
Give them syntax highlighting so the documentation reads like a reference, in both the
light and dark themes.

## Scope

- Highlight static code blocks only. Runnable cells must keep working unchanged.
- Self-contained: no CDN and no network request at view time. Prefer a build-time
  highlighter; a bundled client-side highlighter (highlight.js/Prism, checked into
  `doc-site/src/main/resources/assets/`) is acceptable if it stays offline.
- Match the YAVI palette and keep sufficient contrast in both themes.
- Do not break the generator's existing contract: links, anchors, search index, copy
  buttons, runnable-cell markup, version/language switching.
- The runtime work (`yavi-runtime.js`, its lazy loading and the wasm artifacts) is being
  done in parallel. If a client-side highlighter conflicts with that work, coordinate on
  `docs.js`; if the conflict is not worth it, shipping without highlighting is acceptable.

## Tests

Extend an existing generator test to assert that a Java block is highlighted (token markup
or class), and that the runnable cell is still produced. Keep the test count small; no
visual assertions.

## Done when

The generated 0.17 pages show highlighted code in both themes, runnable cells and the
existing tests still pass, and the site builds offline.
