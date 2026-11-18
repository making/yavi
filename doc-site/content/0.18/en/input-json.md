# JSON Trees

A schema reads the tree model of a JSON library as it is, without copying it to a map. `readingWith(reader)` binds a validator to an `InputReader`, which tells how to read the nodes of the tree, and `from(reader, f)` on the schema builds such a validator.

```java
ValueValidator<JsonNode, User> nodeValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.greaterThanOrEqual(0))
        ._object("address", addressValidator)
        .from(JsonNodeReader.INSTANCE, User::new);
```

YAVI has no dependency on a JSON library. A reader is a small stateless class written once per tree model and shared by every validator, and the rules and violations are those of a map.

## Implementing an input reader

Implement `CoercibleInputReader` of the node type. `coercingStrings()` then reads a tree whose scalars are strings.

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

The class above uses Jackson 3 (`tools.jackson.databind.JsonNode`). The reader applies to nested validators too, so a nested validator is built with `fromMap` whatever input is read in the end, and a validator of maps also reads nodes: `mapValidator.readingWith(JsonNodeReader.INSTANCE)`.

| Method | What it returns |
| --- | --- |
| `isNull(node)` | Whether the node is validated like `null` |
| `value(node)` | A scalar as a JDK type; any other node as it is |
| `isObject(node)`, `property(object, name)`, `properties(object)` | The properties of an object |
| `isArray(node)`, `elements(array)` | The elements of an array |
| `value(node, type)` | The value before it is checked as `type`; override it to read a format of your own |

A reader returns a number as the tree holds it. A validator cannot recover what the parser dropped.

## Mapper configuration

A default `JsonMapper` holds a number with a fraction as a `double` and keeps the last of a key written twice:

| JSON | Read by a default mapper |
| --- | --- |
| `0.10` | `_bigDecimal` receives `0.1`; the scale is lost |
| `12345678901234567.89` | `_bigDecimal` receives `12345678901234568`; 17 digits are kept |
| `19.990000000000000000001` | `_bigDecimal` receives `19.99`, which `fractionDigits(2)` accepts |
| `1e400` | `_double` receives `Infinity`; `_bigDecimal` reports `object.type` |
| `{"role": "user", "role": "admin"}` | `role` is `admin` |

Tell the mapper to keep a number as written and to refuse a key written twice:

```java
JsonMapper jsonMapper = JsonMapper.builder()
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS) // 0.10 is the BigDecimal 0.10
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION) // {"role": "user", "role": "admin"} is refused
        .build();
```

`_bigDecimal` then receives every digit and the scale, `_double` and `_float` round the number once, and `1e400` is not a `Double`. A document with a duplicated key is refused by the parser with a `JacksonException`, before validation.

The same settings apply to `readValue(json, Map.class)` when the validator reads a map instead of a tree.

## Coercing strings

A tree whose scalars are all strings, such as a JSON document whose numbers are sent as strings, is read with `coercingStrings()` of its reader:

```java
ValueValidator<JsonNode, User> validator = userValidator
        .readingWith(JsonNodeReader.INSTANCE.coercingStrings());

ValueValidator<Object, Integer> page = Yavi._integer("page", c -> c.greaterThanOrEqual(1))
        .readingWith(InputReader.ofJdkTypes().coercingStrings());
```

The strings are taken as text that stands for a value, by the rules of [Request Parameters and Forms](input-parameters.md#coercion-and-stringcoercion). `StringCoercion.strict().trimming().booleanWords()` relaxes them for text written by a person. A reader that coerces strings is an `InputReader` only, so the rules are given once, and no text is coerced twice.

## Limits and fidelity

* A reader is a view of the tree: nested objects, arrays and the keys of a map are read from the nodes, and `_embedded` reads the same node as the properties.
* The parser's limits apply before validation: a depth limit, the maximum number of digits, and strict duplicate detection are configured on the mapper.
* A number that no target type holds is reported with `object.type`, never with an exception.
* An absent property and a `null` node are different: `isNull` is for the latter, and an absent property is told by `property(object, name)` returning `null`.
* A violation holds the value as the reader returned it, not the node.
* The tree of a JSON library is read, not written. Use [Writing an Object Back](writing-back.md) for output.
