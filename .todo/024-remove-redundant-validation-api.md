# Remove redundant and deprecated validation API

Difficulty: Medium

## Background

0.18 already carries breaking changes (Java 21 baseline, `Validation` as an interface,
sealed `Validated` with the records `Valid` / `Invalid`). While sealing `Validated`,
three members turned out to be redundant. They were left untouched because removing
them affects user code.

`Validation.yieldSuccess` / `yieldFailure` are not candidates: they are the hook that
lets the generic code in `am.ik.yavi.fn` (`map`, `apply`, `CombiningN.apply`) return a
`Validated` without depending on `am.ik.yavi.core`. Keep them.

## Candidates

- `ValueValidator.andThen(Function)`: `@Deprecated` alias of `map`. Remove it.
- The public `ConstraintViolation` constructor: `@Deprecated` in favor of `builder()`
  and unused inside the repository. Remove it, or make it private if the builder still
  needs it.
- `valueNullable()`: since 0.18.0 `Validation#value()` returns `null` for a validation that
  succeeded with `null` (the value type says whether it can: `T extends @Nullable Object`)
  and `valueNullable()` is a `@Deprecated` alias of it. Remove it in a later release; it
  may still be used by user code.

## Scope

- Remove the two deprecated members and migrate their call sites and tests.
- List the changes in the release notes as breaking.

## Notes

- Look for further members that became redundant in `am.ik.yavi.fn` and
  `am.ik.yavi.core` while doing this, and handle them here.
- Related to 023 (nullness annotations).
