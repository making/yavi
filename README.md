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

This guide uses the API of YAVI 0.18.0, which is not released yet. In order to use its snapshot within a Maven project, add the following dependency and repository to
your `pom.xml`:

```xml
<dependency>
    <groupId>am.ik.yavi</groupId>
    <artifactId>yavi</artifactId>
    <version>0.18.0-SNAPSHOT</version>
</dependency>
```

```xml
<repositories>
    <repository>
        <id>central-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

The latest release is 0.17.3; see [the reference documentation](https://yavi.ik.am) for its API.

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

#### Choosing a style

YAVI validates in two styles. Choose by what you have in hand when validation starts:

| You have | You want | Use |
| --- | --- | --- |
| Input that is not an object yet: a `Map`, a parsed JSON document, request parameters | To validate it and then create the object, so that an invalid object never exists | [`Yavi.schema()` ... `fromMap(...)`](#validating-an-input-and-creating-an-object) |
| An object that is already created, as with [Bean Validation](https://beanvalidation.org/) | The "classic" style: to check the object and get its violations | [`ValidatorBuilder`](#validating-an-object-that-exists) |

#### Validating an input and creating an object

`Yavi.schema()` describes the properties of an input, and `fromMap` ends it with a validator that reads them from a `Map<String, Object>` by their names, checks their types and gives them to a constructor. The object is created only when every property is valid.

Create `src/main/java/com/example/Car.java` and write the following code.

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

Each method of the schema declares a property with its type, its name and its constraints:

* `manufacturer` must be a string
* `licensePlate` must be a string that is between 2 and 14 characters long
* `seatCount` must be an integer that is at least 2

A property is required: an absent key is reported with `object.required` (`"manufacturer" is required`) and `null` with `object.notNull` (`"manufacturer" must not be null`), so `notNull()` need not be written and the constructor is never given `null`. A property that may be `null` says so with `nullable()` or `optional()`, as described [below](#nested-objects-collections-and-optional-properties).

To perform a validation, give a map to the validator. To demonstrate this, let’s have a look at a simple unit test:

Create `src/test/java/com/example/CarTest.java` and write the following code.

```java
package com.example;

import java.util.Map;

import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarTest {

    @Test
    void manufacturerIsMissing() {
        final Validated<Car> validated = Car.validator.validate(Map.of("licensePlate", "DD-AB-123", "seatCount", 4));

        assertThat(validated.isValid()).isFalse();
        final ConstraintViolations violations = validated.errors();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).message()).isEqualTo("\"manufacturer\" is required");
    }

    @Test
    void licensePlateTooShort() {
        final Validated<Car> validated = Car.validator
            .validate(Map.of("manufacturer", "Morris", "licensePlate", "D", "seatCount", 4));

        assertThat(validated.isValid()).isFalse();
        final ConstraintViolations violations = validated.errors();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).message())
            .isEqualTo("The size of \"licensePlate\" must be greater than or equal to 2. The given size is 1");
    }

    @Test
    void seatCountIsNotAnInteger() {
        final Validated<Car> validated = Car.validator
            .validate(Map.of("manufacturer", "Morris", "licensePlate", "DD-AB-123", "seatCount", "four"));

        assertThat(validated.isValid()).isFalse();
        final ConstraintViolations violations = validated.errors();
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).message()).isEqualTo("\"seatCount\" must be of type integer");
    }

    @Test
    void carIsValid() {
        final Validated<Car> validated = Car.validator
            .validate(Map.of("manufacturer", "Morris", "licensePlate", "DD-AB-123", "seatCount", 2));

        assertThat(validated.isValid()).isTrue();
        assertThat(validated.value()).isEqualTo(new Car("Morris", "DD-AB-123", 2));
    }
}
```

`validate()` returns a `Validated<Car>`, which holds either the `Car` (`value()`) or the `ConstraintViolations` (`errors()`). All violations are accumulated, so an invalid input reports every failed constraint at once. `Validated` is a sealed interface, so a result is also handled with an exhaustive `switch`:

```java
switch (Car.validator.validate(map)) {
    case Validated.Valid<Car>(Car car) -> save(car);
    case Validated.Invalid<Car>(ConstraintViolations violations) ->
        violations.forEach(violation -> report(violation.name(), violation.message()));
}
```

`validated(map)` returns the `Car` itself and throws a `ConstraintViolationsException` if the input is invalid.

The same schema also reads [a JSON tree](#validating-the-tree-model-of-a-json-library) and [request parameters](#validating-request-parameters).

#### Validating an object that exists

> YAVI recommends trying [the `Yavi.schema()` style](#validating-an-input-and-creating-an-object) first: an invalid object never exists, and the same schema reads a map, a JSON tree and request parameters. Use `ValidatorBuilder` when the object is already created by something else, or when you migrate from Bean Validation.

If you prefer the classic style of Bean Validation, in which an object is created first and validated afterwards, declare the constraints on the properties of the object with `ValidatorBuilder`:

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

`ValidatorBuilder.constraint` declares the constraints that are applied to the return value of a getter of the `Car` instance. Unlike a property of `Yavi.schema()`, a property that is `null` is valid unless a constraint such as `notNull()` rejects it.

> You can find [the complete source code](https://github.com/making/gs-yavi) on GitHub.

The test code changes to:

```java
package com.example;

import am.ik.yavi.core.ConstraintViolations;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarTest {

    @Test
    void carIsInvalid() {
        final Car car = new Car(null, "D", 1);
        final ConstraintViolations violations = Car.validator.validate(car);

        assertThat(violations.isValid()).isFalse();
        assertThat(violations).hasSize(3);
        assertThat(violations.get(0).message()).isEqualTo("\"manufacturer\" must not be null");
        assertThat(violations.get(1).message())
            .isEqualTo("The size of \"licensePlate\" must be greater than or equal to 2. The given size is 1");
        assertThat(violations.get(2).message()).isEqualTo("\"seatCount\" must be greater than or equal to 2");
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

`validate()` returns a `ConstraintViolations` instance, which you can iterate in order to see which validation errors occurred. It is empty if the object is valid.

`Validator` instances are thread-safe and may be reused multiple times, and so are the validators of `Yavi.schema()`.

The rest of this guide describes `Yavi.schema()`. See [the reference documentation](https://yavi.ik.am) for the details of `ValidatorBuilder`.

#### Reusable validators of a single value

The constraints of a property are domain rules that can potentially be used not only for the `Car` class. `Yavi._string`, `Yavi._integer`, `Yavi._localDate` and so on, which have the names of the properties of `Yavi.schema()`, define the validator of one value. A schema takes it as a property:

```java
package com.example;

import am.ik.yavi.arguments.IntegerValidator;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.validator.Yavi;

public record Car(String manufacturer, String licensePlate, Integer seatCount) {

    public static final StringValidator<String> manufacturerValidator = Yavi._string("manufacturer",
            c -> c.notBlank());

    public static final StringValidator<String> licensePlateValidator = Yavi._string("licensePlate",
            c -> c.greaterThanOrEqual(2).lessThanOrEqual(14));

    public static final IntegerValidator<Integer> seatCountValidator = Yavi._integer("seatCount",
            c -> c.greaterThanOrEqual(2));

    public static final ObjectInputValidator<Car> validator = Yavi.schema()
            ._string(manufacturerValidator)
            ._string(licensePlateValidator)
            ._integer(seatCountValidator)
            .fromMap(Car::new);
}
```

These small validators validate a value by themselves (`Car.licensePlateValidator.validate("D")`) and can be used in the schemas of other objects, which prevents domain rules from being scattered throughout the code.

The name can be omitted for a value that is not a property, such as the element of a list: `Yavi._string(c -> c.notBlank())`. Its violations are named by the place where the validator is used (`roles[0]`); validated by itself, they have the empty name and a message that says "The value" in place of the name (`The value must not be blank`).

A value can be transformed before it is constrained. The transforms of a `String` are written before its constraints and applied in that order; any other value is transformed with `preprocess`. The constraints and the produced value see the transformed value, and `null` is not transformed.

```java
StringValidator<String> emailValidator = Yavi._string("email", c -> c.trim().toLowerCase().notBlank().email());
emailValidator.validate("  USER@Example.COM "); // Valid[value=user@example.com]

BigDecimalValidator<BigDecimal> priceValidator = Yavi._bigDecimal("price", c -> c.positive())
        .preprocess(BigDecimal::stripTrailingZeros);
```

`c.trim()`, `c.toLowerCase()`, `c.toUpperCase()`, `c.toNormalForm(form)` and `c.transform(f)` are available wherever the constraints of a `String` value are written, such as `Yavi.schema()._string("email", c -> ...)`.

#### Nested objects, collections and optional properties

A property can also be a nested object, a list or a map. Their violations are located under the property name (`address.street`, `roles[0]`, `scores.math`), both in `name()` and in the structured `path()`.

```java
ObjectInputValidator<Address> addressValidator = Yavi.schema()
        ._string("street", c -> c.notBlank())
        ._string("city", c -> c.notBlank())
        .fromMap(Address::new);

ObjectInputValidator<User> validator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._string("nickname", c -> c.notBlank()).optional() // Optional<String>
        ._integer("age", c -> c.greaterThanOrEqual(0)).defaultValue(20)
        ._object("address", addressValidator) // address.street
        ._list("roles", Yavi._string(c -> c.notBlank())) // roles[0]
        .fromMap(User::new)
        .constraintOnTarget(user -> user.age() >= 18 || !user.roles().contains("admin"), "roles",
                ViolationMessage.of("user.adultAdmin", "\"{0}\" must not contain \"admin\" for a minor"));
```

* A property is required unless it says otherwise. `optional()`, `nullable()` and `defaultValue(...)` apply to the last added property: a `null` value is then accepted without applying the constraints and produces `Optional.empty()`, `null` or the default value.
* `optional()`, `nullable()`, `defaultValue(...)` and `recover(...)` each decide what `null` produces, so a chain in which one of them has no effect does not compile: `.nullable().optional()`, `.defaultValue(1).nullable()`, `.defaultValue(1).recover(2)`. `recover(...)` after `optional()` or `nullable()` recovers an invalid value, and `optional()`, `nullable()` or `defaultValue(...)` after `recover(...)` changes what `null` produces. A property added with a validator that accepts `null`, such as `_integer(Yavi._integer("page", c -> c.positive()).defaultValue(1))` or `_object("address", addressValidator.nullable())`, takes no modifier that accepts `null` again.
* `nullable()` is the only modifier that produces `null`: `defaultValue(null)` and `recover(null)` of a value that is never `null` (the value of constraints, a list, a map, an `Optional`) are rejected with a `NullPointerException` when the schema is built. Write `.nullable()`, `.nullable().recover(null)` or `.optional().recover(Optional.empty())`.
* `notNull()` is still the way to give the violation of `null` another message. `isNull()` needs `nullable()`: `._string("id", c -> c.isNull()).nullable()` accepts only `null`, whereas without `nullable()` no value is valid, because `null` is rejected as a missing value.
* The type of a validated value tells whether it can be `null`: `nullable()` changes it from `String` to `@Nullable String` and `optional()` to `Optional<String>`. With a nullness checker that reads JSpecify (NullAway, Kotlin), the function given to `fromMap` may therefore take parameters that are not `@Nullable`, and `fromMap(User::new)` does not compile if a `nullable()` argument is given to a parameter that is not `@Nullable` (in Kotlin, to a `String` instead of a `String?`).
* `recover(fallback)` also applies to the last added property and makes it never fail: the fallback is produced for `null` and for a value that is invalid, for example `._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)` for the size of a page. When the property is read from a map, request parameters or a JSON tree, a value that does not represent its type gives the fallback too. `recoverWith(violations -> ...)` computes the fallback from the violations, for example to log them; it is given no violation for `null`.
* The elements of `_list(name, elementValidator)` and the values of `_map(name, valueValidator)` are required like any value; give `elementValidator.nullable()` for a `List<@Nullable String>`. The violations of a list element are located under its index (`roles[0]`, `addresses[0].street`); give an element validator an empty name to report a violation on the element itself.
* `_list(name, elementValidator, c -> c.notEmpty().lessThanOrEqual(10).unique())` and `_map(name, valueValidator, c -> c.notEmpty())` constrain the list or map itself. The constraints are checked on the validated elements, only when every element is valid, and reported on the argument (`roles`). `Yavi._list(elementValidator, c -> ...)` and `Yavi._map(valueValidator, c -> ...)` are the validators of a list or map that is not an argument.
* `ValueValidator.anyOf(a, b)` tries the alternatives in order. If all of them fail, it reports one violation (`object.anyOf`); the violation is a `ConstraintViolation.NoAlternativeMatched`, whose `alternatives()` tells why each alternative failed, one `ConstraintViolations` per alternative. Its detail is a `ViolationDetail.NoAlternativeMatched` with `alternatives`.
* `ValueValidator.lazy(() -> validator)` refers to a validator that is not built yet, and `ValueValidator.recursive(self -> ...)` builds a validator from a reference to itself, for a structure that contains itself such as `record Comment(String body, List<Comment> replies)`: `ValueValidator.recursive(self -> Yavi.schema()._string("body")._list("replies", self).defaultValue(List.of()).fromMap(Comment::new))`. (`lazy()` of an `ArgumentsNValidator` is unrelated: it defers the creation of the object.)
* `constraintOnTarget` adds a rule that spans several properties. It is evaluated only when all properties are valid. After `fromMap` it returns an `ObjectInputValidator`, so `rejectingUnknownProperties()` and `readingParameters()` can follow it.

The same combinators (`optional()`, `nullable()`, `defaultValue(...)`, `recover(...)`, `prefixed(name)`, `liftList()`, `liftMap()`) are available on every `ValueValidator`, whose second type argument is the type of the validated value: `Yavi._string("name", c -> c.notBlank())` is a `ValueValidator<String, String>` that rejects `null`, and its `nullable()` a `ValueValidator<String, @Nullable String>`. `liftList()` locates the violations of an element under its index like `_list` does, so `orderValidator.liftList().prefixed("orders")` reports `orders[1].id`.

#### Validating a map such as a parsed JSON object

A validator built with `fromMap` reads the properties from a `Map<String, Object>` by their names, checks their types and creates the object. No cast is needed and nothing is read by reflection.

```java
ObjectInputValidator<Address> addressValidator = Yavi.schema()
        ._string("street", c -> c.notBlank())
        ._string("city", c -> c.notBlank())
        .fromMap(Address::new);

ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.greaterThanOrEqual(0))
        ._enum("role", Role.class) // "ADMIN"
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

* A value must represent the type of its property, otherwise `object.type` is reported (`"age" must be of type integer`). A number is taken whatever its Java type is as long as the value is kept (`30L` and `30.0` are an `Integer`, `30.5` is not), an enum constant is read from its name, a temporal value, a `Duration` and a `Period` from their ISO-8601 string, and a `UUID` and a `URI` from their string form (`_object("id", UUID.class)`). Nothing else is coerced: `"30"` is not a number, unless the map is read as [request parameters](#validating-request-parameters).
* An absent key is validated like `null`, so `optional()`, `nullable()` and `defaultValue(...)` accept it. The absent key of any other property is reported with `object.required` (`"email" is required`), and an explicit `null` with `object.notNull` or with the violation of a constraint that rejects it.
* `presence()` tells an absent key from `null`, as a partial update (PATCH) needs; see [below](#telling-an-absent-key-from-null).
* A key that no property is defined for is ignored. `rejectingUnknownProperties()` reports it with `object.unknownProperty`; it applies to that map only, not to the nested ones.
* An `_enum` or `_object` property needs its type to be read from a map: `_enum(name, type)`, `_object(name, type)`.
* The name of an enum constant is matched exactly. `ignoringCase()` of its validator reads it in any case, whatever the input is read with: `._enum(Yavi._enum("role", Role.class, c -> c).ignoringCase())` takes `"admin"`. It returns an `Arguments1Validator`, so it is written once, before or after `map`. The description and the JSON Schema keep the names of the constants.
* `apply(f)` in place of `fromMap(f)` ends the schema with a validator of arguments that already have their types, such as an `Arguments3Validator<String, String, Integer, Car>` that is called with `validate(manufacturer, licensePlate, seatCount)`.
* `flatFromMap(f)` takes a function that creates the object itself and can fail: it returns a `Validated`, for example `Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"), start, locale)`, and is applied only when all arguments are valid. `flatApply(f)` is its counterpart for arguments.
* `untyped()` turns any validator that knows its input type into a `ValueValidator<Object, X>`, for example to validate the root of a JSON document or to combine alternatives of different types with `anyOf`. Give the type of a lambda with `untyped(type)`; a lambda that takes an `Object` is the place for a custom coercion.
* A map with a key that is not a string is reported with `object.type`.

#### Telling an absent key from null

In a partial update, such as a PATCH request (JSON Merge Patch), an absent key leaves the property unchanged and `null` clears it. `presence()` reads a property as a `Presence`, which is `Present(value)`, `PresentNull` or `Absent`:

```java
record ProfilePatch(Presence<String> nickname, Presence<String> bio) {}

ObjectInputValidator<ProfilePatch> patchValidator = Yavi.schema()
        ._string("nickname", c -> c.lessThanOrEqual(50)).presence() // cannot be cleared
        ._string("bio", c -> c.lessThanOrEqual(500)).nullable().presence()
        .fromMap(ProfilePatch::new);

ProfilePatch patch = patchValidator.validated(Map.of("bio", "Hello")); // nickname is absent
patch.nickname().ifPresent(profile::setNickname); // not called
profile.setBio(patch.bio().orElse(profile.getBio())); // null if the key is null

switch (patch.bio()) {
    case Presence.Present<String>(String bio) -> profile.setBio(bio);
    case Presence.PresentNull<String> cleared -> profile.setBio(null);
    case Presence.Absent<String> unchanged -> { }
}
```

* An absent key is `Absent` and is not validated, so it is never required.
* A key that is present is validated as without `presence()`, an explicit `null` included: it is a violation unless the property is `nullable()`, which gives `PresentNull`.
* `presence()` is the last modifier of a property: `nullable()`, `defaultValue(...)` and `recover(...)` are written before it and apply to the value of a key that is present. Writing one after it does not compile, and neither does `presence()` on an `_embedded` or `_rest` property, which is not read from a key, or after `optional()`, whose `Optional` would be wrapped in a `Presence`.
* It works with every input. In request parameters, a parameter with an empty value (`bio=`) is present and `null`.
* With `apply(f)`, the argument itself is a `Presence`. `ValueValidator#liftPresence()` is the combinator, and `ObjectInputValidator.PresenceProperty` the part for `ObjectInputValidator.of(parts, mapper)`.
* In the JSON Schema the property is not `required`, and it allows `null` only if the property is `nullable()`.

`DiscriminatedUnionValidator` selects the validator of a map by the value of a key, which reads the subtypes of a sealed interface:

```java
ValueValidator<Map<String, Object>, Shape> shapeValidator = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Yavi.schema()._integer("radius", c -> c.positive()).fromMap(Circle::new))
        .when("square", Yavi.schema()._integer("side", c -> c.positive()).fromMap(Square::new));
```

Only the selected alternative is validated. An absent discriminator is reported with `object.required` and an unexpected one with `object.oneOf`.

An argument of the function does not have to be the value of one key. `_embedded` reads the same map as the properties, which builds an object from the columns of several tables in the row of a JOIN, and `_rest` collects the keys that no property is defined for:

```java
ObjectInputValidator<UserName> userNameValidator = Yavi.schema()
        ._string("first_name", c -> c.notBlank())
        ._string("last_name", c -> c.notBlank())
        .fromMap(UserName::new);

ObjectInputValidator<Department> departmentValidator = Yavi.schema()
        ._string("dept_name", c -> c.notBlank())
        ._string("dept_code", c -> c.notBlank())
        .fromMap(Department::new);

ObjectInputValidator<Employee> employeeValidator = Yavi.schema()
        ._integer("id")
        ._embedded(userNameValidator) // first_name, last_name
        ._embedded(departmentValidator).nullable() // dept_name, dept_code
        ._rest(Yavi._string(c -> c.notBlank())) // a Map<String, String> of the other keys
        .fromMap(Employee::new);
```

* The properties of an embedded validator are properties of the embedding one: they are described, are not unknown to `rejectingUnknownProperties()`, are not part of the rest, and a name defined twice is rejected when the validator is built. A `DiscriminatedUnionValidator` is embedded in the same way.
* An embedded validator with `optional()`, `nullable()` or `defaultValue(...)` is given `null` when none of its properties has a value, like the columns of a LEFT JOIN that matches no row.
* A violation of the rest is located under its key. `_rest()` without a validator takes the values as they are. `_rest` is the last property of a schema, and the validator of a schema with `_rest` has no `rejectingUnknownProperties()`, because no key is unknown.
* The rest is an empty map if there is no other key, never `null`, so it takes `recover(...)` only: `_rest().nullable()` does not compile.
* A lambda may be embedded too. The properties it reads are unknown, so the validator can then neither reject unknown properties nor read the rest.
* Embedding is also the way to read more than 16 keys.

##### Validating the tree model of a JSON library

The same schema reads the tree model of a JSON library, such as Jackson's `JsonNode`, as it is: `from(reader, f)` builds a validator of nodes with an `InputReader`, which tells how to read a node. The tree is not copied to a map, and the rules above and the violations are the same.

```java
ValueValidator<JsonNode, User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.greaterThanOrEqual(0))
        ._object("address", addressValidator)
        .from(JsonNodeReader.INSTANCE, User::new);

Validated<User> validated = userValidator.validate(jsonMapper.readTree(json));
```

`from(reader, f)` is `fromMap(f).readingWith(reader)`. `readingWith` is available on every validator and applies the reader to the whole validator, so nested validators such as `addressValidator` are built with `fromMap` or `from` whatever input is read in the end, and a validator of maps also serves nodes: `mapValidator.readingWith(reader)`. YAVI has no dependency on a JSON library; a reader is a small stateless class written once per tree model and shared by all validators. This is the one for Jackson 3:

```java
public final class JsonNodeReader implements CoercibleInputReader<JsonNode> {

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

A reader returns a number as the tree holds it, and a validator cannot see what the parser dropped. A default `JsonMapper` holds a number with a fraction as a `double`, in a tree and in a map (`readValue(json, Map.class)`) alike, and keeps the last of a key written twice:

| JSON | Read by a default mapper |
| --- | --- |
| `0.10` | `_bigDecimal` receives `0.1`; the scale is lost |
| `12345678901234567.89` | `_bigDecimal` receives `12345678901234568`; only 17 digits are kept |
| `19.990000000000000000001` | `_bigDecimal` receives `19.99`, which `fractionDigits(2)` accepts |
| `1e400` | `_double` receives `Infinity`; `_bigDecimal` reports `object.type` |
| `{"role": "user", "role": "admin"}` | `role` is `admin` |

Tell the mapper to keep a number as it is written and to refuse a key written twice:

```java
JsonMapper jsonMapper = JsonMapper.builder()
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS) // 0.10 is the BigDecimal 0.10
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION) // {"role": "user", "role": "admin"} is refused
        .build();
```

`_bigDecimal` then receives the number with every digit and its scale, `_double` and `_float` round it once, and `1e400` is not a `Double`. A document with a duplicated key is refused by the parser with a `JacksonException`, before validation.

The tree model of the JDK ([JEP 540](https://openjdk.org/jeps/540), the incubator module `jdk.incubator.json` of JDK 28) is read in the same way. A number is read from its text, so no digit is lost, and `Json.parse` refuses a key written twice:

```java
public final class JdkJsonReader implements CoercibleInputReader<JsonValue> {

    public static final JdkJsonReader INSTANCE = new JdkJsonReader();

    @Override
    public boolean isNull(JsonValue node) {
        return node instanceof JsonNull;
    }

    @Override
    public Object value(JsonValue node) {
        return switch (node) {
            case JsonString string -> string.asString();
            case JsonNumber number -> decimal(number);
            case JsonBoolean bool -> bool.asBoolean();
            default -> node;
        };
    }

    private static Object decimal(JsonNumber number) {
        try {
            return new BigDecimal(number.toString());
        }
        catch (NumberFormatException e) { // an exponent that no BigDecimal holds, such as 1e3000000000
            return number; // reported as object.type
        }
    }

    @Override
    public boolean isObject(JsonValue node) {
        return node instanceof JsonObject;
    }

    @Override
    public @Nullable JsonValue property(JsonValue object, String name) {
        return object.asMap().get(name); // null if absent
    }

    @Override
    public Iterable<Map.Entry<String, JsonValue>> properties(JsonValue object) {
        return object.asMap().entrySet();
    }

    @Override
    public boolean isArray(JsonValue node) {
        return node instanceof JsonArray;
    }

    @Override
    public Iterable<JsonValue> elements(JsonValue array) {
        return array.asList();
    }

}
```

```java
Validated<User> validated = userValidator.readingWith(JdkJsonReader.INSTANCE).validate(Json.parse(json));
```

A validator that does not know its input type (a lambda) is read only from JDK types; `readingWith(reader)` throws `UnsupportedOperationException` for it. Give the type with `untyped(type)`: the lambda then receives the value of the node (`InputReader#value`).

##### Validating request parameters

Request parameters and a submitted form are a flat map of strings, possibly several for one key. `readingParameters()` of a validator built with `fromMap` reads such a map with the same schema: a string is taken as text that stands for a value of the type of its property, and a key is the path of a nested value.

```java
ValueValidator<Map<String, List<String>>, SearchForm> searchFormValidator = Yavi.schema()
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

* An empty string is validated like `null` whatever the type of its property is, because text has no other way to say that a value is not given. `optional()`, `nullable()` and `defaultValue(...)` apply to it; give `defaultValue("")` to a string property that must receive `""`.
* A string is a number if it is a decimal number: an optional sign, digits, an optional fraction and an optional exponent (the grammar of `new BigDecimal(String)`), without surrounding whitespace or grouping separators. It is then checked like any other number: `"30"` and `"30.0"` are an `Integer`, `"30.5"`, `" 30"` and `"1,000"` are not.
* A string is a boolean if it is `"true"` or `"false"`.
* A string that does not stand for the type is reported with `object.type` like any other mismatch (`"page" must be of type integer`), never with an exception. The text of a number, a boolean or a date is not trimmed (`" 42 "` is not an integer; a `String` argument is trimmed with `c.trim()`) and nothing is converted to a string.

The keys are paths, and a key is whatever its property expects:

* `address.city` and `address[city]` are the property `city` of the object `address`. A name that holds a dot is written in brackets (`scores[a.b]`), and `tags[]` is `tags`.
* A key with one value is a scalar or a list of one element, and a key with several values is a list (`tags=a&tags=b`). Several values for a scalar property are reported with `object.type`; none is picked.
* Keys with indexes are a list in the order of the indexes: `items[0].name=pen&items[1].name=ink`. An index that is skipped is an element that is `null`, so a violation keeps the index it was sent with (`items[2].name`). An index is at most 255 (`ParameterTree.DEFAULT_MAX_INDEX`): a greater number is a name, so `tags[999999999]` is reported with `object.type` and allocates nothing.
* A key that has both values and nested keys (`a=1&a.b=2`) is reported with `object.type`, and a key that is not written as a path (`a..b`, `a[0`) matches no property.
* An empty list or object cannot be sent: its key is absent, which is validated like `null`. Give `defaultValue(List.of())` to a list that may be empty, such as checkboxes.
* The rules of absent and unknown keys do not change: with `rejectingUnknownProperties()`, `address.zip` is reported by the validator of `address`.

`readingParameters()` validates the `ParameterTree` of the map, which any validator reads with `readingWith(ParameterTree.reader())`; `ParameterTree.of(map, maxIndex)` gives another limit to the indexes.

A tree whose scalars are all strings, such as a JSON document whose numbers are sent as strings, is read with `coercingStrings()`, a decorator available on every `CoercibleInputReader`, the interface a reader of a tree model implements: `JsonNodeReader.INSTANCE.coercingStrings()`, or `InputReader.ofJdkTypes().coercingStrings()` for a tree of JDK types. The strings are read by the rules above. 
The rules are strict on purpose. `StringCoercion` relaxes them for text written by a person, such as the fields of a form or the cells of a CSV file:

```java
StringCoercion form = StringCoercion.strict().trimming().booleanWords();

ValueValidator<Map<String, String>, Signup> validator = Yavi.schema()
        ._integer("age")
        ._boolean("subscribed").defaultValue(false) // an unchecked checkbox sends nothing
        .fromMap(Signup::new)
        .readingParameters(form);

validator.validate(Map.of("age", " 30 ", "subscribed", "on")); // Signup[age=30, subscribed=true]
```

* `trimming()`: a blank string is validated like `null`, and a string read as a number, a boolean, an enum constant or a temporal value is read without its surrounding white space. A `String` value is left as it is (`c.trim()` trims it).
* `booleanWords()`: `true`, `1`, `yes`, `on` and `false`, `0`, `no`, `off` are booleans in any case. `booleanWords(trueWords, falseWords)` takes the words of the application instead.
* Each rule is given once, in any order: `booleanWords(...).booleanWords()`, in which the second words would replace the first ones, does not compile.
* The rules are given to `readingParameters(coercion)`, `ParameterTree.reader(coercion)` and `coercingStrings(coercion)` of any reader. The reader that coerces strings is an `InputReader` and cannot be coerced again, so `reader.coercingStrings(rules).coercingStrings()` does not compile: the rules are given once.
* A validator reads with the last reader it is given: `readingWith(reader)` after `readingWith(reader.coercingStrings())` or `readingParameters()` coerces no string. For any other text, override `InputReader#value(node, type)` in a reader of your own.

See [Comparison with Zod](docs/ComparisonWithZod.md) for how these features map to Zod, and [Comparison with Raoh](docs/ComparisonWithRaoh.md) for the Raoh tutorial written with YAVI.

See [Breaking changes in 0.18.0](docs/BreakingChanges_0.18.0.md) to migrate from an earlier version, and [Limitations of NullAway](docs/NullAwayLimitations.md) if NullAway reports correct code.

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
//   "message": "\"items[1].name\" must not be blank", "params": {}},
//  {"name": "age", "pointer": "/age", "messageKey": "numeric.lessThanOrEqual",
//   "message": "\"age\" must be less than or equal to 150", "params": {"max": 150}}, ...]
```

`pointer` is what the `errors[].pointer` of an RFC 9457 problem detail holds; a single violation has it as `violation.pointer()`. `params` holds the arguments of the violation by name (the names of the built-in constraints are listed in [New features in 0.18.0](docs/NewFeatures_0.18.0.md#handling-the-result); a message of your own names them with `ViolationMessage.of(key, format, names...)`). The violated value is not in a detail; `violation.args()` and `violation.violatedValue()` have it.

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
* A property is `required` unless its value accepts `null`, that is unless it is `optional()`, `nullable()` or has a `defaultValue(...)` or a `recover(...)`, whose value is written as `default`. A property with `presence()` is never `required`. A required value whose constraints accept `null` is described with the constraint `object.notNull`.
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
