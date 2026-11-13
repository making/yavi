# Report changes of the published API in the build

Difficulty: Low

## Problem

Nothing in the build reports a change of the API surface. 0.18.0 breaks on purpose and
its breaks are in `docs/BreakingChanges_0.18.0.md` because someone listed them; most of
the surface is generated (`ArgumentsN*`, `scripts/generate-*.sh`), where a change of a
template changes dozens of signatures at once. Raoh runs `japicmp` against the last
release during `verify` and puts the report in the CI job summary.

## Trigger

Start when 0.18.0 is released, with it as the baseline: a comparison against 0.17 is
one long list of intended breaks.

## Scope

- `japicmp-maven-plugin` in `verify`, comparing with the last release, reporting only
  (no `breakBuild*`) until the API is meant to be stable; the report in the job summary
  of the CI.
- Exclude the packages that are not API, if any.
- A line in `CONTRIBUTING.md`: a reported break is listed in the breaking changes of the
  release.
