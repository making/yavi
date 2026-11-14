# Reject a property that cannot be written back by the compiler

Difficulty: High

To be done before 0.18.0 is released: it changes what compiles. Start from `develop`
after pull request #578 (branch `reversible-validator`) is merged, or on top of that
branch if it is not.

## Problem

`fromMapReversible` (added by #578) fails with an `IllegalStateException` when a property
does not know its reverse: `map(f)` without `g`, a lambda, `andThen`, `anyOf`, and
`_object`, `_embedded`, `_list`, `_map`, `_rest` with such a validator. The accessors,
`map(f, g)`, the stage after `rejectingUnknownProperties()` and the union are checked by
type; this is not.

```java
Yavi.schema()
        ._string(Yavi._string("email").map(Email::new)) // no reverse
        .fromMapReversible(Contact::new, Contact::email); // compiles, throws when built
```

The owner of the project wants every chain that types can reject to be rejected by the
compiler, also at the cost of more stage types and generated code. The run-time check
was a compromise of #578, not a decision to keep.

## Goal

The chain above does not compile. `fromMapReversible` and `flatFromMapReversible` exist
only on a builder whose arguments all know their reverse. `buildReversible()` of
`Yavi.dynamicSchema()` and `ObjectInputValidator.ofReversible` likewise, as far as their
shape allows. The API that is reversible today stays as it is written in
`docs/NewFeatures_0.18.0.md` ("Writing an object back as the map it is read from"): the
examples there and `ReversibleValidatorTest` must compile unchanged, except where a
declared type becomes more specific.

## What exists (from #578)

* `am.ik.yavi.core.ReversibleValueValidator<T, X>`: `reverse(X)` returns the input.
  `ReversibleValueValidator.of(validator, g)` and `of(validator, reversible)`. Its
  `named` and `constraintOnTarget` return a `ReversibleValueValidator`. The
  implementation is `ReversedValueValidator` (package-private).
* `ValueValidator#map(f, g)` returns a `ReversibleValueValidator`; `g` returns the input
  of the validator (not the value before `f`), so it exists on every validator.
* `ValueValidator#reversible()` returns `Optional<ReversibleValueValidator<T, X>>`, the
  run-time question. It is answered by:
  * `DescribedValueValidator#reversing(...)` for `describedAs`, `preprocess`, `prefixed`,
    `formattingWith`, `constraintOnTarget`, and the sixth constructor argument of
    `DescribedNullAcceptingValueValidator` for `optional()`, `nullable()`,
    `defaultValue`, `recover`, `recoverWith` (helpers `reverseOf`, `nullSafeReverseOf`,
    `optionalReverseOf` in `ValueValidator`);
  * `DefaultArguments1Validator#reversible()`: reversible if the mapper is the singleton
    `DefaultArguments1Validator.unmapped()`. Every `XxxValidatorBuilder#build()` passes
    it through a private `create(Function1)`; `build(mapper)` and `map(f)` do not;
  * the anonymous wrappers of `Arguments1Validator` (`from`, `describedAs`,
    `constraintOnTarget`, `preprocess`), generated;
  * `LazyValueValidator#reversible()`: always present, fails when it goes back if the
    referred validator has no reverse (needed for `ValueValidator.recursive`).
* `am.ik.yavi.builder.ArgumentDefinition` has a ninth component `reverse`
  (`@Nullable Function<? super R, ? extends @Nullable Object>`), `null` if unknown. Every
  factory and modifier fills it (`reverseOf`, `nullSafe`, `optionalOf`, `presentOf`,
  `eachOf`, `valuesOf`, `written` for the name of an enum). `part(position)` wraps it in
  the record `Reading`, whose `reversible()` the `ObjectInputValidator` asks.
* `ObjectInputValidator.Reversible<X>` and `Reversible.IgnoringUnknown<X>`, created by
  `ofReversible` / `flatOfReversible(parts, mapper, accessors)`. The constructor throws
  the `IllegalStateException` for a part whose `validator().reversible()` is empty.
* `DiscriminatedUnionValidator.Reversible<X>`:
  `when(value, type, ReversibleValueValidator<Map<String, Object>, Y>)`. Already typed.
* `DynamicSchema#buildReversible()` and `DynamicSchema.Complete#buildReversible()`.
* The generated terminals `fromMapReversible` / `flatFromMapReversible` on
  `ArgumentsNValidatorBuilder` (return `Reversible.IgnoringUnknown<X>`) and on its
  `Complete` stage (return `Reversible<X>`).

## Why it is hard in Java

Whether all arguments are reversible must be in the type of the builder.

* One phantom type parameter does not work: an instance method cannot be made available
  only for one instantiation of a class type parameter. A bound `<M extends W & Marker>`
  is illegal (a type variable cannot be followed by other bounds), a method type
  parameter has no lower bound, and an evidence argument has to be passed by the caller.
* A marker per argument is 3n type parameters (48 for 16 arguments).
* The input type `Ai` cannot carry it: `apply` needs it for `ArgumentsNValidator`.
* Accessors that return the input type `Ai` are checked entirely, but the application
  then writes `u -> u.nickname().orElse(null)` and the like, which is the problem that
  the feature solves. Rejected in the todo of #578.

What is left is a second track of the generated builders.

## Design to work out

* **Two tracks from one template.** Every stage class of `ArgumentsNValidatorBuilder`
  exists once more for the reversible track, which alone has `fromMapReversible`. The
  stage classes are `Modifiable`, `Recoverable`, `Recovered`, `Keyed`, `KeyedList`,
  `KeyedRecoverable`, `KeyedOptional`, `OptionalAccepting`, `KeyedRecovered`,
  `NullAccepting`, `Complete`, `Rest` (about 3,000 lines per arity, 16 arities, plus
  `YaviSchema` for arity 0). A method that adds an argument of constraints
  (`_string(name, c -> ...)`) keeps the track. A method that takes a validator has two
  overloads: the one that takes a `ReversibleValueValidator` keeps the track, the one
  that takes a `ValueValidator` leaves it for the plain track. Decide which track is the
  base class and which the subclass, so that the plain track is not a subtype that offers
  `fromMapReversible`, and whether `Yavi.schema()` starts on the reversible track (it
  must, or the chain needs another root).
* **A static type for every reversible single value.** `Yavi._string(...)` returns
  `StringValidator<T>`, which is reversible only if its value is not mapped. The common
  `_list("tags", Yavi._string(c -> c.notBlank()))` and `_string(Yavi._string("email"))`
  must stay on the reversible track, so the validators that `build()` returns need a type
  that is a `ReversibleValueValidator` (18 classes in `am.ik.yavi.arguments`:
  `StringValidator`, `IntegerValidator`, ..., `EnumValidator`, `ObjectValidator`), and
  `map(f)`, `lazy()` must return one that is not, while `named`, `preprocess`,
  `untrimmed()` (`StringValidator`) and `ignoringCase()` (`EnumValidator`) keep it.
  Options: a nested subtype per class (`StringValidator.Unmapped`?), or making
  `XxxValidator<T>` itself reversible by carrying the reverse through `map(f, g)` only
  and returning a plain `Arguments1Validator` from `map(f)` (breaking for 0.17 code that
  assigns `map(f)` to `StringValidator<T>`: record it in `BreakingChanges_0.18.0.md`).
  `Yavi._xxx` and `XxxValidatorBuilder#build()` return types change accordingly.
* **Null-accepting and reversible together.** `_string(NullAcceptingValueValidator)`
  selects the `NullAccepting` stage by type. `optional()`, `nullable()`, `defaultValue`,
  `recover`, `recoverWith` of a reversible validator must return a type that is both, and
  `NullAcceptingValueValidator` is sealed (`DescribedNullAcceptingValueValidator`).
  Four overloads per method then: plain, null-accepting, reversible, both. Check that a
  validator that is several of them is not ambiguous (the most specific overload must
  exist for the intersection).
* **Nested validators.** `_object(name, v)`, `_embedded(v)`, `_list(name, v)`,
  `_map(name, v)`, `_rest(v)`: the reversible overloads take
  `ReversibleValueValidator`. `_embedded` takes `ValueValidator<? super Map<String,
  Object>, T>`; a wildcard input and `reverse` do not combine, so the reversible overload
  takes `ReversibleValueValidator<Map<String, Object>, T>` exactly.
* **Lazy and recursive.** `ValueValidator.recursive(self -> ...)` gives a
  `ValueValidator`, so `_list("children", self)` would leave the track. Add
  `ReversibleValueValidator.lazy(supplier)` / `recursive(...)`, whose reference is typed
  as reversible. `ReversibleValidatorTest#aUnionAndARecursiveValidatorAreWrittenBack`
  has the case.
* **Value-level lifts.** `liftList()`, `liftMap()`, `liftSet()`, `liftOptional()`,
  `liftPresence()`, and `ListValidator` / `Yavi._list` / `Yavi._map` do not keep the
  reverse today. Decide whether they get reversible forms.
* **`ArgumentDefinition`.** With two tracks the `reverse` component is never `null` on
  the reversible track. Either two definition types, or keep the nullable component and
  an internal assertion.
* **`DynamicSchema`.** `property(name, validator)` has the same two overloads; a schema
  type that is reversible (`DynamicSchema.Reversible<V>`?) has `buildReversible()`.
* **`ObjectInputValidator.ofReversible(parts, mapper, accessors)`.** `Part` holds a
  `ValueValidator<Object, R>`. A part type that holds a `ReversibleValueValidator`
  (and an accessor with it) would make the list typed; the exception stays only if the
  low-level factory keeps taking plain parts. Decide.
* **`ValueValidator#reversible()`.** Decide whether it stays once the types tell. It is
  still what a wrapper written by an application uses, and what `DescribedValueValidator`
  uses internally.
* **Kotlin.** `src/main/kotlin/am/ik/yavi/builder` has extensions; check that they still
  resolve.

## How the code is generated

* `scripts/generate-args.sh` (about 4,900 lines of bash heredocs) writes
  `arguments/ArgumentsN*.java`, `arguments/DefaultArgumentsNValidator.java`,
  `builder/ArgumentsNValidatorBuilder.java` and `builder/YaviSchema.java` (the loop
  `for i in seq 0 16` near line 2483; helper functions that print one modifier or one
  stage class are above it, from line 2171). `builder/ArgumentDefinition.java`,
  `builder/DynamicSchema.java` and the `XxxValidatorBuilder` / `XxxValidator` classes are
  written by hand.
* After editing the script: `./scripts/generate-args.sh && ./mvnw -o
  spring-javaformat:apply`. Running both on a clean tree changes nothing, so the diff is
  what the edit did. The formatter rewrites `R> held` as `R>held` and the like; do not
  match formatted text with exact strings.
* `./mvnw -o compile` runs Error Prone and NullAway in JSpecify mode on main and test
  sources. Pitfalls met in #578:
  * a lambda or a method reference that returns `@Nullable` is rejected as an argument
    for `Function<? super X, ? extends @Nullable T>` and inside `Optional.map` or a
    conditional expression. Assign it to a local variable of that type first, or return
    it from a helper method.
  * `@SuppressWarnings("NullAway")` is used where a map holds `null` although its type
    is `Map<String, Object>` (`ObjectInputValidator.Reversible#toMap`).
* `./mvnw -o test` runs 2,337 tests in about a minute; `-Dtest=ReversibleValidatorTest`
  runs the tests of this feature, with Spring JDBC and SQLite in the test scope.
  `./mvnw -o -q javadoc:javadoc` checks the links.
* The owner wants few tests: extend `ReversibleValidatorTest` rather than add classes.
  The parameterized test `aPropertyThatDoesNotKnowItsReverseIsRejectedWhenTheValidatorIsBuilt`
  lists the chains that must stop compiling; replace it with a check that they do not
  compile (`com.google.testing.compile:compile-testing` is in the test scope and
  `ObjectInputValidatorTest` or `YaviSchemaTest` may already check chains that do not
  compile; look there first).

## When done

* Update "What cannot be written" of `docs/NewFeatures_0.18.0.md` and `.ja.md`, the
  "Encoders" section of `docs/ComparisonWithRaoh.md`, "Codecs" of
  `docs/ComparisonWithZod.md` and the README section "Writing an object back as the map
  it is read from".
* Add what stops compiling for code of 0.17 to `docs/BreakingChanges_0.18.0.md` and
  `.ja.md` (for example the return type of `map(f)` of `StringValidator`).
* The pull request shows the chain above before and after.

If a point of the design turns out not to be expressible, keep the run-time check for
that point only, say so in the documentation, and record why here before closing.
