# Zod との比較

[Zod](https://zod.dev) は TypeScript のスキーマとデコーダです。型なしの入力を検証し、型付きの値を返します。YAVI 0.18 は同じモデルを Java で表現します。`Yavi.schema()` で入力のプロパティを一度記述すると、得られたバリデータはその入力を Map、JSON ツリー、リクエストパラメータから読み取り、すべてのプロパティが有効な場合にだけオブジェクトを作成します。これが 0.18 の主要な用途です。

Zod のプログラムの形は、次のように YAVI に対応します。

* スキーマは `Yavi.schema()` で記述し、`fromMap(f)` で閉じます。関数 `f` は通常の Java レコードまたはクラスのコンストラクタです。
* 結果の型は `f` が返す Java の型で、コンパイラがそれを検査します。型推論 (`z.infer`) や生成されたスキーマ型はありません。
* 入力は 1 つの `unknown` 値ではなく、Map、JSON ツリー、リクエストパラメータ、型付き引数です。同じスキーマがすべての形式を読み取り、形式はバリデータを使う時に選びます。
* プロパティが省略可能か `null` を許すかは型の一部です。`nullable()` は `@Nullable` の値を、`optional()` は `Optional` を生成し、nullness チェッカ (NullAway、Kotlin) とコンパイラがそれを検査します。

## 基本

```ts
const User = z.object({
  email: z.string().email(),
  age: z.number().int().min(0),
  nickname: z.string().optional(),
});

const result = User.safeParse(JSON.parse(input));
if (result.success) {
  result.data; // { email: string; age: number; nickname?: string }
} else {
  result.error.issues;
}
```

```java
record User(String email, Integer age, Optional<String> nickname) {}

ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.greaterThanOrEqual(0))
        ._string("nickname").optional()
        .fromMap(User::new);

Map<String, Object> map = objectMapper.readValue(input, new TypeReference<>() {});
switch (userValidator.validate(map)) {
    case Validated.Valid<User>(User user) -> save(user);
    case Validated.Invalid<User>(ConstraintViolations violations) -> report(violations);
}
```

スキーマが入力を検証し、その後にだけ `User::new` が実行されます。プロパティはコンストラクタに引数として渡されるため、結果は通常のレコードであり、コンパイラがスキーマの記述と一致するかを検査します。

## 用語

| Zod | YAVI |
| --- | --- |
| schema (`ZodType<Output, Input>`) | `ValueValidator<T, X>` (入力 `T`、出力 `X`)。`Yavi.schema()` はプロパティを記述し、`fromMap(f)` または `apply(f)` がそれらのバリデータを作る |
| `z.object({...})` | `Yavi.schema()._xxx(...).fromMap(f)` (`ObjectInputValidator<X>`) |
| `schema.safeParse(input)` | `validator.validate(input)` は `Validated<X>` を返す |
| `schema.parse(input)` | `validator.validated(input)` は `ConstraintViolationsException` を投げる |
| `result.success` / `result.data` / `result.error` | `Validated.Valid(value)` / `Validated.Invalid(errors)` (sealed インタフェース) |
| issue (`code`, `path`, `message`) | `ConstraintViolation` (`messageKey()`、`path()`、`message()`) |
| `z.infer<typeof schema>` | `fromMap` / `apply` に渡した関数の結果型 |

## スキーマ

| Zod | YAVI |
| --- | --- |
| `z.string()`、`z.number()`、`z.boolean()`、`z.date()` ... をプロパティとして | `_string(name, c -> ...)`、`_integer`、`_long`、`_double`、`_bigDecimal`、`_boolean`、`_localDate`、`_instant` ... |
| 同じものを単独のスキーマとして | `Yavi._string(c -> ...)`、`Yavi._integer(c -> ...)` ... |
| `z.enum([...])` | `_enum(name, Role.class)`。`_enum(Yavi._enum(name, Role.class).ignoringCase())` は任意の大文字小文字で名前を読む |
| ネストした `z.object({...})` | `_object(name, anotherObjectInputValidator)` |
| `z.array(schema)` | `_list(name, elementValidator)`、`validator.liftList()` |
| `z.record(z.string(), schema)` | `_map(name, valueValidator)`、`validator.liftMap()` |
| `z.array(schema).nonempty().min(1).max(10)` | `_list(name, elementValidator, c -> c.notEmpty().greaterThanOrEqual(1).lessThanOrEqual(10))`、`Yavi._list(elementValidator, c -> ...)` (`unique()`、`contains(...)` も可) |
| サイズ付きの `z.record(z.string(), schema)` | `_map(name, valueValidator, c -> c.notEmpty())`、`Yavi._map(valueValidator, c -> ...)` |
| `.optional()` | `.optional().rejectingNull()` (`Optional<X>` を生成) |
| `.nullable()` | `.nullable().required()` (`@Nullable X` を生成) |
| `.nullish()` | `.optional()` または `.nullable()` |
| `.optional()` で `undefined` と値を結果で区別 | `.presence()` (`Presence<X>` を生成) |
| `.nullable().optional()` / `.nullish()` で両者を結果で区別 | `.nullable().presence()` |
| `.default(value)` | `.defaultValue(value).rejectingNull()`。`.defaultValue(value)` は `null` も置き換える |
| `.catch(value)` | `.recover(value)` (`null` と省略キーにも) |
| `.catch(ctx => ...)` | `.recoverWith(violations -> ...)` |
| `z.union([a, b])` | `ValueValidator.anyOf(a, b)` |
| `z.discriminatedUnion("type", [...])` | `DiscriminatedUnionValidator.<X>of("type").when("circle", a).when("square", b)` |
| `z.lazy(() => schema)`、スキーマを参照する getter | `ValueValidator.lazy(() -> validator)`、`ValueValidator.recursive(self -> ...)` |
| `z.number().multipleOf(n)` | 整数型、`BigInteger`、`BigDecimal` の `c.multipleOf(n)` (`Double` と `Float` にはない) |
| `z.email()`、`z.uuid()`、`z.ipv4()`、`z.ipv6()`、`z.ulid()` | `c.email()`、`c.uuid()`、`c.ipv4()`、`c.ipv6()`、`c.ulid()` |
| `z.union([z.ipv4(), z.ipv6()])` | `c.ip()` |
| `z.url()` | `c.uri()` (任意のスキーム)、`c.url()` (`java.net.URL` のプロトコル) |
| `z.cuid()`、`z.cuid2()` | `c.pattern(regex)` |
| 値に対する `.refine(predicate)` | 制約 (`c -> c.predicate(...)`) |
| オブジェクトに対する `.refine(predicate)` | `.constraintOnTarget(predicate, name, violationMessage)` |
| `.transform(f)` | `.map(f)` |
| `z.string().trim().toLowerCase()`、`.toUpperCase()`、`.normalize()` | `_string(name, c -> c.trim().toLowerCase()...)`、`c.toUpperCase()`、`c.toNormalForm(form)`、`c.transform(f)` (制約より前に記述) |
| `z.preprocess(f, schema)` | 同じ型の値には `validator.preprocess(f)` |
| `.pipe(schema)` | `.andThen(validator)` |
| `z.unknown()` / `z.any()` | `_object(name, Object.class)` |
| `z.object({...})` (未知キーを削除) | `fromMap` の既定 |
| `z.strictObject({...})` | `.rejectingUnknownProperties()` |
| `z.looseObject({...})` | `._rest()` |
| `z.object({...}).catchall(schema)` | `._rest(valueValidator)` |
| オブジェクトの `a.extend(b.shape)`、`z.intersection(a, b)` | `._embedded(b)` (関数に `b` のオブジェクトを渡す) |
| `z.toJSONSchema(schema)` | `JsonSchema.of(validator.description())` |

制約の一覧は「[組み込みの制約](built-in-constraints.md)」に、値の組み合わせは「[スキーマの定義](defining-a-schema.md)」にあります。

## 入力

Zod のスキーマは常に `unknown` を取り、実際には `JSON.parse` が返すものです。Java では入力に複数の形式があり、同じプロパティがすべての形式から読み取られます。スキーマは `fromMap(f)` で一度書き、入力の形式は最後に選びます。

| 入力 | Zod | YAVI | 入力の型 |
| --- | --- | --- | --- |
| パース済みの JSON ドキュメントを Map として | `schema.safeParse(JSON.parse(input))` | `validator` | `Map<String, Object>` |
| 型の分からない値 | `schema.safeParse(input)` | `validator.untyped()` | `Object` |
| JSON ライブラリのツリーモデル | | `validator.readingWith(reader)` | `JsonNode` などのノード型 |
| リクエストパラメータ、送信されたフォーム | `z.coerce`、`zod-form-data` | `validator.readingParameters()` | `Map<String, String>`、`Map<String, List<String>>`、`Map<String, String[]>` |
| 型付き引数 | | `Yavi.schema()...apply(f)` | 引数の型 |

### Map または型なしの値

`fromMap(f)` は `Map<String, Object>` のバリデータを構築します。`untyped()` はそれと、入力型を知る他のバリデータを、Zod のスキーマに相当する `ValueValidator<Object, X>` に変換します。

```java
ValueValidator<Object, User> any = userValidator.untyped(); // rejects anything but a map
ValueValidator<Object, Object> id = ValueValidator.anyOf(Yavi._string(c -> c.notBlank()).untyped(),
        Yavi._integer(c -> c.positive()).untyped());
```

入力型はバリデータの構築時に分かっていなければなりません。`fromMap` と `untyped()` は、ラムダ、または型を付けずに追加した `_enum` / `_object` のプロパティに対して例外を投げます。`untyped(type)`、`_enum(name, type)`、`_object(name, type)` で型を渡してください。「[Map と型なしの値](input-maps.md)」を参照してください。

### JSON ライブラリのツリーモデル

`readingWith(reader)` はバリデータを `InputReader` に結び付けます。`InputReader` はツリーモデルのノードの読み方を示します。ツリーは Map にコピーされずに検証されます。`from(reader, f)` はスキーマからそのバリデータを構築します。

```java
ValueValidator<JsonNode, User> nodeValidator = userValidator.readingWith(JsonNodeReader.INSTANCE);
ValueValidator<JsonNode, User> same = Yavi.schema()
        ._string("email")
        .from(JsonNodeReader.INSTANCE, User::new);
```

YAVI は JSON ライブラリに依存しません。リーダーはツリーモデルごとに一度書く小さなクラスです。[Jackson](integrations-jackson.md) にリーダーの例があります。

### リクエストパラメータ

リクエストパラメータと送信されたフォームは、文字列のフラットな Map です。`readingParameters()` は、JSON ドキュメントと同じようにプロパティを記述したスキーマでそれを読み取ります。

```java
ValueValidator<Map<String, ?>, SearchForm> validator = Yavi.schema()
        ._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
        ._boolean("includeArchived").defaultValue(false)
        ._list("tags", tagValidator).defaultValue(List.of())
        ._object("address", addressValidator).nullable()
        .fromMap(SearchForm::new)
        .readingParameters();

// page=2&includeArchived=true&tags=a&tags=b&address.city=Tokyo
validator.validate(parameters);
```

これは Zod で 2 つの機能を必要とすることを行います。

* 値はプロパティの型を表す文字列です。Zod では `z.coerce` です。規則は「[文字列](#strings)」にあります。
* キーはパス (`address.city`、`items[0].name`) で、複数の値を持つキーはリストです。Zod では `FormData` のために `zod-form-data` が加えるものです。「[リクエストパラメータとフォーム](input-parameters.md)」を参照してください。

空のリストは送信できないため、空になり得るリストには `defaultValue(List.of())` を付けます。

### 型付き引数

Java では入力は通常すでに型に束縛されているため、YAVI のバリデータは最初に型付けされます (`ValueValidator<String, Email>`)。`apply(f)` は、`fromMap(f)` が Map のバリデータを構築するスキーマから、型付き引数のバリデータを構築します。Zod に対応するものはありません。Zod のスキーマは型と制約を一緒に検査します。「[型付き引数](input-arguments.md)」を参照してください。

## 異なる規則

### 省略と null

Zod のスキーマと同様に、プロパティは別段の指定がない限り必須で、別段の指定は型を変えます。`nullable()` は `@Nullable X` を、`optional()` は `Optional<X>` を生成します。`z.infer` が `X | null` と `X | undefined` を与えるのと同じです。そのため JSpecify を読む nullness チェッカ (NullAway、Kotlin) は、`nullable()` のプロパティを `null` でない値として受け取る関数を拒否します。

Zod は `undefined` (`optional()`) と `null` (`nullable()`) を区別します。YAVI では省略キーは `null` のように検証されるため、`optional()`、`nullable()`、`defaultValue(...)` は両方を受け入れます。必須プロパティで違反が異なるだけです。省略キーは `object.required`、明示的な `null` は `object.notNull`、または `notBlank()` のように `null` を拒否する制約の違反として報告されます。

プロパティの最後の修飾子は、Zod と同様に両者を区別します。`required()` は `null` を受け入れるものの省略キーを拒否し、`rejectingNull()` はキーが省略されてもよいものの明示的な `null` を拒否します。値の型は変わりません。

```ts
const Account = z.object({
  deletedAt: z.iso.datetime().nullable(), // the key is required
  middleName: z.string().optional(),      // null is rejected
  page: z.number().int().default(1),      // null is rejected
});
```

```java
record Account(@Nullable Instant deletedAt, Optional<String> middleName, int page) {}

ObjectInputValidator<Account> accountValidator = Yavi.schema()
        ._instant("deletedAt").nullable().required()
        ._string("middleName").optional().rejectingNull()
        ._integer("page").defaultValue(1).rejectingNull()
        .fromMap(Account::new);
```

`presence()` は 3 つの状態を作成されたオブジェクトに渡し、部分更新 (PATCH) に使います。プロパティは `Presence<X>` です。省略キーは検証されない `Absent` で、それ以外はプロパティが値から作るもので `null` も含みます。

```java
record ProfilePatch(Presence<String> nickname, Presence<String> bio) {}

ObjectInputValidator<ProfilePatch> patchValidator = Yavi.schema()
        ._string("nickname", c -> c.lessThanOrEqual(50)).presence()
        ._string("bio", c -> c.lessThanOrEqual(500)).nullable().presence()
        .fromMap(ProfilePatch::new);
```

「[省略と既定値](optional-and-defaults.md)」を参照してください。

### 未知のキー

どちらもオブジェクトが strict でない限り未知のキーを無視します。`rejectingUnknownProperties()` は `object.unknownProperty` というメッセージキーで報告し、省略可能なプロパティの綴り間違いを見つけます。`z.strictObject` と同様に、そのオブジェクトにだけ適用されます。

`.catchall(schema)` と `z.looseObject` に対応するのは引数です。結果は関数で作成されるため、余分なキーは Map として関数に渡されます。`_rest(valueValidator)` はその値を検証し、`_rest()` はそのまま受け取ります。

```java
record Item(String name, Map<String, String> attrs) {}

ObjectInputValidator<Item> itemValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._rest(Yavi._string(c -> c.notBlank()))
        .fromMap(Item::new);
```

### 型

どちらも既定では厳密です。`"30"` は数値ではありません。YAVI は、JSON ドキュメントがプロパティの型に対して持つ表現を受け入れます。

* Java の型が何であっても、値が保たれる限り数値 (`30L` と `30.0` は `Integer`、`30.5` はそうではない)
* enum の場合は定数の名前
* `LocalDate`、`Instant` などの時間型、`Duration`、`Period` の場合は ISO-8601 文字列
* `UUID` と `URI` の場合はその文字列表現 (`_object("id", UUID.class)`)

不一致は `object.type` (`"age" must be of type integer`) で報告されます。

### 文字列

`z.coerce` はスキーマごとに選び、JavaScript のコンストラクタ (`Number(input)`、`Boolean(input)`) で変換します。YAVI では文字列の変換はプロパティではなく入力に属するため、スキーマは変わりません。

* `readingParameters()` はリクエストパラメータの文字列を変換します。
* `CoercibleInputReader#coercingStrings()` はツリーモデルのリーダーを装飾し、すべてのスカラーが文字列のツリーを扱います。`validator.readingWith(JsonNodeReader.INSTANCE.coercingStrings())` は、数値が文字列で送られる JSON ドキュメントを読み取ります。

どちらも `z.coerce` より厳しい、同じ規則に従います。

| 入力 | `z.coerce` | YAVI |
| --- | --- | --- |
| 数値に対する `""` | `0` | `null` であり、`optional()` と `defaultValue` が適用される |
| 数値に対する `" 30 "` | `30` | `object.type`。`trimming()` で `30` |
| 文字列に対する `" a "` | `" a "`。`.trim()` で `"a"` | `" a "`。`trimming()` または `c.trim()` で `"a"` |
| 数値に対する `"0x1e"` | `30` | `object.type` |
| boolean に対する `"false"` | `true` | `false` |
| boolean に対する `"on"`、`"1"` | `true` | `object.type`。`booleanWords()` で `true` (`z.stringbool()` と同様) |
| 文字列に対する `30` | `"30"` | `object.type` |

`trimming()` と `booleanWords()` は `StringCoercion` のオプションで、`readingParameters(coercion)` または `coercingStrings(coercion)` に渡します。

```java
validator.readingParameters(StringCoercion.strict().trimming().booleanWords());
```

これは送信されたフォームを読み取ります。チェックされたチェックボックスは `on` を送ります。空文字列は文字列プロパティでも `null` です。

### ユニオン

`z.union` と `ValueValidator.anyOf` は代替案を順に試し、どれも一致しない場合に 1 つの問題を報告します。`invalid_union` とその `errors`、各代替案の問題、そして `object.anyOf` です。`object.anyOf` は `ConstraintViolation.NoAlternativeMatched` で、`alternatives()` に各代替案の違反があります (`ViolationDetail.NoAlternativeMatched` の `alternatives`)。`DiscriminatedUnionValidator` は `z.discriminatedUnion` と同様に、判別子が選んだ代替案だけを適用します。代替案は判別子を `z.literal(...)` として定義する必要はありません。判別子を定義しない代替案からはそのキーが隠されます。「[ユニオン](unions.md)」を参照してください。

### エラー

`ConstraintViolation` は問題コードの代わりにメッセージキーを持ち、メッセージは `MessageFormatter` でフォーマットされるため、ローカライズできます。`path()` は `PathSegment.Property` と `PathSegment.Index` のリストで、`name()` はそのフラットな形 (`address.street`、`tags[0]`)、`pointer()` は JSON Pointer (`/address/street`、`/tags/0`) です。

| Zod | YAVI |
| --- | --- |
| `z.flattenError(error)` | `violations.messagesByName()` (名前からメッセージへの Map)。ルートのメッセージは空の名前の下にある |
| `error.issues` を JSON として | `violations.details()` (`ViolationDetail` のリスト。`name`、`pointer`、`messageKey`、`message`、`params`、`object.anyOf` の場合は `alternatives`) |
| `issue.minimum`、`issue.maximum`、`issue.format` など | 詳細の `params`、`violation.params()` (`{"max": 150}`)。違反した値は含まれない |

`z.treeifyError` に対応するものはありません。`messagesByPointer()` のキーがネストした値を指します。「[違反](violations.md)」を参照してください。

## コーデック

`z.codec(input, output, { decode, encode })` と `z.encode(schema, value)` は、読み取ったものを書き戻すバリデータです。

| Zod | YAVI |
| --- | --- |
| `z.codec(z.string(), z.custom<Email>(), { decode, encode })` | `Yavi._string("email", c -> c.email()).map(Email::new, Email::value)` (`ReversibleValueValidator`) |
| `z.encode(codec, value)` | `validator.reverse(value)` |
| `z.encode(objectSchema, value)` | `Yavi.reversibleSchema()...fromMapReversible(f, accessor1, ...)` の後に `validator.toMap(value)` |

`map(f)` のように逆方向を知らないバリデータは、Zod が実行時にエンコードを拒否する transform に相当します。YAVI はコンパイル時に拒否します。それは `ReversibleValueValidator` ではなく、`Yavi.reversibleSchema()` のスキーマはそれを受け取りません。「[オブジェクトの書き戻し](writing-back.md)」を参照してください。

## 利用できないもの

* スキーマを派生させるオブジェクトスキーマの操作: `pick`、`omit`、`partial`。`extend` と `merge` は `_embedded` で、埋め込んだバリデータのオブジェクトを保持します。
* スキーマとしての `z.tuple` と `z.literal` (`equalTo` / `oneOf` の制約を使ってください)。
* 非同期の refinement と transform。
* 1 つのスキーマでの 16 個を超えるプロパティ。`_embedded` でグループ化するか、`ObjectInputValidator.of(parts, mapper)` を使ってください。
