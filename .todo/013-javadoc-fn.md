# Javadoc: fn package (Validation, functions, applicative combinators)

Difficulty: Medium

Depends on: 003. Conventions: `.todo/javadoc-conventions.md` (see "Generated sources").

## Owned

- Hand-written: `M/fn/Validation.java`, `Pair.java`, `package-info.java` (overview: `Validation` as an applicative result type, `Validations.combine`/`apply`, `FunctionN`, currying)
- `scripts/generate-function.sh` and its outputs `M/fn/Function1..16.java`
- `scripts/generate-applicative.sh` and its outputs `M/fn/Combining1..16.java`, `Validations.java`, `Functions.java`

Doclint regex: `am/ik/yavi/fn/[\w-]+\.java`

## Reference (read-only)

- `M/core/Validated.java`, `ConstraintViolation.java`, `ConstraintViolations.java`, `ApplicativeValidator.java`, `ValueValidator.java`
- `M/arguments/Arguments2Combining.java` (consumer of `CombiningN`-style composition)
- `K/fn/ValidationExtensions.kt`
- Tests: `T/fn/*`, `T/core/ApplicativeValidationTest.java`, `TK/fn/ValidationExtensionsTest.kt`
