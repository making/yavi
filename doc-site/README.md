# Documentation site generator

Builds the YAVI documentation site from the Markdown content in `content/`. The
generator is a standalone Maven module (its own reactor) so that flexmark and snakeyaml
never reach the library's dependency tree.

## Build and run

```bash
./mvnw -f doc-site/pom.xml package
java -jar doc-site/target/yavi-docgen.jar --source doc-site/content --out doc-site/target/site
```

To publish the real browser runtime (see "Runnable Java code" below), build it first and
pass its output directory:

```bash
./mvnw -f doc-site/runtime/pom.xml -Pweb package        # needs the GraalVM setup below
java -jar doc-site/target/yavi-docgen.jar --source doc-site/content --out doc-site/target/site \
  --runtime doc-site/runtime/target/web
```

Without `--runtime` every version tree gets the stub, whose cells report that execution
is not available. The runtime module needs Oracle GraalVM 25.5 or later (`native-image`
with the `svm-wasm` tool), Binaryen 119 or later (for `wasm-as`) and a JDK 25 to compile
with; the generator itself keeps building with JDK 21.

Then open `doc-site/target/site/index.html`. From inside `doc-site/` the defaults are
`--source content --out target/site`. The search index and the runtime are fetched over
HTTP, so serve the output when checking those:

```bash
python3 -m http.server -d doc-site/target/site 8000
```

Output tree:

```
<out>/index.html                       redirect to the default version/language
<out>/<page>.html                      redirect to the default tree's top-level page
<out>/versions.json
<out>/<version>/<lang>/**/*.html
<out>/<version>/<lang>/search-index.json, search-body.json
<out>/assets/**
```

The `<out>/<page>.html` stubs keep the unversioned URLs of the old AsciiDoc site
(`/getting-started.html`, `/built-in-constraints.html`, ...) working: each forwards to
the same page under the default version and language.

## Content layout

```
content/
  versions.yaml
  <version>/<lang>/nav.yaml + *.md
```

Every directory under `content/` must be a version listed in `versions.yaml`, and every
listed version must have at least one language directory containing a `nav.yaml`.
Languages are the codes declared in `versions.yaml`. Any mismatch is a build error.

### `versions.yaml`

```yaml
site:
  title: YAVI
  description: Yet Another Validation (lambda based type safe validation) for Java
default_version: "0.17"
default_language: en
languages:
  - code: en
    name: English
  - code: ja
    name: 日本語
versions:
  - version: "0.17"
    label: "0.17"
```

`label` is what the version switcher shows. The default version and language must be
listed. Adding a version means adding `content/<version>/<lang>/nav.yaml` and every page
it names.

### `nav.yaml`

```yaml
title: YAVI
lang_name: English
sections:
  - title: Getting Started
    pages:
      - file: index.md
        title: Overview
      - file: guide.md
        title: Guide
        subpages:
          - file: guide/deep.md
            title: Deep
```

`file` is relative to the language directory and `title` is required. The flat order of
the pages drives the sidebar and the previous/next chain. A page with `subpages` renders
them with a back link to their immediate parent and a chain of their own; sub-pages are
not sidebar rows. Every page named in `nav.yaml` must have its Markdown file.

## Markdown conventions

- GFM tables, autolinks and strikethrough are enabled. Headings get generated ids, which
  the "On this page" TOC and the search index use as anchors.
- Internal links must be written as `page.md` or `page.md#anchor`; the build rewrites
  them to `.html` and fails when a relative link names a page the tree does not publish.
  Absolute URLs ending in `.md` are left alone.
- A translated page must have the same heading layout, in the same order, as the default
  language's page of the same path. Translations keep their own heading text but adopt
  the reference language's anchors by position, so a link written once works in every
  language tree. A heading-count mismatch fails the build.
- Version and language switches point at the same page when the target tree has it, and
  otherwise fall back to that tree's index page.
- Static fenced blocks of Java, Kotlin, XML, JSON, properties and shell are highlighted
  by the generator at build time. There is no client-side highlighter and no network
  request when a page is viewed; the token classes are styled in `assets/docs.css` for
  both themes.

## Runnable Java code

A fenced block whose info string is `java runnable` becomes an editable cell with a Run
button and an output area:

````markdown
```java runnable
public class Main {

	public static void main(String[] args) {
		System.out.println("Hello");
	}

}
```
````

Every other fenced block stays static and gets a copy button.

### Runtime contract

The cell runs through a runtime that `docs.js` loads lazily on the first Run click from
the `data-runtime-src` path the page carries
(`assets/runtime/<version>/yavi-runtime.js`):

```js
window.YaviRuntime.run(source) -> Promise<string>
```

- `source` is one complete Java compilation unit (class, imports, everything).
- The returned promise resolves with the program's standard output and error text.
  Rejecting the promise is shown as an error in the cell's output area.
- The runtime may preload the YAVI jar, so examples can import `am.ik.yavi.*`.
- A page may contain several cells; each call is independent unless the runtime chooses
  to share state.

`docs.js` requests the runtime script only on the first Run; a page with no runnable cell
never requests it. "Reset runtime" reloads the page, which discards any runtime state.

### The shipped runtime

`doc-site/runtime/` builds the real runtime for a version tree. It is a GraalVM Web Image
(a WebAssembly module plus its JavaScript launcher) containing:

- `javac` from the JDK, compiled into the image together with the `java.base` data it
  needs, so the cell's source is compiled inside the browser;
- Espresso, the JVM written in Java, in its pure-Java mode, which executes the class
  files javac produced;
- the YAVI jar of that tree's version (`yavi.version` in the runtime pom, currently
  `0.17.3` for the 0.17 tree), unpacked so no zip support is needed. Examples can import
  `am.ik.yavi.*` without any setup.

`WebMain` publishes `globalThis.YaviJs`; `assets/yavi-runtime.js` wraps it in the
`window.YaviRuntime` contract. The launcher is loaded into a Web Worker: the interpreter
needs host stack space and a worker's stack is far deeper than the page's main thread.

Each run compiles into a fresh in-memory directory and executes in a fresh Espresso
context, so runs do not share state. The compiled classes are rewritten to charge a step
to an execution budget on every method entry and every jump; a program that never
finishes ends with "Execution stopped after 2000000 steps" instead of hanging the page.
Compiler diagnostics, the program's `System.out`/`System.err` output and run-time
exceptions are returned as text.

Limitations:

- Examples run at Java 25 language level but the runtime is an interpreter: it is fast
  enough for documentation-sized programs, not for loops with millions of iterations.
  Deep recursion (several hundred frames) can exhaust the WebAssembly stack; the
  worker raises that limit well above what examples need.
- No threads, sockets, files or reflection beyond what javac compiled in the image
  needs. `main` receives a `null` `String[]` (`args` is not used by the examples).
- The image is about 200 MB of WebAssembly. It is fetched only on the first Run and is
  highly compressible; serve the site with compression enabled.

The generator installs a runtime from `--runtime <dir>` by looking up a sub-directory
named after each documentation version (here `<dir>/0.17/`), so a future version tree
gets its own build against its own YAVI version. The version's page loads
`assets/runtime/<version>/yavi-runtime.js`; a version without a build keeps the stub,
which loads successfully (the status indicator reads "runtime ready") but whose `Run`
resolves with "Code execution is not available yet. …".

```bash
# build one version's runtime
./mvnw -f doc-site/runtime/pom.xml -Pweb package            # output: target/web/0.17/
# drive it without a browser
node --experimental-wasm-exnref doc-site/runtime/test/run.js
```

## Tests

```bash
./mvnw -f doc-site/pom.xml test
```

The tests build a fixture tree under `src/test/resources/fixture` and assert the output
tree, version/language switching, anchor alignment, runnable-cell rendering, link
rewriting and validation, and the search index shape.

The runtime has its own harness, which reads the shipped `java runnable` example from
`content/0.17/en/getting-started.md` and asserts that it compiles and prints its
violations, that a compiler error and a run-time exception are reported, and that an
endless loop is stopped:

```bash
node --experimental-wasm-exnref doc-site/runtime/test/run.js [runtime-dir]
```

## Deployment

`.github/workflows/doc-site.yaml` builds, tests and renders the site on every change to
`doc-site/**`, then publishes the output to the `gh-pages` branch that serves
`yavi.ik.am`. The publish step keeps `CNAME` and the root-level `favicon.ico` and
replaces everything else. Old unversioned URLs are covered by the root redirect stubs
the generator emits, and `/` redirects to the default version and language.

The site used to be generated by `../yavi-doc` and published from its own workflow. That
workflow must be disabled (or its schedule/push trigger removed) once this workflow has
run successfully on `develop`/`main` and the published site has been verified; otherwise
two owners overwrite the same `gh-pages` branch. This repository is the only owner of the
published docs after that switch.
