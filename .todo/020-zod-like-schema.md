# Add a Zod-like schema layer

Difficulty: High

## Goal

Give YAVI a schema API close to Zod's `safeParse` workflow: a composable schema value
that parses untrusted input, returns a discriminated result that can be handled with
pattern matching, and reports structured issues with a path and a code.

Today the constraints are richer than Zod's, but:

- `Validated` is a class with `isValid`/`value`/`errors`, and violation names are flat
  strings such as `address.street` or `items[0].name`; there is no machine-readable path.
- `ValueValidator` has `map`/`andThen`/`compose`/`lift*` but no
  `optional`/`default`/`nullable`/`or`/`refine`.
- `Yavi.arguments()` only composes scalar positional arguments. It cannot nest an
  object with a prefixed path, validate collection elements, express a cross-field
  rule, or make a field optional.
- There is no parse entry point for `Map`/JSON-tree input; the workaround is `compose`
  plus unchecked casts.

## Phases

1. Sealed `ParseResult`/`Issue` model: `Success`/`Failure`, `Missing`/`TypeMismatch`/
   `Constraint`, path segments and codes, exhaustive `switch`. Keep the existing
   `Validated` convenience methods working.
2. Schema combinators and object/collection composition: `optional`, `default`,
   `nullable`, `or`, `refine`, nested object fields with prefixed paths, list/map
   fields with indexed paths. This requires updating `scripts/generate-args.sh`.
3. `Map`/JSON-tree `safeParse` and discriminated unions for polymorphic input.

## Notes

- Depends on 018 (record patterns / switch pattern matching) and 019 (JSpecify
  nullness), and should use Java 21 language features.
- Keep the design zero-dependency: `Map` input needs no JSON library.
- Reuse the existing constraint message keys as `Issue` codes.
