# Breaking changes in 0.18.0

YAVI 0.18.0 contains breaking changes. This page explains, for each of them, why it was
made, which pull requests made it, how to migrate and what the code looks like before and
after.

The page is written change by change. It covers so far:

* [A validated value is required by default](#a-validated-value-is-required-by-default)

## A validated value is required by default

An argument of `Yavi.arguments()` and a validator of a single value (`Yavi._string(...)`,
`StringValidatorBuilder` and so on) now reject `null` without `notNull()`. An argument
that may be `null` says so with `nullable()`, `optional()` or `defaultValue(...)`, and
`nullable()` changes the type of the validated value to `@Nullable`.

`ValidatorBuilder` is not affected. It validates an object that exists, and a property
that is `null` is valid unless a constraint such as `notNull()` rejects it.
`ArgumentsValidatorBuilder`, which validates raw arguments with a `ValidatorBuilder`, is
not affected either.

### Background

Whether an argument accepted `null` was decided inside its constraint lambda
(`c -> c.notNull()`). The type of the argument was the same with and without it, so
neither the compiler nor a nullness checker could see a forgotten `notNull()`:

```java
record Member(String name, int age) {}

Arguments2Validator<String, Integer, Member> validator = Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._integer("age", c -> c.greaterThanOrEqual(0)) // notNull() is missing
        .apply(Member::new);

validator.validate("Taro", null); // before: NullPointerException, thrown out of validate
```

The same mistake made an absent key of a map produce a valid object that held `null`,
and a JSON Schema that did not list the property as `required`:

```java
record Account(String name, Integer age) {}

MapInputValidator<Account> validator = Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._integer("age", c -> c.greaterThanOrEqual(0))
        .fromMap(Account::new);

validator.validate(Map.of("name", "Taro")); // before: Valid[Account[name=Taro, age=null]]
```

`nullable()` did not change the type either, so a value that could be `null` was handed
to code that declared it as not `null`, in Java with NullAway and in Kotlin alike.

Now an argument is required unless it says otherwise, and saying otherwise changes its
type, as in Zod and Raoh. A value is `null` only where its type says so.

#### With NullAway

A project that uses [NullAway](https://github.com/uber/NullAway) expects that code
without a report does not throw `NullPointerException`. YAVI could not keep that promise
for the values it hands over, because NullAway checks nullness through types: the
annotations of parameters, of return types and of type arguments. A fact that exists only
as a call inside a lambda (`c.notNull()`) is not part of any type, so NullAway cannot
carry it to the function given to `apply` a few calls later. That left two ways to
declare the parameters of that function, and both were wrong for some code:

* **`@Nullable`, as before #556.** Every function had to accept `null`, even for a value
  validated with `notNull()`. `apply(Member::new)` was reported for a constructor whose
  parameters are not `@Nullable`, so the user wrapped every argument in
  `Objects.requireNonNull` or declared every property of the domain class `@Nullable`.
* **Not `@Nullable`, as after #556.** `apply(Member::new)` compiled, also where
  `notNull()` was forgotten or `nullable()` was applied. NullAway reported nothing for
  the two validators below, and both threw `NullPointerException` for a `null` age. YAVI
  itself needed `@SuppressWarnings("NullAway")` in five places that passed such a value
  on.

```java
Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._integer("age", c -> c.greaterThanOrEqual(0)) // notNull() is missing
        .apply(Member::new);

Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._integer("age", c -> c.greaterThanOrEqual(0)).nullable()
        .apply(Member::new);
```

Now the fact is a type argument, which NullAway does follow. The first validator rejects
`null`, so its type `Integer` is true and `Member::new` is safe. The second one has the
type `@Nullable Integer` for its age and is reported:

```
[NullAway] parameter age of referenced method is @NonNull, but parameter in functional interface method am.ik.yavi.fn.Function2.apply(String,@Nullable Integer) is @Nullable
```

The same holds for a single value: `validator.nullable().validated(input).length()` is
reported as a dereference of a `@Nullable` expression, and without `nullable()` no check
of the result is needed. The five suppressions are removed, so NullAway checks YAVI
itself in the same way.

### Related pull requests

* [#557](https://github.com/making/yavi/pull/557) makes a validated value required and
  lets `nullable()` change its type.
* [#556](https://github.com/making/yavi/pull/556) is its precondition: the function given
  to `apply` and `build` receives the validated values without `@Nullable` parameters.

### What changes

| | Before | After |
| --- | --- | --- |
| `null` for an argument or a single value whose constraints do not reject `null` | valid, the value is `null` | a violation with `object.notNull` |
| An absent key of a map for such an argument | valid, the value is `null` | a violation with `object.required` |
| `null` for a value with a constraint that rejects it (`notBlank()`, `notNull()`) | the violation of that constraint | unchanged |
| Type of the value after `nullable()` | `R` | `@Nullable R` |
| Type of the value after `optional()`, `presence()` | `Optional<R>`, `Presence<R>` | `Optional<@NonNull R>`, `Presence<@NonNull R>` |
| `ValueValidator.passThrough()` | `ValueValidator<X, X>` | `ValueValidator<X, @Nullable X>` |
| Value of `_rest()` | `Map<String, Object>` | `Map<String, @Nullable Object>` |
| Function of `map` | `Function<? super X, ? extends @Nullable X2>` | `Function<? super X, ? extends X2>`, where `X2` may be a nullable type |
| `isNull()` on an argument or a single value | accepts only `null` | no value is valid without `nullable()` |
| Description of a required value whose constraints accept `null` | no constraint, not `required` in the JSON Schema | the constraint `object.notNull`, `required` in the JSON Schema |
| `Validation.Success#value()` for a success that holds `null` | throws `NoSuchElementException` | returns `null`; `valueNullable()` is deprecated |

The changes of types are annotations and bounds that erase to `Object`, so they are
binary compatible. Java code that is not checked by a nullness checker is source
compatible with them. The change of behavior affects every user.

### Migration

What has to change:

1. Add `.nullable()` to the arguments and single values that are meant to accept `null`:
   those without `notNull()`, `notBlank()`, `notEmpty()` or another constraint that
   rejects `null`, and without `optional()`, `defaultValue(...)` or `recover(...)`. Use
   `.optional()` or `.defaultValue(...)` instead if that fits better.
2. Add `.nullable()` to an argument or a single value that is constrained with
   `isNull()`.
3. With NullAway or Kotlin, declare what receives the value of a `nullable()` argument
   as nullable: the parameter of the constructor or function given to `apply`, `fromMap`
   and `build` (`@Nullable String`, in Kotlin `String?`), and the type argument of a
   declared validator (`ValueValidator<String, @Nullable String>`). Declare the `null`
   that `ValueValidator.passThrough()` and `_rest()` pass on in the same way:
   `ValueValidator<X, @Nullable X>` and `Map<String, @Nullable Object>`.
4. With NullAway, see [Limitations of NullAway](NullAwayLimitations.md) if correct code
   is reported.

A validator of the elements of `_list` or of the values of `_map` is a single value, so
steps 1 to 3 apply to it too: `_list("tags", Yavi._string().nullable())` produces a
`List<@Nullable String>`.

What may stay as it is:

* **An existing `notNull()`.** A value that was already required with `notNull()`,
  `notBlank()` or `notEmpty()` is validated as before and reports the same violation, so
  that code needs no change. `notNull()` is redundant where it only made the value
  required, and removing it makes the code shorter, but nothing depends on it. It is
  still the way to give the violation of `null` another message.
* **`valueNullable()`.** It is deprecated and returns what `value()` returns. Replace it
  with `value()` when convenient.

### Before and after

An argument that must be present. The code before still works and behaves the same;
`notNull()` can be removed:

```java
// before
Yavi.arguments()
        ._string("name", c -> c.notNull())
        ._integer("age", c -> c.notNull().greaterThanOrEqual(0))
        .apply(Member::new);

// after (optional)
Yavi.arguments()
        ._string("name")
        ._integer("age", c -> c.greaterThanOrEqual(0))
        .apply(Member::new);
```

An argument that may be `null`:

```java
record Member(String name, @Nullable String nickname) {}

// before
Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20))
        .apply(Member::new);

// after
Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable()
        .apply(Member::new);
```

In Kotlin the parameter of a `nullable()` argument is a nullable type, and every other
parameter is not:

```kotlin
data class Member(val name: String, val nickname: String?)

Yavi.arguments()
    ._string("name") { it.notBlank() }
    ._string("nickname") { it.lessThanOrEqual(20) }.nullable()
    .apply(::Member)
```

A single value. Only `memo`, which accepted `null`, has to change; `code` and `integer`
work as they were written:

```java
// before
StringValidator<String> code = Yavi._string("code", c -> c.notNull().lessThanOrEqual(8));
ValueValidator<Object, Integer> integer = Yavi._integer(c -> c.notNull()).untyped();
ValueValidator<String, String> memo = Yavi._string("memo", c -> c.lessThanOrEqual(100));

// after
StringValidator<String> code = Yavi._string("code", c -> c.lessThanOrEqual(8));
ValueValidator<Object, Integer> integer = Yavi._integer().untyped();
ValueValidator<String, @Nullable String> memo = Yavi._string("memo", c -> c.lessThanOrEqual(100)).nullable();
```

A partial update, where an absent key is told from `null`. Only `bio`, which can be
cleared, has to change:

```java
// before
Yavi.arguments()
        ._string("nickname", c -> c.notNull().lessThanOrEqual(50)).presence() // cannot be cleared
        ._string("bio", c -> c.lessThanOrEqual(500)).presence()
        .fromMap(ProfilePatch::new);

// after
Yavi.arguments()
        ._string("nickname", c -> c.lessThanOrEqual(50)).presence() // cannot be cleared
        ._string("bio", c -> c.lessThanOrEqual(500)).nullable().presence()
        .fromMap(ProfilePatch::new);
```

A value that must be `null`:

```java
// before
Yavi.arguments()._string("id", c -> c.isNull())

// after
Yavi.arguments()._string("id", c -> c.isNull()).nullable()
```

A successful validation that holds `null`:

```java
// before
Validated<String> validated = validator.validate(input);
@Nullable String value = validated.valueNullable();

// after
Validated<@Nullable String> validated = validator.validate(input);
@Nullable String value = validated.value();
```
