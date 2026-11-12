# Support recursive structures with `ValueValidator.lazy`

Difficulty: High

## Problem

A type that refers to itself (`record Comment(String body, List<Comment> replies)`) needs
a validator that refers to itself. Raoh has `lazy(() -> decoder)`, Zod has getters /
`z.lazy`; `docs/ComparisonWithZod.md` lists `z.lazy` as not available.

The workaround in `docs/ComparisonWithRaoh.md` ("Recursive structures") is a lambda with a
raw cast, `lazy.untyped((Class) Map.class)`. Verified behavior:

* a map and `readingParameters()` work,
* `readingWith(JsonNodeReader.INSTANCE)` reports `replies[0]` with `object.type`, because
  the lambda is checked as a JDK `Map`,
* `JsonSchema` emits `"items": {}` for `replies`.

## Goal

```java
static <T, X> ValueValidator<T, X> lazy(Supplier<? extends ValueValidator<T, X>> supplier)
```

* Resolves the supplier on first use (validation, `readingWith`, `description`), so that
  it can be used while the validator it refers to is being built.
* `readingWith(reader)` delegates lazily to the resolved validator; the result must itself
  be lazy, otherwise building the reading recurses forever.
* `description()` must not recurse forever: a `ValueDescription.Reference` (or similar)
  that `JsonSchema` turns into `$defs` / `$ref`.

## Design questions

* Name: `ArgumentsNValidator#lazy()` already exists with another meaning (it returns a
  validator of `Supplier<X>` that defers the creation of the object, used in record
  constructors). A static `ValueValidator.lazy(supplier)` does not clash for the compiler
  but does for the reader; consider `ValueValidator.deferred(supplier)` or
  `ValueValidator.of(supplier)`.

* How a reference is named in the description (`$defs` key): given by the caller
  (`lazy("Comment", supplier)`) or generated.
* `DiscriminatedUnionValidator#isHiddenFrom` and `ArgumentDefinition` call
  `description()` at build time; with a lazy alternative this happens before the supplier
  can be resolved.
* A depth limit against hostile input is the JSON parser's job; document it.
