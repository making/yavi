# スキーマの定義

スキーマは `Yavi.schema()` に対するプロパティメソッドの列です。各メソッドは入力値の型と名前を与え、ラムダがその制約を追加します。`fromMap(f)` または `apply(f)` がスキーマをバリデータで終えます。

```java
import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.validator.Yavi;

record Signup(String email, Integer age) {}

ObjectInputValidator<Signup> signupValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.greaterThanOrEqual(0).lessThanOrEqual(150))
        .fromMap(Signup::new);
```

## プロパティメソッド

メソッド名は、読み取る Java の型に対応しています。

| 型 | メソッド |
| --- | --- |
| テキスト | `_string` |
| 整数 | `_integer`、`_long`、`_short`、`_bigInteger` |
| 小数 | `_double`、`_float`、`_bigDecimal` |
| 真偽値 | `_boolean` |
| enum | `_enum(name, type)` |
| 日付と時刻 | `_localDate`、`_localTime`、`_localDateTime`、`_instant`、`_offsetDateTime`、`_zonedDateTime`、`_year`、`_yearMonth` |
| その他の値 | `_object(name, type)`。たとえば `UUID.class`、`URI.class`、`Duration.class`、`Period.class` |
| コンテナ | `_list`、`_map`、`_object(name, nestedValidator)`、`_embedded`、`_rest` |

どのメソッドにも、制約を省略した形（`._string("name")`）と、名前を省略した形（`Yavi._string(c -> ...)`）があります。後者は次で説明します。日時の値は ISO-8601 のテキストから、enum は定数名から、`UUID` と `URI` は文字列表現から読み取られます。

## 制約

ラムダはその型の制約オブジェクトを受け取ります。そのメソッドが、その型に適用できる制約です。

```java
Yavi.schema()
        ._string("licensePlate", c -> c.notBlank().greaterThanOrEqual(2).lessThanOrEqual(14).pattern("[A-Z0-9-]+"))
        ._integer("seatCount", c -> c.greaterThanOrEqual(2).lessThanOrEqual(9))
        ._bigDecimal("price", c -> c.positive().fractionDigits(2).integerDigits(6))
        ._boolean("agreed", c -> c.isTrue())
        ._enum("role", Role.class, c -> c.oneOf(Role.ADMIN, Role.MEMBER))
        ._localDate("birthday", c -> c.before(() -> LocalDate.now()))
        .fromMap(Profile::new);
```

その型に存在しない制約は書けないため、`Integer` に対する長さの制約はコンパイルエラーになります。完全な一覧は [組み込みの制約](built-in-constraints.md) にあります。

`notBlank()` または `notEmpty()` を追加しない限り、制約は空文字列を受け入れます。`email()` は値の形式を述べるもので、値が与えられていることではありません。

## 名前とメッセージ

最初の引数は入力におけるプロパティのキーであり、その違反の名前です。ラベルを設定しない限り、メッセージに現れる名前でもあります。

```java
Yavi.schema()
        ._string("name", c -> c.notBlank()).label("Token name")
        ._integer("ttl", c -> c.positive()).typeMessage("\"{0}\" must be a number of seconds")
        ._list("roles", Yavi._string()).required("Select at least one role")
        .fromMap(Token::new);
```

* `label(text)` はプロパティとその要素のすべてのメッセージの `{0}` を置き換えます。違反の `name()`、`path()`、`pointer()` は変わりません。
* `typeMessage(text)` は、別の型の値を報告する `object.type` のメッセージを置き換えます。
* `required(text)` は、キーの不在を報告する `object.required` のメッセージを置き換えます。
* 制約の後の `message(text)` は、その制約のメッセージを置き換えます。

これらは `ViolationMessage` も受け取り、そのキーをリソースバンドルでローカライズできます。

```java
c -> c.notBlank().message(ViolationMessage.of("user.name.notBlank", "Enter your name"))
```

一つのスキーマで同じ名前を二度定義すると、スキーマの構築時に拒否されます。

## 文字列の変換

`String` は制約の前に変換できます。変換は適用される順序で書き、制約と生成される値は変換後のテキストを参照します。

```java
StringValidator<String> emailValidator = Yavi._string("email",
        c -> c.trim().toLowerCase().toNormalForm(Normalizer.Form.NFC).notBlank().email());

emailValidator.validate("  USER@Example.COM "); // Valid[value=user@example.com]
```

変換は `trim()`、`toLowerCase()`、`toUpperCase()`、`toNormalForm(form)`、`transform(f)` です。`null` は変換されません。

その他の型は `preprocess` で変換し、別の型の値は `compose` で適合させます。[値オブジェクトと変換](value-objects.md) を参照してください。

## 単一値のバリデータの再利用

一つの値のバリデータは、プロパティメソッドと同じ名前の `Yavi` のメソッドで書きます。

```java
public static final StringValidator<String> emailValidator =
        Yavi._string("email", c -> c.trim().notBlank().email());
```

スキーマはこれを同じ名前のプロパティとして受け取り、`named(name)` は別のキーで適用します。

```java
ObjectInputValidator<Contact> contactValidator = Yavi.schema()
        ._string(emailValidator)                                  // the key is "email"
        ._string(emailValidator.named("backupEmail")).nullable()  // the key is "backupEmail"
        .fromMap(Contact::new);
```

バリデータは、リストの要素のように場所が名前を与える場合は名前を省略できます。`Yavi._string(c -> c.notBlank())` です。単独で検証すると、違反の名前は空になり、メッセージは名前の代わりに "The value" と述べます。

`_object(name, validator)` は入れ子のバリデータを `name` の下で使います。組み合わせは名前を引き継ぐため、`emailValidator.map(Email::new).named("contact")` と `emailValidator.named("contact").map(Email::new)` は同じです。

## 必須のプロパティ

プロパティは必須です。キーの不在は `object.required` で、明示的な `null` は `object.notNull` で、または `notBlank()` のように `null` を拒否する制約の違反で報告されます。必須のプロパティについて、`fromMap` に渡す関数が `null` で呼ばれることはありません。

`null` または不在を許すプロパティは、`nullable()`、`optional()`、`presence()`、`defaultValue(...)` でそのことを示します。これらの修飾は [省略と既定値](optional-and-defaults.md) で説明します。
