# 0.18.0 の新機能

[English](NewFeatures_0.18.0.md)

YAVI 0.18.0 では、`Yavi.schema()` で入力を一度記述すれば、型付きの引数、マップ、JSON のツリー、リクエストパラメータのどれからでも読み取れるようになりました。`Yavi.schema()` は `Yavi.arguments()` を置き換えるもので、`Yavi.arguments()` は非推奨になりました。

* [単一の値のバリデータ](#単一の値のバリデータ)
* [省略可能なプロパティとネストしたオブジェクトやリストやマップ](#省略可能なプロパティとネストしたオブジェクトやリストやマップ)
* [パース済みの JSON オブジェクトなどのマップを検証する](#パース済みの-json-オブジェクトなどのマップを検証する)
* [JSON ライブラリのツリーモデルを検証する](#json-ライブラリのツリーモデルを検証する)
* [リクエストパラメータを検証する](#リクエストパラメータを検証する)
* [オブジェクトを読み取り元のマップに書き戻す](#オブジェクトを読み取り元のマップに書き戻す)
* [結果を扱う](#結果を扱う)
* [バリデータの記述と JSON Schema の生成](#バリデータの記述と-json-schema-の生成)
* [JSpecify による null 性](#jspecify-による-null-性)
* [付録: 登録フォームとその行と編集フォーム](#付録-登録フォームとその行と編集フォーム)

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

`CollectionConstraint` に `unique(key)` が追加されました。オブジェクトの ID のような、要素のキーで一意であることを求めます。重複したキーはメッセージと `params` に入ります。JSON Schema には対応するキーワードがないため、`uniqueItems` は `unique()` に対してだけ出力されます。

```java
Yavi.schema()._list("products", productValidator, c -> c.unique(Product::id));
// "products" must be unique. [APPLE] is/are duplicated.
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
* `constraintOnTarget` は、複数のプロパティにまたがるルールです。すべてのプロパティが有効な場合に検査されます。続けて追加したルールはすべて検査され、プロパティの違反と同じように、違反したルールがすべて報告されます。`map` や `andThen` のあとに追加したルールは、それより前のルールを満たす場合にだけ検査されます。

```java
ObjectInputValidator<PasswordForm> passwordFormValidator = Yavi.schema()
        ._string("currentPassword", c -> c.notBlank())
        ._string("newPassword", c -> c.greaterThanOrEqual(8))
        ._string("confirmPassword", c -> c.notBlank())
        .fromMap(PasswordForm::new)
        .constraintOnTarget(form -> form.newPassword().equals(form.confirm()), "confirmPassword",
                ViolationMessage.of("password.mismatch", "\"{0}\" must match \"newPassword\""))
        .constraintOnTarget(form -> !form.current().equals(form.newPassword()), "newPassword",
                ViolationMessage.of("password.same", "\"{0}\" must differ from \"currentPassword\""));

passwordFormValidator.validate(Map.of("currentPassword", "newSecret1", "newPassword", "newSecret1",
        "confirmPassword", "different"));
// Invalid: confirmPassword (password.mismatch), newPassword (password.same)
```

* `constraintOnTarget` をオーバーライドして自身の型を返す独自の `ValueValidator` は、ルールを `TargetConstraints` に保持します。`and` でルールを追加し、`check` で値が違反したルールをすべて報告します。
* `flatApply(f)` は、検証を行うファクトリメソッドのような、`Validated` を返す関数を受け取ります。

### プロパティを値オブジェクトに変換する

プロパティの直後の `map(f)` は、検証済みの値を値オブジェクトなどに変換します。ほかの修飾メソッドと同じ位置に書けるので、プロパティをその場で定義したまま変換できます。

```java
ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email()).map(Email::new)
        ._string("nickname").map(Nickname::new).optional()   // Optional<Nickname>
        ._integer("age", c -> c.greaterThanOrEqual(0)).map(Age::new).defaultValue(new Age(20))
        .fromMap(User::new);
```

* 関数には制約を満たした値が渡されます。プロパティの読み取り、違反の報告、記述は変わりません。キー、`object.type` の型、ラベル、JSON Schema は `map` の前の値のものです。
* `map` は、`null` の扱いを決める修飾メソッドより前に書きます。それらは変換後の値に適用されます。関数に `null` は渡されず、`optional()` の結果は `Optional<Nickname>` になり、デフォルト値は変換後の値で指定します。`.optional().map(f)` や `.defaultValue(v).map(f)` はコンパイルできません。
* `label`、`typeMessage`、`withheld()` は `map` の前後どちらにも書けます。リストをテキストから読む指定は `map` より前に書きます（`.splitBy(",").map(Set::copyOf)`）。
* `Yavi.reversibleSchema()` では、書き戻すための逆変換を取る `map(f, g)` になります（`._string("email").map(Email::new, Email::value)`）。`map(f)` だけではコンパイルできません。
* 別の場所で定義したバリデータは、これまでどおりそのまま渡します（`._string(Email.VALIDATOR)`、`._object("email", emailValidator)`）。

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

1 つのパラメータが複数の値を持つことはよくあります。`?tags=java,yavi`、`?ids=1,2,3`、`textarea` に 1 行ずつ書いた URI などです。`_list` の後に `splitBy` を書くと、このようなテキストをリストとして読みます。

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
* `splitBy` は `_list` の後、ほかの修飾メソッドの前に書きます。`splitBy` より前に書けるのは `label` と `typeMessage` だけです。
* 書き戻すスキーマ（`Yavi.reversibleSchema()`）では、パターンと一緒に要素を連結するテキストを渡します（`splitBy(pattern, "\n")`）。リストを 1 つのテキストとして書くためです（[1 つのテキストフィールドに入ったリスト](#オブジェクトを読み取り元のマップに書き戻す)）。
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
* `label` と `typeMessage` はプロパティを追加した後、`optional()` などの前に書きます。`required(...)` は最後の修飾子で、`.required("...").optional()` はコンパイルできません。
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

Spring を使わない場合は、`ResourceBundleMessageFormatter` がクラスパス上のリソースバンドルからキーでメッセージを探します。

```java
// messages_ja.properties: numeric.lessThanOrEqual="{0}" は {1} 以下で入力してください
MessageFormatter formatter = new ResourceBundleMessageFormatter("messages");
validator.formattingWith(formatter).validate(input, Locale.JAPANESE);
```

* バンドルにないキーはデフォルトのメッセージで整形されます。バンドルには置き換えるメッセージだけを書きます。
* バンドルは検証のロケールのもの、次に基底のバンドルの順に探します。`ResourceBundle.getBundle` と違い、プラットフォームのデフォルトロケールのバンドルにはフォールバックしません。日本語のサーバーで英語のロケールで検証しても、日本語のメッセージは返りません。

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

### パラメータを書き戻す

不正なフォームは、送信されたテキストを入れて再表示します。`ParameterTree` は、パラメータをバリデータが読む形で返します。返すのは、バリデータが記述するプロパティと、それに送信された値です。形はスキーマのとおりです。アプリケーションがフィールドを 1 つずつ写す必要はありません。

```java
record Signup(String name, Integer age, List<String> tags, Address address) {}

ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._integer("age")
        ._list("tags", Yavi._string()).defaultValue(List.of())
        ._object("address", addressValidator)
        .fromMap(Signup::new)
        .readingParameters(StringCoercion.strict().trimming());

// name=+Alice+&age=abc&tags=a&address[city]=Tokyo&utm_source=mail
// readingParameters() が検証するマップ。ここでは Servlet API のもの。
// Spring では @RequestParam MultiValueMap<String, String> params
Map<String, String[]> params = request.getParameterMap();
ParameterTree tree = ParameterTree.of(params);
Map<String, Object> form = tree.toMap(signupValidator.description());
// {name=" Alice ", age="abc", tags=[a], address={city="Tokyo"}}
Map<String, Object> flat = tree.toParameters(signupValidator.description());
// {name=" Alice ", age="abc", tags=[a], address.city="Tokyo"}

// 何も送信されなかった場合
ParameterTree.of(Map.of()).toMap(signupValidator.description());
// {name="", age="", tags=[], address={city=""}}
```

* パラメータは `Map<String, ?>` です。`readingParameters()` が検証するどの形でも渡せます。`Map<String, String>`、Spring の `MultiValueMap` のような `Map<String, List<String>>`、Servlet API の `Map<String, String[]>` です。JSON ライブラリのツリー（`readingWith(reader)`）は書き戻せません。
* 値は送信されたテキストです。トリムも、型の変換も、検証もされません。
* `toMap` は `Map`、`List` と値からなるツリーを返します。Mustache のようなテンプレートエンジンに渡せます。`toParameters` は同じ値を違反の名前（`address.city`、`items[0].name`）をキーにして返します。Spring の `MapBindingResult` に渡せます。
* リストは、値が 1 つでも `List` です。`splitBy` を付けたリストが 1 つのテキストで送信された場合は、そのテキストのままです。テキストフィールドにそのまま表示できます。
* プロパティが定義されていないキー（`utm_source`）は返しません。`_rest` を持つスキーマは残りのキーも読むので、それらも返します。
* エントリはどのリクエストでも同じです。送信されなかった単一の値は空文字列です。空のテキストフィールドが送信する値と同じです。リストとマップは空になり、オブジェクトはそのプロパティを持ちます。プロパティの形に合わない値も同じように返します。`age` に 2 つの値が送信された場合がその例で、違反として報告されます。JMustache のように存在しないキーで失敗するテンプレートも、パラメータの欠けたリクエストで失敗しません。同じ呼び出しで空のフォームも得られます。
* 送信されなかった `Boolean` にはエントリがありません。チェックされていないチェックボックスは何も送信しません。また、空文字列を `false` として扱わないテンプレートエンジンがあります。`presence()` を付けたプロパティと、自身を含む構造にもエントリがありません。
* 返されるマップとリストは新しく作られたものです。ページに必要な値は `form.put("id", id)` のように足せます。
* 判別共用体では、すべての選択肢のプロパティを返します。
* 記述は、そのパラメータを検証するバリデータのものを渡します。バリデータが既定と異なる最大インデックスのツリーを読む場合は、`ParameterTree.of(params, maxIndex)` に同じ値を渡します。
* `withheld()` を付けて定義したプロパティ（パスワードなら `._string("password", c -> c.greaterThanOrEqual(8)).withheld()`）は、送信されなかったプロパティとして書かれ、空文字列になります。マップのすべてのフィールドを書くテンプレートでも、パスワードを送り返しません。それ以外のプロパティは、送信された値を持ちます。

`ParameterTree.parametersOf(map)` は `ParameterTree.of(parameters)` の逆です。`Map` と `List` からなる任意のツリーに使えます。

```java
ParameterTree.parametersOf(Map.of("address", Map.of("city", "Tokyo"), "tags", List.of("a", "b")));
// {address.city=Tokyo, tags=[a, b]}
```

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
    void signupForm(@RequestParam MultiValueMap<String, String> params, Model model) {
        Map<String, Object> form = ParameterTree.of(params).toParameters(signupValidator.description());
        model.addAllAttributes(new MapBindingResult(form, "signupForm").getModel());
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
* フィールドの名前は、`th:field` が出力する形（`address.city`、`items[0].name`）にします。この形が違反の名前であり、`MapBindingResult` が値を引くキーでもあるからです。`toParameters` はこの形のキーを返すので、`address[city]` の形で送信された値も再表示されます。
* ハンドラは `MultiValueMap` を検証します。そのため、1 つのキーに複数の値があれば、リストとして読まれるか、スカラーのプロパティでは `object.type` で報告されます。`Map<String, String>` で受けると、最初の値以外は捨てられます。
* `toParameters` は、単一の値にはテキストを、リストのプロパティにはリストを返します。そのため、テキストフィールドには `30` が表示され、チェックされていたチェックボックス（`tags=a&tags=b`）はすべて再びチェックされます。要素の違反は `tags[1]` に付くので、`#fields.hasErrors('tags*')` で調べます。

## オブジェクトを読み取り元のマップに書き戻す

スキーマには、入力のプロパティ名を一度だけ書きます。`Yavi.reversibleSchema()` の `fromMapReversible` で作ったバリデータは、作成したオブジェクトを読み取り元のマップに書き戻すこともできます。SQL 文のパラメータや、保存済みのオブジェクトを表示するフォームのために、プロパティ名をもう一度書く必要はありません。

```java
record Email(String value) {}
record UserName(String first, String last) {}
record Employee(Integer id, UserName name, Email email, Optional<String> nickname, Role role) {}

ReversibleValueValidator<String, Email> emailValidator = Yavi._string("email", c -> c.notBlank().email())
        .map(Email::new, Email::value);

ObjectInputValidator.Reversible<UserName> userNameValidator = Yavi.reversibleSchema()
        ._string("first_name", c -> c.notBlank())
        ._string("last_name", c -> c.notBlank())
        .fromMapReversible(UserName::new, UserName::first, UserName::last);

ObjectInputValidator.Reversible<Employee> employeeValidator = Yavi.reversibleSchema()
        ._integer("id")
        ._embedded(userNameValidator)
        ._string(emailValidator)
        ._string("nickname").optional()
        ._enum("role", Role.class)
        .fromMapReversible(Employee::new, Employee::id, Employee::name, Employee::email,
                Employee::nickname, Employee::role);

employeeValidator.validate(row); // fromMap と同じように読む
employeeValidator.toMap(employee);
// {id=1, first_name=Taro, last_name=Yamada, email=taro@example.com, nickname=null, role=ADMIN}

jdbcClient.sql("INSERT INTO employee (id, first_name, last_name, email, nickname, role) "
        + "VALUES (:id, :first_name, :last_name, :email, :nickname, :role)")
        .params(employeeValidator.toMap(employee))
        .update();
```

* `Yavi.reversibleSchema()` は、書き戻すオブジェクトのための `Yavi.schema()` です。メソッドは同じですが、`Yavi.schema()` が `ValueValidator` を受け取る箇所で `ReversibleValueValidator` を受け取ります。`ReversibleValueValidator` は、逆変換を知っていることが型で分かるバリデータです。もう 1 つの違いはパターンの `splitBy` で、要素を連結するテキストも渡します。
* `fromMapReversible(f, accessor1, ..., accessorN)` は、`fromMap(f)` に引数ごとのアクセサを加えたものです。アクセサは、その引数の検証済みの値をオブジェクトから返します。型はコンパイラが検査するので、レコードならコンストラクタとアクセサのメソッド参照を渡せます。`flatFromMap` には `flatFromMapReversible` が対応します。
* プロパティが 1 つのスキーマには、引数なしの `fromMap()` と `fromMapReversible()` があります。`get()` と同じく、検証済みの値がそのまま結果になります。1 つのプロパティを制約や修飾子ごと一度だけ定義して、複数のスキーマに埋め込めます（`_embedded(passwordSchema)`、`_embedded(passwordSchema).optional()`）。`_rest` だけのスキーマにも同じ終端があります。`Yavi.schema()._rest(Yavi._integer()).fromMap()` は `Map<String, Integer>` のバリデータです。
* `toMap(x)` は往復で定義されます。バリデータが作るすべての `x` について、`validator.validate(validator.toMap(x))` は `Valid(x)` です。`withheld()` を付けて定義したプロパティは `toMap` が書きません。`reverse(x)` はそれも書くので、往復が成り立ちます（[一部のプロパティだけを書く](#一部のプロパティだけを書く)）。
* 返るバリデータは `ObjectInputValidator` です。これまでどおりネストでき、埋め込めて、`readingWith` や `readingParameters` で読めます。`rejectingUnknownProperties()` や `constraintOnTarget` のあとも `Reversible` のままです。
* マップは新しく作られます。SQL 文に必要な値は `map.put("updated_by", user)` のように足せます。

| プロパティ | `toMap` |
| --- | --- |
| 制約の値、`defaultValue`、`recover` | その値 |
| enum の定数 | 定数の名前 |
| `nullable()`、`optional()` で値がない | `null` のエントリ(名前付きパラメータにはキーが必要なため) |
| `rejectingNull()` で値がない | エントリなし |
| `presence()` | `Absent` はエントリなし、`PresentNull` は `null` |
| `_embedded` | 同じマップに書く。値がなければ全プロパティが `null` |
| `_object` | ネストしたマップ |
| `_list`、`_map`、`_rest` | 各要素 |
| `splitBy(",")` | 連結したテキスト。空のリストは `null`(要素のないテキストは送信されないため)。読み戻すにはプロパティに `defaultValue(List.of())` か `nullable()` が必要 |
| `splitBy(pattern, "\n")` | 第 2 引数で連結したテキスト。空のリストの扱いは同じ |
| `map(f, g)` のバリデータ | `g` の結果 |

値は、プロパティの読み取り元である JDK の型の値で、JDBC ドライバや JSON ライブラリにそのまま渡せます。`URI` や `Year` などもそのまま書くので、ドライバが受け取れない型は変換してください。

**1 つのテキストフィールドに入ったリスト。** `splitBy` を付けたリストは、1 行に 1 つずつ値を書く `textarea` のように、フォームの 1 つのフィールドです。そのため、そのフィールドのテキストとして書きます。パターンからは区切りのテキストが分からないので、要素を連結するテキストを一緒に渡します。

```java
ObjectInputValidator.Reversible<Feed> feedSchema = Yavi.reversibleSchema()
        ._string("title")
        ._list("urls", Yavi._string(c -> c.url())).splitBy(Pattern.compile("\\s*\\R\\s*"), "\n")
        .defaultValue(List.of())
        .fromMapReversible(Feed::new, Feed::title, Feed::urls);

feedSchema.toMap(new Feed("News", List.of("https://a.example", "https://b.example")));
// {title=News, urls="https://a.example\nhttps://b.example"}
```

* `Yavi.reversibleSchema()` では、テキストを渡さない `splitBy(pattern)` はコンパイルエラーになります。1 つのテキストから読んだリストを `List` として書くことになり、`textarea` に `[https://a.example, https://b.example]` と表示されるためです。`Yavi.schema()` には、これまでどおりあります。
* パターンは、連結するテキストにマッチしなければなりません。`splitBy(Pattern.compile("\\R"), ", ")` は、スキーマを作る時点で `IllegalArgumentException` になります。書いたテキストが、連結した位置で分割されないためです。`splitBy(",")` は `splitBy(Pattern.compile(","), ",")` と同じです。
* パターンにマッチする部分を含む要素は、1 つの要素としては読み戻せません。

**値オブジェクト。** `map(f, g)` は、`map(f)` に、変換後の値から入力へ戻す関数を加えたものです。返り値は `ReversibleValueValidator` で、`reverse(x)` は `x` の読み取り元の入力を返します。バリデータが逆変換を知っているかどうかは、型で分かります。

| バリデータ | 型 |
| --- | --- |
| `Yavi._string("name")` などの単一の値のバリデータ、`XxxValidatorBuilder#build()` | `StringValidator.Reversible` など(`ReversibleValueValidator` を実装) |
| `map(f, g)`、`ReversibleValueValidator.of(validator, g)` | `ReversibleValueValidator` |
| 上記に対する `named`、`describedAs`、`preprocess`、`prefixed`、`formattingWith`、`constraintOnTarget`、`untrimmed()`、`ignoringCase()` | `ReversibleValueValidator` |
| 上記に対する `optional()`、`nullable()`、`defaultValue`、`recover`、`recoverWith` | `ReversibleValueValidator.NullAccepting`(`NullAcceptingValueValidator` でもある) |
| 上記に対する `liftList()`、`liftSet()`、`liftCollection(factory)`、`liftMap()`、`liftOptional()`、`liftPresence()` | コンテナの `ReversibleValueValidator` |
| `ReversibleValueValidator.lazy(supplier)`、`recursive(self -> ...)` | `ReversibleValueValidator` |
| `fromMapReversible`、`DiscriminatedUnionValidator.Reversible` | マップの `ReversibleValueValidator` |
| `map(f)`、`andThen`、`anyOf`、ラムダ、`Yavi._list`、`Yavi._map`、`fromMap` | `ValueValidator` |

定数はこれらの型で宣言します。`StringValidator.Reversible` などの単一の値のバリデータには型引数がありません。検証済みの値が入力そのものだからです（これまでの `StringValidator<String>` に当たります）。`EnumValidator.Reversible<E>`、`ObjectValidator.Reversible<T>`、`ObjectInputValidator.Reversible<T>` は値の型を型引数に取ります。

```java
static final StringValidator.Reversible NAME = Yavi._string("name", c -> c.notBlank());
static final IntegerValidator.Reversible AGE = Yavi._integer("age", c -> c.positive());
static final EnumValidator.Reversible<Role> ROLE = Yavi._enum("role", Role.class);
static final ReversibleValueValidator<String, Email> EMAIL = Yavi._string("email").map(Email::new, Email::value);
```

**書き戻せないもの。** `ReversibleValueValidator` ではないバリデータ、つまり逆変換のない `map(f)`、ラムダ、`andThen`、`anyOf` は書き戻せません。`Yavi.reversibleSchema()` のスキーマは、プロパティとしても、`_object`、`_embedded`、`_list`、`_map`、`_rest` の引数としても、これらを受け取りません。書き戻せないプロパティは、追加した箇所でコンパイルエラーになります。

```java
Yavi.reversibleSchema()
        ._string("name")
        ._string(Yavi._string("email").map(Email::new)) // コンパイルエラー: 逆変換がない
        .fromMapReversible(Contact::new, Contact::name, Contact::email);
// error: no suitable method found for _string(StringValidator<Email>)

Yavi.reversibleSchema()
        ._string("name")
        ._string(Yavi._string("email").map(Email::new, Email::value)) // コンパイルできる
        .fromMapReversible(Contact::new, Contact::name, Contact::email);
```

その場で変換するプロパティにも、同じように逆変換を渡します。`._string("email").map(Email::new, Email::value)` はコンパイルでき、`._string("email").map(Email::new)` はコンパイルできません。

`Yavi.schema()` に `fromMapReversible` はありません。書き戻さないスキーマは影響を受けず、これまでどおりどのバリデータも受け取ります。

**自分自身を含む構造。** `ReversibleValueValidator.recursive(self -> ...)` は、逆変換を知っている参照を渡す `ValueValidator.recursive` です。

```java
ObjectInputValidator.Reversible<Tree> treeValidator = ReversibleValueValidator.recursive(self -> Yavi.reversibleSchema()
        ._string("name")
        ._list("children", self).defaultValue(List.of())
        .fromMapReversible(Tree::new, Tree::name, Tree::children));
```

**ネストしたオブジェクト。** ネストしたマップは SQL 文にバインドできません。行のカラムは `_embedded` で読むか、`ParameterTree.parametersOf` でマップを違反の名前のキーに展開してバインドします。

```java
// {id=1, address={city=Tokyo, street=null}} -> {id=1, address.city=Tokyo, address.street=null}
jdbcClient.sql("INSERT INTO venue (id, city, street) VALUES (:id, :address.city, :address.street)")
        .params(ParameterTree.parametersOf(venueValidator.toMap(venue)))
        .update();
```

**行。** `listOfRows()` が返す行は、同じバリデータで読めます。ドライバが日付や時刻のカラムに返す `java.sql.Date` は `LocalDate` として、`java.sql.Time` は `LocalTime` として、`java.sql.Timestamp` は `LocalDateTime` または `Instant` として読みます。

ドライバが別の型で保持するカラムには、`map(f, g)` で両方向の変換を与えます。SQLite のドライバは `java.sql.Timestamp` をエポックミリ秒で保存し、`Long` として返します。`_instant("created_at")` はこの行を `object.type` で拒否します。また、書いた `Instant` をこのドライバはテキストで保存するので、テーブルに 2 つの形式が混ざります。同じバリデータで行を登録するテストでは、この問題に気づけません。`Long` として読み書きします。

```java
._long(Yavi._long("created_at").map(Instant::ofEpochMilli, Instant::toEpochMilli))
```

**判別可能なユニオン。** オブジェクトの型を付けて追加した選択肢は、判別子とともに書き戻せます。すべての選択肢をこの方法で追加したユニオンが `Reversible` です。

```java
DiscriminatedUnionValidator.Reversible<Shape> shapeValidator = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Circle.class, circleValidator)
        .when("square", Square.class, squareValidator);
shapeValidator.toMap(new Circle(1.5)); // {type=circle, radius=1.5}
```

**実行時にプロパティが決まるスキーマ。** `Yavi.reversibleDynamicSchema()` は、`property` と `rest` が `ReversibleValueValidator` を受け取る `Yavi.dynamicSchema()` です。`build()` は、検証済みの値のマップを書き戻すバリデータを返します。

**スキーマに収まらない数のプロパティ。** `ObjectInputValidator.ofReversible` は、`ObjectInputValidator.ReversiblePart` のリストを受け取ります。`ReversiblePart` は、パートに逆変換とアクセサを組にしたものです。

### 一部のプロパティだけを書く

`toMap` は、`withheld()` でないすべてのプロパティを書きます。しかし、一覧画面に出すのは一部だけですし、`UPDATE` 文にバインドするのも変更できるカラムだけです。バリデータの `including` と `excluding` は、選んだプロパティだけを書く `PropertyView` を作ります。

```java
ObjectInputValidator.Reversible<User> userValidator = Yavi.reversibleSchema()
        ._integer("id")
        ._string("name")
        ._string("password_hash").withheld()
        ._object("address", addressValidator) // city, street
        .fromMapReversible(User::new, User::id, User::name, User::passwordHash, User::address);

// バリデータの隣で一度だけ作る
static final PropertyView<User> SUMMARY = userValidator.including("id", "name", "address.city");
static final PropertyView<User> LISTED = userValidator.excluding("address");

userValidator.toMap(user); // {id=1, name=Taro, address={city=Tokyo, street=1-1}}
SUMMARY.toMap(user);       // {id=1, name=Taro, address={city=Tokyo}}
LISTED.toMap(user);        // {id=1, name=Taro}

userValidator.excluding("adress");
// IllegalArgumentException: The property 'adress' is not defined by the validator.
// The properties are [id, name, password_hash, address].
```

* **名前はビューを作るときに検査します。** 検査の基準はバリデータの記述（`description()`）で、オブジェクトを書くときには検査しません。バリデータが定義していない名前、ビューが書かない名前、重複した名前は `IllegalArgumentException` になります。ビューをバリデータの隣の定数にしておけば、名前の誤りはクラスのロード時に失敗するので、そのバリデータを使うすべての経路とテストで見つかります。`toMap` が返したマップからキーを削除する方法（`map.remove("adress")`）では、誰も名前を検査しません。
* **ビューはオブジェクトを書く場所で選びます。** ユースケースが必要なビューを持ち、その `toMap` を呼びます。スキーマはビューを知りません。
* **ビューは書くだけです。** バリデータでも `ReversibleValueValidator` でもありません。必須のプロパティを欠いたマップは読み戻せないからです。`Yavi.reversibleSchema()` のプロパティのように往復が必要な箇所にビューを渡すと、コンパイルエラーになります。バリデータが読む内容は変わりません。
* ビューは、ビュー自身の `including` と `excluding` でさらに絞り込めます。例: `userValidator.including("id", "address").excluding("address.street")`

プロパティは、違反の名前と同じ形式で指定します。

| プロパティ | 名前 |
| --- | --- |
| バリデータ自身のプロパティ | `name` |
| ネストしたオブジェクト（`_object`）のプロパティ | `address.city`。`address` はそのすべてのプロパティを表す |
| `_list` の要素と `_map` の値のプロパティ | `addresses.city`。インデックスやキーは書かない |
| `_embedded` のバリデータのプロパティ | そのままの名前（同じマップに書かれるため） |
| ユニオンのプロパティ | いずれかの選択肢が定義している名前。定義していない選択肢は、そのプロパティなしで書かれる |

書かれる内容は次のとおりです。

* ビューに含まれないプロパティは、**エントリ自体がありません**。`null` として書かれるわけではありません。ビューはパラメータの少ない SQL 文のためのもので、`null` をバインドする手段ではありません。
* バリデータ自身のプロパティのうち、書かないもののアクセサは呼びません。取得にコストがかかる値をビューから外せます。
* 残りのキー（`_rest`）は、ビューを作る時点では名前が分かりません。`excluding` のビューは書き、`including` のビューは書きません。
* ユニオンの判別子は、ユニオンのビューでも、ネストしたユニオンでも常に書かれます。`including` にも `excluding` にも指定できません。`_embedded` のユニオンの判別子は、埋め込み先のオブジェクトのプロパティなので、ほかのプロパティと同じように指定します。
* パスは、その深さのプロパティだけを指します。自分自身を含むバリデータの `children.memo` は子の `memo` で、孫の `memo` ではありません。
* `Yavi.reversibleDynamicSchema()` と `DiscriminatedUnionValidator.Reversible` でも同じ方法でビューを作れます。`ParameterTree.ofValues(view.toMap(x))` とすれば、保存済みのオブジェクトを秘密の値なしでフォームに表示できます。

**書き出しを保留するプロパティ。** 書いたマップは、レスポンス、フォーム、ログなどで人の目に触れます。パスワードのように見せてはいけないプロパティを呼び出し側が毎回外すことにすると、そのスキーマをネストしたバリデータの呼び出し側まで、全員が外し忘れないようにしなければなりません。そこで、定義する場所で一度だけ `withheld()` を付けます。

```java
Yavi.reversibleSchema()
        ._integer("id")
        ._string("name")
        ._string("password_hash").withheld()
        .fromMapReversible(User::new, User::id, User::name, User::passwordHash);

userValidator.toMap(user);                                  // {id=1, name=Taro}
userValidator.excluding("name").toMap(user);                // {id=1}
userValidator.including("id", "password_hash").toMap(user); // {id=1, password_hash=...}
userValidator.reverse(user);                                // {id=1, name=Taro, password_hash=...}
orderValidator.toMap(order);                                // {id=7, memo=gift, user={id=1, name=Taro}}
orderValidator.including("user", "user.password_hash");     // user 全体と、そのハッシュも書く
userValidator.excluding("password_hash");                   // IllegalArgumentException: 効果がない
```

* `toMap` と `excluding` のビューは、`withheld()` のプロパティをどの深さでも書きません。ネストしたオブジェクト、リストの要素、`_embedded` のバリデータのプロパティも同じです。
* `reverse` はすべてのプロパティを書くので、何を `withheld()` にしても `validate(reverse(x))` は `Valid(x)` です。そのスキーマをネストしたバリデータは `reverse` で書き戻されます。秘密の値を保存した行を `INSERT` するときにバインドするのも `reverse` のマップです。
* `including` のビューは、名前を指定したときだけ書きます。ネストしたオブジェクトの名前は、`withheld()` でないプロパティだけを表します。`withheld()` のプロパティも書くには、そのパスを並べて指定します。
* ユニオンのプロパティは、それを定義する選択肢のどれかが `withheld()` にしていれば書きません。判別子は `withheld()` にできません。
* `withheld()` は、`null` の扱いを決める修飾メソッドの前（リストでは `splitBy` の後）に一度だけ書きます。`label` と `typeMessage` は、その前にも後にも書けます（`.label("Password").withheld()`）。`.withheld().withheld()`、`.optional().withheld()`、`_rest` や `_embedded` への `withheld()` はコンパイルエラーになります。`Yavi.schema()` と動的スキーマにもあります。
* 同じ印によって、再表示するフォームには送信された値が入らず（[パラメータを書き戻す](#パラメータを書き戻す)）、JSON Schema では `writeOnly` になります。読み取る内容は変わりません。違反は拒否した値を持ったままです（`ConstraintViolation#violatedValue()`。`ViolationDetail` は値を持ちません）。
* 名前は理由ではなく動作を表しています。秘密の値に限らず、大きな値や内部用の値にも同じように使えます。

**別の形で保存する値。** パスワードは送信されたまま読み取り、ハッシュにして保存します。ハッシュはアプリケーションが計算する値で、元の入力に戻る値ではないので、バリデータが書く内容には含まれません。フォームのスキーマでパスワードを `withheld()` にしておけば、`toMap` はパスワードを書きません。ハッシュは SQL 文のパラメータとして別に渡します。

```java
// ._string("password", c -> c.greaterThanOrEqual(8)).withheld()
jdbcClient.sql("INSERT INTO users (id, name, password_hash) VALUES (:id, :name, :password_hash)")
        .params(signupValidator.toMap(signup)) // {id=1, name=Taro, address={...}}: password は含まれない
        .param("password_hash", passwordEncoder.encode(signup.password()))
        .update();
```

フォームと行に共通するプロパティは、1 つのバリデータとして一度だけ定義し、両方のスキーマに埋め込みます（`_embedded(profileValidator)`）。保存済みのユーザーをフォームに表示するには `ParameterTree.ofValues(userValidator.toMap(user)).toMap(editValidator.description())` とします。パスワードのフィールドは空になります。全体の流れは[付録](#付録-登録フォームとその行と編集フォーム)にあります。

名前を 1 か所にまとめるには、スキーマとビューで定数を共有します（`._object(ADDRESS, addressValidator)` と `excluding(ADDRESS)`）。

### 保存済みのオブジェクトをフォームに表示する

`ParameterTree.ofValues` は、バリデータが書いたマップを受け取り、その値をパラメータのテキストとして保持します。保存済みのオブジェクトの最初の表示と、送信後の再表示を同じコードで扱えます。

```java
ObjectInputValidator.Reversible<Signup> signupSchema = Yavi.reversibleSchema()
        ._string("name", c -> c.notBlank())
        ._integer("age")
        ._boolean("agreed").defaultValue(false)
        ._localDate("birthday")
        ._object("address", addressValidator)
        .fromMapReversible(Signup::new, Signup::name, Signup::age, Signup::agreed, Signup::birthday,
                Signup::address);
ValueValidator<Map<String, ?>, Signup> signupValidator = signupSchema
        .readingParameters(StringCoercion.strict().trimming());

ParameterTree tree = stored != null ? ParameterTree.ofValues(signupSchema.toMap(stored))
        : ParameterTree.of(request.getParameterMap());
Map<String, Object> form = tree.toMap(signupSchema.description());
// {name="Taro", age="30", birthday="2000-01-02", address={city="Tokyo", street=""}}
```

* 値は、その値として読まれるテキスト(`StringCoercion.textOf`)になります。`30` は `"30"`、`LocalDate` は ISO-8601 のテキストです。`signupValidator` は `tree.toParameters(...)` を保存済みのオブジェクトとして読みます。
* `null` または `false` のプロパティは、空のフィールドやチェックされていないチェックボックスと同じく、送信されなかったものとして扱います。`toMap(description)` は、送信されなかったプロパティと同じように、空文字列を書くか、`Boolean` ならエントリを書きません。
* パラメータを読むバリデータは `Reversible` ではありません。書き戻す先がマップだからです。フォームは、上の例のようにスキーマとパラメータのバリデータの両方を持ちます。
* `splitBy` を付けたリストはテキストとして書かれるので、`textarea` は保存済みのリストを、送信された値と同じ形で表示します。
* 新規のフォームの初期値は、一部のプロパティだけを持つマップで渡します。それ以外は、送信されなかったプロパティとして書かれます。

  ```java
  ParameterTree.ofValues(Map.of("age", 20, "agreed", true)).toMap(signupSchema.description());
  // {name="", age="20", agreed="true", birthday="", address={city="", street=""}}
  ```

**フォームの型が保存する型と違う場合。** フォームは `ArticleInput(title, body)` として読み、保存するオブジェクトは `Article(id, title, body, createdAt)` やライブラリのクラスである、という場合です。フォームのスキーマが書くのは `ArticleInput` なので、アプリケーションが保存済みのオブジェクトから `ArticleInput` を作ります。

```java
ParameterTree.ofValues(articleInputSchema.toMap(new ArticleInput(article.title(), article.body())))
        .toMap(articleInputSchema.description());
```

スキーマが肩代わりするのは、それぞれの値のテキスト化、`null`、チェックボックスの扱いです。2 つの型の間の対応づけは含みません。保存する型がフォームと共通の部分をオブジェクトとして持てるなら、両方のスキーマに埋め込むプロパティとして定義することで、この対応づけも書かずに済みます（[付録](#付録-登録フォームとその行と編集フォーム)の `_embedded(profileSchema)`）。

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

失敗した結果と違反は、ログや jshell やアサーションの失敗メッセージで、読める形で出力されます。ポインタ、メッセージ、メッセージキーの順です。

```java
userValidator.validate(Map.of("email", "invalid", "age", 300)).toString();
// Invalid[errors=[/email: "email" must be a valid email address (charSequence.email),
//                 /age: "age" must be less than or equal to 150 (numeric.lessThanOrEqual)]]
Yavi._integer(c -> c.positive()).validate(-1).toString();
// Invalid[errors=[The value must be positive (numeric.positive)]]
```

違反した値は、メッセージが表示する場合を除いて出力されません。

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
| `collection.unique`, `collection.uniqueBy` | `duplicates` |
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

ドメインのファクトリメソッドのように、バリデータではない関数（`flatFromMap` や `flatApply` に渡す関数）には、違反した値も検証のロケールも渡されません。そのような関数はどちらも指定せずに失敗を返せます。違反は、バリデータが呼び出されたときのロケールで報告されます。

```java
record DateRange(LocalDate start, LocalDate end) {

    static Validated<DateRange> of(LocalDate start, LocalDate end) {
        return start.isBefore(end) ? Validated.successWith(new DateRange(start, end))
                // before: Validated.failureWith(message, start, Locale.getDefault())
                : Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"));
    }
}

ObjectInputValidator<DateRange> validator = Yavi.schema()
    ._localDate("start")
    ._localDate("end")
    .flatFromMap(DateRange::of);

validator.validate(input, Locale.JAPANESE).errors().get(0).locale(); // ja
```

`Validated.failureWith(message, args...)` と `ConstraintViolation.of(message, args...)` は、違反した値を持たない `Locale.getDefault()` の違反を作ります。`flatFromMap`、`flatApply`、`andThen` は、渡された関数やバリデータの違反を、作られたときのロケールにかかわらず検証のロケールに付け替えます。そのため、関数が `validator.validate(value)` の結果をそのまま返す場合も同じロケールになります。それ以外の場所では、`Validated`、`ConstraintViolations`、`ConstraintViolation` の `localizedWith(locale)` で同じことができます。フォーマッタに対する `formattedWith(messageFormatter)` と対になるメソッドです。

```java
ValueValidator<String, DateRange> dateRange = (s, locale, context) -> DateRange.parse(s).localizedWith(locale);
violations.localizedWith(Locale.JAPANESE).messagesByName();
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
* `withheld()` を付けて定義したプロパティは `writeOnly` になります（`ValueDescription.Property#withheld()`）。
* リストやマップの制約は `minItems` や `uniqueItems` などとして、`rejectingUnknownProperties()` は `additionalProperties: false` として、再帰的な構造は `$ref` として出力されます。
* `DiscriminatedUnionValidator` の選択肢は `AnyOf` として記述され、その `discriminator()` は選択肢を決めるプロパティの名前を返します。
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
| `ParameterTree#toMap`、`toParameters`、`parametersOf` | [#577](https://github.com/making/yavi/pull/577) |
| `fromMapReversible`、`toMap`、`map(f, g)`、`ReversibleValueValidator`、`ParameterTree.ofValues`、行の `java.sql` の日付 | [#578](https://github.com/making/yavi/pull/578) |
| `Yavi.reversibleSchema()`、`Yavi.reversibleDynamicSchema()`、`StringValidator.Reversible` など、`ReversibleValueValidator.NullAccepting`、`ReversibleArguments1Validator`、`ObjectInputValidator.ReversiblePart` | [#579](https://github.com/making/yavi/pull/579) |
| `PropertyView`、リバーシブルなバリデータの `including` と `excluding`、`ValueDescription.AnyOf#discriminator` | [#580](https://github.com/making/yavi/pull/580) |
| プロパティの `withheld()`、`ValueDescription.Property#withheld`、1 つのプロパティの `fromMap()` と `fromMapReversible()` | [#581](https://github.com/making/yavi/pull/581)、[#582](https://github.com/making/yavi/pull/582) |
| プロパティの空のテキストに対する `object.required`、`InputReader#isEmptyText` | [#570](https://github.com/making/yavi/pull/570) |
| `named`、`splitBy`、`liftList(separator)`、`Yavi.dynamicSchema()` | [#572](https://github.com/making/yavi/pull/572) |
| 書き戻すスキーマの `splitBy(pattern, delimiter)`、`withheld()` や `splitBy` の前の `label` と `typeMessage` | [#583](https://github.com/making/yavi/pull/583) |
| コレクションの `unique(key)`、`ResourceBundleMessageFormatter`、違反の `toString()` | [#584](https://github.com/making/yavi/pull/584) |
| `failureWith(message, args...)`、`localizedWith(locale)`、`flatFromMap`・`flatApply`・`andThen` の違反のロケール | [#585](https://github.com/making/yavi/pull/585) |
| `constraintOnTarget` の違反したルールをすべて報告、`TargetConstraints` | [#586](https://github.com/making/yavi/pull/586) |
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
| スキーマのプロパティの `map` | [#587](https://github.com/making/yavi/pull/587) |
| 効果のない修飾メソッドの連鎖はコンパイルできない、`NullAcceptingValueValidator` | [#563](https://github.com/making/yavi/pull/563)、[#564](https://github.com/making/yavi/pull/564)、[#565](https://github.com/making/yavi/pull/565) |
| JSpecify、`@Nullable` のない値 | [#534](https://github.com/making/yavi/pull/534)、[#556](https://github.com/making/yavi/pull/556)、[#557](https://github.com/making/yavi/pull/557) |

## 付録: 登録フォームとその行と編集フォーム

フォームが送ってくるパスワードは、検査はしますが、再表示はしません。テーブルが持つのはそのハッシュで、ハッシュはバリデータの外で計算します。保存済みのアカウントを編集フォームに表示するときは、パスワードのフィールドを空にします。プロパティは一度だけ定義して、各スキーマに埋め込みます。

```java
record Profile(String name, String email) {}
record Signup(Profile profile, String password) {}
record Account(Integer id, Profile profile, String passwordHash) {}
record AccountEdit(Profile profile, Optional<String> password) {}

// フォームと行に共通するプロパティ。定義はここだけ
static final ObjectInputValidator.Reversible<Profile> profileSchema = Yavi.reversibleSchema()
        ._string("name", c -> c.notBlank().lessThanOrEqual(50))
        ._string("email", c -> c.notBlank().email())
        .fromMapReversible(Profile::new, Profile::name, Profile::email);

// 送信されたままのパスワード。制約と withheld() の定義はここだけ
static final ObjectInputValidator.Reversible<String> passwordSchema = Yavi.reversibleSchema()
        ._string("password", c -> c.greaterThanOrEqual(8)).withheld()
        .fromMapReversible();

// 登録フォーム
static final ObjectInputValidator.Reversible<Signup> signupSchema = Yavi.reversibleSchema()
        ._embedded(profileSchema)
        ._embedded(passwordSchema)
        .fromMapReversible(Signup::new, Signup::profile, Signup::password);

// 行
static final ObjectInputValidator.Reversible<Account> accountSchema = Yavi.reversibleSchema()
        ._integer("id")
        ._embedded(profileSchema)
        ._string("password_hash").withheld()
        .fromMapReversible(Account::new, Account::id, Account::profile, Account::passwordHash);

// 編集フォーム。パスワードが空なら変更しない
static final ObjectInputValidator<AccountEdit> editSchema = Yavi.schema()
        ._embedded(profileSchema)
        ._embedded(passwordSchema).optional()
        .fromMap(AccountEdit::new);

static final ValueValidator<Map<String, ?>, Signup> signupForm = signupSchema.readingParameters();
static final ValueValidator<Map<String, ?>, AccountEdit> editForm = editSchema.readingParameters();
```

**登録する。** 短すぎるパスワードは違反になり、再表示するフォームにはその値が入りません。正しいフォームは `toMap` で書きます。`toMap` はパスワードを書かないので、ハッシュは SQL 文のパラメータとして別に渡します。

```java
Map<String, String> parameters = Map.of("name", "Taro", "email", "taro@example.com", "password", "short");
signupForm.validate(parameters).errors();
// The size of "password" must be greater than or equal to 8. The given size is 5
ParameterTree.of(parameters).toParameters(signupForm.description());
// {name=Taro, email=taro@example.com, password=}

parameters = Map.of("name", "Taro", "email", "taro@example.com", "password", "long-enough");
Signup signup = signupForm.validated(parameters);
signupSchema.toMap(signup); // {name=Taro, email=taro@example.com}
jdbcClient.sql("INSERT INTO account (id, name, email, password_hash) VALUES (:id, :name, :email, :password_hash)")
        .params(signupSchema.toMap(signup))
        .param("id", id)
        .param("password_hash", passwordEncoder.encode(signup.password()))
        .update();
```

**行を読んで表示する。** 行は `Account` として読みます。レスポンス、ログ、フォームに渡すのは `toMap` のマップで、ハッシュを含みません。

```java
Map<String, Object> row = jdbcClient.sql("SELECT * FROM account WHERE id = :id").param("id", id).query().singleRow();
Account account = accountSchema.validated(row);
// Account[id=1, profile=Profile[name=Taro, email=taro@example.com], passwordHash={bcrypt}...]
accountSchema.toMap(account); // {id=1, name=Taro, email=taro@example.com}

// 保存済みのアカウントの編集フォーム
ParameterTree.ofValues(accountSchema.toMap(account)).toParameters(editForm.description());
// {name=Taro, email=taro@example.com, password=}
```

**編集する。** 空のままのパスワードは `Optional.empty()` になるので、プロフィールだけを更新します。パスワードが送信された場合は、登録フォームと同じ制約で検査し、そのハッシュを保存します。

```java
Map<String, String> parameters = Map.of("name", "Jiro", "email", "jiro@example.com", "password", "");
AccountEdit edit = editForm.validated(parameters);
// AccountEdit[profile=Profile[name=Jiro, email=jiro@example.com], password=Optional.empty]
jdbcClient.sql("UPDATE account SET name = :name, email = :email WHERE id = :id")
        .params(profileSchema.toMap(edit.profile()))
        .param("id", account.id())
        .update();
edit.password().ifPresent(password ->
        jdbcClient.sql("UPDATE account SET password_hash = :password_hash WHERE id = :id")
                .param("password_hash", passwordEncoder.encode(password))
                .param("id", account.id())
                .update());
```

**行の全体。** ハッシュが必要な場面では、それを明示します。`reverse` はすべてのプロパティを書き、そのマップは読み戻せます。ビューは、名前を指定した `withheld()` のプロパティを書きます。

```java
accountSchema.reverse(account);
// {id=1, name=Taro, email=taro@example.com, password_hash={bcrypt}...}
accountSchema.validate(accountSchema.reverse(account)); // Valid(account)
accountSchema.including("id", "password_hash").toMap(account);
// {id=1, password_hash={bcrypt}...}
```

* `name` と `email` の定義は `profileSchema` だけに、パスワードの制約と `withheld()` は `passwordSchema` だけにあります。`passwordSchema` はプロパティが 1 つのスキーマで、その値がそのまま結果になります（引数なしの `fromMapReversible()`）。埋め込んだプロパティの印は、`optional()` を付けても引き継がれます。
* ハッシュはパスワードに戻る値ではないので、アプリケーションが渡します（`.param("password_hash", ...)`）。
* 埋め込むオブジェクトをそのまま持つレコード（`Signup(Profile profile, String password)`）には、コンストラクタとアクセサを渡せます。フラットなレコード（`Signup(String name, String email, String password)`）でも書けますが、プロフィールを分解する関数と、組み立て直すアクセサが必要です。
