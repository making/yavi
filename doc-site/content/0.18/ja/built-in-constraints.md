# 組み込みの制約

YAVI は対応する型ごとに組み込みの制約を提供します。スキーマスタイルとクラシックスタイルは同じ制約オブジェクトを使うため、各制約はこのページで一度だけ説明します。

```java
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import am.ik.yavi.validator.Yavi;

// Schema style: the validator of a single value
var emailValidator = Yavi._string("email", c -> c.notBlank().email());

// Classic style: a property of an object
record Signup(String email) {}

Validator<Signup> signupValidator = ValidatorBuilder.<Signup>of()
        ._string(Signup::email, "email", c -> c.notBlank().email())
        .build();
```

ラムダはどちらのスタイルでも同じです。スキーマスタイルでは `Yavi.schema()._string("email", c -> ...)` とプロパティとして書くこともできます。クラシックスタイルでは、型付きの `_string` がプロパティの型を選び、型推論の曖昧さを避けます。型を指定しない場合は `constraint` を使います。

## null 値と必須の値

スキーマスタイルとクラシックスタイルでは、`null` の意味が異なります。

* `Yavi.schema()` のプロパティと、`Yavi._string(...)` で作った単一の値のバリデータは、既定で `null` を `object.notNull` で拒否します。`null` になりうる値は `nullable()`（または `optional()`、`defaultValue(...)`、`recover(...)`）でそれを宣言します。[省略と既定値](optional-and-defaults.md)を参照してください。`isNull()` は `nullable()` を付けた場合にだけ使えます。
* `ValidatorBuilder` のバリデータのプロパティは、`null` を拒否する制約がなければ `null` のとき有効で、すべてのチェックがスキップされます。`notNull()`、`notBlank()`、`notEmpty()` は `null` を拒否します。このページの他の制約は拒否しません。
* 文字列のすべての形式は、どちらのスタイルでも空文字列を有効とします。形式は与えられたテキストの見た目を表すもので、テキストが与えられたことではありません。必須にするには `notEmpty()` または `notBlank()` を追加します。

各制約のメッセージキーと `params` の名前を併記します。違反の `params` は引数を名前で保持し、値の名前と違反した値を含みません。位置引数は引き続き `ConstraintViolation#args()` から取得できます。[違反](violations.md#violationdetail)を参照してください。

## 文字列と CharSequence

`String` には `_string` を使います。クラシックスタイルには `CharSequence` 用の `_charSequence` もあります。制約は `am.ik.yavi.constraint.CharSequenceConstraint` にあります。

| 制約 | メソッド | 受け付ける値 | メッセージキー | `params` |
| --- | --- | --- | --- | --- |
| null でない | `notNull()` | null でない任意の値 | `object.notNull` | - |
| null | `isNull()` | `null` のみ | `object.isNull` | - |
| 空でない | `notEmpty()` | null でなく、空でない | `container.notEmpty` | - |
| 空白でない | `notBlank()` | null でなく、空白のみでない | `charSequence.notBlank` | - |
| 固定長 | `fixedSize(int)` | 指定した文字数 | `container.fixedSize` | `size`, `actualSize` |
| 文字数がより大きい | `greaterThan(int)` | 指定した数より多い | `container.greaterThan` | `min`, `actualSize` |
| 文字数が以上 | `greaterThanOrEqual(int)` | 指定した数以上 | `container.greaterThanOrEqual` | `min`, `actualSize` |
| 文字数がより小さい | `lessThan(int)` | 指定した数より少ない | `container.lessThan` | `max`, `actualSize` |
| 文字数が以下 | `lessThanOrEqual(int)` | 指定した数以下 | `container.lessThanOrEqual` | `max`, `actualSize` |
| 含む | `contains(CharSequence)` | 指定した文字列を含むテキスト | `charSequence.contains` | `substring` |
| 前置 | `startsWith(CharSequence)` | 指定した接頭辞で始まるテキスト | `charSequence.startsWith` | `prefix` |
| 後置 | `endsWith(CharSequence)` | 指定した接尾辞で終わるテキスト | `charSequence.endsWith` | `suffix` |
| パターン | `pattern(String)`, `pattern(Pattern)`, `pattern(Supplier<Pattern>)` | 正規表現に一致するテキスト | `charSequence.pattern` | `pattern` |
| メールアドレス | `email()` | メールアドレス | `charSequence.email` | - |
| UUID | `uuid()` | 8-4-4-4-12 の16進数。バージョンとバリアントは検査しない | `charSequence.uuid` | - |
| ULID | `ulid()` | Crockford Base32 の26文字、先頭は `0`-`7`、大文字小文字のどちらでもよい | `charSequence.ulid` | - |
| IP アドレス | `ip()` | IPv4 または IPv6 アドレス | `charSequence.ip` | - |
| IPv4 | `ipv4()` | IPv4 アドレス | `charSequence.ipv4` | - |
| IPv6 | `ipv6()` | IPv6 アドレス。ゾーン ID とプレフィックス長を含む | `charSequence.ipv6` | - |
| URI | `uri()` | 任意のスキームを持つ絶対 URI（`mailto:`、`urn:`、`s3://`）。US-ASCII 以外の文字は拒否する | `charSequence.uri` | - |
| URL | `url()` | `java.net.URI` が解析できる URL | `charSequence.url` | - |
| 制限付き URL | `url(url -> url.protocol(...).host(...).port(...).build())` | プロトコル、ホスト、ポートが指定した集合に含まれる有効な URL | `charSequence.url`, `charSequence.url.protocol`, `charSequence.url.host`, `charSequence.url.port` | `values` |
| Luhn | `luhn()` | Luhn チェックサムを満たす10進数字 | `charSequence.luhn` | - |
| パスワードポリシー | `password(policy -> ...)` | ポリシーを満たすテキスト | `password.required`, `password.optional` | `policy` / `min`, `policies` |
| byte 表現 | `isByte()` | `Byte` として解析できるテキスト | `charSequence.byte` | - |
| short 表現 | `isShort()` | `Short` として解析できるテキスト | `charSequence.short` | - |
| integer 表現 | `isInteger()` | `Integer` として解析できるテキスト | `charSequence.integer` | - |
| long 表現 | `isLong()` | `Long` として解析できるテキスト | `charSequence.long` | - |
| float 表現 | `isFloat()` | `Float` として解析できるテキスト | `charSequence.float` | - |
| double 表現 | `isDouble()` | `Double` として解析できるテキスト | `charSequence.double` | - |
| BigInteger 表現 | `isBigInteger()` | `BigInteger` として解析できるテキスト | `charSequence.biginteger` | - |
| BigDecimal 表現 | `isBigDecimal()` | `BigDecimal` として解析できるテキスト | `charSequence.bigdecimal` | - |
| ローカル日付 | `isoLocalDate()`, `localDate(String pattern)` | パターンに合い、`ResolverStyle.STRICT` で解決できる日付 | `charSequence.localdate` | `pattern` |

`ip()`、`uri()`、`ulid()` は 0.18 で追加されました。`url(builder)` は解析した URL を制限します。`protocol` はプロトコル名、`host` は書かれたとおりに大文字小文字を区別せず比較するホスト名、`port` はポート番号を受け付けます。明示的なポートがない URL は、そのプロトコルの既定ポートと比較されます。

```java
var proxyValidator = Yavi._string("proxy", c -> c.notBlank().ip());
proxyValidator.validate("");                 // "proxy" must not be blank
proxyValidator.validate("192.0.2.1");        // Valid
proxyValidator.validate("2001:db8::1");      // Valid
proxyValidator.validate("example.com");      // Invalid (charSequence.ip)

var homepageValidator = Yavi._string("homepage", c -> c.uri());
homepageValidator.validate("mailto:a@example.com"); // Valid
homepageValidator.validate("/relative/path");       // Invalid
```

`pattern(Supplier<Pattern>)` は検証のたびにサプライヤを呼びます。検証時に組み立てる正規表現に使います。

組み込みのパスワードポリシーは `am.ik.yavi.constraint.password.PasswordPolicy` の `UPPERCASE`、`LOWERCASE`、`ALPHABETS`、`NUMBERS`、`SYMBOLS` です。ポリシーは `required(...)` で必須にするか、`optional(int, ...)` で最低数のうちの1つにします。`uppercase(2)` は大文字2文字を要求します。独自の `PasswordPolicy` も同じように渡せます。

## 文字数のカウントとバイト長

文字列の文字数制約は、正規化と、無視するように指定した異体字セレクタの除去の後に、Unicode コードポイントを数えます。既定の正規化は `Normalizer.Form.NFC` なので、`モシ` と濁点で書いた `モジ` は3文字ではなく2文字として数えられます。テキスト自体を検査する制約（`contains`、`startsWith`、`endsWith`、`pattern`、形式、`codePoints`）は値を正規化しません。

| 制約 | メソッド | 効果 | メッセージキー | `params` |
| --- | --- | --- | --- | --- |
| 正規化 | `normalizer(Normalizer.Form)`, `normalizer(null)` | 文字数の測定前に行う正規化を設定または無効化する | - | - |
| 絵文字 | `emoji()` | 絵文字の並びを拡張書記素クラスタとして1文字に数える。既定では無効 | - | - |
| 異体字セレクタ | `variant(opts -> opts.ivs(...).svs(...).fvs(...))` | 文字数の測定前に指定した異体字セレクタを除去する | - | - |
| コードポイント | `codePoints(...).asWhiteList()`, `.asBlackList()` | 指定したコードポイントを許可または禁止する | `codePoints.asWhiteList`, `codePoints.asBlackList` | `notAllowed` |
| バイト長 | `asByteArray()`, `asByteArray(Charset)` の後に `fixedSize`、`greaterThan`、`greaterThanOrEqual`、`lessThan`、`lessThanOrEqual` | エンコード後のバイト長を測定する | `byteSize.fixedSize`, `byteSize.greaterThan`, `byteSize.greaterThanOrEqual`, `byteSize.lessThan`, `byteSize.lessThanOrEqual` | `size`, `actualSize` / `min`, `actualSize` / `max`, `actualSize` |

`emoji()` は負荷が高いため既定では無効です。0.18 では拡張書記素クラスタを数えるため、`"\r\n"` や、濁点付きの半角カタカナは1文字として数えられます。`Emoji` クラスは削除されました。

`normalizer`、`emoji()`、`variant(...)` はそれ自体が制約ではありません。その結果に適用した文字数制約のメッセージが報告されます。コードポイントの集合は `am.ik.yavi.constraint.charsequence.CodePoints` で表し、組み込みの集合は `AsciiCodePoints` と `UnicodeCodePoints` から利用できます。

絵文字の並びが1文字になるのは `emoji()` を追加した場合だけです。追加しなければコードポイントごとに数えます。`asByteArray()` は文字セットを指定しなければ UTF-8 のバイト長を測定します。バイト長に上限がある列に使います。

```java
var messageValidator = Yavi._string("text", c -> c.emoji().lessThanOrEqual(3)
        .asByteArray().lessThanOrEqual(16));
messageValidator.validate("I❤️☕️️");        // Valid
messageValidator.validate("❤️❤️❤️️️");     // Invalid (byteSize.lessThanOrEqual)
```

## 整数

クラシックスタイルでは `_byte`、`_short`、`_integer`、`_long`、`_character`、`_bigInteger` を対応する型に使います。スキーマスタイルには `_short`、`_integer`、`_long`、`_bigInteger` があります。`Character` の制約は文字の数値を比較します。`positive` などの符号の制約が使うゼロは `Character.MIN_VALUE` です。

| 制約 | メソッド | 受け付ける値 | メッセージキー | `params` |
| --- | --- | --- | --- | --- |
| null でない | `notNull()` | null でない任意の値 | `object.notNull` | - |
| null | `isNull()` | `null` のみ | `object.isNull` | - |
| より大きい | `greaterThan(V)` | 境界より大きい | `numeric.greaterThan` | `min` |
| 以上 | `greaterThanOrEqual(V)` | 境界以上 | `numeric.greaterThanOrEqual` | `min` |
| より小さい | `lessThan(V)` | 境界より小さい | `numeric.lessThan` | `max` |
| 以下 | `lessThanOrEqual(V)` | 境界以下 | `numeric.lessThanOrEqual` | `max` |
| 正 | `positive()` | ゼロより大きい | `numeric.positive` | - |
| 正またはゼロ | `positiveOrZero()` | ゼロ以上 | `numeric.positiveOrZero` | - |
| 負 | `negative()` | ゼロより小さい | `numeric.negative` | - |
| 負またはゼロ | `negativeOrZero()` | ゼロ以下 | `numeric.negativeOrZero` | - |
| 倍数 | `multipleOf(V)` | 除数で割り切れる値 | `numeric.multipleOf` | `divisor` |

`multipleOf` は `Byte`、`Short`、`Integer`、`Long`、`BigInteger`、`BigDecimal` で利用できます。`Character`、`Double`、`Float` にはありません。

```java
var quantityValidator = Yavi._integer("quantity", c -> c.multipleOf(12));
quantityValidator.validate(24);  // Valid
quantityValidator.validate(30);  // Invalid ("quantity" must be a multiple of 12)
```

## BigInteger と BigDecimal

`BigInteger` と `BigDecimal` は、`multipleOf` を含む整数の制約を持ちます。`BigDecimal` にはさらに桁数に関する制約があります。

| 制約 | メソッド | 受け付ける値 | メッセージキー | `params` |
| --- | --- | --- | --- | --- |
| 小数部の桁数 | `fractionDigits(int)` | 小数点以下の桁数が指定値以下。末尾のゼロは数えない | `numeric.fractionDigits` | `max` |
| 整数部の桁数 | `integerDigits(int)` | 小数点以上の桁数が指定値以下。`0.5` のゼロは数えない | `numeric.integerDigits` | `max` |

比較は `BigDecimal#compareTo` を使い、スケールを無視します。`1.0` と `1.00` は同じ値で、どちらも小数部の桁数はゼロです。制約はスケールを変えません。生成する値のスケールを固定するには `preprocess` または `map` を使います。

```java
var priceValidator = Yavi._bigDecimal("price", c -> c.fractionDigits(2).integerDigits(6));
priceValidator.validate(new BigDecimal("1.50"));  // Valid (1.5 has one fraction digit)
priceValidator.validate(new BigDecimal("1.505")); // Invalid (numeric.fractionDigits)
```

## Double と Float

`_double` と `_float` は、`notNull`、`isNull`、`greaterThan`、`greaterThanOrEqual`、`lessThan`、`lessThanOrEqual`、`positive`、`positiveOrZero`、`negative`、`negativeOrZero` を持ち、メッセージキーは整数と同じです。2進小数の剰余は正確でないため、`multipleOf` はありません。`NaN` はどの境界も満たさず、すべての比較で拒否されます。

## 日時

日時の制約は次の型で利用でき、クラシックスタイルのビルダーショートカットがあります。スキーマスタイルでは同じ名前のプロパティメソッドを使います。たとえば `Yavi.schema()._localDate("date", c -> c.past())` です。

| 型 | 制約クラス | クラシックのショートカット |
| --- | --- | --- |
| `LocalDate` | `LocalDateConstraint` | `_localDate` |
| `LocalTime` | `LocalTimeConstraint` | `_localTime` |
| `LocalDateTime` | `LocalDateTimeConstraint` | `_localDateTime` |
| `OffsetDateTime` | `OffsetDateTimeConstraint` | `_offsetDateTime` |
| `Instant` | `InstantConstraint` | `_instant` |
| `Year` | `YearConstraint` | `_year` |
| `YearMonth` | `YearMonthConstraint` | `_yearMonth` |
| `ZonedDateTime` | `ZonedDateTimeConstraint` | `_zonedDateTime` |

| 制約 | メソッド | 受け付ける値 | メッセージキー | `params` |
| --- | --- | --- | --- | --- |
| 過去 | `past()`, `past(Clock)` | 現在より前 | `temporal.past` | - |
| 過去または現在 | `pastOrPresent()`, `pastOrPresent(Clock)` | 現在以前 | `temporal.pastOrPresent` | - |
| 未来 | `future()`, `future(Clock)` | 現在より後 | `temporal.future` | - |
| 未来または現在 | `futureOrPresent()`, `futureOrPresent(Clock)` | 現在以降 | `temporal.futureOrPresent` | - |
| より前 | `before(Supplier<T>)` | 指定した境界より前 | `temporal.before` | `other` |
| 以前 | `beforeOrEqual(Supplier<T>)` | 指定した境界以前 | `temporal.beforeOrEqual` | `other` |
| より後 | `after(Supplier<T>)` | 指定した境界より後 | `temporal.after` | `other` |
| 以後 | `afterOrEqual(Supplier<T>)` | 指定した境界以後 | `temporal.afterOrEqual` | `other` |
| 間 | `between(Supplier<T>, Supplier<T>)` | 指定した2つの境界の間（両端を含まない） | `temporal.between` | `rangeFrom`, `rangeTo` |
| フィールド | `fieldPredicate(TemporalField, LongPredicate)` | 述語が受け付けるフィールドの値 | `temporal.field` | `field` |

`before`、`beforeOrEqual`、`after`、`afterOrEqual`、`between` には `boolean memoize` 引数を持つオーバーロードがあります。既定は `true` で、サプライヤは最大1回だけ呼ばれ、同じ値が比較とメッセージに使われます。`rangeFrom` が `rangeTo` より後の場合、`between` は `IllegalArgumentException` を投げます。`Clock` のオーバーロードは、決定的なテストと、システム既定のゾーン以外の時刻源のためにあります。

`null` 値は有効で、日時のチェックはすべてスキップされます。

## Boolean

| 制約 | メソッド | 受け付ける値 | メッセージキー | `params` |
| --- | --- | --- | --- | --- |
| null でない | `notNull()` | null でない任意の値 | `object.notNull` | - |
| null | `isNull()` | `null` のみ | `object.isNull` | - |
| true | `isTrue()` | `true` | `boolean.isTrue` | - |
| false | `isFalse()` | `false` | `boolean.isFalse` | - |

## コレクションと配列

`Collection` には `_collection`、配列には `_objectArray`、`_booleanArray`、`_byteArray`、`_charArray`、`_shortArray`、`_intArray`、`_longArray`、`_floatArray`、`_doubleArray` を使います。`_list`、`_map`、`forEach` を含む、リスト、マップ、配列の要素や値の検証は [オブジェクトとコレクション](objects-and-collections.md#lists)で説明しています。

| 制約 | メソッド | 受け付ける値 | メッセージキー | `params` |
| --- | --- | --- | --- | --- |
| null でない | `notNull()` | null でない任意の値 | `object.notNull` | - |
| null | `isNull()` | `null` のみ | `object.isNull` | - |
| 空でない | `notEmpty()` | 空でないコレクションまたは配列 | `container.notEmpty` | - |
| 固定サイズ | `fixedSize(int)` | 指定した要素数 | `container.fixedSize` | `size`, `actualSize` |
| サイズがより大きい | `greaterThan(int)` | 指定した数より多い | `container.greaterThan` | `min`, `actualSize` |
| サイズが以上 | `greaterThanOrEqual(int)` | 指定した数以上 | `container.greaterThanOrEqual` | `min`, `actualSize` |
| サイズがより小さい | `lessThan(int)` | 指定した数より少ない | `container.lessThan` | `max`, `actualSize` |
| サイズが以下 | `lessThanOrEqual(int)` | 指定した数以下 | `container.lessThanOrEqual` | `max`, `actualSize` |
| 含む | `contains(E)` | 指定した要素を含むコレクションまたは配列 | `collection.contains`, `array.contains` | `element` |
| 一意 | `unique()` | 重複のない要素 | `collection.unique` | `duplicates` |
| キーで一意 | `unique(Function<? super E, ?>)` | キーが一意な要素 | `collection.uniqueBy` | `duplicates` |
| すべて含む | `containsAll(Collection<? extends E>)` | 指定したすべての要素 | `collection.containsAll` | `values`, `missing` |

`unique(key)` は重複したキーを報告します。`containsAll` は `Set` とそれ以外のコレクションで同じ意味になります。`null` の配列は有効で、`notEmpty()` を除いてチェックはスキップされます。

```java
var productsValidator = Yavi.schema()._list("products", productValidator, c -> c.unique(Product::id));
// "products" must be unique. [APPLE] is/are duplicated.
```

## Map

`Map` には `_map` を使います。

| 制約 | メソッド | 受け付ける値 | メッセージキー | `params` |
| --- | --- | --- | --- | --- |
| null でない | `notNull()` | null でない任意の値 | `object.notNull` | - |
| null | `isNull()` | `null` のみ | `object.isNull` | - |
| 空でない | `notEmpty()` | 空でないマップ | `container.notEmpty` | - |
| 固定サイズ | `fixedSize(int)` | 指定したエントリ数 | `container.fixedSize` | `size`, `actualSize` |
| サイズがより大きい | `greaterThan(int)` | 指定した数より多い | `container.greaterThan` | `min`, `actualSize` |
| サイズが以上 | `greaterThanOrEqual(int)` | 指定した数以上 | `container.greaterThanOrEqual` | `min`, `actualSize` |
| サイズがより小さい | `lessThan(int)` | 指定した数より少ない | `container.lessThan` | `max`, `actualSize` |
| サイズが以下 | `lessThanOrEqual(int)` | 指定した数以下 | `container.lessThanOrEqual` | `max`, `actualSize` |
| キーを含む | `containsKey(K)` | 指定したキーを含むマップ | `map.containsKey` | `key` |
| 値を含む | `containsValue(V)` | 指定した値を含むマップ | `map.containsValue` | `value` |

## Object

その他のオブジェクトには `_object` を使います。クラシックスタイルでは、`constraintOnTarget` が検証対象のオブジェクト自体に制約を適用します。

| 制約 | メソッド | 受け付ける値 | メッセージキー | `params` |
| --- | --- | --- | --- | --- |
| null でない | `notNull()` | null でない任意の値 | `object.notNull` | - |
| null | `isNull()` | `null` のみ | `object.isNull` | - |
| 等しい | `equalTo(Object)` | 指定した値と等しい値 | `object.equalTo` | `value` |
| いずれか | `oneOf(Collection)` | 指定した値のいずれか | `object.oneOf` | `values` |
| いずれでもない | `notOneOf(Collection)` | 指定した値のいずれでもない | `object.notOneOf` | `values` |
| パスワードポリシー | `password(policy -> ...)` | ポリシーを満たす値。ポリシーはオブジェクト全体を検査できる | `password.required`, `password.optional` | `policy` / `min`, `policies` |

## Enum

Enum には `_enum` を使います。スキーマスタイルとクラシックスタイルのどちらも Enum の型を渡します。

```java
var statusValidator = Yavi._enum("status", Status.class, c -> c.oneOf(Status.ACTIVE));
statusValidator.validate(Status.ACTIVE);   // Valid
statusValidator.validate(Status.INACTIVE); // Invalid ("status" must be one of the following values: [ACTIVE])
```

| 制約 | メソッド | 受け付ける値 | メッセージキー | `params` |
| --- | --- | --- | --- | --- |
| null でない | `notNull()` | null でない任意の値 | `object.notNull` | - |
| null | `isNull()` | `null` のみ | `object.isNull` | - |
| いずれか | `oneOf(E...)` | 指定した定数のいずれか。リストは空にできない | `object.oneOf` | `values` |

クラシックスタイルでは、`notNull()` を追加しなければ `null` の Enum 値は有効です。`EnumValidatorBuilder` と `EnumValidator` は単一の Enum 値を検証します。
