# Write the Japanese 0.17 docs: composition, arguments, annotation processor, integrations

Difficulty: Medium

## Scope

Depends on `.todo/084` and `.todo/087`.

Load the `japanese-tech-writing` skill before writing and follow it.

Translate these English pages under `doc-site/content/0.17/en/` to Japanese under
`doc-site/content/0.17/ja/` with the same file names:

- `combining-validation-results.md`
- `validating-arguments.md`
- `combining-validators.md`
- `annotation-processor.md`
- `integrations.md`

The heading layout MUST match the English page exactly: same number of headings, same
order, same level. The generator adopts the reference language's anchors by position and
fails the build when the count differs. Translate heading text but never add, remove or
reorder a heading.

Code blocks are not translated. Translate prose, table headers and note labels only; keep
API names, identifiers, message keys and default messages in English. Use natural
technical Japanese as in the existing `.ja.md` documents under `docs/`.

## Done when

The five Japanese pages exist, the site builds with
`./mvnw -f doc-site/pom.xml package`, the heading-alignment check passes, and the language
switcher opens the corresponding Japanese page.
