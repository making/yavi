## YAVI (*Y*et *A*nother *V*al*I*dation)

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
* Only one dependency (JSpecify)

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

* [Java Runtime](http://www.oracle.com/technetwork/java/index.html) >= 21 for YAVI 0.18 and later, >= 8 for earlier versions
* [Apache Maven](http://maven.apache.org/)

#### Project set up

In order to use YAVI within a Maven project, simply add the following dependency to
your `pom.xml`:

```xml

<dependency>
    <groupId>am.ik.yavi</groupId>
    <artifactId>yavi</artifactId>
    <version>0.17.3</version>
</dependency>
```

This tutorial uses JUnit 5 and AssertJ. Add the following dependencies as needed:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.11.4</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.26.3</version>
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
		assertThat(validated.isValid()).isFalse();
		ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).message())
			.isEqualTo("The size of \"licensePlate\" must be greater than or equal to 2. The given size is 1");
	}

	@Test
	void seatCountTooLow() {
		Validated<Car> validated = Car.of("Morris", "DD-AB-123", 1);
		assertThat(validated.isValid()).isFalse();
		ConstraintViolations violations = validated.errors();
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

#### Reusable and composable validators

Let's take a look at a more advanced usage.

The following constraints are domain rules that can potentially be used not only for the `Car` class:

> * `manufacturer` must never be null
> * `licensePlate` must never be null and must be between 2 and 14 characters long
> * `seatCount` must be at least 2

In YAVI, you can define small validators for each value and then combine them to compose validators for objects.

Please take a look at the following definition:

```java
package com.example;

import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.arguments.IntegerValidator;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.core.Validated;
import am.ik.yavi.validator.Yavi;

public final class Car {

  public static StringValidator<String> manufacturerValidator = Yavi._string("manufacturer", c -> c.notNull());

  public static StringValidator<String> licensePlateValidator = Yavi._string("licensePlate",
          c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14));

  public static IntegerValidator<Integer> seatCountValidator = Yavi._integer("seatCount", c -> c.greaterThanOrEqual(2));

  private static Arguments3Validator<String, String, Integer, Car> validator = manufacturerValidator
          .split(licensePlateValidator)
          .split(seatCountValidator)
          .apply(Car::new);

  private final String manufacturer;

  private final String licensePlate;

  private final Integer seatCount;

  public static Validated<Car> of(String manufacturer, String licensePlate, Integer seatCount) {
    return validator.validate(manufacturer, licensePlate, seatCount);
  }

  // omitted

}
```

After defining validators for `manufacturer`, `licensePlate`, and `seatCount`, it combines these three validators (`StringValidator<T>` is a validator that validates a `String` and returns an object of type `T`, and `IntegerValidator<T>` is a validator that validates an `Integer` and returns an object of type `T`) to create an `Arguments3Validator<String, String, Integer, Car>`.

These small validators can also be used for generating other objects, which prevents domain rules from being scattered throughout the code.

`Yavi._string`, `Yavi._integer`, `Yavi._localDate` and so on have the names of the arguments of `Yavi.arguments()`. The name can be omitted for a value that is not a property, such as the element of a list: `Yavi._string(c -> c.notBlank())`. Its violations are named by the place where the validator is used (`roles[0]`); validated by itself, they have the empty name and a message that says "The value" in place of the name (`The value must not be blank`).

A value can be transformed before it is constrained. The transforms of a `String` are written before its constraints and applied in that order; any other value is transformed with `preprocess`. The constraints and the produced value see the transformed value, and `null` is not transformed.

```java
StringValidator<String> emailValidator = Yavi._string("email", c -> c.trim().toLowerCase().notBlank().email());
emailValidator.validate("  USER@Example.COM "); // Valid[user@example.com]

BigDecimalValidator<BigDecimal> priceValidator = Yavi._bigDecimal("price", c -> c.notNull().positive())
        .preprocess(BigDecimal::stripTrailingZeros);
```

`c.trim()`, `c.toLowerCase()`, `c.toUpperCase()`, `c.toNormalForm(form)` and `c.transform(f)` are available wherever the constraints of a `String` value are written, such as `Yavi.arguments()._string("email", c -> ...)`.

#### Nested objects, collections and optional arguments

An argument can also be a nested object, a list or a map. Their violations are located under the argument name (`address.street`, `roles[0]`, `scores.math`), both in `name()` and in the structured `path()`.

```java
ValueValidator<AddressForm, Address> addressValidator = Yavi.arguments()
        ._string("street", c -> c.notBlank())
        ._string("city", c -> c.notBlank())
        .apply(Address::new)
        .compose(form -> Arguments.of(form.street(), form.city()));

Arguments5Validator<String, String, Integer, AddressForm, List<String>, User> validator = Yavi.arguments()
        ._string("email", c -> c.notBlank().email())
        ._string("nickname", c -> c.notBlank()).optional() // Optional<String>
        ._integer("age", c -> c.greaterThanOrEqual(0)).defaultValue(20)
        ._object("address", addressValidator) // address.street
        ._list("roles", Yavi._string(c -> c.notBlank())) // roles[0]
        .apply(User::new)
        .constraintOnTarget(user -> user.age() >= 18 || !user.roles().contains("admin"), "roles",
                ViolationMessage.of("user.adultAdmin", "\"{0}\" must not contain \"admin\" for a minor"));
```

* `optional()`, `nullable()` and `defaultValue(...)` apply to the last added argument. A `null` value is then accepted without applying the constraints and produces `Optional.empty()`, `null` or the default value.
* `recover(fallback)` also applies to the last added argument and makes it never fail: the fallback is produced for `null` and for a value that is invalid, for example `._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)` for the size of a page. When the argument is read from a map, request parameters or a JSON tree, a value that does not represent its type gives the fallback too. `recoverWith(violations -> ...)` computes the fallback from the violations, for example to log them; it is given no violation for `null`.
* `_object(name, validator)`, `_list(name, elementValidator)` and `_map(name, valueValidator)` reject `null` with `object.notNull` unless one of the above is applied. The violations of a list element are located under its index (`roles[0]`, `addresses[0].street`); give an element validator an empty name to report a violation on the element itself.
* `_list(name, elementValidator, c -> c.notEmpty().lessThanOrEqual(10).unique())` and `_map(name, valueValidator, c -> c.notEmpty())` constrain the list or map itself. The constraints are checked on the validated elements, only when every element is valid, and reported on the argument (`roles`). `Yavi._list(elementValidator, c -> ...)` and `Yavi._map(valueValidator, c -> ...)` are the validators of a list or map that is not an argument.
* `ValueValidator.anyOf(a, b)` tries the alternatives in order. If all of them fail, it reports `object.anyOf` followed by the violations of every alternative.
* `ValueValidator.lazy(() -> validator)` refers to a validator that is not built yet, and `ValueValidator.recursive(self -> ...)` builds a validator from a reference to itself, for a structure that contains itself such as `record Comment(String body, List<Comment> replies)`: `ValueValidator.recursive(self -> Yavi.arguments()._string("body")._list("replies", self).defaultValue(List.of()).fromMap(Comment::new))`. (`lazy()` of an `ArgumentsNValidator` is unrelated: it defers the creation of the object.)
* `constraintOnTarget` adds a rule that spans several arguments. It is evaluated only when all arguments are valid.

The same combinators (`optional()`, `nullable()`, `defaultValue(...)`, `recover(...)`, `prefixed(name)`, `liftList()`, `liftMap()`) are available on every `ValueValidator`. `liftList()` locates the violations of an element under its index like `_list` does, so `orderValidator.liftList().prefixed("orders")` reports `orders[1].id`.

#### Validating a map such as a parsed JSON object

`fromMap` builds a validator from the same arguments that reads them from a `Map<String, Object>` by their names, checks their types and creates the object. No cast is needed and nothing is read by reflection.

```java
MapInputValidator<Address> addressValidator = Yavi.arguments()
        ._string("street", c -> c.notBlank())
        ._string("city", c -> c.notBlank())
        .fromMap(Address::new);

MapInputValidator<User> userValidator = Yavi.arguments()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.notNull().greaterThanOrEqual(0))
        ._enum("role", Role.class, c -> c.notNull()) // "ADMIN"
        ._localDate("birthday") // "2000-01-01"
        ._object("address", addressValidator) // a nested map
        ._list("tags", Yavi._string(c -> c.notBlank())) // a list
        ._string("nickname").optional()
        .fromMap(User::new);

Map<String, Object> map = Map.of(
        "email", "jd@example.com",
        "age", 30,
        "role", "ADMIN",
        "birthday", "2000-01-01",
        "address", Map.of("street", "1-2-3", "city", "Tokyo"),
        "tags", List.of("java", "yavi"));
Validated<User> validated = userValidator.validate(map);
```

* A value must represent the type of its argument, otherwise `object.type` is reported (`"age" must be of type integer`). A number is taken whatever its Java type is as long as the value is kept (`30L` and `30.0` are an `Integer`, `30.5` is not), an enum constant is read from its name and a temporal value from its ISO-8601 string. Nothing else is coerced: `"30"` is not a number, unless the map is read as [request parameters](#validating-request-parameters).
* An absent key is validated like `null`, so `optional()`, `nullable()`, `defaultValue(...)` and arguments without `notNull()` accept it. If the argument rejects `null`, the absent key is reported with `object.required` (`"email" is required`); an explicit `null` is reported by the constraints (`object.notNull`).
* A key that no argument is defined for is ignored. `rejectingUnknownProperties()` reports it with `object.unknownProperty`; it applies to that map only, not to the nested ones.
* An `_enum` or `_object` argument needs its type to be read from a map: `_enum(name, type)`, `_object(name, type)`.
* The name of an enum constant is matched exactly. `ignoringCase()` of its validator reads it in any case, whatever the input is read with: `._enum(Yavi._enum("role", Role.class).ignoringCase())` takes `"admin"`. The description and the JSON Schema keep the names of the constants.
* `flatFromMap(f)` takes a function that creates the object itself and can fail: it returns a `Validated`, for example `Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"), start, locale)`, and is applied only when all arguments are valid. `flatApply(f)` is its counterpart for arguments.
* `untyped()` turns any validator that knows its input type into a `ValueValidator<Object, X>`, for example to validate the root of a JSON document or to combine alternatives of different types with `anyOf`. Give the type of a lambda with `untyped(type)`; a lambda that takes an `Object` is the place for a custom coercion.
* A map with a key that is not a string is reported with `object.type`.

`DiscriminatedUnionValidator` selects the validator of a map by the value of a key, which reads the subtypes of a sealed interface:

```java
ValueValidator<Map<String, Object>, Shape> shapeValidator = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Yavi.arguments()._integer("radius", c -> c.notNull().positive()).fromMap(Circle::new))
        .when("square", Yavi.arguments()._integer("side", c -> c.notNull().positive()).fromMap(Square::new));
```

Only the selected alternative is validated. An absent discriminator is reported with `object.required` and an unexpected one with `object.oneOf`.

An argument does not have to be the value of one key. `_embedded` reads the same map as the other arguments, which builds an object from the columns of several tables in the row of a JOIN, and `_rest` collects the keys that no argument is defined for:

```java
MapInputValidator<UserName> userNameValidator = Yavi.arguments()
        ._string("first_name", c -> c.notBlank())
        ._string("last_name", c -> c.notBlank())
        .fromMap(UserName::new);

MapInputValidator<Department> departmentValidator = Yavi.arguments()
        ._string("dept_name", c -> c.notBlank())
        ._string("dept_code", c -> c.notBlank())
        .fromMap(Department::new);

MapInputValidator<Employee> employeeValidator = Yavi.arguments()
        ._integer("id", c -> c.notNull())
        ._embedded(userNameValidator) // first_name, last_name
        ._embedded(departmentValidator).nullable() // dept_name, dept_code
        ._rest(Yavi._string(c -> c.notBlank())) // a Map<String, String> of the other keys
        .fromMap(Employee::new);
```

* The properties of an embedded validator are properties of the embedding one: they are described, are not unknown to `rejectingUnknownProperties()`, are not part of the rest, and a name defined twice is rejected when the validator is built. A `DiscriminatedUnionValidator` is embedded in the same way.
* An embedded validator with `optional()`, `nullable()` or `defaultValue(...)` is given `null` when none of its properties has a value, like the columns of a LEFT JOIN that matches no row.
* A violation of the rest is located under its key. `_rest()` without a validator takes the values as they are. `_rest` and `rejectingUnknownProperties()` exclude each other.
* A lambda may be embedded too. The properties it reads are unknown, so the validator can then neither reject unknown properties nor read the rest.
* Embedding is also the way to read more than 16 keys.

##### Validating the tree model of a JSON library

The same arguments read the tree model of a JSON library, such as Jackson's `JsonNode`, as it is: `from(reader, f)` builds a validator of nodes with an `InputReader`, which tells how to read a node. The tree is not copied to a map, and the rules above and the violations are the same.

```java
ValueValidator<JsonNode, User> userValidator = Yavi.arguments()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.notNull().greaterThanOrEqual(0))
        ._object("address", addressValidator)
        .from(JsonNodeReader.INSTANCE, User::new);

Validated<User> validated = userValidator.validate(jsonMapper.readTree(json));
```

`from(reader, f)` is `fromMap(f).readingWith(reader)`. `readingWith` is available on every validator and applies the reader to the whole validator, so nested validators such as `addressValidator` are built with `fromMap` or `from` whatever input is read in the end, and a validator of maps also serves nodes: `mapValidator.readingWith(reader)`. YAVI has no dependency on a JSON library; a reader is a small stateless class written once per tree model and shared by all validators. This is the one for Jackson 3:

```java
public final class JsonNodeReader implements InputReader<JsonNode> {

    public static final JsonNodeReader INSTANCE = new JsonNodeReader();

    @Override
    public boolean isNull(JsonNode node) {
        return node.isNull() || node.isMissingNode();
    }

    @Override
    public Object value(JsonNode node) { // a scalar as a JDK type
        if (node.isString()) {
            return node.stringValue();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return node;
    }

    @Override
    public boolean isObject(JsonNode node) {
        return node.isObject();
    }

    @Override
    public @Nullable JsonNode property(JsonNode object, String name) {
        return object.get(name); // null if absent
    }

    @Override
    public Iterable<Map.Entry<String, JsonNode>> properties(JsonNode object) {
        return object.properties();
    }

    @Override
    public boolean isArray(JsonNode node) {
        return node.isArray();
    }

    @Override
    public Iterable<JsonNode> elements(JsonNode array) {
        return array;
    }

}
```

A validator that does not know its input type (a lambda) is read only from JDK types; `readingWith(reader)` throws `UnsupportedOperationException` for it. Give the type with `untyped(type)`: the lambda then receives the value of the node (`InputReader#value`).

##### Validating request parameters

Request parameters and a submitted form are a flat map of strings, possibly several for one key. `readingParameters()` of a validator built with `fromMap` reads such a map with the same arguments: a string is taken as text that stands for a value of the type of its argument, and a key is the path of a nested value.

```java
ValueValidator<Map<String, List<String>>, SearchForm> searchFormValidator = Yavi.arguments()
        ._string("q", c -> c.notBlank())
        ._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
        ._boolean("includeArchived").defaultValue(false)
        ._localDate("since")
        ._list("tags", Yavi._string(c -> c.notBlank())).defaultValue(List.of())
        ._object("address", addressValidator).nullable()
        .fromMap(SearchForm::new)
        .readingParameters();

@GetMapping("/search")
Object search(@RequestParam MultiValueMap<String, String> params) { // ?q=yavi&page=2&since=&tags=a&tags=b&address.city=Tokyo
    Validated<SearchForm> validated = searchFormValidator.validate(params); // SearchForm[q=yavi, page=2, includeArchived=false, since=null, tags=[a, b], address=Address[city=Tokyo]]
    // ...
}
```

The type of the map is the one the validator is assigned to: a `Map<String, String>`, a `Map<String, List<String>>` such as Spring's `MultiValueMap`, or the `Map<String, String[]>` of the Servlet API.

The values are text:

* An empty string is validated like `null` whatever the type of its argument is, because text has no other way to say that a value is not given. `optional()`, `nullable()` and `defaultValue(...)` apply to it; give `defaultValue("")` to a string argument that must receive `""`.
* A string is a number if it is a decimal number: an optional sign, digits, an optional fraction and an optional exponent (the grammar of `new BigDecimal(String)`), without surrounding whitespace or grouping separators. It is then checked like any other number: `"30"` and `"30.0"` are an `Integer`, `"30.5"`, `" 30"` and `"1,000"` are not.
* A string is a boolean if it is `"true"` or `"false"`.
* A string that does not stand for the type is reported with `object.type` like any other mismatch (`"page" must be of type integer`), never with an exception. The text of a number, a boolean or a date is not trimmed (`" 42 "` is not an integer; a `String` argument is trimmed with `c.trim()`) and nothing is converted to a string.

The keys are paths, and a key is whatever its argument expects:

* `address.city` and `address[city]` are the property `city` of the object `address`. A name that holds a dot is written in brackets (`scores[a.b]`), and `tags[]` is `tags`.
* A key with one value is a scalar or a list of one element, and a key with several values is a list (`tags=a&tags=b`). Several values for a scalar argument are reported with `object.type`; none is picked.
* Keys with indexes are a list in the order of the indexes: `items[0].name=pen&items[1].name=ink`. An index that is skipped is an element that is `null`, so a violation keeps the index it was sent with (`items[2].name`). An index is at most 255 (`ParameterTree.DEFAULT_MAX_INDEX`): a greater number is a name, so `tags[999999999]` is reported with `object.type` and allocates nothing.
* A key that has both values and nested keys (`a=1&a.b=2`) is reported with `object.type`, and a key that is not written as a path (`a..b`, `a[0`) matches no argument.
* An empty list or object cannot be sent: its key is absent, which is validated like `null`. Give `defaultValue(List.of())` to a list that may be empty, such as checkboxes.
* The rules of absent and unknown keys do not change: with `rejectingUnknownProperties()`, `address.zip` is reported by the validator of `address`.

`readingParameters()` validates the `ParameterTree` of the map, which any validator reads with `readingWith(ParameterTree.reader())`; `ParameterTree.of(map, maxIndex)` gives another limit to the indexes.

A tree whose scalars are all strings, such as a JSON document whose numbers are sent as strings, is read with `coercingStrings()`, a decorator available on every reader: `JsonNodeReader.INSTANCE.coercingStrings()`, or `InputReader.ofJdkTypes().coercingStrings()` for a tree of JDK types. The strings are read by the rules above. 
The rules are strict on purpose. `StringCoercion` relaxes them for text written by a person, such as the fields of a form or the cells of a CSV file:

```java
StringCoercion form = StringCoercion.strict().trimming().booleanWords();

ValueValidator<Map<String, String>, Signup> validator = Yavi.arguments()
        ._integer("age", c -> c.notNull())
        ._boolean("subscribed").defaultValue(false) // an unchecked checkbox sends nothing
        .fromMap(Signup::new)
        .readingParameters(form);

validator.validate(Map.of("age", " 30 ", "subscribed", "on")); // Signup[age=30, subscribed=true]
```

* `trimming()`: a blank string is validated like `null`, and a string read as a number, a boolean, an enum constant or a temporal value is read without its surrounding white space. A `String` value is left as it is (`c.trim()` trims it).
* `booleanWords()`: `true`, `1`, `yes`, `on` and `false`, `0`, `no`, `off` are booleans in any case. `booleanWords(trueWords, falseWords)` takes the words of the application instead.
* The rules are given to `readingParameters(coercion)`, `ParameterTree.reader(coercion)` and `coercingStrings(coercion)` of any reader. For any other text, override `InputReader#value(node, type)` in a reader of your own.

See [Comparison with Zod](docs/ComparisonWithZod.md) for how these features map to Zod, and [Comparison with Raoh](docs/ComparisonWithRaoh.md) for the Raoh tutorial written with YAVI.

#### Returning the violations

`ConstraintViolations` has the forms that a form and an API response need, so that they are not written as a stream pipeline at every call site:

```java
ConstraintViolations violations = validated.errors();

// for a form: the messages next to each field, the root being the empty name
Map<String, List<String>> byName = violations.messagesByName();
// {"email": ["\"email\" must not be blank"], "items[1].name": ["\"items[1].name\" must not be blank"]}

// the same with JSON Pointers (RFC 6901) as the keys
Map<String, List<String>> byPointer = violations.messagesByPointer();
// {"/email": [...], "/items/1/name": [...]}

// for an API response: records that a JSON library serializes as they are
List<ViolationDetail> details = violations.details();
// [{"name": "items[1].name", "pointer": "/items/1/name", "messageKey": "charSequence.notBlank",
//   "message": "\"items[1].name\" must not be blank", "args": ["items[1].name", ""]}, ...]
```

`pointer` is what the `errors[].pointer` of an RFC 9457 problem detail holds; a single violation has it as `violation.pointer()`. `args` ends with the violated value by convention, so map the details to a type of your own if that value must not be sent back.

#### Describing a validator and generating a JSON Schema

A validator describes the input it expects. `description()` returns a read-only tree (`ValueDescription`) of objects with properties, lists, maps, alternatives and values with their constraints, which tools can walk. `JsonSchema` is one consumer.

```java
ValueDescription description = validator.description();
Map<String, Object> schema = JsonSchema.of(description);
String json = JsonSchema.toJson(description);
// {"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{"email":{"type":"string","minLength":1,"format":"email"},...},"required":["email","address","roles"]}
```

* A constraint is described by its message key and arguments, for example `container.lessThanOrEqual` with `[14]`.
* An object that rejects unknown properties is written with `additionalProperties: false`, and one that reads the rest with the schema of its values.
* The constraints of a list or map are described with it and written as `minItems`, `maxItems`, `uniqueItems`, `minProperties` and `maxProperties`.
* A property is `required` unless its value accepts `null`, that is unless it is `optional()`, `nullable()`, has a `defaultValue(...)` or a `recover(...)`, whose value is written as `default`, or has no constraint that rejects `null` such as `notNull()`.
* The combinators carry the description. A lambda and the result of `ValueValidator.compose` are unknown; describe them with `describedAs(...)`.
* A recursive structure is described with a `ValueDescription.Reference` where it contains itself, and written as `$ref`: to `#` for the described validator itself, and otherwise to `$defs` under the name given with `ValueValidator.lazy(name, ...)` or `recursive(name, ...)`.
* Conditional constraints and `constraintOnTarget` rules are not described. Constraints without a JSON Schema keyword are omitted from the schema.

#### Where to go next?

That concludes the 5 minutes tour through the world of YAVI. If you want a more complete
introduction, it is recommended to read "[Using YAVI](https://yavi.ik.am/#using-yavi)" in the reference document.

### Required

* Java 21+ for YAVI 0.18 and later
* Java 8+ for YAVI 0.17 and earlier

### License

Licensed under the Apache License, Version 2.0.
