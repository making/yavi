# Breaking changes in 0.18.0

[日本語](BreakingChanges_0.18.0.ja.md)

YAVI 0.18.0 contains the following breaking changes from 0.17, the ones that affect the
most users first:

* [Java 21 is required](#java-21-is-required)
* [A validated value is required by default](#a-validated-value-is-required-by-default)
* [`ViolationDetail` is a record](#violationdetail-is-a-record)
* [The violations of a lifted collection are located under the index](#the-violations-of-a-lifted-collection-are-located-under-the-index)
* [The message of a value without a name](#the-message-of-a-value-without-a-name)
* [The nullness annotations are those of JSpecify](#the-nullness-annotations-are-those-of-jspecify)
* [Other changes](#other-changes)

In the examples, "before" is code written for 0.17, "after" is the same code changed so
that it compiles and behaves the same with 0.18, and "after (new API)" uses an API that
is new in 0.18. See [New features in 0.18.0](NewFeatures_0.18.0.md) for the new APIs.

"after" writes `Yavi.schema()` where "before" writes `Yavi.arguments()`. `Yavi.arguments()`
still compiles with 0.18 but is deprecated; see
[`Yavi.arguments()` is deprecated and `YaviArguments` is `YaviSchema`](#yaviarguments-is-deprecated-and-yaviarguments-is-yavischema).

## Java 21 is required

YAVI 0.18.0 is compiled for Java 21. 0.17 ran on Java 8.

### Who is affected

* An application that runs on Java 8 to 20. It stays on 0.17.
* Code compiled against 0.17, such as a library that uses YAVI. `Validation` is an
  interface now and the methods of `Yavi.arguments()` return other types, so it fails
  with 0.18 at run time until it is recompiled.

### Migration

#### 1. Run on Java 21 or later

#### 2. Recompile the code that uses YAVI

This is needed also where the source does not change.

### Related pull requests

* [#532](https://github.com/making/yavi/pull/532) raises the baseline to Java 21.

## A validated value is required by default

An argument of `Yavi.schema()` and a validator of a single value
(`StringValidatorBuilder` and so on) now reject `null` without `notNull()`. A value that
may be `null` has to say so with `nullable()`, which is new in 0.18.0.

```java
record Member(String name, @Nullable String nickname) {}

Arguments2Validator<String, @Nullable String, Member> validator = Yavi.schema()
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
Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable()
        .apply(Member::new);

// after (new API)
// optional() and defaultValue(...) accept null too and keep it out of the validated value
Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).optional() // Optional<String>
        .apply(Member::new);

Yavi.schema()
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
Yavi.schema()._string("id", c -> c.isNull()).nullable()
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

Yavi.schema()
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
  Yavi.schema()
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

## `ViolationDetail` is a record

`ConstraintViolations#details()` returns records that say which value failed. Their
accessors have other names, and so have the properties of the JSON that a JSON library
writes from them.

```java
List<ViolationDetail> details = violations.details();
// [{"name": "items[1].name", "pointer": "/items/1/name", "messageKey": "charSequence.notBlank",
//   "message": "\"items[1].name\" must not be blank", "args": ["items[1].name", ""]}]
```

### Who is affected

Affected:

* Code that calls `getKey()`, `getArgs()` or `getDefaultMessage()` of a
  `ViolationDetail`, or its constructor.
* A client of an API that returns `details()` as JSON.
* Code that modifies the list returned by `details()`.

Not affected:

* `ConstraintViolation`: `name()`, `messageKey()`, `args()` and `message()`.

### Migration

#### 1. Rename the accessors

```java
// before
for (ViolationDetail detail : violations.details()) {
    String key = detail.getKey();
    Object[] args = detail.getArgs();
    String message = detail.getDefaultMessage();
}

// after
for (ViolationDetail detail : violations.details()) {
    String key = detail.messageKey();
    List<Object> args = detail.args();
    String message = detail.message();
}

// after (new API)
for (ViolationDetail detail : violations.details()) {
    String name = detail.name();       // items[1].name
    String pointer = detail.pointer(); // /items/1/name, a JSON Pointer
}
```

#### 2. Follow the new JSON in the client, or keep the JSON of 0.17

```jsonc
// before
[{"key": "charSequence.notBlank", "args": ["items[1].name", ""], "defaultMessage": "\"items[1].name\" must not be blank"}]

// after
[{"name": "items[1].name", "pointer": "/items/1/name", "messageKey": "charSequence.notBlank",
  "message": "\"items[1].name\" must not be blank", "args": ["items[1].name", ""]}]
```

To keep the JSON of 0.17, map the details to a type of your own:

```java
record LegacyDetail(String key, List<Object> args, String defaultMessage) {}

List<LegacyDetail> details = violations.details()
        .stream()
        .map(detail -> new LegacyDetail(detail.messageKey(), detail.args(), detail.message()))
        .toList();
```

### All changes

| | Before | After |
| --- | --- | --- |
| Message key | `getKey()`, JSON `key` | `messageKey()`, JSON `messageKey` |
| Formatted message | `getDefaultMessage()`, JSON `defaultMessage` | `message()`, JSON `message` |
| Arguments | `Object[] getArgs()` | `List<Object> args()`, unmodifiable |
| Name and JSON Pointer of the value | not available | `name()`, `pointer()` |
| Type | a class | a sealed interface of the records `ViolationDetail.Simple` and `ViolationDetail.NoAlternativeMatched` |
| Details of the alternatives of `ValueValidator.anyOf` | not available | `alternatives()` of `ViolationDetail.NoAlternativeMatched`, JSON `alternatives` |
| Constructor | `ViolationDetail(key, args, defaultMessage)` | `ViolationDetail.Simple(name, pointer, messageKey, message, args)` |
| List returned by `details()` | modifiable | unmodifiable |

### Why

A detail did not say which value failed: the name was only the first element of `args`.
`defaultMessage` was the formatted message, not the default one.

### Related pull requests

* [#551](https://github.com/making/yavi/pull/551) makes `ViolationDetail` a record and
  adds `messagesByName()`, `messagesByPointer()` and `pointer()`.
* [#566](https://github.com/making/yavi/pull/566) makes it a sealed interface of two
  records, of which `ViolationDetail.NoAlternativeMatched` has `alternatives`.

## The violations of a lifted collection are located under the index

`liftList()`, `liftSet()` and `liftCollection()` put the index of an element before the
name of the element validator, where they put it after the name.

```java
Yavi._string("tag", c -> c.notBlank())
        .liftList()
        .validate(List.of("java", "")); // [1].tag, was tag[1]
```

The code compiles as it is. The names of the violations change.

### Who is affected

Affected:

* Code that looks up a violation of a lifted validator by its name, such as the field of
  a form or an assertion of a test, where the element validator has a name.
* Code that calls `indexed(int)`.

Not affected:

* `ValidatorBuilder#forEach`.
* A lifted validator whose element has the empty name. It reports `[1]` as before.

### Migration

#### 1. Name the collection, not the element

```java
// before
StringValidatorBuilder.of("tag", c -> c.notBlank()).build()
        .liftList()
        .validate(List.of("java", "")); // tag[1]

// after
StringValidatorBuilder.of("", c -> c.notBlank()).build()
        .liftList().prefixed("tag")
        .validate(List.of("java", "")); // tag[1]

// after (new API)
Yavi._string(c -> c.notBlank())
        .liftList().prefixed("tag")
        .validate(List.of("java", "")); // tag[1]
```

An element that is an object keeps the names of its properties. With `prefixed`, it is
named like an element of `ValidatorBuilder#forEach`:

```java
// before
addressValidator.applicative().liftList().validate(addresses); // street[1]

// after
addressValidator.applicative().liftList().prefixed("addresses").validate(addresses); // addresses[1].street
```

#### 2. Replace `indexed(int)` with `prefixed(int)`

`indexed(int)` is removed from `ConstraintViolation`, `Validated`, `ValueValidator` and
`Arguments1Validator`. `prefixed(int)` of `ConstraintViolation` and `Validated` puts the
index before the name.

```java
// before
validated.indexed(1); // tag[1]

// after
validated.prefixed(1); // [1].tag
```

### Why

The name did not follow the structure of the input: the property `street` of the second
address was `street[1]`, which reads as the second element of `street`.

### Related pull requests

* [#544](https://github.com/making/yavi/pull/544) locates the violations of lifted
  elements under the index and removes `indexed(int)`.

## The message of a value without a name

The message of a violation whose name is empty says "The value" where it had the quoted
empty name.

```java
Yavi._string(c -> c.notBlank()).validate(""); // The value must not be blank
```

### Who is affected

Affected:

* An assertion on, or a display of, the default message of a validator with the empty
  name: `StringValidatorBuilder.of("", ...)` and so on.

Not affected:

* The message key and the arguments. The name is still `""`.
* A message that a `MessageSource` formats with a format of its own.
* A violation with a name (`"tags[0]" must not be blank`).

### Migration

#### 1. Update the expected messages

```java
// before
StringValidatorBuilder.of("", c -> c.notBlank()).build().validate("");
// "" must not be blank

// after
StringValidatorBuilder.of("", c -> c.notBlank()).build().validate("");
// The value must not be blank

// after (new API)
Yavi._string(c -> c.notBlank()).validate("");
// The value must not be blank
```

### Related pull requests

* [#546](https://github.com/making/yavi/pull/546) adds the validators of a single value
  to `Yavi` and changes the message.

## The nullness annotations are those of JSpecify

The package `am.ik.yavi.jsr305` is removed. YAVI is annotated with
[JSpecify](https://jspecify.dev) (`@NullMarked`, `@Nullable`) instead.

### Who is affected

Affected:

* Code that imports an annotation of `am.ik.yavi.jsr305`.
* Code checked by NullAway, and Kotlin (2.1 or later reports a mismatch with JSpecify
  annotations as an error). They now check the nullness of every parameter and result of
  YAVI.

Not affected:

* Any other Java code.

### Migration

#### 1. Import the annotations of JSpecify

YAVI does not bring `org.jspecify:jspecify`. Add it to your dependencies.

```java
// before
import am.ik.yavi.jsr305.Nullable;

// after
import org.jspecify.annotations.Nullable;
```

#### 2. With NullAway or Kotlin, fix the reported nullness

* A value that may be `null` is declared so, as described in
  [step 3 of the required default](#3-with-nullaway-or-kotlin-declare-the-receiver-of-a-nullable-value-as-nullable).
* The validated type of the Kotlin DSL is not nullable: `validator<T> { ... }`, `nest`
  and `forEach` declare `T : Any`.

See [Limitations of NullAway](NullAwayLimitations.md) if NullAway reports correct code.

### Related pull requests

* [#534](https://github.com/making/yavi/pull/534) replaces JSR 305 with JSpecify.
* [#556](https://github.com/making/yavi/pull/556) reduces `@Nullable` to the places where
  `null` is part of the contract.

## Other changes

These affect few users.

### `Validation` is an interface

`Validation` is an interface and `Validated` a sealed interface. A class that extends
`Validation` implements it instead, and `map` / `flatMap` have one type parameter. Code
that calls them without type arguments on a `Validated` compiles as it is.

```java
// before
class Result<T> extends Validation<String, T> { ... }
Validated<Integer> number = validated.<Integer, Validated<Integer>>map(Integer::valueOf);

// after
class Result<T> implements Validation<String, T> { ... }
Validated<Integer> number = validated.<Integer>map(Integer::valueOf);
```

Related pull requests: [#537](https://github.com/making/yavi/pull/537),
[#549](https://github.com/making/yavi/pull/549)

### A combination of validation results returns a declared type

`apply` of a combination returned a type that the caller chose, and the compiler did not
check the choice. It now returns the type it is declared with:

* `Validated#combine` returns the new `ValidatedCombiningN`, whose `apply` returns
  `Validated<R>`.
* `Validation#apply`, `CombiningN#apply` and `Validations.apply` return
  `Validation<E, R>`.
* `Validation#yieldSuccess` and `yieldFailure` are removed.

Code that combines `Validated` results with `combine` and assigns the result to a
`Validated` compiles as it is. The result is a `Validated` also with `var` and in a
`switch` now.

```java
// before
Validated<Address> address = Validations.apply(Address::new, country, street);
Validated<Address> address = Validations.combine(country, street).apply(Address::new);
Validated<Address> address = country.combine(street).<Address, Validated<Address>>apply(Address::new);

// after
Validated<Address> address = country.combine(street).apply(Address::new);
```

A combination that starts with a `Validation<ConstraintViolation, T>` returns a
`Validation`, which is not assignable to a `Validated`. Convert the first one with
`Validated.of`. Before, the last validation of the combination decided the type at run
time, and the assignment failed with a `ClassCastException` if that one was not a
`Validated`.

```java
Validation<ConstraintViolation, Country> country = ...;

// before
Validated<Address> address = country.combine(street).apply(Address::new);

// after
Validated<Address> address = Validated.of(country).combine(street).apply(Address::new);
```

An implementation of `Validation` that overrode `yieldSuccess` and `yieldFailure` to
return its own type overrides `map`, `apply` and `combine` instead, as `Validated` does.

Related pull request: [#558](https://github.com/making/yavi/pull/558)

### Deprecated and redundant members are removed

| Removed | Use instead |
| --- | --- |
| `ValueValidator#andThen(Function)`, `ArgumentsNValidator#andThen(Function)` | `map(Function)`. `andThen(ValueValidator)` is not affected. |
| `new ConstraintViolation(...)` | `ConstraintViolation.builder()` or `ConstraintViolation.of(...)` |
| `new SimpleMessageFormatter()` | `SimpleMessageFormatter.getInstance()` |
| `new Validations()`, `new Functions()` | Nothing. They have static methods only. |
| The field `Validation.Success#value` | `value()` |

```java
// before
new ConstraintViolation("name", "notBlank", "\"{0}\" must not be blank", new Object[] { "name", "" },
        new SimpleMessageFormatter(), Locale.ENGLISH);

// after
ConstraintViolation.builder()
        .name("name")
        .messageKey("notBlank")
        .defaultMessageFormat("\"{0}\" must not be blank")
        .args("name", "")
        .locale(Locale.ENGLISH)
        .build();
```

`Validation.Success` and `Validation.Failure` are records. `toString()` returns
`Success[value=a]` and `Failure[errors=[e]]`.

Related pull request: [#558](https://github.com/making/yavi/pull/558)

### The constructors of `ArgumentsNValidatorBuilder` are removed

```java
// before
new Arguments2ValidatorBuilder<>(nameValidator, ageValidator).apply(Member::new);

// after
Yavi.schema()._string(nameValidator)._integer(ageValidator).apply(Member::new);
```

Related pull request: [#538](https://github.com/making/yavi/pull/538)

### An enum and an object of a schema are added with their type

A property added without its type could not be read from a map: `fromMap` threw an
`IllegalStateException` when the validator was built. The methods without a type are
removed, so a schema serves `apply` and `fromMap` alike.

| Removed | Use instead |
| --- | --- |
| `_enum(name)`, `_enum(name, constraints)` of the schema builders | `_enum(name, type)`, `_enum(name, type, constraints)` |
| `_object(name)`, `_object(name, constraints)` of the schema builders | `_object(name, type)`, `_object(name, type, constraints)`; for a type without a class literal, `_object(Yavi.<List<String>>_object(name, constraints))` |
| `EnumValidatorBuilder.of(name, constraints)`, `EnumValidatorBuilder.wrap(builder)` | `EnumValidatorBuilder.of(name, type, constraints)`, `EnumValidatorBuilder.wrap(type, builder)` |
| `new EnumValidator<>(validator, mapper)` | `new EnumValidator<>(validator, mapper, type)` |

```java
// before
Yavi.arguments()
        ._string("name")
        .<Role>_enum("role", c -> c.oneOf(Role.USER, Role.ADMIN))
        .<Address>_object("address")
        .apply(User::new);

// after
Yavi.schema()
        ._string("name")
        ._enum("role", Role.class, c -> c.oneOf(Role.USER, Role.ADMIN))
        ._object("address", Address.class)
        .apply(User::new);
```

The description of such an enum now holds its constants, so its JSON Schema has `enum`.

Related pull request: [#565](https://github.com/making/yavi/pull/565)

### Two arguments of `apply` cannot have the same name

`apply` and `flatApply` throw an `IllegalArgumentException` when two arguments have the
same name, as `fromMap` does: the validator would describe an object in which one
property stands for two arguments. Give each argument its own name.

```java
// before: built a validator whose JSON Schema had one property "value" for both arguments
Yavi.arguments()._integer("value")._integer("value").apply(Range::new);

// after
Yavi.schema()._integer("from")._integer("to").apply(Range::new);
```

Related pull request: [#565](https://github.com/making/yavi/pull/565)

### `Yavi.arguments()` is deprecated and `YaviArguments` is `YaviSchema`

`Yavi.schema()` is the new name of `Yavi.arguments()`, which still works and is
deprecated. The builder now describes an input that is read from typed arguments, a map,
a JSON tree or request parameters, so its type `YaviArguments` is renamed to
`YaviSchema`. Code that declares a variable of that type must rename it.

```java
// before
YaviArguments builder = Yavi.arguments();

// after
YaviSchema builder = Yavi.schema();
```

### `Arguments1` to `Arguments16` are records

`toString()` returns `Arguments2[arg1=a, arg2=1]`. It was `Arguments2{arg1=a, arg2=1}`.

Related pull request: [#549](https://github.com/making/yavi/pull/549)

### `emoji()` counts extended grapheme clusters

A few sequences that are not emoji count as one where they counted as two: `"\r\n"`, or
a half-width katakana with a voiced sound mark (`ｶﾞ`). The class `Emoji` is removed.

Related pull request: [#533](https://github.com/making/yavi/pull/533)

### The message key separator applies under `forEach`

A `ValidatorBuilder` with a message key separator other than `.` uses it in the names
under `forEach(..., builder -> ...)` too.

```java
new ValidatorBuilder<Order>("/")
        .forEach(Order::addresses, "addresses", b -> b.constraint(Address::street, "street", c -> c.notBlank()))
        .build();
// before: addresses[0].street
// after:  addresses[0]/street
```

Related pull request: [#537](https://github.com/making/yavi/pull/537)

### The constructors of the nested validators take the path

The constructors of `NestedValidator`, `NestedCollectionValidator` and
`NestedConstraintPredicates` take the path as an additional argument.

Related pull request: [#537](https://github.com/making/yavi/pull/537)
