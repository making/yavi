# Javadoc: ValidatorBuilder

Difficulty: Medium

Depends on: 003. Conventions: `.todo/javadoc-conventions.md`.

`ValidatorBuilder` (1500+ lines) is the main user entry point. Many overloads share semantics: document them consistently and point overload families to one canonical description with `{@link}` / `@see` instead of repeating it.

## Owned

- `M/builder/ValidatorBuilder.java`, `ValueValidatorBuilder.java`, `ConflictStrategy.java`
- `M/builder/package-info.java` (overview: `ValidatorBuilder`, typed single-value builders, arguments builders, `YaviArguments`)

Doclint regex: `am/ik/yavi/builder/(ValidatorBuilder|ValueValidatorBuilder|ConflictStrategy|package-info)\.java`

## Reference (read-only)

- `M/core/Validator.java`, `Validatable.java`, `ValueValidator.java`, `ConstraintCondition.java`, `ConstraintGroup.java`, `NullAs.java`, `CustomConstraint.java`
- `M/constraint/*Constraint.java`, `M/constraint/array/*`, `M/constraint/base/ConstraintBase.java` (types passed to the `_xxx` / `constraint` callbacks)
- `M/meta/ConstraintMeta.java` and `M/meta/*ConstraintMeta.java` (meta-based `constraint` overloads)
- `M/message/MessageFormatter.java`, `M/factory/ValidatorFactory.java`, `M/validator/Yavi.java`
- `K/builder/ValidatorBuilderKtDsl.kt` (Kotlin DSL wrapper)
- Tests: `T/core/ValidatorTest.java`, `T/core/*NestedValidatorTest.java`, `T/core/*CollectionValidatorTest.java`, `T/core/*MapValidatorTest.java`, `T/core/*ArrayValidatorTest.java`, `T/core/FailFastTest.java`, `T/core/ConstraintGroupTest.java`
- `README.md`, `docs/GettingStarted.md`, `docs/FromBeanValidationToYAVI.md`
