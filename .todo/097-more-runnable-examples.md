# Add more runnable examples to the documentation

Difficulty: Medium

## Background

The browser runtime is live on yavi.ik.am and the single example in `getting-started`
runs in the browser (javac + Espresso in a Web Image, YAVI 0.17.3 embedded). The
documentation is mostly static fragments, so most pages still do not demonstrate
execution.

## Scope

Add a set of self-contained examples across the 0.17 documentation, covering distinct
features: object validation and violation messages, combining validators,
arguments/Always Valid, nested and collection constraints, string/numeric/temporal
constraints, a custom constraint, message formatting, and similar. Aim for roughly ten
examples in total, not one per constraint.

Every ` ```java runnable ` block must:

- be a complete compilation unit with `public static void main`, using only the 0.17.3
  API (the runtime embeds those classes);
- produce deterministic output that makes the outcome obvious (counts, field names,
  messages); no timestamps, randomness or locale-dependent output;
- finish well within the runtime's 2,000,000-step budget, without deep recursion,
  threads, sockets or general reflection;
- be identical in the English and Japanese pages (code and comments in English).

Place each example on the page that explains its feature, after the relevant prose. Keep
the existing static fragments; convert a fragment to runnable only when it is meaningful
as a complete program. Preserve heading parity between en and ja (the generator enforces
it) and the polite Japanese style used by the Japanese pages. Update the surrounding
prose when it says an example is illustrative so it invites running it.

## Verification

- Extend the runtime test harness to extract every `java runnable` block from
  `doc-site/content/0.17/{en,ja}` and compile and run it through the built runtime,
  asserting the expected output. Wire it into the documentation CI so a broken example
  fails the build.
- Run the harness and the generator tests; generate the site and run several examples in
  the browser (Chrome DevTools MCP if available), including at least one from each
  language.
- Keep the tests focused: one harness that covers all blocks, not a new test per example.

## Done when

Every marked example runs in the browser and in CI with deterministic output, en/ja
parity holds, and no static example was lost.
