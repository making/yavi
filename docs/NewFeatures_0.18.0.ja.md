# 0.18.0 の新機能

[English](NewFeatures_0.18.0.md)

YAVI 0.18.0 では、`Yavi.schema()` で入力を一度記述すれば、型付きの引数、マップ、JSON のツリー、リクエストパラメータのどれからでも読み取れるようになりました。`Yavi.schema()` は `Yavi.arguments()` を置き換えるもので、`Yavi.arguments()` は非推奨になりました。

* [単一の値のバリデータ](#単一の値のバリデータ)
* [省略可能なプロパティとネストしたオブジェクトやリストやマップ](#省略可能なプロパティとネストしたオブジェクトやリストやマップ)
* [パース済みの JSON オブジェクトなどのマップを検証する](#パース済みの-json-オブジェクトなどのマップを検証する)
* [JSON ライブラリのツリーモデルを検証する](#json-ライブラリのツリーモデルを検証する)
* [リクエストパラメータを検証する](#リクエストパラメータを検証する)
* [結果を扱う](#結果を扱う)
* [バリデータの記述と JSON Schema の生成](#バリデータの記述と-json-schema-の生成)
* [JSpecify による null 性](#jspecify-による-null-性)

0.17 からの移行については [0.18.0 の破壊的変更](BreakingChanges_0.18.0.ja.md) を参照してください。

コード例では次の型を使います。

```java
enum Role { ADMIN, MEMBER, GUEST }
record Address(String street, String city) {}
record User(String email, Optional<String> nickname, Integer age, Role role, Address address, List<String> tags) {}
```

## 単一の値のバリデータ

`Yavi._string`、`Yavi._integer`、`Yavi._localDate` などは、1 つの値のバリデータを、ビルダークラスと `build()` なしで作ります。メソッド名は `Yavi.schema()` のプロパティと同じです。

```java
// before
StringValidator<String> emailValidator = StringValidatorBuilder.of("email", c -> c.notBlank().email()).build();

// after
StringValidator<String> emailValidator = Yavi._string("email", c -> c.notBlank().email());
```

* リストの要素のようにプロパティではない値では、名前を省略できます（`Yavi._string(c -> c.notBlank())`）。そのメッセージは、名前の代わりに「The value」と表示します（`The value must not be blank`）。
* `Yavi.schema()` のプロパティと同じく、名前の有無にかかわらず制約も省略できます（`Yavi._boolean("subscribed")`、`Yavi._enum("role", Role.class)`、`Yavi._object("id", UUID.class)`、`Yavi._string()`）。
* `Yavi._list(elementValidator, c -> c.notEmpty().unique())` と `Yavi._map(valueValidator, c -> c.notEmpty())` は、リストやマップを検証し、リストやマップ自体にも制約を付けます。`Yavi._list(elementValidator)` と `Yavi._map(valueValidator)` は、要素や値だけを検証します。
* `named(name)` は、バリデータを別の名前で使います。[後述](#ルールを別の名前で使う)します。

### 制約の前に値を変換する

`String` の変換は、制約の前に書きます。それ以外の値は `preprocess` で変換します。`null` は変換されません。

```java
StringValidator<String> emailValidator = Yavi._string("email", c -> c.trim().toLowerCase().notBlank().email());
emailValidator.validate("  USER@Example.COM "); // Valid[value=user@example.com]

BigDecimalValidator<BigDecimal> priceValidator = Yavi._bigDecimal("price", c -> c.positive())
        .preprocess(BigDecimal::stripTrailingZeros);
```

`String` の変換には、`trim()`、`toLowerCase()`、`toUpperCase()`、`toNormalForm(form)`、`transform(f)` があります。

### ルールを別の名前で使う

バリデータは、作成時に与えられた名前を保持します。`named(name)` は、同じバリデータを別の名前で返します。値のルールを 1 か所で定義し、複数のプロパティで使えます。

```java
public final class Email {
    public static final StringValidator<String> VALIDATOR = Yavi._string("email", c -> c.trim().notBlank().email());
}

record Contact(String email, @Nullable String backupEmail, String replyTo) {}

ObjectInputValidator<Contact> contactValidator = Yavi.schema()
        ._string(Email.VALIDATOR)                                 // キーも名前も "email"
        ._string(Email.VALIDATOR.named("backupEmail")).nullable() // キーも名前も "backupEmail"
        ._object("replyTo", Email.VALIDATOR)                      // named("replyTo") と同じ
        .fromMap(Contact::new);

contactValidator.validate(Map.of("email", "a", "backupEmail", "b", "replyTo", "c"));
// email:       "email" must be a valid email address
// backupEmail: "backupEmail" must be a valid email address
// replyTo:     "replyTo" must be a valid email address
```

* 単一の値のバリデータでは、違反の名前、メッセージ、`description()` の名前が、指定した名前に置き換わります。そのため、バリデータだけを渡して追加したプロパティのキーも、指定した名前になります。
* オブジェクト、リスト、マップ、ラムダの値のように自身の名前を持たない値では、違反が `prefixed(name)` と同じく指定した名前の下に位置づけられます（`addressValidator.named("home")` は `home.street` を報告します）。
* 名前はコンビネータを越えて引き継がれます。`Email.VALIDATOR.map(Email::new).nullable().named("x")` と `Email.VALIDATOR.named("x").map(Email::new).nullable()` は同じ結果になり、`untyped().named("x")` と `named("x").untyped()` も同じ結果になります。`andThen` は前後両方のバリデータに、`anyOf` はすべての選択肢に名前を渡します。
* スキーマの `_object(name, validator)` は、バリデータを `name` の名前で使います。

### コンビネータ

すべての `ValueValidator` に次のメソッドがあります。

| メソッド | 結果 |
| --- | --- |
| `nullable()`、`optional()`、`defaultValue(value)` | `null` を受け付け、`null`、`Optional.empty()`、デフォルト値のいずれかを返す |
| `recover(fallback)`、`recoverWith(violations -> ...)` | 失敗しない。`null` と不正な値に対してフォールバック値を返す |
| `liftList()`、`liftSet()`、`liftMap()` | すべての要素または値を検証する。違反はインデックスまたはキーの下に位置づけられる。要素は入力の型のサブタイプでもよいので、`Object` のバリデータを lift すると `List<String>` を検証できる |
| `liftList(separator)` | `liftList()` に加えて、`1,2,3` のような 1 つのテキストからもリストを読む |
| `prefixed(name)` | 違反の名前の前に名前を付ける（`orders[1].id`） |
| `named(name)` | 値の違反を別の名前で報告する |
| `ValueValidator.anyOf(a, b)` | 最初に有効だった選択肢の結果。すべて失敗した場合は `object.anyOf` の違反 1 件。この違反は `ConstraintViolation.NoAlternativeMatched` で、各選択肢の違反はその `alternatives()` にある |
| `ValueValidator.lazy(supplier)`、`recursive(self -> ...)` | まだ作られていないバリデータへの参照 |

### 新しい制約

`BigDecimalConstraint` に `fractionDigits(max)` と `integerDigits(max)` が追加されました。Bean Validation の `@Digits` に相当します。桁数は表記ではなく値に対して数えるので、`1.500` の小数部は 1 桁です。

```java
Yavi._bigDecimal("price", c -> c.fractionDigits(2).integerDigits(6));
// 1.505: "price" must have at most 2 fraction digits
```

整数型、`BigInteger`、`BigDecimal` の制約に `multipleOf(divisor)` が追加されました。`Double` と `Float` にはありません。2 進小数の剰余は正確ではないためです。

```java
Yavi._integer("quantity", c -> c.multipleOf(12));
// 30: "quantity" must be a multiple of 12
Yavi._bigDecimal("price", c -> c.multipleOf(new BigDecimal("0.05")));
```

`CharSequenceConstraint` に 3 つの形式が追加されました。

| 制約 | 受け付ける値 | メッセージキー |
| --- | --- | --- |
| `ip()` | IPv4 または IPv6 のアドレス | `charSequence.ip` |
| `uri()` | スキームを持つ URI（`mailto:a@example.com`、`urn:isbn:...`、`s3://bucket/key`）。`url()` と違い、`java.net.URL` が扱えるプロトコルに限らない | `charSequence.uri` |
| `ulid()` | ULID。大文字と小文字のどちらでもよい | `charSequence.ulid` |

`JsonSchema` はこれらを `multipleOf` と `"format": "uri"` として出力します。

形式の制約はすべて、`email()` や `url()` と同じく空文字列を受け付けます。形式の制約は与えられたテキストの形を決めるもので、テキストが与えられていることは `notBlank()` や `notEmpty()` が決めます。そのため、省略可能なテキストの制約は形式によらず同じ書き方になります。`ipv4()`、`ipv6()`、`luhn()`、`isoLocalDate()`、`localDate(pattern)` は 0.17 では `""` を拒否していました。[破壊的変更](BreakingChanges_0.18.0.ja.md#文字列の形式の制約は空文字列を受け付ける)を参照してください。

```java
Yavi._string("homepage", c -> c.url()).validate("");       // valid
Yavi._string("proxy", c -> c.ip()).validate("");           // valid
Yavi._string("proxy", c -> c.notBlank().ip()).validate(""); // "proxy" must not be blank
```

## 省略可能なプロパティとネストしたオブジェクトやリストやマップ

`Yavi.schema()` のプロパティは、直後の修飾メソッドで変えない限り `null` を拒否します。プロパティには、オブジェクト、リスト、マップも指定できます。

```java
ObjectInputValidator<Address> addressValidator = Yavi.schema()
        ._string("street", c -> c.notBlank())
        ._string("city", c -> c.notBlank())
        .fromMap(Address::new); // 次の節を参照

Arguments6Validator<String, String, Integer, Role, Map<String, Object>, List<? extends String>, User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._string("nickname", c -> c.notBlank()).optional()       // Optional<String>
        ._integer("age", c -> c.greaterThanOrEqual(0)).defaultValue(20)
        ._enum("role", Role.class).recover(Role.GUEST)
        ._object("address", addressValidator)                    // address.street
        ._list("tags", Yavi._string(c -> c.notBlank()),          // tags[0]
                c -> c.notEmpty().lessThanOrEqual(10).unique())  // tags
        .apply(User::new)
        .constraintOnTarget(user -> user.age() >= 18 || user.role() != Role.ADMIN, "role",
                ViolationMessage.of("user.adultAdmin", "\"{0}\" must not be ADMIN for a minor"));
```

* `nullable()`、`optional()`、`defaultValue(...)`、`recover(...)`、`recoverWith(...)` は、直前に追加したプロパティに適用されます。
* `_object(name, validator)`、`_list(name, elementValidator)`、`_map(name, valueValidator)` の違反は、`name()`（`address.street`、`tags[1]`、`prices.pen`）でも `path()` でも、プロパティの名前の下に位置づけられます。
* `_list` と `_map` の制約は、すべての要素が有効な場合に検査され、プロパティに対して報告されます（`"tags" must be unique. [a] is/are duplicated.`）。
* `constraintOnTarget` は、複数のプロパティにまたがるルールです。すべてのプロパティが有効な場合に検査されます。
* `flatApply(f)` は、検証を行うファクトリメソッドのような、`Validated` を返す関数を受け取ります。

## パース済みの JSON オブジェクトなどのマップを検証する

`fromMap` は、同じプロパティの定義から、`Map<String, Object>` を読むバリデータを作ります。このバリデータは、プロパティを名前で読み取り、型を検査し、オブジェクトを生成します。キャストもリフレクションも使いません。

```java
ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._string("nickname", c -> c.notBlank()).optional()
        ._integer("age", c -> c.greaterThanOrEqual(0)).defaultValue(20)
        ._enum("role", Role.class)
        ._object("address", addressValidator)
        ._list("tags", Yavi._string(c -> c.notBlank())).defaultValue(List.of())
        .fromMap(User::new);

Validated<User> validated = userValidator.validate(Map.of(
        "email", "jd@example.com",
        "age", 30,
        "role", "ADMIN",
        "address", Map.of("street", "1-2-3", "city", "Tokyo"),
        "tags", List.of("java", "yavi")));
```

| 入力 | 結果 |
| --- | --- |
| プロパティの型を表さない値（`"age": "30"`） | `object.type`: `"age" must be of type integer` |
| 値が変わらない数値（`Integer` に対する `30L`、`30.0`） | 受け付ける。`30.5` は `object.type` |
| enum の定数、日時の値 | 定数の名前、ISO-8601 形式の文字列から読み取る |
| `UUID`、`URI`、`Duration`、`Period`（`_object("id", UUID.class)`） | UUID の正規形の文字列、URI の文字列、ISO-8601 形式の文字列（`PT1H30M`、`P1Y2M`）から読み取る。それ以外は `object.type`: `"id" must be of type UUID` |
| 存在しないキー | `null` と同じように検証する。`optional()`、`nullable()`、`defaultValue(...)`、`recover(...)` があれば受け付け、なければ `object.required`。`required()` を付けると常に `object.required` |
| 明示的な `null` | `object.notNull`、または `null` を拒否する制約の違反。`rejectingNull()` を付けると常に `object.notNull` |
| プロパティが定義されていないキー | 無視する。`rejectingUnknownProperties()` を付けると `object.unknownProperty` を報告する |

* `ignoringCase()` を付けると、enum の定数名の大文字と小文字を区別せずに読み取ります。`._enum(Yavi._enum("role", Role.class).ignoringCase())` は `"admin"` を受け付けます。
* `flatFromMap(f)` は `Validated` を返す関数を受け取ります（`.flatFromMap(DateRange::parse)`）。
* `untyped()` は、バリデータを `ValueValidator<Object, X>` に変えます。たとえば、型の異なる選択肢を組み合わせるときに使います（`ValueValidator.anyOf(Yavi._integer(c -> c.positive()).untyped(), Yavi._string(c -> c.uuid()).untyped())`）。

### 判別可能なユニオン

`DiscriminatedUnionValidator` は、あるキーの値によってマップのバリデータを選びます。sealed インターフェースのサブタイプを読むのに使えます。検証されるのは、選ばれた選択肢だけです。

```java
DiscriminatedUnionValidator<Shape> shapeValidator = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Yavi.schema()._integer("radius", c -> c.positive()).fromMap(Circle::new))
        .when("square", Yavi.schema()._integer("side", c -> c.positive()).fromMap(Square::new));

shapeValidator.validate(Map.of("type", "circle", "radius", 2)); // Circle[radius=2]
shapeValidator.validate(Map.of("type", "x")); // "type" must be one of the following values: [circle, square]
```

### 1 つのマップから複数のオブジェクトと残りのキーを読む

`_embedded` は、ほかのプロパティと同じマップを読みます。JOIN の結果の 1 行にある複数テーブルの列から、オブジェクトを組み立てるときに使えます。`_rest` は、どのプロパティにも定義されていないキーを集めます。

```java
record UserName(String first, String last) {}
record Department(String name, String code) {}
record Employee(Integer id, UserName name, @Nullable Department dept, Map<String, String> attrs) {}

ObjectInputValidator<UserName> userNameValidator = Yavi.schema()
        ._string("first_name", c -> c.notBlank())
        ._string("last_name", c -> c.notBlank())
        .fromMap(UserName::new);

ObjectInputValidator<Department> departmentValidator = Yavi.schema()
        ._string("dept_name", c -> c.notBlank())
        ._string("dept_code", c -> c.notBlank())
        .fromMap(Department::new);

ObjectInputValidator<Employee> employeeValidator = Yavi.schema()
        ._integer("id")
        ._embedded(userNameValidator)              // first_name, last_name
        ._embedded(departmentValidator).nullable() // dept_name と dept_code に値がなければ null
        ._rest(Yavi._string(c -> c.notBlank()))    // 残りのキーの Map<String, String>
        .fromMap(Employee::new);

employeeValidator.validate(Map.of("id", 1, "first_name", "Taro", "last_name", "Yamada", "color", "red"));
// Employee[id=1, name=UserName[first=Taro, last=Yamada], dept=null, attrs={color=red}]
```

16 個を超えるキーを読む場合も `_embedded` を使います。バリデータを渡さない `_rest()` は、値をそのまま受け取ります。`_rest` はスキーマの最後のプロパティです。残りのキーがなければ値は空のマップで、`null` にはなりません。

### 存在しないキーと null を区別する

PATCH リクエスト（JSON Merge Patch）のような部分更新では、存在しないキーはプロパティを変更せず、`null` はプロパティをクリアします。`presence()` はプロパティを `Presence` として読み取ります。`Presence` は `Present(value)`、`PresentNull`、`Absent` のいずれかです。

```java
record ProfilePatch(Presence<String> nickname, Presence<@Nullable String> bio) {}

ObjectInputValidator<ProfilePatch> patchValidator = Yavi.schema()
        ._string("nickname", c -> c.lessThanOrEqual(50)).presence()        // 省略できるが、クリアはできない
        ._string("bio", c -> c.lessThanOrEqual(500)).nullable().presence() // 省略もクリアもできる
        .fromMap(ProfilePatch::new);

ProfilePatch patch = patchValidator.validated(Map.of("bio", "Hello")); // nickname=Absent, bio=Present[Hello]
patch.nickname().ifPresent(profile::setNickname);                      // 呼ばれない
profile.setBio(patch.bio().orElse(profile.getBio()));                  // "bio" が null なら null

switch (patch.bio()) {
    case Presence.Present<String>(String bio) -> profile.setBio(bio);
    case Presence.PresentNull<String> cleared -> profile.setBio(null);
    case Presence.Absent<String> unchanged -> { }
}
```

存在しないキーは検証されません。存在するキーは、`presence()` がない場合と同じように検証されます（`{"nickname": null}` は `"nickname" must not be null`）。

部分更新ではない契約では、存在しないキーと `null` の片方だけを受け付けることがよくあります。`required()` と `rejectingNull()` は、値の型を変えずにどちらを受け付けるかを指定します。

```java
record Account(@Nullable Instant deletedAt, Optional<String> bio, int page) {}

ObjectInputValidator<Account> accountValidator = Yavi.schema()
        ._instant("deletedAt").nullable().required()      // 必ず送る。値がなければ null を送る
        ._string("bio").optional().rejectingNull()        // 省略できる。送るなら文字列
        ._integer("page").defaultValue(1).rejectingNull() // 省略したときだけ 1
        .fromMap(Account::new);

accountValidator.validate(Map.of());                                  // "deletedAt" is required
accountValidator.validate(Collections.singletonMap("deletedAt", null)); // Account[deletedAt=null, bio=Optional.empty, page=1]
```

| プロパティ | 存在しないキー | 明示的な `null` |
| --- | --- | --- |
| `_string("x")` | `object.required` | `object.notNull` |
| `.nullable()`、`.optional()`、`.defaultValue(...)`、`.recover(...)` | 受け付ける | 受け付ける |
| 上記に `.required()` を続ける | `object.required` | 受け付ける |
| 上記に `.rejectingNull()` を続ける | 受け付ける | `object.notNull` |
| `.presence()` | `Absent` | プロパティが検証する |

`required()` と `rejectingNull()` は、`optional()`、`nullable()`、`defaultValue(...)`、`recover(...)` のいずれかの後に書きます。型付きの引数（`apply`）には「存在しない」という状態がないので、`null` はこれらを付けない場合と同じように検証されます。

### 実行時にプロパティが決まるスキーマ

`Yavi.schema()` のプロパティはコンパイル時に決まります。検証した値をコンストラクタに渡すからです。`Yavi.dynamicSchema()` は、アンケートの設問やカスタムフィールドのように、プロパティが設定やデータベースから決まる入力を記述します。そのバリデータは、検証した値を名前ごとに持つマップを返します。

```java
record Question(String id, String title, boolean required, int maxLength) {}

DynamicSchema<String> schema = Yavi.dynamicSchema();
for (Question question : survey.questions()) {
    StringValidator<String> answer = Yavi._string(c -> c.notBlank().lessThanOrEqual(question.maxLength()));
    schema = question.required() ? schema.property(question.id(), answer).label(question.title())
            : schema.property(question.id(), answer.defaultValue("")).label(question.title());
}
ObjectInputValidator<Map<String, String>> answersValidator = schema.build();

answersValidator.validate(Map.of("q1", "yavi")); // {q1=yavi, q2=}
answersValidator.validate(Map.of());             // q1: "Your name" is required
```

スキーマは不変で、`property` は新しいスキーマを返します。プロパティのバリデータは、`named(name)` と同じく、プロパティの名前で使われます。

| プロパティ | マップの内容 |
| --- | --- |
| `property(name, validator)` | 検証した値。値は必須 |
| `property(name, validator.nullable())`、`.defaultValue(...)`、`.recover(...)` | 存在しないキーと `null` に対してバリデータが返す値 |
| 上記に `.required()` または `.rejectingNull()` を続ける | `Yavi.schema()` と同じ |
| `.presence()` を続ける | キーが存在しなければエントリなし |
| `rest(valueValidator)` | どのプロパティにも定義されていないキーの、検証した値 |

* `label`、`required(message)`、`typeMessage` は、[スキーマのプロパティ](#プロパティのラベルとメッセージ)のものと同じです。
* 修飾メソッドの型は `Yavi.schema()` と同じく段階的です。`required()` と `rejectingNull()` は `null` を受け付けるバリデータにだけあり、`presence()` の後には修飾メソッドを続けられません。
* 値の型は、スキーマを代入する変数の型で決まります。複数の型のプロパティを持つスキーマは `Yavi.<Object>dynamicSchema()` で作ります。
* 返されるバリデータは `fromMap` のものと同じ種類です。`rejectingUnknownProperties()`、`readingWith(reader)`、`readingParameters()`、`description()` を持ち、別のスキーマのプロパティにもなります。

```java
record Response(String email, Map<String, String> answers) {}

ValueValidator<Map<String, ?>, Response> responseValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._object("answers", answersValidator) // answers.q1=...&answers.q2=...
        .fromMap(Response::new)
        .readingParameters();
```

### 自分自身を含む構造

`ValueValidator.recursive(self -> ...)` は、自分自身への参照を使ってバリデータを作ります。`ValueValidator.lazy(() -> validator)` は、後で作られるバリデータを参照します。互いに参照し合うバリデータに使います。

```java
record Comment(String body, List<Comment> replies) {}

ObjectInputValidator<Comment> commentValidator = ValueValidator.recursive(self -> Yavi.schema()
        ._string("body", c -> c.notBlank())
        ._list("replies", self).defaultValue(List.of())
        .fromMap(Comment::new));
// 違反の例: replies[0].replies[0].body
```

## JSON ライブラリのツリーモデルを検証する

`readingWith(reader)` は、Jackson の `JsonNode` のような JSON ライブラリのツリーモデルを、マップと同じルールと違反でそのまま読み取ります。ツリーをマップにコピーすることはありません。スキーマの `from(reader, f)` は、`fromMap(f).readingWith(reader)` と同じです。

```java
ValueValidator<JsonNode, User> nodeValidator = userValidator.readingWith(JsonNodeReader.INSTANCE);

Validated<User> validated = nodeValidator.validate(jsonMapper.readTree(json));
```

リーダーはネストしたバリデータにも適用されます。そのため `addressValidator` は、最終的にどの入力を読む場合でも `fromMap` で作ります。YAVI は JSON ライブラリに依存しません。`InputReader` は、ツリーモデルごとに一度だけ書く、状態を持たない小さなクラスです。Jackson 3 向けのものを次に示します。

```java
public final class JsonNodeReader implements CoercibleInputReader<JsonNode> {

    public static final JsonNodeReader INSTANCE = new JsonNodeReader();

    @Override
    public boolean isNull(JsonNode node) {
        return node.isNull() || node.isMissingNode();
    }

    @Override
    public Object value(JsonNode node) { // スカラーを JDK の型で返す
        if (node.isString()) {
            return node.stringValue();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return node;
    }

    @Override
    public boolean isObject(JsonNode node) {
        return node.isObject();
    }

    @Override
    public @Nullable JsonNode property(JsonNode object, String name) {
        return object.get(name); // 存在しなければ null
    }

    @Override
    public Iterable<Map.Entry<String, JsonNode>> properties(JsonNode object) {
        return object.properties();
    }

    @Override
    public boolean isArray(JsonNode node) {
        return node.isArray();
    }

    @Override
    public Iterable<JsonNode> elements(JsonNode array) {
        return array;
    }

}
```

リーダーは、ツリーが保持している数値をそのまま返します。既定の `JsonMapper` は、ツリーでもマップでも、小数部を持つ数値を `double` として保持します。そのため `_bigDecimal` は `0.10` を `0.1`、`12345678901234567.89` を `12345678901234568` として受け取り、`_double` は `1e400` を `Infinity` として受け取ります。また、同じキーが 2 回書かれていると、最後の値が残ります。数値を書かれたとおりに保持し、重複したキーを拒否するには、マッパーを次のように設定します。

```java
JsonMapper jsonMapper = JsonMapper.builder()
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS) // 0.10 は BigDecimal の 0.10 になる
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION) // {"role": "user", "role": "admin"} を拒否する
        .build();
```

JDK のツリーモデル（[JEP 540](https://openjdk.org/jeps/540)。JDK 28 のインキュベータモジュール）も同じ方法で読み取れます。README に示すリーダーは数値をそのテキストから読み取り、パーサーは重複したキーを拒否します。

スカラーがすべて文字列であるツリーは、リーダーの `coercingStrings()` で読み取ります（`JsonNodeReader.INSTANCE.coercingStrings()`。JDK の型のツリーには `InputReader.ofJdkTypes().coercingStrings()`）。文字列は、次の節のルールで読み取られます。

## リクエストパラメータを検証する

リクエストパラメータや送信されたフォームは、文字列のフラットなマップであり、1 つのキーに複数の値を持つこともあります。`fromMap` で作ったバリデータの `readingParameters()` は、このようなマップを読み取ります。文字列は、プロパティの型の値を表すテキストとして扱われ、キーはネストした値へのパスとして扱われます。

```java
record SearchForm(String q, Integer page, Boolean includeArchived, @Nullable LocalDate since, List<String> tags,
        @Nullable Address address) {}

ValueValidator<Map<String, ?>, SearchForm> searchFormValidator = Yavi.schema()
        ._string("q", c -> c.notBlank())
        ._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
        ._boolean("includeArchived").defaultValue(false)
        ._localDate("since").nullable()
        ._list("tags", Yavi._string(c -> c.notBlank())).defaultValue(List.of())
        ._object("address", addressValidator).nullable()
        .fromMap(SearchForm::new)
        .readingParameters();

@GetMapping("/search")
Object search(@RequestParam MultiValueMap<String, String> params) {
    // ?q=yavi&page=2&since=&tags=a&tags=b&address.street=1-2-3&address.city=Tokyo
    Validated<SearchForm> validated = searchFormValidator.validate(params);
    // SearchForm[q=yavi, page=2, includeArchived=false, since=null, tags=[a, b], address=Address[street=1-2-3, city=Tokyo]]
}
```

バリデータは、`Map<String, String>`、Spring の `MultiValueMap` のような `Map<String, List<String>>`、Servlet API の `Map<String, String[]>` のどの形のマップも受け取ります。型は代入先にかかわらず `ValueValidator<Map<String, ?>, X>` なので、`readingParameters()` の後にどのコンビネータも続けられます。

```java
ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        .fromMap(Signup::new)
        .readingParameters()
        .formattingWith(new MessageSourceMessageFormatter(messageSource::getMessage));
```

値のルール:

* 空文字列は、プロパティの型にかかわらず `null` と同じように検証されます。`""` を受け取りたい文字列のプロパティには `defaultValue("")` を付けます。
* テキストには `null` がありません。そのため、空文字列が `object.notNull` で報告されるはずのプロパティでは、存在しないキーと同じ `object.required`（`"page" is required`）で報告されます。`notBlank()` や `notNull().message(...)` のように `null` を拒否する制約の違反は、そのまま報告されます。
* 数値は、前後の空白や桁区切りのない 10 進数です。`"30"` と `"30.0"` は `Integer` として読めますが、`"30.5"`、`" 30"`、`"1,000"` は読めません。真偽値は `"true"` または `"false"` です。
* 型を表さない文字列は、例外ではなく `object.type` で報告されます（`"page" must be of type integer`）。

キーのルール:

* `address.city` と `address[city]` は、`address` の `city` プロパティです。`tags[]` は `tags` と同じです。
* 複数の値を持つキーはリストです（`tags=a&tags=b`）。スカラーのプロパティに複数の値がある場合は `object.type` で報告されます。
* インデックス付きのキーは、インデックス順のリストです（`items[0].name=pen&items[1].name=ink`）。
* 空のリストは送信できません。キーが存在しないことになります。チェックボックスのように空になりうるリストには `defaultValue(List.of())` を付けます。
* `splitBy` は、1 つの値（`tags=a,b`）からリストを読みます。[後述](#1-つのパラメータに入ったリスト)します。

フォームのフィールドや CSV のセルのように人が書いたテキストに対しては、`StringCoercion` でこれらのルールを緩められます。

```java
record Signup(Integer age, Boolean subscribed) {}

StringCoercion form = StringCoercion.strict().trimming().booleanWords();

ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
        ._integer("age")
        ._boolean("subscribed").defaultValue(false) // チェックされていないチェックボックスは何も送信しない
        .fromMap(Signup::new)
        .readingParameters(form);

signupValidator.validate(Map.of("age", " 30 ", "subscribed", "on")); // Signup[age=30, subscribed=true]
```

* `trimming()`: どのプロパティのテキストも、前後の空白を除いて読み取られます。空白だけの文字列は `null` と同じように検証されます。
* `booleanWords()`: `true`、`1`、`yes`、`on` と `false`、`0`、`no`、`off` を、大文字と小文字を区別せずに真偽値として読み取ります。

`trimming()` は `String` のプロパティもトリムします。これだけで、すべてのフィールドが「トリムされ、空白だけなら値なし」になり、プロパティごとに `c.trim()` を書く必要はありません。パスワードのように空白も値の一部である `String` は、バリデータの `untrimmed()` でそのことを示します。

```java
record Contact(String name, @Nullable String company, String password) {}

ValueValidator<Map<String, ?>, Contact> contactValidator = Yavi.schema()
        ._string("name", c -> c.notBlank().lessThanOrEqual(50))
        ._string("company").nullable()
        ._string(Yavi._string("password", c -> c.greaterThanOrEqual(8)).untrimmed())
        .fromMap(Contact::new)
        .readingParameters(StringCoercion.strict().trimming());

contactValidator.validate(Map.of("name", " Alice ", "company", "  ", "password", " pass word "));
// Contact[name=Alice, company=null, password= pass word ]
```

* 制約はトリムされたテキストを見ます。`" Alice "` は 5 文字です。
* `untrimmed()` は値の前後の空白を残します。空白だけの値は、この場合も値なしです。`Arguments1Validator` を返すので、`map` や `named` の後に 1 度だけ書きます。
* トリムしないリーダー（`readingParameters()`、JSON のツリー）は、`String` を与えられたとおりに読みます。その場合は `c.trim()` でトリムします。

リクエストを失敗させたくないパラメータには `recover` が使えます。

```java
record PageRequest(Integer page, Integer size) {}

ValueValidator<Map<String, ?>, PageRequest> pageValidator = Yavi.schema()
        ._integer("page", c -> c.greaterThanOrEqual(0)).recover(0)
        ._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
        .fromMap(PageRequest::new)
        .readingParameters();

pageValidator.validate(Map.of("page", "-1", "size", "9999")); // PageRequest[page=0, size=20]
```

### 1 つのパラメータに入ったリスト

1 つのパラメータが複数の値を持つことはよくあります。`?tags=java,yavi`、`?ids=1,2,3`、`textarea` に 1 行ずつ書いた URI などです。`_list` の直後に `splitBy` を書くと、このようなテキストをリストとして読みます。

```java
record Article(String title, List<String> tags, List<Integer> related, List<URI> links) {}

ValueValidator<Map<String, ?>, Article> articleValidator = Yavi.schema()
        ._string("title", c -> c.notBlank())
        ._list("tags", Yavi._string(c -> c.notBlank().lessThanOrEqual(10)), c -> c.unique()).splitBy(",")
        ._list("related", Yavi._integer(c -> c.positive())).splitBy(",").defaultValue(List.of())
        ._list("links", Yavi._object(URI.class)).splitBy(Pattern.compile("\\R")).defaultValue(List.of())
        .fromMap(Article::new)
        .readingParameters();

articleValidator.validate(Map.of("title", "t", "tags", "java,yavi", "related", "1,2,3"));
// Article[title=t, tags=[java, yavi], related=[1, 2, 3], links=[]]

articleValidator.validate(Map.of("title", "t", "tags", "java,,toolongtagname", "related", "1,x"));
// tags[1]:    "tags[1]" must not be blank
// tags[2]:    The size of "tags[2]" must be less than or equal to 10. The given size is 14
// related[1]: "related[1]" must be of type integer
```

* 区切りは、そのまま解釈される文字列か `Pattern` で指定します。
* 要素は、ほかのパラメータと同じく、値を表すテキストとして読まれます。違反は、同じキーを繰り返した場合と同じく、インデックスの下に位置づけられます。
* すべてのテキストが分割されます。同じキーを繰り返した場合の各値も分割されるので、`tags=a,b&tags=c` は `[a, b, c]` です。
* 要素は捨てられず、前後の空白も除かれません。`a,,b` は 3 要素で、2 番目の要素には値がありません。例外は末尾の区切りで、その後ろは要素になりません。最後の行の後の改行と同じく、`a,b,` は `[a, b]` です。空白は、パターン（`\\s*,\\s*`）、`StringCoercion.strict().trimming()`、`c.trim()` のいずれかで除きます。
* 空のパラメータ（`tags=`）は、ほかのプロパティと同じく、値がないものとして扱われます。
* `splitBy` は `_list` の直後、ほかの修飾メソッドの前に書きます。
* ほかの入力も同じように読まれます。`{"tags": "java,yavi"}` と `{"tags": ["java", "yavi"]}` は同じリストです。このプロパティの JSON Schema は `"type": ["array", "string"]` になります。
* `{"tags": 1}` のようにテキストでもリストでもない値は、`object.type` で報告されます（`"tags" must be of type array or string`）。1 要素のリストとしては扱われません。リストの要素のうちテキストでないものは、そのまま検証されます。
* スキーマのプロパティではないリストは、`Yavi._list(elementValidator, constraints).splitBy(",")` または `elementValidator.liftList(pattern)` で分割します。
* 型付きの引数（`apply`）はすでに要素を持っているので、これまでと同じように検証されます。

### プロパティのラベルとメッセージ

フォームは項目をラベルで呼びます。また、値がない（`object.required`）、型が違う（`object.type`）という違反は制約を検査する前に報告されるため、制約の `message(...)` では変えられません。これらの読ませ方はプロパティに指定します。

```java
record Token(String name, Integer ttl, List<String> roles) {}

ValueValidator<Map<String, ?>, Token> tokenValidator = Yavi.schema()
        ._string("name", c -> c.notBlank()).label("Token name")
        ._integer("ttl", c -> c.positive()).typeMessage("\"{0}\" must be a number of seconds")
        ._list("roles", Yavi._string(c -> c.notBlank())).label("Roles").required("Select at least one role")
        .fromMap(Token::new)
        .readingParameters();

tokenValidator.validate(Map.of("name", List.of(""), "ttl", List.of("abc"))).errors().messagesByName();
// {name=["Token name" must not be blank], ttl=["ttl" must be a number of seconds], roles=[Select at least one role]}
```

| 修飾子 | 変わるもの |
| --- | --- |
| `label("Token name")` | プロパティとその要素（`roles`、`roles[1]`）のすべてのメッセージの `{0}`。違反の名前、パス、ポインタは変わらないので、`messagesByName()` のキーと `ViolationDetail` の `name` は元のままです。ラベルは `violation.label()` で取れます。 |
| `typeMessage(text)` | プロパティとその要素の `object.type` のメッセージ。`{1}` は型の名前、`{2}` は値です。 |
| `required(text)` | `object.required` のメッセージ。キーがない場合と、値のないテキストの場合に報告されます。`optional()`、`nullable()`、`defaultValue(...)` の後では、`required()` と同じくキーも必須にします。 |

* テキストは、制約の `message("...")` のテキストと同じく、そのままメッセージになります。`object.required` のメッセージを持つフォーマッタも、これを置き換えません（後述）。
* `typeMessage` と `required` は `ViolationMessage` も受け取ります。この場合はメッセージキーも置き換わるため、リソースバンドルで地域化できます: `required(ViolationMessage.of("token.roles", "Select at least one role"))`。
* `label` と `typeMessage` はプロパティを追加した直後、`optional()` などの前に書きます。`required(...)` は最後の修飾子で、`.required("...").optional()` はコンパイルできません。
* ネストしたプロパティ（`address.street`）の違反は、そのプロパティの指定に従います。明示的な `null` と、存在しない enum 定数は従来どおり報告されます（`object.notNull`、`object.oneOf`）。
* `Yavi.dynamicSchema()` のプロパティにも同じ修飾メソッドがあります。
* `ObjectInputValidator.of(parts, mapper)` のプロパティには、同じ内容を `PropertyMessages` で渡します。

制約のメッセージは、`ValidatorBuilder` と同じく `MessageFormatter` で決まります。`formattingWith` はバリデータが報告するすべての違反をそのフォーマッタで整形します。違反を受け取った後に、フォーマッタやリソースバンドルのラベルを与えることもできます。

```java
MessageFormatter formatter = new MessageSourceMessageFormatter(messageSource::getMessage);

// バリデータのすべての違反。ネストしたバリデータやリーダーが報告するものも含む
ValueValidator<Map<String, ?>, Token> localized = tokenValidator.formattingWith(formatter);

// または違反を表示するとき
violations.labeled(violation -> messageSource.getMessage("label." + violation.name(), null, null, locale))
        .formattedWith(formatter)
        .messagesByName();
```

`labeled` は、関数が `null` を返した違反をそのままにします。

フォーマッタは違反のキーでメッセージを探しますが、1 つの制約や 1 つのプロパティのためにコードに書いたテキスト（`message("...")`、`required("...")`、`typeMessage("...")`）については探しません。バンドルがキーのすべての違反に対して持つメッセージより、具体的なテキストが優先されます。違反のキーは変わりません。

```java
// messages.properties: object.required={0} is required.
tokenValidator.formattingWith(formatter).validate(Map.of()).errors().messagesByName();
// {name=[Token name is required.], ttl=[ttl is required.], roles=[Select at least one role]}
```

| API | 説明 |
| --- | --- |
| `ViolationMessage#withText(text)` | このメッセージのキーと引数名を持ち、テキストをフォーマットとするメッセージ。どのフォーマッタもこれを置き換えません。`required("...")` が渡すのは `OBJECT_REQUIRED.withText("...")` です。 |
| `ViolationMessage#localizable()`、`ConstraintViolation#localizable()` | テキストでは `false`。 |
| `MessageFormatter#format(String, Object[], Locale)` | テキストを整形します。デフォルトは `MessageFormat` で整形します。ほかのメッセージと同じ方法で整形するにはオーバーライドします。 |
| `Constraint#predicates(List)` | `url(url -> ...)` や `password(...)` のように、複数の述語を 1 つの制約として追加します。直後の `message(...)` はそのすべてのメッセージを置き換えます。 |

### Spring MVC でフォームを再表示する

Spring MVC が `BindingResult` を用意するのは、自身がバインドしたフォームオブジェクトに対してだけです。パラメータのマップには用意されません。そこで、コントローラの `@ModelAttribute` メソッドで、パラメータの `MapBindingResult` をモデルに入れます。こうすると、ハンドラは `BindingResult` を引数で受け取れます。Thymeleaf の `th:object`、`th:field`、`#fields` も、フォームオブジェクトの場合と同じように使えます。追加のライブラリは要りません。

```java
@Controller
class SignupController {

    static final ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
            ._integer("age")
            ._boolean("subscribed").defaultValue(false)
            .fromMap(Signup::new)
            .readingParameters(StringCoercion.strict().trimming().booleanWords());

    @ModelAttribute
    void signupForm(@RequestParam Map<String, String> params, Model model) {
        model.addAllAttributes(new MapBindingResult(params, "signupForm").getModel());
    }

    @GetMapping("/signup")
    String form() {
        return "signup";
    }

    @PostMapping("/signup")
    String signup(@RequestParam MultiValueMap<String, String> params, BindingResult result) {
        return switch (signupValidator.validate(params)) {
            case Validated.Valid<Signup>(Signup signup) -> {
                register(signup);
                yield "redirect:/";
            }
            case Validated.Invalid<Signup>(ConstraintViolations violations) -> {
                violations.apply(result::rejectValue);
                yield "signup";
            }
        };
    }

}
```

```html
<form method="post" th:action="@{/signup}" th:object="${signupForm}">
    <input type="text" th:field="*{age}">
    <span th:if="${#fields.hasErrors('age')}" th:errors="*{age}"></span>
    <input type="checkbox" th:field="*{subscribed}" value="on">
    <button type="submit">Sign up</button>
</form>
```

* `@ModelAttribute` メソッドはコントローラのすべてのハンドラの前に実行されるので、GET のハンドラでも空のフォームを表示できます。このメソッドがないと、リクエストは `An Errors/BindingResult argument is expected to be declared immediately after the model attribute` で失敗します。
* Spring が引数に渡すのは、モデルの最後の属性になっている `BindingResult` です。`BindingResult` の前には、モデルに属性を追加する引数を置かないでください。
* フィールドには、送信されたテキストがそのまま再表示されます。プロパティの型を表さないテキスト（`age` に対する `abc`）も再表示されます。
* フィールドエラーのコードはメッセージキー（`object.type`）なので、これまでどおり `MessageSource` でメッセージを差し替えられます。プロパティにラベルがあれば、そのメッセージの `{0}` はラベルです。
* Thymeleaf はチェックボックスに隠しフィールド `_subscribed` を追加します。このキーに対応するプロパティは定義されていないので、このようなフォームは `rejectingUnknownProperties()` を付けたバリデータでは読めません。
* フィールドの名前は、`th:field` が出力する形（`address.city`、`items[0].name`）にします。この形が違反の名前であり、`MapBindingResult` が値を引くキーでもあるからです。`address[city]` の形で送信された値は、検証はされますが再表示されません。
* ハンドラは `MultiValueMap` を検証します。そのため、1 つのキーに複数の値があれば、リストとして読まれるか、スカラーのプロパティでは `object.type` で報告されます。`Map<String, String>` で受けると、最初の値以外は捨てられます。
* `@ModelAttribute` メソッドは `Map<String, String>` で受けます。`MultiValueMap` を渡すと、テキストフィールドに値のリストが `[30]` のように表示されるからです。チェックボックスのように複数の値を持つキー（`tags=a&tags=b`）がある場合は、そのキーの値をリストで持つマップを `MapBindingResult` に渡します。そうしないと、最初のチェックボックスしか再びチェックされません。要素の違反は `tags[1]` に付くので、`#fields.hasErrors('tags*')` で調べます。

## 結果を扱う

`Validated` は、record の `Valid` と `Invalid` からなる sealed インターフェースになりました。結果は網羅的な `switch` で扱えます。

```java
// before
Validated<User> validated = userValidator.validate(map);
if (validated.isValid()) {
    save(validated.value());
}
else {
    validated.errors().forEach(violation -> report(violation.name(), violation.message()));
}

// after
switch (userValidator.validate(map)) {
    case Validated.Valid<User>(User user) -> save(user);
    case Validated.Invalid<User>(ConstraintViolations violations) ->
        violations.forEach(violation -> report(violation.name(), violation.message()));
}
```

`Arguments1` から `Arguments16` も record になりました（`case Validated.Valid(Arguments2(var name, var age))`）。

違反の位置は、名前をパースしなくても取得できます。

```java
violation.name();    // tags[1]
violation.path();    // [Property[name=tags], Index[index=1]]
violation.pointer(); // /tags/1 (JSON Pointer, RFC 6901)
```

`ConstraintViolations` には、フォームや API レスポンスで必要になる形式が用意されています。

```java
// フォーム向け: フィールドごとのメッセージ
Map<String, List<String>> byName = violations.messagesByName();
// {email=["email" must not be blank], address.street=["address.street" must not be blank]}

// 同じ内容で、キーが JSON Pointer
Map<String, List<String>> byPointer = violations.messagesByPointer();
// {/email=[...], /address/street=[...]}

// API レスポンス向け: JSON ライブラリがそのままシリアライズできる record
List<ViolationDetail> details = violations.details();
// [{"name": "address.street", "pointer": "/address/street", "messageKey": "charSequence.notBlank",
//   "message": "\"address.street\" must not be blank", "params": {}}, ...]
```

詳細の `params` は、違反の引数を名前で持ちます。クライアントは、メッセージフォーマットのどの位置に何が入るかを知らなくても、`messageKey` から独自のメッセージを組み立てられます。制約に違反した値は詳細に含まれません。パスワードやトークンかもしれず、クライアントが送ってきた値でもあるためです。位置引数と違反値は `ConstraintViolation` の `args()` と `violatedValue()` で取れます。

```java
// _string("password", c -> c.greaterThanOrEqual(8)) と _integer("age", c -> c.lessThanOrEqual(150))
List<ViolationDetail> details = violations.details();
// [{"name": "password", "pointer": "/password", "messageKey": "container.greaterThanOrEqual",
//   "message": "The size of \"password\" must be greater than or equal to 8. The given size is 6",
//   "params": {"min": 8, "actualSize": 6}},
//  {"name": "age", "pointer": "/age", "messageKey": "numeric.lessThanOrEqual",
//   "message": "\"age\" must be less than or equal to 150", "params": {"max": 150}}]

ConstraintViolation violation = violations.get(1);
violation.params();        // {max=150}
violation.args();          // ["age", 150, 300]。メッセージフォーマットの位置引数
violation.violatedValue(); // 300
```

名前は制約メソッドのパラメータ名です。制約が値から求めたもの (`actualSize`、`duplicates`、`missing`、`notAllowed`) にも名前があります。表にないメッセージキーにパラメータはありません。

| メッセージキー | `params` の名前 |
| --- | --- |
| `object.equalTo`, `map.containsValue` | `value` |
| `object.oneOf`, `object.notOneOf`, `charSequence.url.protocol`, `charSequence.url.host`, `charSequence.url.port` | `values` |
| `object.anyOf` | `count` |
| `object.type` | `type` |
| `numeric.greaterThan`, `numeric.greaterThanOrEqual` | `min` |
| `numeric.lessThan`, `numeric.lessThanOrEqual`, `numeric.fractionDigits`, `numeric.integerDigits` | `max` |
| `numeric.multipleOf` | `divisor` |
| `container.greaterThan`, `container.greaterThanOrEqual`, `byteSize.greaterThan`, `byteSize.greaterThanOrEqual` | `min`, `actualSize` |
| `container.lessThan`, `container.lessThanOrEqual`, `byteSize.lessThan`, `byteSize.lessThanOrEqual` | `max`, `actualSize` |
| `container.fixedSize`, `byteSize.fixedSize` | `size`, `actualSize` |
| `charSequence.contains` | `substring` |
| `charSequence.startsWith` | `prefix` |
| `charSequence.endsWith` | `suffix` |
| `charSequence.pattern`, `charSequence.localdate` | `pattern` |
| `collection.contains`, `array.contains` | `element` |
| `collection.unique` | `duplicates` |
| `collection.containsAll` | `values`, `missing` |
| `map.containsKey` | `key` |
| `codePoints.asWhiteList`, `codePoints.asBlackList` | `notAllowed` |
| `password.required` | `policy` |
| `password.optional` | `min`, `policies` |
| `temporal.before`, `temporal.beforeOrEqual`, `temporal.after`, `temporal.afterOrEqual` | `other` |
| `temporal.between` | `rangeFrom`, `rangeTo` |
| `temporal.field` | `field` |

独自のメッセージでは、`ViolationMessage.of` で `{1}` 以降の引数に名前を付けます。名前を付けなければ、その違反にパラメータはありません。メッセージフォーマットはこれまでどおり位置で引数を参照します。

```java
ViolationMessage outOfStock = ViolationMessage.of("order.outOfStock", "\"{0}\" exceeds the stock of {1}", "stock");
return Validated.failureWith(outOfStock, quantity, locale, stock);
// "messageKey": "order.outOfStock", "params": {"stock": 3}
```

`ValueValidator.anyOf` のどの選択肢にも一致しなかった場合、結果は違反 1 件です。各選択肢の違反は 1 つのリストに混ぜられません。混ぜると、どの違反がどの選択肢のものかがわからなくなるためです。この違反は `ConstraintViolation.NoAlternativeMatched` です。`alternatives()` を持つのはこの型だけで、選択肢ごとの `ConstraintViolations` を返します。それらはこの違反と一緒に名前が付け替えられます。

```java
ConstraintViolations violations = Yavi.schema()
    ._object("contact", ValueValidator.anyOf(emailContact, phoneContact))
    .fromMap(contact -> contact)
    .validate(Map.of("contact", Map.of("kind", "email", "value", "x")))
    .errors();

violations.messagesByName();
// {contact=["contact" must satisfy at least one of the 2 alternatives]}

if (violations.get(0) instanceof ConstraintViolation.NoAlternativeMatched unmatched) {
    unmatched.alternatives().get(0).messagesByName();
    // {contact.value=["contact.value" must be a valid email address]}
    unmatched.alternatives().get(1).messagesByName();
    // {contact.kind=["contact.kind" must be equal to phone], contact.value=["contact.value" must match ^\d{10,15}$]}

    // 要約の代わりに見せる、違反がもっとも少ない選択肢
    ConstraintViolations closest = unmatched.alternatives().stream()
        .min(Comparator.comparingInt(ConstraintViolations::size))
        .orElseThrow();
}

// 詳細に対する switch に default は要らない。record はこの 2 つだけ
for (ViolationDetail detail : violations.details()) {
    List<String> messages = switch (detail) {
        case ViolationDetail.Simple simple -> List.of(simple.message());
        case ViolationDetail.NoAlternativeMatched unmatched -> unmatched.alternatives().stream()
            .flatMap(List::stream)
            .map(ViolationDetail::message)
            .toList();
    };
}
```

`ViolationDetail` は 2 つの record からなる sealed interface です。この違反の詳細は `ViolationDetail.NoAlternativeMatched` で、選択肢ごとの詳細のリストである `alternatives` を持ちます。ほかの詳細は `ViolationDetail.Simple` なので、JSON にこのプロパティは出ません。

自分で書く検証ステップでは、1 回の呼び出しで失敗を返せます。

```java
// before
return Validated.failureWith(ConstraintViolation.builder()
        .name("")
        .messageKey("json.invalid")
        .defaultMessageFormat("\"{0}\" cannot be parsed as JSON")
        .argsWithPrependedName()
        .locale(locale)
        .build());

// after
return Validated.failureWith(ViolationMessage.of("json.invalid", "\"{0}\" cannot be parsed as JSON"), value, locale);
```

## バリデータの記述と JSON Schema の生成

バリデータは、自分が期待する入力を記述できます。`description()` は、オブジェクト、リスト、マップ、選択肢、制約付きの値からなる読み取り専用のツリー（`ValueDescription`）を返し、ツールはこのツリーをたどれます。`JsonSchema` はそのようなツールの 1 つで、JSON ライブラリを必要としません。

```java
ValueDescription description = userValidator.description();
Map<String, Object> schema = JsonSchema.of(description);
String json = JsonSchema.toJson(description);
```

```json
{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{
  "email":{"type":"string","minLength":1,"format":"email"},
  "nickname":{"type":["string","null"],"minLength":1},
  "age":{"type":["integer","null"],"minimum":0,"default":20},
  "role":{"type":"string","enum":["ADMIN","MEMBER","GUEST"]},
  "address":{"type":"object","properties":{"street":{"type":"string","minLength":1},"city":{"type":"string","minLength":1}},"required":["street","city"]},
  "tags":{"type":["array","null"],"items":{"type":"string","minLength":1},"default":[]}},
 "required":["email","role","address"]}
```

* プロパティは、存在しないキーを受け付ける場合を除いて `required` になります。
* リストやマップの制約は `minItems` や `uniqueItems` などとして、`rejectingUnknownProperties()` は `additionalProperties: false` として、再帰的な構造は `$ref` として出力されます。
* ラムダ、条件付きの制約、`constraintOnTarget`、`ValidatorBuilder` で作った Bean の `Validator` は記述に含まれません。ラムダには `describedAs(...)` で記述を与えられます。

## JSpecify による null 性

YAVI には [JSpecify](https://jspecify.dev) のアノテーションが付いているので、NullAway と Kotlin が YAVI の API の null 性を検査します。検証を通った値は宣言どおりの型で渡されるため、`apply` に渡す関数には `@Nullable` のパラメータも `Objects.requireNonNull` も必要ありません。

```java
record Member(String name, @Nullable String nickname) {}

Arguments2Validator<String, @Nullable String, Member> memberValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable()
        .apply(Member::new);
```

`@Nullable` になるのは、`nullable()` を付けたプロパティの値だけです。NullAway が正しいコードを報告する場合は、[NullAway の制限事項](NullAwayLimitations.md) を参照してください。

## 関連するプルリクエスト

| 機能 | プルリクエスト |
| --- | --- |
| sealed な `Validated`、`path()` | [#537](https://github.com/making/yavi/pull/537) |
| コンビネータ、`_object`、`_list`、`_map`、`constraintOnTarget` | [#538](https://github.com/making/yavi/pull/538) |
| `description()`、JSON Schema | [#539](https://github.com/making/yavi/pull/539) |
| `fromMap`、`untyped()`、`DiscriminatedUnionValidator` | [#540](https://github.com/making/yavi/pull/540) |
| `InputReader`、`readingWith` | [#541](https://github.com/making/yavi/pull/541) |
| `coercingStrings()`、`readingParameters()` | [#542](https://github.com/making/yavi/pull/542)、[#543](https://github.com/making/yavi/pull/543) |
| Spring MVC でのリクエストパラメータの `BindingResult`（ドキュメント） | [#569](https://github.com/making/yavi/pull/569) |
| `label`、`required(message)`、`typeMessage`、`formattingWith`、`labeled`、`formattedWith` | [#571](https://github.com/making/yavi/pull/571) |
| コードに書いたテキストはメッセージを探さない、`ViolationMessage#withText`、`localizable()`、`Constraint#predicates(List)` | [#575](https://github.com/making/yavi/pull/575) |
| プロパティの空のテキストに対する `object.required`、`InputReader#isEmptyText` | [#570](https://github.com/making/yavi/pull/570) |
| `named`、`splitBy`、`liftList(separator)`、`Yavi.dynamicSchema()` | [#572](https://github.com/making/yavi/pull/572) |
| lift したバリデータ、`_list`、`_map` はサブタイプのコンテナを受け取る | [#576](https://github.com/making/yavi/pull/576) |
| `String` の `trimming()`、`untrimmed()`、制約のない名前付きバリデータ（`Yavi._enum(name, type)`）、`Yavi._list(elementValidator)`、すべての形式の制約での空文字列 | [#573](https://github.com/making/yavi/pull/573) |
| `_embedded`、`_rest` | [#545](https://github.com/making/yavi/pull/545) |
| `Yavi._string` など | [#546](https://github.com/making/yavi/pull/546) |
| 変換、`preprocess` | [#547](https://github.com/making/yavi/pull/547) |
| `_list` と `_map` の制約 | [#548](https://github.com/making/yavi/pull/548) |
| `flatApply`、`flatFromMap`、`failureWith`、record の `ArgumentsN` | [#549](https://github.com/making/yavi/pull/549) |
| `recover`、`recoverWith` | [#550](https://github.com/making/yavi/pull/550) |
| `messagesByName()`、`pointer()`、`ViolationDetail` | [#551](https://github.com/making/yavi/pull/551) |
| 違反と詳細の `params()`、`ViolationMessage#paramNames()` | [#568](https://github.com/making/yavi/pull/568) |
| `ignoringCase()`、`StringCoercion`、`fractionDigits` | [#552](https://github.com/making/yavi/pull/552) |
| `object.anyOf` の `alternatives()`、`multipleOf`、`ip()`、`uri()`、`ulid()`、文字列からの `UUID`、`URI`、`Duration`、`Period` | [#566](https://github.com/making/yavi/pull/566) |
| `lazy`、`recursive` | [#553](https://github.com/making/yavi/pull/553) |
| `presence()` | [#555](https://github.com/making/yavi/pull/555) |
| `required()`、`rejectingNull()` | [#562](https://github.com/making/yavi/pull/562) |
| 効果のない修飾メソッドの連鎖はコンパイルできない、`NullAcceptingValueValidator` | [#563](https://github.com/making/yavi/pull/563)、[#564](https://github.com/making/yavi/pull/564)、[#565](https://github.com/making/yavi/pull/565) |
| JSpecify、`@Nullable` のない値 | [#534](https://github.com/making/yavi/pull/534)、[#556](https://github.com/making/yavi/pull/556)、[#557](https://github.com/making/yavi/pull/557) |
