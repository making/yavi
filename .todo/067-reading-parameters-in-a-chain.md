# `readingParameters()` followed by another call needs a type witness

Difficulty: Low

## Problem

`readingParameters()` takes the type of the map from the type it is assigned to
(`<M extends Map<String, ?>>`). When another call follows it, there is no target type
any more, `M` is inferred as `Map<String, ?>` and the assignment does not compile:

```java
record Signup(String email) {}

ValueValidator<Map<String, List<String>>, Signup> signupValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        .fromMap(Signup::new)
        .readingParameters()
        .formattingWith(formatter);
// incompatible types: ValueValidator<Map<String,?>,Signup> cannot be converted to
// ValueValidator<Map<String,List<String>>,Signup>

        .<Map<String, List<String>>>readingParameters() // compiles
        .formattingWith(formatter);
```

`formattingWith` is the call that follows most often, because it is the documented way
to give a schema its messages, and it cannot be written before `readingParameters()`: on
an `ObjectInputValidator` it returns a `ValueValidator`, which has no
`readingParameters()`. `constraintOnTarget`, `map` and `named` after
`readingParameters()` fail the same way.

Found while giving every form of a Spring MVC application one `MessageFormatter`.

## First step: decide the shape

1. `ObjectInputValidator#formattingWith` returns an `ObjectInputValidator`, so the
   formatter is given before the input is chosen:
   `.fromMap(Signup::new).formattingWith(formatter).readingParameters()`. Check the other
   combinators of `ValueValidator` that `ObjectInputValidator` could keep its type for.
2. The validator that `readingParameters()` returns accepts every map of parameters, for
   example a type with `validate(Map<String, ?>)`, so nothing is inferred. Check what
   that does to a `Map<String, Object>` that is not parameters.
3. Named variants without inference (`readingMultiValueParameters()`). More methods for
   the same thing.

## If accepted

* Extend the tests of `readingParameters()`; no new test class.
* Show the chain with `formattingWith` in `docs/NewFeatures_0.18.0.md`.
