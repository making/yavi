# Print a failed result as its violations read

Difficulty: Low

## Problem

Found by writing the Raoh tutorial with YAVI (`docs/RaohTutorialInYavi.md`). A failed
result printed in jshell, in a log or in the message of a failed assertion is

```
Invalid[errors=[ConstraintViolation{name='', messageKey='object.type', defaultMessageFormat='"{0}" must be of type {1}', args=[, integer, not a number]}]]
```

where Raoh prints `Err[/: expected integer]`. The message is not formatted and the path
is not shown, so every failing example of the tutorial ends with
`.errors().messagesByPointer()`.

## Proposal

`ConstraintViolation#toString()` shows the pointer and the formatted message, and the
message key (`/age: "age" must be less than or equal to 150 (numeric.lessThanOrEqual)`),
and `ConstraintViolations#toString()` the list of them. A `NoAlternativeMatched` adds
its alternatives.

## To decide

* Whether the violated value is printed. `ViolationDetail` leaves it out because it may
  be a secret; `toString()` reaches logs, so leave it out there too (today `args` prints
  it).
* List the change in the breaking changes: a test may compare the text.
* Drop `.errors().messagesByPointer()` from the tutorial where the result itself reads
  well, and run `docs/verify-examples.py`.
