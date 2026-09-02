# Fix the type bound of the Enum overload in ValidatorBuilderKtDsl

Difficulty: Medium

## Problem

`src/main/kotlin/am/ik/yavi/builder/ValidatorBuilderKtDsl.kt:417`

```kotlin
operator fun <E : Enum<E>?> KProperty1<T, E?>.invoke(block: EnumConstraint<T, E>.() -> Unit) =
	validatorBuilder.constraint(this, this.name) { it.apply(block) }
```

The upper bound `Enum<E>?` allows `E` to be nullable, but `EnumConstraint<T, E extends Enum<E>>`
(`src/main/java/am/ik/yavi/constraint/EnumConstraint.java:28`) requires a non-null `E`. Kotlin
2.4.10 reports this as:

```
Type argument is not within its bounds: type parameter 'E#2 (of class Enum<E : Enum<E>>)' must be
subtype of 'Enum<E#1 ...>', but actual: 'E#1 ...'
```

It is only a warning today, but becomes a compile error in language version 2.5.
See https://youtrack.jetbrains.com/issue/KTLC-358

## Direction

Declare the parameter as `<E : Enum<E>>` and keep the receiver as `KProperty1<T, E?>` so that a
nullable property is still accepted while the constraint type argument stays non-null. Confirm the
existing Kotlin DSL tests still compile and pass, and check whether the change is source-compatible
for callers.

## Verification

- `./mvnw test` must pass with no Kotlin warning at that line.
- Building with `-Dkotlin.compiler.languageVersion=2.5` (once available) must not error.
- Note: use JDK 21 or later locally; JDK 8 works too, and the version pin in `pom.xml` is
  `languageVersion`/`apiVersion` 2.2.
