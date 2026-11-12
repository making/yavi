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

ParseResult<User> result = userSchema.safeParse(map);
```

Discriminated union for polymorphic input (for example a `type` field):

```java
Schema<Map<String, Object>, Shape> shapeSchema =
    Yavi.discriminatedUnion("type", Map.of(
        "circle", circleSchema,
        "square", squareSchema));
```

## Scope

- Add `safeParse(Map<String, Object>)` and a throwing `parse(...)` to the schema API;
  derive field values by the names declared in the schema.
- Report missing fields, type mismatches and unknown keys as `Issue` values. Make the
  unknown-key policy explicit (strict / strip / passthrough) with a safe default.
- Support the optional/default fields from 021 when a key is absent.
- Add a discriminated-union combinator keyed by a field value. When no branch matches,
  report an issue that lists the branch failures.
- Work with Java sealed types and records for the branch result types.
- Keep the implementation dependency-free: `Map`, `List` and JDK types only.

## Design questions to settle

- Whether the type check accepts exact Java types only, or performs opt-in coercion
  (`"1"` to `1`, ISO strings to temporal types). Zod has `z.coerce`; default to exact
  types and make coercion explicit.
- Default unknown-key policy: `STRIP` is the common Zod default, but silent stripping can
  hide typos; `STRICT` is safer for API input. Decide and document.
- Whether `parse` throws the existing `ConstraintViolationsException` or a new
  exception carrying `List<Issue>`.
- How nested maps and lists are represented and validated without reflection.
- How the discriminator behaves when the field is missing or not a string.

## Notes

- Depends on 021.
- Do not add reflection; the schema already knows its field names and constructor.
- This is the closest analogue of `schema.safeParse(JSON.parse(input))` in Java, but it
  remains optional for users who validate DTOs bound by Jackson.
