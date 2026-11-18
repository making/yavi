# Jackson

A JSON document is read with a schema in two steps: the parser creates a tree, and an `InputReader` tells the validator how to read its nodes. YAVI does not depend on Jackson; the reader below is the only code that mentions it.

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

The class uses Jackson 3 (`tools.jackson.databind.JsonNode`). Build the validator with `from(JsonNodeReader.INSTANCE, User::new)`, or bind a validator of maps with `readingWith(JsonNodeReader.INSTANCE)`. The reader applies to nested validators too.

For a document whose scalars are all strings, use `JsonNodeReader.INSTANCE.coercingStrings()`; see [Coercing strings](input-json.md#coercing-strings).

## Mapper settings

The mapper decides what the reader sees. A default `JsonMapper` holds a number with a fraction as a `double` and keeps the last of a key written twice. Configure it before parsing:

```java
JsonMapper jsonMapper = JsonMapper.builder()
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS) // 0.10 is the BigDecimal 0.10
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION) // {"role": "user", "role": "admin"} is refused
        .build();
```

The imports are `tools.jackson.databind.json.JsonMapper`, `tools.jackson.databind.DeserializationFeature` and `tools.jackson.core.StreamReadFeature`.

## Duplicate keys and number fidelity

| JSON | Read by a default mapper |
| --- | --- |
| `0.10` | `_bigDecimal` receives `0.1`; the scale is lost |
| `12345678901234567.89` | `_bigDecimal` receives `12345678901234568`; 17 digits are kept |
| `19.990000000000000000001` | `_bigDecimal` receives `19.99`, which `fractionDigits(2)` accepts |
| `1e400` | `_double` receives `Infinity`; `_bigDecimal` reports `object.type` |
| `{"role": "user", "role": "admin"}` | `role` is `admin` |

With `USE_BIG_DECIMAL_FOR_FLOATS`, `_bigDecimal` receives every digit and the scale, and `_double` and `_float` round the number once. With `STRICT_DUPLICATE_DETECTION`, a document with a duplicated key is refused by the parser with a `JacksonException`, before validation.

A number that no target type holds is reported with `object.type`, never with an exception. A parser limit, such as a depth limit, applies before the validator.

## Serializing violations

`violations.details()` returns records that a JSON library serializes as they are:

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

* `ViolationDetail` is a sealed interface of `Simple` and `NoAlternativeMatched`; `switch` over it needs no `default`.
* `params` holds the arguments by name; the violated value is not in a detail. Read `violation.violatedValue()` if the server needs it.
* `ViolationDetail` works with GraalVM native image out of the box.
* `pointer` is what the `errors[].pointer` of an RFC 9457 problem detail holds; `violation.pointer()` gives it for one violation.
* `messagesByName()` is for a form rather than a JSON response.
