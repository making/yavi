# Add a `recover` combinator that replaces any failure with a fallback

Difficulty: Medium

## Problem

Raoh's `recover(decoder, fallback)` and Zod's `.catch(value)` absorb any failure, for
parameters that must not break a request (`size=9999` falls back to `20`). YAVI has only
`defaultValue`, which applies to `null`.

The workaround in `docs/ComparisonWithRaoh.md` ("Defaults and recovery") is a lambda over
`validator.untyped()` given back its type with `untyped(Object.class)`. It is wrong with
any reader other than the JDK one: with `readingParameters()` the string `"50"` is checked
without coercion, fails, and is silently replaced with the fallback. Verified.

## Goal

```java
// on every ValueValidator
default ValueValidator<T, X> recover(X fallback)

// and on the builders, like defaultValue
._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
```

* Any failure, including a type mismatch reported by the reader and `null`, produces the
  fallback.
* It carries the input type (`readingWith`) and the description like `defaultValue` does
  (`ValueDescription.Optional` with the default; JSON Schema `default`).
* An absent key of a `MapInputValidator` gives the fallback, not `object.required`.

## Design questions

* Name: `recover` (Raoh), `catching`/`orElse` (Zod `.catch`). `catch` is a keyword.
* A variant that receives the violations (`Function<ConstraintViolations, X>`) for logging.

## Done when

* The lambda in "Defaults and recovery" and the note in "Pagination and query parameters"
  of `docs/ComparisonWithRaoh.md` are replaced, and `.catch` is added to
  `docs/ComparisonWithZod.md`.
