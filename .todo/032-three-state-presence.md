# Tell an absent key from an explicit `null`

Difficulty: High

## Problem

An absent key is validated like `null`, so `optional()`, `nullable()` and
`defaultValue(...)` cannot tell "leave unchanged" from "clear the value" in a PATCH
request (JSON Merge Patch). Raoh has `optionalNullableField(name, decoder)` returning
`Presence<T>` (`Present(value)`, `PresentNull`, `Absent`); Zod has
`.nullable().optional()`. `docs/ComparisonWithRaoh.md` and `docs/ComparisonWithZod.md`
list this as a difference.

`MapInputValidator.Reading` already knows the difference (`present`), but uses it only to
choose between `object.required` and the violations for `null`.

## Goal

An argument modifier that produces a three-state value, for a map, every `InputReader` and
request parameters:

```java
record ProfilePatch(Presence<String> nickname, Presence<String> bio) {}

Yavi.arguments()
    ._string("nickname", c -> c.lessThanOrEqual(50)).presence()
    ._string("bio", c -> c.lessThanOrEqual(500)).presence()
    .fromMap(ProfilePatch::new);
```

## Design questions

* The type: a sealed interface with records in `am.ik.yavi.core` (name: `Presence`?), with
  helpers such as `ifPresent`, `orElse`.
* How absence reaches the argument: a `ValueValidator` receives `null` for both. Options
  are a sentinel passed by `MapInputValidator`, or a second method on the argument
  definition (`validateAbsent`). It must not leak to user validators.
* What it means outside a map (`apply(f)` with typed arguments, `ValueValidator#presence()`
  on its own): probably available on the builders only, and `apply` treats `null` as
  `PresentNull` or is refused.
* Request parameters: an empty string is `null` today; decide whether `nickname=` is
  `PresentNull`.
* `ValueDescription` / JSON Schema: not required and nullable.
