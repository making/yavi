# Cross-Field Rules

Some rules involve more than one property. This page describes `constraintOnTarget`, the factories that can fail, and how such violations are located and accumulated.

## Rules on the created object

`constraintOnTarget(predicate, name, violationMessage)` adds a rule that the created object must satisfy. It is available on the validator returned by `fromMap` and on a single-value validator:

```java
ObjectInputValidator<PasswordForm> passwordFormValidator = Yavi.schema()
        ._string("currentPassword", c -> c.notBlank())
        ._string("newPassword", c -> c.greaterThanOrEqual(8))
        ._string("confirmPassword", c -> c.notBlank())
        .fromMap(PasswordForm::new)
        .constraintOnTarget(form -> form.newPassword().equals(form.confirm()), "confirmPassword",
                ViolationMessage.of("password.mismatch", "\"{0}\" must match \"newPassword\""))
        .constraintOnTarget(form -> !form.current().equals(form.newPassword()), "newPassword",
                ViolationMessage.of("password.same", "\"{0}\" must differ from \"currentPassword\""));
```

* The predicate is evaluated only when every property is valid.
* The rule is reported under the name given as the second argument, so a form can show it next to a field. The name is also the `{0}` of the message.
* Rules added one after another are all checked, and every violated rule is reported.
* The result is still an `ObjectInputValidator`, so `rejectingUnknownProperties()`, `readingParameters()` and `constraintOnTarget` can follow.
* The predicate sees the created object. A value that the object does not keep cannot be read here; see below.

A custom `ValueValidator` that overrides `constraintOnTarget` keeps its rules in a `TargetConstraints`: `and` adds a rule and `check` reports every rule that a value violates.

## Locating violations

The second argument of `constraintOnTarget` is the name of the violation, `name()`, `path()` and `pointer()` follow from it. A name that is a property of the object locates the violation at that property. A name that is empty locates it at the object.

For a rule over several fields, choose a field that the user can correct, such as `confirmPassword` for a password mismatch, rather than the object itself. A form can then show the message next to that field with `messagesByName()`.

## Fallible factories

`flatFromMap(f)` and `flatApply(f)` take a function that creates the object and can fail. The function returns a `Validated` and is applied only when all arguments are valid:

```java
record DateRange(LocalDate start, LocalDate end) {

    static Validated<DateRange> of(LocalDate start, LocalDate end) {
        return start.isBefore(end) ? Validated.successWith(new DateRange(start, end))
                : Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"));
    }

}

ObjectInputValidator<DateRange> dateRangeValidator = Yavi.schema()
        ._localDate("start")
        ._localDate("end")
        .flatFromMap(DateRange::of);

dateRangeValidator.validate(Map.of("start", "2025-12-31", "end", "2025-01-01"))
        .errors().get(0).locale(); // the locale of the validation
```

* `Validated.failureWith(ViolationMessage.of(key, format))` creates one violation. Its name is empty, so it is reported on the map itself and takes the name of the property the validator is applied to.
* The function is not given the locale of the validation. The violations it returns are reported with that locale, whichever locale they were created with. `localizedWith(locale)` does the same for a violation created elsewhere.
* A violation that must be located at a property is created with a `ConstraintViolation` that has that name, or the rule is written as `constraintOnTarget`.
* `flatApply(f)` is the counterpart for typed arguments.

## Values not kept in the result

`constraintOnTarget` sees only the created object. When a validated value is not stored in that object, such as the confirmation of a password, a rule that reads it is written in the factory, which receives every validated argument:

```java
record PasswordChange(String newPassword) {}

ObjectInputValidator<PasswordChange> passwordChangeValidator = Yavi.schema()
        ._string("newPassword", c -> c.greaterThanOrEqual(8))
        ._string("confirmPassword", c -> c.notBlank())
        .flatFromMap((newPassword, confirmPassword) -> newPassword.equals(confirmPassword)
                ? Validated.successWith(new PasswordChange(newPassword))
                : Validated.failureWith(ViolationMessage.of("password.mismatch", "passwords must match")));
```

The same applies to a value that is read from the input only to check it. Declare it as a property, so it is validated and described, and let the factory decide what to store.

## Evaluation order

A validation runs in this order:

1. The properties are read and their types are checked.
2. The constraints of each property are evaluated. Violations are accumulated; the remaining properties are still checked.
3. `constraintOnTarget` rules are evaluated, only when every property is valid. Every rule is checked.
4. The factory given to `fromMap`, `flatFromMap`, `apply` or `flatApply` is called, only when there is no violation. A rule added after `map` or `andThen` on a single-value validator is checked only when the rules before it hold.

Because the properties are validated before the rules, a cross-field rule never hides a field violation. Because the rules are accumulated, a request reports every failed rule at once.
