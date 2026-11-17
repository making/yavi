# Reuse built browser runtimes instead of rebuilding every version

Difficulty: Medium

## Problem

The Documentation Site workflow builds one Web Image per documented version on every run
that touches `doc-site/**`, even when only CSS, Markdown or the generator changed. Each
image depends only on the runtime sources, the GraalVM release and that version's YAVI
release, all of which rarely change. A one-line stylesheet edit costs a full wasm build per
version, and the cost grows linearly as versions accumulate (0.18, 0.19, ...).

## Proposal

Cache each version's runtime and skip the build on a hit. The cache key must cover
everything the image is derived from:

- a hash of `doc-site/runtime/**` (sources, native-image config, launcher assets);
- the GraalVM release used for `-Dgraalvm.version`;
- the documentation version and its `yavi_release` from `doc-site/content/versions.yaml`.

Design points:

- Split the runtime build out of the site job so that a fully cached run does not pay for
  GraalVM and Binaryen setup either: a matrix job over the versions that restores the
  cache first and runs the toolchain setup, the build and the artifact upload only on a
  miss, and a site job that consumes the uploaded runtimes through artifacts.
- Keep the generator, the example harness and the generated-site checks exactly as strict
  as today: a cache miss rebuilds, a corrupt or missing entry must not be able to ship a
  stub.
- Cache limits (repository-wide size, eviction, branch scoping) make a miss a slow run,
  never a wrong run. State this in the workflow comment and in `doc-site/README.md`.

## Out of scope

Sharing one image across versions (fetching the version's YAVI classes at run time and
selecting the classpath per run). Revisit only if the cached images grow past the cache
budget or if the per-version images become a deployment problem.

## Acceptance

- A change that leaves the runtime sources and versions.yaml untouched restores every
  runtime from cache and completes without invoking `native-image`; verify by re-running
  the workflow on the same commit and confirming the cache hit in the logs.
- Adding a version to versions.yaml builds only that version's image.
- Changing `doc-site/runtime/**` or a `yavi_release` builds the affected versions and
  still publishes a complete site with a full runtime for every version.
