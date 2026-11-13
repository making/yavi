# Validate the elements of a text that is split

Difficulty: Medium

## Problem

One request parameter often holds several values: `?tags=java,yavi`, `?ids=1,2,3`, one
URI per line in a `textarea`. `readingParameters()` reads a list only from a repeated key
(`tags=a&tags=b`) or from indexed keys. Splitting by hand loses the location of the
violations and the coercion of the elements:

```java
record Article(String title, List<String> tags) {}

ValueValidator<String, List<String>> tags = Yavi._string("tags", c -> c.notBlank())
        .map(text -> Arrays.asList(text.split(",")))
        .andThen(Yavi._string(c -> c.notBlank().lessThanOrEqual(5)).liftList());

ValueValidator<Map<String, String>, Article> articleValidator = Yavi.schema()
        ._string("title", c -> c.notBlank())
        ._string(tags)
        .fromMap(Article::new)
        .readingParameters();

articleValidator.validate(Map.of("title", "t", "tags", "java,,toolongtag"));
// "[1]" must not be blank                 name: [1], path: [Index[index=1]]
// The size of "[2]" must be ...           name: [2]
```

The same elements sent as `tags=java&tags=&tags=toolongtag` to
`._list("tags", Yavi._string(...))` are reported as `tags[1]` and `tags[2]`.

* The name of `tags` is lost: `andThen` reports the violations of the next validator as
  they are. The workaround is `...liftList().prefixed("tags")`, which repeats the name.
* `?ids=1,2,3` cannot be written this way at all: the elements are strings, and
  `Yavi._integer()` takes an `Integer`. Coercion happens only in the reader.
* `andThen(ValueValidator<? super X, X2>)` is invariant in `X2`, so
  `andThen(v.liftCollection(LinkedHashSet::new))` does not compile where a
  `ValueValidator<String, Set<String>>` is expected once another call follows it in the
  chain; an explicit `.<Set<String>>liftCollection(...)` is needed.

## First step: decide the shape

1. A modifier of a list property that reads one text:
   `._list("tags", Yavi._string(c -> ...)).splitBy(",")` (and a pattern for lines). The
   reader splits, so the elements are coerced (`_list("ids", Yavi._integer())`) and
   located (`tags[1]`) as those of a repeated key. Decide whether a repeated key is still
   accepted, whether elements are trimmed and whether empty elements are dropped.
2. An option of the reading: `readingParameters(StringCoercion.strict().splittingLists(","))`.
   Applies to every list, which is wrong for a list whose values may hold the separator.
3. Only fix the location: `andThen` locates the violations of the next validator under
   the name of this one. Changes the names of existing `andThen` chains; check
   `docs/BreakingChanges_0.18.0.md`. Does not give coercion.

Describe the property in `description()` and `JsonSchema` (a string, with the items as
far as JSON Schema can say).

## If accepted

* Extend the tests of `readingParameters()`; no new test class.
* Document it in `README.md` and `docs/NewFeatures_0.18.0.md` (Validating request
  parameters, the keys).
