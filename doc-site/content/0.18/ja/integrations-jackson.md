# Jackson

JSON 文書は 2 つの手順でスキーマを使って読み取ります。パーサーがツリーを生成し、`InputReader` がバリデータにノードの読み取り方を伝えます。YAVI は Jackson に依存せず、以下のリーダーだけが Jackson に言及します。

## JsonNodeReader

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

このクラスは Jackson 3（`tools.jackson.databind.JsonNode`）を使います。バリデータは `from(JsonNodeReader.INSTANCE, User::new)` で構築するか、Map のバリデータを `readingWith(JsonNodeReader.INSTANCE)` で結び付けます。リーダーはネストしたバリデータにも適用されます。

スカラーがすべて文字列の文書には `JsonNodeReader.INSTANCE.coercingStrings()` を使います。[文字列の変換](input-json.md#coercing-strings)を参照してください。

## マッパーの設定

マッパーがリーダーの見るものを決めます。デフォルトの `JsonMapper` は小数を含む数値を `double` で保持し、二度書かれたキーの最後の値を残します。パースの前に設定します。

```java
JsonMapper jsonMapper = JsonMapper.builder()
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS) // 0.10 is the BigDecimal 0.10
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION) // {"role": "user", "role": "admin"} is refused
        .build();
```

インポートは `tools.jackson.databind.json.JsonMapper`、`tools.jackson.databind.DeserializationFeature`、`tools.jackson.core.StreamReadFeature` です。

## 重複キーと数値の忠実性

| JSON | デフォルトのマッパーでの読み取り |
| --- | --- |
| `0.10` | `_bigDecimal` は `0.1` を受け取る。スケールは失われる |
| `12345678901234567.89` | `_bigDecimal` は `12345678901234568` を受け取る。17 桁だけが保たれる |
| `19.990000000000000000001` | `_bigDecimal` は `19.99` を受け取り、`fractionDigits(2)` が受け入れる |
| `1e400` | `_double` は `Infinity` を受け取る。`_bigDecimal` は `object.type` を報告する |
| `{"role": "user", "role": "admin"}` | `role` は `admin` になる |

`USE_BIG_DECIMAL_FOR_FLOATS` により `_bigDecimal` はすべての桁とスケールを受け取り、`_double` と `_float` は一度だけ丸めます。`STRICT_DUPLICATE_DETECTION` により、重複したキーを持つ文書は検証前にパーサーが `JacksonException` で拒否します。

対象の型に収まらない数値は `object.type` で報告され、例外にはなりません。深さの制限のようなパーサーの制限はバリデータより前に適用されます。

## 違反のシリアライズ

`violations.details()` は JSON ライブラリがそのままシリアライズするレコードを返します。

```java
@PostMapping("/users")
ResponseEntity<?> create(@RequestBody Map<String, Object> request) {
    return switch (userValidator.validate(request)) {
        case Validated.Valid<User>(User user) ->
            ResponseEntity.ok(userService.create(user));
        case Validated.Invalid<User>(ConstraintViolations violations) ->
            ResponseEntity.badRequest().body(violations.details());
    };
}
```

```json
[{"name": "items[1].quantity", "pointer": "/items/1/quantity", "messageKey": "numeric.lessThanOrEqual",
  "message": "\"items[1].quantity\" must be less than or equal to 99", "params": {"max": 99}}]
```

* `ViolationDetail` は `Simple` と `NoAlternativeMatched` の sealed インターフェースです。これに対する `switch` に `default` は不要です。
* `params` は引数を名前で保持します。違反した値は detail に含まれません。サーバーが必要とする場合は `violation.violatedValue()` を読みます。
* `ViolationDetail` は GraalVM ネイティブイメージでそのまま動作します。
* `pointer` は RFC 9457 の problem detail の `errors[].pointer` が保持する値です。1 つの違反については `violation.pointer()` が返します。
* `messagesByName()` は JSON レスポンスではなくフォーム用です。
