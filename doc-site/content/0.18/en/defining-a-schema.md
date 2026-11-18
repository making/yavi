# Defining a Schema

A schema is a sequence of property methods on `Yavi.schema()`. Each method gives the type and the name of an input value, and a lambda adds its constraints. `fromMap(f)` or `apply(f)` ends the schema with a validator.

```java
import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.validator.Yavi;

record Signup(String email, Integer age) {}

ObjectInputValidator<Signup> signupValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.greaterThanOrEqual(0).lessThanOrEqual(150))
        .fromMap(Signup::new);
```

## Property methods

The methods have the names of the Java types they read:

| Type | Methods |
| --- | --- |
| Text | `_string` |
| Integral numbers | `_integer`, `_long`, `_short`, `_bigInteger` |
| Fractional numbers | `_double`, `_float`, `_bigDecimal` |
| Boolean | `_boolean` |
| Enum | `_enum(name, type)` |
| Date and time | `_localDate`, `_localTime`, `_localDateTime`, `_instant`, `_offsetDateTime`, `_zonedDateTime`, `_year`, `_yearMonth` |
| Other values | `_object(name, type)`, for example `UUID.class`, `URI.class`, `Duration.class` or `Period.class` |
| Containers | `_list`, `_map`, `_object(name, nestedValidator)`, `_embedded`, `_rest` |

Every method has overloads without constraints (`._string("name")`) and without a name (`Yavi._string(c -> ...)`), which is described below. A temporal value is read from its ISO-8601 text, an enum from the name of its constant, and a `UUID` or a `URI` from its string form.

## Constraints

The lambda receives the constraint object of the type. Its methods are the constraints that apply to that type:

```java
Yavi.schema()
        ._string("licensePlate", c -> c.notBlank().greaterThanOrEqual(2).lessThanOrEqual(14).pattern("[A-Z0-9-]+"))
        ._integer("seatCount", c -> c.greaterThanOrEqual(2).lessThanOrEqual(9))
        ._bigDecimal("price", c -> c.positive().fractionDigits(2).integerDigits(6))
        ._boolean("agreed", c -> c.isTrue())
        ._enum("role", Role.class, c -> c.oneOf(Role.ADMIN, Role.MEMBER))
        ._localDate("birthday", c -> c.before(() -> LocalDate.now()))
        .fromMap(Profile::new);
```

A constraint that does not exist for the type cannot be written, so a length constraint on an `Integer` is a compile error. The complete list is in [Built-in Constraints](built-in-constraints.md).

A constraint accepts the empty string unless `notBlank()` or `notEmpty()` is added: `email()` describes the form of a value, not that it is given.

## Names and messages

The first argument is the key of the property in the input and the name of its violations. It is also the name that appears in a message, unless a label is set:

```java
Yavi.schema()
        ._string("name", c -> c.notBlank()).label("Token name")
        ._integer("ttl", c -> c.positive()).typeMessage("\"{0}\" must be a number of seconds")
        ._list("roles", Yavi._string()).required("Select at least one role")
        .fromMap(Token::new);
```

* `label(text)` replaces `{0}` of every message of the property and its elements. The violation keeps its `name()`, `path()` and `pointer()`.
* `typeMessage(text)` replaces the message of `object.type`, which reports a value of another type.
* `required(text)` replaces the message of `object.required`, which reports an absent key.
* `message(text)` after a constraint replaces the message of that constraint.

Each of these also accepts a `ViolationMessage`, whose key a resource bundle can localize:

```java
c -> c.notBlank().message(ViolationMessage.of("user.name.notBlank", "Enter your name"))
```

A name that is defined twice in one schema is rejected when the schema is built.

## String transforms

A `String` can be transformed before its constraints. The transforms are written in the order they are applied, and the constraints and the produced value see the transformed text:

```java
StringValidator<String> emailValidator = Yavi._string("email",
        c -> c.trim().toLowerCase().toNormalForm(Normalizer.Form.NFC).notBlank().email());

emailValidator.validate("  USER@Example.COM "); // Valid[value=user@example.com]
```

The transforms are `trim()`, `toLowerCase()`, `toUpperCase()`, `toNormalForm(form)` and `transform(f)`. A `null` value is not transformed.

Any other type is transformed with `preprocess`, and a value of another type is adapted with `compose`; see [Value Objects and Transforms](value-objects.md).

## Reusing a single-value validator

A validator of one value is written with the `Yavi` methods that have the names of the property methods:

```java
public static final StringValidator<String> emailValidator =
        Yavi._string("email", c -> c.trim().notBlank().email());
```

A schema takes it as a property of the same name, and `named(name)` applies it under another key:

```java
ObjectInputValidator<Contact> contactValidator = Yavi.schema()
        ._string(emailValidator)                                  // the key is "email"
        ._string(emailValidator.named("backupEmail")).nullable()  // the key is "backupEmail"
        .fromMap(Contact::new);
```

A validator may omit its name when its place gives it one, such as the element of a list: `Yavi._string(c -> c.notBlank())`. Validated alone, its violation has the empty name and its message says "The value" instead of the name.

`_object(name, validator)` uses a nested validator under `name`. The combinators carry the name, so `emailValidator.map(Email::new).named("contact")` and `emailValidator.named("contact").map(Email::new)` are the same.

## Required properties

A property is required. An absent key is reported with `object.required`, and an explicit `null` with `object.notNull` or with the violation of a constraint that rejects `null`, such as `notBlank()`. The function given to `fromMap` is not called with `null` for a required property.

A property that may be `null` or absent says so with `nullable()`, `optional()`, `presence()` or `defaultValue(...)`. These modifiers are described in [Optional and Default Values](optional-and-defaults.md).
