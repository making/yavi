# 引数のバリデーション

YAVI は Arguments Validator を使って、**オブジェクトを生成する前に** コンストラクタやファクトリメソッドの引数を検証できます。

これは YAVI 固有の機能のひとつです。

Arguments Validator はその名のとおり、引数に対する Validator です。

Bean Validation のような通常の Validator は、値を設定したオブジェクトに対してその値が妥当かを検証します。これに対し Arguments Validator は、オブジェクトに設定する**引数**が妥当であることを検証します。

通常の Validator では検証の前にオブジェクトが生成されるため、不完全な状態のオブジェクトが一時的に存在し得ます。
たとえば、Validator 側で `notNull()` の制約を課していても、コンストラクタに `null` チェックを実装している場合、Validator による検証より先にその `null` チェックが働きます。

Arguments Validator は**検証の後にオブジェクトを生成する**ため、不完全なオブジェクトを気にする必要はありません。
[Always Valid モデル](getting-started.md#the-always-valid-model)では、これを private コンストラクタや record と組み合わせて、不正なオブジェクトを生成できなくする方法を示しています。

## 引数バリデータの定義

引数バリデータを構築するには `am.ik.yavi.validator.Yavi#arguments()` を使います。
各 `_xxx` メソッドは引数を 1 つ追加し、その名前と制約を指定します。`apply` がバリデータを生成します:

```java
import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.core.Validated;
import am.ik.yavi.validator.Yavi;

public class Person {

    public static final Arguments3Validator<String, String, Integer, Person> validator = Yavi.arguments()
        ._string("name", c -> c.notBlank().lessThanOrEqual(100)) // Constraint on the first argument of Person::new
        ._string("email", c -> c.notBlank().lessThanOrEqual(100).email()) // Constraint on the second argument of Person::new
        ._integer("age", c -> c.greaterThanOrEqual(0).lessThan(200)) // Constraint on the third argument of Person::new
        .apply(Person::new);

    // ...
}
```

引数バリデータは次のように使えます:

```java
Validated<Person> personValidated = Person.validator.validate("John Doe", "jdoe@example.com", 30);
```

次のプログラムは、private コンストラクタだけを持つクラスに対して同じ引数バリデータを構築します。`Person` は `Validated` を通してのみ生成できます。不正な呼び出しの違反と、正しい呼び出しで生成されたオブジェクトを出力します。Run を押すとブラウザでコンパイル・実行され、結果がセルの下に表示されます。

```java runnable
import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.core.Validated;
import am.ik.yavi.validator.Yavi;

public class Main {

    static final class Person {

        private final String name;

        private final String email;

        private final int age;

        private static final Arguments3Validator<String, String, Integer, Person> validator = Yavi
            .arguments()
            ._string("name", c -> c.notBlank().lessThanOrEqual(20))
            ._string("email", c -> c.notBlank().email())
            ._integer("age", c -> c.greaterThanOrEqual(18))
            .apply(Person::new);

        private Person(String name, String email, int age) {
            this.name = name;
            this.email = email;
            this.age = age;
        }

        static Validated<Person> create(String name, String email, int age) {
            return validator.validate(name, email, age);
        }

        @Override
        public String toString() {
            return this.name + " <" + this.email + ">, age " + this.age;
        }

    }

    public static void main(String[] args) {
        Validated<Person> invalid = Person.create("", "john@", 17);
        System.out.println("valid: " + invalid.isValid());
        invalid.errors().forEach(v -> System.out.println("- " + v.name() + ": " + v.message()));

        Validated<Person> valid = Person.create("John Doe", "jdoe@example.com", 30);
        System.out.println("created: " + valid.value());
    }
}
```
<!-- run: expected output
valid: false
- name: "name" must not be blank
- email: "email" must be a valid email address
- age: "age" must be greater than or equal to 18
created: John Doe <jdoe@example.com>, age 30
-->

`am.ik.yavi.arguments.Arguments1Validator` から `am.ik.yavi.arguments.Arguments16Validator` まで利用できます。
引数の最大数は 16 です。

## 利用できる引数の種類

次の引数の種類を利用できます。種類ごとに対応する制約型があります:

* `_string`
* `_boolean`
* `_short`, `_integer`, `_long`, `_float`, `_double`
* `_bigInteger`, `_bigDecimal`
* `_localDate`, `_localTime`, `_localDateTime`, `_offsetDateTime`, `_instant`, `_year`, `_yearMonth`, `_zonedDateTime`
* `_enum`
* `_object`

`_object` は配列、コレクション、マップを含む任意の参照型を受け付けます:

```java
Arguments1Validator<LocalDate, LocalDate> dateValidator = Yavi.arguments()
    ._localDate("date", c -> c.notNull())
    .apply(date -> date);

Arguments1ValidatorBuilder<Color, Color> colorValidatorBuilder = Yavi.arguments()
    ._enum("color", c -> c.notNull());
Arguments1Validator<Color, Color> colorValidator = colorValidatorBuilder.apply(color -> color);

Arguments1Validator<List<String>, List<String>> tagsValidator = Yavi.arguments()
    ._object(ObjectValidatorBuilder.<List<String>>of("tags", c -> c.notNull()).build())
    .apply(tags -> tags);
```

`_collection`、`_map`、`_objectArray` は引数の種類ではなく、オブジェクトのコレクション、マップ、配列のプロパティを検証するための `ValidatorBuilder` のメソッドです。
詳細は [YAVI の使い方](using-yavi.md) を参照してください。

## メソッド引数のバリデーション

Arguments Validator はメソッドの引数の検証にも使えます。

```java
// From https://beanvalidation.org/
public class UserService {
    public User createUser(/* @Email */ String email,
                           /* @NotNull */ String name) {
        // ...
    }
}
```

上の例の `UserService.createUser` の引数は `ArgumentsValidatorBuilder` で検証できます:

```java
Arguments3Validator<UserService, String, String, User> validator = ArgumentsValidatorBuilder
    .of(UserService::createUser)
    .builder(b -> b
        ._object(Arguments3::arg1, "userService", c -> c.notNull())
        ._string(Arguments3::arg2, "email", c -> c.email())
        ._string(Arguments3::arg3, "name", c -> c.notBlank()))
    .build();

UserService userService = new UserService();
Validated<User> userValidated = validator.validate(userService, "jdoe@example.com", "John Doe");
```

値がインスタンスメソッド参照から得られる場合はこの形式を使います。レシーバが第 1 引数として検証されるためです。
これは [アノテーションプロセッサ](annotation-processor.md) で説明する、アノテーションプロセッサが生成するメタ定数を使う形式でもあります。
ビルダーの `_xxx` メソッドは名前だけでなく、引数の位置に対応するアクセサ (`Arguments3::arg1` など) を受け取ります。`Arguments1Validator`..`Arguments16Validator` は互いに継承しないため、ビルダーの引数の個数に合ったアクセサを使います。

> **注意**
>
> `void` は戻り値の型として使えません。`java.lang.Void` は利用できます。

## 単一の値に対する Validator の定義

単一の `String` や `Integer` などに対する `Arguments1Validator` を定義したいだけなら、次のように書けます:

```java
StringValidator<String> nameValidator = StringValidatorBuilder
    .of("name", c -> c.notBlank().lessThanOrEqual(100))
    .build();

IntegerValidator<Integer> ageValidator = IntegerValidatorBuilder
    .of("age", c -> c.greaterThanOrEqual(0).lessThan(200))
    .build();

Validated<String> nameValidated = nameValidator.validate("John Doe");
Validated<Integer> ageValidated = ageValidator.validate(30);
```

`build(Function)` は検証済みの値を値オブジェクトに変換します:

```java
StringValidator<Name> nameValidator = StringValidatorBuilder
    .of("name", c -> c.notBlank().lessThanOrEqual(100))
    .build(Name::new);

StringValidator<Email> emailValidator = StringValidatorBuilder
    .of("email", c -> c.notBlank().lessThanOrEqual(100).email())
    .build(Email::new);

IntegerValidator<Age> ageValidator = IntegerValidatorBuilder
    .of("age", c -> c.greaterThanOrEqual(0).lessThan(200))
    .build(Age::new);
```

既存のバリデータを `map(Function)` でマッピングすることもできます:

```java
StringValidator<Name> nameValidator = StringValidatorBuilder
    .of("name", c -> c.notBlank().lessThanOrEqual(100))
    .build()
    .map(Name::new);
```

`andThen(Function)` は同じ結果を生成し、`map(Function)` の代わりとして 0.17.0 から非推奨です。
`andThen(ValueValidator)` は非推奨ではなく、生成された値に別のバリデータを連鎖させます:

```java
ObjectValidator<Name, Name> nonNullNameValidator = ObjectValidatorBuilder
    .<Name>of("name", c -> c.notNull())
    .build();

Arguments1Validator<String, Name> checkedNameValidator = nameValidator
    .andThen(nonNullNameValidator);
```

## バリデータの合成と変換

`compose(Function)` は検証の前にソースオブジェクトを変換します:

```java
Arguments1Validator<Integer, Name> nameFromIdValidator = nameValidator.compose(Object::toString);
```

`wrap()` は引数バリデータを `ArgumentsN` コンテナを検証するバリデータに変換し、`Arguments1Validator.unwrap(...)`（または `ArgumentsNValidator.unwrap(...)`）はそれを元に戻します:

```java
Arguments1Validator<Arguments1<String>, Name> wrappedValidator = nameValidator.wrap();
Arguments1Validator<String, Name> unwrappedValidator = Arguments1Validator.unwrap(wrappedValidator);
```

`lazy()` は検証済みの値の `Supplier` を生成するバリデータを返します。
値はサプライヤが使用されるときにだけ生成されるため、コンストラクタ内でコンストラクタへ再帰せずに引数を検証できます:

```java
Arguments1Validator<String, Supplier<Name>> lazyValidator = nameValidator.lazy();
Supplier<Name> nameSupplier = lazyValidator.validated("John Doe");
```

record のコンストラクタでの `lazy()` の使い方については [Always Valid モデル](getting-started.md#the-always-valid-model) を参照してください。

`indexed(int)` は違反名に `[index]` を付加し、コレクション要素の違反を区別できるようにします:

```java
Arguments1Validator<String, Email> indexedEmailValidator = emailValidator.indexed(0);
```

`List` に対する `Arguments1Validator` を生成したい場合は、リストの要素に対する `Validator` を次のように「リフト」できます:

```java
Arguments1Validator<Iterable<String>, List<Email>> emailsValidator = emailValidator.liftList();
Validated<List<Email>> emailsValidated = emailsValidator.validate(List.of("foo@example.com", "bar@example.com"));

Arguments1Validator<Iterable<String>, Set<Email>> emailSetValidator = emailValidator.liftSet();
Arguments1Validator<Iterable<String>, List<Email>> emailCollectionValidator = emailValidator
    .liftCollection(ArrayList::new);
Arguments1Validator<Optional<String>, Optional<Email>> emailOptionalValidator = emailValidator.liftOptional();
```

`liftList()`、`liftSet()`、`liftCollection(Supplier)`、`liftOptional()` も利用できます。

`split(...)` は別々の引数を別々のバリデータで検証し、`combine(...)` は同じ入力を複数のバリデータで検証します。
`ArgumentsValidators.split(...)` と `ArgumentsValidators.combine(...)` については [バリデータの組み合わせ](combining-validators.md) で説明しています。

`ArgumentsValidators.traverseN(...)` は、関数が生成するバリデータで指定したコレクションの各要素を検証し、結果を収集します。
`sequenceN(...)` は複数のバリデータを同じ引数に適用し、結果を収集します:

```java
Arguments1Validator<String, List<Name>> namesValidator = ArgumentsValidators
    .traverse1(List.of("John", "Jane"), value -> nameValidator);

Arguments1Validator<String, List<String>> sequencedValidator = ArgumentsValidators
    .sequence1(List.of(nameValidator.map(Name::value), nameValidator.map(Name::value)));
```

これらの「小さな Validator」は、[バリデータの組み合わせ](combining-validators.md) で説明する Validator コンビネータと組み合わせると、強力な部品になります。

## Arguments コンテナ

`Arguments1` から `Arguments16` は、検証済みの引数の不変コンテナです。
生成には `Arguments.of(...)` を使います:

```java
Arguments3<String, String, Integer> arguments = Arguments.of("John Doe", "jdoe@example.com", 30);
String name = arguments.arg1();
String email = arguments.arg2();
Integer age = arguments.arg3();
```

`argN()` は位置 `N` の引数を返します。`firstN()` と `lastN()` は先頭または末尾の `N` 個の引数を新しいコンテナとして返し、`append`、`prepend`、`reverse` は変更したコピーを返します:

```java
Arguments1<String> first = arguments.first1();
Arguments2<String, String> firstTwo = arguments.first2();
Arguments1<Integer> last = arguments.last1();
Arguments2<String, Integer> lastTwo = arguments.last2();

Arguments4<String, String, Integer, String> appended = arguments.append("extra");
Arguments4<String, String, String, Integer> prepended = arguments.prepend("extra");
Arguments3<Integer, String, String> reversed = arguments.reverse();
```

`equals` と `hashCode` はコンテナの内容に対して実装されています。
`Arguments` は final であり、`ArgumentsN` は `ArgumentsM` を継承しません。等価性の判定には具体的なコンテナクラスも含まれます。
