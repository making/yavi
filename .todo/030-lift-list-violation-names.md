# Locate the violations of `liftList()` elements under the index

Difficulty: Medium

## Problem

`validator.liftList()` (and `liftSet`, `liftCollection`) calls `indexed(index)`, which
appends `[i]` to the name of the violation. For an element that has properties the index
ends up after the property:

```java
orderValidator.liftList().prefixed("orders").validate(rows);
// orders.order_id[1]   path: [Property[orders], Property[order_id], Index[1]]
```

`_list("orders", orderValidator)` and
`Validated.traverseIndexed(rows, (row, i) -> orderValidator.validate(row).prefixed(i)).prefixed("orders")`
report `orders[1].order_id`. `docs/ComparisonWithRaoh.md` therefore tells not to use
`liftList()` for elements with a name (`Result.traverse` of Raoh).

## Goal

`liftList()` of a validator reports `[1].order_id`, so that `prefixed("orders")` gives
`orders[1].order_id` in `name()` and `path()`, for typed input and for `readingWith`.

## Design questions

* `indexed(int)` is public since 0.8.0 and `tag[0]` is what a named single-value validator
  reports today (`StringValidatorBuilder.of("tag", ...).build().liftList()`). Decide whether
  0.18.0 changes that to `[0].tag`, or keeps `indexed` and makes only the `lift*` methods
  use `prefixed(int)`. An element with an empty name is `[0]` either way.
* `ValueValidator#untypedCollection` uses `indexed` too; keep both paths consistent.

## Done when

* The `liftList` sentence in "Several results and several rows" of
  `docs/ComparisonWithRaoh.md` is replaced by the `liftList().prefixed(...)` form.
