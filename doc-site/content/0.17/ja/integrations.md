# 連携

YAVI は何にも依存しないが、フレームワークに自然に組み込めるインターフェースを備えています。

## Spring MVC の `BindingResult` との連携

`ConstraintViolations#apply` は、Spring Framework の `BindingResult#rejectValue` メソッドと同じ引数を持つ関数インターフェースを受け取り、違反メッセージを渡します。

YAVI による検証結果を Spring MVC のコントローラに反映するには、次のコードを書きます。

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

関数型の書き方が好みなら、次のようにも書けます:

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

> **ヒント**
>
> `Validated#fold` の第 1 引数の関数には `ConstraintViolations` ではなく `List<ConstraintViolation>` が渡されます。`List<ContraintViolation>` は `ConstraintViolations#of` メソッドで `ConstraintViolations` に変換できます。

> **注意**
>
> [https://github.com/making/demo-spring-mvc-yavi](https://github.com/making/demo-spring-mvc-yavi) は完全な例です。

## `ConstraintViolations` をレスポンスボディとしてシリアライズできる形式への変換

`ConstraintViolation` は Jackson のようなシリアライザでのシリアライズには向いていません。代わりに `ConstraintViolation#detail` メソッドを使うと、シリアライズしやすい `ViolationDetail` オブジェクトに変換できます。

`ConstraintViolations#details` メソッドはすべての `ConstraintViolation` を変換し、`List<ViolationDetail>` を返します。

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

関数型の書き方が好みなら、次のようにも書けます:

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

> **ヒント**
>
> `ViolationDetail` は GraalVM ネイティブイメージでそのまま動作します。

## Spring WebFlux.fn との連携

YAVI は [Spring WebFlux.fn](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-fn) によく合います。

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

> **ヒント**
>
> YAVI はもともと、Spring WebFlux.fn に自然に適合するバリデータとして開発されました。

## Spring Framework の `MessageSource` との連携

`am.ik.yavi.message.MessageSourceMessageFormatter` は、Spring Framework の `MessageSource#getMessage` と同じ引数を持つ関数インターフェースを受け取ります。
これにより、違反メッセージのフォーマットの制御を Spring Framework に委ねられます。

`MessageSourceMessageFormatter` は次のように使えます:

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

## IoC コンテナでの `ValidatorBuilder` の管理

`ValidatorBuilder` をカスタマイズして Spring Framework のような IoC コンテナで管理したい場合は、`am.ik.yavi.factory.ValidatorFactory` を使えます。

次は Spring Framework で `ValidatorFactory` を定義する例です:

```java
@Bean
public ValidatorFactory yaviValidatorFactory(MessageSource messageSource) {
    MessageFormatter messageFormatter = new MessageSourceMessageFormatter(messageSource::getMessage);
    return new ValidatorFactory("." /* Message Key Separator */, messageFormatter);
}
```

Validator の使い方は次のようになります:

```java
@RestController
public class OrderController {
    private final Validator<CartItem> validator;

    public OrderController(ValidatorFactory factory) {
        this.validator = factory.validator(builder -> builder.constraint(...));
    }
}
```

## `BiConsumer` の取得

0.16.0 から、バリデータは専用の型ではなく `java.util.function.BiConsumer<T, E>` として公開されます。`T` は対象オブジェクトの型、`E` はエラーオブジェクトの型です。

これは、YAVI と、`org.springframework.validation.Validator#validate(Object, Errors)` のようにこれら 2 つの引数を受け取る他の検証フレームワークの両方に対応したいライブラリやアプリに役立ちます。

`Validatable#toBiConsumer` と `ValueValidator#toBiConsumer` は `am.ik.yavi.core.ErrorHandler<E>` を受け取ります:

```java
public interface ErrorHandler<E> {
    void handleError(E errors, String name, String messageKey, Object[] args, String defaultMessage);
}
```

Spring Framework の `Errors` に対しては、ハンドラで `Errors#rejectValue` を使って違反を記録できます:

```java
Validator<CartItem> cartItemValidator = ...;
BiConsumer<CartItem, Errors> validator = cartItemValidator
    .toBiConsumer((errors, name, messageKey, args, defaultMessage) ->
        errors.rejectValue(name, messageKey, args, defaultMessage));
// or
BiConsumer<CartItem, Errors> validator = cartItemValidator.toBiConsumer(Errors::rejectValue);
```

`BiConsumer` のファクトリもあります:

```java
@Bean
public BiConsumerFactory<Errors> biConsumerFactory() {
    return new BiConsumerFactory<>(Errors::rejectValue);
}
```

または、メッセージの設定をカスタマイズする場合:

```java
@Bean
public BiConsumerFactory<Errors> biConsumerFactory(MessageSource messageSource) {
    MessageFormatter messageFormatter = new MessageSourceMessageFormatter(messageSource::getMessage);
    return new BiConsumerFactory<>("." /* Message Key Separator */, messageFormatter, Errors::rejectValue);
}
```

`BiConsumer` の使い方は次のようになります:

```java
@RestController
public class OrderController {
    private final BiConsumer<CartItem, Errors> validator;

    public OrderController(BiConsumerFactory<Errors> factory) {
        this.validator = factory.validator(builder -> builder.constraint(...));
    }
}
```
