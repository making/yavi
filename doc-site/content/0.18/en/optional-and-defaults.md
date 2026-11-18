# Optional and Default Values

A property of `Yavi.schema()` is required. This page describes the modifiers that accept an absent key or `null`, that produce a default or a fallback, and that make a key required again.

## Absent keys and explicit null

When a schema reads a map, request parameters or a JSON tree, an absent key is validated like `null`. For a required property:

| Input | Violation |
| --- | --- |
| The key is absent | `object.required`: `"name" is required` |
| The value is explicitly `null` | `object.notNull`: `"name" must not be null`, or the violation of a constraint that rejects `null` |

A property that accepts `null` accepts an absent key too. A partial update, where an absent key means "leave unchanged" and `null` means "clear", is written with `presence()`, which tells the two apart.

## Accepting null

`nullable()`, `optional()` and `defaultValue(...)` apply to the last added property:

```java
record User(String email, @Nullable String nickname, Optional<String> bio, Integer age) {}

ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable()
        ._string("bio", c -> c.lessThanOrEqual(500)).optional()
        ._integer("age", c -> c.greaterThanOrEqual(0)).defaultValue(20)
        .fromMap(User::new);
```

* `nullable()` produces `@Nullable String` and is the only modifier that produces `null`.
* `optional()` produces `Optional<String>`: `Optional.empty()` for an absent key or `null`.
* `defaultValue(20)` produces the default for an absent key or `null`.

The function given to `fromMap` must accept the produced type. With NullAway or Kotlin, a `nullable()` property requires a `@Nullable` parameter (in Kotlin, `String?`), and `optional()` requires an `Optional<String>`.

A modifier chain in which one modifier has no effect does not compile: `.nullable().optional()`, `.defaultValue(1).nullable()`, `.defaultValue(1).recover(2)`. A property added with a validator that already accepts `null`, such as `_integer(Yavi._integer("page", c -> c.positive()).defaultValue(1))`, takes no second modifier that accepts `null`.

`notNull()` is still the way to give the violation of `null` another message. `isNull()` needs `nullable()`: `._string("id", c -> c.isNull()).nullable()` accepts only `null`, whereas without `nullable()` no value is valid.

## Presence

`presence()` reads a property as a `Presence`, which is one of `Present(value)`, `PresentNull` and `Absent`. It is the modifier for a PATCH request or a JSON Merge Patch, where an absent key leaves a value unchanged and `null` clears it:

```java
record ProfilePatch(Presence<String> nickname, Presence<@Nullable String> bio) {}

ObjectInputValidator<ProfilePatch> patchValidator = Yavi.schema()
        ._string("nickname", c -> c.lessThanOrEqual(50)).presence()
        ._string("bio", c -> c.lessThanOrEqual(500)).nullable().presence()
        .fromMap(ProfilePatch::new);

ProfilePatch patch = patchValidator.validated(Map.of("bio", "Hello")); // nickname is Absent
patch.nickname().ifPresent(profile::setNickname); // not called
profile.setBio(patch.bio().orElse(profile.getBio())); // null if the key is null
```

* An absent key is `Absent` and is not validated, so the property is never required.
* A present key is validated as without `presence()`. An explicit `null` is a violation unless the property is `nullable()`, which produces `PresentNull`.
* `presence()` is the last modifier. `nullable()`, `defaultValue(...)` and `recover(...)` are written before it and apply to the value of a present key.
* `presence()` is not available on an `_embedded` or `_rest` property, which is not read from a key, or after `optional()`, whose `Optional` would be wrapped.
* With `apply(f)`, the argument itself is a `Presence`; `liftPresence()` is the combinator for a single-value validator.
* In request parameters, a parameter with an empty value (`bio=`) is present and `null`.

## Default values

`defaultValue(value)` produces the value for an absent key and for `null`. The default is not validated; it is the value of the property.

```java
._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
._boolean("includeArchived").defaultValue(false)
._list("tags", Yavi._string(c -> c.notBlank())).defaultValue(List.of())
```

A default value is of the type of the property, so a default that is `null` is written with `nullable()`. `defaultValue(null)` of a value that is never `null`, such as the value of constraints, a list, a map or an `Optional`, is rejected with a `NullPointerException` when the schema is built.

## Recovery

`recover(fallback)` makes a property never fail: the fallback is produced for `null` and for a value that is invalid. When the property is read from a map, request parameters or a JSON tree, a value that does not represent its type produces the fallback too. `recoverWith(violations -> ...)` computes the fallback from the violations, for example to log them; it is given no violation for `null`.

```java
._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
._integer("page", c -> c.greaterThanOrEqual(0)).recover(0)
```

`recover` after `optional()` or `nullable()` recovers an invalid value. `optional()`, `nullable()` or `defaultValue(...)` after `recover(...)` changes what `null` produces. A recovery value that is `null` is written as `.nullable().recover(null)`.

## Requiring a key or rejecting null

A contract that is not a partial update often accepts only one of the two states. `required()` and `rejectingNull()` say which, without changing the type of the value:

```java
record Account(@Nullable Instant deletedAt, Optional<String> bio, int page) {}

ObjectInputValidator<Account> accountValidator = Yavi.schema()
        ._instant("deletedAt").nullable().required()      // must be sent, as null if there is none
        ._string("bio").optional().rejectingNull()        // may be omitted, a text if it is sent
        ._integer("page").defaultValue(1).rejectingNull() // 1 only if omitted
        .fromMap(Account::new);

accountValidator.validate(Map.of());                                  // "deletedAt" is required
accountValidator.validate(Collections.singletonMap("deletedAt", null)); // deletedAt=null, bio=Optional.empty, page=1
```

They are written after `optional()`, `nullable()`, `defaultValue(...)` or `recover(...)`. Typed arguments (`apply`) have no absent state, so `null` is validated there as without them.

| Property | An absent key | An explicit `null` |
| --- | --- | --- |
| `_string("x")` | `object.required` | `object.notNull` |
| `.nullable()`, `.optional()`, `.defaultValue(...)`, `.recover(...)` | accepted | accepted |
| followed by `.required()` | `object.required` | accepted |
| followed by `.rejectingNull()` | accepted | `object.notNull` |
| `.presence()` | `Absent` | validated by the property |

## Nullness and type changes

The type of a validated value tells whether it can be `null`:

| Modifier | Produced type |
| --- | --- |
| none | `String` |
| `nullable()` | `@Nullable String` |
| `optional()` | `Optional<String>` |
| `defaultValue(v)` | `String` |
| `recover(v)` | `String`, or `@Nullable String` for `recover(null)` on a nullable value |
| `presence()` | `Presence<String>`, or `Presence<@Nullable String>` after `nullable()` |

With a nullness checker that reads JSpecify (NullAway, Kotlin), `fromMap(User::new)` does not compile if a `nullable()` argument is given to a parameter that is not `@Nullable`. This is the check that the 0.18 model adds: a forgotten `nullable()` is a compile error at the call site, not a `NullPointerException` at run time.

## Behavior summary

* An absent key is validated like `null` and is never passed to the constructor as a missing value.
* A modifier that accepts `null` decides what is produced: `Optional.empty()`, `null` or the default.
* `required()` rejects an absent key and `rejectingNull()` rejects an explicit `null`; neither changes the type.
* `presence()` is for the two states of a partial update and is the last modifier of a property.
* A chain in which a modifier has no effect does not compile.
