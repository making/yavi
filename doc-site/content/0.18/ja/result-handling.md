# 結果の扱い

`validate(...)` は `Validated<X>` を返し、生成されたオブジェクトか `ConstraintViolations` のどちらかを持ちます。すべての違反が蓄積されるため、無効な入力は失敗したすべての制約を一度に報告します。

```java
switch (userValidator.validate(map)) {
    case Validated.Valid<User>(User user) -> save(user);
    case Validated.Invalid<User>(ConstraintViolations violations) ->
        violations.forEach(violation -> report(violation.name(), violation.message()));
}
```

## Validated と網羅的な switch

`Validated` はレコード `Valid` と `Invalid` の sealed インターフェースであるため、結果は `default` のない網羅的な `switch` で扱えます。`Arguments1` から `Arguments16` もレコードです（`case Validated.Valid(Arguments2(var name, var age))`）。

switch が便利でない箇所では `isValid()`、`value()`、`errors()` を使えます。失敗した結果の `value()` と、成功した結果の `errors()` は例外を投げます。

失敗した結果は、ポインタ、メッセージ、メッセージキーを含めて読める形で出力されます。違反した値は、メッセージがそれを示さない限り出力されません。

```java
userValidator.validate(Map.of("email", "invalid", "age", 300)).toString();
// Invalid[errors=[/email: "email" must be a valid email address (charSequence.email), ...]]
```

## validated() と ConstraintViolationsException

`validated(...)` はオブジェクトを返し、入力が無効な場合は `ConstraintViolationsException` を投げます。例外は違反を保持します。

```java
try {
    User user = userValidator.validated(map);
}
catch (ConstraintViolationsException e) {
    ConstraintViolations violations = e.violations();
}
```

無効な入力が例外的である境界では `validated(...)` を使います。フォームや API レスポンスは `Validated` 自体を扱います。違反は `violatedValue()` を持ちますが、`ViolationDetail` には含まれません。[違反](violations.md)を参照してください。

## 結果の変換

`map` は成功した値を変換し、失敗を保持します。`flatMap` は別の `Validated` で継続し、両方の違反を蓄積します。

```java
Validated<Order> order = userValidator.validate(map)
        .map(User::toOrder)
        .flatMap(orderService::place);
```

`fold(errorsMapper, mapper)` は両方の場合を 1 つの式で扱い、`bimap`、`mapErrors`、`mapError` は失敗を変換します。`map` と `flatMap` の関数は失敗した結果には適用されません。

## 成功と失敗のレコード

手書きの処理は `Validated.successWith(value)` または `Validated.failureWith(...)` で結果を生成します。失敗は `ViolationMessage` から構築します。

```java
return Validated.failureWith(
        ViolationMessage.of("order.outOfStock", "\"{0}\" exceeds the stock of {1}", "stock"),
        quantity, locale, stock);
```

`Validated.failureWith(message, args...)` と `ConstraintViolation.of(message, args...)` は、違反した値を持たない `Locale.getDefault()` の違反を生成します。`flatFromMap` や `flatApply` に渡す関数にはロケールが渡されません。返された違反は、他の箇所で `localizedWith(locale)` がするのと同じく、検証のロケールで報告されます。

## スレッドセーフ性

`Yavi.schema()`、`fromMap`、`apply` で構築したバリデータは不変で、共有して並行に再利用できます。`Validated`、`ConstraintViolations`、`ConstraintViolation` は不変の値であり、返されるコレクションは変更できません。`MessageFormatter` は検証中とメッセージの整形時に呼ばれるため、バリデータを共有する場合はスレッドセーフでなければなりません。可変状態を捕捉するラムダのバリデータは、それ自体ではスレッドセーフではありません。
