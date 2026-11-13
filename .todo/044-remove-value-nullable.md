# Remove Validation#valueNullable

Difficulty: Low

## Background

Since 0.18.0 `Validation#value()` returns `null` for a validation that succeeded with
`null`, and `valueNullable()` is an alias of it, deprecated for removal. It was kept in
0.18 because 0.17 user code calls it and had no deprecation warning to act on yet.

## Scope

- Remove `Validation#valueNullable()` in the release after 0.18.
- List the removal in the breaking changes of that release.
