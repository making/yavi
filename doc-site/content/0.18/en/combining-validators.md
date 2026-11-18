# Combining Validators

A `ValueValidator<S, T>` validates an input of type `S` and produces a value of type `T`. Argument validators and applicative validators are value validators, so they can be split and combined to validate a larger object.

## Splitting a validator

`ArgumentsValidators.split` combines value validators into an arguments validator. Each validator validates one argument of the function that creates the object.

```java
Arguments3Validator<String, String, Integer, Person> validator = ArgumentsValidators
        .split(nameValidator, emailValidator, ageValidator)
        .apply(Person::new);
```

The same combination can be built from the validators one after another:

```java
Arguments3Validator<String, String, Integer, Person> validator = nameValidator
        .split(emailValidator)
        .split(ageValidator)
        .apply(Person::new);
```

The arguments of `split` are `ValueValidator`s, so an `ApplicativeValidator` can be combined in the same way. A value object is produced when the validators map the values:

```java
Arguments2Validator<Email, PhoneNumber, ContactInfo> validator = ArgumentsValidators
        .split(emailValidator, phoneNumberValidator)
        .apply(ContactInfo::new);

Validated<ContactInfo> validated = validator.validate(new Email("yavi@example.com"),
        new PhoneNumber("090-123-4567"));
```

All violations are accumulated. A combination is validated only when every argument is valid, so the function is not called with an invalid value.

## Chaining and mapping value validators

`andThen(ValueValidator)` applies the value produced by one validator to the next validator.

```java
ValueValidator<String, String> checked = emailValidator
        .andThen(ValueValidator.<Email>passThrough().map(Email::value));
```

`map(Function)` maps the validated value without applying another validator.

```java
ValueValidator<String, String> emailValue = emailValidator.map(Email::value);
```

`andThen(Function)` is removed in 0.18. Use `map(Function)` instead. A validator that knows how to write its value back is built with `map(f, g)`, as described in [Writing an Object Back](writing-back.md).

## Lifting a validator over a collection

`liftList`, `liftSet`, `liftCollection(factory)`, `liftMap` and `liftOptional` apply a validator to every element or value of a container. The violations of an element are located under its index or key.

```java
Arguments1Validator<Iterable<? extends String>, List<Email>> emails = emailValidator.liftList();
Validated<List<Email>> validated = emails.validate(List.of("foo@example.com", "bar@example.com"));

Arguments1Validator<Iterable<? extends String>, Set<Email>> emailSet = emailValidator.liftSet();
Arguments1Validator<Iterable<? extends String>, List<Email>> emailList = emailValidator
        .liftCollection(ArrayList::new);
Arguments1Validator<Optional<? extends String>, Optional<Email>> emailOptional = emailValidator
        .liftOptional();
```

In 0.18 the lifted validator takes a container of a subtype, `Iterable<? extends String>`, so a validator of `Object` lifted to a list validates a `List<String>`.

The violations of a lifted element are located under the index, before the name of the element validator:

```java
Yavi._string("tag", c -> c.notBlank())
        .liftList()
        .validate(List.of("java", ""));
// [1].tag, was tag[1] in 0.17
```

Name the collection rather than the element, with `prefixed(String)`:

```java
Yavi._string(c -> c.notBlank())
        .liftList().prefixed("tag")
        .validate(List.of("java", ""));
// tag[1]

addressValidator.applicative().liftList().prefixed("addresses").validate(addresses);
// addresses[1].street
```

`indexed(int)` is removed. `prefixed(int)` of a `ConstraintViolation` or a `Validated` puts the index before the name:

```java
validated.prefixed(1); // [1].tag
```

`liftList(separator)` also reads a list from one text, such as `1,2,3`.

```java
ValueValidator<Iterable<? extends Integer>, List<Integer>> ids = Yavi._integer(c -> c.positive())
        .liftList(Pattern.compile(","));
```

## Validating the source before the target

`compose` validates a value of another type by converting it first. A validator for a request or a map can therefore create a domain object from values that it reads.

```java
Arguments1Validator<HttpServletRequest, Name> requestNameValidator = nameValidator
        .compose(req -> req.getParameter("name"));
Arguments1Validator<HttpServletRequest, Email> requestEmailValidator = emailValidator
        .compose(req -> req.getParameter("email"));
Arguments1Validator<HttpServletRequest, Integer> requestAgeValidator = ageValidator
        .compose(req -> Integer.valueOf(req.getParameter("age")));

Arguments1Validator<HttpServletRequest, Person> personValidator = ArgumentsValidators
        .combine(requestNameValidator, requestEmailValidator, requestAgeValidator)
        .apply(Person::new);

Validated<Person> validated = personValidator.validate(request);
```

A validator of typed arguments can be converted in the same way; `Arguments.of` builds the arguments the target validator expects.

```java
Arguments1Validator<HttpServletRequest, Person> personValidator = argumentsValidator
        .compose(req -> Arguments.of(req.getParameter("name"), req.getParameter("email"),
                Integer.valueOf(req.getParameter("age"))));
```

The source is validated with the same combination rules: every violation is accumulated and the target is created only when the source is valid.
