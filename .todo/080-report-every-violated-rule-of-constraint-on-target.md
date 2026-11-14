# Report every violated rule of constraintOnTarget

Difficulty: Medium

## Problem

Found by writing the Raoh tutorial with YAVI (section 23 of
`docs/RaohTutorialInYavi.md`). `constraintOnTarget` returns a validator that wraps the
one it is called on, so the second rule is checked only if the first holds:

```java
.fromMap(PasswordForm::new)
.constraintOnTarget(form -> form.newPassword().equals(form.confirm()), "confirmPassword", mismatch)
.constraintOnTarget(form -> !form.current().equals(form.newPassword()), "newPassword", same)
```

An input that violates both reports `confirmPassword` only, and the user sees the second
violation after fixing the first. The properties of a schema are accumulated, and so are
the class-level constraints of Bean Validation and the `refine`s of Zod. The Javadoc of
`constraintOnTarget` does not say which it is.

## Proposal

Rules added one after another on the same value are all checked when the properties are
valid, and their violations are accumulated. A rule that needs another to hold is written
in one predicate, or after `andThen`.

## To decide

* Where the accumulation lives: a validator that holds a list of rules and returns
  itself with one more, for `ValueValidator`, `ObjectInputValidator` (its
  `IgnoringUnknown` and `Reversible` forms) and `ArgumentsNValidator`.
* A rule after `map`, `andThen` or `rejectingUnknownProperties()` starts a new group.
* State the behaviour in the Javadoc and in the new features, and update section 23 of
  the tutorial.
