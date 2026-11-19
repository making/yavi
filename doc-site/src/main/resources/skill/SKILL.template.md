---
name: yavi
description: Write Java validation with YAVI 0.18, whose schema API (Yavi.schema() with fromMap/apply) reads untyped input such as a Map, a JSON tree or request parameters and creates a typed object, in the style of Zod. Covers the schema style, the classic ValidatorBuilder style, input readers, Validated results and violations, Spring, Jackson, Kotlin and NullAway integration, and the built-in constraints. Use this whenever a task involves YAVI, Yavi.schema(), ValidatorBuilder, ObjectInputValidator, ConstraintViolations, or validating input in Java.
---

# YAVI

YAVI (Yet Another Validation) is a Java validation library. Version 0.18 adds a schema
API that reads untyped input and creates the object, so that an invalid object never
exists. The classic `ValidatorBuilder` API, which checks an object created beforehand,
is still supported and is the right choice only for that case.

```xml
<dependency>
    <groupId>am.ik.yavi</groupId>
    <artifactId>yavi</artifactId>
    <version>{{yavi-release}}</version>
</dependency>
```

- Java 21 or later. YAVI is annotated with JSpecify, and the annotations are `provided`:
  nothing extra is needed at run time.
- This skill was built from the {{docs-version}} documentation,
  {{site-base}}/{{docs-version}}/{{lang}}/ ; the pages are bundled verbatim under
  `references/`, indexed by `references/{{contents-page}}`.
- The browser runtime that the site uses to run the examples is not part of the skill;
  compile the code in a normal Maven or Gradle project.

## Rules

1. Prefer the schema API (`Yavi.schema()` ... `fromMap(f)` or `apply(f)`) whenever the
   input is not the object yet: a `Map`, a JSON document, request parameters, form
   fields, path variables. Use `ValidatorBuilder` only when the object already exists,
   as with Bean Validation.
2. A schema in progress is not a validator. It ends with `fromMap(f)`, which returns an
   `ObjectInputValidator<X>`, or with `apply(f)`, which returns an `ArgumentsNValidator`.
   The builder is immutable, so each call returns a new stage.
3. A property is required unless `nullable()`, `optional()`, `defaultValue(v)` or
   `recover(v)` says otherwise. An absent key and an explicit `null` are violations with
   `object.required` and `object.notNull`; do not add `notNull()` for that, and the
   function given to `fromMap` is never passed a `null` for a required property.
4. Handle a result with an exhaustive `switch` over the sealed `Validated`:
   `case Validated.Valid<X>(X x) -> ...` and
   `case Validated.Invalid<X>(ConstraintViolations violations) -> ...`. `validate` is the
   `safeParse` of a Zod-style API; the name is kept from 0.17 for backward
   compatibility, and there is no `parse` method. Where every handler of an API maps a
   failure the same way, `validated(...)` throws `ConstraintViolationsException` and one
   `@RestControllerAdvice` handles it; a switch in each handler repeats the response
   mapping.
5. Constraints are methods of the constraint object of the property type. A constraint
   of another type does not compile, so a guess surfaces at build time; check
   `references/built-in-constraints.md` before using a name, and do not invent one.
   `email()` and the other form constraints accept the empty string unless `notBlank()`
   or `notEmpty()` is added.
6. YAVI depends on neither Jackson nor Spring. A JSON tree is read through an
   `InputReader` written once per tree model (`references/integrations-jackson.md`); the
   Spring, Kotlin and NullAway pages are adapters and checks on top of the library.
7. In Kotlin, `am.ik.yavi.builder._enum<T>` and `_object<T>` are reified extensions that
   remove the `Class` argument. A schema that ends with `apply` takes a method reference
   (`apply(::Account)`), not a lambda.
8. The classic API (`ValidatorBuilder`) treats a `null` property as valid unless a
   constraint rejects it, and `validate` on it returns `ConstraintViolations` rather
   than a `Validated`. Do not mix the two models in one validation path.
9. Call `rejectingUnknownProperties()` on the body and the query of an API, so a typo in
   a key is a violation instead of being silently ignored. `readingParameters()` is
   written after it. A form with a checkbox is the exception (Thymeleaf adds
   `_subscribed`).
10. A path variable is text. Take it as a `String` and validate it with the domain's own
    validator rather than a Spring type conversion, so an invalid `{id}` produces the
    same violation shape as a malformed body.
11. A schema that ends in `fromMapReversible` writes the object back as the map of an SQL
    statement; see "A JSON API and an SQL statement". A value has a text form and a Java
    form, so the map validator reads the text and a validator that ends in `apply(f)`
    reads the Java value. Share the single-value validators, keep the two endings, and do
    not convert a value you already hold to text only to reuse the text validator.

## Quickstart

The documentation page below is bundled as it is; it compiles as written, and its
remaining examples run in the site's browser runtime.

{{include:getting-started.md}}

## A complete Spring MVC controller

This is the shape a YAVI application has at a request boundary. The domain class holds
the `ObjectInputValidator` of its own input, in its own file -- a validator belongs to
the domain, not to a controller, and is reused by a job or a test. The controller is
where that validator is bound to request parameters with `readingParameters`, and where
the result reaches the `BindingResult`. The individual pieces are documented under
`references/integrations-spring.md` and `references/input-parameters.md`.

```java
// signup/Signup.java
import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.validator.Yavi;

public record Signup(String name, String email, Integer age) {

    public static final ObjectInputValidator<Signup> SCHEMA = Yavi.schema()
            ._string("name", c -> c.notBlank().lessThanOrEqual(50))
            ._string("email", c -> c.notBlank().email())
            ._integer("age", c -> c.greaterThanOrEqual(18).lessThanOrEqual(120))
            .fromMap(Signup::new);

}
```

```java
// web/SignupController.java
import java.util.Map;

import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.StringCoercion;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.input.ParameterTree;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.util.MultiValueMap;

@Controller
class SignupController {

    // Signup is a domain type in its own file, and SignupService is another. The same
    // domain schema serves any request that carries the same input; the coercion of the
    // text of a form is the controller's decision.
    static final ValueValidator<Map<String, ?>, Signup> signupValidator = Signup.SCHEMA
            .readingParameters(StringCoercion.strict().trimming());

    private final SignupService signupService;

    SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    // Runs before every handler of this controller, so the GET request renders the
    // empty form and a rejected POST renders the values that were sent.
    @ModelAttribute
    void signupForm(@RequestParam MultiValueMap<String, String> params, Model model) {
        Map<String, Object> form = ParameterTree.of(params).toParameters(signupValidator.description());
        model.addAllAttributes(new MapBindingResult(form, "signupForm").getModel());
    }

    @GetMapping("/signup")
    String form() {
        return "signup";
    }

    @PostMapping("/signup")
    String signup(@RequestParam MultiValueMap<String, String> params, BindingResult result) {
        return switch (signupValidator.validate(params)) {
            case Validated.Valid<Signup>(Signup signup) -> {
                this.signupService.register(signup);
                yield "redirect:/";
            }
            case Validated.Invalid<Signup>(ConstraintViolations violations) -> {
                violations.apply(result::rejectValue);
                yield "signup";
            }
        };
    }

}
```

```html
<form method="post" th:action="@{/signup}" th:object="${signupForm}">
    <input type="text" th:field="*{name}">
    <span th:if="${#fields.hasErrors('name')}" th:errors="*{name}"></span>
    <input type="text" th:field="*{email}">
    <span th:if="${#fields.hasErrors('email')}" th:errors="*{email}"></span>
    <input type="text" th:field="*{age}">
    <span th:if="${#fields.hasErrors('age')}" th:errors="*{age}"></span>
    <button type="submit">Sign up</button>
</form>
```

- The `@ModelAttribute` method is not optional: without a model attribute, a handler
  that takes a `BindingResult` fails with `An Errors/BindingResult argument is expected
  to be declared immediately after the model attribute`, because Spring MVC creates a
  `BindingResult` only for a form object that it binds.
- Take the parameters as a `MultiValueMap`; a `Map<String, String>` drops all but the
  first value of a key.
- The code of a field error is the message key (`charSequence.email`, `object.type`), so
  a `MessageSource` replaces the message. See `references/messages.md`.
- Thymeleaf adds a hidden `_subscribed` field to a checkbox. No property is defined for
  that key, so a form with a checkbox cannot use `rejectingUnknownProperties()`.

### One controller per form, or the model setup inside the handler

A `@ModelAttribute` method runs before every handler of its controller, and a
`BindingResult` argument resolves to the last model attribute added -- not to the form the
handler validates. With two forms in one controller, every handler receives the
`BindingResult` of whichever form was added last, and no error is raised (verified on
Spring Framework 7); `@ModelAttribute("signupForm") BindingResult` does not select one
either, because the model attribute is the form map, not the `BindingResult`. Keep one
form per controller, or do what the `@ModelAttribute` method does inside each handler:
build the `MapBindingResult` from the parameters, add `getModel()` to the `Model`, and
take no `BindingResult` argument. Nothing is added before the handler runs then, so one
controller can serve several forms (verified).

```java
@PostMapping("/signup")
String signup(@RequestParam MultiValueMap<String, String> params, Model model) {
    MapBindingResult result = new MapBindingResult(
            ParameterTree.of(params).toParameters(signupValidator.description()), "signupForm");
    return switch (signupValidator.validate(params)) {
        case Validated.Valid<Signup>(Signup signup) -> {
            this.signupService.register(signup);
            yield "redirect:/";
        }
        case Validated.Invalid<Signup>(ConstraintViolations violations) -> {
            violations.apply(result::rejectValue);
            model.addAllAttributes(result.getModel());
            yield "signup";
        }
    };
}
```

A GET handler adds the model of the empty form the same way:
`model.addAllAttributes(new MapBindingResult(Map.of(), "signupForm").getModel())`.

## Where validation belongs

Validation is a boundary operation: it turns untyped input -- a JSON body, request
parameters, a path variable, a form, a row -- into a domain object, and every input of an
API is untrusted. The handler or the reader that receives the request runs the contract;
the service and the repository take the typed value and do not check it again. A value
the application generated itself (an identifier, a timestamp) already has its type, so
checking it adds nothing.

At an API boundary the contract is also the error contract: reject unknown keys (rule 9),
validate a path variable with the domain's validator (rule 10), and map
`ConstraintViolationsException` once (rule 4).

## Reusable single-value validators

A field that appears in several contracts is written once as a single-value validator and
composed into each schema:

```java
public static final ReversibleValueValidator<String, String> AUTHOR = Yavi._string("author",
        c -> c.notBlank().lessThanOrEqual(50));

// read from a map, a form or a row
Yavi.schema()._string(AUTHOR)._string(CONTENT).fromMap(...)

// read a Java value the caller already holds
Yavi.schema()._string(AUTHOR)._string(CONTENT).apply(...)
```

- `named("x")` uses a defined validator under another key and `prefixed(...)` places it
  under a collection element; the constraint is not copied.
- `map(f, g)` gives a value its domain type and keeps the text form for writing back; a
  reversible schema built from it writes the object as the parameters of a statement.
- Share the single-value validators, not the schema endings: the map validator reads the
  text form and the `apply` validator reads the Java value. Do not convert a value you
  already hold to text only to reuse the map validator.
- Reusing a validator keeps the name and the message of the field across the contracts.

## A JSON API and an SQL statement

The same schema carries a value from a JSON request, a row and a statement. The property
name is the named parameter of the statement and the alias of the column; a column whose
name differs (`created_at` for `createdAt`) is selected with an alias, not mapped by hand.

```java
this.jdbcClient.sql("""
        INSERT INTO comments (id, author, content, created_at)
        VALUES (:id, :author, :content, :createdAt)
        """).params(Comment.SCHEMA.toMap(comment)).update();

List<Comment> comments = this.jdbcClient.sql("""
        SELECT id, author, content, created_at AS "createdAt" FROM comments
        """).query().listOfRows().stream().map(Comment.SCHEMA::validated).toList();
```

`toMap` writes the text form of a value whose reversible validator maps it (a `UUID`, an
`Instant`), and an entry with `null` for an absent nullable or optional property, because
a named parameter needs its key. A nested map cannot be bound to a statement: read the
columns with `_embedded`, or flatten them.

For a value the application already holds, the schema ends in `apply(f)` and takes the
Java value (`Arguments4Validator<UUID, String, String, Instant, Comment>`); the map
validator for the same value reads the text form:

```java
public static final ReversibleValueValidator<String, UUID> ID = Yavi._string("id",
        c -> c.notBlank().uuid()).map(UUID::fromString, UUID::toString);
```

To read a body without a record in between, build `Arguments2` with
`fromMap(Arguments2::new)` and call `args.map((first, second) -> ...)`.

At the request boundary, call `validated(...)` and handle the exception once, so the
violations of every endpoint have one shape:

```java
@ExceptionHandler(ConstraintViolationsException.class)
ResponseEntity<ApiError> violations(ConstraintViolationsException exception) {
    ConstraintViolations violations = exception.violations();
    // ...
}
```

## Where to look

| Task | Page |
| --- | --- |
| A schema of a Map or a JSON object | `references/defining-a-schema.md`, `references/input-maps.md` |
| Optional, nullable or default property | `references/optional-and-defaults.md` |
| Nested objects, lists and maps | `references/objects-and-collections.md` |
| A value that is one of several shapes | `references/unions.md` |
| Rules across properties | `references/cross-field-rules.md` |
| A constructor that must not receive raw input | `references/value-objects.md` |
| A JSON tree without copying it | `references/input-json.md` |
| Request parameters or a form | `references/input-parameters.md` |
| The result, the violations, the response | `references/result-handling.md`, `references/violations.md`, `references/messages.md` |
| Writing the validated object back | `references/writing-back.md` |
| Where to validate | this page, "Where validation belongs" |
| Reusing a field validator across schemas | this page, "Reusable single-value validators" |
| A JSON API and SQL parameters | this page, "A JSON API and an SQL statement" |
| Spring, Jackson, Kotlin, NullAway | `references/integrations-*.md` |
| The classic `ValidatorBuilder` API | `references/using-yavi.md` and the rest of the Classic Validators section |
| Every constraint | `references/built-in-constraints.md` |
| Coming from Bean Validation or Zod | `references/coming-from-bean-validation.md`, `references/coming-from-zod.md` |

## The bundled documentation

Every page of the site, at the path it has there.

{{nav-table}}
