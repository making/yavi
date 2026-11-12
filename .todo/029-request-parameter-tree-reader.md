# Read multi-valued and nested request parameters as a tree

Difficulty: Medium

## Background

027 added `InputReader#coercingStrings()`: a map of strings such as
`@RequestParam Map<String, String>` is validated with the arguments of `fromMap`, a
string being read as the type of its argument. It coerces scalars only; the shape of the
tree is left to the reader it decorates, and the reader of JDK types takes the map as it
is. Request parameters and submitted forms are flat and may be multi-valued, so the
following does not work yet:

- `MultiValueMap<String, String>` (`Map<String, List<String>>`): every value is a list,
  so a scalar argument is reported with `object.type`. With a `Map<String, String>`, a
  `_list` argument is reported because its value is a string, not a list.
- Nested keys: `address.street=...`, `tags[0]=...`, `items[0].name=...` are top-level keys,
  so `_object("address", ...)` reports `address` as required.

## Goal

A reader (an `InputReader`, composed with `coercingStrings()`) that presents a flat map of
parameters as a tree, so that `_object`, `_list` and `_map` arguments are read from
request parameters:

```java
ValueValidator<Object, SearchForm> validator = Yavi.arguments()
    ._string("q")
    ._list("tags", StringValidatorBuilder.of("", c -> c.notBlank()).build())
    ._object("address", addressValidator)
    .from(<reader of parameters>.coercingStrings(), SearchForm::new);
```

## Design questions to settle

- Multi-values: a list of one element where a scalar is expected is its element; what a
  list of several elements is for a scalar argument (a mismatch, or the first value as
  Spring does); whether a single string is a list of one element for `_list`. A reader
  does not know the expected shape when `isArray` is called; check whether
  `InputReader#value(node, type)` is enough or the reader needs to be told the shape.
- The syntax of nested keys (`a.b`, `a[0]`, `a[b]`, `a[]`), sparse and unordered indexes,
  and a limit on the index so that `tags[999999999]` allocates nothing.
- How `rejectingUnknownProperties()` reports a key that belongs to no argument
  (`address.zip` under `address`).
- The nodes of the tree are views of a prefix of the map; avoid scanning all keys for
  every property.
- No dependency on Spring: `MultiValueMap` is a `Map<String, List<String>>`.

## Notes

- `core` depends only on `fn` and `message`, and `input` only on `core`.
- Test it in `MapInputValidatorTest` as another `Model` if the same inputs can be
  flattened, instead of adding a parallel test class.
