# Constraints on the list or map of `_list` and `_map`

Difficulty: Medium

## Problem

`_list(name, elementValidator)` and `_map(name, valueValidator)` validate the elements
only. Raoh has `list(dec).nonempty().minSize(1).maxSize(10).unique()` and
`map(dec).minSize(1)`; Zod has `.min()`, `.max()`, `.nonempty()`. In YAVI the tutorial
needs a second validator with hand-written predicates and message keys
(`docs/ComparisonWithRaoh.md`, "Lists"), although `CollectionConstraint` and
`MapConstraint` already have `notEmpty`, `greaterThanOrEqual`, `lessThanOrEqual`,
`fixedSize`, `unique`, `contains`, `containsKey`.

## Goal

```java
._list("tags", tagValidator, c -> c.notEmpty().lessThanOrEqual(10).unique())
._map("prices", priceValidator, c -> c.notEmpty())
```

* The constraints apply to the validated list (the produced elements), only when every
  element is valid, and are reported on `name`.
* Also on a lifted validator, for a list that is not an argument:
  `validator.liftList(c -> c.notEmpty())` or an equivalent.
* The constraints are described (`ValueDescription.Elements` / `Entries` carry
  constraints) and `JsonSchema` emits `minItems`, `maxItems`, `uniqueItems`,
  `minProperties`.

## Notes

* `ArgumentDefinition.list` / `map` and the generated builders (template under `scripts/`).
* Replace the `tagsValidator` example in `docs/ComparisonWithRaoh.md` and add
  `z.array(...).min()` to `docs/ComparisonWithZod.md`.
