# Ready-made forms of the violations for forms and API responses

Difficulty: Low

## Problem

Raoh's `Issues` has `flatten()` (path to messages, for a form) and `toJsonList()` (a list
of `path`, `code`, `message`, `meta`, for an API response). Zod has `z.flattenError`,
`z.treeifyError`. In YAVI both are stream pipelines written by the caller
(`docs/ComparisonWithRaoh.md`, "Errors"), and `ConstraintViolations#details()` returns
`ViolationDetail` (key, args, default message) without the name or the path, so it cannot
tell which property failed.

## Goal

On `ConstraintViolations`:

* a map of name to messages in the order of the violations (`Map<String, List<String>>`),
  the root being the empty name,
* a list of serializable objects with `name` (or a JSON Pointer of `path()`),
  `messageKey`, `message`, `args`,

and decide what `details()` becomes: add the name to `ViolationDetail`, or deprecate it in
favor of the new form.

## Notes

* A JSON Pointer rendering of `List<PathSegment>` (`/items/1/name`) is cheap once this
  exists and is what RFC 9457 problem details with `errors[].pointer` use.
