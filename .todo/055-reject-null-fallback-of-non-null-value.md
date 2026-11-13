# Reject a `null` default value or fallback of a value that is not nullable

Difficulty: Medium

## Problem

`defaultValue`, `recover` and `recoverWith` of a schema property take a value of the
type of the validated value. Only a nullness checker that reads JSpecify rejects
`null`; with plain `javac` these compile and give `null` to a parameter whose type says
it is never `null` (run on the branch of #563):

```java
Yavi.schema()._string("x").defaultValue(null).fromMap(x -> x).validate(Map.of());        // null as a String
Yavi.schema()._string("x").recover(null).fromMap(x -> x).validate(Map.of());             // null as a String
Yavi.schema()._string("x").recoverWith(violations -> null).fromMap(x -> x).validate(Map.of("x", 1));
Yavi.schema()._string("x").optional().recover(null).fromMap(x -> x).validate(Map.of("x", 1)); // null as an Optional
```

The last one is the worst: an `Optional` that is `null`. `nullable()` is the modifier
that says `null`, and `.nullable().recover(null)` is the chain that must stay.

## Direction

The staged builders know whether the validated value is nullable: `recover` is reached
either from the fresh stage, after `optional()` (never `null`) or after `nullable()`.
Decide between:

- a check at run time (`Objects.requireNonNull` in `ArgumentDefinition.withDefault`,
  `recovering`, and on the result of the function of `recoveringWith`) wherever the
  stage is not the one after `nullable()`, which needs a stage of its own for
  `nullable()` or a flag in `ArgumentDefinition`;
- nothing but documentation, on the ground that a nullness checker reports it.

A fresh property whose validator produces a nullable value on its own
(`_string(ValueValidator.passThrough())`) must keep accepting a `null` default.

`ValueValidator#defaultValue(null)` itself cannot tell (the type argument may be
nullable); leave it.

## Related: `presence()` after `optional()`

`.optional().presence()` produces a `Presence<Optional<X>>` in which an explicit `null`
is `Present[Optional.empty]` and `PresentNull` never occurs, where
`.nullable().presence()` gives the three states directly. If `nullable()` gets a stage
of its own, decide whether the stage after `optional()` should offer `presence()`.
