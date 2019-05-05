## YAVI (*Y*et *A*nother *V*al*I*dation)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/am.ik.yavi/yavi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/am.ik.yavi/yavi)

![image](https://user-images.githubusercontent.com/106908/45175056-66af8080-b247-11e8-8bc2-ccf34fe77cd7.png)

YAVI (pronounced jɑ-vάɪ) 
is a lambda based type safe validation for Java. 

### Why YAVI?

YAVI sounds as same as a Japanese slang "YABAI" that means awesome or awful depending on the context.

The concepts are

* No more reflection!
* No more annotation!
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
    <version>0.1.0</version>
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

If you are using Kotlin, you can write a bit shorter

```kotlin
import am.ik.yavi.builder.constraint

val validator: Validator<User> = ValidatorBuilder.of<User>()
        .constraint(User::name) {
            notNull() //
                    .lessThanOrEqual(20)
        } //
        .constraint(User::email) {
            notNull() //
                    .greaterThanOrEqual(5) //
                    .lessThanOrEqual(50) //
                    .email()
        } //
        .constraint(User::age) {
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

#### Either API

```java
Either<ConstraintViolations, User> either = validator.validateToEither(user);

Optional<ConstraintViolations> violations = either.left();
Optional<User> user = either.right();

HttpStatus status = either.fold(v -> HttpStatus.BAD_REQUEST, u -> HttpStatus.OK);
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
                .flatMap(body -> validator.validateToEither(body) //
                    .leftMap(violations -> {
                        Map<String, Object> error = new LinkedHashMap<>();
                        error.put("message", "Invalid request body");
                        error.put("details", violations.details());
                        return error;
                    })
                    .fold(error -> badRequest().syncBody(error), //
                          user -> ok().syncBody(user))))
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
    return validator.validateToEither(userForm)
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

#### Example with Spring Fu

```kotlin
package com.example.demospringfuyavi

import am.ik.yavi.builder.ValidatorBuilder
import am.ik.yavi.builder.constraint
import org.springframework.boot.WebApplicationType
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.webflux.webFlux

val app = application(WebApplicationType.REACTIVE) {
    webFlux {
        port = if (profiles.contains("test")) 8181 else 8080
        router {
            POST("/") { req ->
                req.bodyToMono<Message>()
                        .flatMap { message ->
                            Message.validator.validateToEither(message)
                                    .leftMap { mapOf("details" to it.details()) }
                                    .fold(badRequest()::syncBody, ok()::syncBody)
                        }
            }
        }
        codecs {
            jackson()
        }
    }
}

data class Message(
        val text: String
) {
    companion object {
        val validator = ValidatorBuilder.of<Message>()
                .constraint(Message::text, {
                    notBlank().lessThanOrEqual(3)
                })
                .build()
    }
}

fun main() {
    app.run()
}
```

[sample app](https://github.com/making/demo-spring-fu-yavi)


### Example with Ktor

```kotlin
package com.example

import am.ik.yavi.core.Validator
import am.ik.yavi.builder.ValidatorBuilder
import am.ik.yavi.builder.constraintOnCharSequence
import am.ik.yavi.builder.nest
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

data class Snippet(val text: String)
data class PostSnippet(val snippet: PostSnippet.Text) {
    data class Text(val text: String)

    companion object {
        val validator = ValidatorBuilder.of<PostSnippet>()
            .nest(PostSnippet::snippet) {
                constraint(Text::text) {
                    notEmpty().lessThanOrEqual(3)
                }
            }
            .build()
    }
}

val snippets = Collections.synchronizedList(
    mutableListOf(
        Snippet("hello"),
        Snippet("world")
    )
)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/snippets") {
            call.respond(mapOf("snippets" to synchronized(snippets) { snippets.toList() }))
        }
        post("/snippets") {
            val post = call.receive<PostSnippet>()
            val violations = PostSnippet.validator.validate(post)
            if (!violations.isValid) {
                call.respond(HttpStatusCode.BadRequest, mapOf("details" to violations.details()))
            } else {
                snippets += Snippet(post.snippet.text)
                call.respond(mapOf("OK" to true))
            }
        }
    }
}
```

### Required

* Java 8+

### License

Licensed under the Apache License, Version 2.0.
