# 型付き引数

`apply(f)` はスキーマを、すでに型に束縛された値のための型付き引数のバリデータで終えます。結果は `ArgumentsNValidator` で、引数を与えて呼び出し、すべての引数が有効な場合にだけオブジェクトを生成します。

```java
Arguments3Validator<String, String, Integer, Car> carValidator = Yavi.schema()
        ._string("manufacturer", c -> c.notBlank())
        ._string("licensePlate", c -> c.greaterThanOrEqual(2).lessThanOrEqual(14))
        ._integer("seatCount", c -> c.greaterThanOrEqual(2))
        .apply(Car::new);

Validated<Car> validated = carValidator.validate("Morris", "DD-AB-123", 4);
```

## apply でスキーマを終える

プロパティは `fromMap` と同じように、同じ制約と段階付けされた型で宣言します。その名前は、オブジェクトを Map から読む場合と同じく、違反、`description()`、JSON Schema で使われます。

* 各引数は独自の名前を持ちます。`apply` と `flatApply` は、2 つの引数が同じ名前を持つと `IllegalArgumentException` を投げます。
* すべての違反が蓄積されるため、無効な呼び出しは失敗したすべての制約を一度に報告します。
* `null` の引数は `null` として検証され、存在しない状態はありません。`nullable()` は引数の型を `@Nullable` に変えます。
* `validated(...)` はオブジェクトを返し、無効な呼び出しでは `ConstraintViolationsException` を投げます。
* バリデータは不変でスレッドセーフです。

## フラットな生成

`flatApply(f)` は `Validated` を返し失敗しうる関数、たとえばドメインのファクトリメソッドを取ります。

```java
Arguments2Validator<LocalDate, LocalDate, DateRange> validator = Yavi.schema()
        ._localDate("start")
        ._localDate("end")
        .flatApply(DateRange::of);
```

関数はすべての引数が有効な場合にだけ適用されます。検証のロケールも違反した値も渡されません。返された違反は検証のロケールで報告されます。失敗させるには `Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"))` を使います。

## fromMap との関係

`fromMap` と `apply` は同じプロパティを記述します。違いは入力です。

| 入力 | 終わり方 | 型 |
| --- | --- | --- |
| `Map<String, Object>`、JSON ツリー、リクエストパラメータ | `fromMap(f)` | `ObjectInputValidator<X>` |
| すでに型を持つ値 | `apply(f)` | `ArgumentsNValidator<A1, ..., AN, X>` |

呼び出し元が値をすでに Java オブジェクトとして持つ場合（複数のパラメータを取るメソッドなど）は `apply` を、入力が型なしの場合は `fromMap` を使います。同じスキーマを両方の終わり方で同時に使うことはできません。同じプロパティ定義から 2 つのバリデータを構築します。

## 埋め込み引数と残りの引数

`_embedded` で追加した引数は、与えられたバリデータで検証される `Map<String, Object>` です。`_rest` で追加した引数は、他のどの引数にも定義されていないキーの Map です。どちらも `apply` に渡す関数は検証済みの Map を受け取ります。

```java
Arguments2Validator<Map<String, Object>, Map<String, String>, Account> validator = Yavi.schema()
        ._embedded(userNameValidator)
        ._rest(Yavi._string(c -> c.notBlank()))
        .apply(Account::new);
```

`optional()`、`nullable()`、`defaultValue(...)` を付けた `_embedded` のバリデータは、どのプロパティにも値がない場合に `null` を受け取ります。`_rest` は最後の引数です。

## 引数の数と ObjectInputValidator.of

スキーマの引数は最大 16 個です。より多くのキーを読む方法は埋め込みです。`_embedded` は別のバリデータのプロパティを追加し、同じ Map を読み取ります。

引数を動的に構築する場合は `ObjectInputValidator.of(parts, mapper)` を使います。パートは `Part.Property(name, validator)`、`Part.Embedded(validator)`、`Part.Rest(validator)` で、mapper はパートの順に検証済みの値を受け取ります。

```java
ObjectInputValidator<Pair> validator = ObjectInputValidator.of(List.of(
        new ObjectInputValidator.Part.Property<>("first", Yavi._string("first", c -> c.notBlank()).untyped()),
        new ObjectInputValidator.Part.Property<>("second", Yavi._string("second", c -> c.notBlank()).untyped())),
        values -> new Pair((String) values.get(0), (String) values.get(1)));
```

`ObjectInputValidator.ofReversible(parts, mapper)` は `ReversiblePart`、すなわちパートとその逆変換およびアクセサを取ります。
