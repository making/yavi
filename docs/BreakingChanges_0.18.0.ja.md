# 0.18.0 の破壊的変更

[English](BreakingChanges_0.18.0.md)

YAVI 0.18.0 には、0.17 に対して次の破壊的変更があります。影響を受けるユーザーが多い順に並べています。

* [Java 21 が必須](#java-21-が必須)
* [引数と単一の値のバリデータはデフォルトで `null` を拒否する](#引数と単一の値のバリデータはデフォルトで-null-を拒否する)
* [`ViolationDetail` は record になった](#violationdetail-は-record-になった)
* [liftList などの違反名はインデックスが先になる](#liftlist-などの違反名はインデックスが先になる)
* [名前のない値のメッセージ](#名前のない値のメッセージ)
* [null 性アノテーションは JSpecify のものになった](#null-性アノテーションは-jspecify-のものになった)
* [その他の変更](#その他の変更)

コード例の「before」は 0.17 向けに書かれたコードです。「after」は、同じコードを 0.18 でコンパイルでき、同じ動作になるように直したものです。「after (new API)」は 0.18 で追加された API を使ったものです。新しい API は [0.18.0 の新機能](NewFeatures_0.18.0.ja.md) で説明しています。

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

`Yavi.arguments()` の引数と、単一の値のバリデータ（`StringValidatorBuilder` など）は、`notNull()` がなくても `null` を拒否するようになりました。`null` になりうる値には、0.18.0 で追加された `nullable()` を付けます。

```java
record Member(String name, @Nullable String nickname) {}

Arguments2Validator<String, @Nullable String, Member> validator = Yavi.arguments()
        ._string("name", c -> c.lessThanOrEqual(50))                // null は有効だったが、違反になった
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable() // null は有効
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
          .constraint(Car::seatCount, "seatCount", c -> c.greaterThanOrEqual(2)) // null は引き続き有効
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
Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable()
        .apply(Member::new);

// after (new API)
// optional() と defaultValue(...) も null を受け付けるが、バリデータが返す値は null にならない
Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).optional() // Optional<String>
        .apply(Member::new);

Yavi.arguments()
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

#### 2. `isNull()` で制約した値に `nullable()` を付ける

付けなければ、どの値も有効になりません。

```java
// before
Yavi.arguments()._string("id", c -> c.isNull())

// after
Yavi.arguments()._string("id", c -> c.isNull()).nullable()
```

#### 3. NullAway または Kotlin では null を受け取る側を nullable と宣言する

null 性チェッカーで検査していない Java コードは、この手順なしでコンパイルできます。

* `apply` に渡すコンストラクタや関数のパラメータのうち、`nullable()` の引数を受け取るもの: `@Nullable String`、Kotlin では `String?`。それ以外のパラメータは `null` を受け付ける必要がなくなりました。
* 宣言したバリデータの型引数: `ValueValidator<String, @Nullable String>`、`Validated<@Nullable String>`。
* `ValueValidator.passThrough()`: `ValueValidator<X, @Nullable X>`。

```kotlin
data class Member(val name: String, val nickname: String?)

Yavi.arguments()
    ._string("name") { it.notBlank() }
    ._string("nickname") { it.lessThanOrEqual(20) }.nullable()
    .apply(::Member)
```

`@Nullable` と宣言されていないパラメータを、NullAway は次のように報告します。

```
[NullAway] parameter age of referenced method is @NonNull, but parameter in functional interface method am.ik.yavi.fn.Function2.apply(String,@Nullable Integer) is @Nullable
```

NullAway が正しいコードを報告する場合は、[NullAway の制限事項](NullAwayLimitations.md) を参照してください。

#### 4. 任意の整理

以下はどれも必須ではありません。変更前のコードはそのまま動き、動作も変わりません。

* `null` を拒否させるためだけに付けた `notNull()` は削除できます。`null` の違反に別のメッセージを付けたい場合は残してください。

  ```java
  // before
  Yavi.arguments()
          ._string("name", c -> c.notNull())
          ._integer("age", c -> c.notNull().greaterThanOrEqual(0))
          .apply(Member::new);

  // after
  Yavi.arguments()
          ._string("name")
          ._integer("age", c -> c.greaterThanOrEqual(0))
          .apply(Member::new);
  ```

* `valueNullable()` は非推奨になりました。`value()` は、`null` を保持する成功の場合も含めて同じ値を返します。

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
| 制約が `null` を拒否しない引数や単一の値に対する `null` | 有効。値は `null` | `object.notNull` の違反 |
| `null` を拒否する制約（`notBlank()`、`notNull()`）を持つ値に対する `null` | その制約の違反 | 変更なし |
| 引数や単一の値に対する `isNull()` | `null` だけを受け付ける | `nullable()` がなければ、どの値も有効にならない |
| `null` を保持する成功に対する `Validation.Success#value()` | `NoSuchElementException` を投げる | `null` を返す。`valueNullable()` は非推奨 |

型:

| | 変更前 | 変更後 |
| --- | --- | --- |
| `apply` に渡す関数のパラメータ | すべての引数が `@Nullable` | `nullable()` の引数だけが `@Nullable` |
| `ValueValidator.passThrough()` | `ValueValidator<X, X>` | `ValueValidator<X, @Nullable X>` |

型の変更はアノテーションだけなので、バイナリ互換です。null 性チェッカーで検査していない Java コードにはソース互換でもあります。

### 理由

値が `null` を受け付けるかどうかは、制約のラムダの中（`c -> c.notNull()`）で決まっていました。`notNull()` の有無で値の型は変わらなかったため、`notNull()` の書き忘れをコンパイラも null 性チェッカーも検出できませんでした。

```java
record Member(String name, int age) {}

Arguments2Validator<String, Integer, Member> validator = Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._integer("age", c -> c.greaterThanOrEqual(0)) // notNull() がない
        .apply(Member::new);

validator.validate("Taro", null); // 変更前: validate から NullPointerException が投げられる
```

[NullAway](https://github.com/uber/NullAway) は型だけで null 性を検査するので、この二つの場合を区別できませんでした。`apply` に渡す関数のパラメータはすべての引数について `@Nullable` であり、`notNull()` で検証した値に対しても `apply(Member::new)` が報告されていました。

0.18.0 では、値は明示しない限り `null` を拒否し、`null` を許すと明示すれば型が変わります。これは Zod や Raoh と同じ考え方です。値が `null` になるのは型がそう示している箇所だけなので、NullAway と Kotlin で検査できます。

### 関連するプルリクエスト

* [#557](https://github.com/making/yavi/pull/557): 引数と単一の値のバリデータがデフォルトで `null` を拒否するようにし、`nullable()` で型が変わるようにします。
* [#556](https://github.com/making/yavi/pull/556): その前提となる変更です。`apply` に渡す関数は、検証を通った値を `@Nullable` でないパラメータで受け取ります。

## `ViolationDetail` は record になった

`ConstraintViolations#details()` は、どの値が失敗したかを示す record を返します。アクセサの名前が変わり、JSON ライブラリがそこから出力する JSON のプロパティ名も変わりました。

```java
List<ViolationDetail> details = violations.details();
// [{"name": "items[1].name", "pointer": "/items/1/name", "messageKey": "charSequence.notBlank",
//   "message": "\"items[1].name\" must not be blank", "args": ["items[1].name", ""]}]
```

### 影響を受けるユーザー

影響を受けるもの:

* `ViolationDetail` の `getKey()`、`getArgs()`、`getDefaultMessage()`、またはコンストラクタを呼ぶコード。
* `details()` を JSON として返す API のクライアント。
* `details()` が返すリストを変更するコード。

影響を受けないもの:

* `ConstraintViolation` の `name()`、`messageKey()`、`args()`、`message()`。

### 移行手順

#### 1. アクセサの名前を変える

```java
// before
for (ViolationDetail detail : violations.details()) {
    String key = detail.getKey();
    Object[] args = detail.getArgs();
    String message = detail.getDefaultMessage();
}

// after
for (ViolationDetail detail : violations.details()) {
    String key = detail.messageKey();
    List<Object> args = detail.args();
    String message = detail.message();
}

// after (new API)
for (ViolationDetail detail : violations.details()) {
    String name = detail.name();       // items[1].name
    String pointer = detail.pointer(); // /items/1/name (JSON Pointer)
}
```

#### 2. クライアントを新しい JSON に合わせるか 0.17 の JSON を維持する

```jsonc
// before
[{"key": "charSequence.notBlank", "args": ["items[1].name", ""], "defaultMessage": "\"items[1].name\" must not be blank"}]

// after
[{"name": "items[1].name", "pointer": "/items/1/name", "messageKey": "charSequence.notBlank",
  "message": "\"items[1].name\" must not be blank", "args": ["items[1].name", ""]}]
```

0.17 の JSON を維持するには、独自の型に詰め替えます。

```java
record LegacyDetail(String key, List<Object> args, String defaultMessage) {}

List<LegacyDetail> details = violations.details()
        .stream()
        .map(detail -> new LegacyDetail(detail.messageKey(), detail.args(), detail.message()))
        .toList();
```

### すべての変更

| | 変更前 | 変更後 |
| --- | --- | --- |
| メッセージキー | `getKey()`、JSON では `key` | `messageKey()`、JSON では `messageKey` |
| フォーマット済みのメッセージ | `getDefaultMessage()`、JSON では `defaultMessage` | `message()`、JSON では `message` |
| 引数 | `Object[] getArgs()` | `List<Object> args()`。変更不可 |
| 値の名前と JSON Pointer | なし | `name()`、`pointer()` |
| コンストラクタ | `ViolationDetail(key, args, defaultMessage)` | `ViolationDetail(name, pointer, messageKey, message, args)` |
| `details()` が返すリスト | 変更可 | 変更不可 |

### 理由

これまでの `ViolationDetail` からは、どの値が失敗したかがわかりませんでした。名前は `args` の先頭要素に入っているだけでした。また、`defaultMessage` はデフォルトのメッセージではなく、フォーマット済みのメッセージでした。

### 関連するプルリクエスト

* [#551](https://github.com/making/yavi/pull/551): `ViolationDetail` を record にし、`messagesByName()`、`messagesByPointer()`、`pointer()` を追加します。

## liftList などの違反名はインデックスが先になる

`liftList()`、`liftSet()`、`liftCollection()` は、要素のインデックスを要素のバリデータの名前の前に置くようになりました。これまでは名前の後ろに置いていました。

```java
Yavi._string("tag", c -> c.notBlank())
        .liftList()
        .validate(List.of("java", "")); // [1].tag (これまでは tag[1])
```

コードはそのままコンパイルできますが、違反の名前が変わります。

### 影響を受けるユーザー

影響を受けるもの:

* 要素のバリデータに名前があり、lift したバリデータの違反を名前で探すコード（フォームのフィールドへの対応づけ、テストのアサーションなど）。
* `indexed(int)` を呼ぶコード。

影響を受けないもの:

* `ValidatorBuilder#forEach`。
* 要素の名前が空の lift したバリデータ。これまでどおり `[1]` を報告します。

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

要素がオブジェクトの場合、プロパティの名前はそのまま残ります。`prefixed` を付けると、`ValidatorBuilder#forEach` の要素と同じ名前になります。

```java
// before
addressValidator.applicative().liftList().validate(addresses); // street[1]

// after
addressValidator.applicative().liftList().prefixed("addresses").validate(addresses); // addresses[1].street
```

#### 2. `indexed(int)` を `prefixed(int)` に置き換える

`indexed(int)` は `ConstraintViolation`、`Validated`、`ValueValidator`、`Arguments1Validator` から削除されました。`ConstraintViolation` と `Validated` の `prefixed(int)` は、インデックスを名前の前に置きます。

```java
// before
validated.indexed(1); // tag[1]

// after
validated.prefixed(1); // [1].tag
```

### 理由

これまでの名前は入力の構造に対応していませんでした。2 番目の住所の `street` プロパティが `street[1]` となり、「`street` の 2 番目の要素」と読めてしまいます。

### 関連するプルリクエスト

* [#544](https://github.com/making/yavi/pull/544): lift した要素の違反をインデックスの下に位置づけ、`indexed(int)` を削除します。

## 名前のない値のメッセージ

名前が空の違反のメッセージは、引用符付きの空の名前の代わりに「The value」と表示するようになりました。

```java
Yavi._string(c -> c.notBlank()).validate(""); // The value must not be blank
```

### 影響を受けるユーザー

影響を受けるもの:

* 空の名前を持つバリデータ（`StringValidatorBuilder.of("", ...)` など）のデフォルトメッセージを、アサーションで検証したり画面に表示したりするコード。

影響を受けないもの:

* メッセージキーと引数。名前は `""` のままです。
* `MessageSource` が独自のフォーマットで組み立てるメッセージ。
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

`am.ik.yavi.jsr305` パッケージは削除されました。YAVI には、代わりに [JSpecify](https://jspecify.dev) のアノテーション（`@NullMarked`、`@Nullable`）が付いています。

### 影響を受けるユーザー

影響を受けるもの:

* `am.ik.yavi.jsr305` のアノテーションを import しているコード。
* NullAway で検査するコードと Kotlin のコード。YAVI のすべてのパラメータと戻り値の null 性が検査されるようになりました。Kotlin 2.1 以降は、JSpecify アノテーションとの不一致をエラーとして報告します。

影響を受けないもの:

* それ以外の Java コード。

### 移行手順

#### 1. JSpecify のアノテーションを import する

YAVI は `org.jspecify:jspecify` を推移的な依存として提供しません。自分の依存関係に追加してください。

```java
// before
import am.ik.yavi.jsr305.Nullable;

// after
import org.jspecify.annotations.Nullable;
```

#### 2. NullAway または Kotlin では報告された null 性を直す

* `null` になりうる値は、[`null` を拒否するデフォルトの手順 3](#3-nullaway-または-kotlin-では-null-を受け取る側を-nullable-と宣言する) のとおりに宣言します。
* Kotlin DSL で検証する型は nullable にできません。`validator<T> { ... }`、`nest`、`forEach` は `T : Any` を宣言しています。

NullAway が正しいコードを報告する場合は、[NullAway の制限事項](NullAwayLimitations.md) を参照してください。

### 関連するプルリクエスト

* [#534](https://github.com/making/yavi/pull/534): JSR 305 を JSpecify に置き換えます。
* [#556](https://github.com/making/yavi/pull/556): `@Nullable` を、`null` が契約の一部である箇所だけに減らします。

## その他の変更

以下は、影響を受けるユーザーが少ない変更です。

### `Validation` はインターフェースになった

`Validation` はインターフェースに、`Validated` は sealed インターフェースになりました。`Validation` を継承していたクラスは、代わりに実装します。`map` と `flatMap` の型パラメータは 1 つになりました。`Validated` に対して型引数なしで呼んでいるコードは、そのままコンパイルできます。

```java
// before
class Result<T> extends Validation<String, T> { ... }
Validated<Integer> number = validated.<Integer, Validated<Integer>>map(Integer::valueOf);

// after
class Result<T> implements Validation<String, T> { ... }
Validated<Integer> number = validated.<Integer>map(Integer::valueOf);
```

関連するプルリクエスト: [#537](https://github.com/making/yavi/pull/537)、[#549](https://github.com/making/yavi/pull/549)

### バリデーション結果の組み合わせは宣言どおりの型を返す

組み合わせの `apply` は、呼び出し側が選んだ型を返していました。その選択をコンパイラは検査していませんでした。これからは宣言どおりの型を返します。

* `Validated#combine` は新しい `ValidatedCombiningN` を返します。その `apply` は `Validated<R>` を返します。
* `Validation#apply`、`CombiningN#apply`、`Validations.apply` は `Validation<E, R>` を返します。
* `Validation#yieldSuccess` と `yieldFailure` は削除されました。

`Validated` の結果を `combine` で組み合わせて `Validated` に代入しているコードは、そのままコンパイルできます。`var` や `switch` でも、結果は `Validated` になりました。

```java
// before
Validated<Address> address = Validations.apply(Address::new, country, street);
Validated<Address> address = Validations.combine(country, street).apply(Address::new);
Validated<Address> address = country.combine(street).<Address, Validated<Address>>apply(Address::new);

// after
Validated<Address> address = country.combine(street).apply(Address::new);
```

`Validation<ConstraintViolation, T>` から始まる組み合わせは `Validation` を返すので、`Validated` には代入できません。先頭を `Validated.of` で変換してください。これまでは、組み合わせの最後のバリデーション結果が実行時に型を決めていました。それが `Validated` でなければ、代入は `ClassCastException` で失敗していました。

```java
Validation<ConstraintViolation, Country> country = ...;

// before
Validated<Address> address = country.combine(street).apply(Address::new);

// after
Validated<Address> address = Validated.of(country).combine(street).apply(Address::new);
```

`yieldSuccess` と `yieldFailure` をオーバーライドして独自の型を返していた `Validation` の実装は、`Validated` と同じように、代わりに `map`、`apply`、`combine` をオーバーライドします。

関連するプルリクエスト: [#PRNUM](https://github.com/making/yavi/pull/PRNUM)

### 非推奨のメンバーと冗長なメンバーは削除された

| 削除されたもの | 代わりに使うもの |
| --- | --- |
| `ValueValidator#andThen(Function)`、`ArgumentsNValidator#andThen(Function)` | `map(Function)`。`andThen(ValueValidator)` は影響を受けません。 |
| `new ConstraintViolation(...)` | `ConstraintViolation.builder()` または `ConstraintViolation.of(...)` |
| `new SimpleMessageFormatter()` | `SimpleMessageFormatter.getInstance()` |
| `new Validations()`、`new Functions()` | なし。どちらも static メソッドだけを持ちます。 |
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

`Validation.Success` と `Validation.Failure` は record になりました。`toString()` は `Success[value=a]` や `Failure[errors=[e]]` を返します。

関連するプルリクエスト: [#PRNUM](https://github.com/making/yavi/pull/PRNUM)

### `ArgumentsNValidatorBuilder` のコンストラクタは削除された

```java
// before
new Arguments2ValidatorBuilder<>(nameValidator, ageValidator).apply(Member::new);

// after
Yavi.arguments()._string(nameValidator)._integer(ageValidator).apply(Member::new);
```

関連するプルリクエスト: [#538](https://github.com/making/yavi/pull/538)

### `Arguments1` から `Arguments16` は record になった

`toString()` は `Arguments2[arg1=a, arg2=1]` を返します。これまでは `Arguments2{arg1=a, arg2=1}` でした。

関連するプルリクエスト: [#549](https://github.com/making/yavi/pull/549)

### `emoji()` は拡張書記素クラスタを数える

絵文字ではない一部の並びが、2 ではなく 1 と数えられるようになりました。`"\r\n"` や、濁点付きの半角カタカナ（`ｶﾞ`）が該当します。`Emoji` クラスは削除されました。

関連するプルリクエスト: [#533](https://github.com/making/yavi/pull/533)

### メッセージキーのセパレータは `forEach` の下にも適用される

`.` 以外のメッセージキーのセパレータを指定した `ValidatorBuilder` は、`forEach(..., builder -> ...)` の下の名前にもそのセパレータを使うようになりました。

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
