# Decide whether YAVI provides encoders (a domain object to a map)

Difficulty: High

## Problem

Sections 29-32 of the Raoh tutorial are encoders, the reverse of decoders:
`object(property("id", Item::id, long_().contramap(ItemId::value)), ...)` produces a
`Map<String, Object>` for JDBC binding or JSON output, with `nested`, `list`,
`nullableProperty`, `propertyWithDefault` and `discriminate` for sealed types. Zod 4 has
codecs (`z.codec`, `z.encode`). YAVI has nothing; `docs/ComparisonWithRaoh.md` lists it
under "Not available".

## First step

Decide the scope before any design: YAVI is a validation library and an encoder validates
nothing. Arguments for: the property names and the discriminator of a
`ObjectInputValidator` / `DiscriminatedUnionValidator` are already written once and an
encoder repeats them, so a round trip defined in one place is the only form worth adding.
Arguments against: Jackson and record accessors already cover output, and 16-arity
generated code doubles.

## A use case for the decision: the first display of an edit form

A form is shown in two situations, and both need the same data: the text of each field
under the names of the schema.

* Shown again after a violation: the text that was sent. That is 070, which needs no
  encoder.
* Shown first for an object that is already stored: the text of each field of the
  object. Today the application writes it by hand, repeating the names of the schema a
  third time:

```java
record Signup(String name, Integer age, List<String> tags, Address address) {}

// today, in the application, for every form
Map<String, Object> form = Map.of("name", signup.name(), "age", String.valueOf(signup.age()),
        "tags", signup.tags(), "address", Map.of("city", signup.address().city()));
```

An encoder defined with the schema would return this map, in the shape that 070 returns
for the parameters, so a template is given one kind of data in both situations:

```java
// a possible shape
Map<String, Object> form = signupValidator.encode(signup);
// {name="Alice", age="30", tags=[a], address={city="Tokyo"}}
```

What this use case adds to the decision:

* It is the round trip that the arguments for an encoder name: the schema is written
  once and read in both directions.
* The values are text, the reverse of `StringCoercion` (`30` to `"30"`, a `LocalDate` to
  its ISO-8601 text), not the JDK types that JDBC binding or JSON output want. Decide
  whether one encoder serves both, with the target as an option as `readingWith` and
  `readingParameters` are for the input.
* A property that is not the reverse of an accessor has no encoder without more
  information: `map(Duration::ofSeconds)`, `splitBy`, `_embedded`, a value built by
  `constraintOnTarget` rules. `fromMap(Signup::new)` gives the constructor, not the
  accessors, so each property would need its accessor
  (`._string("name", Signup::name, c -> ...)`), which is the form of `ValidatorBuilder`
  and doubles the overloads of `Yavi.schema()`.
* Only maps of JDK types are returned. No type of a web framework is used.

If only this use case remains, weigh it against a record accessor per field in the
template (`{{signup.name}}`), which already works when the template does not need the
values as text under the names of the schema.

If rejected, record the decision in `docs/ComparisonWithRaoh.md` ("Not available") and
`docs/ComparisonWithZod.md`, and close this item as cancelled.
