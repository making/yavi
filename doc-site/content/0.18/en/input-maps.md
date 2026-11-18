# Maps and Untyped Values

`fromMap` ends a schema with a validator that reads a `Map<String, Object>`, such as a parsed JSON object, by the names of its properties. No cast and no reflection are involved, and the object is created only when every property is valid.

```java
record User(String email, Integer age, Role role) {}

ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.greaterThanOrEqual(0))
        ._enum("role", Role.class)
        .fromMap(User::new);

Validated<User> validated = userValidator.validate(Map.of(
        "email", "jd@example.com", "age", 30, "role", "ADMIN"));
```

## Reading an object from a map

The values are checked against the type of their property:

* A value that does not represent the type is reported with `object.type`: `"age" must be of type integer`. Nothing is coerced, so `"30"` is not a number in a map. Use [request parameters](input-parameters.md) for text.
* A number whose value is kept is accepted whatever its Java type is: `30L` and `30.0` are an `Integer`, `30.5` is not.
* An enum constant is read from its name, a temporal value, a `Duration` and a `Period` from their ISO-8601 string, a `UUID` and a `URI` from their string form: `_object("id", UUID.class)`.
* An absent key is validated like `null`. A property is required unless `optional()`, `nullable()` or `defaultValue(...)` says otherwise; the absent key is reported with `object.required` and an explicit `null` with `object.notNull`.
* A key that no property is defined for is ignored. `rejectingUnknownProperties()` reports it with `object.unknownProperty`, on that map only.
* A map with a key that is not a string is reported with `object.type`.

```java
ObjectInputValidator<User> strict = userValidator
        .rejectingUnknownProperties();
strict.validate(Map.of("email", "jd@example.com", "age", 30, "role", "ADMIN", "admin", true));
// "admin" is not a known property
```

## The type of a property

Some properties need their type to read the input. `_enum(name, type)` and `_object(name, type)` take it; without it the schema cannot be used for a map:

```java
._enum("role", Role.class)
._object("id", UUID.class)
```

`ignoringCase()` of an enum validator reads a constant in any case: `._enum(Yavi._enum("role", Role.class).ignoringCase())`.

## Validating a value that is not a property

`Yavi._string`, `Yavi._integer` and the other validators of a single value validate a value by themselves. A validator without a name is the element of a list, the value of a map or an alternative of `anyOf`, and its violations are named by the place where it is used (`roles[0]`). Validated on its own, it has the empty name and the message says "The value" in place of the name.

```java
StringValidator<String> tag = Yavi._string(c -> c.notBlank().lessThanOrEqual(10));
tag.validate("").errors().get(0).message(); // The value must not be blank
```

## Combining validators of unknown type

`untyped()` turns any validator that knows its input type into a `ValueValidator<Object, X>`, for example to validate the root of a document or to combine alternatives of different types:

```java
ValueValidator<Object, Object> id = ValueValidator.anyOf(
        Yavi._string(c -> c.notBlank()).untyped(),
        Yavi._integer(c -> c.positive()).untyped());
```

Give the type of a lambda with `untyped(type)`, because a lambda does not know its input type. The reader that coerces strings is described in [JSON Trees](input-json.md#coercing-strings).

## When the input type must be known

The input type must be known when the validator is built. `fromMap` and `untyped()` throw an `UnsupportedOperationException` for a validator whose input type is unknown: a lambda, or an `_enum` / `_object` property added without its type. Give it with `untyped(type)`, `_enum(name, type)` or `_object(name, type)`.

`readingWith(reader)` of a validator of unknown type is accepted only for a reader of JDK types. Give the type first if the input is a node of a tree model.

The type of a property and the type of the created object are checked by the compiler. A `fromMap(User::new)` whose parameter types do not match the validated values does not compile, including the nullness of a `nullable()` argument.
