# Spring

A Spring application uses YAVI at the boundary of a request: the schema reads the parameters or the body, and the result becomes a `BindingResult`, a response body or a redirect. YAVI does not depend on Spring; the adapters are functional interfaces that match Spring's method signatures.

## BindingResult

`ConstraintViolations#apply` accepts a function with the arguments of `BindingResult#rejectValue` and passes the violation messages:

```java
@PostMapping("/signup")
String signup(@RequestParam MultiValueMap<String, String> params, BindingResult result) {
    return switch (signupValidator.validate(params)) {
        case Validated.Valid<Signup>(Signup signup) -> {
            register(signup);
            yield "redirect:/";
        }
        case Validated.Invalid<Signup>(ConstraintViolations violations) -> {
            violations.apply(result::rejectValue);
            yield "signup";
        }
    };
}
```

The code of a field error is the message key (`object.type`), so a `MessageSource` replaces the message as before. `{0}` of that message is the label of the property if it has one. A text written in code is shown as it is; `violation.localizable()` tells the two cases apart.

Spring looks the message key up again in `rejectValue`, so a message of the bundle still wins there.

## MessageSource

`MessageSourceMessageFormatter` takes the functional interface of `MessageSource#getMessage` and delegates the formatting to Spring:

```java
@Bean
MessageFormatter messageFormatter(MessageSource messageSource) {
    return new MessageSourceMessageFormatter(messageSource::getMessage);
}
```

Give it to a validator with `formattingWith`; it applies to every violation of the validator, those of nested validators and of the reader included:

```java
ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        .fromMap(Signup::new)
        .readingParameters()
        .formattingWith(messageFormatter);
```

## Showing a form again

Spring MVC creates a `BindingResult` only for a form object that it binds, not for a map of parameters. A `@ModelAttribute` method of the controller puts a `MapBindingResult` of the parameters into the model. A handler then takes the `BindingResult` as an argument, and `th:object`, `th:field` and `#fields` of Thymeleaf work as they do with a form object.

```java
@ModelAttribute
void signupForm(@RequestParam MultiValueMap<String, String> params, Model model) {
    Map<String, Object> form = ParameterTree.of(params).toParameters(signupValidator.description());
    model.addAllAttributes(new MapBindingResult(form, "signupForm").getModel());
}
```

* The `@ModelAttribute` method runs before every handler of the controller, so the GET handler renders the empty form. Without it a request fails with `An Errors/BindingResult argument is expected to be declared immediately after the model attribute`.
* One controller per form. Because the method runs before every handler, a controller with two forms would put both into the model, and a `BindingResult` argument resolves to the last model attribute added: every handler would receive the `BindingResult` of whichever form was added last, without an error. `@ModelAttribute("signupForm") BindingResult` does not select one either, since the model attribute is the form map rather than the `BindingResult`. Split the controller.
* A field shows the text that was sent again, even if it is not of the type of its property (`abc` for `age`).
* Name a field as `th:field` does: `address.city`, `items[0].name`. These are the names of the violations and the keys that `MapBindingResult` looks up.
* `toParameters` gives the text of a single value and the list of a list property, so a text field shows `30` and every checked checkbox is checked again.
* Thymeleaf adds a hidden field `_subscribed` to a checkbox. No property is defined for that key, so such a form cannot be read with `rejectingUnknownProperties()`.
* The handler validates a `MultiValueMap`; a `Map<String, String>` would drop all but the first value.

## Validators in an IoC container

A validator built with `Yavi.schema()` is immutable and thread-safe, so it is registered as a bean like any other service:

```java
@Bean
ObjectInputValidator<User> userValidator() {
    return Yavi.schema()
            ._string("email", c -> c.notBlank().email())
            ._integer("age", c -> c.greaterThanOrEqual(0))
            .fromMap(User::new);
}
```

`ValidatorFactory` builds classic `Validator`s of `ValidatorBuilder` with a message key separator and a formatter, and `BiConsumerFactory` builds `BiConsumer<T, Errors>` for code that accepts Spring's `Validator#validate(Object, Errors)` signature:

```java
@Bean
BiConsumerFactory<Errors> biConsumerFactory(MessageSource messageSource) {
    MessageFormatter formatter = new MessageSourceMessageFormatter(messageSource::getMessage);
    return new BiConsumerFactory<>(".", formatter, Errors::rejectValue);
}
```

## WebFlux

A reactive handler validates the body and folds the result into a `ServerResponse`:

```java
RouterFunction<ServerResponse> routes() {
    return RouterFunctions.route()
        .POST("/users", request -> request.bodyToMono(UserRequest.class)
            .flatMap(body -> userValidator.validate(body)
                .map(UserRequest::toUser)
                .map(userService::create)
                .fold(violations -> ServerResponse.badRequest()
                        .body(ConstraintViolations.of(violations).details()),
                    created -> ServerResponse.ok().body(created))))
        .build();
}
```

`fold(errorsMapper, mapper)` is called when the body has been read. An invalid body produces `violations.details()`, which a JSON library serializes; see [Jackson](integrations-jackson.md#serializing-violations).
