# Reject or document the remaining chains in which a part has no effect

Difficulty: Medium

## Background

#563 staged the modifiers of a schema property. A survey of the other fluent APIs
found these chains that compile although a part of them has no effect (all run on the
branch of #563).

## Problem

1. **`optional()`, `nullable()`, `defaultValue(...)` of a `_rest` property with
   `fromMap`.** The rest is an empty map if there is none, never `null`:

   ```java
   Yavi.schema()._string("x")._rest().nullable().fromMap(...)             // {} for no other key, never null
   Yavi.schema()._string("x")._rest().defaultValue(Map.of("d", 1)).fromMap(...) // {} too, the default is dead
   ```

   They have an effect with `apply` (the argument may be `null`), and `recover` has one
   with both. Same shape as `required()` + `apply`, which #563 decided to keep; decide
   here too, and at least document it at `_rest`.

2. **A reader given after a reader.** `readingWith` re-reads the validator from its
   definition, so the last reader wins and the earlier one is dropped without a word:

   ```java
   validator.readingWith(jdk.coercingStrings()).readingWith(jdk).validate(Map.of("n", "1")); // object.type
   validator.readingParameters().readingWith(jdk).validate(Map.of("n", "1"));                // object.type
   jdk.coercingStrings(StringCoercion.strict().trimming()).coercingStrings();                // trimming is dropped
   ```

   `readingWith` is on every `ValueValidator`, so types cannot remove it. Options: the
   validator returned by `readingWith` / `readingParameters` rejects another
   `readingWith` at run time, or the Javadoc says that the last one wins.
   `coercingStrings` on a reader that already coerces could keep the first coercion or
   throw.

3. **`DiscriminatedUnionValidator.of("type")` without `when`.** It is a validator that
   rejects every input with `object.oneOf`. A stage (`of` returns a builder whose
   `when` returns the validator) would reject it by type.

## Not a problem (checked)

- `EnumValidator#ignoringCase()` survives `map`, `andThen`, `constraintOnTarget`,
  `describedAs`, `prefixed`, `optional()`, `preprocess`, `lazy()`, `liftList()`.
- `rejectingUnknownProperties()` survives `readingWith` and `constraintOnTarget`.
- A transform of a `String` (`trim()`) after a constraint does not compile.
- Contradicting constraints (`c.isNull()` without `nullable()`, `c.notNull()` with
  `nullable()`) are values, not types, and are documented in the README.
