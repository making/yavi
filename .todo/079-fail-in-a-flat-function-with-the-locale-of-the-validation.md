# Fail in the function of flatFromMap with the locale of the validation

Difficulty: Medium

## Problem

Found by writing the Raoh tutorial with YAVI (sections 12 and 25 of
`docs/RaohTutorialInYavi.md`). The function given to `flatFromMap` / `flatApply`, and a
factory method that it refers to, receive the validated properties only. To fail they
call

```java
Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"), start, Locale.getDefault());
```

* `Locale.getDefault()` is not the locale of `validate(input, locale)`, so the message of
  this violation is formatted for another locale than the others.
* The violated value and the locale are noise next to `Result.fail(code, message)` of
  Raoh.

## Proposal

A failure that does not name a locale: `Validated.failureWith(message, args...)` (or
`ViolationMessage#fail(args...)`) creates a violation whose message is formatted by the
validator that receives it, with its locale and its `MessageFormatter`
(`formattingWith`). `flatFromMap`, `flatApply` and `andThen` do that for the violations
of the function.

## To decide

* How a violation without a locale is represented (`ConstraintViolation` holds a locale
  and a formatter today) and what `message()` returns before a validator has seen it.
* Whether the violated value stays a parameter; `violatedValue()` of such a violation
  could be the created arguments.
* Update sections 12 and 25 of the tutorial and `docs/ComparisonWithRaoh.md`.
