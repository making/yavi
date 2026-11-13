# Reject the modifier chains of a schema property that have no effect, by type

Difficulty: Medium

## Background

The builders returned by `Yavi.schema()` are staged: `Modifiable` (after `_embedded`,
`_rest`), `Keyed` (after a property read from a key) and `NullAccepting` (after
`optional()`, `nullable()`, `defaultValue(...)`, `recover(...)`, `recoverWith(...)` of a
`Keyed`). `required()` and `rejectingNull()` are on `NullAccepting` only, so
`_string("x").required()` does not compile.

Policy: a setting without effect is rejected by the compiler wherever types can tell,
above all in the fluent API.

## Problem

Chains that still compile although a part of them has no effect:

1. **Stacked modifiers that accept `null`.** Each of `optional()`, `nullable()`,
   `defaultValue(...)`, `recover(...)`, `recoverWith(...)` handles `null` before the
   validator it wraps, so the `null` handling of the inner one is never reached:

   | Chain | What is dead |
   | --- | --- |
   | `.nullable().optional()` | `nullable()` |
   | `.defaultValue(1).nullable()` | the default value |
   | `.defaultValue(1).recover(2)` | the default value (`recover` also replaces `null`) |
   | `.optional().defaultValue(Optional.of("a"))` | the empty `Optional` |

   Chains that do have an effect and must stay: a modifier after `recover(...)` /
   `recoverWith(...)` changes what `null` produces while an invalid value is still
   recovered (`.recover(0).nullable()`), and `recover` after `optional()` /
   `nullable()` recovers an invalid value (`.optional().recover(Optional.empty())`),
   although it takes over `null` too. Enumerate all pairs first and decide which stay.

2. **A property added with a validator that already accepts `null`** is a `Keyed`, not
   a `NullAccepting`: `_string(Yavi._string("x").nullable())` cannot be followed by
   `.required()`, which would have an effect. `.nullable().required()` gives the same
   result with a redundant modifier. (Read from the signatures; not run.) The type of
   the validated value (`@Nullable R`, `Optional<R>`) does not tell a `defaultValue`,
   so decide whether an overload, a marker type or a runtime check of
   `description().acceptsNull()` is the answer.

3. **`apply` after `required()` / `rejectingNull()`.**
   Typed arguments have no absent state, so the modifier changes only the description.
   The same builder has both `fromMap` and `apply`; decide whether the builder returned
   by `required()` / `rejectingNull()` should offer `fromMap` (and `flatFromMap`) only.
   This loses "one definition, both terminals" for such a schema: weigh it.

4. **`new ObjectInputValidator.Property<>(name, validator, Strictness.REQUIRED)`** with
   a validator that rejects `null`. Types cannot tell; harmless. Out of scope unless a
   check at construction (`description().acceptsNull()`) is wanted for consistency
   with 2.

## Scope

- Stage types in `scripts/generate-args.sh` (regenerate, `spring-javaformat:apply`).
- `Modifiable` (`_embedded`, `_rest`) has the same stacking as 1; treat it alike.
- `ValueValidator` itself (`validator.nullable().optional()`) is not staged; leave it,
  or say why.
- `docs/NewFeatures_0.18.0.md` and its Japanese version (the table of the section on
  an absent key and `null`), `docs/BreakingChanges_0.18.0.md` only if a chain that
  compiled in 0.17 stops compiling (the modifiers are new in 0.18.0, so probably none).
