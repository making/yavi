# JSON ツリー

スキーマは JSON ライブラリのツリーモデルを、Map にコピーせずそのまま読み取ります。`readingWith(reader)` はバリデータを `InputReader` に結び付け、`InputReader` がツリーのノードの読み取り方を示します。スキーマの `from(reader, f)` はそのようなバリデータを構築します。

```java
ValueValidator<JsonNode, User> nodeValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.greaterThanOrEqual(0))
        ._object("address", addressValidator)
        .from(JsonNodeReader.INSTANCE, User::new);
```

YAVI は JSON ライブラリに依存しません。リーダーはツリーモデルごとに一度だけ書く小さなステートレスなクラスで、すべてのバリデータで共有します。ルールと違反は Map の場合と同じです。

## 入力リーダーの実装

ノード型の `CoercibleInputReader` を実装します。[Jackson](integrations-jackson.md#jsonnodereader) に `JsonNode` 用の完全なリーダーがあります。実装するメソッドは次のとおりです。

| メソッド | 返すもの |
| --- | --- |
| `isNull(node)` | ノードを `null` として検証するか |
| `value(node)` | スカラーを JDK 型で。それ以外のノードはそのまま |
| `isObject(node)`, `property(object, name)`, `properties(object)` | オブジェクトのプロパティ |
| `isArray(node)`, `elements(array)` | 配列の要素 |
| `value(node, type)` | `type` として検査する前の値。独自の形式を読むにはオーバーライドする |

リーダーはネストしたバリデータにも適用されるため、ネストしたバリデータは最終的に読む入力に関係なく `fromMap` で構築でき、Map のバリデータもノードを読み取れます（`mapValidator.readingWith(reader)`）。

リーダーはツリーが保持しているとおりに数値を返します。パーサーが落とした情報をバリデータが復元することはできません。

## JDK の JSON API（JEP 540）

JDK は JSON API をインキュベートしています（JDK 28 の [JEP 540](https://openjdk.org/jeps/540)）。独自のツリーモデルを提供するため、JSON ライブラリなしでリーダーを書けます。

```java
@Override
public Object value(JsonValue node) {
    return switch (node) {
        case JsonString string -> string.asString();
        case JsonNumber number -> new BigDecimal(number.toString()); // the text of the number, so that no digit is lost
        case JsonBoolean bool -> bool.asBoolean();
        default -> node;
    };
}
```

その他のメソッドは `JsonObject.asMap()` と `JsonArray.asList()` を使います。完全な実装は YAVI のテストに [`JdkJsonReader.java`](https://github.com/making/yavi/blob/7cd63eb24c23b2569e67f0a3e4f8ea1b83adc38e/src/test/java28/am/ik/yavi/input/JdkJsonReader.java) としてあります。このモジュールは JDK 28 のインキュベータであるため、別のソースルート（`src/test/java28`）に置かれています。それ以前の JDK では上記の Jackson のリーダーを使ってください。隣の [`JdkJsonWriter.java`](https://github.com/making/yavi/blob/develop/src/test/java28/am/ik/yavi/input/JdkJsonWriter.java) は、このモデルを `OutputWriter` で書きます。同じ理由で、数値は `JsonNumber.of(number.toString())` で書きます。

## 文字列の変換

スカラーがすべて文字列のツリー（数値が文字列で送られる JSON 文書など）は、そのリーダーの `coercingStrings()` で読み取ります。

```java
ValueValidator<JsonNode, User> validator = userValidator
        .readingWith(JsonNodeReader.INSTANCE.coercingStrings());

ValueValidator<Object, Integer> page = Yavi._integer("page", c -> c.greaterThanOrEqual(1))
        .readingWith(InputReader.ofJdkTypes().coercingStrings());
```

文字列は、値を表すテキストとして[リクエストパラメータとフォーム](input-parameters.md#coercion-and-stringcoercion)のルールで扱われます。人が書いたテキストには `StringCoercion.strict().trimming().booleanWords()` でルールを緩められます。文字列を変換するリーダーは `InputReader` のみであるため、ルールは一度だけ与えられ、同じテキストが二度変換されることはありません。

## 制限と忠実性

* リーダーはツリーのビューです。ネストしたオブジェクト、配列、Map のキーはノードから読み取られ、`_embedded` はプロパティと同じノードを読み取ります。
* パーサーの制限は検証より前に適用されます。深さの制限、最大桁数、重複キーの厳格な検出はマッパーで設定します。
* 対象の型に収まらない数値は `object.type` で報告され、例外にはなりません。
* 存在しないプロパティと `null` ノードは異なります。`isNull` は後者のためのもので、存在しないプロパティは `property(object, name)` が `null` を返すことで判別します。
* 違反はノードではなく、リーダーが返した値を保持します。
* JSON ライブラリのツリーは読み取り専用です。出力には[オブジェクトの書き戻し](writing-back.md)を使います。
