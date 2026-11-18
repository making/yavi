# Spring

Spring アプリケーションはリクエストの境界で YAVI を使います。スキーマがパラメータやボディを読み取り、結果は `BindingResult`、レスポンスボディ、リダイレクトのいずれかになります。YAVI は Spring に依存せず、アダプターは Spring のメソッドシグネチャに一致する関数インターフェースです。

## BindingResult

`ConstraintViolations#apply` は `BindingResult#rejectValue` の引数を持つ関数を受け取り、違反メッセージを渡します。

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

フィールドエラーのコードはメッセージキー（`object.type`）であるため、`MessageSource` がこれまでどおりメッセージを置き換えます。そのメッセージの `{0}` は、プロパティにラベルがあればそのラベルです。コードに書いたテキストはそのまま表示されます。`violation.localizable()` で 2 つの場合を区別できます。

`rejectValue` では Spring がメッセージキーを再度検索するため、バンドルのメッセージがそこでは優先されます。

## MessageSource

`MessageSourceMessageFormatter` は `MessageSource#getMessage` の関数インターフェースを取り、整形を Spring に委ねます。

```java
@Bean
MessageFormatter messageFormatter(MessageSource messageSource) {
    return new MessageSourceMessageFormatter(messageSource::getMessage);
}
```

バリデータには `formattingWith` で与えます。ネストしたバリデータとリーダーのものを含め、バリデータのすべての違反に適用されます。

```java
ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        .fromMap(Signup::new)
        .readingParameters()
        .formattingWith(messageFormatter);
```

## フォームの再表示

Spring MVC は、バインドするフォームオブジェクトに対してのみ `BindingResult` を生成し、パラメータの Map には生成しません。コントローラの `@ModelAttribute` メソッドが、パラメータの `MapBindingResult` をモデルに入れます。ハンドラは `BindingResult` を引数に取り、Thymeleaf の `th:object`、`th:field`、`#fields` はフォームオブジェクトの場合と同じように動作します。

```java
@ModelAttribute
void signupForm(@RequestParam MultiValueMap<String, String> params, Model model) {
    Map<String, Object> form = ParameterTree.of(params).toParameters(signupValidator.description());
    model.addAllAttributes(new MapBindingResult(form, "signupForm").getModel());
}
```

* `@ModelAttribute` メソッドはコントローラのすべてのハンドラより前に実行されるため、GET ハンドラは空のフォームを表示します。これがないと、リクエストは `An Errors/BindingResult argument is expected to be declared immediately after the model attribute` で失敗します。
* フォーム 1 つにつきコントローラ 1 つにします。メソッドはすべてのハンドラより前に実行されるため、1 つのコントローラに 2 つのフォームを置くとどちらもモデルに入り、`BindingResult` の引数は最後に追加されたモデル属性に解決されます。エラーにならず、どのハンドラも最後に追加されたフォームの `BindingResult` を受け取ります。`@ModelAttribute("signupForm") BindingResult` でも選べません。モデル属性は `BindingResult` ではなくフォームのマップだからです。コントローラを分けてください。
* フィールドは、プロパティの型でなくても（`age` に対する `abc`）、送信されたテキストを再表示します。
* フィールドは `th:field` と同じように命名します。`address.city`、`items[0].name` です。これらは違反の名前であり、`MapBindingResult` が検索するキーです。
* `toParameters` は単一の値のテキストと、リストプロパティのリストを返すため、テキストフィールドは `30` を表示し、チェックされたすべてのチェックボックスが再びチェックされます。
* Thymeleaf はチェックボックスに隠しフィールド `_subscribed` を追加します。そのキーにはプロパティが定義されていないため、そのようなフォームは `rejectingUnknownProperties()` では読み取れません。
* ハンドラは `MultiValueMap` を検証します。`Map<String, String>` では最初の値以外が失われます。

## IoC コンテナでのバリデータ

`Yavi.schema()` で構築したバリデータは不変でスレッドセーフであるため、他のサービスと同じように Bean として登録します。

```java
@Bean
ObjectInputValidator<User> userValidator() {
    return Yavi.schema()
            ._string("email", c -> c.notBlank().email())
            ._integer("age", c -> c.greaterThanOrEqual(0))
            .fromMap(User::new);
}
```

`ValidatorFactory` は、メッセージキーの区切りとフォーマッターを指定して `ValidatorBuilder` の従来の `Validator` を構築します。`BiConsumerFactory` は、Spring の `Validator#validate(Object, Errors)` シグネチャを受け取るコードのために `BiConsumer<T, Errors>` を構築します。

```java
@Bean
BiConsumerFactory<Errors> biConsumerFactory(MessageSource messageSource) {
    MessageFormatter formatter = new MessageSourceMessageFormatter(messageSource::getMessage);
    return new BiConsumerFactory<>(".", formatter, Errors::rejectValue);
}
```

## WebFlux

リアクティブなハンドラはボディを検証し、結果を `ServerResponse` に畳み込みます。

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

`fold(errorsMapper, mapper)` はボディが読み取られた後に呼ばれます。無効なボディは `violations.details()` を生成し、JSON ライブラリがそれをシリアライズします。[Jackson](integrations-jackson.md#serializing-violations)を参照してください。
