# バリデーション結果の結合

YAVI は [Applicative Functor](https://en.wikipedia.org/wiki/Applicative_functor) と呼ばれる関数型プログラミングの概念をサポートしています。
一連のバリデーションは、実行チェーンの中で一部またはすべてのバリデーションが失敗した場合でも、結果 (`ConstraintViolation`) を蓄積しながら実行されます。

これは、複数の[値オブジェクト](https://martinfowler.com/bliki/ValueObject.html)のバリデーション結果を組み合わせて新しいオブジェクトを生成したい場合に役立ちます。（もちろん、値オブジェクト以外のオブジェクトにも使えます。）

## Applicative Functor を使った検証

`am.ik.yavi.fn.Validation<E, T>` クラスが Applicative Functor の実装です。`E` はエラーの型、`T` は対象オブジェクトの型です。

`am.ik.yavi.core.ApplicativeValidator` は `Validator` から `applicative()` メソッドで変換でき、これによって `Validation` を取得できます。

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(User::getName, "name", c -> c.notNull().lessThanOrEqual(20))
    // ...
    .build();

ApplicativeValidator<User> applicativeValidator = validator.applicative();
```

`am.ik.yavi.core.Validated<T>` は、`Validator` での使用に特化した `Validation<ConstraintViolation, T>` のショートカットです。

```java
Validator<Email> emailValidator = ValidatorBuilder.<Email> of()
    .constraint(Email::value, "email", c -> c.notBlank().email())
    .build();
Validator<PhoneNumber> phoneNumberValidator = ValidatorBuilder.<PhoneNumber> of()
    .constraint(PhoneNumber::value, "phoneNumber", c -> c.notBlank().pattern("[0-9\\-]+"))
    .build();

Validated<Email> emailValidated = emailValidator.applicative().validate(email);
Validated<PhoneNumber> phoneNumberValidated = phoneNumberValidator.applicative().validate(phoneNumber);
```

検証済みの対象、または制約違反は、`Validated` インスタンスから次のように取得できます:

```java
if (emailValidated.isValid()) {
    Email email = emailValidated.value(); // throws NoSuchElementException if it is invalid
} else {
    ConstraintViolations violations = emailValidated.errors(); // throws NoSuchElementException if it is valid
}
// or
Email email = emailValidated.orElseThrow(violations -> new ConstraintViolationsException(violations));
```

`Validation#throwIfInvalid` と `ConstraintViolations#throwIfInvalid` は、値を返す代わりに関数引数で指定した例外を投げます:

```java
emailValidated.throwIfInvalid(ConstraintViolationsException::new);
```

検証の成功・失敗にかかわらず同じ型のインスタンスを生成したい場合は、`fold` メソッドが便利です。

```java
HttpStatus status = emailValidated.fold(violations -> HttpStatus.BAD_REQUEST, email -> HttpStatus.OK);
```

`Validated` の場合、`fold` のエラー型は `List<ConstraintViolation>` です。

## バリデーション結果のマッピングと検査

`Validation` は結果を変換または取り出すための `map`、`flatMap`、`mapError`、`mapErrors`、`bimap`、`orElseThrow`、`throwIfInvalid` を提供します:

```java
Validation<ConstraintViolation, String> mapped = emailValidated.map(Email::value);
Validation<String, Email> mappedErrors = emailValidated.mapError(ConstraintViolation::messageKey);
Validation<String, String> bimapped = emailValidated
    .bimap(violations -> List.of("invalid"), Email::value);
```

`am.ik.yavi.core.Validated` は `Validation<ConstraintViolation, T>` のショートカットです。
`Validated.successWith`、`Validated.failureWith`、`Validated.of` で生成できます:

```java
Validated<Email> success = Validated.successWith(new Email("yavi@example.com"));
Validated<Email> failure = Validated.failureWith(violations); // ConstraintViolations is a List<ConstraintViolation>
Validated<Email> fromValidation = Validated.of(Validation.success(new Email("yavi@example.com")));
```

複数の結果は `Validated.sequence`、`Validated.traverse`、`Validated.traverseIndexed`、`Validated.traverseOptional` で結合できます:

```java
Validated<List<Email>> emails = Validated.sequence(List.of(emailValidated, anotherEmailValidated));
Validated<List<Email>> traversed = Validated.traverse(inputs, emailValidator::validate);
Validated<List<Email>> indexed = Validated.traverseIndexed(inputs,
    (input, index) -> emailValidator.indexed(index).validate(input));
Validated<Optional<Email>> optional = Validated.traverseOptional(optionalInput, emailValidator::validate);
```

`ConstraintViolations#of(List<ConstraintViolation>)` は違反のリストから `ConstraintViolations` を生成します。

## `Validation` / `Validated` オブジェクトの結合

`Validation` / `Validated` オブジェクトを結合して新しいオブジェクトを生成できます。
以下の例では、`Email` と `PhoneNumber` を検証したうえで `ContactInfo` インスタンスを生成しています。

```java
Validated<ContactInfo> contactInfoValidated = emailValidated.combine(phoneNumberValidated)
    .apply((em, ph) -> new ContactInfo(em, ph));
// or
Validated<ContactInfo> contactInfoValidated = Validations.combine(emailValidated, phoneNumberValidated)
    .apply((em, ph) -> new ContactInfo(em, ph));
```

`Validations.combine(...)` は 2 から 16 個のバリデーションをサポートします。

ここで重要なのは、`Email` または `PhoneNumber` の検証が失敗しても、途中で打ち切られることなく、すべての検証結果が蓄積される点です。

たとえば `Email` に空白を、`PhoneNumber` に `a` を指定して `ContactInfo` を生成しようとすると検証は失敗し、次の 3 つの `ConstraintViolation` が得られます:

```
* "email" must not be blank
* "email" must be a valid email address
* "phoneNumber" must match [0-9\-]+
```

次のプログラムは、失敗する 2 つの検証を組み合わせて蓄積された違反をすべて出力し、その後、成功する 2 つの検証から `ContactInfo` を生成します。Run を押すとブラウザでコンパイル・実行され、結果がセルの下に表示されます。

```java runnable
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Validations;

public class Main {

    record Email(String value) {
    }

    record PhoneNumber(String value) {
    }

    record ContactInfo(Email email, PhoneNumber phoneNumber) {
    }

    static final Validator<Email> emailValidator = ValidatorBuilder.<Email>of()
        .constraint(Email::value, "email", c -> c.notBlank().email())
        .build();

    static final Validator<PhoneNumber> phoneNumberValidator = ValidatorBuilder.<PhoneNumber>of()
        .constraint(PhoneNumber::value, "phoneNumber", c -> c.notBlank().pattern("[0-9\\-]+"))
        .build();

    public static void main(String[] args) {
        Validated<Email> email = emailValidator.applicative().validate(new Email(" "));
        Validated<PhoneNumber> phoneNumber = phoneNumberValidator.applicative()
            .validate(new PhoneNumber("a"));
        Validated<ContactInfo> invalid = Validations.combine(email, phoneNumber)
            .apply(ContactInfo::new);
        System.out.println("valid: " + invalid.isValid());
        System.out.println("violations: " + invalid.errors().size());
        invalid.errors().forEach(v -> System.out.println("- " + v.message()));

        Validated<ContactInfo> valid = Validations
            .combine(emailValidator.applicative().validate(new Email("yavi@example.com")),
                    phoneNumberValidator.applicative().validate(new PhoneNumber("090-123-4567")))
            .apply(ContactInfo::new);
        System.out.println("contact: " + valid.value().email().value() + " / "
                + valid.value().phoneNumber().value());
    }
}
```
<!-- run: expected output
valid: false
violations: 3
- "email" must not be blank
- "email" must be a valid email address
- "phoneNumber" must match [0-9\-]+
contact: yavi@example.com / 090-123-4567
-->

> **ヒント**
>
> 上の例での `ContactInfo` の検証は、次のように `nest` メソッドを使っても実現できます:
>
> ```java
> Validator<Email> emailValidator = ValidatorBuilder.<Email> of()
>     .constraint(Email::value, "value", c -> c.notBlank().email())
>     .build();
> Validator<PhoneNumber> phoneNumberValidator = ValidatorBuilder.<PhoneNumber> of()
>     .constraint(PhoneNumber::value, "value", c -> c.notBlank().pattern("[0-9\\-]+"))
>     .build();
>
> Validator<ContactInfo> validator = ValidatorBuilder.<ContactInfo> of()
>     .nest(ContactInfo::email, "email", emailValidator)
>     .nest(ContactInfo::phoneNumber, "phoneNumber", phoneNumberValidator)
>     .build();
> ```
>
> または、[バリデータの組み合わせ](combining-validators.md)で紹介しているように、`ApplicativeValidator<Email>` と `ApplicativeValidator<PhoneNumber>` を組み合わせてもかまいません。
>
> ```java
> Arguments2Validator<Email, PhoneNumber, ContactInfo> contactInfoValidator = ArgumentsValidators
>     .split(emailValidator.applicative(), phoneNumberValidator.applicative())
>     .apply(ContactInfo::new);
>
> Validated<ContactInfo> contactInfoValidated = contactInfoValidator.validate(new Email("yavi@example.com"), new PhoneNumber("090-123-4567"));
> ```
