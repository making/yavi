# オブジェクトとコレクション

プロパティは、入れ子のオブジェクト、リスト、Map にできます。このページでは、同じ Map から読み取るプロパティ、未知のキー、プロパティが実行時に決まるスキーマ、再帰構造も扱います。

## 入れ子のオブジェクト

入れ子のオブジェクトは `_object(name, validator)` で追加します。バリデータは `fromMap` または `from` で構築し、入れ子の Map またはノードを読み取ります。

```java
ObjectInputValidator<Address> addressValidator = Yavi.schema()
        ._string("street", c -> c.notBlank())
        ._string("city", c -> c.notBlank())
        .fromMap(Address::new);

ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._object("address", addressValidator)
        .fromMap(User::new);
```

入れ子の値の違反は、プロパティ名の下に位置づけられます。`name()`（`address.street`）と `path()` の両方です。入れ子のバリデータは複数のスキーマで再利用でき、Map 用に定義したバリデータは、外側のバリデータを `readingWith(reader)` で読み取る場合に JSON ツリーにも使えます。

## リスト

`_list(name, elementValidator)` は、与えられたバリデータで各要素を検証します。要素の違反はそのインデックスの下に位置づけられます。

```java
ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._list("roles", Yavi._string(c -> c.notBlank())) // roles[0]
        ._list("tags", Yavi._string(c -> c.notBlank()),
                c -> c.notEmpty().lessThanOrEqual(10).unique()) // tags
        .fromMap(User::new);
```

* 要素は他の値と同じように必須です。`List<@Nullable String>` には `elementValidator.nullable()` を与えます。
* `_list` の制約は、すべての要素が有効な場合にだけ、検証済みの要素に対して検査され、プロパティ（`roles`）で報告されます。
* `unique(key)` は、識別子など要素のキーによって要素を一意にします。重複したキーはメッセージと `params` に含まれます。
* 名前が空の要素バリデータは、要素のプロパティではなく要素自体の違反を報告します。
* オブジェクトを読み取るバリデータを伴う `_list` は `addresses[0].street` を報告します。

## Map

`_map(name, valueValidator)` は Map の各値を検証します。キーはプロパティ名ではなくデータであるため、違反はキーの下に位置づけられます（`scores.math`）。

```java
ObjectInputValidator<Exam> examValidator = Yavi.schema()
        ._string("student")
        ._map("scores", Yavi._integer(c -> c.greaterThanOrEqual(0).lessThanOrEqual(100)),
                c -> c.notEmpty())
        .fromMap(Exam::new);
```

文字列でないキーは `object.type` で報告されます。`_map` の制約は検証済みの値に対して検査され、プロパティで報告されます。

## 埋め込みオブジェクト

`_embedded(validator)` は、プロパティと同じ Map を読み取ります。JOIN の行にある複数のテーブルの列からオブジェクトを構築します。

```java
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
        ._embedded(departmentValidator).nullable() // null if dept_name and dept_code have no value
        .fromMap(Employee::new);
```

* 埋め込みバリデータのプロパティは、埋め込む側のプロパティです。記述され、`rejectingUnknownProperties()` から未知とは見なされず、同じ名前を二度定義するとバリデータの構築時に拒否されます。
* `optional()`、`nullable()`、`defaultValue(...)` を伴う埋め込みバリデータは、どのプロパティにも値がない場合に `null` を受け取ります。
* 埋め込みは、16 個を超えるキーを読み取る方法でもあります。
* ラムダも埋め込めます。読み取るプロパティが不明なため、そのバリデータは未知のプロパティを拒否できず、rest も読めません。

## 未知のプロパティ

どのプロパティにも定義されていないキーは無視されます。`rejectingUnknownProperties()` はそれを `object.unknownProperty` で報告し、JSON Schema に `additionalProperties: false` を書きます。

```java
ObjectInputValidator<Signup> signupValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        .fromMap(Signup::new)
        .rejectingUnknownProperties();
```

この設定はその Map にだけ適用され、入れ子の Map には適用されません。任意のプロパティの綴り間違いを見つけられます。チェックボックスのように隠しフィールドを追加するフォームは、そのキーのプロパティか rest を必要とします。

## rest プロパティ

`_rest(valueValidator)` は、どのプロパティにも定義されていないキーを検証し、Map としてコンストラクタに渡します。`_rest()` は値をそのまま受け取ります。

```java
record Item(String name, Map<String, String> attrs) {}

ObjectInputValidator<Item> itemValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._rest(Yavi._string(c -> c.notBlank()))
        .fromMap(Item::new);
```

* `_rest` はスキーマの最後のプロパティであり、`_rest` を持つスキーマに `rejectingUnknownProperties()` はありません。未知のキーが存在しないためです。
* 他のキーがなければ rest は空の Map であり、`null` にはなりません。そのため `recover(...)` だけを取り、`_rest().nullable()` はコンパイルできません。
* rest の違反はそのキーの下に位置づけられます。

## 実行時に決まるスキーマ

`Yavi.dynamicSchema()` は、設定やデータベースから来るプロパティ、たとえばアンケートの質問やカスタムフィールドを記述します。そのバリデータは、検証済みの値を名前ごとにまとめた Map を生成します。

```java
record Question(String id, String title, boolean required, int maxLength) {}

DynamicSchema<String> schema = Yavi.dynamicSchema();
for (Question question : survey.questions()) {
    StringValidator<String> answer = Yavi._string(c -> c.notBlank().lessThanOrEqual(question.maxLength()));
    schema = question.required() ? schema.property(question.id(), answer).label(question.title())
            : schema.property(question.id(), answer.defaultValue("")).label(question.title());
}
ObjectInputValidator<Map<String, String>> answersValidator = schema.build();

answersValidator.validate(Map.of("q1", "yavi")); // {q1=yavi}
```

スキーマは不変です。`property(name, validator)` は新しいスキーマを返します。バリデータはプロパティの名前の下で使われ、値はバリデータが `null` を受け入れない限り必須です。修飾は `label`、`required(message)`、`typeMessage`、`presence()` で、`Yavi.schema()` と同じ段階的な型を持ちます。`rest(valueValidator)` はどのプロパティにも定義されていないキーを検証し、Map に追加します。結果は `rejectingUnknownProperties()`、`readingParameters()`、`description()` を持ち、別のスキーマのプロパティにもできます。

## 再帰構造

`ValueValidator.recursive(self -> ...)` は、自身への参照からバリデータを構築します。自身を含む構造のためです。

```java
record Comment(String body, List<Comment> replies) {}

ObjectInputValidator<Comment> commentValidator = ValueValidator.recursive(self -> Yavi.schema()
        ._string("body", c -> c.notBlank())
        ._list("replies", self).defaultValue(List.of())
        .fromMap(Comment::new));
// a violation: replies[0].replies[0].body
```

`ValueValidator.lazy(() -> validator)` は、まだ構築されていないバリデータを参照します。互いに参照するバリデータのためです。`$defs` で記述を書く場合は、参照に名前を与えます。`lazy("comment", ...)` または `recursive("comment", ...)` です。

再帰構造の深さは入力の深さです。YAVI は制限を設けないため、信頼できない文書にはパーサの最大深さなど、独自の制限が必要です。
