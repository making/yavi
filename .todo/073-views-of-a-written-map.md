# Write only some of the properties back

Difficulty: High

To be decided before 0.18.0 is released if it changes `ObjectInputValidator.Reversible`
or the builders of `Yavi.reversibleSchema()`; an addition can follow later.

## Problem

`ObjectInputValidator.Reversible#toMap` (#578, #579) writes every property of the schema.
An application often wants fewer:

* a secret that is read but must not leave: a password, a token, a hash;
* a view: a list shows `id` and `name`, the detail everything; an audit log leaves out
  personal data; an `UPDATE` binds the columns that may change and not `created_at`.

Today the application removes the keys from the map that `toMap` returns
(`map.remove("password")`), which is a name written a second time and checked by nobody:
a typo removes nothing and the secret is written.

```java
ObjectInputValidator.Reversible<User> userValidator = Yavi.reversibleSchema()
        ._string("name")
        ._string("email")
        ._string("password")
        .fromMapReversible(User::new, User::name, User::email, User::password);

Map<String, Object> map = userValidator.toMap(user);
map.remove("pasword"); // compiles, runs, and the password is in the response
```

## Goal

The properties that are written are chosen without that risk. The owner asked for a
choice that is type safe if the mechanism allows it. The minimum, if it does not: a
method of `toMap` that is given the names to write, or the names not to write, and that
fails at run time for a name that the schema does not have.

## What a view is not

`toMap` is defined by the round trip: `validate(toMap(x))` is `Valid(x)`. A map without a
required property is not read back, so what writes a view is not a
`ReversibleValueValidator`: it only writes. State this in the type (a `toMap`-only
object, not a `Reversible`), and in the documentation.

## Designs to weigh

1. **A modifier where the property is defined.** `._string("password").notWritten()`
   (name to decide) on the stages of the builders of `Yavi.reversibleSchema()`. No name
   is repeated and nothing can be mistyped, so this is fully checked by the compiler. It
   covers the secret, not several views. Decide what `toMap` of such a validator is: the
   round trip no longer holds, so either `fromMapReversible` then returns a type that
   only writes plus reads (not a `ReversibleValueValidator`), or the property must accept
   `null` and is written as absent. The modifier is one more method in the generated
   stage classes (`scripts/generate-args.sh`, the reversible track only), and
   `ReversibleDynamicSchema`.
2. **Views named by a type of the application.** As `@JsonView` of Jackson:
   `._string("email").in(Views.DETAIL)`, `validator.toMap(user, Views.DETAIL)`, with an
   enum or a sealed interface of the application. A view that no property names is still
   not rejected by the compiler unless the type of the view is a type parameter of every
   builder (17 arities, 12 stages each): weigh that cost against a check when the
   validator is built.
3. **A second schema for the view.** A terminal that only writes,
   `Yavi.reversibleSchema()._string("name")._string("email").toMapOnly(User::name, User::email)`
   (no constructor is needed). Fully typed and needs no new concept, but the names are
   written again, which is what the feature set out to avoid. `_embedded` of a shared
   part reduces that.
4. **Names given to the validator, checked once.** The minimum line, made to fail early:
   `userValidator.writing(names -> names.excluding("password"))` or
   `userValidator.including("id", "name")` returns an object that writes, built once
   (a constant next to the validator), and throws an `IllegalArgumentException` when it
   is built if the schema has no property of that name, not at every `toMap`. A
   `toMap(x, names)` that checks at every call is weaker: the mistake is found only on
   the path that calls it.

Method references of the accessors (`toMap(user, User::name, User::email)`) cannot select
properties: two method references of the same method are not equal, so they cannot be
matched with the accessors given to `fromMapReversible`.

## Decided by the owner (2026-11-14)

* **The view is chosen when the object is written.** The view depends on the use case, so
  the caller of `toMap` must be able to say which one it wants. A design in which the
  properties that are written are fixed when the schema is defined is not enough on its
  own. This rules out 1 and 3 as the only mechanism, and leaves 2 (a view named in the
  schema, chosen at the call) and 4 (names chosen at the call or in a view built before).
* **A property that is never written may be marked where it is defined** (design 1), in
  addition to the views. A view cannot bring such a property back: decide whether asking
  for it is an error or has no effect, and prefer the error.

Still open: 2 or 4 for the views. With 4, a view that the use case builds once and passes
to `toMap(x, view)` fails for an unknown name when the view is built; `toMap(x, names)`
with names given at the call is the minimum line and fails at that call. Compare 2 before
deciding: it is the only one that keeps the names in one place for several views.

## Cases the design must answer

* **Nested and embedded.** A property of `_object` and of an element of `_list` / `_map`
  is addressed by a path (`address.street`, as the names of violations and
  `ParameterTree.parametersOf`). A property of `_embedded` is in the same map, under its
  own name. Excluding a nested validator as a whole and one of its properties both occur.
* **The rest (`_rest`).** Its keys are not known when the view is built. Decide whether a
  view writes the rest, never, or on request.
* **A discriminated union.** The alternatives have different properties: a name is valid
  if any alternative has it, or only if all have it. The discriminator is always written.
* **Excluded or null.** An excluded property has no entry. A named parameter of a
  statement needs the key of every parameter, so a view is for a statement with fewer
  parameters, not a way to bind `null`.
* **`ParameterTree.ofValues(validator.toMap(x))`.** A form that shows a stored object
  without its secret: the view feeds it as the full map does.
* **`ReversibleDynamicSchema`.** Names are the only handle there; design 4 fits as it is.
* **The writer of a tree (todo 071).** If an `OutputWriter` is added, a view applies to it
  in the same way; keep the selection separate from the target that is written.
* **Reading.** A view does not change what the validator reads. `Projection` in
  `am.ik.yavi.input` (package-private) already filters the properties of an input by a
  predicate of names for embedded validators; see whether it is the counterpart to reuse.

## How to verify

* Extend `ReversibleValidatorTest` (few tests: add to an existing test) with a view of a
  flat, a nested, an embedded and a union validator, and with the name that the schema
  does not have.
* What must not compile goes to the chains of
  `YaviSchemaTest#chainWithoutEffectDoesNotCompile`.
* `./mvnw -o test`, and `./mvnw -o javadoc:jar` (the goal that CI runs; `javadoc:javadoc`
  does not report unresolved references of the generated builders).

## When done

* `docs/NewFeatures_0.18.0.md` and `.ja.md`, section "Writing an object back as the map it
  is read from", the README section of the same name, and the "Encoders" section of
  `docs/ComparisonWithRaoh.md`.
* `docs/BreakingChanges_0.18.0.md` only if a type released in 0.17 changes.
