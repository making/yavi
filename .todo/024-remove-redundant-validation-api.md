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
- `valueNullable()`: `Validated.Valid.value()` now returns `null` for a validation that
  succeeded with `null`, so on a `Validated` it equals `value()`.
  `Validation.Success.value()` still throws `NoSuchElementException` for `null`, which
  makes the two inconsistent.
  - Make `Validation.Success.value()` return `null` as well, then deprecate
    `valueNullable()` as an alias of `value()`. Do not remove it in the same release;
    it is used by the Kotlin extensions and by user code.
  - Express the nullable success in the type (`T extends @Nullable Object`) instead of
    suppressing NullAway, if that fits the outcome of 023.

## Scope

- Remove the two deprecated members and migrate their call sites and tests.
- Align `value()` on a `null` success across `Validation` and `Validated`, deprecate
  `valueNullable()` and replace its internal uses.
- List the changes in the release notes as breaking.

## Notes

- Look for further members that became redundant in `am.ik.yavi.fn` and
  `am.ik.yavi.core` while doing this, and handle them here.
- Related to 023 (nullness annotations).
