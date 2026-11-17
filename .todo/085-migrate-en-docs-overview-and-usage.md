# Migrate the English 0.17 docs: overview, getting started, usage

Difficulty: Medium

## Scope

Depends on `.todo/084` (site layout, nav format and Markdown conventions).

Convert these pages of `../yavi-doc/src/main/asciidoc/` from AsciiDoc to Markdown under
`doc-site/content/0.17/en/`:

- `index.adoc` -> `index.md` (overview; the old index only includes the other files, so
  write a real short overview page instead of an include list)
- `introduction.adoc` -> `introduction.md`
- `getting-started.adoc` -> `getting-started.md`
- `using-yavi.adoc` -> `using-yavi.md`

Keep the section order, ids, tables, notes, examples and messages of the original. AsciiDoc
`NOTE:`/`WARNING:` become GFM blockquotes with a bold label. `[source,java]` blocks become
` ```java ` blocks. Tables become GFM tables; where the old table uses a complex AsciiDoc
cell, keep the same information in a readable GFM table. Preserve all cross-page references
as Markdown links (`using-yavi.md#anchor`).

Do not rewrite the technical content. The target is the 0.17.3 API exactly as documented
in yavi-doc. Java code blocks stay static; mark a block `java runnable` only when it is a
complete compilation unit that will run without external setup (see `.todo/091`).

## Done when

The four pages exist, the site builds with `./mvnw -f doc-site/pom.xml package`, every
internal link resolves, and the text reads like the original.
