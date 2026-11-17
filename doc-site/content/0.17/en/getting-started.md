# Getting Started

> **Note**
>
> This content is derived from https://hibernate.org/validator/documentation/getting-started/

Welcome to YAVI.

The following paragraphs will guide you through the initial steps required to integrate YAVI into your application.

## Prerequisites

* [Java Runtime](http://www.oracle.com/technetwork/java/index.html) >= 8
* [Apache Maven](http://maven.apache.org/)

## Project set up

In order to use YAVI within a Maven project, simply add the following dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>am.ik.yavi</groupId>
    <artifactId>yavi</artifactId>
    <version>0.17.3</version>
</dependency>
```

This tutorial uses JUnit 5 and AssertJ.
Add the following dependencies as needed:

```xml

<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.14.4</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.27.7</version>
    <scope>test</scope>
</dependency>
```

## Applying constraints

Let's dive into an example to see how to apply constraints:

Create `src/main/java/com/example/Car.java` and write the following code.

```java
package com.example;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;

public class Car {
    private final String manufacturer;

    private final String licensePlate;

    private final int seatCount;

    public static final Validator<Car> validator = ValidatorBuilder.<Car>of()
            .constraint(Car::getManufacturer, "manufacturer", c -> c.notNull())
            .constraint(Car::getLicensePlate, "licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
            .constraint(Car::getSeatCount, "seatCount", c -> c.greaterThanOrEqual(2))
            .build();

    public Car(String manufacturer, String licencePlate, int seatCount) {
        this.manufacturer = manufacturer;
        this.licensePlate = licencePlate;
        this.seatCount = seatCount;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public int getSeatCount() {
        return seatCount;
    }
}
```

The `ValidatorBuilder#constraint` is used to declare the constraints which should be applied to the return values of getter for the `Car` instance:

* `manufacturer` must never be null
* `licensePlate` must never be null and must be between 2 and 14 characters long
* `seatCount` must be at least 2

> **Tip**
>
> You can find [the complete source code](https://github.com/making/gs-yavi) on GitHub.

## Validating constraints

To perform a validation of these constraints, you use a `Validator` instance.
To demonstrate this, let's have a look at a simple unit test:

Create `src/test/java/com/example/CarTest.java` and write the following code.

```java
package com.example;

import am.ik.yavi.core.ConstraintViolations;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarTest {

    @Test
    void manufacturerIsNull() {
        final Car car = new Car(null, "DD-AB-123", 4);
        final ConstraintViolations violations = Car.validator.validate(car);

        assertThat(violations.isValid()).isFalse();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).message()).isEqualTo("\"manufacturer\" must not be null");
    }

    @Test
    void licensePlateTooShort() {
        final Car car = new Car("Morris", "D", 4);
        final ConstraintViolations violations = Car.validator.validate(car);

        assertThat(violations.isValid()).isFalse();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).message()).isEqualTo("The size of \"licensePlate\" must be greater than or equal to 2. The given size is 1");
    }

    @Test
    void seatCountTooLow() {
        final Car car = new Car("Morris", "DD-AB-123", 1);
        final ConstraintViolations violations = Car.validator.validate(car);

        assertThat(violations.isValid()).isFalse();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).message()).isEqualTo("\"seatCount\" must be greater than or equal to 2");
    }

    @Test
    void carIsValid() {
        final Car car = new Car("Morris", "DD-AB-123", 2);
        final ConstraintViolations violations = Car.validator.validate(car);

        assertThat(violations.isValid()).isTrue();
        assertThat(violations).hasSize(0);
    }
}
```

`Validator` instances are thread-safe and may be reused multiple times.

The `validate()` method returns a `ConstraintViolations` instance, which you can iterate in order to see which validation errors occurred.
The first three test methods show some expected constraint violations:

* The `notNull()` constraint on `manufacturer` is violated in `manufacturerIsNull()`
* The `greaterThanOrEqual(int)` constraint on `licensePlate` is violated in `licensePlateTooShort()`
* The `greaterThanOrEqual(int)` constraint on `seatCount` is violated in `seatCountTooLow()`

If the object validates successfully, `validate()` returns an empty `ConstraintViolations` as you can see in `carIsValid()`.
You can also check if the validation was successful with the `ConstraintViolations.isValid` method.

## Running the example

The program below is a complete compilation unit. It defines the same validation, validates an invalid `Car` and prints each violation it finds. Press Run to compile and execute it in your browser; the output appears below the cell.

```java runnable
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

public class Main {

    record Car(String manufacturer, String licensePlate, int seatCount) {
    }

    static final Validator<Car> validator = ValidatorBuilder.<Car>of()
            .constraint(Car::manufacturer, "manufacturer", c -> c.notNull())
            .constraint(Car::licensePlate, "licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
            .constraint(Car::seatCount, "seatCount", c -> c.greaterThanOrEqual(2))
            .build();

    public static void main(String[] args) {
        ConstraintViolations violations = validator.validate(new Car(null, "D", 1));
        System.out.println("Validation failed: " + violations.size() + " constraint violations");
        for (ConstraintViolation violation : violations) {
            System.out.println("- " + violation.name() + ": " + violation.message());
        }
    }
}
```
<!-- run: expected output
Validation failed: 3 constraint violations
- manufacturer: "manufacturer" must not be null
- licensePlate: The size of "licensePlate" must be greater than or equal to 2. The given size is 1
- seatCount: "seatCount" must be greater than or equal to 2
-->

## The Always Valid model

The examples above create a `Car` instance and then validate it, so an invalid `Car` can exist temporarily.
YAVI can also make invalid objects impossible to create by validating the arguments before the constructor runs and returning the result only when all arguments are valid.

Make the constructor private and expose a static factory method that returns `Validated<Car>`:

```java
package com.example;

import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.core.Validated;
import am.ik.yavi.validator.Yavi;

public final class Car {

    private static final Arguments3Validator<String, String, Integer, Car> validator = Yavi.arguments()
        ._string("manufacturer", c -> c.notNull())
        ._string("licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
        ._integer("seatCount", c -> c.greaterThanOrEqual(2))
        .apply(Car::new);

    private final String manufacturer;

    private final String licensePlate;

    private final Integer seatCount;

    public static Validated<Car> of(String manufacturer, String licensePlate, Integer seatCount) {
        return validator.validate(manufacturer, licensePlate, seatCount);
    }

    private Car(String manufacturer, String licensePlate, Integer seatCount) {
        this.manufacturer = manufacturer;
        this.licensePlate = licensePlate;
        this.seatCount = seatCount;
    }

    public String manufacturer() {
        return manufacturer;
    }

    public String licensePlate() {
        return licensePlate;
    }

    public Integer seatCount() {
        return seatCount;
    }
}
```

`Car.of(...)` validates the arguments and returns the result wrapped in `Validated`.
Because the constructor is private, a `Car` can only be obtained from the validated result.
When validation fails, inspect the violations with `Validated#errors()`:

```java
Validated<Car> validated = Car.of(null, "DD-AB-123", 4);
if (!validated.isValid()) {
    ConstraintViolations violations = validated.errors();
    // ...
}
```

If you prefer a record and want validation inside the constructor, use `lazy()` and call `validated(...)` from the compact constructor:

```java
package com.example;

import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.validator.Yavi;

public record Car(String manufacturer, String licensePlate, Integer seatCount) {

    private static final Arguments3Validator<String, String, Integer, Car> validator = Yavi.arguments()
        ._string("manufacturer", c -> c.notNull())
        ._string("licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
        ._integer("seatCount", c -> c.greaterThanOrEqual(2))
        .apply(Car::new);

    public Car {
        validator.lazy().validated(manufacturer, licensePlate, seatCount);
    }
}
```

`lazy()` returns a validator that produces a `Supplier` instead of invoking the mapper, so the validation runs without calling `Car::new` again.
Without `lazy()`, the validator would construct a `Car` while the constructor is still running and recurse until a `StackOverflowError`.

Invalid arguments make the compact constructor throw `ConstraintViolationsException`.
Its `violations()` method returns the violations:

```java
assertThatThrownBy(() -> new Car(null, "DD-AB-123", 4))
    .isInstanceOf(ConstraintViolationsException.class)
    .satisfies(e -> {
        ConstraintViolations violations = ((ConstraintViolationsException) e).violations();
        assertThat(violations).hasSize(1);
    });
```

See [Validating Arguments](validating-arguments.md) for the `Yavi.arguments()` builder and the other ways to define arguments validators.
