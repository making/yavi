# Read a JSON tree model (Jackson JsonNode and others) without copying it to a Map

Difficulty: High

## Background

022 added `MapInputValidator` (`fromMap`), `DiscriminatedUnionValidator` and
`ValueValidator.untyped()`. They read a tree of JDK types: `Map<String, Object>`,
`Iterable`, `String`, `Number`, `Boolean` and `null`. A tree model of a JSON library, such
as Jackson's `JsonNode`, Gson's `JsonElement` or Jakarta's `JsonValue`, is none of those,
so every property is reported with `object.type` when such a node is given as is.

Today the node is converted first, in user code, so YAVI needs no dependency:

```java
ValueValidator<JsonNode, User> nodeValidator = userValidator.untyped()
    .compose(node -> objectMapper.convertValue(node, Object.class));
```

This copies the whole tree once. The goal of this item is to read the nodes directly.

## Constraints

- YAVI must not depend on Jackson or any other JSON library (JSpecify stays the only
  dependency). The adapter for a library lives in user code or in a separate module.
- A new package must not introduce a dependency cycle: `core` depends only on `fn` and
  `message`, and `input` only on `core`.
- Keep the existing `Map` input working and source-compatible; it is the adapter for JDK
  types.

## Proposed shape

Names are placeholders.

- A small interface that tells how to read a node, implemented once per tree model:
  whether a node is an object, an array, a string, a number, a boolean or null; the keys
  and the value for a key of an object; the elements of an array; the scalar value as a
  JDK type.
- The type check of `am.ik.yavi.core.UntypedInput` and the key lookup of
  `MapInputValidator` / `DiscriminatedUnionValidator` go through that interface instead of
  `instanceof Map` / `Iterable` / `Number`.
- The numeric rules (a number is taken whatever its type is as long as the value is kept),
  the enum and ISO-8601 rules and the message keys (`object.type`, `object.required`,
  `object.unknownProperty`, `object.oneOf`) stay as they are.

## Design questions to settle

- How the reader reaches the validators. `untyped()` is called when a validator is built
  and the reader is known when it validates. Options: a parameter of `untyped(...)` /
  `fromMap(...)`, or an input type that carries the reader with the node.
- Whether the input type of `MapInputValidator` stays `Map<String, Object>` with a second
  validator type for nodes, or becomes generic.
- Whether a lazy `Map` / `List` view over the nodes, written in user code, is enough. It
  needs no change in YAVI but unwraps every scalar on access; measure before adding an
  interface.
- Where a Jackson adapter is published: documentation only, or a separate artifact.

## Notes

- Do this only if the copy is a measured problem; the conversion above is the supported
  way until then.
- Verify with the Jackson version in use that `convertValue(node, Object.class)` expands
  a `JsonNode` to `Map` / `List` and does not return the node itself, and document the
  result in the README section "Validating a map such as a parsed JSON object".
