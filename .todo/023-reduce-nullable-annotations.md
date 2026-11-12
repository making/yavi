# Reduce unnecessary @Nullable annotations

Difficulty: Medium

## Background

The JSpecify/NullAway migration (#420, PR #534) confined `@Nullable` to the
boundaries where null is part of the contract, but the main sources now contain
about 1120 `@Nullable` usages (684 before the migration). Most of the increase is
mechanical and forced by the existing nullable contracts:

- `arguments` (+306): fields of `ArgumentsN` that store possibly-null arguments
  and overrides that mirror the pre-existing `ValueValidator.validate(@Nullable T)`
  signature.
- `builder` (+83): `To*` property extractors, `nest`/object extractors, mapper
  inputs and results, and builder fields that are assigned after construction.
- `core` (+33): nullable values in `Validated`/`Validation`/`ViolatedValue`,
  staged-builder fields and extractor functions.

The user wants to keep `@Nullable` as small as possible and avoid nullability that
is only a consequence of the current API shape.

## Options

1. Make the argument type parameters nullable-capable instead of annotating each
   storage location. For example declare `ArgumentsN<A1 extends @Nullable Object, ...>`
   and the same bounds in `ArgumentsNValidator`, `ArgumentsNCombining`,
   `ArgumentsNSplitting` and the builders. This removes the `@Nullable` on the
   argument fields and accessors (about 120 places), makes
   `ArgumentsNValidator<@Nullable String, ...>` expressible, and also lets Kotlin
   declare nullable argument validators. The trade-off is looser public generic
   bounds, so callers that rely on non-null type arguments need review.

2. Default-initialize builder fields that are only unset until a staged setter runs
   so they no longer need `@Nullable`:
   - `ConstraintViolation.Builder` fields (the staged interfaces already make
     `build()` unreachable before the required setters).
   - `ValidatorBuilder.messageFormatter` (the default `SimpleMessageFormatter` can be
     stored immediately; the setter then maps `null` to the default).
   - `ArgumentsValidatorBuilder` `builder` fields (only if the documented
     `NullPointerException` for calling `build()` without `builder(...)` may change).

3. Audit the remaining annotations and remove ones that are not contractually
   required, including test fixtures. Keep `@NullMarked` and NullAway checking as the
   default model; do not relax `OnlyNullMarked` to reduce annotations.

## Acceptance

- The number of `@Nullable` usages in main and test sources is measurably lower.
- No nullability annotation is removed from a location that can actually receive
  null; a deliberately nullable dereference still fails the build.
- `./mvnw clean test` and `./mvnw javadoc:jar` pass, and the Kotlin extensions and
  Kotlin tests still compile with `-Xjspecify-annotations=strict`.
- Public API changes (for example the argument type-parameter bounds) are documented
  in the Javadoc and keep the runtime dependency set unchanged.

## Implementation notes

- The generator scripts `scripts/generate-args.sh`, `scripts/generate-applicative.sh`
  and `scripts/generate-function.sh` are the source of truth for the generated
  Types; update them and regenerate instead of editing generated files.
- The nullability-maven-plugin remains the source of the Error Prone/NullAway
  configuration; do not duplicate compiler flags by hand.
