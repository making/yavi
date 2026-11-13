# Read `UUID` and `URI` values from their string form

Difficulty: Medium

## Problem

A temporal property is read from its ISO-8601 string (`UntypedInput.PARSERS`), so
`_localDate("since")` produces a `LocalDate` from a map, a JSON tree and request
parameters. `UUID` and `URI` have a single string form too, but are not read:

```java
// Raoh
field("id", string().uuid())   // UUID
field("site", string().url())  // URI

// YAVI
._string(Yavi._string("id", c -> c.uuid()).map(UUID::fromString))
._string(Yavi._string("site", c -> c.url()).map(URI::create))
```

The YAVI form states the format twice (the constraint and the conversion), the
description says `string`, and `_object("id", UUID.class)` accepts only a `UUID`
instance.

## First step: decide the entry point

* `_object(name, UUID.class)` / `Yavi._object(UUID.class)` read the string form, by
  adding the types to the parsers of `UntypedInput` and `StringCoercion`. No new
  generated methods. Mismatch is `object.type` (`must be of type UUID`), as for a date.
* Or dedicated `_uuid(name)` / `_uri(name)` in `YaviSchema` and `Yavi`, at the price of
  two more methods per arity in the generated code.

Prefer the first unless constraints specific to the type are wanted.

## Scope

- `UUID` and `URI`; consider `Duration` and `Period` (ISO-8601) in the same change.
  `UUID.fromString` accepts forms that are not canonical (`1-1-1-1-1`); check the
  canonical form before parsing.
- `JsonSchema`: `"type": "string"` with `"format": "uuid"` / `"uri"` / `"duration"`.
- `docs/ComparisonWithRaoh.md` (the `uuid()` and `url()` rows, the domain primitive
  example), `docs/ComparisonWithZod.md`, `docs/NewFeatures_0.18.0.md` and its Japanese
  version.
