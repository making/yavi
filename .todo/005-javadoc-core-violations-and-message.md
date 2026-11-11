# Javadoc: violations and message formatting

Difficulty: Medium

Depends on: 003. Conventions: `.todo/javadoc-conventions.md`.

## Owned

- `M/core/ConstraintViolation.java`, `ConstraintViolations.java`, `ConstraintViolationsException.java`, `ErrorHandler.java`
- `M/core/ViolatedArguments.java`, `ViolatedValue.java`, `ViolationDetail.java`
- `M/core/ViolationMessage.java`: document every `Default` constant with its message id, default message, and what each `{n}` argument holds (the constraint todos rely on this as the single source of message semantics).
- `M/message/*.java` incl. `package-info.java`

Doclint regex: `am/ik/yavi/(core/(ConstraintViolations?|ConstraintViolationsException|ErrorHandler|Violated\w+|ViolationDetail|ViolationMessage)|message/[\w-]+)\.java`

## Reference (read-only)

- `M/core/Validator.java`, `ConstraintPredicate.java`, `ConstraintPredicates.java` (where violations and arguments are produced)
- `M/constraint/base/ConstraintBase.java`, `M/constraint/CharSequenceConstraint.java` (how message ids and arguments are attached)
- `M/fn/Validation.java`, `M/core/Validated.java` (violations as the error type)
- Tests: `T/core/ConstraintViolationTest.java`, `ConstraintViolationsTest.java`, `ConstraintViolationsExceptionTest.java`, `T/message/*`
