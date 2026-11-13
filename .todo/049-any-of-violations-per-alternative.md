# Keep the violations of `anyOf` grouped by alternative

Difficulty: Medium

## Problem

When no alternative of `ValueValidator.anyOf` matches, the result is one flat list:
`object.anyOf` followed by the violations of every alternative
(`ValueValidator#anyOf`, `violations.addAll(validated.errors())`). Nothing says which
alternative a violation belongs to:

```java
anyOf(emailContact, phoneContact).validate(Map.of("kind", "email", "value", "x"));
// "" object.anyOf
// value: must be a valid email address      (alternative 0)
// kind:  must be equal to phone             (alternative 1)
// value: must match ^\d{10,15}$             (alternative 1)
```

`messagesByName()` then shows a form two contradictory messages under `value`, and a
client cannot pick "the alternative that was closest". Raoh reports one issue
(`one_of_failed`) and keeps the issues of each candidate in `meta.candidates`, which
`resolve()` and `rebase()` reach; Zod keeps `errors: ZodIssue[][]` on the
`invalid_union` issue.

## First step: decide the shape

* The `object.anyOf` violation carries the violations of each alternative
  (`List<ConstraintViolations>`), exposed by `ConstraintViolation` and `ViolationDetail`,
  and renamed with the violation by `prefixed` / `rename`. Decide whether they stay in
  the flat list as well (compatible with 0.18.0) or only under the leading violation
  (what Raoh and Zod do; `messagesByName()` gets one entry).
* A cheaper heuristic to consider with it: when exactly one alternative gets past a
  literal property (`equalTo`), report only that alternative. `DiscriminatedUnionValidator`
  already covers this case explicitly, so this may not be worth it.

0.18.0 is not released yet; a change of the flat list is not a breaking change if it
lands first.

## Scope

- `ValueValidator#anyOf`, `ConstraintViolation`, `ViolationDetail`, `details()`.
- "Unions" of `docs/ComparisonWithRaoh.md` and `docs/ComparisonWithZod.md`, the
  combinator table of `docs/NewFeatures_0.18.0.md` and its Japanese version.
