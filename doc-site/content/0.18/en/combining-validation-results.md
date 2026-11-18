# Combining Results

A validation result can be combined with another one. The combination is applicative: every validation is executed and every violation is accumulated, so a caller sees all failures at once.

## Applicative validation

`Validation<E, T>` is the validation result type. `am.ik.yavi.core.Validated<T>` is a `Validation<ConstraintViolation, T>` that a validator returns.

```java
ApplicativeValidator<Email> emailValidator = validator.applicative();
Validated<Email> validated = emailValidator.validate(email);

if (validated.isValid()) {
    Email value = validated.value();
}
else {
    ConstraintViolations violations = validated.errors();
}
```

In 0.18 `Validation` is an interface and `Validated` is a sealed interface of the records `Validated.Valid` and `Validated.Invalid`, so a result is handled with an exhaustive `switch`:

```java
switch (validated) {
    case Validated.Valid<Email>(Email email) -> send(email);
    case Validated.Invalid<Email>(ConstraintViolations violations) ->
        violations.forEach(violation -> report(violation.name(), violation.message()));
}
```

`validated.value()` throws when the result is invalid and `errors()` throws when it is valid. `orElseThrow` and `throwIfInvalid` raise the exception returned by their function.

```java
Email email = validated.orElseThrow(ConstraintViolationsException::new);
validated.throwIfInvalid(ConstraintViolationsException::new);
```

`fold` creates a value of one type for both outcomes.

```java
HttpStatus status = validated.fold(violations -> HttpStatus.BAD_REQUEST, email -> HttpStatus.OK);
```

## Mapping and inspecting results

`map`, `flatMap`, `mapError`, `mapErrors`, `bimap`, `peek` and `peekErrors` transform a result. In 0.18 `map` and `flatMap` have one type parameter:

```java
Validation<ConstraintViolation, String> mapped = validated.map(Email::value);
Validation<String, Email> mappedErrors = validated.mapError(ConstraintViolation::messageKey);
Validation<String, String> bimapped = validated
        .bimap(violations -> List.of("invalid"), Email::value);
```

`Validated.successWith`, `Validated.failureWith` and `Validated.of` create a result. `Validated.of` converts a `Validation` to a `Validated`; `ConstraintViolations.of` creates the violations of a `failureWith`.

```java
Validated<Email> success = Validated.successWith(new Email("yavi@example.com"));
Validated<Email> failure = Validated.failureWith(violations);
Validated<Email> converted = Validated.of(Validation.success(new Email("yavi@example.com")));
```

A failure can also be created from a message, for example in a `flatFromMap` or `flatApply` function:

```java
record DateRange(LocalDate start, LocalDate end) {

    static Validated<DateRange> of(LocalDate start, LocalDate end) {
        return start.isBefore(end) ? Validated.successWith(new DateRange(start, end))
                : Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"));
    }

}
```

Several results can be combined with `Validated.sequence`, `Validated.traverse`, `Validated.traverseIndexed` and `Validated.traverseOptional`.

```java
Validated<List<Email>> emails = Validated.sequence(List.of(emailValidated, anotherEmailValidated));
Validated<List<Email>> traversed = Validated.traverse(inputs, emailValidator::validate);
```

## Combining Validated values

`Validated#combine` returns a `ValidatedCombining2`, and its `apply` returns a `Validated`. Further results are added with `combine` up to 16.

```java
Validated<ContactInfo> contactInfo = emailValidated.combine(phoneNumberValidated)
        .apply(ContactInfo::new);
```

All violations of both results are accumulated and the function is not called when one of them is invalid.

```java
Validated<ContactInfo> invalid = emailValidated.combine(phoneNumberValidated)
        .apply(ContactInfo::new);
// invalid: "email" must not be blank, "email" must be a valid email address,
// "phoneNumber" must match [0-9\-]+
```

`Validations.combine(a, b)` combines the same results and its `apply` returns a `Validation<E, R>`. `Validations.apply(f, a, b)` applies a function directly. In 0.18 these methods return the type they are declared with instead of the type chosen by the caller:

```java
Validation<ConstraintViolation, Address> address = Validations
        .combine(country, street)
        .apply(Address::new);
```

A combination that starts with a `Validation` returns a `Validation`, which is not assignable to a `Validated`. Convert the first result with `Validated.of`:

```java
Validation<ConstraintViolation, Country> country = ...;

Validated<Address> address = Validated.of(country).combine(street).apply(Address::new);
```

## Type changes in 0.18

The 0.18 result types replace the ones of 0.17:

* `Validation` is an interface. A class that extended it implements it, and a `Validated` is a sealed interface.
* `Validated#combine` returns `ValidatedCombiningN`, whose `apply` returns `Validated<R>`.
* `Validation#apply`, `CombiningN#apply` and `Validations.apply` return `Validation<E, R>`.
* `Validation#yieldSuccess` and `Validation#yieldFailure` are removed. Override `map`, `apply` and `combine` instead.
* `Validation#valueNullable()` is deprecated; `value()` returns the null of a success that holds it.
* `Validated#prefixed(int)` replaces `indexed(int)`. `Validation.Success` and `Validation.Failure` are records, and `Validation.Success#value` is no longer a public field.

Code that combines `Validated` results with `combine` and assigns the result to a `Validated` compiles as it is.
