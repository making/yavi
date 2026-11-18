# Overview

YAVI (pronounced jɑ-vάɪ) gives Java a Zod-like way to describe and validate input. Since 0.18 a schema written in code reads untyped input and either creates a typed object or reports every violation with its location. The same idea is found in [Zod](https://zod.dev) for TypeScript and in [Raoh](https://github.com/raoh-project/raoh-java) for Java; [Comparison with Zod](coming-from-zod.md) maps the two APIs.

[View the project on GitHub](https://github.com/making/yavi)

## Design principles

* Input-first. An input is described once and read from typed arguments, a map, a JSON tree or request parameters. The object is created only when every property is valid.
* Type safety by construction. Required and nullable values have different Java types, so NullAway and the Kotlin compiler can check the use of a validated value. `Validated` is sealed, and a reversible validator is checked at compile time.
* No reflection. A property is read by a method reference, not by inspecting a class.
* No runtime annotations. Constraints are written as Java code, so the compiler checks them.
* No required runtime dependency. YAVI's JSpecify annotations are `provided`, so they are not transitive and an application runs with only the YAVI jar on its classpath. Add `org.jspecify:jspecify` to compile code that declares `@Nullable`, or to run a nullness checker such as NullAway; static null checking is recommended, not required.
* Staged builders. A modifier that has no effect, such as `.nullable().optional()`, does not compile.

The classic `ValidatorBuilder` remains for objects that already exist, but the schema-first API is the recommended starting point.

## What 0.18 adds

YAVI 0.18 is compiled for Java 21 and adds a schema-first API:

* `Yavi.schema()` describes an input once and reads it from a `Map`, a JSON tree, request parameters or typed arguments.
* `fromMap(f)` creates the object only when every property is valid, so an invalid object never exists.
* Properties can be nested objects, lists, maps, unions and structures that contain themselves.
* A validated property can be mapped to a value object and written back with a reversible schema.
* Rules that span several properties are written with `constraintOnTarget` and fallible factories.
* `description()` and `JsonSchema` describe the input, for documentation and client generation.

The classic `ValidatorBuilder` API remains available for objects that already exist. See [Migration from 0.17](migration-0.17-to-0.18.md) for the changes between the releases.

## Who this is for

This documentation is for developers who receive input they do not control, such as a JSON request body, a submitted form or a row read from a database, and who want a typed object or a complete list of violations in return. It assumes Java 21 and a working Maven or Gradle build.

Readers coming from Zod or Raoh can start with [Comparison with Zod](coming-from-zod.md). Bean Validation solves the different problem of checking an object that already exists; that model is supported through `ValidatorBuilder` and described in [Object Validation](using-yavi.md).

## Documentation map

* Getting Started
  * [Introduction](introduction.md): the validation models and the guarantees of the library.
  * [Getting Started](getting-started.md): the dependency, the first schema and the result.
  * [Core Concepts](concepts.md): input, schemas, `Validated`, violations and nullness.
* Schemas
  * [Defining a Schema](defining-a-schema.md): properties, constraints, names and transforms.
  * [Optional and Default Values](optional-and-defaults.md): absent keys, `null`, defaults and recovery.
  * [Value Objects and Transforms](value-objects.md): mapping a value to a domain type.
  * [Objects and Collections](objects-and-collections.md): nested objects, lists, maps, embedding and recursion.
  * [Unions](unions.md): alternatives and discriminated unions.
  * [Cross-Field Rules](cross-field-rules.md): rules over several properties and fallible factories.
  * [Describing a Schema](describing-a-schema.md): descriptions and JSON Schema.
* Inputs and results
  * [Maps and Untyped Values](input-maps.md), [JSON Trees](input-json.md), [Request Parameters and Forms](input-parameters.md), [Typed Arguments](input-arguments.md).
  * [Handling Results](result-handling.md), [Violations](violations.md), [Messages and Localization](messages.md).
  * [Writing an Object Back](writing-back.md): reversible schemas.
* Reference
  * [Built-in Constraints](built-in-constraints.md) lists every constraint.
  * [Object Validation](using-yavi.md) covers the classic `ValidatorBuilder` API.
  * [Comparison with Bean Validation](coming-from-bean-validation.md) maps the annotations to YAVI constraints.
