# Build the documentation site generator

Difficulty: High

## Goal

Replace the separate `../yavi-doc` AsciiDoc site with a Markdown documentation site built
from this repository. The generator is our own implementation; no external static site
generator. `docs/` is taken by the Markdown notes, so the site lives in `doc-site/`.

Use `../rontolisp/docs-tool/` and `../rontolisp/doc/` as the reference implementation. Read
`DocGen`, `Nav`, `Catalog`, `HtmlTemplate`, `TocBuilder`, `SearchIndex` and `RunnableBlockTransformer`
before designing.

## Layout

Standalone Maven module at `doc-site/` (its own reactor, Java 21, flexmark + snakeyaml,
shade plugin, main class `am.ik.yavi.docgen.DocGen`):

```
doc-site/
  pom.xml
  src/main/java/am/ik/yavi/docgen/*.java
  src/main/resources/assets/{docs.css,docs.js,yavi-runtime.js}
  content/versions.yaml
  content/0.17/en/nav.yaml + *.md
  content/0.17/ja/nav.yaml + *.md
```

`content/versions.yaml` lists published versions; `0.17` is the first and default.

Generated output:

```
<out>/index.html                      redirect to the default version/language
<out>/versions.json
<out>/<version>/<lang>/**/*.html
<out>/<version>/<lang>/search-index.json, search-body.json
<out>/assets/**
```

## Features

- Markdown to HTML with GFM tables, heading ids and code fences.
- `nav.yaml` with nested subpages; sidebar, previous/next chain, back links, "On this page"
  TOC, and a client-side search over the pages of one version/language tree.
- Version switcher rendered on every page. A switch prefers the same page path in the
  other version and falls back to that version's language index when the page is absent.
  Validate `versions.yaml` against the content directories; a version without `nav.yaml`
  is a build error.
- Language switcher (`en`, `ja`) in the same version. Translated pages adopt the reference
  language's heading anchors by position (as rontolisp does) and the build fails when the
  heading layout has drifted.
- Runnable Java code is a first-class feature from the beginning:
  - A fenced block whose info string is `java runnable` becomes an editable code cell
    (`data-lang="java"`, Run button, output area), like rontolisp's cells.
  - Every other code block stays static and gets a copy button.
  - `docs.js` lazily loads the runtime from `data-runtime-src` (a path to
    `assets/yavi-runtime.js`) on the first Run and calls
    `window.YaviRuntime.run(source)` which returns a Promise of the program output text.
  - Ship a stub `yavi-runtime.js` that defines that API and resolves with a clear
    "code execution is not available yet" message. The real GraalVM runtime replaces only
    this file later (see `.todo/093`).
  - Document the contract in `doc-site/README.md`: source is a complete compilation unit,
    the runtime receives the source and returns stdout/stderr text, and the runtime may
    preload the YAVI jar.

## Content placeholders

Create both `nav.yaml` files and one placeholder page per entry (title plus a one-line
note) for version `0.17`, so the site builds before the content migration. Pages:
`index`, `introduction`, `getting-started`, `using-yavi`, `combining-validation-results`,
`validating-arguments`, `combining-validators`, `built-in-constraints`,
`annotation-processor`, `integrations`. Japanese nav titles are Japanese; page bodies are
filled by later items.

## Tests

Unit tests with a small fixture tree under `src/test/resources` cover: nav parsing, version
and language switching, heading-anchor alignment failure, runnable vs static block
rendering, link rewriting, search index shape and output tree. Keep the test count small
and extend existing tests rather than adding one per behavior.

## Verification

```
./mvnw -f doc-site/pom.xml package
java -jar doc-site/target/yavi-docgen.jar --source doc-site/content --out doc-site/target/site
```

The generated site must open locally, switch version and language, search, and show Run
buttons that report the stub message.
