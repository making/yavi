# Bean Validation との比較

[Bean Validation](https://beanvalidation.org/) は制約をアノテーションとしてクラスに宣言し、既に存在するオブジェクトを検証します。YAVI は同じ制約を Java のメソッド呼び出しで表現します。このページではアノテーションを制約 API に対応づけ、違いを一覧します。

## 基本

Bean Validation のフォームオブジェクト:

```java
public class UserForm {

  @NotNull
  @Size(min = 1, max = 20)
  private String name;

  @NotNull
  @Size(min = 1, max = 50)
  @Email
  private String email;

  @NotNull
  @Min(0)
  @Max(200)
  private Integer age;

  // omitted setter/getter

}
```

対応するコードは `ValidatorBuilder` でオブジェクトを検証します:

```java
Validator<UserForm> validator = ValidatorBuilder.<UserForm>of()
        .constraint(UserForm::getName, "name", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(20))
        .constraint(UserForm::getEmail, "email", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(50).email())
        .constraint(UserForm::getAge, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200))
        .build();
```

Bean Validation と `ValidatorBuilder` はどちらも、フレームワークやデシリアライザなど別のコードが作成したオブジェクトを検証するため、互いに対応します。0.18 の主要なスタイルは異なります。`Yavi.schema()` はまだ存在しない入力のプロパティを記述し、すべてのプロパティが有効な場合にだけ型付きオブジェクトを作成します。従来の API は [オブジェクトの検証](using-yavi.md)、スキーマ中心の API は [はじめる](getting-started.md) を参照してください。

## 制約

各アノテーションはプロパティの制約メソッドに対応します。`@Min` と `@Max` は境界を含むため、`greaterThanOrEqual` と `lessThanOrEqual` に対応します。境界を含まない形式は `inclusive = false` が指定された場合にだけ使います。

### `@NotNull`

```java
.constraint(Foo::getText, "text", c -> c.notNull())
```

### `@Null`

```java
.constraint(Foo::getText, "text", c -> c.isNull())
```

### `@Pattern`

```java
.constraint(Foo::getText, "text", c -> c.pattern("[0-9]+"))
```

### `@Email`

```java
.constraint(Foo::getEmail, "email", c -> c.email())
```

### `@URL`

```java
.constraint(Foo::getUrl, "url", c -> c.url())
```

`url(url -> url.protocol("https").host("example.com").port(8443).build())` はプロトコル、ホスト、ポートを制限します。`c.uri()` は `mailto:` や `s3://` など任意のスキームの URI を受け付けます。

### `@Min`

```java
.constraint(Foo::getIntValue, "intValue", c -> c.greaterThanOrEqual(1))
.constraint(Foo::getLongValue, "longValue", c -> c.greaterThanOrEqual(1L))
```

`@Min(value = 1, inclusive = false)` は `c.greaterThan(1)` に対応します。

### `@Max`

```java
.constraint(Foo::getIntValue, "intValue", c -> c.lessThanOrEqual(100))
.constraint(Foo::getLongValue, "longValue", c -> c.lessThanOrEqual(100L))
```

`@Max(value = 100, inclusive = false)` は `c.lessThan(100)` に対応します。

### `@Positive`

```java
.constraint(Foo::getIntValue, "intValue", c -> c.greaterThan(0))
```

### `@PositiveOrZero`

```java
.constraint(Foo::getIntValue, "intValue", c -> c.greaterThanOrEqual(0))
```

### `@Negative`

```java
.constraint(Foo::getIntValue, "intValue", c -> c.lessThan(0))
```

### `@NegativeOrZero`

```java
.constraint(Foo::getIntValue, "intValue", c -> c.lessThanOrEqual(0))
```

### `@DecimalMin`

```java
.constraint(Foo::getBigDecimalValue, "bigDecimalValue", c -> c.greaterThanOrEqual(new BigDecimal("1")))
```

`@DecimalMin(value = "1", inclusive = false)` は `c.greaterThan(new BigDecimal("1"))` に対応します。

### `@DecimalMax`

```java
.constraint(Foo::getBigDecimalValue, "bigDecimalValue", c -> c.lessThanOrEqual(new BigDecimal("100")))
```

`@DecimalMax(value = "100", inclusive = false)` は `c.lessThan(new BigDecimal("100"))` に対応します。

### `@Size`

```java
.constraint(Foo::getText, "text", c -> c.greaterThanOrEqual(4).lessThanOrEqual(64))
.constraint(Foo::getTexts, "texts", c -> c.greaterThanOrEqual(1).lessThanOrEqual(8))
```

同じメソッドを文字列、コレクション、配列、Map に適用できます。境界は含みます。

### `@NotEmpty`

```java
.constraint(Foo::getTexts, "texts", c -> c.notEmpty())
```

### `@NotBlank`

```java
.constraint(Foo::getText, "text", c -> c.notBlank())
```

### `@Digits`

```java
.constraint(Foo::getPrice, "price", c -> c.integerDigits(6).fractionDigits(2))
```

この制約は `BigDecimal` で利用できます。小数部の末尾のゼロは数えないため、`1.500` の小数桁数は 1 です。

### `@AssertTrue`

```java
.constraint(Foo::getBooleanValue, "booleanValue", c -> c.isTrue())
```

### `@AssertFalse`

```java
.constraint(Foo::getBooleanValue, "booleanValue", c -> c.isFalse())
```

### `@Future`

```java
.constraint(Event::getStartsAt, "startsAt", c -> c.future())
```

### `@Past`

```java
Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));
Validator<Event> validator = ValidatorBuilder.<Event>of()
        .constraint(Event::getCreatedAt, "createdAt", c -> c.past(clock))
        .build();
```

`past()` はシステムの既定のタイムゾーンの現在時刻と比較します。`past(Clock)` は指定された `Clock` から現在時刻を読むため、`Clock.fixed` を使ったテストは実行のたびに同じ結果になります。

### `@FutureOrPresent`

```java
.constraint(Event::getStartsAt, "startsAt", c -> c.futureOrPresent())
```

### `@PastOrPresent`

```java
.constraint(Event::getCreatedAt, "createdAt", c -> c.pastOrPresent())
```

時間の制約は `Instant`、`LocalDate`、`LocalDateTime`、`LocalTime`、`OffsetDateTime`、`ZonedDateTime`、`Year`、`YearMonth` で利用できます。メッセージキーは `temporal.future`、`temporal.past`、`temporal.futureOrPresent`、`temporal.pastOrPresent` です。

## `@Valid`

### 入れ子のオブジェクト

```java
public class OrderForm {

  @NotNull
  @Valid
  private AddressForm address;

  // omitted setter/getter

}
```

次に:

```java
Validator<AddressForm> addressValidator = ValidatorBuilder.<AddressForm>of()
        .constraint(AddressForm::getZipCode, "zipCode", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(10))
        .constraint(AddressForm::getAddress, "address", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(100))
        .build();

Validator<OrderForm> validator = ValidatorBuilder.<OrderForm>of()
        .nest(OrderForm::getAddress, "address", addressValidator)
        .build();
```

入れ子のオブジェクトの違反は `address.zipCode` のようにその名前の下に配置されます。`nestIfPresent` は入れ子のオブジェクトが `null` でない場合にだけ検証します。スキーマでは同じオブジェクトを `_object` で追加します。[オブジェクトとコレクション](objects-and-collections.md) を参照してください。

### 入れ子のリスト、配列、Map

```java
public class OrderForm {

  @Valid
  private List<AddressForm> addresses;

  // omitted setter/getter

}
```

次に:

```java
Validator<OrderForm> validator = ValidatorBuilder.<OrderForm>of()
        .forEach(OrderForm::getAddresses, "addresses", addressValidator)
        .build();
```

`forEach` はリスト、配列、Map の値の各要素にバリデータを適用し、`forEachIfPresent` は `null` のコレクションを飛ばします。違反はインデックスの下に配置されます (`addresses[0].zipCode`)。スキーマでは代わりに `_list`、`_map`、入れ子の `_object` を使います。

## カスタム制約

Bean Validation の `ConstraintValidator`:

```java
public class UpperCaseValidator implements ConstraintValidator<UpperCase, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || value.equals(value.toUpperCase());
  }

}
```

対応するものは `CustomConstraint` で、述語をメッセージキーと既定のメッセージとともにまとめます:

```java
public enum UpperCaseConstraint implements CustomConstraint<String> {

    SINGLETON;

    @Override
    public boolean test(String value) {
        return value.equals(value.toUpperCase());
    }

    @Override
    public String messageKey() {
        return "custom.upperCase";
    }

    @Override
    public String defaultMessageFormat() {
        return "\"{0}\" must be upper case";
    }

}
```

```java
Validator<Foo> validator = ValidatorBuilder.<Foo>of()
        .constraint(Foo::getText, "text", c -> c.predicate(UpperCaseConstraint.SINGLETON))
        .build();
```

一度だけ使う規則は `c.predicate(predicate, messageKey, defaultMessageFormat)` でインラインに書けます。[カスタム制約](custom-constraints.md) を参照してください。

## 違い

* メタデータ層もアノテーションもありません。制約は Java の式であるため、コンパイラとリファクタリングツールがそれを認識します。
* Bean Validation の `Validation.buildDefaultValidatorFactory()` やプロバイダの検索はありません。バリデータは `ValidatorBuilder` または `Yavi.schema()` で構築します。
* 制約はコンパイラが検査します。誤った型のメソッド参照や制約メソッドはコンパイルできませんが、誤った型のフィールドへのアノテーションは実行時にしか分かりません。
* グループと条件付き検証は `ValidatorBuilder` の `constraintOnGroup` と `constraintOnCondition` で利用できます。[オブジェクトの検証](using-yavi.md) を参照してください。
* 違反は構造化された `path()`、JSON の `pointer()`、`messageKey()` を持つため、API がそのまま返せます。[違反](violations.md) を参照してください。
* nullness は JSpecify (`@Nullable`) で宣言し、NullAway などの nullness チェッカが検証済みの値の使用を検査します。[NullAway](integrations-nullaway.md) を参照してください。
* スキーマ中心の入力検証が 0.18 の主要な用途です。`Yavi.schema()` は Map、JSON ツリー、リクエストパラメータを読み取り、すべてのプロパティが有効な場合にだけオブジェクトを作成します。`Yavi.arguments()` はこれに代わるものとして非推奨です。従来の `ValidatorBuilder` API は非推奨ではありません。
