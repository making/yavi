# Make Java examples runnable cells with a documented runtime contract

Difficulty: Medium

## Scope

Depends on `.todo/084`. This item delivers the documentation-side half of browser code
execution; the actual GraalVM runtime is `.todo/093`.

- Define the source convention for a runnable example: a fenced ` ```java runnable `
  block is a complete compilation unit. Every other Java block stays static.
- Render runnable blocks as an editable cell with Run, copy and output, matching the
  version and language tree it belongs to (relative paths must work in `/<version>/<lang>/`
  subdirectories).
- Specify and test `window.YaviRuntime.run(source)` -> `Promise<string>`: stdout and
  stderr of the program, with a compilation error returned as readable text. Input is the
  full source; the runtime owns compiling and running it.
- Keep the runtime lazily loaded on the first Run from `data-runtime-src`. A page with no
  runnable cell must not load the runtime.
- Add a "Reset runtime" action that reloads the runtime state (a full page reload is
  acceptable) and a status indicator as in rontolisp.
- Mark the examples that are genuinely self-contained in the 0.17 content as
  `java runnable` once the pages exist; leave fragments static.
- Document the contract in `doc-site/README.md` so `.todo/093` can replace only
  `assets/yavi-runtime.js` (and add its wasm artifacts) without touching pages.

## Done when

Cells behave correctly in the generated site, the stub runtime reports that execution is
not available yet, and no runtime is fetched on pages without a runnable cell.
