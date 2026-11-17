# Combining Validation Results

YAVI supports a functional programming concept known as [Applicative Functor](https://en.wikipedia.org/wiki/Applicative_functor).
A sequence of validations are executed while accumulating the results (`ConstraintViolation`), even if some or all of these validations fail during the execution chain.

It is helpful when you want to combine validation results of multiple [Value Object](https://martinfowler.com/bliki/ValueObject.html)s to produce a new object. (Of course, it is also useful for any objects other than Value Objects.)

## Validating with Applicative Functor

`am.ik.yavi.fn.Validation<E, T>` class is the implementation of Applicative Functor. `E` is the type of error and `T` is the type of target object.

It can be obtained by `am.ik.yavi.core.ApplicativeValidator` that can be converted from `Validator` by `applicative()` method.

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(User::getName, "name", c -> c.notNull().lessThanOrEqual(20))
    // ...
    .build();

ApplicativeValidator<User> applicativeValidator = validator.applicative();
```

`am.ik.yavi.core.Validated<T>` is a shortcut of `Validation<ConstraintViolation, T>` which is specialized for `Validator` 's usage.

```java
Validator<Email> emailValidator = ValidatorBuilder.<Email> of()
    .constraint(Email::value, "email", c -> c.notBlank().email())
    .build();
Validator<PhoneNumber> phoneNumberValidator = ValidatorBuilder.<PhoneNumber> of()
    .constraint(PhoneNumber::value, "phoneNumber", c -> c.notBlank().pattern("[0-9\\-]+"))
    .build();

Validated<Email> emailValidated = emailValidator.applicative().validate(email);
Validated<PhoneNumber> phoneNumberValidated = phoneNumberValidator.applicative().validate(phoneNumber);
```

The validated target or constraint violations can be retrieved from the `Validated` instance as follows:

```java
if (emailValidated.isValid()) {
    Email email = emailValidated.value(); // throws NoSuchElementException if it is invalid
} else {
    ConstraintViolations violations = emailValidated.errors(); // throws NoSuchElementException if it is valid
}
// or
Email email = emailValidated.orElseThrow(violations -> new ConstraintViolationsException(violations));
```

`Validation#throwIfInvalid` and `ConstraintViolations#throwIfInvalid` throw the exception supplied by their function argument instead of returning a value:

```java
emailValidated.throwIfInvalid(ConstraintViolationsException::new);
```

`fold` method is convenient if you want to create an instance of the same type regardless of the success or failure of the validation.

```java
HttpStatus status = emailValidated.fold(violations -> HttpStatus.BAD_REQUEST, email -> HttpStatus.OK);
```

For `Validated`, the error type of `fold` is `List<ConstraintViolation>`.

## Mapping and inspecting validation results

`Validation` provides `map`, `flatMap`, `mapError`, `mapErrors`, `bimap`, `orElseThrow` and `throwIfInvalid` to transform or unwrap the result:

```java
Validation<ConstraintViolation, String> mapped = emailValidated.map(Email::value);
Validation<String, Email> mappedErrors = emailValidated.mapError(ConstraintViolation::messageKey);
Validation<String, String> bimapped = emailValidated
    .bimap(violations -> List.of("invalid"), Email::value);
```

`am.ik.yavi.core.Validated` is a shortcut of `Validation<ConstraintViolation, T>`.
It can be created with `Validated.successWith`, `Validated.failureWith` and `Validated.of`:

```java
Validated<Email> success = Validated.successWith(new Email("yavi@example.com"));
Validated<Email> failure = Validated.failureWith(violations); // ConstraintViolations is a List<ConstraintViolation>
Validated<Email> fromValidation = Validated.of(Validation.success(new Email("yavi@example.com")));
```

Multiple results can be combined with `Validated.sequence`, `Validated.traverse`, `Validated.traverseIndexed` and `Validated.traverseOptional`:

```java
Validated<List<Email>> emails = Validated.sequence(List.of(emailValidated, anotherEmailValidated));
Validated<List<Email>> traversed = Validated.traverse(inputs, emailValidator::validate);
Validated<List<Email>> indexed = Validated.traverseIndexed(inputs,
    (input, index) -> emailValidator.indexed(index).validate(input));
Validated<Optional<Email>> optional = Validated.traverseOptional(optionalInput, emailValidator::validate);
```

`ConstraintViolations#of(List<ConstraintViolation>)` creates a `ConstraintViolations` from a list of violations.

## Combining `Validation` / `Validated` objects

`Validation` / `Validated` objects can be combined to produce a new object.
In the bellow example, `ContactInfo` instance is created using `Email` and `PhoneNumber` after validating them.

```java
Validated<ContactInfo> contactInfoValidated = emailValidated.combine(phoneNumberValidated)
    .apply((em, ph) -> new ContactInfo(em, ph));
// or
Validated<ContactInfo> contactInfoValidated = Validations.combine(emailValidated, phoneNumberValidated)
    .apply((em, ph) -> new ContactInfo(em, ph));
```

`Validations.combine(...)` supports 2 to 16 validations.

The important thing here is that even if the validation of `Email` or `PhoneNumber` fails, all validation results are accumulated without shortcuts in the middle.

For example, if you put a blank space in `Email` and `a` in `PhoneNumber` and try to create a `ContactInfo`, the validation will fail, and you will get the following three `ConstraintViolation` s:

```
* "email" must not be blank
* "email" must be a valid email address
* "phoneNumber" must match [0-9\-]+
```

The program below combines two failing validations and prints every accumulated violation, then combines two successful validations into a `ContactInfo`. Press Run to compile and execute it in your browser; the output appears below the cell.

```java runnable
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Validations;

public class Main {

    record Email(String value) {
    }

    record PhoneNumber(String value) {
    }

    record ContactInfo(Email email, PhoneNumber phoneNumber) {
    }

    static final Validator<Email> emailValidator = ValidatorBuilder.<Email>of()
        .constraint(Email::value, "email", c -> c.notBlank().email())
        .build();

    static final Validator<PhoneNumber> phoneNumberValidator = ValidatorBuilder.<PhoneNumber>of()
        .constraint(PhoneNumber::value, "phoneNumber", c -> c.notBlank().pattern("[0-9\\-]+"))
        .build();

    public static void main(String[] args) {
        Validated<Email> email = emailValidator.applicative().validate(new Email(" "));
        Validated<PhoneNumber> phoneNumber = phoneNumberValidator.applicative()
            .validate(new PhoneNumber("a"));
        Validated<ContactInfo> invalid = Validations.combine(email, phoneNumber)
            .apply(ContactInfo::new);
        System.out.println("valid: " + invalid.isValid());
        System.out.println("violations: " + invalid.errors().size());
        invalid.errors().forEach(v -> System.out.println("- " + v.message()));

        Validated<ContactInfo> valid = Validations
            .combine(emailValidator.applicative().validate(new Email("yavi@example.com")),
                    phoneNumberValidator.applicative().validate(new PhoneNumber("090-123-4567")))
            .apply(ContactInfo::new);
        System.out.println("contact: " + valid.value().email().value() + " / "
                + valid.value().phoneNumber().value());
    }
}
```
<!-- run: expected output
valid: false
violations: 3
- "email" must not be blank
- "email" must be a valid email address
- "phoneNumber" must match [0-9\-]+
contact: yavi@example.com / 090-123-4567
-->

> **Tip**
>
> Validation for `ContactInfo` in the above example can be achieved by using `nest` method as follows:
>
> ```java
> Validator<Email> emailValidator = ValidatorBuilder.<Email> of()
>     .constraint(Email::value, "value", c -> c.notBlank().email())
>     .build();
> Validator<PhoneNumber> phoneNumberValidator = ValidatorBuilder.<PhoneNumber> of()
>     .constraint(PhoneNumber::value, "value", c -> c.notBlank().pattern("[0-9\\-]+"))
>     .build();
>
> Validator<ContactInfo> validator = ValidatorBuilder.<ContactInfo> of()
>     .nest(ContactInfo::email, "email", emailValidator)
>     .nest(ContactInfo::phoneNumber, "phoneNumber", phoneNumberValidator)
>     .build();
> ```
>
> Alternatively, you can combine `ApplicativeValidator<Email>` and `ApplicativeValidator<PhoneNumber>` as introduced in [Combining validators](combining-validators.md).
>
> ```java
> Arguments2Validator<Email, PhoneNumber, ContactInfo> contactInfoValidator = ArgumentsValidators
>     .split(emailValidator.applicative(), phoneNumberValidator.applicative())
>     .apply(ContactInfo::new);
>
> Validated<ContactInfo> contactInfoValidated = contactInfoValidator.validate(new Email("yavi@example.com"), new PhoneNumber("090-123-4567"));
> ```
