# Core Concepts

This page explains the pieces every schema uses: how input becomes an object, what a schema and a validator are, how results and violations are represented, and what the type system checks.

## Input and validated object

Reading an input is a pipeline:

1. The input is parsed by the caller or by an `InputReader`: a `Map<String, Object>`, a JSON node, request parameters, or typed arguments.
2. Each property is read by name and its type is checked against the property method.
3. The constraints of the property are evaluated.
4. When every property is valid, the validated values are passed to the function given to `fromMap(f)` or `apply(f)`, which creates the object.

A failure at any step ends the pipeline for that property. The remaining properties are still checked, and every violation is collected. The function that creates the object is not called when there is at least one violation.

Nested objects, lists and maps repeat the same pipeline at their own level. Their violations are located under the property name, for example `address.street`, `roles[0]` or `scores.math`.

## Schemas and typed validators

`Yavi.schema()` returns a builder whose methods add properties. A schema in progress is not a validator: it cannot validate anything until `fromMap(f)` or `apply(f)` ends it. The builder is immutable: each call returns a new stage, and the schema is not reused or mutated. `fromMap(f)` ends it with an `ObjectInputValidator<X>`, and `apply(f)` ends it with an `ArgumentsNValidator<..., X>`.

Both are `ValueValidator`s. A `ValueValidator<I, X>` knows its input type `I` and the type `X` it produces. The same description can therefore be given another input type with `readingWith(reader)` or `untyped()`, and it can be nested as a property of another schema.

A validator of a single value, such as `Yavi._string("email", c -> c.email())`, is a `ValueValidator<String, String>`. It validates one value and can be used on its own, as a list element, or as a property.

## Validated and violations

`Validated<T>` is a sealed interface:

* `Validated.Valid<T>` holds the created object in `value()`.
* `Validated.Invalid<T>` holds a `ConstraintViolations` in `errors()`.

`validate` plays the role of `safeParse` in a Zod-style API; the name is kept from 0.17.

`ConstraintViolations` is a list of `ConstraintViolation`. Each violation exposes:

* `name()`: the flat name, such as `address.street` or `tags[1]`.
* `path()`: the structured path, as `PathSegment.Property` and `PathSegment.Index`.
* `pointer()`: the same location as a JSON Pointer (RFC 6901), such as `/address/street`.
* `messageKey()` and `message()`: the key and the formatted message.
* `args()` and `violatedValue()`: the positional arguments and the value that failed.

`messagesByName()` and `messagesByPointer()` group the messages for a form or an API response, and `details()` returns `ViolationDetail` records that are safe to serialize. The violated value is not part of a detail.

## Required and nullable values

A property of a schema is required unless a modifier says otherwise. An absent key and an explicit `null` are both violations for a required property, with different message keys (`object.required` and `object.notNull`).

The modifier changes the type of the validated value:

* `nullable()` produces `@Nullable X`.
* `optional()` produces `Optional<X>`.
* `defaultValue(v)` and `recover(v)` produce `X` and never fail for `null`.

The compiler and a nullness checker see these types. A modifier that has no effect, such as `.nullable().optional()`, does not compile. `presence()` is the exception: it produces a `Presence<X>` that keeps `Absent`, `PresentNull` and `Present` apart.

`ValidatorBuilder` follows the other model: a property that is `null` is valid unless a constraint rejects it.

## JSpecify nullness

YAVI is annotated with [JSpecify](https://jspecify.dev). The annotations are `provided`, so YAVI does not add a runtime dependency, and an application that declares `@Nullable` values adds `org.jspecify:jspecify` itself.

With NullAway or the Kotlin compiler, the annotations are checked across the API:

* A function given to `fromMap` or `apply` receives a non-null parameter unless the corresponding property is `nullable()`.
* A value declared as `ValueValidator<String, @Nullable String>` must be handled as nullable.
* `fromMap(User::new)` does not compile when a `nullable()` property is passed to a parameter that is not `@Nullable`.

See [NullAway](integrations-nullaway.md) for the checks and their limitations.

## Thread safety

A schema and a validator are immutable. A validator built once can be stored in a `static final` field and used concurrently by any number of threads. Violations and `Validated` results are created per call.

The functions given to `fromMap`, `apply` and the constraints are called during validation. They must be thread-safe themselves if the validator is shared.

## Compile-time and run-time checks

The compiler checks:

* that a constraint exists for the type of the value it is applied to,
* that a modifier chain is meaningful, because the stages change the available methods,
* that the function given to `fromMap` or `apply` accepts the types the schema produces, including nullness where a checker is enabled,
* that a reversible schema receives a validator that knows how to write the value back.

The run time checks:

* that each input value represents the declared type,
* that each constraint holds,
* that required keys are present.

No check depends on reflection or on annotations being retained at run time.
