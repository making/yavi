# Combining Validators

`ApplicativeValidator<T>` and `Arguments1Validator<S, T>` we've seen so far extend `ValueValidator<S, T>`.
(`T` is the type of the target object to be validated and `S` is the type of the source object to create the target object.)

`ApplicativeValidator<T>` is a `ValueValidator<T, T>`.

Multiple `ValueValidator` s can be combined to validate a larger object.

`ArgumentsValidators#split` and `ArgumentsValidators#combine` support up to 16 arguments since 0.10.0.
The `apply` method consumes `Function1` to `Function16` accordingly.

## Splitting a validator for an object into small pieces

You can use `split` method to split a validator for the arguments of a constructor or a factory method for creating a large object into small validators and combine them.

Let's take a look at an example.

Validator for constructor `Person(String, String, Integer)` is `Arguments3Validator<String, String, Integer, Person>`.
This can be divided into two `StringValidator<String>` s and one `IntegerValidator<Integer>` and then combined.

It can be defined as bellow:

```java
public class Person {
    public Person(String name, String email, Integer age) {
        // ...
    }
}

StringValidator<String> nameValidator = /* see examples above */;
StringValidator<String> emailValidator = /* see examples above */;
IntegerValidator<Integer> ageValidator = /* see examples above */;

Arguments3Validator<String, String, Integer, Person> personValidator = ArgumentsValidators
    .split(nameValidator, emailValidator, ageValidator)
    .apply(Person::new);
// or
Arguments3Validator<String, String, Integer, Person> personValidator = nameValidator
    .split(emailValidator)
    .split(ageValidator)
    .apply(Person::new);
```

The same goes for Validators for Value Objects.

```java
public class Person {
    public Person(Name name, Email email, Age age) {
        // ...
    }
}

StringValidator<Name> nameValidator = /* see examples above */;
StringValidator<Email> emailValidator = /* see examples above */;
IntegerValidator<Age> ageValidator = /* see examples above */;

Arguments3Validator<String, String, Integer, Person> personValidator = ArgumentsValidators
    .split(nameValidator, emailValidator, ageValidator)
    .apply(Person::new);
// or
Arguments3Validator<String, String, Integer, Person> personValidator = nameValidator
    .split(emailValidator)
    .split(ageValidator)
    .apply(Person::new);
```

The created Validator can be used as follows:

```java
Validated<Person> personValidated = Person.validator.validate("Jone Doe", "jdoe@example.com", 30);
```

Since the arguments of `ArgumentsValidators#split` are `ValueValidator` s rather than `Arguments1Validator` s, you can also combine `ApplicativeValidator` s as follows:

```java
ApplicativeValidator<Email> emailValidator = /* see examples above */;
ApplicativeValidator<PhoneNumber> phoneNumberValidator = /* see examples above */;

Arguments2Validator<Email, PhoneNumber, ContactInfo> contactInfoValidator = ArgumentsValidators
    .split(emailValidator, phoneNumberValidator)
    .apply(ContactInfo::new);

Validated<ContactInfo> contactInfoValidated = contactInfoValidator.validate(new Email("yavi@example.com"), new PhoneNumber("090-123-4567"));
```

## Chaining and mapping value validators

`ValueValidator#andThen(ValueValidator)` applies the value produced by one validator to the next validator.
`ValueValidator#map(Function)` maps the validated value without applying further validation.
`andThen(Function)` is deprecated since 0.17.0 and is equivalent to `map(Function)`.

```java
Arguments1Validator<String, Email> emailValidator = /* see examples above */;
ValueValidator<String, String> emailValue = emailValidator.map(Email::value);
ValueValidator<String, String> checked = emailValidator
    .andThen(ValueValidator.<Email> passThrough().map(Email::value));
```

## Validating every element of an iterable

`ArgumentsValidators#sequenceN` applies every given validator to the same arguments and collects the validated values into a `List`.
`ArgumentsValidators#traverseN` builds a validator from an iterable of values and a function that produces a validator for each value.
Both are available for 1 to 16 arguments since 0.10.0.

```java
Arguments1Validator<String, Email> emailValidator = /* see examples above */;
Arguments1Validator<String, Email> adminEmailValidator = /* see examples above */;
Arguments1Validator<String, List<Email>> emailsValidator = ArgumentsValidators
    .sequence1(List.of(emailValidator, adminEmailValidator));

Validated<List<Email>> emails = emailsValidator.validate("yavi@example.com");
```

See [Validating arguments](validating-arguments.md) for constructing arguments validators.

## Validating the source object before creating the target object

In a web application, you often get values from a `Map`, Form Object, or `HttpServletRequest` to create a Domain Object.

`Arguments1Validator` can validate the source object and then convert it to the target object using `compose` method.

For example, in the case where `name`, `email` and `age` are obtained from `HttpServletRequest` object as HTTP request parameters in Servlet, it can be defined as follows:

```java
Arguments1Validator<HttpServletRequest, Name> requestNameValidator = nameValidator
    .compose(req -> req.getParameter("name"));
Arguments1Validator<HttpServletRequest, Email> requestEmailValidator = emailValidator
    .compose(req -> req.getParameter("email"));
Arguments1Validator<HttpServletRequest, Age> requestAgeValidator = ageValidator
    .compose(req -> Integer.valueOf(req.getParameter("age")));

HttpServletRequest request = ...;
Validated<Name> nameValidated = requestNameValidator.validate(request);
Validated<Email> emailValidated = requestEmailValidator.validate(request);
Validated<Age> ageValidated = requestAgeValidator.validate(request);
```

You can combine these Validators with `combine` method as follows to create a Validator for `Person` object:

```java
Arguments1Validator<HttpServletRequest, Person> requestPersonValidator = ArgumentsValidators
    .combine(requestNameValidator, requestEmailValidator, requestAgeValidator)
    .apply(Person::new);
// or
Arguments1Validator<HttpServletRequest, Person> requestPersonValidator = requestNameValidator
    .combine(requestEmailValidator)
    .combine(requestAgeValidator)
    .apply(Person::new);

HttpServletRequest request = ...;
Validated<Person> personValidated = requestPersonValidator.validate(request);
```

This Validator can also be converted from `Arguments3Validator<String, String, Integer, Person>` as follows:

```java
Arguments3Validator<String, String, Integer, Person> personValidator = /* see examples above */;
Arguments1Validator<HttpServletRequest, Person> requestPersonValidator = personValidator
    .compose(req -> Arguments.of(req.getParameter("name"), req.getParameter("email"), Integer.valueOf(req.getParameter("age"))));
```

By combining Validators in this way, you can create various patterns of Validators. Creating a small Arguments Validator not only allows you to validate the values before creating the object, but also makes it more reusable.
