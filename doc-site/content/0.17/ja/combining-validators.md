# バリデータの組み合わせ

これまでに見てきた `ApplicativeValidator<T>` と `Arguments1Validator<S, T>` は `ValueValidator<S, T>` を継承しています。
(`T` は検証対象オブジェクトの型、`S` は対象オブジェクトを生成するソースオブジェクトの型です。)

`ApplicativeValidator<T>` は `ValueValidator<T, T>` です。

複数の `ValueValidator` を組み合わせて、より大きなオブジェクトを検証できます。

`ArgumentsValidators#split` と `ArgumentsValidators#combine` は 0.10.0 から最大 16 個の引数をサポートします。
`apply` メソッドはそれに応じて `Function1` から `Function16` を受け取ります。

## オブジェクトのバリデータを小さな部品に分割する

大きなオブジェクトを生成するコンストラクタやファクトリメソッドの引数に対するバリデータは、`split` メソッドを使って小さなバリデータに分割し、それらを組み合わせられます。

例を見てみましょう。

コンストラクタ `Person(String, String, Integer)` に対する Validator は `Arguments3Validator<String, String, Integer, Person>` です。
これは 2 つの `StringValidator<String>` と 1 つの `IntegerValidator<Integer>` に分割したうえで組み合わせられます。

次のように定義できます:

```java
public class Person {
    public Person(String name, String email, Integer age) {
        // ...
    }
}

StringValidator<String> nameValidator = /* see examples above */;
StringValidator<String> emailValidator = /* see examples above */;
IntegerValidator<Integer> ageValidator = /* see examples above */;

Arguments3Validator<String, String, Integer, Person> personValidator = ArgumentsValidators
    .split(nameValidator, emailValidator, ageValidator)
    .apply(Person::new);
// or
Arguments3Validator<String, String, Integer, Person> personValidator = nameValidator
    .split(emailValidator)
    .split(ageValidator)
    .apply(Person::new);
```

値オブジェクトに対する Validator についても同様です。

```java
public class Person {
    public Person(Name name, Email email, Age age) {
        // ...
    }
}

StringValidator<Name> nameValidator = /* see examples above */;
StringValidator<Email> emailValidator = /* see examples above */;
IntegerValidator<Age> ageValidator = /* see examples above */;

Arguments3Validator<String, String, Integer, Person> personValidator = ArgumentsValidators
    .split(nameValidator, emailValidator, ageValidator)
    .apply(Person::new);
// or
Arguments3Validator<String, String, Integer, Person> personValidator = nameValidator
    .split(emailValidator)
    .split(ageValidator)
    .apply(Person::new);
```

生成した Validator は次のように使えます:

```java
Validated<Person> personValidated = Person.validator.validate("Jone Doe", "jdoe@example.com", 30);
```

`ArgumentsValidators#split` の引数は `Arguments1Validator` ではなく `ValueValidator` であるため、次のように `ApplicativeValidator` を組み合わせることもできます:

```java
ApplicativeValidator<Email> emailValidator = /* see examples above */;
ApplicativeValidator<PhoneNumber> phoneNumberValidator = /* see examples above */;

Arguments2Validator<Email, PhoneNumber, ContactInfo> contactInfoValidator = ArgumentsValidators
    .split(emailValidator, phoneNumberValidator)
    .apply(ContactInfo::new);

Validated<ContactInfo> contactInfoValidated = contactInfoValidator.validate(new Email("yavi@example.com"), new PhoneNumber("090-123-4567"));
```

次のプログラムは、コンストラクタ引数用の 3 つの小さなバリデータを作成し、`split` で組み合わせます。不正な引数の組と正しい組を検証し、違反を出力します。Run を押すとブラウザでコンパイル・実行され、結果がセルの下に表示されます。

```java runnable
import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.arguments.IntegerValidator;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.IntegerValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.Validated;

public class Main {

    record Person(String name, String email, int age) {
    }

    public static void main(String[] args) {
        StringValidator<String> nameValidator = StringValidatorBuilder
            .of("name", c -> c.notBlank().lessThanOrEqual(20))
            .build();
        StringValidator<String> emailValidator = StringValidatorBuilder
            .of("email", c -> c.notBlank().email())
            .build();
        IntegerValidator<Integer> ageValidator = IntegerValidatorBuilder
            .of("age", c -> c.greaterThanOrEqual(18))
            .build();
        Arguments3Validator<String, String, Integer, Person> validator = ArgumentsValidators
            .split(nameValidator, emailValidator, ageValidator)
            .apply(Person::new);

        Validated<Person> invalid = validator.validate("", "not-an-email", 15);
        System.out.println("valid: " + invalid.isValid());
        invalid.errors().forEach(v -> System.out.println("- " + v.name() + ": " + v.message()));

        Validated<Person> valid = validator.validate("John Doe", "jdoe@example.com", 30);
        System.out.println("created: " + valid.value().name() + " <" + valid.value().email() + ">");
    }
}
```
<!-- run: expected output
valid: false
- name: "name" must not be blank
- email: "email" must be a valid email address
- age: "age" must be greater than or equal to 18
created: John Doe <jdoe@example.com>
-->

## 値バリデータの連鎖とマッピング

`ValueValidator#andThen(ValueValidator)` は、あるバリデータが生成した値を次のバリデータに適用します。
`ValueValidator#map(Function)` は、さらなる検証を行わずに検証済みの値をマッピングします。
`andThen(Function)` は 0.17.0 から非推奨であり、`map(Function)` と等価です。

```java
Arguments1Validator<String, Email> emailValidator = /* see examples above */;
ValueValidator<String, String> emailValue = emailValidator.map(Email::value);
ValueValidator<String, String> checked = emailValidator
    .andThen(ValueValidator.<Email> passThrough().map(Email::value));
```

## イテラブルのすべての要素を検証する

`ArgumentsValidators#sequenceN` は、与えられたすべてのバリデータを同じ引数に適用し、検証済みの値を `List` に収集します。
`ArgumentsValidators#traverseN` は、値のイテラブルと、各値に対するバリデータを生成する関数からバリデータを構築します。
どちらも 0.10.0 から 1 から 16 個の引数で利用できます。

```java
Arguments1Validator<String, Email> emailValidator = /* see examples above */;
Arguments1Validator<String, Email> adminEmailValidator = /* see examples above */;
Arguments1Validator<String, List<Email>> emailsValidator = ArgumentsValidators
    .sequence1(List.of(emailValidator, adminEmailValidator));

Validated<List<Email>> emails = emailsValidator.validate("yavi@example.com");
```

引数バリデータの構築については [引数のバリデーション](validating-arguments.md) を参照してください。

## 対象オブジェクトを生成する前のソースオブジェクトの検証

Web アプリケーションでは、`Map`、フォームオブジェクト、`HttpServletRequest` から値を取得してドメインオブジェクトを生成することがよくあります。

`Arguments1Validator` はソースオブジェクトを検証したうえで、`compose` メソッドを使って対象オブジェクトに変換できます。

たとえば、Servlet の `HttpServletRequest` オブジェクトから HTTP リクエストパラメータとして `name`、`email`、`age` を取得する場合は、次のように定義できます:

```java
Arguments1Validator<HttpServletRequest, Name> requestNameValidator = nameValidator
    .compose(req -> req.getParameter("name"));
Arguments1Validator<HttpServletRequest, Email> requestEmailValidator = emailValidator
    .compose(req -> req.getParameter("email"));
Arguments1Validator<HttpServletRequest, Age> requestAgeValidator = ageValidator
    .compose(req -> Integer.valueOf(req.getParameter("age")));

HttpServletRequest request = ...;
Validated<Name> nameValidated = requestNameValidator.validate(request);
Validated<Email> emailValidated = requestEmailValidator.validate(request);
Validated<Age> ageValidated = requestAgeValidator.validate(request);
```

これらの Validator は `combine` メソッドで次のように組み合わせ、`Person` オブジェクトに対する Validator を生成できます:

```java
Arguments1Validator<HttpServletRequest, Person> requestPersonValidator = ArgumentsValidators
    .combine(requestNameValidator, requestEmailValidator, requestAgeValidator)
    .apply(Person::new);
// or
Arguments1Validator<HttpServletRequest, Person> requestPersonValidator = requestNameValidator
    .combine(requestEmailValidator)
    .combine(requestAgeValidator)
    .apply(Person::new);

HttpServletRequest request = ...;
Validated<Person> personValidated = requestPersonValidator.validate(request);
```

この Validator は、`Arguments3Validator<String, String, Integer, Person>` から次のように変換することもできます:

```java
Arguments3Validator<String, String, Integer, Person> personValidator = /* see examples above */;
Arguments1Validator<HttpServletRequest, Person> requestPersonValidator = personValidator
    .compose(req -> Arguments.of(req.getParameter("name"), req.getParameter("email"), Integer.valueOf(req.getParameter("age"))));
```

このように Validator を組み合わせることで、さまざまなパターンの Validator を生成できます。小さな Arguments Validator を生成しておくと、オブジェクトを生成する前に値を検証できるだけでなく、再利用しやすくなります。
