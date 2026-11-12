# Transform a value before its constraints (trim, lower case, normalize)

Difficulty: Medium

## Problem

Raoh chains transforms with constraints: `string().trim().toLowerCase().email()`,
`string().normalize().maxLength(1)`. Zod has `.trim()`, `.toLowerCase()`,
`z.preprocess`. A YAVI constraint never changes the value, so the Raoh tutorial needs two
validators per transformed value (`docs/ComparisonWithRaoh.md`, "Transforms and
normalization"):

```java
StringValidatorBuilder.of("", c -> c.notNull()).build()
    .map(s -> s.trim().toLowerCase(Locale.ROOT))
    .andThen(StringValidatorBuilder.of("", c -> c.notBlank().email()).build());
```

* It cannot be written in place: `_string("email", c -> ...)` has no transform.
* `compose(String::trim)` reads better but drops the input type and the description, so
  it needs `untyped(String.class)` again and is unknown to `JsonSchema`.
* Size constraints count in NFC by default, but the produced value stays decomposed.

## Goal

A transform of the same type that keeps the input type and the description, usable on a
validator and on an argument of the builders. Sketch:

```java
// ValueValidator: applied to a non-null input before validation
default ValueValidator<T, X> preprocess(UnaryOperator<T> f)

// builders
._string("email", c -> c.notBlank().email()).preprocess(s -> s.trim().toLowerCase(Locale.ROOT))
```

## Design questions

* Whether common string transforms get names (`trimmed()`, `lowerCased()`,
  `normalized(Form)`) on `StringValidatorBuilder` / the string argument, or only the
  general hook exists.
* With `readingWith(reader)` the transform must run after the type check of the reader,
  on the typed value.
* The violated value in the violation: the transformed one.
* Interaction with string coercion (`" 42 "` for an integer is `object.type` today): a
  transform on an `Integer` argument cannot trim the text; see 038.
