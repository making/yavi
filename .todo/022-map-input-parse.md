# Parse Map/JSON-tree input and support discriminated unions

Difficulty: High

## Background

Zod's defining workflow is `schema.safeParse(JSON.parse(input))`: the input is
untyped and the schema performs both the shape check and the construction. In Java,
Jackson normally binds JSON to a DTO first, so this layer is optional for many users.
It is still the closest analogue of Zod, and it makes missing-field and type-mismatch
errors first-class instead of unchecked casts.

Today the workaround is:

```java
Arguments1Validator<Map<String, Object>, User> mapValidator = arguments3Validator
    .compose(m -> Arguments.of((String) m.get("name"), (String) m.get("email"),
        (Integer) m.get("age")));
```

The casts throw or silently pass the wrong type, and there is no missing-key or
unknown-key reporting.

## Proposed API

```java
Schema<Map<String, Object>, User> userSchema = Yavi.schema(User::new)
    ._string("email", c -> c.notBlank().email())
    ._integer("age", c -> c.notNull().min(0))
    ._optional("nickname", S.string())
    .unknownKeys(UnknownKeys.STRIP);

Validated<User> validated = userSchema.validate(map);
```

Discriminated union for polymorphic input (for example a `type` field):

```java
Schema<Map<String, Object>, Shape> shapeSchema =
    Yavi.discriminatedUnion("type", Map.of(
        "circle", circleSchema,
        "square", squareSchema));
```

## Scope

- Add `validate(Map<String, Object>)` and the throwing `validated(...)` to the schema API;
  derive field values by the names declared in the schema.
- Report missing fields, type mismatches and unknown keys as `ConstraintViolation`s
  with dedicated message keys. Make the
  unknown-key policy explicit (strict / strip / passthrough) with a safe default.
- Support the optional/default fields from 021 when a key is absent.
- Add a discriminated-union combinator keyed by a field value. When no branch matches,
  report a violation that lists the branch failures.
- Work with Java sealed types and records for the branch result types.
- Keep the implementation dependency-free: `Map`, `List` and JDK types only.

## Design questions to settle

- Whether the type check accepts exact Java types only, or performs opt-in coercion
  (`"1"` to `1`, ISO strings to temporal types). Zod has `z.coerce`; default to exact
  types and make coercion explicit.
- Default unknown-key policy: `STRIP` is the common Zod default, but silent stripping can
  hide typos; `STRICT` is safer for API input. Decide and document.
- How nested maps and lists are represented and validated without reflection.
- How the discriminator behaves when the field is missing or not a string.

## What 020 provides

- `Validated` is a sealed interface with the records `Validated.Valid<T>(T value)` and
  `Validated.Invalid<T>(ConstraintViolations errors)`, so the result of `validate(map)`
  can be handled with an exhaustive `switch` and record patterns. `ValueValidator` already
  has the throwing counterpart `validated(...)`, which throws
  `ConstraintViolationsException`.
- There is no separate issue type. A `ConstraintViolation` carries `path()`
  (`PathSegment.Property` / `PathSegment.Index`), `messageKey()` and `message()`. Report
  missing fields, type mismatches and unknown keys as violations with dedicated message
  keys, which keeps i18n and the `ConstraintViolations` API working, and set their path
  with `ConstraintViolation.builder()....path(...)`.
- Use YAVI's terms rather than Zod's (see "Vocabulary" in 021).

## What 021 provides

021 kept `Yavi.arguments()` as the only entry and added no `Schema` type, so read
`Yavi.schema(...)`, `_optional(...)` and `S.string()` in "Proposed API" as placeholders.

- Field kinds on the generated builders: `_object(name, validator)` (nested, path
  `address.street`), `_list(name, elementValidator)` (`roles[0]`) and
  `_map(name, valueValidator)` (`scores.math`). They reject `null` with `object.notNull`.
  A nested object is any `ValueValidator`, so a nested map-input validator
  (`ValueValidator<Map<String, Object>, X>`) is nested with `_object` as is.
- `optional()`, `nullable()` and `defaultValue(...)` on the builders modify the last added
  argument; the same combinators exist on `ValueValidator`. `null` is the only "missing"
  value in typed input. Decide here whether an absent key and an explicit `null` differ.
- `ValueValidator.anyOf(...)` reports `object.anyOf` followed by the violations of every
  alternative. Report an unmatched discriminator the same way: one violation with a
  dedicated message key, then the details.
- `constraintOnTarget(predicate, name, violationMessage)` on `ValueValidator` and every
  `ArgumentsNValidator` is the cross-field rule.
- `ConstraintViolation.prefixed(name)`, `Validated.prefixed(name)` and
  `ValueValidator.prefixed(name)` are the public path-aware prefix operations.
- Every generated `ArgumentsNValidatorBuilder` keeps one package-private
  `am.ik.yavi.builder.ArgumentDefinition` per argument (`d1` ... `dN`): `name` (`null`
  for `_xxx(ValueValidator)`) and `validator`. Everything else is the description of the
  validator (see "What 025 provides").

## What 025 provides

- `ValueValidator.description()` and `ArgumentsNValidator.description()` return an
  `am.ik.yavi.core.ValueDescription`: `Value(name, type, constraints)`,
  `Properties(List<Property(name, value)>)`, `Elements(element)`, `Entries(value)`,
  `AnyOf(alternatives)`, `NotNull(value)`, `Optional(value, defaultValue)` and
  `Unknown`. `acceptsNull()` tells whether a key may be absent.
- A validator built by `ArgumentsNValidatorBuilder.apply(...)` or
  `ArgumentsValidators.split(...).apply(...)` is `Properties`, one property per argument
  in order. `ArgumentsNValidator.compose` keeps it, so a nested `_object(name, validator)`
  shows its properties; `_list` / `_map` show the element description.
- Every property has a name. An argument added without a name takes the name of its
  value validator (`Value.name()`), or `argN` if that is unknown; decide whether the
  map-input terminal accepts `argN` or rejects it.
- `Value.type()` is the raw type known to the typed validator (`String.class` for
  `_string`); it is `Enum.class` for `_enum` and `Object.class` for `_object(name,
  constraints)`, and a lambda is `Unknown`. Checking those against untyped input still
  needs a type token or a validator that accepts `Object`.
- `am.ik.yavi.jsonschema.JsonSchema` is the first consumer and shows how to walk the
  tree.

## Notes

- Depends on 021 (done).
- Do not add reflection; the schema already knows its field names and constructor.
- This is the closest analogue of `schema.safeParse(JSON.parse(input))` in Java, but it
  remains optional for users who validate DTOs bound by Jackson.
