# Write the Japanese 0.17 docs: overview, getting started, usage

Difficulty: Medium

## Scope

Depends on `.todo/084` and `.todo/085`.

Load the `japanese-tech-writing` skill before writing and follow it.

Translate the four English pages under `doc-site/content/0.17/en/` (`index.md`,
`introduction.md`, `getting-started.md`, `using-yavi.md`) to Japanese under
`doc-site/content/0.17/ja/` with the same file names.

The heading layout MUST match the English page exactly: same number of headings, same
order, same level. The generator adopts the reference language's anchors by position and
fails the build when the count differs. Translate the heading text but never add, remove
or reorder a heading.

Code blocks are not translated (identifiers, comments and messages stay in English). Only
prose, table headers and note labels are Japanese. Use natural technical Japanese as in
the existing `.ja.md` documents under `docs/`; do not translate API names or message
keys. All internal links point to the same `*.md` path as in the English page.

## Done when

The four Japanese pages exist, the site builds with
`./mvnw -f doc-site/pom.xml package`, the heading-alignment check passes, and the
Japanese language switcher opens the corresponding page.
