# Run Java examples in the browser with GraalVM Web Image

Difficulty: High

## Background

The documentation cells (`.todo/091`) call `window.YaviRuntime.run(source)`.
Implement that runtime so a reader can edit and run a Java example against the documented
YAVI version without leaving the page.

Reference: `../graalvm-demos/web-image/javac/` (cloned at
`/Users/toshiaki/git/graalvm-demos`). That demo compiles Java to `.class` in the browser
with javac compiled to Wasm (`--tool:svm-wasm`, profile `javacweb`, main
`com.example.WebMain`). It only compiles and disassembles; to RUN the class we need a JVM
in the browser as well. Evaluate Espresso (Java on Truffle) in the same Web Image, or a
combined image that compiles with javac and executes the resulting bytecode.

## Scope

- A module that builds the wasm runtime with GraalVM Web Image and Binaryen. The image
  must include the YAVI jar/classes for the target version (the 0.17 line initially; 0.17.3 is its current patch), so examples
  can `import am.ik.yavi...` without shipping the dependency separately.
- A JavaScript binding that implements `window.YaviRuntime.run(source)`:
  - compile the source, report compiler diagnostics as text;
  - run the produced class with stdout and stderr captured;
  - bound execution (timeout, no threads/sockets) and return a clear error instead of
    hanging the page.
- Publish the wasm and binding as `assets/yavi-runtime.js` plus its artifacts for each
  version tree; the version-specific runtime must match the version's API.
- The generator only swaps the stub `yavi-runtime.js` for the real binding. No page
  content changes.
- CI builds the runtime; a local build is documented for contributors.
- Tests: at least one self-contained example per version compiles and runs in a headless
  browser or Node harness. Do not benchmark; assert output and error text.

## Done when

The published 0.17 page runs its marked examples in a browser, a compilation error shows
the javac diagnostic in the cell, and an infinite loop is stopped with a readable message.
