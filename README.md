## YAVI (*Y*et *A*nother *V*al*I*dation)

[![Apache 2.0](https://img.shields.io/github/license/making/yavi.svg)](https://www.apache.org/licenses/LICENSE-2.0) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/am.ik.yavi/yavi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/am.ik.yavi/yavi) [![Javadocs](https://www.javadoc.io/badge/am.ik.yavi/yavi.svg)](https://www.javadoc.io/doc/am.ik.yavi/yavi) [![Actions Status](https://github.com/making/yavi/workflows/CI/badge.svg)](https://github.com/making/yavi/actions) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/4232a408001840e994e707ff6a7d4e04)](https://app.codacy.com/manual/making/yavi?utm_source=github.com&utm_medium=referral&utm_content=making/yavi&utm_campaign=Badge_Grade_Dashboard) [![Codacy Badge](https://api.codacy.com/project/badge/Coverage/8ae26f1168ad479ebea8f1f6e6329ac5)](https://www.codacy.com/manual/making/yavi?utm_source=github.com&utm_medium=referral&utm_content=making/yavi&utm_campaign=Badge_Coverage)


![YAVI Logo](https://user-images.githubusercontent.com/106908/120071055-66770380-c0c8-11eb-83f1-d7eff04bad54.png)

YAVI (pronounced jɑ-vάɪ) 
is a lambda based type safe validation for Java. 

### Why YAVI?

YAVI sounds as same as a Japanese slang "YABAI" that means awesome or awful depending on the context.

The concepts are

* No more reflection!
* No more (runtime) annotation!
* No more Java Beans!
* Zero dependency!

If you are not a fan of [Bean Validation](https://beanvalidation.org/), YAVI will be an awesome alternative.

For the migration from Bean Validation, refer [the doc](docs/FromBeanValidationToYAVI.md).

### Usage

Add the following dependency in your `pom.xml`

```xml
<dependency>
    <groupId>am.ik.yavi</groupId>
    <artifactId>yavi</artifactId>
    <version>0.7.0</version>
</dependency>
```

If you want to try a snapshot version, add the following repository:

```xml
<repository>
    <id>sonatype-snapshots</id>
    <name>Sonatype Snapshots</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
### Example

#### Simple

```java
Validator<User> validator = ValidatorBuilder.<User> of() // or ValidatorBuilder.of(User.class)
    .constraint(User::getName, "name", c -> c.notNull() //
        .lessThanOrEqual(20)) //
    .constraint(User::getEmail, "email", c -> c.notNull() //
        .greaterThanOrEqual(5) //
        .lessThanOrEqual(50) //
        .email()) //
    .constraint(User::getAge, "age", c -> c.notNull() //
        .greaterThanOrEqual(0) //
        .lessThanOrEqual(200))
    .build();

ConstraintViolations violations = validator.validate(user);
violations.isValid(); // true or false
violations.forEach(x -> System.out.println(x.message()));
```

[sample code](src/test/java/am/ik/yavi/core/ValidatorTest.java)

In order to avoid ambiguous type inferences, you can use explicit `_<type>` method per type instead of `constraint` as follows:

```java
// Needs YAVI 0.3.0+

Validator<User> validator = ValidatorBuilder.<User> of() // or ValidatorBuilder.of(User.class)
    ._string(User::getName, "name", c -> c.notNull() //
        .lessThanOrEqual(20)) //
    ._string(User::getEmail, "email", c -> c.notNull() //
        .greaterThanOrEqual(5) //
        .lessThanOrEqual(50) //
        .email()) //
    ._integer(User::getAge, "age", c -> c.notNull() //
        .greaterThanOrEqual(0) //
        .lessThanOrEqual(200))
    .build();
```

Does specifying `"fieldName"`s look redundant?

If you want to write as following, try [Annotation Processor](docs/AnnotationProcessor.md).

```java
// Needs YAVI 0.4.0+

Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(_UserMeta.NAME, c -> c.notNull()
        .lessThanOrEqual(20))
    .constraint(_UserMeta.EMAIL, c -> c.notNull()
        .greaterThanOrEqual(5)
        .lessThanOrEqual(50)
        .email())
    .constraint(_UserMeta.AGE, c -> c.notNull()
        .greaterThanOrEqual(0)
        .lessThanOrEqual(200))
    .build();
```

If you are using Kotlin, you can write a bit shorter using `konstraint` method instead of `constraint`

```kotlin
val validator: Validator<User> = ValidatorBuilder.of<User>()
    .konstraint(User::name) {
        notNull() //
            .lessThanOrEqual(20)
    } //
    .konstraint(User::email) {
        notNull() //
            .greaterThanOrEqual(5) //
            .lessThanOrEqual(50) //
            .email()
    } //
    .konstraint(User::age) {
        notNull() //
            .greaterThanOrEqual(0) //
            .lessThanOrEqual(200)
    }
    .build()
```

#### Nested

```java
Validator<Country> countryValidator = ValidatorBuilder.<Country> of() //
    .nest(Country::getName, "name", c -> c.notBlank() //
        .lessThanOrEqual(20))
    .build();
Validator<City> cityValidator = ValidatorBuilder.<City> of() //
    .nest(City::getName, "name", c -> c.notBlank() //
        .lessThanOrEqual(100))
    .build();

Validator<Address> validator = ValidatorBuilder.<Address> of() //
    .nest(Address::getCountry, "country", countryValidator) //
    .nest(Address::getCity, "city", cityValidator)
    .build();
```

[sample code](src/test/java/am/ik/yavi/core/NestedValidatorTest.java)

or

```java
Validator<Address> validator = ValidatorBuilder.<Address> of() //
      .nest(Address::getCountry, "country", //
            b -> b.constraint(Country::getName, "name", c -> c.notBlank() //
                                    .lessThanOrEqual(20))) //
      .nest(Address::getCity, "city", //
            b -> b.constraint(City::getName, "name", c -> c.notBlank() //
                                    .lessThanOrEqual(100))) //
      .build();
```

[sample code](src/test/java/am/ik/yavi/core/InlineNestedValidatorTest.java)

Does specifying `"fieldName"`s look redundant?

If you want to write as following, try [Annotation Processor](docs/AnnotationProcessor.md).

```java
Validator<Address> validator = ValidatorBuilder.<Address> of()
    .nest(_AddressMeta.COUNTRY, countryValidator)
    .nest(_AddressMeta.CITY, cityValidator)
    .build();

// or

Validator<Address> validator = ValidatorBuilder.<Address> of()
      .nest(_AddressMeta.COUNTRY,
            b -> b.constraint(_Address_CountryMeta.NAME, c -> c.notBlank()
                                    .lessThanOrEqual(20)))
      .nest(_AddressMeta.CITY,
            b -> b.constraint(_Address_CityMeta.NAME, c -> c.notBlank()
                                    .lessThanOrEqual(100)))
      .build();
```

[sample code](https://github.com/making/yavi/tree/develop/src/test/java/am/ik/yavi/meta)

#### Elements in a List / Map / Array

* [sample code (List)](src/test/java/am/ik/yavi/core/CollectionValidatorTest.java)
* [sample code (Map)](src/test/java/am/ik/yavi/core/MapValidatorTest.java)
* [sample code (Array)](src/test/java/am/ik/yavi/core/ArrayValidatorTest.java)

#### Overriding violation messages

```java
Validator<User> validator = ValidatorBuilder.<User> of() //
    .constraint(User::getName, "name", c -> c.notNull().message("name is required!") //
        .greaterThanOrEqual(1).message("name is too small!") //
        .lessThanOrEqual(20).message("name is too large!")) //
    .build()
```

#### Custom

```java
public enum IsbnConstraint implements CustomConstraint<String> {
    SINGLETON;

    @Override
    public boolean test(String s) {
        return ISBNValidator.isISBN13(s);
    }

    @Override
    public String messageKey() {
        return "custom.isbn13";
    }

    @Override
    public String defaultMessageFormat() {
        return "\"{0}\" must be ISBN13 format";
    }
}
```

```java
Validator<Book> book = ValidatorBuilder.<Book> of() //
    .constraint(Book::getTitle, "title", c -> c.notBlank() //
        .lessThanOrEqual(64)) //
    .constraint(Book::getIsbn, "isbn", c -> c.notBlank()//
        .predicate(IsbnConstraint.SINGLETON))
    .build(); //
```

[sample code](src/test/java/am/ik/yavi/core/CustomValidatorTest.java)

#### Multi-fields validation

```java
Validator<Range> validator = ValidatorBuilder.<Range> of() //
    .constraint(range::getFrom, "from", c -> c.greaterThan(0)) //
    .constraint(range::getTo, "to", c -> c.greaterThan(0)) //
    .constraintOnTarget(range -> range.to > range.from, "to", "to.isGreaterThanFrom", "\"to\" must be greater than \"from\".") //
    .build();
```

#### Validation using Applicative Functor

Since 0.6.0, YAVI supports a functional programming concept known as [Applicative Functor](https://en.wikipedia.org/wiki/Applicative_functor).
A sequence of validations are executed while accumulating the results (`ConstraintViolation`), even if some or all of these validations fail during the execution chain.

It will be helpful when you wanto combine validations of multiple value objects to produce a new object.

`Validation<E, T>` class is the implementation of Applicative Functor. It can be obtained by `ApplicativeValidator` that can be converted from `Validator` by `applicative()` method.

`Validated<T>` is a shortcut of `Validation<ConstraintViolation, T>` which is specialized for `Validator`'s usage. 

```java
Validator<Email> emailValidator = ValidatorBuilder.<Email> of()
            .constraint(Email::value, "value", c -> c.notBlank().email())
            .build();
Validator<PhoneNumber> phoneNumberValidator = ValidatorBuilder.<PhoneNumber> of()
            .constraint(PhoneNumber::value, "value", c -> c.notBlank().pattern("[0-9\-]+"))
            .build();

Validated<Email> emailValidated = emailValidator.applicative().validate(email);
Validated<PhoneNumber> phoneNumberValidated = phoneNumberValidator.applicative().validate(phoneNumber);

Validated<ContactInfo> contactInfoValidated = emailValidated.combine(phoneNumberValidated)
            .apply((em, ph) -> new ContactInfo(em, ph));
// or
Validated<ContactInfo> contactInfoValidated = Validations.combine(emailValidated, phoneNumberValidation)
		.apply((em, ph) -> new ContactInfo(em, ph));

//
if (contactInfoValidated.isValid()) {
    ContactInfo contactInfo = contactInfoValidated.value();	
} else {
    ConstraintViolations violations = contactInfoValidated.errors();
}

ContactInfo contactInfo = contactInfoValidated
        		.orElseThrow(violations -> new ConstraintViolationsException(violation));

HttpStatus status = contactInfoValidated
        		.fold(violations -> HttpStatus.BAD_REQUEST, contactInfo -> HttpStatus.OK);
```

if you want to add a prefix to the name of `ContraintViolation` (e.g. `email.value` rather than `value`), you can use `prefixed()` of `Validator` as follows.

```java
Validated<Email> emailValidated = emailValidator.prefixed("email").applicative().validate(email);
Validated<PhoneNumber> phoneNumberValidated = phoneNumberValidator.prefixed("phoneNumber").applicative().validate(phoneNumber);
```

[Validation API](src/main/java/am/ik/yavi/fn/Validation.java)

#### Either API

> Since YAVI 0.6.0, Applicative Functor is the preferred method to Either

```java
Either<ConstraintViolations, User> either = validator.either().validate(user);
// prior to YAVI 0.6.0
// Either<ConstraintViolations, User> either = validator.validateToEither(user);

Optional<ConstraintViolations> violations = either.left();
Optional<User> user = either.right();

HttpStatus status = either.fold(v -> HttpStatus.BAD_REQUEST, u -> HttpStatus.OK);
```

`Validation` <-> `Either` can be converted as follows:

```java
// Either -> Validation
Either<ConstraintViolations, User> either = ...
Validation<ConstraintViolation, User> validation = Validation.fromEither(either);

// Validation -> Either
Either<List<ConstraintViolation>, User> either = validation.toEither();
```

[Either API](src/main/java/am/ik/yavi/fn/Either.java)

#### Conditional Constraint

You can impose a condition on constraints with [`ConstraintCondition`](src/main/java/am/ik/yavi/core/ConstraintCondition.java) interface:

```java
Validator<User> validator = ValidatorBuilder.of(User.class) //
    .constraintOnCondition((user, constraintGroup) -> !user.getName().isEmpty(), //
        b -> b.constraint(User::getEmail, "email",
            c -> c.email().notEmpty())) // <- this constraint on email is active only when name is not empty
    .build();
```

You can group the constraint as a part of `ConstraintCondition` with [`ConstraintGroup`](src/main/java/am/ik/yavi/core/ConstraintGroup.java) aas well:

```java
enum Group implements ConstraintGroup {
    CREATE, UPDATE, DELETE
}

Validator<User> validator = ValidatorBuilder.of(User.class) //
    .constraintOnCondition(Group.UPDATE.toCondition(), //
        b -> b.constraint(User::getEmail, "email", c -> c.email().notEmpty()))
    .build();
```

The group to validate is specified in `validate` method:

```java
validator.validate(user, Group.UPDATE);
```

You can also use a shortcut `constraintOnGroup` method

```java
Validator<User> validator = ValidatorBuilder.of(User.class) //
    .constraintOnGroup(Group.UPDATE, //
        b -> b.constraint(User::getEmail, "email", c -> c.email().notEmpty()))
    .build();
```

Note that all constraints without conditions will be validated for any constraint group.

#### Arguments Validator

Since YAVI 0.3.0, you can validate arguments of a constructor or factory method "before" creating an object using it.

##### Validating Constructor Arguments

You can get the object only if the arguments pass the validation.
Up to 16 arguments are supported.

```java
Person person = ArgumentsValidatorBuilder
    .of(Person::new)
    .builder(b -> b
        ._string(Arguments1::arg1, "firstName",
            c -> c.greaterThanOrEqual(1).lessThanOrEqual(50))
        ._string(Arguments2::arg2, "lastName",
            c -> c.greaterThanOrEqual(1).lessThanOrEqual(50))
        ._integer(Arguments3::arg3, "age",
            c -> c.greaterThanOrEqual(20).lessThanOrEqual(99)))
    .build()
    .validated("John", "Doe", 35);
```

You can also get the result as `Validated`.

```java
Validated<Person> validatedPerson  = ArgumentsValidatorBuilder
    .of(Person::new)
    .builder(b -> b
        ._string(Arguments1::arg1, "firstName",
            c -> c.greaterThanOrEqual(1).lessThanOrEqual(50))
        ._string(Arguments2::arg2, "lastName",
            c -> c.greaterThanOrEqual(1).lessThanOrEqual(50))
        ._integer(Arguments3::arg3, "age",
            c -> c.greaterThanOrEqual(20).lessThanOrEqual(99)))
    .build()
    .validateArgs("John", "Doe", 35);
```

Kotlin version

```kotlin
val person = ArgumentsValidatorBuilder
    .of(::Person)
    .builder { b ->
        b._string({ it.arg1() }, "firstName") {
            it.greaterThanOrEqual(1).lessThanOrEqual(50)
        }._string({ it.arg2() }, "lastName") {
            it.greaterThanOrEqual(1).lessThanOrEqual(50)
        }._integer({ it.arg3() }, "age") {
            it.greaterThanOrEqual(20).lessThanOrEqual(99)
        }
    }
    .build()
    .validate("John", "Doe", 20)
```

Does specifying `"fieldName"`s look redundant? Try [Annotation Processor](docs/AnnotationProcessor.md#constraintarguments).

##### Convenient wrappers for single argument validators

For single argument constructors or methods, convenient wrappers are supported since 0.7.0

```java
StringValidator<String> nameValidator = StringValidatorBuilder
    .of("name", c -> c.notBlank())
    .build();  // -> StringValidator<String> extends Arguments1Validator<String, String>

StringValidator<String> emailValidator = StringValidatorBuilder
    .of("email", c -> c.notBlank().email())
    .build();  // -> StringValidator<String> extends Arguments1Validator<String, String>

IntegerValidator<Age> ageValidator = IntegerValidatorBuilder
    .of("age", c -> c.greaterThanOrEqual(0))
    .build(); // -> IntegerValidator<Integer> extends Arguments1Validator<Integer, Integer>

Validated<String> nameValidated = nameValidator.validate("Jone Doe");
Validated<String> emailValidated = nameValidator.validate("jdoe@example.com");
Validated<Integer> ageValidated = nameValidator.validate(30);
```

These validators can use `andThen` method to create another object with the validated values.

```java
public class Name {
    public Name(String value) { /* ... */ }
    // ...
}

public class Email {
    public Email(String value) { /* ... */ }
    // ...
}

public class Age {
    public Age(Integer value) { /* ... */ }
    // ...
}

StringValidator<Name> nameValidator = StringValidatorBuilder
    .of("name", c -> c.notBlank())
    .build()
    .andThen(Name::new); // -> StringValidator<Name> extends Arguments1Validator<String, Name>

StringValidator<Email> emailValidator = StringValidatorBuilder
    .of("email", c -> c.notBlank().email())
    .build()
    .andThen(Email::new); // -> StringValidator<Email> extends Arguments1Validator<String, Email>

IntegerValidator<Age> ageValidator = IntegerValidatorBuilder
    .of("age", c -> c.greaterThanOrEqual(0))
    .build()
    .andThen(Age::new) // -> IntegerValidator<Age> extends Arguments1Validator<Integer, Age>

Validated<Name> nameValidated = nameValidator.validate("Jone Doe");
Validated<Email> emailValidated = nameValidator.validate("jdoe@example.com");
Validated<Age> ageValidated = nameValidator.validate(30);
```

You can use `compose` method to produce a validator that gets a value from the object from which a String or Integer is derived.

```java
// name, email and age are derived from HttpServletRequest
Argument1Validator<HttpServletRequest, Name> requestNameValidator = nameValidator
    .compose(req -> req.getParameter("name"));
Argument1Validator<HttpServletRequest, Email> requestEmailValidator = emailValidator
    .compose(req -> req.getParameter("email"));
Argument1Validator<HttpServletRequest, Age> requestAgeValidator = ageValidator
    .compose(req -> Integer.valueOf(req.getParameter("age")));

HttpServletRequest request = ...;
Validated<Name> nameValidated = requestNameValidator.validate(request);
Validated<Email> emailValidated = requestEmailValidator.validate(request);
Validated<Age> ageValidated = requestAgeValidator.validate(request);
```

You can also validate against a List like bellow:

```java
Arguments1Validator<Iterable<String>, List<Email>> emailsValidator = ArgumentsValidators.liftList(emailValidator);
Validated<List<Email>> validatedEmails = emailsValidator.validate(List.of("foo@example.com", "bar@example.com"));
```

##### Small Validator combination

Since YAVI 0.7.0, small arguments validators (for String, Integer, Value Object etc) can be composed to create a large (Object) validator.

You can use `split` method to split the constructor and the arguments of the factory method for creating a large Object into smaller validators and combine them.

```java
public class Person {
    public Person(Name name, Email email, Age age) { /* ... */ }
    // ...
}

Arguments3Validator<String, String, Integer, Person> personValidator = ArgumentsValidators
    .split(nameValidator, emailValidator, ageValidator)
    .apply(Person::new);
// or
Arguments3Validator<String, String, Integer, Person> personValidator = nameValidator
    .split(emailValidator)
    .split(ageValidator)
    .apply(Person::new);

Validated<Person> personValidated = personValidator.validate("Jone Doe", "jdoe@example.com", 30);
```

You can use `combine` method to produce a validator for an object created by combining objects created from the same source object.

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
Validated<Person> personValidated = personValidator.validate("Jone Doe", "jdoe@example.com", 30);
```
Alternatively, you can create it like this:

```java
Arguments1Validator<HttpServletRequest, Person> requestPersonValidator = personValidator
    .compose(req -> Arguments.of(req.getParameter("name"), req.getParameter("email"), Integer.valueOf(req.getParameter("age")));)
```

##### Validating Method Arguments

Arguments Validator can be used for validating method arguments as well.

```java
// From https://beanvalidation.org/
public class UserService {
  public User createUser(/* @Email */ String email,
                         /* @NotNull */ String name) {
    // ...
  }
}
```

```java
static final Arguments3Validator<UserService, String, String, User> validator = ArgumentsValidatorBuilder
        .of(UserService::createUser) //
        .builder(b -> b //
                ._object(Arguments1::arg1, "userService", c -> c.notNull())
                ._string(Arguments2::arg2, "email", c -> c.email())
                ._string(Arguments3::arg3, "name", c -> c.notNull())) //
        .build();

UserService userService = new UserService();
Validated<User> userValidated = validator.validate(userService, "jdoe@example.com", "John Doe");
```

Note that `void` cannot be used as return type while `java.lang.Void` is available.

Does specifying `"fieldName"`s look redundant? Try [Annotation Processor](docs/AnnotationProcessor.md#constraintarguments).

#### (Experimental) Emoji support

By default, some Emojis are not counted as you expect.

For example,

```java
Validator<Message> validator = ValidatorBuilder.<Message> of() //
      .constraint(Message::getText, "text", c -> c.notBlank() //
          .lessThanOrEqual(3)) //
      .build(); //
validator.validate(new Message("I❤️☕️")).isValid(); // false
```

If you want to count as you see (3, in this case), use `emoji()`.

```java
Validator<Message> validator = ValidatorBuilder.<Message> of() //
      .constraint(Message::getText, "text", c -> c.notBlank() //
          .emoji().lessThanOrEqual(3)) //
      .build(); //
validator.validate(new Message("I❤️☕️")).isValid(); // true
```

For the safety (such as storing into a database), you can also check the size as byte arrays

```java
Validator<Message> validator = ValidatorBuilder.<Message> of() //
      .constraint(Message::getText, "text", c -> c.notBlank() //
          .emoji().lessThanOrEqual(3)
          .asByteArray().lessThanOrEqual(16)) //
      .build(); //
validator.validate(new Message("I❤️☕️")).isValid(); // true
validator.validate(new Message("❤️️❤️️❤️️")).isValid(); // false
```

#### Example with Spring WebFlux.fn

YAVI will be a great fit for [Spring WebFlux.fn](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-fn)

```java
static RouterFunction<ServerResponse> routes() {
  return route()
      .POST("/", req -> req.bodyToMono(User.class) //
        .flatMap(body -> validator.either().validate(body) //
          .leftMap(violations -> {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("message", "Invalid request body");
            error.put("details", violations.details());
            return error;
          })
          .fold(error -> badRequest().bodyValue(error), //
              user -> ok().bodyValue(user))))
      .build();
}
```

[sample app](https://github.com/making/minimal-webflux)

#### Example with Spring MVC

```java
@PostMapping("users")
public String createUser(Model model, UserForm userForm, BindingResult bindingResult) {
  ConstraintViolations violations = validator.validate(userForm);
  if (!violations.isValid()) {
    violations.apply(BindingResult::rejectValue);
    return "userForm";
  }
  // ...
  return "redirect:/";
}
```

or 

```java
@PostMapping("users")
public String createUser(Model model, UserForm userForm, BindingResult bindingResult) {
  return validator.either().validate(userForm)
    .fold(violations -> {
      violations.apply(BindingResult::rejectValue);
      return "userForm";
    }, form -> {
      // ...
      return "redirect:/";
    });
}
```

[sample app](https://github.com/making/demo-spring-mvc-yavi)

#### Validator Factory

If you want to customize `ValidatorBuilder` and manage it with an IoC Container like Spring Framework, you can use `ValidatorFactory` since 0.5.0

```java
@Bean
public ValidatorFactory yaviValidatorFactory(MessageSource messageSource) {
  MessageFormatter messageFormatter = new MessageSourceMessageFormatter(messageSource::getMessage);
  return new ValidatorFactory(null, messageFormatter);
}
```

The usage of a `Validator` would look like following:

```java
@RestController
public class OrderController {
  private final Validator<CartItem> validator;

  public OrderController(ValidatorFactory factory) {
    this.validator = factory.validator(builder -> builder.constraint(...));
  }
}
```

#### BiValidator

`BiValidator<T, E>` is a `BiConsumer<T, E>`. `T` means the type of target object as usual and `E` means the type of errors object. 

This class is helpful for libraries or apps to adapt both YAVI and other validation framework that accepts these two arguments like Spring Framework's `org.springframework.validation.Validator#validate(Object, Errors)`.

`BiValidator` can be obtained as below

```java
BiValidator<CartItem, Errors> validator = ValidatorBuilder.<CartItem>of()
	     .constraint(...)
	     .build(Errors::rejectValue);
```

There is a factory for `BiValidator` as well

```java
@Bean
public BiValidatorFactory<Errors> biValidatorFactory() {
  return new BiValidatorFactory<>(Errors::rejectValues);
}
```
or, if you want to customize the builder

```java
@Bean
public BiValidatorFactory<Errors> biValidatorFactory(MessageSource messageSource) {
  MessageFormatter messageFormatter = new MessageSourceMessageFormatter(messageSource::getMessage);
  return new BiValidatorFactory<>(null, messageFormatter, Errors::rejectValues);
}
```

The usage of a `BiValidator` would look like following:

```java
@RestController
public class OrderController {
  private final BiValidator<CartItem, Errors> validator;

  public OrderController(BiValidatorFactory<Errors> factory) {
    this.validator = factory.validator(builder -> builder.constraint(...));
  }
}
```

### Required

* Java 8+

### License

Licensed under the Apache License, Version 2.0.
