# 0.17 からの移行

YAVI 0.18.0 には、0.17 に対する次の破壊的変更があります。影響を受けるユーザーが多い順に並べています。

コード例の「before」は 0.17 向けに書かれたコードです。「after」は、同じコードを 0.18 でコンパイルでき、同じ動作になるように直したものです。「after (new API)」は 0.18 で追加された API を使ったものです。新しい API は [スキーマの定義](defining-a-schema.md)で説明しています。

「before」の `Yavi.arguments()` は、「after」では `Yavi.schema()` と書いています。`Yavi.arguments()` は 0.18 でもコンパイルできますが、非推奨です。詳しくは [`Yavi.arguments()` は非推奨になり、`YaviArguments` は `YaviSchema` になった](#yaviarguments-is-deprecated-and-yaviarguments-is-yavischema)を参照してください。

## Java 21 が必須

YAVI 0.18.0 は Java 21 向けにコンパイルされています。0.17 は Java 8 で動作していました。

### 影響を受けるユーザー

* Java 8 から 20 で動作するアプリケーション。0.17 を使い続けてください。
* 0.17 に対してコンパイルされたコード（YAVI を利用するライブラリなど）。`Validation` がインターフェースになり、`Yavi.arguments()` のメソッドの戻り値の型も変わったため、再コンパイルするまで 0.18 では実行時に失敗します。

### 移行手順

#### 1. Java 21 以降で実行する

#### 2. YAVI を使うコードを再コンパイルする

ソースを変更しない箇所でも再コンパイルが必要です。

### 関連するプルリクエスト

* [#532](https://github.com/making/yavi/pull/532): ベースラインを Java 21 に引き上げます。

## 引数と単一の値のバリデータはデフォルトで `null` を拒否する

`Yavi.schema()` の引数と、単一の値のバリデータ（`StringValidatorBuilder` など）は、`notNull()` がなくても `null` を拒否するようになりました。`null` になりうる値には、0.18.0 で追加された `nullable()` を付けます。[省略と既定値](optional-and-defaults.md#accepting-null)を参照してください。

```java
record Member(String name, @Nullable String nickname) {}

Arguments2Validator<String, @Nullable String, Member> validator = Yavi.schema()
        ._string("name", c -> c.lessThanOrEqual(50))                // null was valid, is a violation now
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable() // null is valid
        .apply(Member::new);
```

`nullable()` は、バリデータが返す値の型も `@Nullable` に変えます。これは NullAway で検査するコードと Kotlin に影響します。

### 影響を受けるユーザー

影響を受けるもの:

* `Yavi.arguments()` の引数。
* `StringValidatorBuilder`、`IntegerValidatorBuilder` などで、`of(name, constraints)` または `wrap(b -> ...)` を使って作った単一の値のバリデータ。
* `ValueValidator.passThrough()`。型だけが変わります。
* 直接使っている `Validation` の `value()` と `valueNullable()`。

影響を受けないもの:

* `ValidatorBuilder` と `Validator`。`null` のプロパティは、`notNull()` などの制約が拒否しない限り、これまでどおり有効です。

```java
Validator<Car> validator = ValidatorBuilder.<Car>of()
        .constraint(Car::manufacturer, "manufacturer", c -> c.notNull())
        .constraint(Car::seatCount, "seatCount", c -> c.greaterThanOrEqual(2)) // null is still valid
        .build();
```

* `ArgumentsValidatorBuilder.of(f).builder(b -> ...)` と、アノテーションプロセッサがそのために生成するメタクラス。
* `validator.applicative()`。

### 移行手順

手順 1 と 2 はすべてのユーザーが対象です。手順 3 は NullAway で検査するコードと Kotlin が対象です。手順 4 は任意です。

#### 1. `null` になりうる値に `nullable()` を付ける

対象は、`null` を拒否する制約（`notNull()`、`notBlank()`、`notEmpty()` など）を持たない値です。`nullable()` がなければ、`null` は `object.notNull` の違反になります。

引数の場合:

```java
// before
Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20))
        .apply(Member::new);

// after
Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable()
        .apply(Member::new);

// after (new API)
// optional() and defaultValue(...) accept null too and keep it out of the validated value
Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).optional() // Optional<String>
        .apply(Member::new);

Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).defaultValue("") // String
        .apply(Member::new);
```

単一の値の場合:

```java
// before
ValueValidator<String, String> memo = StringValidatorBuilder
        .of("memo", c -> c.lessThanOrEqual(100)).build();

// after
ValueValidator<String, @Nullable String> memo = StringValidatorBuilder
        .of("memo", c -> c.lessThanOrEqual(100)).build().nullable();

// after (new API)
ValueValidator<String, @Nullable String> memo = Yavi._string("memo", c -> c.lessThanOrEqual(100)).nullable();
```

#### 2. `isNull()` を使う値に `nullable()` を付ける

`nullable()` がなければ、どの値も有効になりません。

```java
// before
Yavi.arguments()._string("id", c -> c.isNull())

// after
Yavi.schema()._string("id", c -> c.isNull()).nullable()
```

#### 3. NullAway または Kotlin では、null になりうる値を受け取る側を nullable として宣言する

null 性チェッカで検査していない Java コードは、この手順なしでコンパイルできます。

* `apply` に渡すコンストラクタまたは関数のうち、`nullable()` の引数を受け取るパラメータ: `@Nullable String`、Kotlin では `String?`。他のパラメータは `null` を受け付ける必要がなくなりました。
* 宣言するバリデータの型引数: `ValueValidator<String, @Nullable String>`、`Validated<@Nullable String>`。
* `ValueValidator.passThrough()`: `ValueValidator<X, @Nullable X>`。

```kotlin
data class Member(val name: String, val nickname: String?)

Yavi.schema()
    ._string("name") { it.notBlank() }
    ._string("nickname") { it.lessThanOrEqual(20) }.nullable()
    .apply(::Member)
```

`@Nullable` を宣言していないパラメータについて、NullAway は次のように報告します:

```
[NullAway] parameter age of referenced method is @NonNull, but parameter in functional interface method am.ik.yavi.fn.Function2.apply(String,@Nullable Integer) is @Nullable
```

NullAway が正しいコードを報告する場合は [制限](integrations-nullaway.md#limitations)を参照してください。

#### 4. 任意の後片付け

以下は必須ではありません。変更前のコードはそのまま動作し、以前と同じ振る舞いになります。

* 値を必須にするためだけの `notNull()` は削除できます。`null` の違反に別のメッセージを与えたい場合は残します。

```java
// before
Yavi.arguments()
        ._string("name", c -> c.notNull())
        ._integer("age", c -> c.notNull().greaterThanOrEqual(0))
        .apply(Member::new);

// after
Yavi.schema()
        ._string("name")
        ._integer("age", c -> c.greaterThanOrEqual(0))
        .apply(Member::new);
```

* `valueNullable()` は非推奨です。`value()` は、`null` を保持する成功結果でも同じ値を返します。

```java
// before
@Nullable String value = validated.valueNullable();

// after
@Nullable String value = validated.value();
```

### すべての変更

動作:

| | 変更前 | 変更後 |
| --- | --- | --- |
| `null` を拒否しない制約の引数または単一の値に対する `null` | 有効で、値は `null` | `object.notNull` の違反 |
| `null` を拒否する制約（`notBlank()`、`notNull()`）を持つ値に対する `null` | その制約の違反 | 変更なし |
| 引数または単一の値に対する `isNull()` | `null` のみ受け付ける | `nullable()` がなければどの値も有効にならない |
| `null` を保持する成功結果の `Validation.Success#value()` | `NoSuchElementException` を投げる | `null` を返す。`valueNullable()` は非推奨 |

型:

| | 変更前 | 変更後 |
| --- | --- | --- |
| `apply` に渡す関数のパラメータ | すべての引数が `@Nullable` | `nullable()` の引数だけが `@Nullable` |
| `ValueValidator.passThrough()` | `ValueValidator<X, X>` | `ValueValidator<X, @Nullable X>` |

型の変更はアノテーションによるものなので、バイナリ互換です。null 性チェッカで検査していない Java コードとはソース互換です。

### 理由

値が `null` を受け付けるかどうかは、制約のラムダ（`c -> c.notNull()`）の中で決まっていました。値の型はそれがあってもなくても同じだったため、コンパイラも null 性チェッカも `notNull()` の付け忘れを見つけられませんでした:

```java
record Member(String name, int age) {}

Arguments2Validator<String, Integer, Member> validator = Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._integer("age", c -> c.greaterThanOrEqual(0)) // notNull() is missing
        .apply(Member::new);

validator.validate("Taro", null); // before: NullPointerException, thrown out of validate
```

[NullAway](https://github.com/uber/NullAway) は型だけで null 性を検査するため、この2つの場合を区別できませんでした。`apply` に渡す関数のパラメータはすべての引数で `@Nullable` であり、`notNull()` で検証した値でも `apply(Member::new)` が報告されていました。

現在は、値がそう宣言しない限り必須になり、宣言すると型が変わります。Zod や Raoh と同じです。値が `null` になるのは型がそう示す場合だけであり、NullAway と Kotlin がそれを検査できます。

### 関連するプルリクエスト

* [#557](https://github.com/making/yavi/pull/557): 検証済みの値を必須にし、`nullable()` がその型を変えられるようにします。
* [#556](https://github.com/making/yavi/pull/556): その前提です。`apply` に渡す関数は、検証済みの値を `@Nullable` のパラメータなしで受け取ります。

## `ViolationDetail` は record になった

`ConstraintViolations#details()` は、どの値が失敗したかを示す record を返します。アクセサの名前が変わり、JSON ライブラリがそこから書き出す JSON のプロパティ名も変わります。位置引数の `args` は `params` に置き換わり、引数は値の名前と違反した値を含まない名前付きの値になります。[違反](violations.md#violationdetail)を参照してください。

```java
List<ViolationDetail> details = violations.details();
// [{"name": "items[1].quantity", "pointer": "/items/1/quantity", "messageKey": "numeric.lessThanOrEqual",
//   "message": "\"items[1].quantity\" must be less than or equal to 99", "params": {"max": 99}}]
```

### 影響を受けるユーザー

影響を受けるもの:

* `ViolationDetail` の `getKey()`、`getArgs()`、`getDefaultMessage()`、またはそのコンストラクタを呼ぶコード。
* `details()` を JSON で返す API のクライアント。特に `args` を読むもの。
* クライアントが引数を読むカスタム制約や手書きの違反。メッセージが名前を付けている場合にだけ `params` に入ります。
* `details()` が返すリストを変更するコード。

影響を受けないもの:

* `ConstraintViolation` の `name()`、`messageKey()`、`args()`、`message()`。

### 移行手順

#### 1. アクセサを改名し、引数を違反から読む

```java
// before
for (ViolationDetail detail : violations.details()) {
    String key = detail.getKey();
    Object[] args = detail.getArgs();
    String message = detail.getDefaultMessage();
}

// after: a detail has no positional arguments, a violation has
for (ConstraintViolation violation : violations) {
    String key = violation.messageKey();
    Object[] args = violation.args();
    String message = violation.message();
}

// after (new API)
for (ViolationDetail detail : violations.details()) {
    String name = detail.name();                // items[1].quantity
    String pointer = detail.pointer();          // /items/1/quantity, a JSON Pointer
    Map<String, Object> params = detail.params(); // {max=99}
}
```

#### 2. クライアントを新しい JSON に合わせるか、0.17 の JSON を維持する

```json
// before
[{"key": "charSequence.notBlank", "args": ["items[1].name", ""], "defaultMessage": "\"items[1].name\" must not be blank"}]

// after
[{"name": "items[1].name", "pointer": "/items/1/name", "messageKey": "charSequence.notBlank",
  "message": "\"items[1].name\" must not be blank", "params": {}}]
```

違反した値を返す 0.17 の JSON を維持するには、違反を独自の型にマップします:

```java
record LegacyDetail(String key, List<Object> args, String defaultMessage) {}

List<LegacyDetail> details = violations.stream()
        .map(violation -> new LegacyDetail(violation.messageKey(), Arrays.asList(violation.args()), violation.message()))
        .toList();
```

#### 3. 独自メッセージの引数に名前を付ける

組み込みの制約の引数には名前があります。独自のメッセージの引数は、メッセージが名前を付けている場合にだけ `params` に入ります:

```java
// before: "args": ["quantity", 3, 5]
ViolationMessage.of("order.outOfStock", "\"{0}\" exceeds the stock of {1}");

// after (new API): "params": {"stock": 3}
ViolationMessage.of("order.outOfStock", "\"{0}\" exceeds the stock of {1}", "stock");
```

### すべての変更

| | 変更前 | 変更後 |
| --- | --- | --- |
| メッセージキー | `getKey()`、JSON の `key` | `messageKey()`、JSON の `messageKey` |
| 整形済みメッセージ | `getDefaultMessage()`、JSON の `defaultMessage` | `message()`、JSON の `message` |
| 引数 | `Object[] getArgs()`、JSON の `args`: 位置引数で、名前から始まり違反した値で終わる | `Map<String, Object> params()`、JSON の `params`: 名前付きで、名前と違反した値を含まず、変更不可。位置引数は `ConstraintViolation#args()` |
| 値の名前と JSON Pointer | 利用できない | `name()`、`pointer()` |
| 型 | クラス | record `ViolationDetail.Simple` と `ViolationDetail.NoAlternativeMatched` の sealed インターフェース |
| `ValueValidator.anyOf` の選択肢の詳細 | 利用できない | `ViolationDetail.NoAlternativeMatched` の `alternatives()`、JSON の `alternatives` |
| コンストラクタ | `ViolationDetail(key, args, defaultMessage)` | `ViolationDetail.Simple(name, pointer, messageKey, message, params)` |
| `details()` が返すリスト | 変更可能 | 変更不可 |

### 理由

詳細はどの値が失敗したかを示していませんでした。名前は `args` の最初の要素にすぎませんでした。`defaultMessage` は整形済みのメッセージで、既定のメッセージではありませんでした。

独自のメッセージを描画するクライアントは、メッセージキーごとに `args` のどの位置に何が入るかを知る必要があり、その表は API の一部ではありませんでした。また `args` は違反した値で終わっていたため、呼び出し側が詳細を独自の型にマップしない限り、`details()` を返すと拒否したパスワードやトークンも返していました。

### 関連するプルリクエスト

* [#551](https://github.com/making/yavi/pull/551): `ViolationDetail` を record にし、`messagesByName()`、`messagesByPointer()`、`pointer()` を追加します。
* [#566](https://github.com/making/yavi/pull/566): 2つの record の sealed インターフェースにし、`ViolationDetail.NoAlternativeMatched` が `alternatives` を持ちます。
* [#568](https://github.com/making/yavi/pull/568): `args` を `params` に置き換えます。

## liftList などの違反名はインデックスが先になる

`liftList()`、`liftSet()`、`liftCollection()` は、要素のバリデータの名前の前にインデックスを置きます。0.17 では名前の後に置いていました。要素の検証は [オブジェクトとコレクション](objects-and-collections.md#lists)で説明しています。

```java
Yavi._string("tag", c -> c.notBlank())
        .liftList()
        .validate(List.of("java", "")); // [1].tag, was tag[1]
```

コードはそのままコンパイルできます。違反の名前が変わります。

### 影響を受けるユーザー

影響を受けるもの:

* 要素のバリデータに名前がある場合に、違反を名前で参照するコード。フォームのフィールドやテストのアサーションなど。
* `indexed(int)` を呼ぶコード。

影響を受けないもの:

* `ValidatorBuilder#forEach`。
* 要素の名前が空の lifted バリデータ。以前と同じく `[1]` を報告します。

### 移行手順

#### 1. 要素ではなくコレクションに名前を付ける

```java
// before
StringValidatorBuilder.of("tag", c -> c.notBlank()).build()
        .liftList()
        .validate(List.of("java", "")); // tag[1]

// after
StringValidatorBuilder.of("", c -> c.notBlank()).build()
        .liftList().prefixed("tag")
        .validate(List.of("java", "")); // tag[1]

// after (new API)
Yavi._string(c -> c.notBlank())
        .liftList().prefixed("tag")
        .validate(List.of("java", "")); // tag[1]
```

オブジェクトの要素はプロパティの名前を保持します。`prefixed` を付けると、`ValidatorBuilder#forEach` の要素と同じ名前になります:

```java
// before
addressValidator.applicative().liftList().validate(addresses); // street[1]

// after
addressValidator.applicative().liftList().prefixed("addresses").validate(addresses); // addresses[1].street
```

#### 2. `indexed(int)` を `prefixed(int)` に置き換える

`indexed(int)` は `ConstraintViolation`、`Validated`、`ValueValidator`、`Arguments1Validator` から削除されました。`ConstraintViolation` と `Validated` の `prefixed(int)` は、名前の前にインデックスを置きます。

```java
// before
validated.indexed(1); // tag[1]

// after
validated.prefixed(1); // [1].tag
```

### 理由

名前が入力の構造に従っていませんでした。2番目の住所の `street` プロパティは `street[1]` で、`street` の2番目の要素のように読めました。

### 関連するプルリクエスト

* [#544](https://github.com/making/yavi/pull/544): lifted 要素の違反をインデックスの下に置き、`indexed(int)` を削除します。

## コードに書いたメッセージはフォーマッタに置き換えられない

制約の `message(String)`、またはプロパティの `required(String)` と `typeMessage(String)` でコードに書いたテキストが、違反のメッセージになります。`MessageSourceMessageFormatter` などの `MessageFormatter` は、制約のメッセージキーに対応するメッセージでそのテキストを置き換えなくなりました。引数を代入するだけです。メッセージキーは保持されます。[メッセージとローカライズ](messages.md#messages-written-in-code)を参照してください。

```java
// messages.properties: charSequence.notBlank={0} must not be blank.
Validator<User> validator = ValidatorBuilder.<User>of()
        .messageFormatter(new MessageSourceMessageFormatter(messageSource::getMessage))
        ._string(User::name, "name", c -> c.notBlank().message("Enter your name"))
        .build();

validator.validate(new User("")).get(0).message();
// before: name must not be blank.
// after:  Enter your name
```

複数の述語からなる制約、`url(url -> ...)`、`password(policies -> ...)` の直後の `message(...)` は、最後の1つだけでなく、それらすべてのメッセージを置き換えます。

```java
._string(Site::url, "url", c -> c.url(url -> url.protocol("http", "https").build()).message("Enter an http URL"))
// "javascript:alert(1)" (charSequence.url)
// before: "url" must be a valid URL
// after:  Enter an http URL
```

### 影響を受けるユーザー

影響を受けるもの:

* `message(String)`、`required(String)`、`typeMessage(String)` のメッセージキーに対応するメッセージを、バリデータのフォーマッタが持つ場合。通常は、既定のメッセージをローカライズするリソースバンドルを持つ `MessageSourceMessageFormatter` です。テキストはすべてのロケールで表示されるようになります。
* `protocol`、`host`、`port` を持つ `url(url -> ...)` の直後、または複数のポリシーを持つ `password(...)` の直後の `message(...)`。
* メッセージの参照以外のことをする独自の `MessageFormatter`。テキストは新しい `format(String messageFormat, Object[] args, Locale locale)` に渡され、オーバーライドしなければ `MessageFormat` で整形されます。
* `ConstraintViolation.builder().name(...).message(String)`。メッセージキー `_` は参照されなくなりました。

影響を受けないもの:

* `message(ViolationMessage)`、`required(ViolationMessage)`、`typeMessage(ViolationMessage)`。以前と同じく独自のキーで参照されます。
* 違反のメッセージキーと引数、したがって `ViolationDetail`。
* 既定の `SimpleMessageFormatter` を使うバリデータ。何も参照しません。
* Spring MVC の `violations.apply(bindingResult::rejectValue)`: Spring がメッセージキーを再度参照するため、バンドルのメッセージが引き続き優先されます。`violation.localizable()` はテキストと、参照すべきメッセージを区別します。

### 移行手順

#### 1. フォーマッタが置き換えていたテキストを削除する

そのテキストは 0.17 では表示されていませんでした。削除すれば、違反は以前と同じように読めます。

```java
// before
c -> c.notBlank().message("Enter your name")

// after: the message of the bundle, as in 0.17
c -> c.notBlank()
```

#### 2. 独自のキーをローカライズするテキストを与える

```java
// after
c -> c.notBlank().message(ViolationMessage.of("user.name.notBlank", "Enter your name"))
// messages_ja.properties: user.name.notBlank=名前を入力してください
```

#### 3. `url(...)` または `password(...)` の1つの述語に独自のメッセージを与える

```java
// before: the text is that of the protocol only
c -> c.url(url -> url.protocol("http", "https").build()).message("Enter an http URL")

// after: the same messages as 0.17
c -> c.url(url -> {
    List<ConstraintPredicate<String>> predicates = new ArrayList<>(url.protocol("http", "https").build());
    predicates.set(1, predicates.get(1).overrideMessage("Enter an http URL"));
    return predicates;
})
```

### 理由

あるプロパティのために書いたテキストは、バンドルがそのキーのすべての違反に持つメッセージより具体的です。0.17 では、アプリケーションが既定のメッセージをローカライズした途端に、より具体的なテキストが黙って負けていました。キーは、キーで違反を区別するクライアントが引き続き区別できるように保持されます。

`message(...)` は、制約が追加した最後の述語のメッセージを置き換えていました。`url(...)` では組み立て方によって変わり、`ftp://example.com`（`charSequence.url.protocol`）ではテキストが表示され、`javascript:alert(1)`（`charSequence.url`）では表示されませんでした。

### 関連するプルリクエスト

* [#575](https://github.com/making/yavi/pull/575)

## 名前のない値のメッセージ

名前が空の違反のメッセージは、引用符付きの空の名前ではなく「The value」と言うようになりました。

```java
Yavi._string(c -> c.notBlank()).validate(""); // The value must not be blank
```

### 影響を受けるユーザー

影響を受けるもの:

* 名前が空のバリデータの既定メッセージに対するアサーションや表示: `StringValidatorBuilder.of("", ...)` など。

影響を受けないもの:

* メッセージキーと引数。名前は引き続き `""` です。
* `MessageSource` が独自の形式で整形するメッセージ。
* 名前のある違反（`"tags[0]" must not be blank`）。

### 移行手順

#### 1. 期待するメッセージを更新する

```java
// before
StringValidatorBuilder.of("", c -> c.notBlank()).build().validate("");
// "" must not be blank

// after
StringValidatorBuilder.of("", c -> c.notBlank()).build().validate("");
// The value must not be blank

// after (new API)
Yavi._string(c -> c.notBlank()).validate("");
// The value must not be blank
```

### 関連するプルリクエスト

* [#546](https://github.com/making/yavi/pull/546): 単一の値のバリデータを `Yavi` に追加し、メッセージを変更します。

## null 性アノテーションは JSpecify のものになった

パッケージ `am.ik.yavi.jsr305` は削除されました。YAVI は代わりに [JSpecify](https://jspecify.dev)（`@NullMarked`、`@Nullable`）でアノテーションされています。[NullAway](integrations-nullaway.md#jspecify-annotations)を参照してください。

### 影響を受けるユーザー

影響を受けるもの:

* `am.ik.yavi.jsr305` のアノテーションを import するコード。
* NullAway で検査するコードと Kotlin（2.1 以降は JSpecify アノテーションとの不一致をエラーとして報告します）。YAVI のすべてのパラメータと戻り値の null 性を検査するようになります。

影響を受けないもの:

* その他の Java コード。

### 移行手順

#### 1. JSpecify のアノテーションを import する

YAVI は `org.jspecify:jspecify` を同梱していません。依存関係に追加してください。

```java
// before
import am.ik.yavi.jsr305.Nullable;

// after
import org.jspecify.annotations.Nullable;
```

#### 2. NullAway または Kotlin では、報告された null 性を修正する

* `null` になりうる値は、[必須化の手順 3](#3-with-nullaway-or-kotlin-declare-the-receiver-of-a-nullable-value-as-nullable)で説明したように宣言します。
* Kotlin DSL の検証済みの型は nullable ではありません: `validator<T> { ... }`、`nest`、`forEach` は `T : Any` を宣言します。

NullAway が正しいコードを報告する場合は [制限](integrations-nullaway.md#limitations)を参照してください。

### 関連するプルリクエスト

* [#534](https://github.com/making/yavi/pull/534): JSR 305 を JSpecify に置き換えます。
* [#556](https://github.com/making/yavi/pull/556): `@Nullable` を、`null` が契約の一部である箇所に限定します。

## その他の変更

これらは影響を受けるユーザーが少ない変更です。

### `Validation` はインターフェースになった

`Validation` はインターフェース、`Validated` は sealed インターフェースになりました。`Validation` を継承するクラスは代わりに実装し、`map` / `flatMap` の型パラメータは1つになりました。`Validated` に対して型引数なしで呼ぶコードはそのままコンパイルできます。

```java
// before
class Result<T> extends Validation<String, T> { ... }
Validated<Integer> number = validated.<Integer, Validated<Integer>>map(Integer::valueOf);

// after
class Result<T> implements Validation<String, T> { ... }
Validated<Integer> number = validated.<Integer>map(Integer::valueOf);
```

関連するプルリクエスト: [#537](https://github.com/making/yavi/pull/537)、[#549](https://github.com/making/yavi/pull/549)

### 検証結果の組み合わせは宣言された型を返す

組み合わせの `apply` は呼び出し側が選んだ型を返し、コンパイラはその選択を検査していませんでした。現在は宣言された型を返します:

* `Validated#combine` は新しい `ValidatedCombiningN` を返し、その `apply` は `Validated<R>` を返します。
* `Validation#apply`、`CombiningN#apply`、`Validations.apply` は `Validation<E, R>` を返します。
* `Validation#yieldSuccess` と `yieldFailure` は削除されました。

`Validated` の結果を `combine` で組み合わせ、結果を `Validated` に代入するコードはそのままコンパイルできます。`var` や `switch` でも結果は `Validated` になります。

```java
// before
Validated<Address> address = Validations.apply(Address::new, country, street);
Validated<Address> address = Validations.combine(country, street).apply(Address::new);
Validated<Address> address = country.combine(street).<Address, Validated<Address>>apply(Address::new);

// after
Validated<Address> address = country.combine(street).apply(Address::new);
```

`Validation<ConstraintViolation, T>` から始まる組み合わせは `Validation` を返し、`Validated` には代入できません。最初の値を `Validated.of` で変換します。以前は組み合わせの最後の検証が実行時に型を決め、それが `Validated` でなければ代入が `ClassCastException` で失敗しました。

```java
Validation<ConstraintViolation, Country> country = ...;

// before
Validated<Address> address = country.combine(street).apply(Address::new);

// after
Validated<Address> address = Validated.of(country).combine(street).apply(Address::new);
```

`yieldSuccess` と `yieldFailure` をオーバーライドして独自の型を返していた `Validation` の実装は、`Validated` と同様に `map`、`apply`、`combine` をオーバーライドします。

関連するプルリクエスト: [#558](https://github.com/making/yavi/pull/558)

### 非推奨および冗長なメンバーが削除された

| 削除されたもの | 代わりに使うもの |
| --- | --- |
| `ValueValidator#andThen(Function)`、`ArgumentsNValidator#andThen(Function)` | `map(Function)`。`andThen(ValueValidator)` は影響を受けません。 |
| `new ConstraintViolation(...)` | `ConstraintViolation.builder()` または `ConstraintViolation.of(...)` |
| `new SimpleMessageFormatter()` | `SimpleMessageFormatter.getInstance()` |
| `new Validations()`、`new Functions()` | なし。静的メソッドだけを持ちます。 |
| フィールド `Validation.Success#value` | `value()` |

```java
// before
new ConstraintViolation("name", "notBlank", "\"{0}\" must not be blank", new Object[] { "name", "" },
        new SimpleMessageFormatter(), Locale.ENGLISH);

// after
ConstraintViolation.builder()
        .name("name")
        .messageKey("notBlank")
        .defaultMessageFormat("\"{0}\" must not be blank")
        .args("name", "")
        .locale(Locale.ENGLISH)
        .build();
```

`Validation.Success` と `Validation.Failure` は record です。`toString()` は `Success[value=a]` と `Failure[errors=[e]]` を返します。

関連するプルリクエスト: [#558](https://github.com/making/yavi/pull/558)

### `ArgumentsNValidatorBuilder` のコンストラクタが削除された

```java
// before
new Arguments2ValidatorBuilder<>(nameValidator, ageValidator).apply(Member::new);

// after
Yavi.schema()._string(nameValidator)._integer(ageValidator).apply(Member::new);
```

関連するプルリクエスト: [#538](https://github.com/making/yavi/pull/538)

### スキーマの enum と object は型とともに追加する

型なしで追加したプロパティはマップから読めませんでした。`fromMap` はバリデータの構築時に `IllegalStateException` を投げていました。型なしのメソッドは削除され、スキーマは `apply` と `fromMap` の両方に使えます。

| 削除されたもの | 代わりに使うもの |
| --- | --- |
| スキーマビルダの `_enum(name)`、`_enum(name, constraints)` | `_enum(name, type)`、`_enum(name, type, constraints)` |
| スキーマビルダの `_object(name)`、`_object(name, constraints)` | `_object(name, type)`、`_object(name, type, constraints)`。クラスリテラルのない型には `_object(Yavi.<List<String>>_object(name, constraints))` |
| `EnumValidatorBuilder.of(name, constraints)`、`EnumValidatorBuilder.wrap(builder)` | `EnumValidatorBuilder.of(name, type, constraints)`、`EnumValidatorBuilder.wrap(type, builder)` |
| `new EnumValidator<>(validator, mapper)` | `new EnumValidator<>(validator, mapper, type)` |

```java
// before
Yavi.arguments()
        ._string("name")
        .<Role>_enum("role", c -> c.oneOf(Role.USER, Role.ADMIN))
        .<Address>_object("address")
        .apply(User::new);

// after
Yavi.schema()
        ._string("name")
        ._enum("role", Role.class, c -> c.oneOf(Role.USER, Role.ADMIN))
        ._object("address", Address.class)
        .apply(User::new);
```

このような enum の記述は定数を持つようになり、JSON Schema に `enum` が含まれます。

関連するプルリクエスト: [#565](https://github.com/making/yavi/pull/565)

### `apply` の2つの引数は同じ名前にできない

`apply` と `flatApply` は、`fromMap` と同様に、2つの引数が同じ名前の場合に `IllegalArgumentException` を投げます。バリデータは、1つのプロパティが2つの引数を表すオブジェクトを記述することになるためです。各引数に独自の名前を付けてください。

```java
// before: built a validator whose JSON Schema had one property "value" for both arguments
Yavi.arguments()._integer("value")._integer("value").apply(Range::new);

// after
Yavi.schema()._integer("from")._integer("to").apply(Range::new);
```

関連するプルリクエスト: [#565](https://github.com/making/yavi/pull/565)

### `Yavi.arguments()` は非推奨になり、`YaviArguments` は `YaviSchema` になった

`Yavi.schema()` は `Yavi.arguments()` の新しい名前です。`Yavi.arguments()` は引き続き動作しますが、非推奨です。ビルダは、型付き引数、マップ、JSON ツリー、リクエストパラメータから読み取る入力を記述するようになったため、その型 `YaviArguments` は `YaviSchema` に改名されました。その型の変数を宣言するコードは改名が必要です。[スキーマの定義](defining-a-schema.md)を参照してください。

```java
// before
YaviArguments builder = Yavi.arguments();

// after
YaviSchema builder = Yavi.schema();
```

### `Arguments1` から `Arguments16` は record になった

`toString()` は `Arguments2[arg1=a, arg2=1]` を返します。以前は `Arguments2{arg1=a, arg2=1}` でした。

関連するプルリクエスト: [#549](https://github.com/making/yavi/pull/549)

### `andThen` はサブタイプのバリデータを受け取る

`ValueValidator#andThen` と `ArgumentsNValidator#andThen` は `ValueValidator<? super X, ? extends X2>` を受け取ります。以前は `ValueValidator<? super X, X2>` でした。呼び出しは以前と同じようにコンパイルできます。`andThen` をオーバーライドするクラスは、新しいパラメータの型を宣言します。

```java
// before
@Override
public <X2> ValueValidator<String, X2> andThen(ValueValidator<? super Name, X2> validator) { ... }

// after
@Override
public <X2> ValueValidator<String, X2> andThen(ValueValidator<? super Name, ? extends X2> validator) { ... }
```

関連するプルリクエスト: [#572](https://github.com/making/yavi/pull/572)

### lifted なバリデータはサブタイプのコンテナを受け取る

`ValueValidator` と `Arguments1Validator` の `liftList()`、`liftSet()`、`liftCollection(factory)`、`liftOptional()`、および `ValueValidator` と `ArgumentsValidators` の同じ名前の静的メソッドは、`Iterable<? extends T>` または `Optional<? extends T>` のバリデータを返します。以前は `Iterable<T>` または `Optional<T>` のバリデータを返し、`T` が `Object` の場合に `List<String>` を拒否していました。一度に検証する呼び出しは以前と同じようにコンパイルできます。lifted なバリデータを宣言する変数、パラメータ、戻り値の型は、新しい型か `var` を宣言します。

```java
// before
ValueValidator<Iterable<String>, List<String>> tags = tagValidator.liftList();
Arguments1Validator<Optional<String>, Optional<PhoneNumber>> phone = phoneNumberValidator.liftOptional();

// after
ValueValidator<Iterable<? extends String>, List<String>> tags = tagValidator.liftList();
Arguments1Validator<Optional<? extends String>, Optional<PhoneNumber>> phone = phoneNumberValidator.liftOptional();
```

関連するプルリクエスト: [#576](https://github.com/making/yavi/pull/576)

### 単一の値のビルダの `build()` は逆変換を知るバリデータを返す

`StringValidatorBuilder`、`IntegerValidatorBuilder` などの単一の値のビルダの `build()` は、`StringValidator.Reversible`、`IntegerValidator.Reversible` などを返します。以前は `StringValidator<String>`、`IntegerValidator<Integer>` などを返し、新しい型はそれらを継承するため、古い型を宣言するコードはそのままコンパイルできます。0.17 でコンパイルしたコードは、他の変更と同様に再コンパイルします。

`var` で宣言した変数は新しい型になり、マップしたバリデータは代入できません。型を宣言するか、別の変数を使ってください:

```java
// before
var name = StringValidatorBuilder.of("name", c -> c.notBlank()).build();
name = name.map(String::strip);

// after
StringValidator<String> name = StringValidatorBuilder.of("name", c -> c.notBlank()).build();
name = name.map(String::strip);
```

新しい型は、バリデータを書き戻せることを示します。[オブジェクトの書き戻し](writing-back.md)を参照してください。

関連するプルリクエスト: [#579](https://github.com/making/yavi/pull/579)

### `emoji()` は拡張書記素クラスタを数える

絵文字ではない一部の並びが、2文字ではなく1文字として数えられるようになりました: `"\r\n"`、または濁点付きの半角カタカナ（`ｶﾞ`）。`Emoji` クラスは削除されました。[組み込みの制約](built-in-constraints.md#character-counting-and-byte-size)を参照してください。

関連するプルリクエスト: [#533](https://github.com/making/yavi/pull/533)

### 文字列の形式は空文字列を受け付ける

`CharSequenceConstraint` の `ipv4()`、`ipv6()`、`luhn()`、`isoLocalDate()`、`localDate(pattern)` は `""` を受け付けます。`email()`、`url()`、`uuid()`、`isInteger()` は以前から受け付けていました。0.17 では、空のときに有効かどうかが形式によって異なりました。`""` を拒否する必要がある箇所には `notBlank()` または `notEmpty()` を追加します。[組み込みの制約](built-in-constraints.md#string-and-charsequence)を参照してください。

```java
// before: "" is a violation (charSequence.ipv4)
ValidatorBuilder.<Server>of()._string(Server::address, "address", c -> c.ipv4()).build();

// after: the same result with 0.18
ValidatorBuilder.<Server>of()._string(Server::address, "address", c -> c.notEmpty().ipv4()).build();
```

関連するプルリクエスト: [#573](https://github.com/making/yavi/pull/573)

### メッセージキーの区切り文字は `forEach` の下にも適用される

`.` 以外のメッセージキー区切り文字を持つ `ValidatorBuilder` は、`forEach(..., builder -> ...)` の下の名前にもそれを使います。

```java
new ValidatorBuilder<Order>("/")
        .forEach(Order::addresses, "addresses", b -> b.constraint(Address::street, "street", c -> c.notBlank()))
        .build();
// before: addresses[0].street
// after:  addresses[0]/street
```

関連するプルリクエスト: [#537](https://github.com/making/yavi/pull/537)

### ネストしたバリデータのコンストラクタはパスを受け取る

`NestedValidator`、`NestedCollectionValidator`、`NestedConstraintPredicates` のコンストラクタは、追加の引数としてパスを受け取ります。

関連するプルリクエスト: [#537](https://github.com/making/yavi/pull/537)

### 違反の `toString()` はポインタ、メッセージ、キーになる

`ConstraintViolation#toString()` は `/age: "age" must be less than or equal to 150 (numeric.lessThanOrEqual)` を返し、`ConstraintViolations#toString()` はそのリストを返します。以前は `ConstraintViolation{name='age', messageKey='numeric.lessThanOrEqual', defaultMessageFormat='"{0}" must be less than or equal to {1}', args=[age, 150, 300]}` でした。メッセージが示さない限り、違反した値は表示されなくなりました。テキストを比較するコード（テストのアサーションなど）は、代わりに `messageKey()`、`name()`、`args()` を比較します。

関連するプルリクエスト: [#584](https://github.com/making/yavi/pull/584)

### 文字列の制約は `am.ik.yavi.constraint` にある

`ByteSizeConstraint`、`CodePointsConstraint`、`EmojiConstraint` は `am.ik.yavi.constraint.charsequence` ではなく `am.ik.yavi.constraint` にあります。`CharSequenceConstraint` の流暢なメソッドは以前と同じくそれらを返します。import するコードは別のパッケージを使います。
