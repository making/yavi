# Write the Japanese 0.17 docs: built-in constraints

Difficulty: Medium

## Scope

Depends on `.todo/084` and `.todo/086`.

Load the `japanese-tech-writing` skill before writing and follow it.

Translate `doc-site/content/0.17/en/built-in-constraints.md` to
`doc-site/content/0.17/ja/built-in-constraints.md`.

The heading layout MUST match the English page exactly: same number of headings, same
order, same level. The generator adopts the reference language's anchors by position and
fails the build when the count differs. Translate heading text only; never add, remove or
reorder a heading.

Translate prose, table headers and note labels. Keep code, constraint names, message keys
and default message formats in English exactly as written. Every message-key table row of
the English page must have a Japanese counterpart with the same keys and arguments.

## Done when

The Japanese page exists, the site builds with `./mvnw -f doc-site/pom.xml package`, the
heading-alignment check passes, and all message tables are preserved.
