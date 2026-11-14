# A list whose elements are unique by a key

Difficulty: Low

## Problem

Found by writing the Raoh tutorial with YAVI (section 21 of
`docs/RaohTutorialInYavi.md`). `unique()` of a collection compares the elements. A list
of objects is usually unique by one property (the id of a product, the code of a line),
which needs a hand-written step:

```java
._object("products", productValidator.liftList().andThen((products, locale, context) -> { ... }))
```

or a `constraintOnTarget` that cannot name the duplicates.

## Scope

* `CollectionConstraint#unique(Function<? super E, ?> key)`: violated if two elements
  have the same key, with the duplicated keys in the message and in `params`
  (`duplicates`), as `unique()` has them.
* `_list("products", productValidator, c -> c.unique(Product::id))` then covers the
  scenario; shorten section 21 of the tutorial and the same scenario of
  `docs/ComparisonWithRaoh.md`.
* The JSON Schema has no keyword for it: `uniqueItems` is not written for this
  constraint.
