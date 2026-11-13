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
* `Yavi._list(elementValidator, c -> c.notEmpty().unique())` と `Yavi._map(valueValidator, c -> c.notEmpty())` は、リストやマップを検証し、リストやマップ自体にも制約を付けます。

### 制約の前に値を変換する

`String` の変換は、制約の前に書きます。それ以外の値は `preprocess` で変換します。`null` は変換されません。

```java
StringValidator<String> emailValidator = Yavi._string("email", c -> c.trim().toLowerCase().notBlank().email());
emailValidator.validate("  USER@Example.COM "); // Valid[value=user@example.com]

BigDecimalValidator<BigDecimal> priceValidator = Yavi._bigDecimal("price", c -> c.positive())
        .preprocess(BigDecimal::stripTrailingZeros);
```

`c.trim()`、`c.toLowerCase()`、`c.toUpperCase()`、`c.toNormalForm(form)`、`c.transform(f)` は、`Yavi.schema()._string("email", c -> ...)` のように、`String` の値の制約を書く場所ならどこでも使えます。

### コンビネータ

すべての `ValueValidator` に次のメソッドがあります。

| メソッド | 結果 |
| --- | --- |
| `nullable()`、`optional()`、`defaultValue(value)` | `null` を受け付け、`null`、`Optional.empty()`、デフォルト値のいずれかを返す |
| `recover(fallback)`、`recoverWith(violations -> ...)` | 失敗しない。`null` と不正な値に対してフォールバック値を返す |
| `liftList()`、`liftSet()`、`liftMap()` | すべての要素または値を検証する。違反はインデックスまたはキーの下に位置づけられる |
| `prefixed(name)` | 違反の名前の前に名前を付ける（`orders[1].id`） |
| `ValueValidator.anyOf(a, b)` | 最初に有効だった選択肢の結果。すべて失敗した場合は `object.anyOf` に続けて各選択肢の違反 |
| `ValueValidator.lazy(supplier)`、`recursive(self -> ...)` | まだ作られていないバリデータへの参照 |

### 新しい制約

`BigDecimalConstraint` に `fractionDigits(max)` と `integerDigits(max)` が追加されました。Bean Validation の `@Digits` に相当します。桁数は表記ではなく値に対して数えるので、`1.500` の小数部は 1 桁です。

```java
Yavi._bigDecimal("price", c -> c.fractionDigits(2).integerDigits(6));
// 1.505: "price" must have at most 2 fraction digits
```

## 省略可能なプロパティとネストしたオブジェクトやリストやマップ

`Yavi.schema()` のプロパティは、直後の修飾メソッドで変えない限り `null` を拒否します。プロパティには、オブジェクト、リスト、マップも指定できます。

```java
ObjectInputValidator<Address> addressValidator = Yavi.schema()
        ._string("street", c -> c.notBlank())
        ._string("city", c -> c.notBlank())
        .fromMap(Address::new); // 次の節を参照

Arguments6Validator<String, String, Integer, Role, Map<String, Object>, List<String>, User> userValidator = Yavi.schema()
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

* `nullable()`、`optional()`、`defaultValue(...)`、`recover(...)`、`recoverWith(...)` は、直前に追加したプロパティに適用されます。`nullable()` は値の型を `@Nullable` に変えます。
* `_object(name, validator)`、`_list(name, elementValidator)`、`_map(name, valueValidator)` の違反は、`name()`（`address.street`、`tags[1]`、`prices.pen`）でも `path()` でも、プロパティの名前の下に位置づけられます。
* `null` を受け付けるバリデータは、名前を付けて渡しても `null` を受け付けます。`_object("address", addressValidator.nullable())` は、`_object("address", addressValidator).nullable()` と同じく `null` に対して `null` を返します。
* `_list` と `_map` の制約は、すべての要素が有効な場合に検査され、プロパティに対して報告されます（`"tags" must be unique. [a] is/are duplicated.`）。
* `constraintOnTarget` は、複数のプロパティにまたがるルールです。すべてのプロパティが有効な場合に検査されます。`fromMap` の後では `ObjectInputValidator` を返すので、続けて `rejectingUnknownProperties()` や `readingParameters()` を書けます。
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
| 存在しないキー | `null` と同じように検証する。`optional()`、`nullable()`、`defaultValue(...)`、`recover(...)` があれば受け付け、なければ `object.required`。`required()` を付けると常に `object.required` |
| 明示的な `null` | `object.notNull`、または `null` を拒否する制約の違反。`rejectingNull()` を付けると常に `object.notNull` |
| プロパティが定義されていないキー | 無視する。`rejectingUnknownProperties()` を付けると `object.unknownProperty` を報告する |

* `_enum` と `_object` のプロパティをマップから読むには、型の指定が必要です（`_enum(name, type)`、`_object(name, type)`）。
* `ignoringCase()` を付けると、enum の定数名の大文字と小文字を区別せずに読み取ります。`._enum(Yavi._enum("role", Role.class, c -> c).ignoringCase())` は `"admin"` を受け付けます。
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

`of("type")` だけではバリデータになりません。選択肢のないユニオンはどんな入力も拒否するためです。最初の `when` がバリデータを返します。

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

16 個を超えるキーを読む場合も `_embedded` を使います。バリデータを渡さない `_rest()` は、値をそのまま受け取ります。残りのキーがなければ `_rest` の値は空のマップで、`null` にはなりません。そのため続けられる修飾メソッドは `recover(...)` だけで、`_rest().nullable()` や `_rest().defaultValue(...)` はコンパイルできません。`_rest` はスキーマの最後のプロパティなので、書けるのは 1 回だけです。`_rest` を持つスキーマのバリデータには未知のキーがないので、`rejectingUnknownProperties()` はありません。

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

存在しないキーは検証されません。存在するキーは、`presence()` がない場合と同じように検証されます（`{"nickname": null}` は `"nickname" must not be null`）。`presence()` は、プロパティの最後の修飾メソッドとして書きます。

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

| プロパティ | `fromMap`: 存在しないキー | `fromMap`: 明示的な `null` | `apply`: 引数が `null` | JSON Schema |
| --- | --- | --- | --- | --- |
| `_string("x")` | `object.required` | `object.notNull` | `object.notNull` | `required`、`"type": "string"` |
| `.nullable()`、`.optional()`、`.defaultValue(...)`、`.recover(...)` | 受け付ける | 受け付ける | 受け付ける | `"type": ["string", "null"]` |
| 上記に `.required()` を続ける | `object.required` | 受け付ける | 受け付ける | `required`、`"type": ["string", "null"]` |
| 上記に `.rejectingNull()` を続ける | 受け付ける | `object.notNull` | 受け付ける | `"type": "string"` |
| `.presence()` | `Absent` | プロパティが検証する | `Absent`（引数の型は `Presence`） | `"type": "string"` |

どちらも `presence()` と同じく、プロパティの最後の修飾メソッドとして、`optional()`、`nullable()`、`defaultValue(...)`、`recover(...)` のいずれかの後に書きます。`null` を受け付けないプロパティは存在しないキーも `null` も拒否するので、`_string("x").required()` はコンパイルできません。この 2 つは存在しないキーと `null` を区別するものなので、キーを持つ入力（マップ、`readingWith(reader)` で読むツリー、パラメーター）に対して働きます。値が空のパラメーターは `null` として読み取られるので、キーは存在する扱いになります。型付きの引数（`apply`）には「存在しない」という状態がありません。`null` が存在しない値を表すため、`rejectingNull()` を付けた場合も含めて、これらを付けない場合と同じように検証されます。JSON Schema は `fromMap` と `apply` で同じです。

`optional()`、`nullable()`、`defaultValue(...)`、`recover(...)` はどれも、プロパティを検証する前に `null` に対して返す値を決めます。2 つ重ねても、効くのは最後に書いたものだけです。効果のない部分を含む連鎖はコンパイルできません。

| 連鎖 | |
| --- | --- |
| `.optional().recover(Optional.empty())`、`.nullable().recover(null)` | コンパイルできる。不正な値を、値がない場合と同じ結果にする |
| `.recover(0).nullable()`、`.recover(0).optional()`、`.recover(0).defaultValue(1)` | コンパイルできる。`null` に対して、不正な値のフォールバック値とは別の値を返す |
| `.nullable().optional()`、`.defaultValue(1).nullable()`、`.optional().defaultValue(...)` | コンパイルできない。1 つ目が `null` に対して返す値は使われない |
| `.defaultValue(1).recover(2)` | コンパイルできない。`recover` は `null` も置き換える。`.recover(2).defaultValue(1)` と書く |
| `.recover(1).recover(2)` | コンパイルできない。1 つ目は失敗しない |

`_embedded` のプロパティも同じです。`ValueValidator` のコンビネーターには制限がなく、`NullAcceptingValueValidator` を返します。これは `null` を受け付けることが型でわかるバリデーターです。このバリデーターで追加したプロパティには `required()`、`rejectingNull()`、`presence()` を続けられ、`null` を重ねて受け付ける修飾メソッドは続けられません。

```java
NullAcceptingValueValidator<Integer, Integer> page = Yavi._integer("page", c -> c.positive()).defaultValue(1);

Yavi.schema()._integer(page).rejectingNull() // 省略したときだけ 1
```

スキーマのほかの部分も同じように段階化されています。効果のない書き方や、バリデータを作る時点で失敗する書き方はコンパイルできません。

| 連鎖 | |
| --- | --- |
| `.optional().presence()` | コンパイルできない。`Optional` が `Presence` に包まれてしまう。`.nullable().presence()` と書く |
| `._rest()._rest()`、`._rest()._string("x")` | コンパイルできない。`_rest` は最後のプロパティ |
| `._rest().fromMap(f).rejectingUnknownProperties()` | コンパイルできない。未知のキーがない |
| `.fromMap(f).rejectingUnknownProperties().rejectingUnknownProperties()` | コンパイルできない。`fromMap` は `ObjectInputValidator.IgnoringUnknown` を返し、指定できるのは 1 回だけ |
| `Yavi._enum(Role.class).ignoringCase().ignoringCase()` | コンパイルできない。`ignoringCase()` は `map` の後、最後に書く |
| `._enum("role")`、`._object("address")` | 存在しない。enum とオブジェクトは、`fromMap` に必要な型を付けて追加する |

`null` を返す修飾メソッドは `nullable()` だけです。`null` にならない値（制約で検証した値、リスト、マップ、`Optional`）のデフォルト値やフォールバック値に `null` を渡すと、nullness チェッカーがあればコンパイル時に報告されます。チェッカーがない場合も、スキーマを組み立てた時点で `NullPointerException` になります。引数の型が `null` を許さない関数に `null` が渡ることはありません。

| 連鎖 | |
| --- | --- |
| `._string("x").defaultValue(null)`、`._string("x").recover(null)` | `NullPointerException`。`.nullable()` または `.nullable().recover(null)` と書く |
| `._string("x").optional().recover(null)` | `NullPointerException`。`.optional().recover(Optional.empty())` と書く |
| `._string("x").recoverWith(violations -> null)` | 関数が `null` を返した時点で `NullPointerException` |
| `._string("x").nullable().recover(null)` | 受け付ける |
| `._string(ValueValidator.passThrough()).defaultValue(null)` | 受け付ける。バリデータ自身が `@Nullable String` を返す |

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

JDK のツリーモデル（[JEP 540](https://openjdk.org/jeps/540)。JDK 28 のインキュベータモジュール）も同じ方法で読み取れます。そのリーダーは README にあります。

スカラーがすべて文字列であるツリーは、`coercingStrings()` で読み取ります。これは `CoercibleInputReader`（ツリーモデルのリーダーが実装するインターフェース）に付けられるデコレータです（`JsonNodeReader.INSTANCE.coercingStrings()`。JDK の型のツリーには `InputReader.ofJdkTypes().coercingStrings()`）。文字列は、次の節のルールで読み取られます。文字列を変換するリーダーの型は `InputReader` で、重ねて変換することはできません。`reader.coercingStrings(rules).coercingStrings()` はコンパイルできないので、ルールは一度にまとめて渡します。`StringCoercion` の各ルールも一度しか書けず、`booleanWords(...).booleanWords()` はコンパイルできません。バリデータは最後に渡されたリーダーで読み取ります。`readingWith(reader.coercingStrings())` や `readingParameters()` の後に `readingWith(reader)` を呼ぶと、文字列は変換されません。

## リクエストパラメータを検証する

リクエストパラメータや送信されたフォームは、文字列のフラットなマップであり、1 つのキーに複数の値を持つこともあります。`fromMap` で作ったバリデータの `readingParameters()` は、このようなマップを読み取ります。文字列は、プロパティの型の値を表すテキストとして扱われ、キーはネストした値へのパスとして扱われます。

```java
record SearchForm(String q, Integer page, Boolean includeArchived, @Nullable LocalDate since, List<String> tags,
        @Nullable Address address) {}

ValueValidator<Map<String, List<String>>, SearchForm> searchFormValidator = Yavi.schema()
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

マップの型は、バリデータを代入する変数の型で決まります。`Map<String, String>`、Spring の `MultiValueMap` のような `Map<String, List<String>>`、Servlet API の `Map<String, String[]>` のいずれかを指定できます。

値のルール:

* 空文字列は、プロパティの型にかかわらず `null` と同じように検証されます。`""` を受け取りたい文字列のプロパティには `defaultValue("")` を付けます。
* 数値は、前後の空白や桁区切りのない 10 進数です。`"30"` と `"30.0"` は `Integer` として読めますが、`"30.5"`、`" 30"`、`"1,000"` は読めません。真偽値は `"true"` または `"false"` です。
* 型を表さない文字列は、例外ではなく `object.type` で報告されます（`"page" must be of type integer`）。

キーのルール:

* `address.city` と `address[city]` は、`address` の `city` プロパティです。`tags[]` は `tags` と同じです。
* 複数の値を持つキーはリストです（`tags=a&tags=b`）。スカラーのプロパティに複数の値がある場合は `object.type` で報告されます。
* インデックス付きのキーは、インデックス順のリストです（`items[0].name=pen&items[1].name=ink`）。飛ばされたインデックスは `null` の要素になるので、違反は送信時のインデックスを保ちます。インデックスの上限は 255 です（`ParameterTree.DEFAULT_MAX_INDEX`）。
* 空のリストは送信できません。キーが存在しないことになります。チェックボックスのように空になりうるリストには `defaultValue(List.of())` を付けます。

これらのルールは意図的に厳密にしてあります。フォームのフィールドや CSV のセルのように人が書いたテキストに対しては、`StringCoercion` でルールを緩められます。

```java
record Signup(Integer age, Boolean subscribed) {}

StringCoercion form = StringCoercion.strict().trimming().booleanWords();

ValueValidator<Map<String, String>, Signup> signupValidator = Yavi.schema()
        ._integer("age")
        ._boolean("subscribed").defaultValue(false) // チェックされていないチェックボックスは何も送信しない
        .fromMap(Signup::new)
        .readingParameters(form);

signupValidator.validate(Map.of("age", " 30 ", "subscribed", "on")); // Signup[age=30, subscribed=true]
```

* `trimming()`: 空白だけの文字列は `null` と同じように検証されます。数値、真偽値、enum の定数、日時の値として読む文字列は、前後の空白を除いて読み取られます。`String` の値はそのままです（`c.trim()` でトリムします）。
* `booleanWords()`: `true`、`1`、`yes`、`on` と `false`、`0`、`no`、`off` を、大文字と小文字を区別せずに真偽値として読み取ります。

リクエストを失敗させたくないパラメータには `recover` が使えます。

```java
record PageRequest(Integer page, Integer size) {}

ValueValidator<Map<String, String>, PageRequest> pageValidator = Yavi.schema()
        ._integer("page", c -> c.greaterThanOrEqual(0)).recover(0)
        ._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
        .fromMap(PageRequest::new)
        .readingParameters();

pageValidator.validate(Map.of("page", "-1", "size", "9999")); // PageRequest[page=0, size=20]
```

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
//   "message": "\"address.street\" must not be blank", "args": ["address.street", ""]}, ...]
```

`args` の末尾には違反した値が入ります。その値をレスポンスに含めたくない場合は、独自の型に詰め替えてください。

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

* プロパティは、`optional()`、`nullable()`、`defaultValue(...)`、`recover(...)` のいずれかが付いていて `required()` が続かない場合と、`presence()` で読む場合を除いて、`required` になります。これらのプロパティは、`rejectingNull()` で終わる場合を除いて、型 `null` も受け付けます。
* リストやマップの制約は `minItems`、`maxItems`、`uniqueItems`、`minProperties`、`maxProperties` として、`rejectingUnknownProperties()` は `additionalProperties: false` として、再帰的な構造は `$ref` として出力されます。
* ラムダと `ValueValidator.compose` の結果は、内容が不明として扱われます。`describedAs(...)` で記述を与えてください。条件付きの制約と `constraintOnTarget` は記述に含まれません。
* `ValidatorBuilder` で作った Bean の `Validator` は記述に対応していません。

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
| `_embedded`、`_rest` | [#545](https://github.com/making/yavi/pull/545) |
| `Yavi._string` など | [#546](https://github.com/making/yavi/pull/546) |
| 変換、`preprocess` | [#547](https://github.com/making/yavi/pull/547) |
| `_list` と `_map` の制約 | [#548](https://github.com/making/yavi/pull/548) |
| `flatApply`、`flatFromMap`、`failureWith`、record の `ArgumentsN` | [#549](https://github.com/making/yavi/pull/549) |
| `recover`、`recoverWith` | [#550](https://github.com/making/yavi/pull/550) |
| `messagesByName()`、`pointer()`、`ViolationDetail` | [#551](https://github.com/making/yavi/pull/551) |
| `ignoringCase()`、`StringCoercion`、`fractionDigits` | [#552](https://github.com/making/yavi/pull/552) |
| `lazy`、`recursive` | [#553](https://github.com/making/yavi/pull/553) |
| `presence()` | [#555](https://github.com/making/yavi/pull/555) |
| `required()`、`rejectingNull()` | [#562](https://github.com/making/yavi/pull/562) |
| 効果のない修飾メソッドの連鎖はコンパイルできない、`NullAcceptingValueValidator` | [#563](https://github.com/making/yavi/pull/563)、[#564](https://github.com/making/yavi/pull/564)、[#565](https://github.com/making/yavi/pull/565) |
| JSpecify、`@Nullable` のない値 | [#534](https://github.com/making/yavi/pull/534)、[#556](https://github.com/making/yavi/pull/556)、[#557](https://github.com/making/yavi/pull/557) |
