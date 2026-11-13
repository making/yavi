# Decide whether reading parameters can trim text values

Difficulty: Low

## Problem

`StringCoercion.trimming()` trims a string that is read as a number, a boolean, an enum
constant or a temporal value, and reads a blank string as `null`. A `String` value is left
as it is, by design: a password must not be trimmed. In a form almost every text field is
trimmed, so every property repeats `c.trim()`, and both switches are needed to get
"trimmed, and absent if blank":

```java
record Contact(String name, @Nullable String company, @Nullable String note) {}

Yavi.schema()
    ._string("name", c -> c.trim().notBlank())
    ._string("company", c -> c.trim()).nullable()
    ._string("note", c -> c.trim()).nullable()
    .fromMap(Contact::new)
    .readingParameters(StringCoercion.strict().trimming());
```

* Without `trimming()`, `company=%20%20` is `""`, not `null`: `c.trim()` runs after the
  reader decided that the value is present.
* Without `c.trim()`, `company=%20Acme%20` is `" Acme "`.

Forgetting either one is silent.

## First step: decide whether to add it

For: one switch for the common case, for example
`StringCoercion.strict().trimming().trimmingText()`, with the property that must keep its
white space opting out.

Against: there is no opt-out today. A property would need a way to say "as it is"
(`._string("password").verbatim()`), which is a second concept for one case; and a
coercion that changes a `String` value is no longer only a reading of text as another
type. `c.trim()` is explicit and local.

If rejected, state the pair (`trimming()` and `c.trim()`) in
`docs/NewFeatures_0.18.0.md` where `trimming()` is described, with the `""` case.
