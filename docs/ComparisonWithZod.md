# Comparison with Zod

[Zod](https://zod.dev) validates untyped input against a schema and returns a typed value.
YAVI covers the same workflow in Java since 0.18.0. This page maps the concepts of Zod to
YAVI and lists the differences.

## Basic

```ts
const User = z.object({
  email: z.string().email(),
  age: z.number().int().min(0),
  nickname: z.string().optional(),
});

const result = User.safeParse(JSON.parse(input));
if (result.success) {
  result.data; // { email: string; age: number; nickname?: string }
} else {
  result.error.issues;
}
```

to

```java
record User(String email, Integer age, Optional<String> nickname) {}

MapInputValidator<User> userValidator = Yavi.arguments()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.notNull().greaterThanOrEqual(0))
        ._string("nickname").optional()
        .fromMap(User::new);

Map<String, Object> map = objectMapper.readValue(input, new TypeReference<>() {});
switch (userValidator.validate(map)) {
    case Validated.Valid<User>(User user) -> user;
    case Validated.Invalid<User>(ConstraintViolations violations) -> violations;
}
```

The type is not inferred from the schema as with `z.infer`. The arguments are given to a
constructor (or any function), so the result is an ordinary record or class and the
compiler checks that the arguments match it.

## Vocabulary

| Zod | YAVI |
| --- | --- |
| schema (`ZodType<Output, Input>`) | `ValueValidator<T, X>` (input `T`, output `X`) |
| `z.object({...})` | `Yavi.arguments()._xxx(...).fromMap(f)` (`MapInputValidator<X>`) |
| `schema.safeParse(input)` | `validator.validate(input)` returns `Validated<X>` |
| `schema.parse(input)` | `validator.validated(input)` throws `ConstraintViolationsException` |
| `result.success` / `result.data` / `result.error` | `Validated.Valid(value)` / `Validated.Invalid(errors)`, a sealed interface |
| issue (`code`, `path`, `message`) | `ConstraintViolation` (`messageKey()`, `path()`, `message()`) |
| `z.infer<typeof schema>` | the result type of the function given to `fromMap` / `apply` |

## Schemas

| Zod | YAVI |
| --- | --- |
| `z.string()`, `z.number()`, `z.boolean()`, `z.date()` ... | `_string`, `_integer`, `_long`, `_double`, `_bigDecimal`, `_boolean`, `_localDate`, `_instant` ... |
| `z.enum([...])` | `_enum(name, Role.class)` |
| `z.object({...})` nested | `_object(name, anotherMapInputValidator)` |
| `z.array(schema)` | `_list(name, elementValidator)`, `validator.liftList()` |
| `z.record(z.string(), schema)` | `_map(name, valueValidator)`, `validator.liftMap()` |
| `.optional()` | `.optional()` (produces `Optional<X>`) |
| `.nullable()` | `.nullable()` |
| `.default(value)` | `.defaultValue(value)` |
| `z.union([a, b])` | `ValueValidator.anyOf(a, b)` |
| `z.discriminatedUnion("type", [...])` | `DiscriminatedUnionValidator.<X>of("type").when("circle", a).when("square", b)` |
| `.refine(predicate)` on a value | a constraint (`c -> c.predicate(...)`) |
| `.refine(predicate)` on an object | `.constraintOnTarget(predicate, name, violationMessage)` |
| `.transform(f)` | `.map(f)` |
| `.pipe(schema)` | `.andThen(validator)` |
| `z.unknown()` / `z.any()` | `_object(name, Object.class)` |
| `z.strictObject({...})` | the default of `fromMap` |
| `z.object({...})` (strips unknown keys) | `.ignoringUnknownProperties()` |
| `z.toJSONSchema(schema)` | `JsonSchema.of(validator.description())` |

## Differences

### Unknown keys are rejected by default

Zod strips unknown keys unless the object is strict. `fromMap` reports them with the
message key `object.unknownProperty`, because a stripped key hides a typo in an optional
property. `ignoringUnknownProperties()` gives the behavior of Zod. There is no
counterpart of `z.looseObject` (passthrough): the result is created by a function, so
there is nowhere to keep the extra keys.

### Absent and `null`

Zod tells `undefined` (`optional()`) from `null` (`nullable()`). In YAVI an absent key is
validated like `null`, so `optional()`, `nullable()`, `defaultValue(...)` and an argument
without `notNull()` accept both. Only the violation differs when the argument rejects
`null`: an absent key is reported with `object.required` and an explicit `null` with the
violation of the constraint, such as `object.notNull`.

A value is not required unless a constraint says so. `z.string()` rejects `undefined`,
whereas `_string("name")` without `notNull()` or `notBlank()` accepts an absent key.

### Types and coercion

Both are exact by default: `"30"` is not a number. YAVI accepts the representation a JSON
document has for the type of the argument:

* a number whatever its Java type is, as long as the value is kept (`30L` and `30.0` are an
  `Integer`, `30.5` is not),
* the name of a constant for an enum,
* an ISO-8601 string for `LocalDate`, `Instant` and the other temporal types.

A mismatch is reported with `object.type` (`"age" must be of type integer`). There is no
`z.coerce`. A coercion is a validator that takes an `Object`:

```java
ValueValidator<Object, Integer> coerced = (value, locale, context) -> ValueValidator.<Integer>passThrough()
        .untyped(Integer.class)
        .validate(value instanceof String s ? Integer.valueOf(s) : value, locale, context);

Yavi.arguments()._object("age", coerced.untyped(Object.class))...
```

### Typed input is the primary case

A Zod schema always takes `unknown`. A YAVI validator is typed first
(`ValueValidator<String, Email>`), because in Java the input is usually already bound to a
type. `apply(f)` builds a validator of typed arguments and `fromMap(f)` builds one of a
map from the same arguments. `untyped()` turns a validator that knows its input type into
a `ValueValidator<Object, X>`, which is what a Zod schema is:

```java
ValueValidator<Object, User> any = userValidator.untyped(); // rejects anything but a map
ValueValidator<Object, Object> id = ValueValidator.anyOf(stringValidator.untyped(), integerValidator.untyped());
```

The input type must be known when the validator is built. `fromMap` and `untyped()` throw
for a lambda or an `_enum` / `_object` argument added without a type; give it with
`untyped(type)`, `_enum(name, type)` or `_object(name, type)`.

### Unions

`z.union` and `ValueValidator.anyOf` try the alternatives in order and report the failures
of all of them (`object.anyOf` followed by the violations of every alternative).
`DiscriminatedUnionValidator` applies only the alternative selected by the discriminator,
like `z.discriminatedUnion`. An alternative does not have to define the discriminator as
`z.literal(...)`: the key is removed from the map given to an alternative that does not
define it.

### Errors

A `ConstraintViolation` has a message key instead of an issue code, and the message is
formatted with a `MessageFormatter`, so it can be localized. `path()` is a list of
`PathSegment.Property` and `PathSegment.Index`, and `name()` is its flat form
(`address.street`, `tags[0]`).

### Not available

* Object schema operations: `extend`, `merge`, `pick`, `omit`, `partial`.
* `z.intersection`, `z.tuple`, `z.lazy` (recursive schemas) and `z.literal` as a schema
  (use the `equalTo` / `oneOf` constraints).
* Asynchronous refinements and transforms.
* More than 16 properties with the builders; use `MapInputValidator.of(properties, mapper)`.
* A tree model of a JSON library as input. Convert it first:
  `userValidator.untyped().compose(node -> objectMapper.convertValue(node, Object.class))`.
