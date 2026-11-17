# Migrate the English 0.17 docs: built-in constraints

Difficulty: Medium

## Scope

Depends on `.todo/084` (site layout, nav format and Markdown conventions).

Convert `../yavi-doc/src/main/asciidoc/built-in-constraints.adoc` (about 2,700 lines) to
`doc-site/content/0.17/en/built-in-constraints.md`.

This is the largest page. Keep every constraint section and its order: string,
CharSequence, numeric, temporal, array/container, enum, object, boolean and the rest. Each
constraint section keeps its description, its example and its message-key table with the
default message and arguments. AsciiDoc `====` headings become the corresponding Markdown
heading level under the page title.

Do not split the page into several pages: keep the old site's structure. Do not rewrite the
technical content; this is a mechanical conversion of the 0.17 documentation.

## Done when

The page exists, the site builds with `./mvnw -f doc-site/pom.xml package`, the TOC matches
the old heading structure, and no message key or argument is missing.
