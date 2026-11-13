# Make an argument required by default so that the result type tells whether it is null

Difficulty: High

## Problem

Since #556 the function given to `apply` / `build` receives the validated values without
`@Nullable` parameters. That removed a false error, but the check is now trusted, not
proven: an argument without a constraint that rejects `null` still produces `null`, and
no checker sees it.

```java
record Member(String name, int age) {}

Yavi.arguments()
    ._string("name", c -> c.notBlank())
    ._integer("age", c -> c.greaterThanOrEqual(0)) // notNull() is missing
    .apply(Member::new);                           // compiles; NullPointerException for a null age
```

Whether `null` is accepted is decided inside the constraint lambda (`c -> c.notNull()`),
so the type of the argument (`R2 = Integer`) is the same with and without it. The library
keeps three `@SuppressWarnings("NullAway")` for this (`ArgumentsN#map`,
`Validation#apply`, `Arguments1ValidatorBuilder#flatApply`) besides
`Validated#successWith`.

## Why NullAway's `@Contract` does not help

Checked against the NullAway wiki (Supported Annotations, Contracts). A contract relates
the nullness of the parameters of one method to its own result (`null -> false`,
`!null -> !null`, `null -> fail`, `_, null -> true`), and is applied to the local
variables at the call site. `@RequiresNonNull` / `@EnsuresNonNull` / `@EnsuresNonNullIf`
name fields of the receiver of the annotated method. Neither can say "the second
parameter of the function passed to `apply` is non-null if `notNull()` was called in the
lambda passed to `_integer` three calls earlier": the fact has to travel through the
return types of a fluent chain, which only a type argument can do. NullAway also cannot
infer the nullness of a call-site argument from the nullness of a result
(uber/NullAway#1121).

## Proposal

Like Zod and Raoh: an argument is required unless it says otherwise, and saying otherwise
changes its type.

```java
record Member(String name, @Nullable String nickname, int age) {}

Yavi.arguments()
    ._string("name", c -> c.notBlank())                  // R1 = String
    ._string("nickname", c -> c.lessThanOrEqual(20)).nullable() // R2 = @Nullable String
    ._integer("age", c -> c.greaterThanOrEqual(0))       // R3 = Integer, null is a violation
    .apply(Member::new);
```

1. Runtime: an argument without `optional()`, `nullable()`, `defaultValue(...)`,
   `recover(...)` or `presence()` reports a violation for `null`, whatever its
   constraints are. `notNull()` stays valid and becomes redundant there.
2. Types: `nullable()` returns a builder (and `ValueValidator#nullable()` a validator)
   whose result is `@Nullable R`. This needs the result type parameters to admit a
   nullable type argument: `R1..RN` of the builders, `X` of `ValueValidator` and
   `ArgumentsNValidator`, `T` of `Validated` / `Validation`, and `T1..TN` / `R` of
   `FunctionN` (`extends @Nullable Object`). The input type parameters (`A1..AN`, `T` of
   `ValueValidator`) and the `@Nullable` parameters of `validate` do not change; see the
   design note of #556 for why.
3. The four suppressions above are removed: a value is `null` only where its type says so.

## Decide first

- Scope. `Yavi.arguments()` and the single-value builders (`Yavi._string(...)`,
  `StringValidatorBuilder` and so on) are the candidates. `ValidatorBuilder` validates an
  existing object and keeps "null is valid unless `notNull()`" (`NullAs`); say so in the
  docs. `ArgumentsValidatorBuilder.of(f).builder(b -> ...)` validates raw arguments with a
  `ValidatorBuilder`, so its function cannot be typed this way: decide whether it stays
  trusted or is deprecated in favour of `Yavi.arguments()`.
- Migration. This silently changes the result of every existing argument without
  `notNull()`: a `null` that was accepted becomes a violation. Options: change it in
  0.18.0 with a migration note ("add `.nullable()`"), or introduce it under a new entry
  point and deprecate the old one. A silent change needs the explicit agreement of the
  maintainer.
- Message. `MapInputValidator` already reports `object.required` for an absent key of an
  argument that rejects `null`; decide whether a `null` of a required argument uses the
  same key or `object.notNull`, and what `notNull()` written explicitly reports.
- `ValueDescription#acceptsNull()` and the `required` of the JSON Schema are derived from
  `ConstraintDescription#rejectsNull()` today; they would follow the modifier instead.
- `presence()` after an argument that is not `nullable()`: `PresentNull` is then
  impossible, which is what `notNull()` + `presence()` means today.

## Acceptance

- The example of the problem section does not compile with NullAway (`age` without
  `nullable()` is required at run time, and with `nullable()` its type is
  `@Nullable Integer`, which `Member::new` rejects), and the Kotlin counterpart sees
  `Int?` only after `nullable()`.
- No `@SuppressWarnings("NullAway")` remains for a validated value that may be `null`.
- `./mvnw clean verify` and `./mvnw javadoc:jar` pass, the Kotlin sources compile with
  `-Xjspecify-annotations=strict`, and the generated types come from the scripts in
  `scripts/`.
- README, `docs/ComparisonWithZod.md` and `docs/ComparisonWithRaoh.md` describe the new
  default, and the breaking changes are listed in the pull request.
