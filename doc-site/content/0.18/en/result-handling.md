# Handling Results

`validate(...)` returns a `Validated<X>`, which holds either the created object or the `ConstraintViolations`. All violations are accumulated, so an invalid input reports every failed constraint at once.

```java
switch (userValidator.validate(map)) {
    case Validated.Valid<User>(User user) -> save(user);
    case Validated.Invalid<User>(ConstraintViolations violations) ->
        violations.forEach(violation -> report(violation.name(), violation.message()));
}
```

## Validated and exhaustive switches

`Validated` is a sealed interface of the records `Valid` and `Invalid`, so a result is handled with an exhaustive `switch` and no `default` branch. `Arguments1` to `Arguments16` are records too: `case Validated.Valid(Arguments2(var name, var age))`.

`isValid()`, `value()` and `errors()` are available where a switch is not convenient. `value()` of a failed result and `errors()` of a successful result throw.

A failed result prints as it reads, with the pointer, the message and the message key. The violated value is not printed unless the message shows it.

```java
userValidator.validate(Map.of("email", "invalid", "age", 300)).toString();
// Invalid[errors=[/email: "email" must be a valid email address (charSequence.email), ...]]
```

## validated() and ConstraintViolationsException

`validated(...)` returns the object and throws a `ConstraintViolationsException` if the input is invalid. The exception holds the violations:

```java
try {
    User user = userValidator.validated(map);
}
catch (ConstraintViolationsException e) {
    ConstraintViolations violations = e.violations();
}
```

Use `validated(...)` at a boundary where an invalid input is exceptional. A form or an API response handles `Validated` itself. A violation has its own `violatedValue()`, which is not in a `ViolationDetail`; see [Violations](violations.md).

## Transforming a result

`map` transforms a successful value and keeps a failure. `flatMap` continues with another `Validated` and accumulates the violations of both:

```java
Validated<Order> order = userValidator.validate(map)
        .map(User::toOrder)
        .flatMap(orderService::place);
```

`fold(errorsMapper, mapper)` handles both cases in one expression, and `bimap`, `mapErrors` and `mapError` transform the failure. The functions of `map` and `flatMap` are not applied to a failed result.

## Success and failure records

A hand-written step creates a result with `Validated.successWith(value)` or `Validated.failureWith(...)`. A failure is built from a `ViolationMessage`:

```java
return Validated.failureWith(
        ViolationMessage.of("order.outOfStock", "\"{0}\" exceeds the stock of {1}", "stock"),
        quantity, locale, stock);
```

`Validated.failureWith(message, args...)` and `ConstraintViolation.of(message, args...)` create a violation of `Locale.getDefault()` without a violated value. A function given to `flatFromMap` or `flatApply` is not given the locale: the violations it returns are reported with the locale of the validation, as `localizedWith(locale)` does elsewhere.

## Thread safety

Validators built with `Yavi.schema()`, `fromMap` or `apply` are immutable and may be shared and reused concurrently. A `Validated`, a `ConstraintViolations` and a `ConstraintViolation` are immutable values; the collections they return are unmodifiable. A `MessageFormatter` is called during validation and when a message is formatted, so it must be thread-safe if the validator is shared. A lambda validator that captures mutable state is not thread-safe by itself.
