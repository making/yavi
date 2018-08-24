## YAVI (*Y*et *A*nother *V*al*I*dation)
YAVI (pronounced jɑ-vάɪ) 
is a lambda based type safe validation for Java. 
 

### Why YAVI?

YAVI sounds as same as a Japanese slang "YABAI" that means awesome or awful depending on the context.

The concepts are

* No more reflection!
* No more annotation!
* No more Java Beans!
* Zero dependency!

If you are not a fun of [Bean Validation](https://beanvalidation.org/), YAVI will be an awesome alternative.

### Usage

Add the following dependency in your `pom.xml`

```xml
<dependency>
    <groupId>am.ik.yavi</groupId>
    <artifactId>yavi</artifactId>
    <version>0.0.5</version>
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
Validator<User> validator = Validator.<User> builder() // or Validator.builder(User.class)
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

#### Nested

```java
Validator<Country> countryValidator = Validator.<Country> builder() //
            .constraint(Country::getName, "name", c -> c.notBlank() //
                    .lessThanOrEqual(20))
            .build();
Validator<City> cityValidator = Validator.<City> builder() //
            .constraint(City::getName, "name", c -> c.notBlank() //
                    .lessThanOrEqual(100))
            .build();

Validator<Address> validator = Validator.<Address> builder() //
            .constraint(Address::getCountry, "country", countryValidator) //
            .constraint(Address::getCity, "city", cityValidator)
            .build();
```

[sample code](src/test/java/am/ik/yavi/core/NestedValidatorTest.java)

or

```java
Validator<Address> validator = Validator.<Address> builder() //
            .constraintForObject(Address::getCountry, "country", Constraint::notNull)
            .constraint(Address::getCountry, //
                        b -> b.constraint(Country::getName, "country.name", c -> c.notBlank() //
                                                                        .lessThanOrEqual(20))) //
            .constraintForObject(Address::getCity, "city", Constraint::notNull)
            .constraint(Address::getCity, //
                        b -> b.constraint(City::getName, "city.name", c -> c.notBlank() //
                                                                        .lessThanOrEqual(100))) //
            .build();
```

[sample code](src/test/java/am/ik/yavi/core/InlineNestedValidatorTest.java)

#### Custom

```java
public enum IsbnConstraint implements CustomConstraint<String> {
    SINGLETON;

    @Override
    public Predicate<String> predicate() {
        return ISBNValidator::isISBN13;
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
Validator<Book> book = Validator.<Book> builder() //
            .constraint(Book::getTitle, "title", c -> c.notBlank() //
                    .lessThanOrEqual(64)) //
            .constraint(Book::getIsbn, "isbn", c -> c.notBlank()//
                    .predicate(IsbnConstraint.SINGLETON))
            .build(); //
```

[sample code](src/test/java/am/ik/yavi/core/CustomValidatorTest.java)

#### Either API

```java
Either<ConstraintViolations, User> either = validator.validateToEither(user);

Optional<ConstraintViolations> violations = either.left();
Optional<User> user = either.right();

HttpStatus status = either.fold(v -> HttpStatus.BAD_REQUEST, u -> HttpStatus.OK);
```

[Either API](src/main/java/am/ik/yavi/fn/Either.java)

#### Example with Spring WebFlux.fn

YAVI will be a great fit for [Spring WebFlux.fn](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-fn)

```java
static RouterFunction<ServerResponse> routes() {
    return route(POST("/"), req -> req.bodyToMono(User.class) //
            .flatMap(body -> validator.validateToEither(body) //
                    .fold(violations -> {
                        Map<String, Object> res = new LinkedHashMap<>();
                        res.put("message", "Invalid request body");
                        res.put("details", violations.details());
                        return badRequest().syncBody(res);
                    }, user -> ok().syncBody(user))));
}
```

#### Example with Spring MVC

```java
@PostMapping("users")
public String createUser(Model model, UserForm userForm, BindingResult bindingResult) {
    ConstraintViolations violations = validator.validate(userForm);
    if (!violations.isValid()) {
        violations.forEach(v -> 
            bindingResult.rejectValue(v.name(), v.messageKey(), v.args(), v.message()));
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
    return validator.validateToEither(userForm)
        .fold(violations -> {
            violations.forEach(v -> 
                bindingResult.rejectValue(v.name(), v.messageKey(), v.args(), v.message()));
            return "userForm";
        }, form -> {
            // ...
            return "redirect:/";
        });
}
```

### Required

* Java 8+

### License

Licensed under the Apache License, Version 2.0.
