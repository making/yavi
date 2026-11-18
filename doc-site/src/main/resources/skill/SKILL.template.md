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
   compatibility, and there is no `parse` method.
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

## Quickstart

The documentation page below is bundled as it is; it compiles as written, and its
remaining examples run in the site's browser runtime.

{{include:getting-started.md}}

## A complete Spring MVC controller

This is the shape a YAVI application has at a request boundary. The domain class and
the `ObjectInputValidator` of its input are files of their own -- a validator belongs to
the domain, not to a controller, and is reused by a job or a test. The controller is
where that validator is bound to request parameters with `readingParameters`, and where
the result reaches the `BindingResult`. The individual pieces are documented under
`references/integrations-spring.md` and `references/input-parameters.md`.

```java
// signup/Signup.java
public record Signup(String name, String email, Integer age) {}
```

```java
// signup/SignupValidator.java -- the input of the domain, next to the domain class
import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.validator.Yavi;

public final class SignupValidator {

    public static final ObjectInputValidator<Signup> INSTANCE = Yavi.schema()
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

    // SignupValidator and SignupService are separate types in their own files.
    // The same domain validator serves any request that carries the same input; the
    // coercion of the text of a form is the controller's decision.
    static final ValueValidator<Map<String, ?>, Signup> signupValidator = SignupValidator.INSTANCE
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

- The `@ModelAttribute` method is not optional: without it a request fails with
  `An Errors/BindingResult argument is expected to be declared immediately after the
  model attribute`, because Spring MVC creates a `BindingResult` only for a form object
  that it binds.
- Take the parameters as a `MultiValueMap`; a `Map<String, String>` drops all but the
  first value of a key.
- The `BindingResult` argument must come immediately after the model attribute.
- The code of a field error is the message key (`charSequence.email`, `object.type`), so
  a `MessageSource` replaces the message. See `references/messages.md`.
- Thymeleaf adds a hidden `_subscribed` field to a checkbox. No property is defined for
  that key, so a form with a checkbox cannot use `rejectingUnknownProperties()`.

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
| Spring, Jackson, Kotlin, NullAway | `references/integrations-*.md` |
| The classic `ValidatorBuilder` API | `references/using-yavi.md` and the rest of the Classic Validators section |
| Every constraint | `references/built-in-constraints.md` |
| Coming from Bean Validation or Zod | `references/coming-from-bean-validation.md`, `references/coming-from-zod.md` |

## The bundled documentation

Every page of the site, at the path it has there.

{{nav-table}}
