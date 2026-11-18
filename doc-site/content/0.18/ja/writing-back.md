# オブジェクトの書き戻し

スキーマは入力のプロパティを一度だけ命名します。`Yavi.reversibleSchema()` は、生成したオブジェクトを読み取り元の Map として書き戻すバリデータを構築します。ステートメントのパラメータや、保存済みオブジェクトを表示するフォームのために名前を書き直す必要はありません。

```java
record Employee(Integer id, UserName name, Email email, Optional<String> nickname, Role role) {}

ObjectInputValidator.Reversible<Employee> employeeValidator = Yavi.reversibleSchema()
        ._integer("id")
        ._embedded(userNameValidator) // first_name, last_name
        ._string(Yavi._string("email", c -> c.notBlank().email()).map(Email::new, Email::value))
        ._string("nickname").optional()
        ._enum("role", Role.class)
        .fromMapReversible(Employee::new, Employee::id, Employee::name, Employee::email,
                Employee::nickname, Employee::role);

employeeValidator.toMap(employee);
// {id=1, first_name=Taro, last_name=Yamada, email=taro@example.com, nickname=null, role=ADMIN}
```

## 可逆スキーマ

`Yavi.reversibleSchema()` は `Yavi.schema()` と同じメソッドを持ちますが、スキーマが `ValueValidator` を取る箇所で `ReversibleValueValidator` を取ります。これは型によって逆変換を知っているバリデータです。逆変換を指定せずに値をマップするバリデータは、追加された箇所でコンパイルできません。

* `fromMapReversible(f, accessor1, ..., accessorN)` は `fromMap(f)` に引数ごとのアクセサを加えたもので、オブジェクトからその引数の検証済みの値を返します。
* `map(f, g)` は `map(f)` に、マップされた値から入力へ戻る関数を加えたものです。`ReversibleValueValidator` を返し、その `reverse(x)` は `x` の読み取り元の入力です。
* `flatFromMapReversible` と `flatOfReversible` は `flatFromMap` と `of` の対応物です。
* `ReversibleValueValidator.recursive(self -> ...)` は、自身を含む構造のための `recursive` です。
* `Yavi.reversibleDynamicSchema()` は検証済みの値の Map を書き戻します。
* `ObjectInputValidator.ofReversible(parts, mapper)` は `ReversiblePart` のリスト、すなわち逆変換とアクセサを持つパートを取ります。

`toMap(x)` はオブジェクトを書き、`reverse(x)` は withheld のプロパティも含めてすべてを書きます。バリデータは `fromMap` と同じように入力を読み取り、これまでどおりネスト、埋め込み、`readingWith`、`readingParameters` が可能です。

## ネストした値と省略可能な値の書き込み

| プロパティ | `toMap` |
| --- | --- |
| 制約、`defaultValue`、`recover` の値 | その値 |
| enum 定数 | その名前 |
| `nullable()`、`optional()` の値なし | 名前付きパラメータにはキーが必要なため `null` のエントリ |
| `rejectingNull()` の後の値なし | エントリなし |
| `presence()` | `Absent` はエントリなし、`PresentNull` は `null` |
| `_embedded` | 同じ Map 内。値がなければすべてのプロパティが `null` |
| `_object` | ネストした Map |
| `_list`、`_map`、`_rest` | すべての要素 |
| `splitBy(",")` | 結合したテキスト。空のリストは `null` |
| `map(f, g)` のバリデータ | `g` の結果 |
| `withheld()` | エントリなし |

`splitBy` を付けたリストはフォームの 1 つのフィールドであるため、そのフィールドのテキストとして書かれます。パターンは要素を区切るテキストを伝えないため、要素を結合するテキストを一緒に与えます。

```java
ObjectInputValidator.Reversible<Feed> feedSchema = Yavi.reversibleSchema()
        ._string("title")
        ._list("urls", Yavi._string(c -> c.url())).splitBy(Pattern.compile("\\s*\\R\\s*"), "\n")
        .defaultValue(List.of())
        .fromMapReversible(Feed::new, Feed::title, Feed::urls);

feedSchema.toMap(new Feed("News", List.of("https://a.example", "https://b.example")));
// {title=News, urls="https://a.example\nhttps://b.example"}
```

`Yavi.reversibleSchema()` では、結合するテキストのない `splitBy(pattern)` はコンパイルできません。パターンはテキストに一致しなければなりません。`splitBy(Pattern.compile("\\R"), ", ")` は、書かれたテキストが結合した箇所で分割されないため、スキーマの構築時に失敗します。

## ビュー

`including` と `excluding` は、選んだプロパティを書く `PropertyView` を構築します。ビューは一度構築され、その名前は構築時にバリデータが記述するものと照合されます。バリデータが定義しない名前は `IllegalArgumentException` になりますが、`toMap` が返す Map から削除したキーは誰も検査しません。

```java
static final PropertyView<AccountUser> SUMMARY = userValidator.including("id", "name", "address.city");
static final PropertyView<AccountUser> LISTED = userValidator.excluding("address");

SUMMARY.toMap(user); // {id=1, name=Taro, address={city=Tokyo}}
LISTED.toMap(user);  // {id=1, name=Taro}
```

* ビューにないプロパティはエントリを持ちません。`null` として書かれることはありません。書かれないプロパティのアクセサは呼ばれません。
* ビューは書き込み専用です。バリデータでも `ReversibleValueValidator` でもないため、往復が必要な箇所では拒否されます。
* ビューは自身の `including` と `excluding` で狭められます。
* `renaming(function)` はすべての深さでプロパティを別のキーに書き、`renaming(path, key)` は 1 つのプロパティを改名します。変わるのはキーだけです。改名されたビューはレスポンス用であり、読み戻されたり JSON Schema に記述されたりしません。

## withheld のプロパティ

表示してはならないプロパティは、定義された箇所で印を付けます。

```java
._string("password_hash").withheld()
```

`toMap` と除外するビューは、どの深さでもこれを書きません。含めるビューは、名前が指定された場合にだけ書きます。`reverse` はすべてのプロパティを書くため、往復が成立します。同じ印は、パスワードのために送信された値を再表示するフォームから除外し、JSON Schema では `writeOnly` にします。読み取りは変えず、違反は拒否された値を保持します。

パスワードは多くの場合、アプリケーションが計算するハッシュなど別の形で保存されます。フォームのスキーマはパスワードを withheld にし、ステートメントにはハッシュを追加のパラメータとして与えます。

## パラメータとフォーム

`ParameterTree` はスキーマの形でパラメータを返し、`ParameterTree.ofValues` はバリデータが書いた値をパラメータのテキストとして保持します。これにより、保存済みオブジェクトの初回表示と、再表示されるフォームは同じコード経路になります。

```java
ParameterTree tree = stored != null ? ParameterTree.ofValues(signupSchema.toMap(stored))
        : ParameterTree.of(request.getParameterMap());
Map<String, Object> form = tree.toMap(signupSchema.description());
// {name="Taro", age="30", birthday="2000-01-02", address={city="Tokyo", street=""}}
```

* `ParameterTree.of(parameters)` は `readingParameters()` が検証する形式の `Map<String, String>`、`Map<String, List<String>>`、`Map<String, String[]>` を取ります。
* `toMap(description)` はテンプレートエンジン用に、`Map`、`List`、送信されたテキストのツリーを返します。`toParameters(description)` は同じ値を違反の名前（`address.city`、`items[0].name`）の下で返し、Spring の `MapBindingResult` に使います。
* 値はトリム、変換、検証されません。送信されなかった値は空文字列です。送信されなかった `Boolean` はエントリを持ちません。
* どのプロパティにも定義されていないキーは除外されます。`_rest` を持つスキーマは他のキーを返します。
* withheld のプロパティは、送信されなかったプロパティとして書かれます。
* `ParameterTree.parametersOf(tree)` は、任意の `Map` と `List` のツリーに対する `ParameterTree.of(parameters)` の逆です。
* `ParameterTree.of(parameters, maxIndex)` は別のインデックス制限を与えます。`ParameterTree.DEFAULT_MAX_INDEX` は 255 です。

## 往復

`validator.validate(validator.toMap(x))` は、バリデータが生成するすべての `x` について `Valid(x)` です。`reverse` は withheld のプロパティも書くため、往復が成立します。逆変換のない `map(f)`、ラムダ、`anyOf` など `ReversibleValueValidator` でないバリデータは、コンパイル時にスキーマが拒否します。

Map は新しく生成されます。ステートメントに必要なものは追加します（`map.put("updated_by", user)`）。ネストした Map はステートメントに束縛できないため、行の列は `_embedded` で読み取るか、`ParameterTree.parametersOf` で違反の名前の下に束縛します。
