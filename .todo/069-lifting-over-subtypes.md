# Lifting a validator over a container of subtypes

Difficulty: Medium

## Problem

`liftList()`, `liftSet()`, `liftCollection(factory)`, `liftMap()`, `liftOptional()` and
`liftPresence()` of `ValueValidator`, and the same methods of `Arguments1Validator` and
`ArgumentsValidators`, take the container of exactly the input type (`Iterable<T>`,
`Map<String, T>`, `Optional<T>`). A validator of `T` can validate every element of a
container of a subtype of `T`, but the compiler rejects it:

```java
ValueValidator<Map<String, ?>, MemberImport> rowValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        .fromMap(MemberImport::new)
        .readingParameters();

List<Map<String, String>> csvRows = ...;
rowValidator.liftList().validate(csvRows);
// Iterable<Map<String,?>> cannot be applied to List<Map<String,String>>

ValueValidator<Object, String> name = Yavi._string("name").untyped();
List<String> names = ...;
name.liftList().validate(names); // same for any untyped validator
```

The workaround is a copy with the wider type: `List<Map<String, ?>> rows = List.copyOf(csvRows)`
(shown in `docs/ComparisonWithRaoh.md`, "CSV import").

Found while making `readingParameters()` return `ValueValidator<Map<String, ?>, X>`.

## First step: decide the shape

1. The input of a lifted validator is `Iterable<? extends T>`, `Map<String, ? extends T>`,
   `Optional<? extends T>`. A variable declared as `ValueValidator<Iterable<String>, ...>`
   no longer compiles, and the methods were released before 0.18.0, so it is a breaking
   change for `docs/BreakingChanges_0.18.0.md`. Check the generated builders
   (`Arguments*ValidatorBuilder`) and `_list`/`_map` of the schema builders.
2. Leave the types and document the copy.

## If accepted

* Extend the existing tests of the lift methods; no new test class.
* Remove the copy from `docs/ComparisonWithRaoh.md`.
