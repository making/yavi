# An empty request parameter is reported as "must not be null"

Difficulty: Medium

## Problem

`readingParameters()` validates an empty string like `null`. For a required property the
violation then depends on whether a constraint of the property rejects `null`:

```java
ValueValidator<Map<String, List<String>>, User> formValidator = Yavi.schema()
    ._string("name", c -> c.notBlank())
    ._integer("age", c -> c.greaterThanOrEqual(0))
    .fromMap(User::new)
    .readingParameters(StringCoercion.strict().trimming());

// name=&age=   (a form submitted with empty fields)
// name: charSequence.notBlank  "name" must not be blank
// age:  object.notNull         "age" must not be null
// no key at all
// name: object.required        "name" is required
// age:  object.required        "age" is required
```

(verified with `demo-spring-mvc-yavi` against 0.18.0-SNAPSHOT.)

`"age" must not be null` is the wrong thing to tell a person who left a field of a form
empty: a form has no `null`, and a text field cannot be absent, so an empty field is the
only way a form says "no value". With 0.17 the demo wrote `c.notNull()` and got the same
message, so this is not a regression, but 0.18 is the first version that knows the input
is a parameter and could do better.

## Options

1. An empty (with `trimming()`, blank) parameter is an absent key, not an explicit
   `null`: `object.required`, `"age" is required`. Consequences to work out:
   * `optional().rejectingNull()` would accept an empty field, `nullable().required()`
     would reject it, and `presence()` would give `Absent` and never `PresentNull`. For a
     PATCH-like form "empty clears the value" would be lost; check whether
     `docs/NewFeatures_0.18.0.md` promises it ("An empty string is validated like
     `null`").
   * `name=` with `notBlank()` would turn from `charSequence.notBlank` into
     `object.required`, a visible change of message keys.
   * `coercingStrings()` of a tree reader (a CSV cell, a tree of strings) shares the rule
     in `StringCoercion` (`StringCoercion.java:198`); decide whether it follows.
2. Keep explicit `null`, and make the choice a rule of `StringCoercion`
   (for example `emptyAsAbsent()`), strict by default.
3. Keep the behaviour and only document that `object.notNull` is what an empty field of a
   property without a null-rejecting constraint reports, with the advice to override the
   message of `object.notNull` / `object.required` in the `MessageSource`.

Preference at the time of writing: 1 or 2. 3 leaves every form application to find it out.

## Scope

- Decide among the options; the decision is about `ParameterTree` and `StringCoercion`.
- Tests: extend the existing tests of `readingParameters()` for an empty and an absent
  parameter rather than adding new ones.
- `docs/NewFeatures_0.18.0.md` / `.ja.md` ("The values" in "Validating request
  parameters") and the table of absent key / explicit `null`.
- Update `createUser_ng_null` of https://github.com/making/demo-spring-mvc-yavi if the key changes.
