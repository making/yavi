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

Implement `CoercibleInputReader` of the node type. [Jackson](integrations-jackson.md#jsonnodereader) shows a complete reader for its `JsonNode`; the methods it implements are:

| Method | What it returns |
| --- | --- |
| `isNull(node)` | Whether the node is validated like `null` |
| `value(node)` | A scalar as a JDK type; any other node as it is |
| `isObject(node)`, `property(object, name)`, `properties(object)` | The properties of an object |
| `isArray(node)`, `elements(array)` | The elements of an array |
| `value(node, type)` | The value before it is checked as `type`; override it to read a format of your own |

The reader applies to nested validators too, so a nested validator is built with `fromMap` whatever input is read in the end, and a validator of maps also reads nodes: `mapValidator.readingWith(reader)`.

A reader returns a number as the tree holds it. A validator cannot recover what the parser dropped.

## The JDK JSON API (JEP 540)

The JDK incubates a JSON API, [JEP 540](https://openjdk.org/jeps/540) in JDK 28. It provides a tree model of its own, so a reader can be written without a JSON library:

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

The other methods use `JsonObject.asMap()` and `JsonArray.asList()`. YAVI's test suite contains the complete implementation, [`JdkJsonReader.java`](https://github.com/making/yavi/blob/7cd63eb24c23b2569e67f0a3e4f8ea1b83adc38e/src/test/java28/am/ik/yavi/input/JdkJsonReader.java). It lives in a separate source root (`src/test/java28`) because the module is an incubator of JDK 28; on an earlier JDK, use the Jackson reader above.

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
* The parser's limits apply before validation: a depth limit, the maximum number of digits and strict duplicate detection are configured on the parser; see [Jackson](integrations-jackson.md#mapper-settings).
* A number that no target type holds is reported with `object.type`, never with an exception.
* An absent property and a `null` node are different: `isNull` is for the latter, and an absent property is told by `property(object, name)` returning `null`.
* A violation holds the value as the reader returned it, not the node.
* The tree of a JSON library is read, not written. Use [Writing an Object Back](writing-back.md) for output.
