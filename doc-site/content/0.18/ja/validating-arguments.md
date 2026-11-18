# 引数の検証

引数バリデータは、オブジェクトが作成される前にコンストラクタやファクトリメソッドの引数を検証します。そのため、規則を満たさないオブジェクトは存在しません。

```java
public final class Person {

    public static final Arguments3Validator<String, String, Integer, Person> validator = Yavi.schema()
            ._string("name", c -> c.notBlank().lessThanOrEqual(100))
            ._string("email", c -> c.notBlank().lessThanOrEqual(100).email())
            ._integer("age", c -> c.greaterThanOrEqual(0).lessThan(200))
            .apply(Person::new);

    private Person(String name, String email, int age) {
        // ...
    }

}
```

```java
Validated<Person> validated = Person.validator.validate("John Doe", "jdoe@example.com", 30);
```

`Arguments1Validator` から `Arguments16Validator` まで利用できます。引数の最大数は 16 です。

`Yavi.arguments()` は `Yavi.schema()` の旧名です。引き続き動作しますが非推奨であり、引数バリデータにも `Yavi.schema()` を使います。違いは非推奨の呼び出しだけです。ビルダーと引数バリデータは同じです。

```java
// deprecated
Yavi.arguments();

// current
Yavi.schema();
```

ビルダーの型は `YaviSchema` です。以前の名前 `YaviArguments` はなくなりました。

## 利用できる引数の種類

引数は型ごとのメソッドで追加します。

* `_string`
* `_boolean`
* `_short`、`_integer`、`_long`、`_float`、`_double`
* `_bigInteger`、`_bigDecimal`
* `_localDate`、`_localTime`、`_localDateTime`、`_offsetDateTime`、`_instant`、`_year`、`_yearMonth`、`_zonedDateTime`
* `_enum`
* `_object`
* `_list`、`_map`

`_object` はコレクションや Map を含む任意の参照型を受け付けます。`_list` と `_map` はリストや Map の要素または値を検証します。

```java
Arguments1Validator<LocalDate, LocalDate> dateValidator = Yavi.schema()
        ._localDate("date", c -> c.notNull())
        .apply(date -> date);

Arguments1Validator<List<? extends String>, List<String>> tagsValidator = Yavi.schema()
        ._list("tags", Yavi._string(c -> c.notBlank()))
        .apply(tags -> tags);
```

0.18 では `_enum` と `_object` の引数は型を取ります (`_enum("role", Role.class)`、`_object("id", UUID.class)`)。これにより、引数を Map から読むこともできます。

## メソッド引数の検証

`ArgumentsValidatorBuilder.of` は、レシーバを最初の引数として含め、メソッドの引数を検証します。各 `_xxx` メソッドは名前の代わりに引数の位置のアクセサ (`Arguments3::arg1` など) を取ります。

```java
Arguments3Validator<UserService, String, String, User> validator = ArgumentsValidatorBuilder
        .of(UserService::createUser)
        .builder(b -> b._object(Arguments3::arg1, "userService", c -> c.notNull())
                ._string(Arguments3::arg2, "email", c -> c.email())
                ._string(Arguments3::arg3, "name", c -> c.notBlank()))
        .build();

Validated<User> validated = validator.validate(userService, "jdoe@example.com", "John Doe");
```

`Arguments1Validator` から `Arguments16Validator` は互いに継承しないため、ビルダーの `_xxx` メソッドはアクセサを取ります。アリティに合うアクセサを使ってください。これはアノテーションプロセッサが生成する形式でもあります。「[アノテーションプロセッサ](annotation-processor.md)」を参照してください。

## 単一値のバリデータ

1 つの値のバリデータは、`Yavi._string`、`Yavi._integer` などの `Yavi` のメソッド、または型のビルダーで構築します。

```java
StringValidator<String> nameValidator = Yavi._string("name", c -> c.notBlank().lessThanOrEqual(100));

IntegerValidator<Integer> ageValidator = IntegerValidatorBuilder
        .of("age", c -> c.greaterThanOrEqual(0).lessThan(200))
        .build();

Validated<String> nameValidated = nameValidator.validate("John Doe");
```

`map` は検証済みの値を変換します。たとえば値オブジェクトにできます。単一値のビルダーの `build(Function)` も同じです。

```java
StringValidator<Name> nameValidator = Yavi._string("name", c -> c.notBlank().lessThanOrEqual(100))
        .map(Name::new);
```

0.18 では、`nullable()` が示さない限り単一値は必須です。また `build()` は値を書き戻す方法を知る型 (`StringValidator.Reversible` など) を返します。`StringValidator<String>` として宣言した変数も引き続きコンパイルできます。

## 合成と適応

`compose` は別の型の値を先に変換して検証します。

```java
Arguments1Validator<Integer, Name> nameFromIdValidator = nameValidator.compose(Object::toString);
```

`wrap` は引数バリデータを `ArgumentsN` コンテナを検証するバリデータにし、`unwrap` は元に戻します。

```java
Arguments1Validator<Arguments1<String>, Name> wrapped = nameValidator.wrap();
Arguments1Validator<String, Name> unwrapped = Arguments1Validator.unwrap(wrapped);
```

`lazy()` は検証済みの値の `Supplier` を生成するバリデータを返すため、値はサプライヤを使う時にだけ作成されます。

```java
Arguments1Validator<String, Supplier<Name>> lazyValidator = nameValidator.lazy();
Supplier<Name> nameSupplier = lazyValidator.validated("John Doe");
```

`prefixed(String)` は値の違反を名前の下に配置します。リフトしたバリデータは要素の違反をインデックスの下に配置します。「[バリデータの組み合わせ](combining-validators.md)」を参照してください。

```java
ValueValidator<Iterable<? extends String>, List<Email>> emails = emailValidator.liftList();
```

0.18 では `indexed(int)` は削除されました。インデックスを名前の前に置くには、`Validated` または `ConstraintViolation` の `prefixed(int)` を使います。

```java
Validated<Email> validated = emailValidator.validate("yavi@example.com").prefixed(0);
// [0].email
```

`split` と `combine` はバリデータを組み合わせます。「[バリデータの組み合わせ](combining-validators.md)」で説明します。

## Arguments コンテナ

`Arguments1` から `Arguments16` は、検証済みの引数を保持する不変のレコードです。`Arguments.of(...)` で作成します。

```java
Arguments3<String, String, Integer> arguments = Arguments.of("John Doe", "jdoe@example.com", 30);
String name = arguments.arg1();
String email = arguments.arg2();
Integer age = arguments.arg3();
```

`argN()` は位置 `N` の引数を返します。`firstN()` と `lastN()` は先頭または末尾の `N` 個の引数を返し、`append`、`prepend`、`reverse` は変更したコピーを返します。`toString()` は `Arguments3[arg1=..., arg2=..., arg3=...]` を返します。

```java
Arguments2<String, String> firstTwo = arguments.first2();
Arguments4<String, String, Integer, String> appended = arguments.append("extra");
Arguments3<Integer, String, String> reversed = arguments.reverse();
```

`ArgumentsN` は `ArgumentsM` を継承せず、等価性には具体的なコンテナクラスが含まれます。2 つのコンテナは、型と引数の値が同じ場合にだけ等しくなります。
