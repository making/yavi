# Comparison with Zod

[Zod](https://zod.dev) validates untyped input against a schema and returns a typed value.
YAVI covers the same workflow in Java since 0.18.0. This page maps the concepts of Zod to
YAVI and lists the differences.

* [Basic](#basic)
* [Vocabulary](#vocabulary)
* [Schemas](#schemas)
* [Inputs](#inputs): a map, a JSON tree, request parameters, typed values
* [Rules that differ](#rules-that-differ): absent keys, unknown keys, types, strings, unions,
  errors
* [Not available](#not-available)

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

ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._integer("age", c -> c.greaterThanOrEqual(0))
        ._string("nickname").optional()
        .fromMap(User::new);

Map<String, Object> map = objectMapper.readValue(input, new TypeReference<>() {});
switch (userValidator.validate(map)) {
    case Validated.Valid<User>(User user) -> user;
    case Validated.Invalid<User>(ConstraintViolations violations) -> violations;
}
```

The type is not inferred from the schema as with `z.infer`. The validated properties are
given to a constructor (or any function) as its arguments, so the result is an ordinary
record or class and the compiler checks that they match it.

## Vocabulary

| Zod | YAVI |
| --- | --- |
| schema (`ZodType<Output, Input>`) | `ValueValidator<T, X>` (input `T`, output `X`). `Yavi.schema()` is not yet one: it describes the properties, and `fromMap(f)` or `apply(f)` makes a validator of them |
| `z.object({...})` | `Yavi.schema()._xxx(...).fromMap(f)` (`ObjectInputValidator<X>`) |
| `schema.safeParse(input)` | `validator.validate(input)` returns `Validated<X>` |
| `schema.parse(input)` | `validator.validated(input)` throws `ConstraintViolationsException` |
| `result.success` / `result.data` / `result.error` | `Validated.Valid(value)` / `Validated.Invalid(errors)`, a sealed interface |
| issue (`code`, `path`, `message`) | `ConstraintViolation` (`messageKey()`, `path()`, `message()`) |
| `z.infer<typeof schema>` | the result type of the function given to `fromMap` / `apply` |

## Schemas

| Zod | YAVI |
| --- | --- |
| `z.string()`, `z.number()`, `z.boolean()`, `z.date()` ... as a property | `_string(name, c -> ...)`, `_integer`, `_long`, `_double`, `_bigDecimal`, `_boolean`, `_localDate`, `_instant` ... |
| the same as a schema of its own | `Yavi._string(c -> ...)`, `Yavi._integer(c -> ...)` ... |
| `z.enum([...])` | `_enum(name, Role.class)`; `_enum(Yavi._enum(name, Role.class, c -> c).ignoringCase())` reads the name in any case |
| `z.object({...})` nested | `_object(name, anotherObjectInputValidator)` |
| `z.array(schema)` | `_list(name, elementValidator)`, `validator.liftList()` |
| `z.record(z.string(), schema)` | `_map(name, valueValidator)`, `validator.liftMap()` |
| `z.array(schema).nonempty().min(1).max(10)` | `_list(name, elementValidator, c -> c.notEmpty().greaterThanOrEqual(1).lessThanOrEqual(10))`, `Yavi._list(elementValidator, c -> ...)` (also `unique()`, `contains(...)`) |
| `z.record(z.string(), schema)` with a size | `_map(name, valueValidator, c -> c.notEmpty())`, `Yavi._map(valueValidator, c -> ...)` |
| `.optional()` | `.optional().rejectingNull()` (produces `Optional<X>`) |
| `.nullable()` | `.nullable().required()` (produces `@Nullable X`) |
| `.nullish()` | `.optional()` or `.nullable()` |
| `.optional()` with `undefined` told from a value in the result | `.presence()` (produces `Presence<X>`) |
| `.nullable().optional()` / `.nullish()` with the two told apart in the result | `.nullable().presence()` |
| `.default(value)` | `.defaultValue(value).rejectingNull()`; `.defaultValue(value)` also replaces `null` |
| `.catch(value)` | `.recover(value)` (also for `null` and an absent key) |
| `.catch(ctx => ...)` | `.recoverWith(violations -> ...)` |
| `z.union([a, b])` | `ValueValidator.anyOf(a, b)` |
| `z.discriminatedUnion("type", [...])` | `DiscriminatedUnionValidator.<X>of("type").when("circle", a).when("square", b)` |
| `z.lazy(() => schema)`, a getter that refers to the schema | `ValueValidator.lazy(() -> validator)`, `ValueValidator.recursive(self -> ...)` |
| `z.number().multipleOf(n)` | `c.multipleOf(n)` of the integral types, `BigInteger` and `BigDecimal` (not of `Double` and `Float`) |
| `z.email()`, `z.uuid()`, `z.ipv4()`, `z.ipv6()`, `z.ulid()` | `c.email()`, `c.uuid()`, `c.ipv4()`, `c.ipv6()`, `c.ulid()` |
| `z.union([z.ipv4(), z.ipv6()])` | `c.ip()` |
| `z.url()` | `c.uri()` (any scheme), `c.url()` (a protocol of `java.net.URL`) |
| `z.cuid()`, `z.cuid2()` | `c.pattern(regex)` |
| `.refine(predicate)` on a value | a constraint (`c -> c.predicate(...)`) |
| `.refine(predicate)` on an object | `.constraintOnTarget(predicate, name, violationMessage)` |
| `.transform(f)` | `.map(f)` |
| `z.string().trim().toLowerCase()`, `.toUpperCase()`, `.normalize()` | `_string(name, c -> c.trim().toLowerCase()...)`, `c.toUpperCase()`, `c.toNormalForm(form)`, `c.transform(f)`, written before the constraints |
| `z.preprocess(f, schema)` | `validator.preprocess(f)` for a value of the same type, `validator.compose(f)` for another type |
| `.pipe(schema)` | `.andThen(validator)` |
| `z.unknown()` / `z.any()` | `_object(name, Object.class)` |
| `z.object({...})` (strips unknown keys) | the default of `fromMap` |
| `z.strictObject({...})` | `.rejectingUnknownProperties()` |
| `z.looseObject({...})` | `._rest()` |
| `z.object({...}).catchall(schema)` | `._rest(valueValidator)` |
| `a.extend(b.shape)`, `z.intersection(a, b)` of objects | `._embedded(b)`, which gives the object of `b` to the function |
| `z.coerce.number()`, `z.coerce.boolean()` | a property of the input, see [Strings](#strings) |
| `z.toJSONSchema(schema)` | `JsonSchema.of(validator.description())` |

## Inputs

A Zod schema always takes `unknown`, which in practice is what `JSON.parse` returns. In
Java the input has several forms, and the same properties are read from all of them. The schema is
written once with `fromMap(f)`, and the form of the input is chosen last:

| Input | Zod | YAVI | Input type |
| --- | --- | --- | --- |
| A parsed JSON document as a map | `schema.safeParse(JSON.parse(input))` | `validator` | `Map<String, Object>` |
| A value of unknown type | `schema.safeParse(input)` | `validator.untyped()` | `Object` |
| The tree model of a JSON library | | `validator.readingWith(reader)` | the node type, such as `JsonNode` |
| Request parameters, a submitted form | `z.coerce`, `zod-form-data` | `validator.readingParameters()` | `Map<String, String>`, `Map<String, List<String>>`, `Map<String, String[]>` |
| Typed arguments | | `Yavi.schema()...apply(f)` | the types of the arguments |

### A map or a value of unknown type

`fromMap(f)` builds a validator of a `Map<String, Object>`. `untyped()` turns it, and any
other validator that knows its input type, into a `ValueValidator<Object, X>`, which is
what a Zod schema is:

```java
ValueValidator<Object, User> any = userValidator.untyped(); // rejects anything but a map
ValueValidator<Object, Object> id = ValueValidator.anyOf(Yavi._string(c -> c.notBlank()).untyped(),
        Yavi._integer(c -> c.positive()).untyped());
```

The input type must be known when the validator is built. `fromMap` and `untyped()` throw
for a lambda or an `_enum` / `_object` property added without a type; give it with
`untyped(type)`, `_enum(name, type)` or `_object(name, type)`.

### The tree model of a JSON library

`readingWith(reader)` binds a validator to an `InputReader`, which tells how to read the
nodes of a tree model. The tree is validated without being copied to a map.
`from(reader, f)` builds such a validator from the schema:

```java
ValueValidator<JsonNode, User> nodeValidator = userValidator.readingWith(JsonNodeReader.INSTANCE);
ValueValidator<JsonNode, User> same = Yavi.schema()._string("email")...from(JsonNodeReader.INSTANCE, User::new);
```

The README shows the reader for Jackson.

### Request parameters

Request parameters and a submitted form are a flat map of strings. `readingParameters()`
reads it with the properties written as for a JSON document:

```java
ValueValidator<Map<String, List<String>>, SearchForm> validator = Yavi.schema()
        ._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
        ._boolean("includeArchived").defaultValue(false)
        ._list("tags", tagValidator).defaultValue(List.of())
        ._object("address", addressValidator).nullable()
        .fromMap(SearchForm::new)
        .readingParameters();

// page=2&includeArchived=true&tags=a&tags=b&address.city=Tokyo
validator.validate(parameters);
```

It does what Zod needs two things for:

* The values are strings that stand for the type of their property, which is `z.coerce` in
  Zod. The rules are in [Strings](#strings).
* The keys are paths (`address.city`, `items[0].name`) and a key with several values is a
  list, which is what `zod-form-data` adds to Zod for a `FormData`. The README states the
  syntax of the keys.

An empty list cannot be sent, so a list that may be empty has `defaultValue(List.of())`.

### Typed arguments

In Java the input is usually already bound to a type, so a YAVI validator is typed first
(`ValueValidator<String, Email>`). `apply(f)` builds a validator of typed arguments from
the schema that `fromMap(f)` builds a validator of a map from. Zod has no counterpart:
its schemas check the type and the constraints together.

## Rules that differ

### Absent and `null`

Like a Zod schema, a property is required unless it says otherwise, and saying otherwise
changes its type: `nullable()` produces `@Nullable X` and `optional()` `Optional<X>`, as
`z.infer` gives `X | null` and `X | undefined`. A nullness checker that reads JSpecify
(NullAway, Kotlin) therefore rejects a function that takes a `nullable()` property as a
value that is not `null`.

Zod tells `undefined` (`optional()`) from `null` (`nullable()`). In YAVI an absent key is
validated like `null`, so `optional()`, `nullable()` and `defaultValue(...)` accept both.
Only the violation differs for a required property: an absent key is reported with
`object.required` and an explicit `null` with `object.notNull`, or with the violation of
a constraint that rejects `null` such as `notBlank()`.

The last modifier of a property keeps the two apart as Zod does. `required()` rejects an
absent key although `null` is accepted, and `rejectingNull()` rejects an explicit `null`
although the key may be absent. The type of the value does not change.

```ts
const Account = z.object({
  deletedAt: z.iso.datetime().nullable(), // the key is required
  middleName: z.string().optional(),      // null is rejected
  page: z.number().int().default(1),      // null is rejected
});
```

```java
record Account(@Nullable Instant deletedAt, Optional<String> middleName, int page) {}

ObjectInputValidator<Account> accountValidator = Yavi.schema()
        ._instant("deletedAt").nullable().required()
        ._string("middleName").optional().rejectingNull()
        ._integer("page").defaultValue(1).rejectingNull()
        .fromMap(Account::new);
```

`presence()` gives the three states to the created object, for a partial update (PATCH). The property is a
`Presence<X>`: `Absent` for an absent key, which is not validated, and otherwise what the
property makes of the value, `null` included.

```ts
const ProfilePatch = z.object({
  nickname: z.string().max(50).optional(), // undefined or a string
  bio: z.string().max(500).nullable().optional(), // undefined, null or a string
});
```

```java
record ProfilePatch(Presence<String> nickname, Presence<String> bio) {}

ObjectInputValidator<ProfilePatch> patchValidator = Yavi.schema()
        ._string("nickname", c -> c.lessThanOrEqual(50)).presence()
        ._string("bio", c -> c.lessThanOrEqual(500)).nullable().presence()
        .fromMap(ProfilePatch::new);
```

Only the properties of `Yavi.schema()` and the validators of a single value
(`Yavi._string(...)`) are required. `ValidatorBuilder` validates an object that exists,
where a property that is `null` is valid unless a constraint such as `notNull()` rejects
it.

### Unknown keys

Both ignore unknown keys unless the object is strict. `rejectingUnknownProperties()`
reports them with the message key `object.unknownProperty`, which catches a typo in an
optional property; like `z.strictObject`, it applies to that object only.

The counterpart of `.catchall(schema)` and `z.looseObject` is an argument: the result is
created by a function, so the extra keys are given to it as a map. `_rest(valueValidator)`
validates their values and `_rest()` takes them as they are.

```java
record Item(String name, Map<String, String> attrs) {}

ObjectInputValidator<Item> itemValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._rest(Yavi._string(c -> c.notBlank()))
        .fromMap(Item::new);
```

### Types

Both are exact by default: `"30"` is not a number. YAVI accepts the representation a JSON
document has for the type of the property:

* a number whatever its Java type is, as long as the value is kept (`30L` and `30.0` are an
  `Integer`, `30.5` is not),
* the name of a constant for an enum,
* an ISO-8601 string for `LocalDate`, `Instant` and the other temporal types, `Duration`
  and `Period`,
* the string form of a `UUID` and a `URI` (`_object("id", UUID.class)`).

A mismatch is reported with `object.type` (`"age" must be of type integer`).

### Strings

`z.coerce` is chosen per schema and converts with the JavaScript constructors
(`Number(input)`, `Boolean(input)`). In YAVI the coercion of strings belongs to the
input, not to a property, so the schema does not change:

* `readingParameters()` coerces the strings of request parameters.
* `CoercibleInputReader#coercingStrings()` decorates a reader of a tree model, for a tree whose scalars are all
  strings: `validator.readingWith(JsonNodeReader.INSTANCE.coercingStrings())` reads a JSON
  document whose numbers are sent as strings.

Both follow the same rules, which are stricter than `z.coerce`:

| Input | `z.coerce` | YAVI |
| --- | --- | --- |
| `""` for a number | `0` | `null`, so `optional()` and `defaultValue` apply |
| `" 30 "` for a number | `30` | `object.type`; `30` with `trimming()` |
| `"0x1e"` for a number | `30` | `object.type` |
| `"false"` for a boolean | `true` | `false` |
| `"on"`, `"1"` for a boolean | `true` | `object.type`; `true` with `booleanWords()`, like `z.stringbool()` |
| `30` for a string | `"30"` | `object.type` |

`trimming()` and `booleanWords()` are options of `StringCoercion`, given to
`readingParameters(coercion)` or `coercingStrings(coercion)`:
`validator.readingParameters(StringCoercion.strict().trimming().booleanWords())` reads a
submitted form, whose checked checkbox sends `on`.

An empty string is `null` for a string property too. Any other coercion is a validator
that takes an `Object`, or a reader that overrides `InputReader#value(node, type)`:

```java
ValueValidator<Object, Integer> coerced = (value, locale, context) -> Yavi._integer(c -> c)
        .untyped()
        .validate(value instanceof Boolean b ? Integer.valueOf(b ? 1 : 0) : value, locale, context);

Yavi.schema()._object("age", coerced.untyped(Object.class))...
```

### Unions

`z.union` and `ValueValidator.anyOf` try the alternatives in order and report one issue
when none matches: `invalid_union` with `errors`, the issues of each alternative, and
`object.anyOf`, a `ConstraintViolation.NoAlternativeMatched` with `alternatives()`, the
violations of each alternative (`alternatives` of a `ViolationDetail.NoAlternativeMatched`).
`DiscriminatedUnionValidator` applies only the alternative selected by the discriminator,
like `z.discriminatedUnion`. An alternative does not have to define the discriminator as
`z.literal(...)`: the key is hidden from an alternative that does not define it.

### Errors

A `ConstraintViolation` has a message key instead of an issue code, and the message is
formatted with a `MessageFormatter`, so it can be localized. `path()` is a list of
`PathSegment.Property` and `PathSegment.Index`, `name()` is its flat form
(`address.street`, `tags[0]`) and `pointer()` its JSON Pointer (`/address/street`,
`/tags/0`).

| Zod | YAVI |
| --- | --- |
| `z.flattenError(error)` | `violations.messagesByName()`, a map of the name to the messages; the messages of the root are under the empty name |
| `error.issues` as JSON | `violations.details()`, a list of `ViolationDetail` (`name`, `pointer`, `messageKey`, `message`, `args`, and `alternatives` for `object.anyOf`) |

`z.treeifyError` has no counterpart: the keys of `messagesByPointer()` address the
nested values instead.

## Not available

* Object schema operations that derive a schema: `pick`, `omit`, `partial`. `extend` and
  `merge` are `_embedded`, which keeps the object of the embedded validator.
* `z.tuple` and `z.literal` as a schema (use the `equalTo` / `oneOf` constraints).
* Asynchronous refinements and transforms.
* More than 16 properties in one schema; group them with `_embedded`, or use
  `ObjectInputValidator.of(parts, mapper)`.
