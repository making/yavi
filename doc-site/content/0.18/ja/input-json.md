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

ノード型の `CoercibleInputReader` を実装します。`coercingStrings()` は、スカラーがすべて文字列のツリーを読み取れるようにします。

```java
import java.util.Map;

import am.ik.yavi.core.CoercibleInputReader;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

public final class JsonNodeReader implements CoercibleInputReader<JsonNode> {

    public static final JsonNodeReader INSTANCE = new JsonNodeReader();

    @Override
    public boolean isNull(JsonNode node) {
        return node.isNull() || node.isMissingNode();
    }

    @Override
    public Object value(JsonNode node) { // a scalar as a JDK type
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
        return object.get(name); // null if absent
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

上記のクラスは Jackson 3（`tools.jackson.databind.JsonNode`）を使います。リーダーはネストしたバリデータにも適用されるため、ネストしたバリデータは最終的に読む入力に関係なく `fromMap` で構築でき、Map のバリデータもノードを読み取れます（`mapValidator.readingWith(JsonNodeReader.INSTANCE)`）。

| メソッド | 返すもの |
| --- | --- |
| `isNull(node)` | ノードを `null` として検証するか |
| `value(node)` | スカラーを JDK 型で。それ以外のノードはそのまま |
| `isObject(node)`, `property(object, name)`, `properties(object)` | オブジェクトのプロパティ |
| `isArray(node)`, `elements(array)` | 配列の要素 |
| `value(node, type)` | `type` として検査する前の値。独自の形式を読むにはオーバーライドする |

リーダーはツリーが保持しているとおりに数値を返します。パーサーが落とした情報をバリデータが復元することはできません。

## マッパーの設定

デフォルトの `JsonMapper` は小数を含む数値を `double` で保持し、二度書かれたキーの最後の値を残します。

| JSON | デフォルトのマッパーでの読み取り |
| --- | --- |
| `0.10` | `_bigDecimal` は `0.1` を受け取る。スケールは失われる |
| `12345678901234567.89` | `_bigDecimal` は `12345678901234568` を受け取る。17 桁だけが保たれる |
| `19.990000000000000000001` | `_bigDecimal` は `19.99` を受け取り、`fractionDigits(2)` が受け入れる |
| `1e400` | `_double` は `Infinity` を受け取る。`_bigDecimal` は `object.type` を報告する |
| `{"role": "user", "role": "admin"}` | `role` は `admin` になる |

数値を書かれたとおりに保ち、二度書かれたキーを拒否するようにマッパーを設定します。

```java
JsonMapper jsonMapper = JsonMapper.builder()
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS) // 0.10 is the BigDecimal 0.10
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION) // {"role": "user", "role": "admin"} is refused
        .build();
```

これにより `_bigDecimal` はすべての桁とスケールを受け取り、`_double` と `_float` は一度だけ丸め、`1e400` は `Double` になりません。重複したキーを持つ文書は、検証前にパーサーが `JacksonException` で拒否します。

同じ設定は、バリデータがツリーではなく Map を読む場合の `readValue(json, Map.class)` にも適用されます。

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
