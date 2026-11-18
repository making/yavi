# Introduction

YAVI gives Java a Zod-like way to describe and validate input. A schema written in code reads untyped input, such as a parsed JSON document or a submitted form, and produces a typed object or a complete list of violations. This page explains that model, the older object-validation model it complements, and what the library guarantees.

## Two validation approaches

There are two ways to validate data:

1. Validate the input and create the object. The input is checked before the constructor runs, and the constructor is called only with valid values. An object that violates a rule cannot be created. This is the model of Zod and Raoh, and the main model of YAVI 0.18.
2. Validate an object that already exists. The object is created first and its fields are checked afterwards. This is the model of [Bean Validation](https://beanvalidation.org/): `new Car(null, "D", 1)` succeeds, and a separate call reports the violations.

`Yavi.schema()` with `fromMap(f)` implements the first model: it reads an untyped input by property name, checks each value, and passes the validated values to a function. `ValidatorBuilder` implements the second for an object that is created by something else, such as a framework or a deserializer.

## Why schema-first

The schema-first model has properties that object validation cannot offer:

* An invalid object never exists. There is no window in which a `Car` holds a negative `seatCount`.
* The schema describes the input, not the class. The same description reads a map, a JSON tree and request parameters, and can write the object back.
* A missing property is a violation by default. With object validation, a forgotten `notNull()` leaves `null` usable.
* The type of a validated value says whether it can be `null`. `nullable()` changes the type to `@Nullable`, and a nullness checker such as NullAway or the Kotlin compiler rejects a function that passes it to a non-null parameter.
* The input is read without reflection and without casts, so the rules can be checked statically where the schema is written.

The 0.18 API is therefore the recommended starting point. `Yavi.schema()` with `apply(f)` builds a validator of typed arguments, which is the direct successor of `Yavi.arguments()`, and with `fromMap(f)` it builds a validator of untyped input, which is the new capability of the release.

## Relation to Bean Validation

YAVI covers the constraints of Bean Validation with method calls rather than annotations. For example, `@NotNull`, `@Size`, `@Min`, `@Max`, `@Email`, `@Pattern` and `@Digits` have direct counterparts in the constraint API. Groups and conditional validation are available on `ValidatorBuilder`.

The differences are deliberate:

* There is no metadata layer. A constraint is a Java expression, so refactoring tools and the compiler see it.
* There is no `ConstraintValidator` registration and no `Validation.buildDefaultValidatorFactory()`.
* A schema can read raw input, not only an annotated object.
* Violations carry a name, a structured path, a JSON Pointer and a message key, so an API can return them directly.

The full mapping of annotations to YAVI constraints is in [Comparison with Bean Validation](coming-from-bean-validation.md).

## What YAVI guarantees

* `validate(input)` returns a `Validated<T>`: either `Validated.Valid` with the created object, or `Validated.Invalid` with every violation that was found.
* A function given to `fromMap`, `apply`, `flatFromMap` or `flatApply` is called only when all of its arguments are valid.
* A property is required unless a modifier says otherwise, and that modifier is visible in the type of the validated value.
* Violations are reported in a stable structure, with `name()`, `path()`, `pointer()` and `messageKey()`.
* A validator is immutable and thread-safe once built.
* No reflection is used to read properties, and no annotation processing is required.

YAVI does not decide what is valid. It evaluates the constraints that were written and reports the result.
