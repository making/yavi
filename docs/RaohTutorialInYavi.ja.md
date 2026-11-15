# YAVI で書く Raoh のチュートリアル

[English](RaohTutorialInYavi.md)

このページは、[Raoh のデコーダの実践ガイド](https://github.com/raoh-project/raoh-java/blob/main/docs/tutorial.md)を
YAVI 0.18.0 で書き直したものです。節の番号をそのままにしてあるので、両者を並べて読めます。
各例は jshell で実行し、コメントにはその出力を示しました。`python3 docs/verify-examples.py` は、
その例を再実行して出力を比較します。[Raoh との比較](ComparisonWithRaoh.md) では、2 つの API の
対応を表にまとめています。

* [0. セットアップ](#0-セットアップ)
* [1. プリミティブ値の検証](#1-プリミティブ値の検証)
* [2. 制約の追加](#2-制約の追加)
* [3. ドメインプリミティブへの変換](#3-ドメインプリミティブへの変換)
* [4. オブジェクトの検証](#4-オブジェクトの検証)
* [5. ネストしたオブジェクト](#5-ネストしたオブジェクト)
* [6. フラットなデータの構造化](#6-フラットなデータの構造化)
* [7. リスト](#7-リスト)
* [8. マップ](#8-マップ)
* [9. 省略可能、nullable、3 状態のプロパティ](#9-省略可能nullable3-状態のプロパティ)
* [10. enum](#10-enum)
* [11. ユニオン型](#11-ユニオン型)
* [12. 複数フィールドにまたがる検証](#12-複数フィールドにまたがる検証)
* [13. 条件付き検証](#13-条件付き検証)
* [14. 異なる入力の結果の結合](#14-異なる入力の結果の結合)
* [15. リストの行の検証](#15-リストの行の検証)
* [16. 既定値と復旧](#16-既定値と復旧)
* [17. 不明なプロパティの拒否](#17-不明なプロパティの拒否)
* [18. 再帰的な構造](#18-再帰的な構造)
* [19. 違反の扱い](#19-違反の扱い)
* [20. シナリオ: ユーザー登録 API](#20-シナリオ-ユーザー登録-api)
* [21. リストからマップへの変換](#21-リストからマップへの変換)
* [22. 既知のプロパティと残りのキー](#22-既知のプロパティと残りのキー)
* [23. パスワードの確認](#23-パスワードの確認)
* [24. ページネーションとクエリパラメータ](#24-ページネーションとクエリパラメータ)
* [25. 金額と通貨](#25-金額と通貨)
* [26. CSV の取り込み](#26-csv-の取り込み)
* [27. 設定ファイル](#27-設定ファイル)
* [28. タプル](#28-タプル)
* [29. オブジェクトをマップに書き戻す](#29-オブジェクトをマップに書き戻す)
* [30. null と既定値の書き込み](#30-null-と既定値の書き込み)
* [31. ネストしたオブジェクトの書き込み](#31-ネストしたオブジェクトの書き込み)
* [32. タグ付きユニオンの書き込み](#32-タグ付きユニオンの書き込み)

## なぜ値を返すバリデータなのか

リクエストボディ、CSV ファイル、別の API の応答、テーブルの行のように外部から届くデータは、
アプリケーションが想定する型ではありません。YAVI のバリデータは、そのような入力を取り、
ドメインオブジェクトか、パス付きの違反のすべてかを返します。

```text
input (typed arguments / Map / JSON tree / request parameters)
    ↓  validator.validate(input)
Valid[domain object]  or  Invalid[violations with a name, a path, a message key and a message]
```

Raoh のデコーダはここでは `ValueValidator<I, T>`、Raoh の `decode` は `validate`、
Raoh の `Ok` / `Err` は `Validated.Valid` / `Validated.Invalid` にあたります。

## 0. セットアップ

YAVI の jar だけで jshell を起動します。ほかのライブラリは要りません。

```bash
jshell --class-path yavi-0.18.0.jar
```

```java
import am.ik.yavi.arguments.*;
import am.ik.yavi.core.*;
import am.ik.yavi.input.*;
import am.ik.yavi.message.*;
import am.ik.yavi.validator.Yavi;
import java.time.*;
```

* jshell は、行がスニペットを完成させた時点で評価します。例のように `.` の前で折り返した
  チェーンは、1 行にまとめて貼り付けます。
* jshell は sealed インターフェースとそのサブタイプを同じスニペットに要求するので、例では
  通常の `interface` を宣言しています。

## 1. プリミティブ値の検証

最小の単位は 1 つの値のバリデータです。`Yavi._string()`、`Yavi._integer()`、
`Yavi._bigDecimal()`、`Yavi._boolean()` などがあります。

```java
Yavi._string().validate("hello")
// ==> Valid[value=hello]

Yavi._integer().validate(42)
// ==> Valid[value=42]

Yavi._boolean().validate(true)
// ==> Valid[value=true]

Yavi._bigDecimal().validate(new BigDecimal("19.99"))
// ==> Valid[value=19.99]

Yavi._double().validate(3.14)
// ==> Valid[value=3.14]

Yavi._float().validate(2.5f)
// ==> Valid[value=2.5]

Yavi._object(byte[].class).validate(new byte[]{1, 2, 3})
// ==> Valid[value=[B@...]
```

Raoh の `ObjectDecoders` のデコーダと違い、これらのバリデータには型があります。
`Yavi._integer().validate("not a number")` はコンパイルできません。`untyped()` は `Object` の
バリデータを返し、最初に型を検査します。

```java
Yavi._integer().untyped().validate("not a number")
// ==> Invalid[errors=[The value must be of type integer (object.type)]]

Yavi._integer().untyped().validate(42L)
// ==> Valid[value=42]  (値が保たれる限り数値として受け付ける)

Yavi._bigDecimal().untyped().validate(19.99)
// ==> Valid[value=19.99]
```

失敗した結果は、違反をそのまま読める形で示します。値の JSON Pointer（検証した値そのものでは
空）、メッセージ、メッセージキーです。`untyped()` を直接書く場面はほとんどありません。
プロパティの型はスキーマ（[4 節](#4-オブジェクトの検証)）が検査します。

## 2. 制約の追加

制約は、バリデータにラムダ式で与えます。制約に違反すると、検証は失敗します。

### 文字列の制約

```java
Yavi._string(c -> c.trim().toLowerCase().email()).validate("  USER@Example.COM  ")
// ==> Valid[value=user@example.com]

Yavi._string(c -> c.trim().toLowerCase().email()).validate("not-email")
// ==> Invalid[errors=[The value must be a valid email address (charSequence.email)]]

Yavi._string(c -> c.greaterThanOrEqual(3).lessThanOrEqual(20)).validate("ab")
// ==> Invalid[errors=[The size of the value must be greater than or equal to 3. The given size is 2 (container.greaterThanOrEqual)]]

Yavi._string(c -> c.pattern("^\\d{3}-\\d{4}$")).validate("123-4567")
// ==> Valid[value=123-4567]

Yavi._string(c -> c.uuid()).validate("550e8400-e29b-41d4-a716-446655440000")
// ==> Valid[value=550e8400-e29b-41d4-a716-446655440000]  (String のまま)

Yavi._object(UUID.class).untyped().validate("550e8400-e29b-41d4-a716-446655440000")
// ==> Valid[value=550e8400-e29b-41d4-a716-446655440000]  (UUID になる)
```

* 変換（`trim()`、`toLowerCase()`）は最初の制約より前に書きます。`c.email().trim()` は
  コンパイルできません。
* 文字列の長さは size です。`greaterThanOrEqual(3)` は Raoh の `minLength(3)`、
  `lessThanOrEqual(20)` は Raoh の `maxLength(20)`、`fixedSize(n)` は Raoh の
  `fixedLength(n)` にあたります。
* 制約は値の型を変えません。別の型の値は、その型のバリデータが読みます。
  `Yavi._object(UUID.class)` は正規形の文字列を読みます。

### 正規化

size の制約は NFC 形式のコードポイントを数えるので、分解された `が`（U+304B U+3099）は、
変換なしでも 1 文字です。値そのものは変わらず、`toNormalForm(form)` が正規化します。

```java
String nfd = "が";

Yavi._string(c -> c.lessThanOrEqual(1)).validate(nfd)
// ==> Valid[value=が]  (2 文字のまま)

Yavi._string(c -> c.toNormalForm(java.text.Normalizer.Form.NFC).lessThanOrEqual(1)).validate(nfd).value().length()
// ==> 1

Yavi._string(c -> c.normalizer(null).lessThanOrEqual(1)).validate(nfd)
// ==> Invalid[errors=[The size of the value must be less than or equal to 1. The given size is 2 (container.lessThanOrEqual)]]
```

`c.normalizer(form)` は数える形式を選び、`c.normalizer(null)` は正規化しません。

### URI / URL の検証

```java
Yavi._string(c -> c.uri()).validate("file:///tmp/data.csv")
// ==> Valid[value=file:///tmp/data.csv]

Yavi._string(c -> c.uri()).validate("foo/bar")
// ==> Invalid[errors=[The value must be a valid URI (charSequence.uri)]]

Yavi._string(c -> c.url()).validate("https://example.com/api")
// ==> Valid[value=https://example.com/api]

Yavi._string(c -> c.url()).validate("ftp://example.com")
// ==> Valid[value=ftp://example.com]

Yavi._string(c -> c.url(u -> u.protocol("http", "https").build())).validate("ftp://example.com")
// ==> Invalid[errors=[The protocol of the value must be one of the following values: [http, https] (charSequence.url.protocol)]]

Yavi._object(URI.class).untyped().validate("https://example.com/api")
// ==> Valid[value=https://example.com/api]  (URI になる)
```

`c.uri()` は、Raoh の `uri()` と同じくスキームを必要とします。`c.url()` は、プロトコルを
与えなければ `java.net.URL` が扱うすべてのプロトコルを受け付けます。どちらも `String` のまま
です。`Yavi._object(URI.class)` は `URI` を作り、相対 URI を含む任意の URI 参照を受け付けます。

### 数値の制約

```java
Yavi._integer(c -> c.greaterThanOrEqual(0).lessThanOrEqual(150)).validate(25)
// ==> Valid[value=25]

Yavi._integer(c -> c.greaterThanOrEqual(0).lessThanOrEqual(150)).validate(300)
// ==> Invalid[errors=[The value must be less than or equal to 150 (numeric.lessThanOrEqual)]]

Yavi._integer(c -> c.positive()).validate(-1)
// ==> Invalid[errors=[The value must be positive (numeric.positive)]]

Yavi._bigDecimal(c -> c.fractionDigits(4)).validate(new BigDecimal("0.1234"))
// ==> Valid[value=0.1234]
```

### 日時の制約

日時のバリデータは ISO-8601 形式の文字列を読むので、`string().date()` はありません。

```java
Yavi._localDate().untyped().validate("2025-06-15")
// ==> Valid[value=2025-06-15]

Yavi._instant().untyped().validate("2025-06-15T10:30:00Z")
// ==> Valid[value=2025-06-15T10:30:00Z]

Yavi._localDate(c -> c.after(() -> LocalDate.of(2020, 1, 1))).validate(LocalDate.of(2025, 6, 15))
// ==> Valid[value=2025-06-15]

Yavi._localDate(c -> c.before(() -> LocalDate.of(2030, 1, 1))).validate(LocalDate.of(2035, 1, 1))
// ==> Invalid[errors=[The value has to be before 2030-01-01 (temporal.before)]]

Yavi._localDate(c -> c.between(() -> LocalDate.of(2020, 1, 1), () -> LocalDate.of(2030, 12, 31))).validate(LocalDate.of(2025, 6, 15))
// ==> Valid[value=2025-06-15]
```

境界はサプライヤーです。Raoh が `past()` と `future()` を持たないのは意図的な設計です。
YAVI にはこの 2 つがあり、`Clock` を取るので、結果を再現できます。

```java
var now = Instant.parse("2025-06-15T12:00:00Z");
Yavi._instant(c -> c.before(() -> now)).validate(Instant.parse("2025-06-15T11:00:00Z"))
// ==> Valid[value=2025-06-15T11:00:00Z]

var clock = Clock.fixed(now, ZoneOffset.UTC);
Yavi._instant(c -> c.past(clock)).validate(Instant.parse("2025-06-15T11:00:00Z"))
// ==> Valid[value=2025-06-15T11:00:00Z]
```

### 文字列から型への変換 (coerce)

Raoh では変換がデコーダの一部です（`string().toInt().range(0, 150)`）。YAVI では変換は入力の
側にあり、`Integer` のバリデータはそのままで、リーダーが型を表すテキストとして文字列を受け
取ります。

```java
InputReader<Object> text = InputReader.ofJdkTypes().coercingStrings();

Yavi._integer().readingWith(text).validate("42")
// ==> Valid[value=42]

Yavi._integer(c -> c.greaterThanOrEqual(0).lessThanOrEqual(150)).readingWith(text).validate("25")
// ==> Valid[value=25]

Yavi._integer().readingWith(text).validate("abc")
// ==> Invalid[errors=[The value must be of type integer (object.type)]]

Yavi._long().readingWith(text).validate("9999999999")
// ==> Valid[value=9999999999]

Yavi._bigDecimal(c -> c.fractionDigits(2)).readingWith(text).validate("19.99")
// ==> Valid[value=19.99]

Yavi._bigDecimal().readingWith(text).validate("abc")
// ==> Invalid[errors=[The value must be of type number (object.type)]]
```

リーダーに別の規則を与えない限り、規則は厳格です。真偽値は `true` か `false` で、数値の前後
に空白は置けません。人が書いたテキストには、`StringCoercion` で規則を緩めます。

```java
Yavi._boolean().readingWith(text).validate("true")
// ==> Valid[value=true]

Yavi._boolean().readingWith(text).validate("1")
// ==> Invalid[errors=[The value must be of type boolean (object.type)]]

InputReader<Object> form = InputReader.ofJdkTypes().coercingStrings(StringCoercion.strict().trimming().booleanWords());

Yavi._boolean().readingWith(form).validate("1")
// ==> Valid[value=true]

Yavi._boolean().readingWith(form).validate("no")
// ==> Valid[value=false]

Yavi._boolean().readingWith(form).validate("maybe")
// ==> Invalid[errors=[The value must be of type boolean (object.type)]]

// isTrue() は同意（利用規約など）を要求する
Yavi._boolean(c -> c.isTrue()).readingWith(form).validate("false")
// ==> Invalid[errors=[The value must be true (boolean.isTrue)]]

Yavi._integer().readingWith(form).validate("  42  ")
// ==> Valid[value=42]
```

`booleanWords()` は `true`/`1`/`yes`/`on` と `false`/`0`/`no`/`off` を大文字と小文字を区別
せずに受け付け、`trimming()` はすべてのテキストの前後の空白を除きます。文字列のマップ全体は
`readingParameters()` で読みます（[24 節](#24-ページネーションとクエリパラメータ)）。

## 3. ドメインプリミティブへの変換

`map` は、検証済みの値をドメインの型に変換します。アプリケーションの内部には、検証済みの
ドメインの型の値だけが流れます。

```java
record Email(String value) {}
record Age(int value) {}
record UserId(UUID value) {}

var emailValidator = Yavi._string(c -> c.trim().toLowerCase().notBlank().email()).map(Email::new);
var ageValidator = Yavi._integer(c -> c.greaterThanOrEqual(0).lessThanOrEqual(150)).map(Age::new);
var userIdValidator = Yavi._object(UUID.class).map(UserId::new);

emailValidator.validate("  ALICE@example.com  ")
// ==> Valid[value=Email[value=alice@example.com]]

ageValidator.validate(25)
// ==> Valid[value=Age[value=25]]

userIdValidator.untyped().validate("550e8400-e29b-41d4-a716-446655440000")
// ==> Valid[value=UserId[value=550e8400-e29b-41d4-a716-446655440000]]
```

`email()` は、ほかの形式の制約と同じく空文字列を受け付けます。値が与えられていることは
`notBlank()` で表します。

## 4. オブジェクトの検証

`Yavi.schema()` でオブジェクトのプロパティに名前を付け、`fromMap` で、その名前どおりに
`Map<String, Object>` を読み、型を検査してコンストラクタを呼ぶバリデータにします。Raoh の
`combine(field(...), ...).map(f)` がこのチェーンにあたります。すべてのプロパティの違反が
集められます。

```java
record User(UserId id, Email email, Age age) {}

var userValidator = Yavi.schema()
        ._object("id", userIdValidator)
        ._object("email", emailValidator)
        ._object("age", ageValidator)
        .fromMap(User::new);

userValidator.validate(Map.of(
        "id", "550e8400-e29b-41d4-a716-446655440000",
        "email", "alice@example.com",
        "age", 30))
// ==> Valid[value=User[id=UserId[value=550e8400-e29b-41d4-a716-446655440000], email=Email[value=alice@example.com], age=Age[value=30]]]

userValidator.validate(Map.of("id", "not-uuid", "email", "invalid", "age", 300))
// ==> Invalid[errors=[/id: "id" must be of type UUID (object.type), /email: "email" must be a valid email address (charSequence.email), /age: "age" must be less than or equal to 150 (numeric.lessThanOrEqual)]]
```

`_object(name, validator)` は、バリデータを名前付きで使います。プロパティはその場で書くことも
でき（`._string("email", c -> c.notBlank().email())`）、そのあとの `map` がドメインの型を
作ります。Raoh の `field("email", string().email().map(Email::new))` にあたります。

```java
Yavi.schema()
        ._object("id", UUID.class).map(UserId::new)
        ._string("email", c -> c.trim().toLowerCase().notBlank().email()).map(Email::new)
        ._integer("age", c -> c.greaterThanOrEqual(0).lessThanOrEqual(150)).map(Age::new)
        .fromMap(User::new);
```

`map` はプロパティの修飾子の 1 つで、`null` が何になるかを決める修飾子より前に書きます。
`._string("nickname").map(Nickname::new).optional()` は `Optional<Nickname>` になります。

スキーマはマップに縛られません。同じプロパティを `apply(User::new)` で終えれば型付きの引数に
なり、`fromMap` のバリデータは `readingWith` で JSON のツリーを、`readingParameters` で
リクエストパラメータを読みます。Raoh は `MapDecoders` か `JsonDecoders` を静的インポートして、
先に入力を選びます。

## 5. ネストしたオブジェクト

Raoh の `nested(decoder)` は `_object(name, validator)` にあたります。違反のパスは
組み合わされます。

```java
record Address(String city, String zip) {}
record Customer(String name, Address address) {}

var addressValidator = Yavi.schema()
        ._string("city", c -> c.notBlank())
        ._string("zip", c -> c.pattern("^\\d{3}-\\d{4}$"))
        .fromMap(Address::new);

var customerValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._object("address", addressValidator)
        .fromMap(Customer::new);

customerValidator.validate(Map.of(
        "name", "Alice",
        "address", Map.of("city", "Tokyo", "zip", "100-0001")))
// ==> Valid[value=Customer[name=Alice, address=Address[city=Tokyo, zip=100-0001]]]

customerValidator.validate(Map.of(
        "name", "",
        "address", Map.of("city", "", "zip", "bad")))
// ==> Invalid[errors=[/name: "name" must not be blank (charSequence.notBlank), /address/city: "address.city" must not be blank (charSequence.notBlank), /address/zip: "address.zip" must match ^\d{3}-\d{4}$ (charSequence.pattern)]]
```

違反にはポインタ（`/address/city`）と名前（`address.city`）があり、メッセージには名前が
現れます。

## 6. フラットなデータの構造化

JOIN の結果や CSV ファイルはフラットです。`_embedded` は Raoh の `flat` にあたり、
埋め込むバリデータはプロパティと同じマップを読みます。

```java
record UserName(String first, String last) {}
record Department(String name, String code) {}
record Employee(UserName name, Department dept) {}

var userNameValidator = Yavi.schema()
        ._string("first_name", c -> c.notBlank())
        ._string("last_name", c -> c.notBlank())
        .fromMap(UserName::new);

var departmentValidator = Yavi.schema()
        ._string("dept_name", c -> c.notBlank())
        ._string("dept_code", c -> c.notBlank())
        .fromMap(Department::new);

var employeeValidator = Yavi.schema()
        ._embedded(userNameValidator)
        ._embedded(departmentValidator)
        .fromMap(Employee::new);

employeeValidator.validate(Map.of(
        "first_name", "Alice",
        "last_name", "Smith",
        "dept_name", "Engineering",
        "dept_code", "ENG"))
// ==> Valid[value=Employee[name=UserName[first=Alice, last=Smith], dept=Department[name=Engineering, code=ENG]]]
```

埋め込む側のバリデータは、埋め込んだバリデータのプロパティを知っています。そのため strict に
でき（[17 節](#17-不明なプロパティの拒否)）、Raoh の `flat` を使う結合ではこれができません。
2 つの埋め込みバリデータが同じ名前を定義すると、バリデータの作成時に拒否されます。列は SQL の
別名で改名します。

LEFT JOIN では、行がないとき結合した側のすべての列が `null` になります。`null` を受け付ける
埋め込みバリデータには、そのプロパティのどれにも値がないとき `null` が渡されます。そのため
部署は、1 つの列の `Presence` ではなく `Optional` になります。

```java
record EmployeeWithDept(UserName name, Optional<Department> dept) {}

var employeeWithDeptValidator = Yavi.schema()
        ._embedded(userNameValidator)
        ._embedded(departmentValidator).optional()
        .fromMap(EmployeeWithDept::new);

var row = new HashMap<String, Object>();
row.put("first_name", "Bob");
row.put("last_name", "Jones");
row.put("dept_name", null);
row.put("dept_code", null);

employeeWithDeptValidator.validate(row)
// ==> Valid[value=EmployeeWithDept[name=UserName[first=Bob, last=Jones], dept=Optional.empty]]

row.put("dept_name", "Engineering");
employeeWithDeptValidator.validate(row)
// ==> Invalid[errors=[/dept_code: "dept_code" must not be blank (charSequence.notBlank)]]
```

### 1 対多の JOIN

各行は、親と子を埋め込んだバリデータで検証し、そのあとで行を親ごとにまとめます。`liftList()`
は各行を検証し、`prefixed` は違反の位置を決めます（[15 節](#15-リストの行の検証)）。

```java
record OrderHeader(String orderId, String customerName) {}
record OrderLine(String productId, int quantity, BigDecimal unitPrice) {}
record OrderWithLines(OrderHeader header, List<OrderLine> lines) {}

var headerValidator = Yavi.schema()
        ._string("order_id", c -> c.notBlank())
        ._string("customer_name", c -> c.notBlank())
        .fromMap(OrderHeader::new);

var lineValidator = Yavi.schema()
        ._string("product_id", c -> c.notBlank())
        ._integer("qty", c -> c.positive())
        ._bigDecimal("unit_price", c -> c.positive())
        .fromMap(OrderLine::new);

var rowValidator = Yavi.schema()
        ._embedded(headerValidator)
        ._embedded(lineValidator)
        .fromMap((header, line) -> Map.entry(header, line));

// SELECT o.order_id, o.customer_name, l.product_id, l.qty, l.unit_price
// FROM orders o JOIN order_lines l ON o.order_id = l.order_id
var joinRows = List.<Map<String, Object>>of(
        Map.of("order_id", "ORD-001", "customer_name", "Alice", "product_id", "A01", "qty", 2, "unit_price", 1500),
        Map.of("order_id", "ORD-001", "customer_name", "Alice", "product_id", "B02", "qty", 1, "unit_price", 3000),
        Map.of("order_id", "ORD-002", "customer_name", "Bob", "product_id", "A01", "qty", 5, "unit_price", 1500));

var orders = rowValidator.liftList().prefixed("rows").validate(joinRows).map(entries ->
        entries.stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        LinkedHashMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())))
                .entrySet().stream()
                .map(e -> new OrderWithLines(e.getKey(), e.getValue()))
                .toList());
orders
// ==> Valid[value=[OrderWithLines[header=OrderHeader[orderId=ORD-001, customerName=Alice], lines=[OrderLine[productId=A01, quantity=2, unitPrice=1500], OrderLine[productId=B02, quantity=1, unitPrice=3000]]], OrderWithLines[header=OrderHeader[orderId=ORD-002, customerName=Bob], lines=[OrderLine[productId=A01, quantity=5, unitPrice=1500]]]]]
```

## 7. リスト

`_list(name, elementValidator)` はすべての要素を検証し、違反をそのインデックスの下に位置づけ
ます。リスト自体の制約は第 3 引数です。

```java
record OrderItem(String productId, int quantity) {}

var orderItemValidator = Yavi.schema()
        ._string("productId", c -> c.notBlank())
        ._integer("quantity", c -> c.positive())
        .fromMap(OrderItem::new);

var orderItemsValidator = Yavi.schema()
        ._list("items", orderItemValidator, c -> c.notEmpty())
        .fromMap();

orderItemsValidator.validate(Map.of("items", List.of(
        Map.of("productId", "A001", "quantity", 3),
        Map.of("productId", "", "quantity", -1))))
// ==> Invalid[errors=[/items/1/productId: "items[1].productId" must not be blank (charSequence.notBlank), /items/1/quantity: "items[1].quantity" must be positive (numeric.positive)]]
```

プロパティ 1 つのスキーマは、メソッド参照を渡さない `fromMap()` で終えます。プロパティの値が
結果になります。Raoh の `field(...)` 1 つの代わりです。

```java
Yavi.schema()._list("tags", Yavi._string(), c -> c.greaterThanOrEqual(1).lessThanOrEqual(10)).fromMap()
        .validate(Map.of("tags", List.of("a", "b")))
// ==> Valid[value=[a, b]]

Yavi.schema()._list("tags", Yavi._string(), c -> c.unique()).fromMap()
        .validate(Map.of("tags", List.of("a", "b", "a")))
// ==> Invalid[errors=[/tags: "tags" must be unique. [a] is/are duplicated. (collection.unique)]]

Yavi.schema()._list("codes", Yavi._string(), c -> c.fixedSize(3)).fromMap()
        .validate(Map.of("codes", List.of("X", "Y")))
// ==> Invalid[errors=[/codes: The size of "codes" must be 3. The given size is 2 (container.fixedSize)]]
```

独自のメッセージは、その制約の後に続けます。

```java
Yavi.schema()._list("tags", Yavi._string(), c -> c.greaterThanOrEqual(1).message("select at least one tag")).fromMap()
        .validate(Map.of("tags", List.of()))
// ==> Invalid[errors=[/tags: select at least one tag (container.greaterThanOrEqual)]]
```

リストの制約は、すべての要素が有効なときに検査されます。プロパティでないリストは
`Yavi._list(elementValidator, c -> ...)` です。

## 8. マップ

`_map(name, valueValidator)` は、キーが動的なマップの値を検証します。

```java
var pricesValidator = Yavi.schema()
        ._map("prices", Yavi._bigDecimal(c -> c.positive()), c -> c.notEmpty())
        .fromMap();

pricesValidator.validate(Map.of("prices", Map.of("apple", 120, "banana", 80)))
// ==> Valid[value={banana=80, apple=120}]

pricesValidator.validate(Map.of("prices", Map.of("apple", -1)))
// ==> Invalid[errors=[/prices/apple: "prices.apple" must be positive (numeric.positive)]]
```

### 文字列に入った JSON

Raoh の `pipe` は `andThen` にあたります。パーサーは、失敗を違反として報告するバリデータです。
ここでは Jackson を使うので、jshell のクラスパスに追加します。

```java
var mapper = new tools.jackson.databind.ObjectMapper();

ValueValidator<String, Map<String, Object>> parseJson = (s, locale, context) -> {
    try {
        @SuppressWarnings("unchecked")
        Map<String, Object> parsed = (Map<String, Object>) mapper.readValue(s, Map.class);
        return Validated.successWith(parsed);
    }
    catch (Exception e) {
        return Validated.failureWith(ViolationMessage.of("json.invalid", "\"{0}\" cannot be parsed as JSON"), s, locale);
    }
};

record Params(String kind, int count) {}

var paramsValidator = Yavi.schema()
        ._string("kind")
        ._integer("count")
        .fromMap(Params::new);

// "params" は JSON を持つ文字列の列
var validator = Yavi.schema()
        ._object("params", Yavi._string().andThen(parseJson).andThen(paramsValidator))
        .fromMap();

validator.validate(Map.of("params", "{\"kind\":\"like\",\"count\":3}"))
// ==> Valid[value=Params[kind=like, count=3]]

validator.validate(Map.of("params", "not json"))
// ==> Invalid[errors=[/params: "params" cannot be parsed as JSON (json.invalid)]]

validator.validate(Map.of("params", "{\"kind\":\"like\"}"))
// ==> Invalid[errors=[/params/count: "params.count" is required (object.required)]]
```

手書きのバリデータは、入力、ロケール、コンテキストを取るラムダです。違反はメッセージキーと
メッセージの書式から作ります。`{0}` は、違反を報告する名前になります。

## 9. 省略可能、nullable、3 状態のプロパティ

プロパティは必須で、後ろに修飾子がなければ `null` を拒否します。

```java
// 必須。null は許さない
Yavi.schema()._string("email", c -> c.email()).fromMap().validate(Map.of("email", "a@b.com"))
// ==> Valid[value=a@b.com]

// null を許す。値は @Nullable String になる
Yavi.schema()._string("nickname").nullable().fromMap().validate(Collections.singletonMap("nickname", null))
// ==> Valid[value=null]

// 省略可能。値は Optional<String> になる
Yavi.schema()._string("middleName").optional().fromMap().validate(Map.of())
// ==> Valid[value=Optional.empty]
```

`nullable()`、`optional()`、`defaultValue(...)` は、存在しないキーと `null` のどちらも受け付け
ます。Raoh はこの 2 つを区別し、`field(name, nullable(dec))` はキーを必須にし、
`optionalField(name, dec)` は `null` を拒否します。プロパティの最後の修飾子で同じことを言え
ます。

```java
Yavi.schema()._string("nickname").nullable().required().fromMap()
        .validate(Map.of())
// ==> Invalid[errors=[/nickname: "nickname" is required (object.required)]]

Yavi.schema()._string("middleName").optional().rejectingNull().fromMap()
        .validate(Collections.singletonMap("middleName", null))
// ==> Invalid[errors=[/middleName: "middleName" must not be null (object.notNull)]]
```

`presence()` はプロパティを `Presence` として読み、3 つの状態を区別します。

```java
var bioValidator = Yavi.schema()._string("bio").nullable().presence().fromMap();

bioValidator.validate(Map.of("bio", "hello"))
// ==> Valid[value=Present[value=hello]]

var m = new HashMap<String, Object>(); m.put("bio", null);
bioValidator.validate(m)
// ==> Valid[value=PresentNull[]]

bioValidator.validate(Map.of())
// ==> Valid[value=Absent[]]
```

`presence()` が加えるのは、存在しない状態だけです。`null` を受け付けるかはプロパティが決め
るので、`nullable()` がなければ、プロパティは省略できてもクリアできません。この形は PATCH
リクエストに合います。

```java
record ProfilePatch(Presence<String> nickname, Presence<String> bio) {}

var profilePatchValidator = Yavi.schema()
        ._string("nickname", c -> c.lessThanOrEqual(50)).presence()        // 省略できるが、クリアできない
        ._string("bio", c -> c.lessThanOrEqual(500)).nullable().presence() // 省略もクリアもできる
        .fromMap(ProfilePatch::new);

profilePatchValidator.validate(Map.of("nickname", "alice"))
// ==> Valid[value=ProfilePatch[nickname=Present[value=alice], bio=Absent[]]]

profilePatchValidator.validate(Collections.singletonMap("nickname", null))
// ==> Invalid[errors=[/nickname: "nickname" must not be null (object.notNull)]]
```

## 10. enum

enum の定数は、その名前から読みます。名前は完全に一致させます。enum のバリデータの
`ignoringCase()` を付けると、Raoh の `enumOf` と同じく大文字と小文字を区別しません。

```java
enum Role { ADMIN, MEMBER, GUEST }

Yavi.schema()._enum("role", Role.class).fromMap().validate(Map.of("role", "ADMIN"))
// ==> Valid[value=ADMIN]

Yavi.schema()._enum("role", Role.class).fromMap().validate(Map.of("role", "admin"))
// ==> Invalid[errors=[/role: "role" must be one of the following values: [ADMIN, MEMBER, GUEST] (object.oneOf)]]

Yavi.schema()._enum(Yavi._enum("role", Role.class).ignoringCase()).fromMap().validate(Map.of("role", "admin"))
// ==> Valid[value=ADMIN]

Yavi.schema()._enum("role", Role.class).defaultValue(Role.MEMBER).fromMap().validate(Map.of())
// ==> Valid[value=MEMBER]
```

## 11. ユニオン型

Raoh の `oneOf(a, b)` は `ValueValidator.anyOf(a, b)` にあたり、最初に有効だった選択肢を
返します。Raoh の `literal("email")` は制約 `equalTo` にあたります。

```java
interface Contact {}
record EmailContact(String address) implements Contact {}
record PhoneContact(String number) implements Contact {}

ValueValidator<Map<String, Object>, Contact> contactValidator = ValueValidator.anyOf(
        Yavi.schema()
                ._string("kind", c -> c.equalTo("email"))
                ._string("value", c -> c.email())
                .fromMap((kind, value) -> new EmailContact(value)),
        Yavi.schema()
                ._string("kind", c -> c.equalTo("phone"))
                ._string("value", c -> c.pattern("^\\d{10,15}$"))
                .fromMap((kind, value) -> new PhoneContact(value)));

contactValidator.validate(Map.of("kind", "email", "value", "a@b.com"))
// ==> Valid[value=EmailContact[address=a@b.com]]

contactValidator.validate(Map.of("kind", "phone", "value", "09012345678"))
// ==> Valid[value=PhoneContact[number=09012345678]]

contactValidator.validate(Map.of("kind", "fax", "value", "123"))
// ==> Invalid[errors=[The value must satisfy at least one of the 2 alternatives (object.anyOf) alternatives=[[/kind: "kind" must be equal to email (object.equalTo), /value: "value" must be a valid email address (charSequence.email)], [/kind: "kind" must be equal to phone (object.equalTo), /value: "value" must match ^\d{10,15}$ (charSequence.pattern)]]]]
```

スーパータイプへのキャストは要りませんが、変数の型は要ります。`var` は `Record & Contact` と
推論します。1 つの違反が、各選択肢の違反を持ちます。

```java
if (contactValidator.validate(Map.of("kind", "fax", "value", "123")).errors().get(0) instanceof ConstraintViolation.NoAlternativeMatched unmatched) {
    unmatched.alternatives().forEach(violations -> System.out.println(violations.messagesByPointer()));
}
// {/kind=["kind" must be equal to email], /value=["value" must be a valid email address]}
// {/kind=["kind" must be equal to phone], /value=["value" must match ^\d{10,15}$]}
```

### 判別可能なユニオン

Raoh の `discriminate("type", variant(...), ...)` は `DiscriminatedUnionValidator` に
あたります。最初に判別子を読み、選ばれた選択肢だけを検証します。選択肢は判別子を
定義しません。

```java
interface Shape {}
record Circle(double radius) implements Shape {}
record Rect(double width, double height) implements Shape {}

var shapeValidator = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Yavi.schema()._double("radius", c -> c.positive()).fromMap(Circle::new))
        .when("rect", Yavi.schema()
                ._double("width", c -> c.positive())
                ._double("height", c -> c.positive())
                .fromMap(Rect::new));

shapeValidator.validate(Map.of("type", "circle", "radius", 5.0))
// ==> Valid[value=Circle[radius=5.0]]

shapeValidator.validate(Map.of("type", "rect", "width", 3.0, "height", 4.0))
// ==> Valid[value=Rect[width=3.0, height=4.0]]

shapeValidator.validate(Map.of("type", "hexagon"))
// ==> Invalid[errors=[/type: "type" must be one of the following values: [circle, rect] (object.oneOf)]]
```

プロパティ 1 つのスキーマも、ほかと同じバリデータです。`asDecoder()` は要りません。同じタグに
選択肢を 2 つ定義すると、バリデータの作成時に `IllegalArgumentException` になります。

## 12. 複数フィールドにまたがる検証

Raoh の `flatMap` は `flatFromMap` にあたります。ドメインのルールを検査するファクトリメソッドを
そのままメソッド参照で渡せます。渡されるのは検証済みのプロパティで、返り値は `Validated` です。
呼ばれるのは、すべてのプロパティが有効なときだけです。

```java
record DateRange(LocalDate start, LocalDate end) {
    static Validated<DateRange> parse(LocalDate start, LocalDate end) {
        if (!start.isBefore(end)) {
            return Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"));
        }
        return Validated.successWith(new DateRange(start, end));
    }
}

var dateRangeValidator = Yavi.schema()
        ._localDate("start")
        ._localDate("end")
        .flatFromMap(DateRange::parse);

dateRangeValidator.validate(Map.of("start", "2025-01-01", "end", "2025-12-31"))
// ==> Valid[value=DateRange[start=2025-01-01, end=2025-12-31]]

dateRangeValidator.validate(Map.of("start", "2025-12-31", "end", "2025-01-01"))
// ==> Invalid[errors=[start must be before end (dateRange.invalid)]]

Yavi.schema()._object("period", dateRangeValidator).fromMap()
        .validate(Map.of("period", Map.of("start", "2025-12-31", "end", "2025-01-01")))
// ==> Invalid[errors=[/period: start must be before end (dateRange.invalid)]]
```

違反はオブジェクト自体で報告され、バリデータをネストした箇所では `period` の下で報告され
ます。`failureWith(message, args...)` は Raoh の `Result.fail(code, message)` にあたります。
ファクトリメソッドは、違反した値もロケールも指定しません。どちらも渡されないからです。
バリデータは、プロパティの違反と同じく、検証のロケールで違反を報告します。

```java
dateRangeValidator.validate(Map.of("start", "2025-12-31", "end", "2025-01-01"), Locale.JAPANESE)
        .errors().get(0).locale()
// ==> ja
```

作成したオブジェクトに対する述語で足りるルールには、ファクトリメソッドは要りません。
`constraintOnTarget` は、述語、違反の位置を決める名前（空の名前はオブジェクト自体）、
メッセージを取ります。

```java
var dateRangeValidator2 = Yavi.schema()
        ._localDate("start")
        ._localDate("end")
        .fromMap(DateRange::new)
        .constraintOnTarget(range -> range.start().isBefore(range.end()), "end",
                ViolationMessage.of("dateRange.invalid", "\"{0}\" must be after \"start\""));

dateRangeValidator2.validate(Map.of("start", "2025-12-31", "end", "2025-01-01"))
// ==> Invalid[errors=[/end: "end" must be after "start" (dateRange.invalid)]]
```

## 13. 条件付き検証

あるプロパティの値で残りのプロパティの検証を変えるのは、判別可能なユニオンです。Raoh の
チュートリアルで `method` を読んで分岐するラムダは要りません。

```java
interface Payment {}
record CreditCard(String number, String expiry) implements Payment {}
record BankTransfer(String bankCode, String accountNumber) implements Payment {}

var paymentValidator = DiscriminatedUnionValidator.<Payment>of("method")
        .when("credit_card", Yavi.schema()
                ._string("number", c -> c.notBlank())
                ._string("expiry", c -> c.pattern("^\\d{2}/\\d{2}$"))
                .fromMap(CreditCard::new))
        .when("bank_transfer", Yavi.schema()
                ._string("bankCode", c -> c.notBlank())
                ._string("accountNumber", c -> c.notBlank())
                .fromMap(BankTransfer::new));

paymentValidator.validate(Map.of("method", "credit_card", "number", "4111111111111111", "expiry", "12/26"))
// ==> Valid[value=CreditCard[number=4111111111111111, expiry=12/26]]

paymentValidator.validate(Map.of("method", "bank_transfer", "bankCode", "0001", "accountNumber", "1234567"))
// ==> Valid[value=BankTransfer[bankCode=0001, accountNumber=1234567]]

paymentValidator.validate(Map.of("method", "cash"))
// ==> Invalid[errors=[/method: "method" must be one of the following values: [credit_card, bank_transfer] (object.oneOf)]]
```

## 14. 異なる入力の結果の結合

スキーマは 1 つの入力のプロパティを結合します。異なる入力の 2 つの結果は `combine` で結合し
ます。これは Raoh の `Result.map2` にあたります。

```java
record PersonalName(String first, String last) {}
record ContactInfo(String email, String phone) {}
record CustomerProfile(PersonalName name, ContactInfo contact) {}

var nameValidator = Yavi.schema()
        ._string("first", c -> c.notBlank())
        ._string("last", c -> c.notBlank())
        .fromMap(PersonalName::new);

var contactInfoValidator = Yavi.schema()
        ._string("email", c -> c.email())
        ._string("phone", c -> c.notBlank())
        .fromMap(ContactInfo::new);

var nameResult = nameValidator.validate(Map.of("first", "Alice", "last", "Smith"));
var contactResult = contactInfoValidator.validate(Map.of("email", "a@b.com", "phone", "090-1234-5678"));

nameResult.combine(contactResult).apply(CustomerProfile::new)
// ==> Valid[value=CustomerProfile[name=PersonalName[first=Alice, last=Smith], contact=ContactInfo[email=a@b.com, phone=090-1234-5678]]]

nameValidator.validate(Map.of("first", ""))
        .combine(contactInfoValidator.validate(Map.of("email", "x")))
        .apply(CustomerProfile::new)
// ==> Invalid[errors=[/first: "first" must not be blank (charSequence.notBlank), /last: "last" is required (object.required), /email: "email" must be a valid email address (charSequence.email), /phone: "phone" is required (object.required)]]
```

## 15. リストの行の検証

Raoh の `Result.traverse(rows, decoder::decode, Path.of("orders"))` は、`prefixed` を
付けた `liftList()` にあたります。各行が検証され、違反はそのインデックスの下に
位置づけられます。

```java
record Order(String orderId, BigDecimal total) {}

var orderValidator = Yavi.schema()
        ._string("order_id", c -> c.notBlank())
        ._bigDecimal("total", c -> c.positiveOrZero())
        .fromMap(Order::new);

var rows = List.<Map<String, Object>>of(
        Map.of("order_id", "A001", "total", 1000),
        Map.of("order_id", "", "total", -500),
        Map.of("order_id", "A003", "total", 300));

orderValidator.liftList().prefixed("orders").validate(rows)
// ==> Invalid[errors=[/orders/1/order_id: "orders[1].order_id" must not be blank (charSequence.notBlank), /orders/1/total: "orders[1].total" must be positive or zero (numeric.positiveOrZero)]]
```

## 16. 既定値と復旧

### defaultValue

Raoh の `withDefault` は `defaultValue(...)` にあたります。既定値は、存在しないキーと
`null` に適用されます。値が存在して無効な場合は違反です。

```java
var roleValidator = Yavi.schema()
        ._enum(Yavi._enum("role", Role.class).ignoringCase()).defaultValue(Role.MEMBER)
        .fromMap();

roleValidator.validate(Map.of())
// ==> Valid[value=MEMBER]

roleValidator.validate(Map.of("role", "admin"))
// ==> Valid[value=ADMIN]

roleValidator.validate(Map.of("role", "invalid"))
// ==> Invalid[errors=[/role: "role" must be one of the following values: [ADMIN, MEMBER, GUEST] (object.oneOf)]]
```

修飾子は入力の種類によらずプロパティに付きます。同じスキーマが、JSON の `null`、存在しない
メンバー、空のリクエストパラメータに既定値を与えます。

### recover

`recover(fallback)` は、無効な値を含むすべての失敗を置き換えます。

```java
var pageSizeValidator = Yavi.schema()
        ._integer("pageSize", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
        .fromMap();

pageSizeValidator.validate(Map.of("pageSize", 999))
// ==> Valid[value=20]

pageSizeValidator.validate(Map.of())
// ==> Valid[value=20]
```

もともと省略可能なプロパティには `defaultValue` を使い、何が届いても壊してはならないプロパティ
には `recover` を使います。`recoverWith(violations -> ...)` は違反から代替値を計算します。
違反を記録するときなどに使います。

## 17. 不明なプロパティの拒否

Raoh の `strict(f)` はバリデータの `rejectingUnknownProperties()` にあたります。

```java
record ApiRequest(String action, int amount) {}

var apiRequestValidator = Yavi.schema()
        ._string("action", c -> c.notBlank())
        ._integer("amount", c -> c.positive())
        .fromMap(ApiRequest::new)
        .rejectingUnknownProperties();

apiRequestValidator.validate(Map.of("action", "transfer", "amount", 100))
// ==> Valid[value=ApiRequest[action=transfer, amount=100]]

apiRequestValidator.validate(Map.of("action", "transfer", "amount", 100, "extra", true))
// ==> Invalid[errors=[/extra: "extra" is not a known property (object.unknownProperty)]]
```

## 18. 再帰的な構造

`ValueValidator.recursive(self -> ...)` は、バリデータを作るラムダ式にそのバリデータ自身への
参照を渡します。代入より前にバリデータを配列で保持する必要はありません。

```java
record Comment(String body, List<Comment> replies) {}

ObjectInputValidator<Comment> commentValidator = ValueValidator.recursive(self -> Yavi.schema()
        ._string("body", c -> c.notBlank())
        ._list("replies", self).defaultValue(List.of())
        .fromMap(Comment::new));

commentValidator.validate(Map.of(
        "body", "top",
        "replies", List.of(
                Map.of("body", "reply1", "replies", List.of()),
                Map.of("body", "reply2", "replies", List.of(
                        Map.of("body", "nested", "replies", List.of()))))))
// ==> Valid[value=Comment[body=top, replies=[Comment[body=reply1, replies=[]], Comment[body=reply2, replies=[Comment[body=nested, replies=[]]]]]]]

commentValidator.validate(Map.of(
        "body", "top",
        "replies", List.of(
                Map.of("body", "reply1"),
                Map.of("body", "reply2", "replies", List.of(Map.of("body", ""))))))
// ==> Invalid[errors=[/replies/1/replies/0/body: "replies[1].replies[0].body" must not be blank (charSequence.notBlank)]]
```

変数の型は宣言します。`var` ではコンパイラに `self` の型が伝わらないからです。
`ValueValidator.lazy(() -> validator)` は Raoh の `lazy` にあたり、互いに参照し合うバリデータ
に使います。

## 19. 違反の扱い

```java
var checkValidator = Yavi.schema()
        ._string("email", c -> c.email())
        ._integer("age", c -> c.greaterThanOrEqual(0).lessThanOrEqual(150))
        .fromMap((email, age) -> Map.<String, Object>of("email", email, "age", age));

var result = checkValidator.validate(Map.of("email", "bad", "age", 300));
```

`Validated` は record `Valid` と `Invalid` の sealed インターフェースなので、結果は `switch` で
扱えます。`errors()` は失敗した結果の違反を返します。

### フォーム向け

Raoh の `issues.flatten()` は `messagesByPointer()`、フォームのフィールドの名前を
使うなら `messagesByName()` にあたります。

```java
switch (result) {
    case Validated.Invalid(var violations) -> System.out.println(violations.messagesByPointer());
    case Validated.Valid(var value) -> {}
}
// {/email=["email" must be a valid email address], /age=["age" must be less than or equal to 150]}

result.errors().messagesByName()
// ==> {email=["email" must be a valid email address], age=["age" must be less than or equal to 150]}
```

### API 向け

Raoh の `issues.toJsonList()` は `details()` にあたります。JSON ライブラリがそのまま
直列化する record のリストです。`params` は Raoh の `meta` にあたります。

```java
result.errors().details()
// ==> [Simple[name=email, pointer=/email, messageKey=charSequence.email, message="email" must be a valid email address, params={}], Simple[name=age, pointer=/age, messageKey=numeric.lessThanOrEqual, message="age" must be less than or equal to 150, params={max=150}]]
```

### ロケールに応じたメッセージ

`MessageFormatter` がメッセージを整形します。`ResourceBundleMessageFormatter` は、クラスパスに
あるリソースバンドル（ここでは `messages_ja.properties`）からキーでメッセージを引きます。
Raoh の `ResourceBundleMessageResolver` にあたります。

```properties
charSequence.email="{0}" はメールアドレスの形式で入力してください
numeric.lessThanOrEqual="{0}" は {1} 以下で入力してください
```

```java
var formatter = new ResourceBundleMessageFormatter("messages");

checkValidator.formattingWith(formatter).validate(Map.of("email", "bad", "age", 300), Locale.JAPANESE)
// ==> Invalid[errors=[/email: "email" はメールアドレスの形式で入力してください (charSequence.email), /age: "age" は 150 以下で入力してください (numeric.lessThanOrEqual)]]
```

フォーマッタはバリデータに与えるか、あとから違反に与えます（`violations.formattedWith(formatter)`）。
バンドルにないキーは、既定のメッセージのままです。Spring では、
`new MessageSourceMessageFormatter(messageSource::getMessage)` が `MessageSource` を読みます。

## 20. シナリオ: ユーザー登録 API

```java
interface ContactMethod {}
record EmailMethod(String address) implements ContactMethod {}
record PhoneMethod(String number) implements ContactMethod {}
record Password(String value) {}
record UserRegistration(Email email, Password password, Role role, List<ContactMethod> contacts) {}

ValueValidator<Map<String, Object>, ContactMethod> contactMethodValidator = ValueValidator.anyOf(
        Yavi.schema()
                ._string("kind", c -> c.equalTo("email"))
                ._string("value", c -> c.email())
                .fromMap((kind, value) -> new EmailMethod(value)),
        Yavi.schema()
                ._string("kind", c -> c.equalTo("phone"))
                ._string("value", c -> c.pattern("^\\d{10,15}$"))
                .fromMap((kind, value) -> new PhoneMethod(value)));

var userRegistrationValidator = Yavi.schema()
        ._object("email", emailValidator)
        ._string("password", c -> c.greaterThanOrEqual(8).lessThanOrEqual(128)).map(Password::new)
        ._enum("role", Role.class).defaultValue(Role.MEMBER)
        ._list("contacts", contactMethodValidator, c -> c.notEmpty())
        .fromMap(UserRegistration::new);

userRegistrationValidator.validate(Map.of(
        "email", "  ALICE@Example.com  ",
        "password", "secureP@ss1",
        "contacts", List.of(
                Map.of("kind", "email", "value", "alice@example.com"),
                Map.of("kind", "phone", "value", "09012345678"))))
// ==> Valid[value=UserRegistration[email=Email[value=alice@example.com], password=Password[value=secureP@ss1], role=MEMBER, contacts=[EmailMethod[address=alice@example.com], PhoneMethod[number=09012345678]]]]

userRegistrationValidator.validate(Map.of(
        "email", "bad",
        "password", "short",
        "contacts", List.of(
                Map.of("kind", "email", "value", "not-email"))))
// ==> Invalid[errors=[/email: "email" must be a valid email address (charSequence.email), /password: The size of "password" must be greater than or equal to 8. The given size is 5 (container.greaterThanOrEqual), /contacts/0: "contacts[0]" must satisfy at least one of the 2 alternatives (object.anyOf) alternatives=[[/contacts/0/value: "contacts[0].value" must be a valid email address (charSequence.email)], [/contacts/0/kind: "contacts[0].kind" must be equal to phone (object.equalTo), /contacts/0/value: "contacts[0].value" must match ^\d{10,15}$ (charSequence.pattern)]]]]
```

JSON を受け取るコントローラは、同じバリデータでツリーを読みます。読み込み元が違うだけで、
インポートは変わりません。[0.18.0 の新機能](NewFeatures_0.18.0.ja.md#json-ライブラリのツリーモデルを検証する)
では、`JsonNodeReader` と、数値を書かれたとおりに保持するマッパーの設定を説明しています。
Raoh はこれを独自の `readTree` で実現しています。

```java
@PostMapping("/users")
ResponseEntity<?> register(@RequestBody JsonNode body) {
    return switch (userRegistrationValidator.readingWith(JsonNodeReader.INSTANCE).validate(body)) {
        case Validated.Valid<UserRegistration>(var registration) ->
            ResponseEntity.status(201).body(userService.register(registration));
        case Validated.Invalid<UserRegistration>(var violations) ->
            ResponseEntity.badRequest().body(violations.details());
    };
}
```

## 21. リストからマップへの変換

リストを検証し、`map` で変換します。

```java
record Product(String id, BigDecimal price) {}

var productValidator = Yavi.schema()
        ._string("id", c -> c.notBlank())
        ._bigDecimal("price", c -> c.positive())
        .fromMap(Product::new);

var priceMapValidator = Yavi.schema()
        ._list("products", productValidator)
        .fromMap()
        .map(products -> products.stream().collect(Collectors.toMap(Product::id, Product::price)));

priceMapValidator.validate(Map.of("products", List.of(
        Map.of("id", "APPLE", "price", new BigDecimal("120")),
        Map.of("id", "BANANA", "price", new BigDecimal("80")))))
// ==> Valid[value={APPLE=120, BANANA=80}]
```

id の重複はドメインのルールの違反です。リストの `unique(key)` が重複したキーを報告します。
Raoh では、これを `flatMap` で手作業で検査します。

```java
var strictPriceMapValidator = Yavi.schema()
        ._list("products", productValidator, c -> c.unique(Product::id))
        .fromMap()
        .map(products -> products.stream().collect(Collectors.toMap(Product::id, Product::price)));

strictPriceMapValidator.validate(Map.of("products", List.of(
        Map.of("id", "APPLE", "price", new BigDecimal("120")),
        Map.of("id", "APPLE", "price", new BigDecimal("99")))))
// ==> Invalid[errors=[/products: "products" must be unique. [APPLE] is/are duplicated. (collection.uniqueBy)]]
```

## 22. 既知のプロパティと残りのキー

`_rest` は、どのプロパティにも定義がないキーを集めます。Raoh では、これを手書きのデコーダと
`Result.map2` で実現します。値は検証され、違反はそのキーの下に位置づけられます。

```java
record Goods(String name, Map<String, String> attrs) {}

var goodsValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._rest(Yavi._string())
        .fromMap(Goods::new);

goodsValidator.validate(Map.of("name", "T-shirt", "color", "red", "size", "L"))
// ==> Valid[value=Goods[name=T-shirt, attrs={color=red, size=L}]]

goodsValidator.validate(Map.of("name", "", "color", 1))
// ==> Invalid[errors=[/name: "name" must not be blank (charSequence.notBlank), /color: "color" must be of type string (object.type)]]
```

バリデータを渡さない `_rest()` は、値をそのまま受け取ります。

## 23. パスワードの確認

2 つのルールは、3 つの値に対する述語です。確認用の値は保持しないので、ルールは中間の record
を読み、`map` が結果を作ります。違反はそれぞれフォームのフィールドに位置づけられます。

```java
record PasswordForm(String current, String newPassword, String confirm) {}
record PasswordChange(String current, String newPassword) {}

var passwordChangeValidator = Yavi.schema()
        ._string("currentPassword", c -> c.notBlank())
        ._string("newPassword", c -> c.greaterThanOrEqual(8).lessThanOrEqual(128))
        ._string("confirmPassword", c -> c.notBlank())
        .fromMap(PasswordForm::new)
        .constraintOnTarget(form -> form.newPassword().equals(form.confirm()), "confirmPassword",
                ViolationMessage.of("password.mismatch", "\"{0}\" must match \"newPassword\""))
        .constraintOnTarget(form -> !form.current().equals(form.newPassword()), "newPassword",
                ViolationMessage.of("password.same", "\"{0}\" must differ from \"currentPassword\""))
        .map(form -> new PasswordChange(form.current(), form.newPassword()));

passwordChangeValidator.validate(Map.of(
        "currentPassword", "oldSecret",
        "newPassword", "newSecret1",
        "confirmPassword", "newSecret1"))
// ==> Valid[value=PasswordChange[current=oldSecret, newPassword=newSecret1]]

passwordChangeValidator.validate(Map.of(
        "currentPassword", "oldSecret",
        "newPassword", "short",
        "confirmPassword", "different"))
// ==> Invalid[errors=[/newPassword: The size of "newPassword" must be greater than or equal to 8. The given size is 5 (container.greaterThanOrEqual)]]

passwordChangeValidator.validate(Map.of(
        "currentPassword", "newSecret1",
        "newPassword", "newSecret1",
        "confirmPassword", "different"))
// ==> Invalid[errors=[/confirmPassword: "confirmPassword" must match "newPassword" (password.mismatch), /newPassword: "newPassword" must differ from "currentPassword" (password.same)]]
```

ルールは、`flatMap` の中の検査と同じく、すべてのプロパティが有効なときにだけ検査されます。
ただし `flatMap` と違い、続けて加えたルールはすべて検査されます。最後の入力は 2 つのルールの
両方に違反し、両方が報告されるので、利用者はフォームを 1 回直せば済みます。

別のルールが成り立つときだけ意味を持つルールは、`map` か `andThen` の後に加えます。これらの
関数には、それ以前のルールを満たす値が渡されます。次の例では、`newPassword` のルールは、
確認用の値が一致するときにだけ検査されます。

```java
var stepwiseValidator = Yavi.schema()
        ._string("currentPassword", c -> c.notBlank())
        ._string("newPassword", c -> c.greaterThanOrEqual(8).lessThanOrEqual(128))
        ._string("confirmPassword", c -> c.notBlank())
        .fromMap(PasswordForm::new)
        .constraintOnTarget(form -> form.newPassword().equals(form.confirm()), "confirmPassword",
                ViolationMessage.of("password.mismatch", "\"{0}\" must match \"newPassword\""))
        .map(form -> new PasswordChange(form.current(), form.newPassword()))
        .constraintOnTarget(change -> !change.current().equals(change.newPassword()), "newPassword",
                ViolationMessage.of("password.same", "\"{0}\" must differ from \"currentPassword\""));

stepwiseValidator.validate(Map.of(
        "currentPassword", "newSecret1",
        "newPassword", "newSecret1",
        "confirmPassword", "different"))
// ==> Invalid[errors=[/confirmPassword: "confirmPassword" must match "newPassword" (password.mismatch)]]
```

## 24. ページネーションとクエリパラメータ

`defaultValue`（存在しないとき）と `recover`（任意の失敗から）で、パラメータごとにどこまで
許容するかを決めます。

```java
enum SortOrder { ASC, DESC }
record PageRequest(int page, int size, SortOrder order) {}

var pageRequestValidator = Yavi.schema()
        // page: 存在しないときは 0。負の数は違反
        ._integer("page", c -> c.greaterThanOrEqual(0)).defaultValue(0)
        // size: 存在しないか無効なときは 20
        ._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
        // sort: 存在しないか無効なときは ASC
        ._enum(Yavi._enum("sort", SortOrder.class).ignoringCase()).recover(SortOrder.ASC)
        .fromMap(PageRequest::new);

pageRequestValidator.validate(Map.of("page", 2, "size", 50, "sort", "desc"))
// ==> Valid[value=PageRequest[page=2, size=50, order=DESC]]

pageRequestValidator.validate(Map.of())
// ==> Valid[value=PageRequest[page=0, size=20, order=ASC]]

pageRequestValidator.validate(Map.of("size", 9999))
// ==> Valid[value=PageRequest[page=0, size=20, order=ASC]]

pageRequestValidator.validate(Map.of("page", -1))
// ==> Invalid[errors=[/page: "page" must be greater than or equal to 0 (numeric.greaterThanOrEqual)]]
```

`recover` は存在しないキーも対象にするので、既定値とは組み合わせません。クエリパラメータは
文字列で、Raoh のチュートリアルのマップには出てきません。`readingParameters()` は同じスキーマ
で読みます。

```java
var queryValidator = pageRequestValidator.readingParameters();

queryValidator.validate(Map.of("page", "2", "size", "50", "sort", "desc"))
// ==> Valid[value=PageRequest[page=2, size=50, order=DESC]]

queryValidator.validate(Map.of("page", "x", "size", "many"))
// ==> Invalid[errors=[/page: "page" must be of type integer (object.type)]]
```

## 25. 金額と通貨

金額に許される小数部の桁数は、通貨によって変わります。

```java
enum Currency { JPY, USD, EUR }
record Money(BigDecimal amount, Currency currency) {}

var moneyValidator = Yavi.schema()
        ._bigDecimal("amount", c -> c.positiveOrZero())
        ._enum(Yavi._enum("currency", Currency.class).ignoringCase())
        .flatFromMap((amount, currency) -> {
            int maxScale = switch (currency) {
                case JPY -> 0;
                case USD, EUR -> 2;
            };
            if (amount.scale() > maxScale) {
                return Validated.failureWith(ViolationMessage.of("money.scale", "{1} does not allow more than {2} decimal places", "currency", "maxScale"),
                        currency, maxScale);
            }
            return Validated.successWith(new Money(amount, currency));
        });

moneyValidator.validate(Map.of("amount", new BigDecimal("1000"), "currency", "jpy"))
// ==> Valid[value=Money[amount=1000, currency=JPY]]

moneyValidator.validate(Map.of("amount", new BigDecimal("10.5"), "currency", "jpy"))
// ==> Invalid[errors=[JPY does not allow more than 0 decimal places (money.scale)]]

moneyValidator.validate(Map.of("amount", new BigDecimal("9.99"), "currency", "usd"))
// ==> Valid[value=Money[amount=9.99, currency=USD]]
```

通貨を必要としないルールは、金額の制約（`positiveOrZero()`）にします。メッセージに引数の
ないルールは、述語で書くほうが短くなります。

```java
        .fromMap(Money::new)
        .constraintOnTarget(money -> money.amount().scale() <= (money.currency() == Currency.JPY ? 0 : 2), "amount",
                ViolationMessage.of("money.scale", "\"{0}\" has too many decimal places for the currency"));
// ==> Invalid[errors=[/amount: "amount" has too many decimal places for the currency (money.scale)]]
```

## 26. CSV の取り込み

すべての行が検査され、違反は行のインデックス付きで報告されます。

```java
record MemberImport(String email, String name, int age) {}

var memberValidator = Yavi.schema()
        ._string("email", c -> c.trim().toLowerCase().notBlank().email())
        ._string("name", c -> c.trim().notBlank().lessThanOrEqual(100))
        ._integer("age", c -> c.greaterThanOrEqual(0).lessThanOrEqual(150))
        .fromMap(MemberImport::new);

var rows = List.<Map<String, Object>>of(
        Map.of("email", "alice@example.com", "name", "Alice", "age", 30),
        Map.of("email", "bad-email", "name", "", "age", 200),
        Map.of("email", "bob@example.com", "name", "Bob", "age", 25),
        Map.of("email", "also-bad", "name", "Carol", "age", -1));

switch (memberValidator.liftList().prefixed("rows").validate(rows)) {
    case Validated.Valid(var members) -> System.out.println("Imported: " + members.size());
    case Validated.Invalid(var violations) -> violations.messagesByPointer().forEach((pointer, messages) ->
            System.out.println(pointer + ": " + messages));
}
// /rows/1/email: ["rows[1].email" must be a valid email address]
// /rows/1/name: ["rows[1].name" must not be blank]
// /rows/1/age: ["rows[1].age" must be less than or equal to 150]
// /rows/3/email: ["rows[3].email" must be a valid email address]
// /rows/3/age: ["rows[3].age" must be greater than or equal to 0]
```

実際の CSV ファイルのセルは文字列です。同じバリデータが `readingParameters` でセルを読み、
`trimming()` がすべてのセルをトリムするので、プロパティごとに `c.trim()` を繰り返す必要は
ありません。

```java
var cells = List.of(
        Map.of("email", " Alice@Example.com ", "name", " Alice ", "age", "30"),
        Map.of("email", "bad-email", "name", "  ", "age", "abc"));

memberValidator.readingParameters(StringCoercion.strict().trimming()).liftList().prefixed("rows").validate(cells)
// ==> Invalid[errors=[/rows/1/email: "rows[1].email" must be a valid email address (charSequence.email), /rows/1/name: "rows[1].name" must not be blank (charSequence.notBlank), /rows/1/age: "rows[1].age" must be of type integer (object.type)]]
```

## 27. 設定ファイル

マップに解析した設定ファイルの、必須のセクションと省略可能なセクションです。

```java
record DbConfig(String host, int port, String database) {}
record CacheConfig(String host, int port, int ttlSeconds) {}
record AppConfig(DbConfig db, CacheConfig cache, String logLevel) {}

var dbConfigValidator = Yavi.schema()
        ._string("host", c -> c.notBlank())
        ._integer("port", c -> c.greaterThanOrEqual(1).lessThanOrEqual(65535)).defaultValue(5432)
        ._string("database", c -> c.notBlank())
        .fromMap(DbConfig::new);

var cacheConfigValidator = Yavi.schema()
        ._string("host", c -> c.notBlank()).defaultValue("localhost")
        ._integer("port", c -> c.greaterThanOrEqual(1).lessThanOrEqual(65535)).defaultValue(6379)
        ._integer("ttlSeconds", c -> c.positive()).defaultValue(300)
        .fromMap(CacheConfig::new);

var appConfigValidator = Yavi.schema()
        ._object("db", dbConfigValidator)
        // cache セクション自体が省略可能
        ._object("cache", cacheConfigValidator).defaultValue(new CacheConfig("localhost", 6379, 300))
        ._string("logLevel", c -> c.notBlank()).defaultValue("INFO")
        .fromMap(AppConfig::new);

appConfigValidator.validate(Map.of(
        "db", Map.of("host", "db.example.com", "database", "myapp")))
// ==> Valid[value=AppConfig[db=DbConfig[host=db.example.com, port=5432, database=myapp], cache=CacheConfig[host=localhost, port=6379, ttlSeconds=300], logLevel=INFO]]

appConfigValidator.validate(Map.of(
        "db", Map.of("host", "", "database", "myapp")))
// ==> Invalid[errors=[/db/host: "db.host" must not be blank (charSequence.notBlank)]]
```

## 28. タプル

`Arguments::of` は Raoh の `Tuple2::new` にあたります。`fromMap(Arguments::of)` は
`Arguments2` を作ります。これは `switch` で分解できる record です。`Arguments1` から
`Arguments16` まであります。

```java
var input = Map.<String, Object>of("name", "Alice", "age", 30);

var tupleValidator = Yavi.schema()
        ._string("name")
        ._integer("age")
        .fromMap(Arguments::of);

switch (tupleValidator.validate(input)) {
    case Validated.Valid(Arguments2(var name, var age)) -> System.out.println(name + " is " + age);
    case Validated.Invalid(var violations) -> System.out.println(violations.messagesByPointer());
}
// Alice is 30
```

パターンを使わない場合は、`arg1()`、`arg2()`、… で要素を取れます。

## 29. オブジェクトをマップに書き戻す

Raoh はデコーダの隣にエンコーダを書きます。YAVI では、マップを読むスキーマがそのまま書き戻し
も行います。`Yavi.reversibleSchema()` を `fromMapReversible` で終えます。これは `fromMap` に
プロパティごとのアクセサーを加えたもので、`toMap` は Raoh の `encode` にあたります。
名前を書くのは 1 回だけです。

```java
record ItemId(long value) {}
record Item(ItemId id, String name, BigDecimal price) {}

var itemSchema = Yavi.reversibleSchema()
        ._long("id").map(ItemId::new, ItemId::value)
        ._string("name")
        ._bigDecimal("price")
        .fromMapReversible(Item::new, Item::id, Item::name, Item::price);

itemSchema.toMap(new Item(new ItemId(42L), "Widget", new BigDecimal("9.99")))
// ==> {id=42, name=Widget, price=9.99}

itemSchema.validate(itemSchema.toMap(new Item(new ItemId(42L), "Widget", new BigDecimal("9.99"))))
// ==> Valid[value=Item[id=ItemId[value=42], name=Widget, price=9.99]]
```

| Raoh のエンコーダ | YAVI |
| --- | --- |
| `property("name", T::name, string())` | `_string("name")` とアクセサー `T::name` |
| `object(property(...), ...)` | `fromMapReversible(T::new, accessors...)` |
| `long_().contramap(ItemId::value)` | `._long("id").map(ItemId::new, ItemId::value)` |
| `nested(subEncoder)` | `_object(name, subSchema)` |
| `list(elementEncoder)` | `_list(name, elementSchema)` |
| `nullableProperty("x", T::x, enc)` | `.nullable()`、`.optional()` |
| `propertyWithDefault("x", T::x, enc, v)` | `.defaultValue(v)` |

`map(f, g)` は、`map(f)` に逆変換を加えたものです。書き戻すスキーマのプロパティに `map(f)`
だけを書くことはできず、逆変換のないバリデータはプロパティとして使えません。どちらもコンパイル
できません。

## 30. null と既定値の書き込み

書き込まれる値は、プロパティの修飾子で決まります。値のないプロパティは `null` として書き込み
ます。名前付きパラメータを持つ文には、キーが必要だからです。

```java
record Tag(String name) {}
record Article(String title, Optional<String> description, List<Tag> tags) {}

var tagSchema = Yavi.reversibleSchema()
        ._string("name")
        .fromMapReversible(Tag::new, Tag::name);

var articleSchema = Yavi.reversibleSchema()
        ._string("title")
        ._string("description").optional()
        ._list("tags", tagSchema).defaultValue(List.of())
        .fromMapReversible(Article::new, Article::title, Article::description, Article::tags);

articleSchema.toMap(new Article("t", Optional.empty(), List.of()))
// ==> {title=t, description=null, tags=[]}

articleSchema.toMap(new Article("t", Optional.of("d"), List.of(new Tag("java"))))
// ==> {title=t, description=d, tags=[{name=java}]}
```

書き込むオブジェクトはバリデータが作ったものなので、ここで `tags` が `null` になることはあり
ません。Raoh の `propertyWithDefault` は、ゲッターの `null` を置き換えます。

## 31. ネストしたオブジェクトの書き込み

ネストしたスキーマはネストしたマップを書き、それらのリストはマップのリストを書きます。

```java
record Buyer(String name) {}
record Purchase(long id, Buyer buyer, List<Item> items) {}

var buyerSchema = Yavi.reversibleSchema()
        ._string("name")
        .fromMapReversible(Buyer::new, Buyer::name);

var purchaseSchema = Yavi.reversibleSchema()
        ._long("id")
        ._object("buyer", buyerSchema)
        ._list("items", itemSchema)
        .fromMapReversible(Purchase::new, Purchase::id, Purchase::buyer, Purchase::items);

purchaseSchema.toMap(new Purchase(1L, new Buyer("Alice"), List.of(new Item(new ItemId(42L), "Widget", new BigDecimal("9.99")))))
// ==> {id=1, buyer={name=Alice}, items=[{id=42, name=Widget, price=9.99}]}
```

## 32. タグ付きユニオンの書き込み

オブジェクトの型を添えて加えた選択肢は、判別子付きで書き戻します。読むユニオンが、そのまま
書くユニオンです。

```java
var circleSchema = Yavi.reversibleSchema()
        ._double("radius")
        .fromMapReversible(Circle::new, Circle::radius);

var rectSchema = Yavi.reversibleSchema()
        ._double("width")
        ._double("height")
        .fromMapReversible(Rect::new, Rect::width, Rect::height);

var shapeSchema = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Circle.class, circleSchema)
        .when("rect", Rect.class, rectSchema);

shapeSchema.toMap(new Circle(2.0))
// ==> {type=circle, radius=2.0}

shapeSchema.validate(shapeSchema.toMap(new Circle(2.0)))
// ==> Valid[value=Circle[radius=2.0]]
```

どの選択肢にも定義のない型のオブジェクトは、Raoh と同じく `IllegalArgumentException` に
なります。

一部のプロパティだけを書くビューと、書き込まないプロパティについては、
[0.18.0 の新機能](NewFeatures_0.18.0.ja.md#オブジェクトを読み取り元のマップに書き戻す) を参照
してください。
