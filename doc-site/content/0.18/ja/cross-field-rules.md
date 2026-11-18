# オブジェクト全体の規則

複数のプロパティにまたがる規則があります。このページでは `constraintOnTarget`、失敗しうるファクトリ、そのような違反の位置づけと蓄積を説明します。

## 生成されたオブジェクトに対する規則

`constraintOnTarget(predicate, name, violationMessage)` は、生成されたオブジェクトが満たすべき規則を追加します。`fromMap` が返すバリデータと、単一値のバリデータで利用できます。

```java
ObjectInputValidator<PasswordForm> passwordFormValidator = Yavi.schema()
        ._string("currentPassword", c -> c.notBlank())
        ._string("newPassword", c -> c.greaterThanOrEqual(8))
        ._string("confirmPassword", c -> c.notBlank())
        .fromMap(PasswordForm::new)
        .constraintOnTarget(form -> form.newPassword().equals(form.confirm()), "confirmPassword",
                ViolationMessage.of("password.mismatch", "\"{0}\" must match \"newPassword\""))
        .constraintOnTarget(form -> !form.current().equals(form.newPassword()), "newPassword",
                ViolationMessage.of("password.same", "\"{0}\" must differ from \"currentPassword\""));
```

* 述語は、すべてのプロパティが有効な場合にだけ評価されます。
* 規則は二番目の引数の名前で報告されるため、フォームはそのフィールドの隣にメッセージを表示できます。名前はメッセージの `{0}` でもあります。
* 続けて追加した規則はすべて検査され、違反したすべての規則が報告されます。
* 結果は引き続き `ObjectInputValidator` であり、`rejectingUnknownProperties()`、`readingParameters()`、`constraintOnTarget` を続けられます。
* 述語は生成されたオブジェクトを参照します。オブジェクトが保持しない値はここでは読めません。次を参照してください。

`constraintOnTarget` をオーバーライドする独自の `ValueValidator` は、規則を `TargetConstraints` に保持します。`and` が規則を追加し、`check` が値に違反するすべての規則を報告します。

## 違反の位置

`constraintOnTarget` の二番目の引数は違反の名前であり、`name()`、`path()`、`pointer()` はそこから決まります。オブジェクトのプロパティである名前は、そのプロパティに違反を位置づけます。空の名前はオブジェクト自体に位置づけます。

複数のフィールドにまたがる規則では、オブジェクト自体ではなく、利用者が修正できるフィールド、たとえばパスワードの不一致なら `confirmPassword` を選びます。フォームは `messagesByName()` でそのフィールドの隣にメッセージを表示できます。

## 失敗しうるファクトリ

`flatFromMap(f)` と `flatApply(f)` は、オブジェクトを生成し、失敗しうる関数を受け取ります。関数は `Validated` を返し、すべての引数が有効な場合にだけ適用されます。

```java
record DateRange(LocalDate start, LocalDate end) {

    static Validated<DateRange> of(LocalDate start, LocalDate end) {
        return start.isBefore(end) ? Validated.successWith(new DateRange(start, end))
                : Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"));
    }

}

ObjectInputValidator<DateRange> dateRangeValidator = Yavi.schema()
        ._localDate("start")
        ._localDate("end")
        .flatFromMap(DateRange::of);

dateRangeValidator.validate(Map.of("start", "2025-12-31", "end", "2025-01-01"))
        .errors().get(0).locale(); // the locale of the validation
```

* `Validated.failureWith(ViolationMessage.of(key, format))` は一つの違反を生成します。名前が空であるため、Map 自体で報告され、バリデータを適用したプロパティの名前を取ります。
* 関数には検証のロケールが渡されません。関数が返す違反は、作成時のロケールにかかわらず、その検証のロケールで報告されます。別の場所で作成した違反には `localizedWith(locale)` が同じことをします。
* プロパティに位置づける必要がある違反は、その名前を持つ `ConstraintViolation` で作成するか、規則を `constraintOnTarget` で書きます。
* `flatApply(f)` は型付き引数に対する対応物です。

## 結果に保持されない値

`constraintOnTarget` は生成されたオブジェクトだけを参照します。パスワードの確認のように、検証済みの値がそのオブジェクトに保存されない場合、それを読む規則はファクトリで書きます。ファクトリはすべての検証済み引数を受け取ります。

```java
record PasswordChange(String newPassword) {}

ObjectInputValidator<PasswordChange> passwordChangeValidator = Yavi.schema()
        ._string("newPassword", c -> c.greaterThanOrEqual(8))
        ._string("confirmPassword", c -> c.notBlank())
        .flatFromMap((newPassword, confirmPassword) -> newPassword.equals(confirmPassword)
                ? Validated.successWith(new PasswordChange(newPassword))
                : Validated.failureWith(ViolationMessage.of("password.mismatch", "passwords must match")));
```

入力を検査するためだけに読み取る値にも同じことが当てはまります。その値をプロパティとして宣言し、検証と記述の対象にしたうえで、何を保存するかはファクトリが決めます。

## 評価の順序

検証は次の順序で実行されます。

1. プロパティが読み取られ、型が検査されます。
2. 各プロパティの制約が評価されます。違反は蓄積され、残りのプロパティも検査されます。
3. `constraintOnTarget` の規則が、すべてのプロパティが有効な場合にだけ評価されます。すべての規則が検査されます。
4. `fromMap`、`flatFromMap`、`apply`、`flatApply` に渡すファクトリが、違反がない場合にだけ呼ばれます。単一値のバリデータで `map` や `andThen` の後に追加した規則は、その前の規則が成り立つ場合にだけ検査されます。

プロパティが規則より先に検証されるため、複数フィールドの規則がフィールドの違反を隠すことはありません。規則が蓄積されるため、リクエストは失敗したすべての規則を一度に報告します。
