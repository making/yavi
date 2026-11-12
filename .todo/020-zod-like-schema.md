# Add sealed parse results and structured issues

Difficulty: Medium

## Background

The overall goal is to make YAVI usable in the way Zod is used. Zod's `safeParse`
returns a discriminated union (`{ success: true, data } | { success: false, error }`)
and its `error.issues` entries carry a machine-readable `code` and a `path`. YAVI
already has a richer constraint library than Zod, and `ValueValidator<T, X>` already
separates the input type from the validated output type, which is a good foundation.

What is missing is the result and error shape that makes the Zod workflow ergonomic:

- `am.ik.yavi.core.Validated` is a class extending `am.ik.yavi.fn.Validation`; callers
  use `isValid()` / `value()` / `errors()` and there is no compile-time distinction
  between the success and failure cases.
- `am.ik.yavi.core.ConstraintViolation.name()` is a flat string. Nested validators
  produce names such as `address.street` (the separator comes from
  `ValidatorBuilder(String messageKeySeparator)`), and `ConstraintViolation.indexed(int)`
  appends `[0]`. The path cannot be recovered reliably by splitting the string.
- Constraint message keys such as `charSequence.notBlank` and `numeric.greaterThan` are
  already suitable issue codes.

This todo only introduces the result/issue model. Schema composition is 021 and
loose-input parsing is 022.

## Proposed API

```java
sealed interface ParseResult<T> permits ParseResult.Success, ParseResult.Failure {
    record Success<T>(T value) implements ParseResult<T> {}
    record Failure<T>(List<Issue> issues) implements ParseResult<T> {}
}

sealed interface Issue permits Issue.Missing, Issue.TypeMismatch, Issue.Constraint {
    List<PathSegment> path();
    String code();
    String message();

    record Missing(List<PathSegment> path) implements Issue {}
    record TypeMismatch(List<PathSegment> path, String expected, Object actual) implements Issue {}
    record Constraint(List<PathSegment> path, String code, String message, Object[] args) implements Issue {}
}

sealed interface PathSegment permits PathSegment.Field, PathSegment.Index {
    record Field(String name) implements PathSegment {}
    record Index(int index) implements PathSegment {}
}

// Client code
switch (result) {
case ParseResult.Success<User>(var user) -> save(user);
case ParseResult.Failure<User>(var issues) -> issues.forEach(i -> problem(i.path(), i.code()));
}
```

## Design questions to settle

- Whether to make `Validated` itself the sealed type (nested `Success`/`Failure`
  records) or add a separate `ParseResult` with a conversion from `Validated`.
  - `Validated` extends `Validation`, which is an abstract class; a sealed class can
    extend a non-sealed class, so either shape is possible. Keeping the existing
    `isValid()` / `value()` / `errors()` / `validated()` methods working is required for
    source compatibility either way. A separate type is less invasive but duplicates the
    algebra; the existing `Validation` methods should not be broken.
- How to carry the structured path. Do not parse the flat `name`; the separator is
  configurable. Add path metadata where the violation is created and propagate it
  through `NestedValidator` / `ConstraintViolation.indexed`, or derive it in
  `ViolationDetail`.
- How much of the existing `ConstraintViolations` API to keep. `details()` already
  exposes key/args/message and can be extended rather than replaced.
- Whether `Issue.Constraint` should expose `messageKey()` as `code()` directly. The
  message key is already the stable identifier used for i18n.

## Scope

- Add the sealed result and issue types, path segments, and pattern-matching tests.
- Make `ConstraintViolation` expose its path segments in addition to the flat name.
- Keep the old convenience methods working.
- Add tests that deconstruct both success and failure with `switch` patterns,
  including a nested object and an indexed collection element.

## Non-goals

- No changes to `Yavi.arguments()` or new schema combinators (021).
- No `Map`/JSON-tree input parsing (022).

## Notes

- Depends on 018 (record patterns / switch pattern matching) and 019 (JSpecify
  nullness).
- This is a breaking change for code that subclasses `Validated`; keep the old methods
  available so call sites still compile.
