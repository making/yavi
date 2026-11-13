# Reject by type a schema that `fromMap` rejects when it is built

Difficulty: High

## Problem

The builders of `Yavi.schema()` accept chains that `fromMap` (and `flatFromMap`,
`from`) reject only when they are called, with an exception (run on the branch of
#563):

| Chain | Exception of `fromMap` |
| --- | --- |
| `._enum("role")`, `._enum("role", constraints)` | `IllegalStateException`: the input type is unknown |
| `._object("o")`, `._object("o", constraints)` | `IllegalStateException`: the input type is unknown |
| `._string(Yavi._string(c -> c.notBlank()))` (a validator without a name) | `IllegalStateException`: the argument has no name |
| `._rest()._rest()` | `IllegalArgumentException`: the rest is defined twice |
| `._string("a")._string("a")` | `IllegalArgumentException`: the property is defined twice |
| `._rest().fromMap(f).rejectingUnknownProperties()` | `IllegalStateException`: no property is unknown |

The same cause outside `fromMap`: `Yavi._enum("role", c -> c).ignoringCase()` throws
`IllegalStateException` (the enum type is unknown), because `_enum(name)` and
`_enum(name, type)` return the same `EnumValidator`.

They fail fast, when the validator is built, so nothing wrong is validated. But the
policy is that types reject what they can tell, and the first two rows are plain
method choices.

`apply` does not check a name that is defined twice:
`._string("a")._string("a").apply(f)` builds a validator whose description, and so its
JSON Schema, has the property `a` twice.

## Direction

Enumerate per row what types can tell and what it costs:

- `_enum(name)` / `_object(name)` without a type: since 0.14, usable with `apply` only.
  0.18.0 already has breaking changes, so removing them in favour of
  `_enum(name, type)` / `_object(name, type)` is an option; so is deprecating them.
  Weigh against the callers that use `apply` only. Whatever is decided covers
  `EnumValidator#ignoringCase()` too: without the untyped factories it cannot fail, or
  else the untyped ones return a type without it.
- A validator without a name: `Yavi._string(constraints)` and
  `Yavi._string(name, constraints)` return the same type. A marker for a named
  validator would tell, at the price of a second overload per `_xxx(validator)` (see
  how #563 did it for `NullAcceptingValueValidator`).
- `_rest` twice, `rejectingUnknownProperties()` with `_rest`: a builder family or a
  result type that remembers `_rest`. Probably not worth 16 more builders; say so.
  A cheaper way for `_rest` twice: since #564 `_rest` returns the `Recoverable` stage,
  which extends the builder. A stage of its own that has the terminals and `recover`
  but no `_xxx` makes `_rest` the last property by type. Price: no property can be
  written after `_rest`, so the rest is always the last argument of the function.
- A name defined twice: types cannot tell. Make `apply` consistent with `fromMap`
  (reject), or say why not.

Related decision of #563: `apply` and `fromMap` stay on the same builder ("one
definition, both terminals"), so a solution that splits the builders by terminal is
not wanted.
