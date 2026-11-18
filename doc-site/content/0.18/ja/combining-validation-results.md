# 検証結果の組み合わせ

検証結果は他の検証結果と組み合わせられます。組み合わせは適用可能 (applicative) であり、すべての検証が実行され、すべての違反が蓄積されるため、呼び出し元はすべての失敗を一度に確認できます。

## 適用バリデーション

`Validation<E, T>` は検証結果の型です。`am.ik.yavi.core.Validated<T>` はバリデータが返す `Validation<ConstraintViolation, T>` です。

```java
ApplicativeValidator<Email> emailValidator = validator.applicative();
Validated<Email> validated = emailValidator.validate(email);

if (validated.isValid()) {
    Email value = validated.value();
}
else {
    ConstraintViolations violations = validated.errors();
}
```

0.18 では `Validation` はインタフェースで、`Validated` はレコード `Validated.Valid` と `Validated.Invalid` の sealed インタフェースであるため、結果は網羅的な `switch` で扱えます。

```java
switch (validated) {
    case Validated.Valid<Email>(Email email) -> send(email);
    case Validated.Invalid<Email>(ConstraintViolations violations) ->
        violations.forEach(violation -> report(violation.name(), violation.message()));
}
```

`validated.value()` は結果が無効な場合に、`errors()` は有効な場合に例外を投げます。`orElseThrow` と `throwIfInvalid` は、関数が返す例外を送出します。

```java
Email email = validated.orElseThrow(ConstraintViolationsException::new);
validated.throwIfInvalid(ConstraintViolationsException::new);
```

`fold` は両方の結果に対して 1 つの型の値を作成します。

```java
HttpStatus status = validated.fold(violations -> HttpStatus.BAD_REQUEST, email -> HttpStatus.OK);
```

## 結果のマッピングと検査

`map`、`flatMap`、`mapError`、`mapErrors`、`bimap`、`peek`、`peekErrors` は結果を変換します。0.18 では `map` と `flatMap` の型パラメータは 1 つです。

```java
Validation<ConstraintViolation, String> mapped = validated.map(Email::value);
Validation<String, Email> mappedErrors = validated.mapError(ConstraintViolation::messageKey);
Validation<String, String> bimapped = validated
        .bimap(violations -> List.of("invalid"), Email::value);
```

`Validated.successWith`、`Validated.failureWith`、`Validated.of` は結果を作成します。`Validated.of` は `Validation` を `Validated` に変換し、`ConstraintViolations.of` は `failureWith` の違反を作成します。

```java
Validated<Email> success = Validated.successWith(new Email("yavi@example.com"));
Validated<Email> failure = Validated.failureWith(violations);
Validated<Email> converted = Validated.of(Validation.success(new Email("yavi@example.com")));
```

失敗はメッセージから作成することもできます。たとえば `flatFromMap` や `flatApply` の関数で使います。

```java
record DateRange(LocalDate start, LocalDate end) {

    static Validated<DateRange> of(LocalDate start, LocalDate end) {
        return start.isBefore(end) ? Validated.successWith(new DateRange(start, end))
                : Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"));
    }

}
```

複数の結果は `Validated.sequence`、`Validated.traverse`、`Validated.traverseIndexed`、`Validated.traverseOptional` で組み合わせられます。

```java
Validated<List<Email>> emails = Validated.sequence(List.of(emailValidated, anotherEmailValidated));
Validated<List<Email>> traversed = Validated.traverse(inputs, emailValidator::validate);
```

## Validated の組み合わせ

`Validated#combine` は `ValidatedCombining2` を返し、その `apply` は `Validated` を返します。さらに結果は `combine` で最大 16 個まで追加できます。

```java
Validated<ContactInfo> contactInfo = emailValidated.combine(phoneNumberValidated)
        .apply(ContactInfo::new);
```

両方の結果のすべての違反が蓄積され、いずれかが無効な場合は関数が呼ばれません。

```java
Validated<ContactInfo> invalid = emailValidated.combine(phoneNumberValidated)
        .apply(ContactInfo::new);
// invalid: "email" must not be blank, "email" must be a valid email address,
// "phoneNumber" must match [0-9\-]+
```

`Validations.combine(a, b)` は同じ結果を組み合わせ、その `apply` は `Validation<E, R>` を返します。`Validations.apply(f, a, b)` は関数を直接適用します。0.18 では、これらのメソッドは呼び出し元が選んだ型ではなく、宣言された型を返します。

```java
Validation<ConstraintViolation, Address> address = Validations
        .combine(country, street)
        .apply(Address::new);
```

`Validation` から始まる組み合わせは `Validation` を返し、`Validated` には代入できません。最初の結果を `Validated.of` で変換します。

```java
Validation<ConstraintViolation, Country> country = ...;

Validated<Address> address = Validated.of(country).combine(street).apply(Address::new);
```

## 0.18 の型の変更

0.18 の結果型は 0.17 のものを置き換えます。

* `Validation` はインタフェースです。これを継承したクラスは実装するクラスになり、`Validated` は sealed インタフェースです。
* `Validated#combine` は `ValidatedCombiningN` を返し、その `apply` は `Validated<R>` を返します。
* `Validation#apply`、`CombiningN#apply`、`Validations.apply` は `Validation<E, R>` を返します。
* `Validation#yieldSuccess` と `Validation#yieldFailure` は削除されました。代わりに `map`、`apply`、`combine` を上書きします。
* `Validation#valueNullable()` は非推奨です。`value()` は `null` を保持する成功に対してその `null` を返します。
* `Validated#prefixed(int)` が `indexed(int)` を置き換えます。`Validation.Success` と `Validation.Failure` はレコードで、`Validation.Success#value` は公開フィールドではなくなりました。

`Validated` の結果を `combine` で組み合わせ、その結果を `Validated` に代入するコードはそのままコンパイルできます。
