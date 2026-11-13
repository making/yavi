# Breaking changes in 0.18.0

YAVI 0.18.0 contains one breaking change:

* [A validated value is required by default](#a-validated-value-is-required-by-default)

## A validated value is required by default

An argument of `Yavi.arguments()` and a validator of a single value
(`StringValidatorBuilder` and so on) now reject `null` without `notNull()`. A value that
may be `null` has to say so with `nullable()`, which is new in 0.18.0.

```java
record Member(String name, @Nullable String nickname) {}

Arguments2Validator<String, @Nullable String, Member> validator = Yavi.arguments()
        ._string("name", c -> c.lessThanOrEqual(50))                // null was valid, is a violation now
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable() // null is valid
        .apply(Member::new);
```

`nullable()` also changes the type of the validated value to `@Nullable`, which matters
for code checked by NullAway and for Kotlin.

### Who is affected

Affected:

* An argument of `Yavi.arguments()`.
* A validator of a single value built with `StringValidatorBuilder`,
  `IntegerValidatorBuilder` and so on, with `of(name, constraints)` or with
  `wrap(b -> ...)`.
* `ValueValidator.passThrough()`, in its type only.
* `Validation` used directly: `value()` and `valueNullable()`.

Not affected:

* `ValidatorBuilder` and `Validator`. A property that is `null` is still valid unless a
  constraint such as `notNull()` rejects it:

  ```java
  Validator<Car> validator = ValidatorBuilder.<Car>of()
          .constraint(Car::manufacturer, "manufacturer", c -> c.notNull())
          .constraint(Car::seatCount, "seatCount", c -> c.greaterThanOrEqual(2)) // null is still valid
          .build();
  ```

* `ArgumentsValidatorBuilder.of(f).builder(b -> ...)` and the meta classes that the
  annotation processor generates for it.
* `validator.applicative()`.

### Migration

Steps 1 and 2 apply to every user. Step 3 applies to code checked by NullAway and to
Kotlin. Step 4 is optional.

#### 1. Add `nullable()` to a value that may be `null`

Such a value has no constraint that rejects `null` (`notNull()`, `notBlank()`,
`notEmpty()` and so on). Without `nullable()`, `null` is now a violation with
`object.notNull`.

An argument:

```java
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

// after (new API)
// optional() and defaultValue(...) accept null too and keep it out of the validated value
Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).optional() // Optional<String>
        .apply(Member::new);

Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).defaultValue("") // String
        .apply(Member::new);
```

A single value:

```java
// before
ValueValidator<String, String> memo = StringValidatorBuilder
        .of("memo", c -> c.lessThanOrEqual(100)).build();

// after
ValueValidator<String, @Nullable String> memo = StringValidatorBuilder
        .of("memo", c -> c.lessThanOrEqual(100)).build().nullable();

// after (new API)
ValueValidator<String, @Nullable String> memo = Yavi._string("memo", c -> c.lessThanOrEqual(100)).nullable();
```

#### 2. Add `nullable()` to a value constrained with `isNull()`

Without it no value is valid.

```java
// before
Yavi.arguments()._string("id", c -> c.isNull())

// after
Yavi.arguments()._string("id", c -> c.isNull()).nullable()
```

#### 3. With NullAway or Kotlin, declare the receiver of a nullable value as nullable

Java code that is not checked by a nullness checker compiles without this step.

* The parameter of the constructor or function given to `apply` that receives a
  `nullable()` argument: `@Nullable String`, in Kotlin `String?`. The other parameters
  no longer have to accept `null`.
* The type argument of a declared validator: `ValueValidator<String, @Nullable String>`,
  `Validated<@Nullable String>`.
* `ValueValidator.passThrough()`: `ValueValidator<X, @Nullable X>`.

```kotlin
data class Member(val name: String, val nickname: String?)

Yavi.arguments()
    ._string("name") { it.notBlank() }
    ._string("nickname") { it.lessThanOrEqual(20) }.nullable()
    .apply(::Member)
```

NullAway reports a parameter that is not declared `@Nullable` like this:

```
[NullAway] parameter age of referenced method is @NonNull, but parameter in functional interface method am.ik.yavi.fn.Function2.apply(String,@Nullable Integer) is @Nullable
```

See [Limitations of NullAway](NullAwayLimitations.md) if NullAway reports correct code.

#### 4. Optional cleanup

Nothing below is required. The code before works and behaves as it did.

* A `notNull()` that only made the value required can be removed. Keep it to give the
  violation of `null` another message.

  ```java
  // before
  Yavi.arguments()
          ._string("name", c -> c.notNull())
          ._integer("age", c -> c.notNull().greaterThanOrEqual(0))
          .apply(Member::new);

  // after
  Yavi.arguments()
          ._string("name")
          ._integer("age", c -> c.greaterThanOrEqual(0))
          .apply(Member::new);
  ```

* `valueNullable()` is deprecated. `value()` returns the same value, also for a success
  that holds `null`.

  ```java
  // before
  @Nullable String value = validated.valueNullable();

  // after
  @Nullable String value = validated.value();
  ```

### All changes

Behavior:

| | Before | After |
| --- | --- | --- |
| `null` for an argument or a single value whose constraints do not reject `null` | valid, the value is `null` | a violation with `object.notNull` |
| `null` for a value with a constraint that rejects it (`notBlank()`, `notNull()`) | the violation of that constraint | unchanged |
| `isNull()` on an argument or a single value | accepts only `null` | no value is valid without `nullable()` |
| `Validation.Success#value()` for a success that holds `null` | throws `NoSuchElementException` | returns `null`; `valueNullable()` is deprecated |

Types:

| | Before | After |
| --- | --- | --- |
| Parameters of the function given to `apply` | `@Nullable` for every argument | `@Nullable` only for a `nullable()` argument |
| `ValueValidator.passThrough()` | `ValueValidator<X, X>` | `ValueValidator<X, @Nullable X>` |

The changes of types are annotations, so they are binary compatible, and source
compatible for Java code that is not checked by a nullness checker.

### Why

Whether a value accepted `null` was decided inside its constraint lambda
(`c -> c.notNull()`). The type of the value was the same with and without it, so neither
the compiler nor a nullness checker could see a forgotten `notNull()`:

```java
record Member(String name, int age) {}

Arguments2Validator<String, Integer, Member> validator = Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._integer("age", c -> c.greaterThanOrEqual(0)) // notNull() is missing
        .apply(Member::new);

validator.validate("Taro", null); // before: NullPointerException, thrown out of validate
```

[NullAway](https://github.com/uber/NullAway) checks nullness through types only, so it
could not tell the two cases apart either. The parameters of the function given to
`apply` were `@Nullable` for every argument, and `apply(Member::new)` was reported even
for values validated with `notNull()`.

Now a value is required unless it says otherwise, and saying otherwise changes its type,
as in Zod and Raoh. A value is `null` only where its type says so, which NullAway and
Kotlin can check.

### Related pull requests

* [#557](https://github.com/making/yavi/pull/557) makes a validated value required and
  lets `nullable()` change its type.
* [#556](https://github.com/making/yavi/pull/556) is its precondition: the function given
  to `apply` receives the validated values without `@Nullable` parameters.
