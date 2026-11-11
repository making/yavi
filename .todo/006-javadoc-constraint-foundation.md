# Javadoc: constraint foundation, object/container constraints

Difficulty: Medium

Depends on: 003. Conventions: `.todo/javadoc-conventions.md`.

## Owned

- `M/core/Constraint.java`, `CustomConstraint.java`, `NullAs.java`
- `M/constraint/base/*.java` incl. `package-info.java`
- `M/constraint/package-info.java` (overview of all constraint types and subpackages)
- `M/constraint/ObjectConstraint.java`, `BooleanConstraint.java`, `EnumConstraint.java`, `CollectionConstraint.java`, `MapConstraint.java`
- `M/constraint/array/*.java` incl. `package-info.java`

Doclint regex: `am/ik/yavi/(core/(Constraint|CustomConstraint|NullAs)|constraint/(base/[\w-]+|array/[\w-]+|package-info|ObjectConstraint|BooleanConstraint|EnumConstraint|CollectionConstraint|MapConstraint))\.java`

## Reference (read-only)

- `M/core/ViolationMessage.java` (message ids and arguments), `ConstraintPredicate.java`, `ViolatedArguments.java`
- `M/constraint/*Constraint.java` not owned here (to describe the hierarchy in `package-info`)
- `M/builder/ValidatorBuilder.java` (`constraint(...)`, `constraintOnTarget`, `_object`/`_collection`/`_map`/array entry points)
- Tests: `T/constraint/*`, `T/constraint/array/*`, `T/core/CustomValidatorTest.java`, `T/core/ArrayValidatorTest.java`, `MapValidatorTest.java`, `EnumValidatorTest.java`
