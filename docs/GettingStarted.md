# Getting Started

> This content is derived from https://hibernate.org/validator/documentation/getting-started/

Welcome to YAVI.

The following paragraphs will guide you through the initial steps required to integrate
YAVI into your application.

## Prerequisites

* [Java Runtime](http://www.oracle.com/technetwork/java/index.html) >= 8
* [Apache Maven](http://maven.apache.org/)

## Project set up

In order to use YAVI within a Maven project, simply add the following dependency to
your `pom.xml`:

```xml

<dependency>
	<groupId>am.ik.yavi</groupId>
	<artifactId>yavi</artifactId>
	<version>0.7.0</version>
</dependency>
```

This tutorial uses JUnit 5 and AssertJ. Add the following dependencies as needed:

```xml

<dependency>
	<groupId>org.junit.jupiter</groupId>
	<artifactId>junit-jupiter-api</artifactId>
	<version>5.7.2</version>
	<scope>test</scope>
</dependency>
<dependency>
<groupId>org.assertj</groupId>
<artifactId>assertj-core</artifactId>
<version>3.18.1</version>
<scope>test</scope>
</dependency>
```

## Applying constraints

Let’s dive into an example to see how to apply constraints:

Create `src/main/java/com/example/Car.java` and write the following code.

```java
package com.example;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;

public class Car {
	private final String manufacturer;

	private final String licensePlate;

	private final int seatCount;

	public static Validator<Car> validator = ValidatorBuilder.<Car>of()
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

The `ValidatorBuilder.constraint` is used to declare the constraints which should be
applied to the return values of getter for the `Car` instance:

* `manufacturer` must never be null
* `licensePlate` must never be null and must be between 2 and 14 characters long
* `seatCount` must be at least 2

> You can find [the complete source code](https://github.com/making/gs-yavi) on GitHub.

## Validating constraints

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

## Where to go next?

That concludes the 5 minutes tour through the world of YAVI. If you want a more complete
introduction, it is recommended to read [the YAVI's README](https://github.com/making/yavi/blob/develop/README.md).