# メッセージとローカライズ

違反はメッセージキーとメッセージを持ちます。キーはルールを識別し、メッセージは検証のロケールに合わせて `MessageFormatter` で整形されます。バリデータには `formattingWith` でフォーマッターを与え、違反を表示する際には別のフォーマッターを与えることもできます。

## MessageFormatter

```java
public interface MessageFormatter {
    String format(String messageKey, String defaultMessageFormat, Object[] args, Locale locale);

    default String format(String messageFormat, Object[] args, Locale locale) { ... }
}
```

2 番目のメソッドのデフォルト実装は、テキストを `MessageFormat` で整形します。コードに書いたメッセージを、他のメッセージと同じ方法で整形するにはオーバーライドします。フォーマッターが知らないキーはデフォルトのメッセージ形式で整形されるため、フォーマッターは持っているメッセージだけを置き換えます。

フォーマッターは検証中とメッセージの整形時に呼ばれます。バリデータを共有する場合はスレッドセーフでなければなりません。

## リソースバンドル

Spring を使わない場合、`ResourceBundleMessageFormatter` はクラスパス上のリソースバンドルからキーでメッセージを検索します。

```java
// messages_ja.properties: numeric.lessThanOrEqual="{0}" は {1} 以下で入力してください
MessageFormatter formatter = new ResourceBundleMessageFormatter("messages");
validator.formattingWith(formatter).validate(input, Locale.JAPANESE);
```

バンドルは検証のロケールのもの（`messages_ja_JP`、次に `messages_ja`）、次に基底バンドル（`messages`）です。`ResourceBundle.getBundle` と異なり、プラットフォームのデフォルトロケールのバンドルはフォールバックになりません。日本語サーバー上での英語の検証が日本語で返ることはありません。バンドルにないキーはデフォルトのメッセージで整形されます。

Spring の `MessageSource` には `MessageSourceMessageFormatter` を使います。[Spring](integrations-spring.md#messagesource)を参照してください。

## ViolationMessage とキー

独自のメッセージは `ViolationMessage` です。メッセージキー、フォーマット、`{1}` 以降の引数の名前からなります。

```java
ViolationMessage outOfStock =
        ViolationMessage.of("order.outOfStock", "\"{0}\" exceeds the stock of {1}", "stock");
```

メッセージ形式は引数を位置で参照し続けます。`{0}` は値の名前、`{1}` は最初の引数です。これから生成された違反は `messageKey() = order.outOfStock`、`params() = {stock=3}` を持ちます。リソースバンドルでキーをローカライズできます。

`ViolationMessage#withText(text)` はキーと引数名を保ち、フォーマッターが置き換えないテキストでフォーマットを置き換えます。`localizable()` はテキストと、検索すべきメッセージを区別します。

## コードに書いたメッセージ

コードに与えたテキストは違反のメッセージになります。`MessageFormatter` は、メッセージキーに対して持っているメッセージでこれを置き換えなくなりました。引数を代入するだけで、キーは保たれます。

* 制約の `message(String)` は違反のメッセージです。
* プロパティの `required(String)` と `typeMessage(String)` は `object.required` と `object.type` のメッセージです。
* 複数の述語からなる制約（`url(url -> ...)` や `password(...)`）の直後の `message(...)` は、それぞれのメッセージを置き換えます。

テキストをローカライズするには、`ViolationMessage` で独自のキーを与えます。

```java
c -> c.notBlank().message(ViolationMessage.of("user.name.notBlank", "Enter your name"))
// messages_ja.properties: user.name.notBlank=名前を入力してください
```

`message(ViolationMessage)`、`required(ViolationMessage)`、`typeMessage(ViolationMessage)` は、これまでどおり独自のキーで検索されます。

## メッセージの上書き

Spring MVC の `violations.apply(bindingResult::rejectValue)` は上記のルールの影響を受けません。Spring がメッセージキーを再度検索するため、バンドルのメッセージがそこでは優先されます。`violation.localizable()` で 2 つの場合を区別できます。

メッセージは、ルールやプロパティを定義する箇所で、または違反を表示する際に上書きします。

```java
ViolationMessage label = ViolationMessage.of("token.roles", "Select at least one role");
Yavi.schema()._list("roles", Yavi._string()).label("Roles").required(label)
        .fromMap(Token::new)
        .readingParameters()
        .formattingWith(new MessageSourceMessageFormatter(messageSource::getMessage));
```

`formattingWith` は、ネストしたバリデータとリーダーのものを含め、バリデータのすべての違反に適用されます。`ConstraintViolations#labeled(...).formattedWith(...)` は違反を表示する際に行います。`labeled` は関数が `null` を返した場合、違反をそのままにします。

## ロケール

`validate(input, locale)` は与えられたロケールで検証し、`localizedWith(locale)` は結果、違反、違反のリストのロケールを変更します。`flatFromMap` や `flatApply` に渡す関数にはロケールが渡されません。その違反は検証のロケールで報告されます。

ロケールはリソースバンドルを選択し、`MessageFormat` でメッセージを整形します。存在しないバンドルはどのメッセージも置き換えないため、基底バンドルだけでは何もローカライズされません。ロケールごとにバンドルを用意してください。
