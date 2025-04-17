## YAVI (*Y*et *A*nother *V*al*I*dation)

[![Apache 2.0](https://img.shields.io/github/license/making/yavi.svg)](https://www.apache.org/licenses/LICENSE-2.0) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/am.ik.yavi/yavi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/am.ik.yavi/yavi) [![Javadocs](https://www.javadoc.io/badge/am.ik.yavi/yavi.svg)](https://www.javadoc.io/doc/am.ik.yavi/yavi) [![Actions Status](https://github.com/making/yavi/workflows/CI/badge.svg)](https://github.com/making/yavi/actions)


![YAVI Logo](https://user-images.githubusercontent.com/106908/120071055-66770380-c0c8-11eb-83f1-d7eff04bad54.png)

YAVI (pronounced jɑ-vάɪ) 
is a lambda based type safe validation for Java. 

### Why YAVI?

YAVI sounds as same as a Japanese slang "YABAI (ヤバイ)" that means awesome or awful depending on the context (like "Crazy").
If you use YAVI, you will surely understand that it means the former.

The concepts are

* No reflection!
* No (runtime) annotation!
* Not only Java Beans!
* Zero dependency!

If you are not a fan of [Bean Validation](https://beanvalidation.org/), YAVI will be an awesome alternative.

YAVI has the following features:

* Type-safe constraints, unsupported constraints cannot be applied to the wrong type
* Fluent and intuitive API
* Constraints on any object. Java Beans, [Records](https://openjdk.java.net/jeps/395), [Protocol Buffers](https://developers.google.com/protocol-buffers), [Immutables](https://immutables.github.io/) and anything else.
* Lots of powerful built-in constraints
* Easy custom constraints
* Validation for groups, conditional validation
* Validation for arguments before creating an object
* Support for API and combination of validation results and validators that incorporate the concept of functional programming

See [the reference documentation](https://yavi.ik.am) for details.

### Presentations

* 2021-07-01 YAVIの紹介 (Japanese) [[Deck](https://docs.google.com/presentation/d/1ZcGN7qZpZ92XD6FwdHxpxJ1duF1kqilI97zq-0JpzsA/edit?usp=sharing)] [[Recording](https://www.youtube.com/watch?v=o0-u6QSBlv8)]

### Getting Started

> This content is derived from https://hibernate.org/validator/documentation/getting-started/

Welcome to YAVI.

The following paragraphs will guide you through the initial steps required to integrate
YAVI into your application.

#### Prerequisites

* [Java Runtime](http://www.oracle.com/technetwork/java/index.html) >= 8
* [Apache Maven](http://maven.apache.org/)

#### Project set up

In order to use YAVI within a Maven project, simply add the following dependency to
your `pom.xml`:

```xml

<dependency>
    <groupId>am.ik.yavi</groupId>
    <artifactId>yavi</artifactId>
    <version>0.14.4</version>
</dependency>
```

This tutorial uses JUnit 5 and AssertJ. Add the following dependencies as needed:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.9.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.24.2</version>
    <scope>test</scope>
</dependency>
```

#### Applying constraints

Let’s dive into an example to see how to apply constraints:

Create `src/main/java/com/example/Car.java` and write the following code.

```java
package com.example;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;

public record Car(String manufacturer, String licensePlate, Integer seatCount) {
    public static final Validator<Car> validator = ValidatorBuilder.<Car>of()
            .constraint(Car::manufacturer, "manufacturer", c -> c.notNull())
            .constraint(Car::licensePlate, "licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
            .constraint(Car::seatCount, "seatCount", c -> c.greaterThanOrEqual(2))
            .build();
}
```

The `ValidatorBuilder.constraint` is used to declare the constraints which should be
applied to the return values of getter for the `Car` instance:

* `manufacturer` must never be null
* `licensePlate` must never be null and must be between 2 and 14 characters long
* `seatCount` must be at least 2

> You can find [the complete source code](https://github.com/making/gs-yavi) on GitHub.

#### Validating constraints

To perform a validation of these constraints, you use a `Validator` instance. To
demonstrate this, let’s have a look at a simple unit test:

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

The `validate()` method returns a `ConstraintViolations` instance, which you can iterate
in order to see which validation errors occurred. The first three test methods show some
expected constraint violations:

* The `notNull()` constraint on `manufacturer` is violated in `manufacturerIsNull()`
* The `greaterThanOrEqual(int)` constraint on `licensePlate` is violated
  in `licensePlateTooShort()`
* The `greaterThanOrEqual(int)` constraint on `seatCount` is violated
  in `seatCountTooLow()`

If the object validates successfully, `validate()` returns an empty `ConstraintViolations`
as you can see in
`carIsValid()`. You can also check if the validation was successful with
the `ConstraintViolations.isValid` method.

### Always Valid model?

Are you dissatisfied with the classic validation approach of creating an object (that might be invalid) first and then validating it? If you want to create a model that can only be generated and exist in a valid state, YAVI can help you achieve this as well.

In this case, create your `Car` class as follows:

```java
package com.example;

import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.core.Validated;
import am.ik.yavi.validator.Yavi;

public final class Car {

	private static Arguments3Validator<String, String, Integer, Car> validator = Yavi.arguments()
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

`Arguments3Validator<String, String, Integer, Car>` is a validator that validates three arguments (`String`, `String`, `Integer`) and creates a `Car` instance only when validation succeeds.

With this approach, since the `Car` class's constructor is private, you need to create a `Car` instance through the `of` factory method. The `of` method validates the arguments before creating a `Car` instance. It returns the validation result wrapped in a `Validated` type. If validation fails, you cannot obtain a `Car` instance.

The test code changes to:

```java
package com.example;

import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarTest {

	@Test
	void manufacturerIsNull() {
		Validated<Car> validated = Car.of(null, "DD-AB-123", 4);
		assertThat(validated.isValid()).isFalse();
		ConstraintViolations violations = validated.errors();
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).message()).isEqualTo("\"manufacturer\" must not be null");
	}

	@Test
	void licensePlateTooShort() {
		Validated<Car> validated = Car.of("Morris", "D", 4);
		ConstraintViolations violations = validated.errors();
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).message())
			.isEqualTo("The size of \"licensePlate\" must be greater than or equal to 2. The given size is 1");
	}

	@Test
	void seatCountTooLow() {
		Validated<Car> validated = Car.of("Morris", "DD-AB-123", 1);
		ConstraintViolations violations = validated.errors();
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).message()).isEqualTo("\"seatCount\" must be greater than or equal to 2");
	}

	@Test
	void carIsValid() {
		Validated<Car> validated = Car.of("Morris", "DD-AB-123", 2);
		assertThat(validated.isValid()).isTrue();
		Car car = validated.value();
		assertThat(car.manufacturer()).isEqualTo("Morris");
		assertThat(car.licensePlate()).isEqualTo("DD-AB-123");
		assertThat(car.seatCount()).isEqualTo(2);
	}

}
```

Would you prefer to perform validation within the constructor rather than through a factory method? (And would you like to keep using records?) This can also be achieved with YAVI.
This time, let's modify the `Car` class as follows:

```java
package com.example;

import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.validator.Yavi;

public record Car(String manufacturer, String licensePlate, Integer seatCount) {

	private static Arguments3Validator<String, String, Integer, Car> validator = Yavi.arguments()
		._string("manufacturer", c -> c.notNull())
		._string("licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
		._integer("seatCount", c -> c.greaterThanOrEqual(2))
		.apply(Car::new);

	public Car {
		validator.lazy().validated(manufacturer, licensePlate, seatCount);
	}
}
```

With this approach, validation is performed at the constructor stage, and invalid objects will not be created - instead, a `ConstraintViolationsException` will be thrown.

(Using the `lazy` method prevents recursive creation of `Car` instances in the constructor even when validation succeeds. This is necessary to avoid a StackOverflow error.)

The test code changes to:

```java
package com.example;

import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarTest {

	@Test
	void manufacturerIsNull() {
		assertThatThrownBy(() -> {
			new Car(null, "DD-AB-123", 4);
		}).isInstanceOf(ConstraintViolationsException.class).satisfies(e -> {
			ConstraintViolations violations = ((ConstraintViolationsException) e).violations();
			assertThat(violations.isValid()).isFalse();
			assertThat(violations).hasSize(1);
			assertThat(violations.get(0).message()).isEqualTo("\"manufacturer\" must not be null");
		});
	}

	@Test
	void licensePlateTooShort() {
		assertThatThrownBy(() -> {
			new Car("Morris", "D", 4);
		}).isInstanceOf(ConstraintViolationsException.class).satisfies(e -> {
			ConstraintViolations violations = ((ConstraintViolationsException) e).violations();
			assertThat(violations.isValid()).isFalse();
			assertThat(violations).hasSize(1);
			assertThat(violations.get(0).message())
				.isEqualTo("The size of \"licensePlate\" must be greater than or equal to 2. The given size is 1");
		});
	}

	@Test
	void seatCountTooLow() {
		assertThatThrownBy(() -> {
			new Car("Morris", "DD-AB-123", 1);
		}).isInstanceOf(ConstraintViolationsException.class).satisfies(e -> {
			ConstraintViolations violations = ((ConstraintViolationsException) e).violations();
			assertThat(violations.isValid()).isFalse();
			assertThat(violations).hasSize(1);
			assertThat(violations.get(0).message()).isEqualTo("\"seatCount\" must be greater than or equal to 2");
		});
	}

	@Test
	void carIsValid() {
		assertThatCode(() -> {
			new Car("Morris", "DD-AB-123", 2);
		}).doesNotThrowAnyException();
	}

}
```

#### Where to go next?

That concludes the 5 minutes tour through the world of YAVI. If you want a more complete
introduction, it is recommended to read "[Using YAVI](https://yavi.ik.am/#using-yavi)" in the reference document.

### Required

* Java 8+

### License

Licensed under the Apache License, Version 2.0.
