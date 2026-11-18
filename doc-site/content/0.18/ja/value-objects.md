# 値オブジェクトと変換

検証済みの値を、オブジェクトに渡す前にドメイン型へ変換できます。このページでは `map`、可逆マッピング、`preprocess`、`compose`、名前付きバリデータの再利用を扱います。

## プロパティの値オブジェクトへのマッピング

プロパティの後の `map(f)` は、検証済みの値を値オブジェクトなどにマップします。プロパティはその場所に書いたままです。

```java
record Email(String value) {}
record Nickname(String value) {}
record Age(int value) {}
record User(Email email, Optional<Nickname> nickname, Age age) {}

ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email()).map(Email::new)
        ._string("nickname", c -> c.notBlank()).map(Nickname::new).optional()
        ._integer("age", c -> c.greaterThanOrEqual(0)).map(Age::new).defaultValue(new Age(20))
        .fromMap(User::new);
```

* 関数には制約を満たす値が渡されます。`null` が渡されることはありません。
* プロパティは以前と同じように読み取られ、報告され、記述されます。キー、`object.type` の型、ラベル、JSON Schema は `map` の前の値のものです。
* `map` は `null` が生成するものを決める修飾より前に書き、それらの修飾はマップ後の値に適用されます。`.optional()` は `Optional<Nickname>` を生成し、既定値はマップ後の値です。`.optional().map(f)` と `.defaultValue(v).map(f)` はコンパイルできません。
* `label`、`typeMessage`、`withheld()` は `map` の前でも後でも書けます。
* 関数は例外を投げられます。検査例外は呼び出し元が包みます。失敗しうる検証は `flatFromMap` で書き、[オブジェクト全体の規則](cross-field-rules.md) で説明します。

## 可逆マッピング

オブジェクトを書き戻すスキーマには逆関数が必要です。`Yavi.reversibleSchema()` は `map(f, g)` を受け取り、`g` は `f` の入力を返します。

```java
ReversibleValueValidator<String, Email> emailValidator =
        Yavi._string("email", c -> c.notBlank().email()).map(Email::new, Email::value);
```

型が、バリデータが逆変換を知っているかどうかを示します。`map(f)` は `ValueValidator` で、`map(f, g)` は `ReversibleValueValidator` です。可逆スキーマはこれを受け取り、通常のスキーマも受け入れます。`toMap`、`reverse`、ビューについては [オブジェクトの書き戻し](writing-back.md) を参照してください。

## 前処理

`preprocess(f)` は、同じ型の入力を制約の前に変換します。制約と生成される値は変換後の値を参照し、`null` は変換されません。

```java
BigDecimalValidator<BigDecimal> priceValidator = Yavi._bigDecimal("price", c -> c.positive())
        .preprocess(BigDecimal::stripTrailingZeros);
```

後に追加した関数が先に適用されます。`validator.preprocess(f).preprocess(g)` は `g`、次に `f` を適用します。`String` は通常、制約を書く場所で `c.trim()`、`c.toLowerCase()` などの [スキーマの定義](defining-a-schema.md) の変換を使って変換します。

## バリデータの合成

`compose(f)` はバリデータを別の入力型に適合させます。関数が入力を変換し、結果は以前と同じように検証されます。

```java
ValueValidator<byte[], String> tokenValidator = Yavi._string("token", c -> c.notBlank())
        .compose(bytes -> Base64.getEncoder().encodeToString(bytes));
```

合成したバリデータの入力型は不明であり、その記述も不明です。文書のルートや `anyOf` の代替のように入力型が必要な場所で使う場合は、`untyped(type)` で与えます。

## ドメインプリミティブ

独自の規則を持つ値オブジェクトは一度だけ定義し、プロパティ、要素、単独の値として使います。

```java
public final class Email {

    public static final StringValidator<Email> VALIDATOR =
            Yavi._string("email", c -> c.trim().notBlank().email()).map(Email::new);

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String value) {
        return VALIDATOR.validated(value);
    }

    public String value() {
        return value;
    }

}
```

スキーマでは `._string(Email.VALIDATOR)` が値を読み取り、検証し、値オブジェクトを生成します。呼び出し側で `map(Email::new)` を書く必要はありません。プライベートなコンストラクタはクラス内に留まり、規則を繰り返すこともありません。`Email.VALIDATOR` の変更はそれを使うすべてのスキーマに届きます。

## 名前付きバリデータの再利用

バリデータは構築時の名前を保持します。`named(name)` は同じバリデータを別の名前で返すため、規則を一度定義し、各プロパティのキーで使えます。

```java
ObjectInputValidator<Contact> contactValidator = Yavi.schema()
        ._string(Email.VALIDATOR)                                  // the key and the name are "email"
        ._string(Email.VALIDATOR.named("backupEmail")).nullable()  // the key and the name are "backupEmail"
        ._object("replyTo", Email.VALIDATOR)                       // the same as named("replyTo")
        .fromMap(Contact::new);
```

名前は、違反、そのメッセージ、`description()` におけるバリデータの名前を置き換えます。オブジェクトやリストのように独自の名前を持たない値は、その名前の下に位置づけられます（`home.street`）。組み合わせは名前を引き継ぐため、`map`、`nullable()`、`untyped()` の前でも後でも指定できます。
