# Javadoc: core validation engine

Difficulty: Medium

Depends on: 003. Conventions: `.todo/javadoc-conventions.md`.

## Owned

- `M/core/package-info.java` (package overview: validator types, conditions/groups, fail-fast, applicative style)
- `M/core/Validatable.java`, `Validator.java`, `ValueValidator.java`, `ApplicativeValidator.java`, `Validated.java`
- `M/core/NestedValidator.java`, `CollectionValidator.java`, `NestedCollectionValidator.java`
- `M/core/ConstraintCondition.java`, `NestedConstraintCondition.java`, `ConstraintGroup.java`, `ConstraintContext.java`
- `M/core/ConstraintPredicate.java`, `ConstraintPredicates.java`, `NestedConstraintPredicates.java`

Doclint regex: `am/ik/yavi/core/(package-info|Validatable|Validator|ValueValidator|ApplicativeValidator|Validated|Nested\w+|CollectionValidator|ConstraintCondition|ConstraintGroup|ConstraintContext|ConstraintPredicates?)\.java`

## Reference (read-only)

- `M/core/ConstraintViolation.java`, `ConstraintViolations.java`, `ViolationMessage.java`, `ErrorHandler.java`, `NullAs.java`, `Constraint.java`
- `M/fn/Validation.java` (superclass of `Validated`), `M/fn/Validations.java`
- `M/builder/ValidatorBuilder.java` (how validators, nested/collection validators and conditions are built)
- `M/arguments/Arguments1Validator.java` (main `ValueValidator` implementation family)
- `M/message/MessageFormatter.java`
- Tests: `T/core/ValidatorTest.java`, `ValueValidatorTest.java`, `ValidatedTest.java`, `ApplicativeValidationTest.java`, `FailFastTest.java`, `ConstraintGroupTest.java`, `ConstraintContextTest.java`, `NestedValidatorTest.java`, `CollectionValidatorTest.java`, `NestedConstraintConditionGh127Test.java`
- `README.md`, `docs/GettingStarted.md`
