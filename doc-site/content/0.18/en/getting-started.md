# Getting Started

Welcome to YAVI. This page adds the dependency, writes the first schema and validates an input.

> This content is derived from https://hibernate.org/validator/documentation/getting-started/

## Prerequisites

* Java 21 or later. YAVI 0.18 is compiled for Java 21.
* Apache Maven, or an equivalent Gradle setup.

## Project setup

Add YAVI to `pom.xml`:

```xml
<dependency>
    <groupId>am.ik.yavi</groupId>
    <artifactId>yavi</artifactId>
    <version>{{yavi_release}}</version>
</dependency>
```

## Choosing a style

YAVI validates in two styles. Choose by what is available when validation starts:

| You have | You want | Use |
| --- | --- | --- |
| Input that is not an object yet: a `Map`, a parsed JSON document, request parameters | To validate it and create the object, so that an invalid object never exists | `Yavi.schema()` with `fromMap(f)` |
| An object that is already created, as with Bean Validation | To check the object and get its violations | `ValidatorBuilder` |

The rest of this page describes the first style. The classic style is shown at the end.

## Defining a schema

`Yavi.schema()` describes the properties of the input. Each property method gives the type, the name and the constraints, and `fromMap` ends the schema with a validator that reads those properties from a `Map<String, Object>` and calls a constructor.

```java
package com.example;

import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.validator.Yavi;

public record Car(String manufacturer, String licensePlate, Integer seatCount) {
    public static final ObjectInputValidator<Car> validator = Yavi.schema()
            ._string("manufacturer")
            ._string("licensePlate", c -> c.greaterThanOrEqual(2).lessThanOrEqual(14))
            ._integer("seatCount", c -> c.greaterThanOrEqual(2))
            .fromMap(Car::new);
}
```

The schema states that:

* `manufacturer` must be a string,
* `licensePlate` must be a string of 2 to 14 characters,
* `seatCount` must be an integer of at least 2.

A property is required. An absent key is reported with `object.required`, and `null` with `object.notNull`, so no `notNull()` is needed and `Car::new` is never given `null`. A property that may be `null` says so with `nullable()` or `optional()`, as described in [Optional and Default Values](optional-and-defaults.md).

## Validating input

Give a map to the validator.

> Note: a Zod-style API would call this `parse` or `safeParse`. YAVI keeps `validate` and `validated` for backward compatibility with the 0.17 API. `validate` checks the input and returns a `Validated`, so it plays the role of `safeParse`.

The following program is complete. Press the **Run** button below it to compile and execute the program in your browser; the output appears under the program. The first run downloads the WebAssembly runtime and can take several seconds, so the page may appear idle for a while. Later runs are faster.

The input leaves out `manufacturer`, so the first run reports the missing property. The cell is editable: add `"manufacturer", "yavi"` to the `Map` and press **Run** again. The program then prints the created `Car` (`valid: Car[manufacturer=yavi, ...]`).

```java runnable
import java.util.Map;

import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validated;
import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.validator.Yavi;

public class Main {

    record Car(String manufacturer, String licensePlate, Integer seatCount) {
    }

    static final ObjectInputValidator<Car> validator = Yavi.schema()
            ._string("manufacturer")
            ._string("licensePlate", c -> c.greaterThanOrEqual(2).lessThanOrEqual(14))
            ._integer("seatCount", c -> c.greaterThanOrEqual(2))
            .fromMap(Car::new);

    public static void main(String[] args) {
        switch (validator.validate(Map.of("licensePlate", "DD-AB-123", "seatCount", 4))) {
            case Validated.Valid<Car>(Car car) -> System.out.println("valid: " + car);
            case Validated.Invalid<Car>(ConstraintViolations violations) -> {
                System.out.println("invalid");
                System.out.println(violations.size());
                System.out.println(violations.get(0).message());
            }
        }
    }
}
```
<!-- run: expected output
invalid
1
"manufacturer" is required
-->

A value that does not represent the type of its property is reported with `object.type`, for example `"seatCount" must be of type integer` for the string `"four"`. No exception is thrown for a malformed input.

## Handling the result

`validate` returns a `Validated<Car>`, which holds either the `Car` or the `ConstraintViolations`. Every violation is accumulated, so an invalid input reports every failed rule at once. `Validated` is a sealed interface, so an exhaustive `switch` with record deconstruction handles both cases and the compiler checks that neither is forgotten:

```java
switch (Car.validator.validate(map)) {
    case Validated.Valid<Car>(Car car) -> save(car);
    case Validated.Invalid<Car>(ConstraintViolations violations) ->
        violations.forEach(violation -> report(violation.name(), violation.message()));
}
```

When the caller wants the object or an exception, `validated(map)` returns the `Car` itself and throws a `ConstraintViolationsException` if the input is invalid. `violations.messagesByName()` returns the messages grouped by property, and `violations.details()` returns records that a JSON library serializes for an API response. See [Handling Results](result-handling.md) and [Violations](violations.md).

## The same schema on another input

The schema is not tied to a `Map`. The same validator reads a JSON tree with `readingWith(reader)`, request parameters with `readingParameters()`, and typed arguments with `apply(f)` instead of `fromMap(f)`. See [JSON Trees](input-json.md), [Request Parameters and Forms](input-parameters.md) and [Typed Arguments](input-arguments.md).

## Validating an object that already exists (classic API)

When the object already exists, declare the constraints on its properties with `ValidatorBuilder`. The classic API returns `ConstraintViolations` for an instance:

```java
package com.example;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

public class CarValidation {

    record Car(String manufacturer, String licensePlate, Integer seatCount) {
    }

    static final Validator<Car> validator = ValidatorBuilder.<Car>of()
            .constraint(Car::manufacturer, "manufacturer", c -> c.notNull())
            .constraint(Car::licensePlate, "licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
            .constraint(Car::seatCount, "seatCount", c -> c.greaterThanOrEqual(2))
            .build();

    public static void main(String[] args) {
        ConstraintViolations violations = validator.validate(new Car(null, "D", 1));
        System.out.println(violations.size()); // 3
        System.out.println(violations.get(0).message()); // "manufacturer" must not be null
    }
}
```

`ValidatorBuilder.constraint` applies the constraints to the value returned by a getter of the `Car` instance. Unlike a schema property, a value that is `null` is valid unless a constraint such as `notNull()` rejects it. See [Object Validation](using-yavi.md).

## Next steps

* [Core Concepts](concepts.md) explains the pipeline behind these calls.
* [Defining a Schema](defining-a-schema.md) lists the property methods and constraints.
* [Optional and Default Values](optional-and-defaults.md) covers absent keys, `null` and defaults.
* [Objects and Collections](objects-and-collections.md) covers nested objects, lists and maps.
