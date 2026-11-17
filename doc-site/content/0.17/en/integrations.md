# Integrations

Although YAVI doesn't depend on anything, it has interfaces that magically fit into Framework.

## Integration with `BindingResult` in Spring MVC

`ConstraintViolations#apply` accepts the Function Interface with the same arguments as Spring Framework's `BindingResult#rejectValue` method and passes the violation messages.

To reflect validation results by YAVI in a Controller of Spring MVC, you can write the following code.

```java
final Validator<UserForm> validator = ...;

@PostMapping(path = "users")
public String createUser(Model model, UserForm userForm, BindingResult bindingResult) {
    ConstraintViolations violations = validator.validate(userForm);
    if (!violations.isValid()) {
        violations.apply(bindingResult::rejectValue);
        return "userForm";
    }
    // ...
    return "redirect:/";
}
```

If you like a functional way, you can also write:

```java
final Validator<UserForm> validator = ...;

@PostMapping(path = "users")
public String createUser(Model model, UserForm userForm, BindingResult bindingResult) {
    return validator.applicative()
        .validate(userForm)
        .fold(violations -> {
            ConstraintViolations.of(violations).apply(bindingResult::rejectValue);
            return "userForm";
        }, form -> {
            // ...
            return "redirect:/";
        });
}
```

> **Tip**
>
> `List<ConstraintViolation>` is passed to the function of the first argument of `Validated#fold` instead of `ConstraintViolations`. You can convert the `List<ContraintViolation>` to `ConstraintViolations` by using the `ConstraintViolations#of` method.

> **Note**
>
> [https://github.com/making/demo-spring-mvc-yavi](https://github.com/making/demo-spring-mvc-yavi) is a full example.

## Converting `ConstraintViolations` to the format to serialize as a response body

`ConstraintViolation` is not suitable for serializing with a serializer like Jackson. Instead, you can use `ConstraintViolation#detail` method to convert it to a `ViolationDetail` object that is easy to serialize.

The `ConstraintViolations#details` method translates all `ConstraintViolation` s and returns `List<ViolationDetail>`.

```java
final Validator<UserCreateRequest> validator = ...;

@PostMapping(path = "users")
public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {
    ConstraintViolations violations = validator.validate(request);
    if (violations.isValid()) {
        User created = userService.create(request.toUser());
        return ResponseEntity.ok(created);
    } else {
        return ResponseEntity.badRequest().body(violations.details());
    }
}
```

If you like a functional way, you can also write:

```java
final Validator<UserCreateRequest> validator = ...;

@PostMapping(path = "users")
public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {
    return validator.applicative()
        .validate(request)
        .map(req -> userService.create(req.toUser()))
        .mapErrors(violations -> ConstraintViolations.of(violations).details())
        // or .mapError(ConstraintViolation::detail)
        .fold(details -> ResponseEntity.badRequest().body(details),
            created -> ResponseEntity.ok(created));
}
```

> **Tip**
>
> `ViolationDetail` works with GraalVM native image out of the box.

## Integration with Spring WebFlux.fn

YAVI will be a great fit for [Spring WebFlux.fn](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-fn).

```java
final Validator<UserCreateRequest> validator = ...;

public RouterFunction<ServerResponse> routes() {
    return RouterFunctions.route()
        .POST("/users", request -> request.bodyToMono(UserCreateRequest.class)
            .flatMap(body -> validator.applicative()
                .validate(body)
                .map(req -> userService.create(req.toUser()))
                .mapErrors(violations -> ConstraintViolations.of(violations).details())
                // or .mapError(ConstraintViolation::detail)
                .fold(details -> ServerResponse.badRequest().bodyValue(details),
                    created -> ServerResponse.ok().bodyValue(created))))
        .build();
}
```

> **Tip**
>
> YAVI was originally developed as a validator naturally fit with Spring WebFlux.fn.

## Integration with `MessageSource` in Spring Framework

`am.ik.yavi.message.MessageSourceMessageFormatter` accepts the Functional Interface with the same arguments as Spring Framework's `MessageSource#getMessage`.
This allows you to delegate control of the message format of violation messages to Spring Framework.

`MessageSourceMessageFormatter` can be used as follows:

```java
@RestController
public class OrderController {
    private final Validator<CartItem> validator;

    public OrderController(MessageSource messageSource) {
        MessageFormatter messageFormatter = new MessageSourceMessageFormatter(messageSource::getMessage);
        this.validator = ValidatorBuilder.<CartItem> of()
            .constraints(...)
            .messageFormatter(messageFormatter)
            .build();
    }
}
```

## Managing `ValidatorBuilder` in an IoC Container

If you want to customize `ValidatorBuilder` and manage it with an IoC Container like Spring Framework, you can use `am.ik.yavi.factory.ValidatorFactory`.

The following is an example of defining a `ValidatorFactory` in Spring Framework:

```java
@Bean
public ValidatorFactory yaviValidatorFactory(MessageSource messageSource) {
    MessageFormatter messageFormatter = new MessageSourceMessageFormatter(messageSource::getMessage);
    return new ValidatorFactory("." /* Message Key Separator */, messageFormatter);
}
```

The usage of a Validator would look like following:

```java
@RestController
public class OrderController {
    private final Validator<CartItem> validator;

    public OrderController(ValidatorFactory factory) {
        this.validator = factory.validator(builder -> builder.constraint(...));
    }
}
```

## Obtaining a `BiConsumer`

Since 0.16.0, a validator is exposed as a `java.util.function.BiConsumer<T, E>` instead of a dedicated type, where `T` is the type of the target object and `E` is the type of the errors object.

This is helpful for libraries or apps to adapt both YAVI and other validation frameworks that accept these two arguments, like Spring Framework's `org.springframework.validation.Validator#validate(Object, Errors)`.

`Validatable#toBiConsumer` and `ValueValidator#toBiConsumer` take an `am.ik.yavi.core.ErrorHandler<E>`:

```java
public interface ErrorHandler<E> {
    void handleError(E errors, String name, String messageKey, Object[] args, String defaultMessage);
}
```

For Spring Framework's `Errors`, the handler can record the violation with `Errors#rejectValue`:

```java
Validator<CartItem> cartItemValidator = ...;
BiConsumer<CartItem, Errors> validator = cartItemValidator
    .toBiConsumer((errors, name, messageKey, args, defaultMessage) ->
        errors.rejectValue(name, messageKey, args, defaultMessage));
// or
BiConsumer<CartItem, Errors> validator = cartItemValidator.toBiConsumer(Errors::rejectValue);
```

There is a factory for `BiConsumer` as well:

```java
@Bean
public BiConsumerFactory<Errors> biConsumerFactory() {
    return new BiConsumerFactory<>(Errors::rejectValue);
}
```

or, if you want to customize the message configuration

```java
@Bean
public BiConsumerFactory<Errors> biConsumerFactory(MessageSource messageSource) {
    MessageFormatter messageFormatter = new MessageSourceMessageFormatter(messageSource::getMessage);
    return new BiConsumerFactory<>("." /* Message Key Separator */, messageFormatter, Errors::rejectValue);
}
```

The usage of a `BiConsumer` would look like following:

```java
@RestController
public class OrderController {
    private final BiConsumer<CartItem, Errors> validator;

    public OrderController(BiConsumerFactory<Errors> factory) {
        this.validator = factory.validator(builder -> builder.constraint(...));
    }
}
```
