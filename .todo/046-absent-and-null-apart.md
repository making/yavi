# Decide whether a property can tell an absent key from `null` without `presence()`

Difficulty: Medium

## Problem

An absent key is validated like `null`, so `nullable()`, `optional()` and
`defaultValue(...)` accept both. Raoh and Zod keep the two apart:

| Contract | Raoh | Zod | YAVI |
| --- | --- | --- | --- |
| key required, value may be `null` | `field("x", nullable(dec))` | `z.string().nullable()` | not expressible: `nullable()` also accepts an absent key |
| key may be absent, `null` rejected | `optionalField("x", dec)` | `z.string().optional()` | `presence()` only, which changes the type to `Presence<X>`; `optional()` also accepts `null` |

The first row is the one with no counterpart. It is a common API contract
(`"deletedAt": null` must be sent), and the JSON Schema of a `nullable()` property cannot
say `required` with `"type": ["string", "null"]`. The workaround is
`.nullable().presence()` plus a `constraintOnTarget` that rejects `Absent`.

## First step: decide

Options:

* A modifier that keeps the key required, e.g. `_string("x").nullable().required()`:
  an absent key is `object.required`, `null` is accepted, the value stays `@Nullable X`,
  and the JSON Schema lists the property under `required`.
* A strict `optional()` variant that rejects an explicit `null` and still produces
  `Optional<X>` (the second row without `Presence`).
* Keep the rule (absent is `null`) and document the workaround in
  `docs/ComparisonWithRaoh.md` and `docs/ComparisonWithZod.md`.

Request parameters and typed arguments (`apply`) have no absent state; say what the
modifier means there (probably nothing: an absent parameter is `null`).

## Scope if accepted

- The modifier in `YaviSchema` and the generated builders (`scripts/generate-args.sh`).
- `ValueDescription` and `JsonSchema` (`required`).
- Both comparison documents, `docs/NewFeatures_0.18.0.md` and its Japanese version.
