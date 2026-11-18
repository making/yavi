# Typed Arguments

`apply(f)` ends a schema with a validator of typed arguments, for values that are already bound to their types. The result is an `ArgumentsNValidator` that is called with the arguments and creates the object only when every argument is valid.

```java
Arguments3Validator<String, String, Integer, Car> carValidator = Yavi.schema()
        ._string("manufacturer", c -> c.notBlank())
        ._string("licensePlate", c -> c.greaterThanOrEqual(2).lessThanOrEqual(14))
        ._integer("seatCount", c -> c.greaterThanOrEqual(2))
        .apply(Car::new);

Validated<Car> validated = carValidator.validate("Morris", "DD-AB-123", 4);
```

## Ending a schema with apply

The properties are declared as for `fromMap`, with the same constraints and staged types. Their names are used in the violations, in `description()` and in the JSON Schema, as they are when the object is read from a map.

* Each argument has its own name: `apply` and `flatApply` throw an `IllegalArgumentException` when two arguments have the same name.
* All violations are accumulated, so an invalid call reports every failed constraint at once.
* A `null` argument is validated as `null`; there is no absent state. `nullable()` changes the type of the argument to `@Nullable`.
* `validated(...)` returns the object and throws a `ConstraintViolationsException` for an invalid call.
* The validator is immutable and thread-safe.

## Flat creation

`flatApply(f)` takes a function that returns a `Validated` and can fail, for example a factory method of the domain:

```java
Arguments2Validator<LocalDate, LocalDate, DateRange> validator = Yavi.schema()
        ._localDate("start")
        ._localDate("end")
        .flatApply(DateRange::of);
```

The function is applied only when every argument is valid. It is given neither the locale of the validation nor the violated value; the violations it returns are reported with the locale of the validation. `Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"))` is the way to fail.

## Relation to fromMap

`fromMap` and `apply` describe the same properties. The difference is the input:

| Input | Ending | Type |
| --- | --- | --- |
| A `Map<String, Object>`, a JSON tree, request parameters | `fromMap(f)` | `ObjectInputValidator<X>` |
| Values that already have their types | `apply(f)` | `ArgumentsNValidator<A1, ..., AN, X>` |

Use `apply` when the caller already has the values as Java objects, such as a method that takes several parameters, and `fromMap` when the input is untyped. The same schema cannot be used with both endings at once; build two validators from the same property definitions.

## Embedded and rest arguments

An argument added with `_embedded` is a `Map<String, Object>` validated by the given validator, and one added with `_rest` is a map of the keys that no other argument is defined for. In both cases the function given to `apply` receives the validated map:

```java
Arguments2Validator<Map<String, Object>, Map<String, String>, Account> validator = Yavi.schema()
        ._embedded(userNameValidator)
        ._rest(Yavi._string(c -> c.notBlank()))
        .apply(Account::new);
```

An `_embedded` validator with `optional()`, `nullable()` or `defaultValue(...)` is given `null` when none of its properties has a value. `_rest` is the last argument.

## Arity and ObjectInputValidator.of

A schema has at most 16 arguments. Embedding is the way to read more keys: `_embedded` adds the properties of another validator, and the same map is read.

When the arguments are built dynamically, use `ObjectInputValidator.of(parts, mapper)`. A part is `Part.Property(name, validator)`, `Part.Embedded(validator)` or `Part.Rest(validator)`, and the mapper receives the validated values in the order of the parts:

```java
ObjectInputValidator<Pair> validator = ObjectInputValidator.of(List.of(
        new ObjectInputValidator.Part.Property<>("first", Yavi._string("first", c -> c.notBlank()).untyped()),
        new ObjectInputValidator.Part.Property<>("second", Yavi._string("second", c -> c.notBlank()).untyped())),
        values -> new Pair((String) values.get(0), (String) values.get(1)));
```

`ObjectInputValidator.ofReversible(parts, mapper)` takes `ReversiblePart`s, a part with its reverse and its accessor, and produces a validator that also writes the object back.
