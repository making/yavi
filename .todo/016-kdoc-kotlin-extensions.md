# KDoc: Kotlin DSL and extensions

Difficulty: Low

Depends on: 003. Conventions: `.todo/javadoc-conventions.md` (apply the content rules in KDoc syntax: `[Type]` links, `@param`, `@return`).

## Owned

- `K/builder/ValidatorBuilderKtDsl.kt`
- `K/fn/ValidationExtensions.kt`

Verification: `./mvnw -q compile` and `./mvnw -q test -Dtest='ValidatorBuilderKtDslTest,ValidationExtensionsTest'` (no doclint for Kotlin).

## Reference (read-only)

- `M/builder/ValidatorBuilder.java`, `M/constraint/*Constraint.java`, `M/fn/Validation.java`
- Tests: `TK/builder/ValidatorBuilderKtDslTest.kt`, `TK/fn/ValidationExtensionsTest.kt`
