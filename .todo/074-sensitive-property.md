# Mark a property as sensitive where it is defined

Difficulty: High

To be decided before 0.18.0 is released: the mark is carried by `ValueDescription`
(`ValueDescription.Property` is a record that is new in 0.18.0; a component added after
the release breaks its constructor and its record patterns).

## Problem

A secret (a password as it is sent, a token, a hash) is kept out of what leaves the
application only by code that remembers to do so, in three places:

1. **A form that is shown again.** `ParameterTree#toMap(ValueDescription)` and
   `toParameters` (#577) return what was sent for every described property. Their Javadoc
   says so: "The returned tree holds every described value, a password included, so a
   template decides what it writes into a page" (also `docs/NewFeatures_0.18.0.md`,
   "Showing the form again in Spring MVC": "The map holds every property, a password
   included"). A template that writes every field of the map sends the password back.
   This is the path on which a password as it was sent really exists, and a
   `PropertyView` has no part in it: the validator may be one of `Yavi.schema()`.
2. **A view that excludes.** `PropertyView` (#580) of `excluding(...)` is a deny list: a
   secret that is added to the schema later is written by every such view, also by the
   view of a validator that nests the schema (`orderValidator.excluding("memo")` writes
   `user.password_hash`). `including(...)` is an allow list and does not have this
   problem, as the second encoder of Raoh does not (see below).
3. **A JSON Schema.** `JsonSchema` cannot say `writeOnly: true`, the keyword of JSON
   Schema for exactly this.

```java
ObjectInputValidator.Reversible<User> userValidator = Yavi.reversibleSchema()
        ._integer("id")
        ._string("name")
        ._string("password_hash") // added later
        .fromMapReversible(User::new, User::id, User::name, User::passwordHash);

static final PropertyView<User> AUDIT = userValidator.excluding("name"); // written before
AUDIT.toMap(user); // {id=1, password_hash=...}: nobody was asked
```

## Proposal

A modifier of a property, where it is defined (name to decide, `sensitive()` proposed):

```java
Yavi.reversibleSchema()
        ._integer("id")
        ._string("name")
        ._string("password_hash").sensitive()
        .fromMapReversible(User::new, User::id, User::name, User::passwordHash);

userValidator.toMap(user);                         // {id, name, password_hash}: the reverse, for INSERT
userValidator.excluding("name").toMap(user);       // {id}
userValidator.including("id", "password_hash");    // written, because it is named
userValidator.excluding("password_hash");          // IllegalArgumentException: has no effect
```

* **`Reversible#toMap` still writes everything.** It is the reverse and is what an
  `INSERT` binds, so the round trip holds and the validator stays a
  `ReversibleValueValidator`. This is why the mark is not `notWritten()` of todo 073,
  which was not added: a validator that never writes a required property is not
  reversible, and stating that in the type needs a second track of generated builders.
  Do not reopen that.
* **A view that excludes does not write a sensitive property**, at any depth, and naming
  it in `excluding` is an error (prefer the error to no effect). **A view that includes
  writes it only if it is named**: decide what `including("user")`, a nested object as a
  whole, does with `user.password_hash`; proposed: not written unless
  `including("user", ...)` is replaced by paths that name it, or state the opposite and
  say why.
* **`ParameterTree#toMap(description)` and `toParameters` write a sensitive property
  without a value** (an empty string, as a field that was not sent), so the form that is
  shown again does not hold it. Change the two sentences quoted above.
* **`JsonSchema` writes `writeOnly: true`.**
* The modifier is also on `Yavi.schema()`: case 1 does not need a reversible validator.

### Why not the alternatives

* *Only document "use `including` for what leaves"*: it is advice, checked by nobody, and
  does nothing for case 1.
* *A terminal that only writes, with accessors (`toMapOnly(User::id, User::name)`)*: what
  Raoh does, fully checked by the compiler, but the properties are written again; the
  owner ruled it out as the only mechanism in todo 073 (design 3). It can be added later
  and does not replace the mark.

## Design to decide

* **Where the mark is carried.** Proposed: a component of `ValueDescription.Property`
  (`Property(name, value, sensitive)` with the two-argument constructor kept), because
  the properties of an embedded validator are copied one by one
  (`ObjectInputValidator#alternativesOf` creates new `Property` objects for an optional
  embedded validator, `DiscriminatedUnionValidator#describe` copies them): a mark on the
  `Properties` or on the value would be lost or would add a case to every exhaustive
  switch over `ValueDescription` (`acceptsNull`, `named`, `acceptsAbsent`, `JsonSchema`,
  `ParameterTree`, `PropertyView#collect`). Compare a wrapper variant
  (`ValueDescription.Sensitive(value)`) before deciding.
* **How a property gets it.** `ObjectInputValidator.Property` and `PresenceProperty` are
  public records (name, validator, strictness, messages): a component, or a field of
  `PropertyMessages`, which already holds what is said about a property and is passed
  through `ArgumentDefinition#withMessages`. In the builders: `ArgumentDefinition` and
  `ReversibleArgumentDefinition`, the stage classes of both tracks
  (`scripts/generate-args.sh`), `DynamicSchema` and `ReversibleDynamicSchema`.
* **What the compiler rejects** (the owner wants a chain without effect rejected by type
  wherever types can, even at the cost of a stage): `sensitive()` twice, `sensitive()` of
  `_rest` and of `_embedded` (mark the properties of the embedded validator instead).
  Weigh the number of stage classes: the simplest is to offer `sensitive()` next to
  `label(...)` and the other modifiers of `MessageModifiers`, and to see how those reject
  a repetition.
* **`PropertyView`.** It prunes by a shape that is built from the paths, and writes a
  nested object that is not named as a whole (`Whole`), without looking at its
  description. A sensitive property at any depth means that a whole value is pruned by
  its description when it is written. A structure that contains itself
  (`ValueDescription.Reference`) cannot be expanded when the view is built: walk the
  description lazily next to the written value. The accessor of a sensitive property of
  the validator itself is not called, as for an excluded one
  (`ObjectInputValidator.Reversible#write(object, names)`).
* **A union.** A property is sensitive if the alternative that writes the object marks
  it; with alternatives that disagree, decide (proposed: sensitive if any alternative
  that has the property marks it, so that the mistake is on the safe side). The
  discriminator cannot be sensitive.
* **Not in scope unless it is cheap**: `ConstraintViolation#violatedValue()` / `args()`
  and a message format that shows the value hold a rejected password too. Say in the
  documentation what the mark does not cover, or file it separately.

## Context from the work on todo 073 (PR #580)

* `PropertyView<X>` in `am.ik.yavi.input`: `including` / `excluding` of
  `ObjectInputValidator.Reversible` and `DiscriminatedUnionValidator.Reversible`, names
  checked against `description()` when the view is built, only writes.
  `ValueDescription.AnyOf#discriminator` was added for it in the same way that this item
  adds a mark: a fact of the schema that a consumer of the description needs.
* Compared with Raoh (`../raoh-java`, `net.unit8.raoh.encode`): it has no view. A subset
  is a second `object(property(...), ...)`, which is an allow list checked by the
  compiler (getter references) with the names written again and no round trip. YAVI
  writes the names once and has the round trip, but `excluding` is a deny list and the
  names are checked when the class is loaded. This item closes the first difference; the
  second stays.
* Hashing is not done by a validator: a hash does not go back (`map(f)` without a reverse
  does not compile in a reversible schema). The application writes
  `.params(view.toMap(user)).param("password_hash", encoder.encode(user.password()))`
  (documented in "Writing some of the properties"). So the secret of a reversible schema
  is either the password as it was sent, in the schema of a form, or the hash, in the
  schema of a row, and the mark is for both.
* What stays open after this item: `userValidator.toMap(user)` called directly writes a
  sensitive property. That is intended (see above); the documentation tells to publish
  the view.

## How to verify

* `ReversibleValidatorTest`: extend the existing tests (a view of a flat, a nested, an
  embedded, a union and a recursive validator already exist) with a sensitive property;
  the form test with `ParameterTree#toParameters` of submitted parameters.
* The JSON Schema test that covers the keywords: `writeOnly`.
* What must not compile goes to `YaviSchemaTest#chainWithoutEffectDoesNotCompile`.
* `./mvnw -o test` and `./mvnw -o javadoc:jar`.

## When done

* `docs/NewFeatures_0.18.0.md` and `.ja.md`: "Writing some of the properties", "Showing
  the form again in Spring MVC", "Describing a validator and generating a JSON Schema";
  the README sections of the same names; "Encoders" of `docs/ComparisonWithRaoh.md`.
* `docs/BreakingChanges_0.18.0.md` only if a type released in 0.17 changes.
