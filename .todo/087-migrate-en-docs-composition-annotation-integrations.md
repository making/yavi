# Migrate the English 0.17 docs: composition, arguments, annotation processor, integrations

Difficulty: Medium

## Scope

Depends on `.todo/084` (site layout, nav format and Markdown conventions).

Convert these pages of `../yavi-doc/src/main/asciidoc/` to Markdown under
`doc-site/content/0.17/en/`:

- `combining-validation-results.adoc` -> `combining-validation-results.md`
- `validating-arguments.adoc` -> `validating-arguments.md`
- `combining-validators.adoc` -> `combining-validators.md`
- `annotation-processor.adoc` -> `annotation-processor.md`
- `integrations.adoc` -> `integrations.md`

Keep the section order, ids, tables, notes, examples and messages of the original. AsciiDoc
`NOTE:`/`WARNING:` become GFM blockquotes with a bold label. `[source,java]` blocks become
` ```java ` blocks. Preserve cross-page references as Markdown links
(`combining-validators.md#anchor`). Do not rewrite the technical content; the target is the
0.17.3 API exactly as documented in yavi-doc.

## Done when

The five pages exist, the site builds with `./mvnw -f doc-site/pom.xml package`, and every
internal link resolves.
