# Unify the tone of the Japanese documentation

Difficulty: Low

## Problem

The Japanese pages under `doc-site/content/0.17/ja/` were translated in parallel and mix
the plain `である` style (`index`, `getting-started`, `using-yavi`, `annotation-processor`,
...) with the polite `です・ます` style (`built-in-constraints`). The existing Japanese
documents under `docs/*.ja.md` use the polite style.

## Scope

Load the `japanese-tech-writing` skill and rewrite the prose of every Japanese page in the
polite `です・ます` style used by `docs/NewFeatures_0.18.0.ja.md` and
`docs/BreakingChanges_0.18.0.ja.md`.

- Change prose only. Do not change headings, heading count/order/level, code blocks,
  tables, message keys, default messages, API names, links or anchors.
- Keep the English technical terms and inline code exactly as they are.
- Do not touch the English pages, nav.yaml, generator or assets.

## Done when

All ten Japanese pages read consistently in the polite style, the heading layout still
matches the English pages page by page, and `./mvnw -f doc-site/pom.xml test` passes.
