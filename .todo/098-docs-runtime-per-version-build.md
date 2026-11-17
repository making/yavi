# Build and verify one browser runtime per documented version

Difficulty: Medium

## Problem

The documentation site serves one Web Image per documentation version
(`assets/runtime/<version>/`), and `DocGen.copyRuntime` falls back to the stub when a
version has no build. The deployment workflow builds the runtime only once, from the
`yavi.version`/`docs.version` properties in `doc-site/runtime/pom.xml`, so adding 0.18
would publish its pages with silently disabled Run buttons: the site builds, deploys, and
only a reader discovers that execution is unavailable.

The runtime cannot be shared across versions: the YAVI classes are unpacked into the image
and `JavaRunner` uses a fixed `libs/yavi-classes` classpath, so each documented version
needs an image built against that version's release.

## Proposal

- Build the runtime per documented version in `.github/workflows/doc-site.yaml`, driven by
  the version list the content already declares (`doc-site/content/versions.yaml`), with
  each version mapped to the YAVI release it documents. A matrix job or a loop over the
  versions is fine; the YAVI version must not be duplicated in a third place.
- Extend the generated-site check so every version listed in `versions.yaml` must have
  `assets/runtime/<version>/yavi-runtime.js`, `yavi-runtime-wasm.js` and the wasm beside
  it, and fail the build when one is missing. Today only 0.17 is asserted by name.
- Decide whether the generator should keep the stub fallback for local runs. If it does,
  make it explicit (a warning naming the version) so a local build without
  `--runtime` and a CI build that forgot a version are distinguishable.

## To decide

- Whether the per-version images should instead carry several YAVI versions under
  `libs/yavi-classes/<version>/` and `YaviRuntime.run(source, version)` select one, which
  would ship a single image for all trees. Only worth it if the extra image size or the
  build time per version becomes a problem; it changes the run contract.
- Whether the workflow should rebuild every version's runtime on every doc-site change or
  cache the images keyed by version and YAVI release.

## Acceptance

Adding a version to `versions.yaml` and `content/<version>/` either builds its runtime and
publishes it, or fails the build with a message naming the version. No version ships with
the stub on the deployed site.
