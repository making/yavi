# Documentation site generator

Builds the YAVI documentation site from the Markdown content in `content/`. The
generator is a standalone Maven module (its own reactor) so that flexmark and snakeyaml
never reach the library's dependency tree.

## Build and run

```bash
./mvnw -f doc-site/pom.xml package
java -jar doc-site/target/yavi-docgen.jar --source doc-site/content --out doc-site/target/site
```

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
the `data-runtime-src` path the page carries (`assets/yavi-runtime.js`):

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

The shipped `assets/yavi-runtime.js` is a stub that does not compile or run anything: it
loads successfully (the status indicator reads "runtime ready"), and every `Run` resolves
with "Code execution is not available yet. …" instead of output. Replacing that one file
with a real implementation (for example one based on GraalVM) is the only change needed;
the generator and the pages stay as they are.

## Tests

```bash
./mvnw -f doc-site/pom.xml test
```

The tests build a fixture tree under `src/test/resources/fixture` and assert the output
tree, version/language switching, anchor alignment, runnable-cell rendering, link
rewriting and validation, and the search index shape.

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
