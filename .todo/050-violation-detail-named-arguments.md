# Decide whether a violation exposes named arguments

Difficulty: Medium

## Problem

`violations.details()` is the form for an API response, and its `args` are the
positional arguments of the message format:

```json
{"name": "age", "pointer": "/age", "messageKey": "numeric.lessThanOrEqual",
 "message": "\"age\" must be less than or equal to 150", "args": ["age", 150, 300]}
```

* A client that renders its own message must know, per message key, that index 0 is the
  name, the last index is the violated value and what is in between. That table is not
  part of the API.
* The violated value is always sent back (a password, a token).
  `docs/NewFeatures_0.18.0.md` tells the user to map the details to a type of their own.

Raoh's `Issue.meta` is a map with names (`{"min": 0, "max": 150}`) that its message
templates refer to (`must be between {min} and {max}`), and Zod issues have named
fields (`minimum`, `inclusive`, `format`).

## First step: decide

* Whether `ViolationDetail` gets named arguments (e.g. `Map<String, Object> params`
  such as `{"max": 150}`) beside or instead of `args`. The names can come from
  `ConstraintDescription`, which already knows them for `JsonSchema`
  (check how much of the mapping exists there before designing a second one).
* Whether the violated value leaves the default `details()` (a separate accessor or an
  explicit `details(includingValue)`), so that the default is safe to return.
* Message formats stay positional (`{0}`): `MessageFormat` and the existing bundles of
  users depend on it. Named arguments are for the consumer of the detail only.

0.18.0 is not released yet, and `ViolationDetail` became a record with `pointer` in it,
so its shape can still change without a second break.

## Scope if accepted

- `ViolationDetail`, `ConstraintViolation#detail`, custom violations
  (`ViolationMessage.of`, where the names are unknown: decide the fallback).
- "Errors" of both comparison documents, "Handling the result" of
  `docs/NewFeatures_0.18.0.md` and its Japanese version.
