# Kotlin

Kotlin uses the same public API as Java. A schema is built with `Yavi.schema()`, and the nullness annotations of JSpecify are read by the Kotlin compiler, so a value that may be `null` has to be a nullable Kotlin type. The `validator<T>` DSL builds a classic `Validator`.

## Using the schema API from Kotlin

A constraint function is written with `it`, and the constructor is called with a method reference:

```kotlin
data class Member(val name: String, val nickname: String?)

val memberValidator = Yavi.schema()
    ._string("name") { it.notBlank().lessThanOrEqual(50) }
    ._string("nickname") { it.lessThanOrEqual(20) }.nullable()
    .apply(::Member)
```

`.apply(::Member)` returns an `ArgumentsNValidator`; `.fromMap(::Member)` reads a map. The Kotlin type of a parameter tells the compiler what the validator accepts, as in Java.

For a property after the first, give the constructor as a method reference (`apply(::Account)`): Kotlin cannot name the earlier argument types in the extension, so a lambda would see them as `Nothing`.

A single value validator is built with the top-level functions of `am.ik.yavi.validator`, which take the enum or object type as a type argument instead of a `Class` literal. Import them by name, and alias one of them if the functions of the same name from `am.ik.yavi.builder` are imported too:

```kotlin
import am.ik.yavi.validator._enum
import am.ik.yavi.validator._object

val role = _enum<Role>("role") { it.oneOf(Role.ADMIN) }
val id = _object<UUID>("id")
```

## The Kotlin DSL

`validator<T>` of `am.ik.yavi.builder` builds a `Validator<T>` for an object that already exists. The DSL obtains property names with Kotlin reflection, so `kotlin-reflect` must be on the runtime classpath.

```kotlin
val validator = validator<User> {
    User::name {
        notNull()
        lessThanOrEqual(20)
    }
    User::email {
        notNull()
        email()
    }
}
```

A property can be given another name, and nested objects and collections are validated with `nest` and `forEach`:

```kotlin
val validator = validator<Order> {
    (Order::customer)("Customer") {
        notNull()
    }
    Order::items forEach {
        OrderItem::name {
            notBlank()
        }
    }
    Order::customer nest {
        Customer::name {
            notBlank()
        }
    }
}
```

* `nestIfPresent` and `forEachIfPresent` validate nullable nested objects, collections, maps and arrays only when they are present.
* Temporal and enum properties have their own overloads; for example, `past()` and `oneOf(Status.ACTIVE)`.
* `onCondition` and `onGroup` add conditional and group-specific constraints, and `unwrap()` gives the underlying `ValidatorBuilder`.
* The DSL builds the classic `Validator` of [Object Validation](using-yavi.md), not the schema API. A `Validator` validates an object that exists; a schema creates it.

## Nullability

YAVI is annotated with JSpecify. A `nullable()` property is `@Nullable`, which Kotlin reads as `String?`, and a function given to `apply` or `fromMap` must declare that parameter nullable:

```kotlin
data class Member(val name: String, val nickname: String?)

val validator = Yavi.schema()
    ._string("name") { it.notBlank() }
    ._string("nickname") { it.lessThanOrEqual(20) }.nullable()
    .apply(::Member)
```

The validated type of the Kotlin DSL is not nullable: `validator<T>`, `nest` and `forEach` declare `T : Any`. Kotlin 2.1 or later reports a mismatch with the JSpecify annotations as an error; compile with `-Xjspecify-annotations=strict` to check every parameter and result.

## Coroutines

The extensions in `am.ik.yavi.fn` suspend the handling of a `Validation` result. `awaitFold`, `awaitBimap`, `awaitMap`, `awaitMapErrors` and `awaitMapError` correspond to `fold`, `bimap`, `map`, `mapErrors` and `mapError`, and require `kotlinx-coroutines-core`:

```kotlin
import am.ik.yavi.fn.awaitFold

suspend fun createUser(validated: Validated<User>): HttpStatus = validated.awaitFold(
    errorsMapper = { violations -> HttpStatus.BAD_REQUEST },
    mapper = { user -> HttpStatus.OK },
)
```
