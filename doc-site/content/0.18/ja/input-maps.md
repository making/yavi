# Map と型なしの値

`fromMap` はスキーマを、`Map<String, Object>`（パース済みの JSON オブジェクトなど）からプロパティ名で値を読み取るバリデータで終えます。キャストもリフレクションも使わず、すべてのプロパティが有効な場合にだけオブジェクトを生成します。

```java
record User(String email, Integer age, Role role) {}

ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.greaterThanOrEqual(0))
        ._enum("role", Role.class)
        .fromMap(User::new);

Validated<User> validated = userValidator.validate(Map.of(
        "email", "jd@example.com", "age", 30, "role", "ADMIN"));
```

## Map からオブジェクトを読む

値はプロパティの型に対して検査されます。

* 型を表さない値は `object.type` で報告されます（`"age" must be of type integer`）。変換は行われないため、Map では `"30"` は数値ではありません。テキストを扱うには[リクエストパラメータ](input-parameters.md)を使います。
* 値を保ったままの数値は Java の型が何であっても受け入れられます。`30L` と `30.0` は `Integer` であり、`30.5` はそうではありません。
* enum 定数はその名前から、時刻値・`Duration`・`Period` は ISO-8601 文字列から、`UUID` と `URI` はその文字列表現から読み取られます（`_object("id", UUID.class)`）。
* 存在しないキーは `null` として検証されます。`optional()`、`nullable()`、`defaultValue(...)` が指定されない限りプロパティは必須で、存在しないキーは `object.required`、明示的な `null` は `object.notNull` で報告されます。
* どのプロパティにも定義されていないキーは無視されます。`rejectingUnknownProperties()` を指定すると `object.unknownProperty` で報告されます。これはその Map だけに適用されます。
* 文字列でないキーを持つ Map は `object.type` で報告されます。

```java
ObjectInputValidator<User> strict = userValidator
        .rejectingUnknownProperties();
strict.validate(Map.of("email", "jd@example.com", "age", 30, "role", "ADMIN", "admin", true));
// "admin" is not a known property
```

## プロパティの型

一部のプロパティは入力を読み取るために型を必要とします。`_enum(name, type)` と `_object(name, type)` は型を取ります。型がないと、そのスキーマを Map には使えません。

```java
._enum("role", Role.class)
._object("id", UUID.class)
```

enum バリデータの `ignoringCase()` は定数を大文字小文字を問わず読み取ります（`._enum(Yavi._enum("role", Role.class).ignoringCase())`）。

## プロパティでない値の検証

`Yavi._string`、`Yavi._integer` などの単一値のバリデータは、値を単独で検証します。名前のないバリデータはリストの要素、Map の値、`anyOf` の代替であり、その違反は使用された場所（`roles[0]`）で命名されます。単独で検証した場合、名前は空になり、メッセージでは名前の代わりに "The value" と表示されます。

```java
StringValidator<String> tag = Yavi._string(c -> c.notBlank().lessThanOrEqual(10));
tag.validate("").errors().get(0).message(); // The value must not be blank
```

## 型の不明なバリデータの組み合わせ

`untyped()` は入力型を知っている任意のバリデータを `ValueValidator<Object, X>` に変換します。文書のルートを検証する場合や、異なる型の代替を組み合わせる場合に使います。

```java
ValueValidator<Object, Object> id = ValueValidator.anyOf(
        Yavi._string(c -> c.notBlank()).untyped(),
        Yavi._integer(c -> c.positive()).untyped());
```

ラムダは入力型を知らないため、`untyped(type)` で型を与えます。文字列を変換するリーダーは [JSON ツリー](input-json.md#coercing-strings)で説明します。

## 入力型が判明していなければならない場合

入力型はバリデータの構築時に判明している必要があります。`fromMap` と `untyped()` は、入力型が不明なバリデータ（ラムダ、または型なしで追加された `_enum` / `_object` プロパティ）に対して `UnsupportedOperationException` を投げます。`untyped(type)`、`_enum(name, type)`、`_object(name, type)` で型を与えてください。

型が不明なバリデータの `readingWith(reader)` は、JDK 型のリーダーに対してのみ受け入れられます。ツリーモデルのノードを入力にする場合は、先に型を与えます。

プロパティの型と生成されるオブジェクトの型はコンパイラが検査します。`nullable()` の引数の null 性を含め、パラメータの型が検証済みの値と一致しない `fromMap(User::new)` はコンパイルできません。
