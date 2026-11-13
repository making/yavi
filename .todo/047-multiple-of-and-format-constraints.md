# Add `multipleOf` and the string formats Raoh and Zod have

Difficulty: Low

## Problem

Constraints that Raoh 0.9 and Zod 4 have and YAVI lacks (checked against
`constraint/base/NumericConstraintBase` and `CharSequenceConstraint`):

| Raoh | Zod | YAVI |
| --- | --- | --- |
| `int_().multipleOf(n)`, `long_()`, `decimal()` | `z.number().multipleOf(n)` | none; `JsonSchema` writes `multipleOf` only for `fractionDigits` |
| `string().ip()` (IPv4 or IPv6) | `z.union([z.ipv4(), z.ipv6()])` | `ipv4()` and `ipv6()` only |
| `string().ulid()`, `cuid()` | `z.ulid()`, `z.cuid()` | none |
| `string().uri()` (RFC 3986, any scheme) | `z.url()` | `url()` only, which is what `java.net.URL` accepts |

## Scope

- `multipleOf(n)` for the integral types, `BigInteger` and `BigDecimal` (not `Double` and
  `Float`, as in Raoh: the remainder of a binary fraction is not exact). Message key
  `numeric.multipleOf`, all message bundles, `JsonSchema` (`multipleOf`).
- `ip()`, `ulid()` and `uri()` on `CharSequenceConstraint`, with `format` in `JsonSchema`
  where one exists (`uri`). Decide `cuid()` separately: cuid is deprecated by its
  authors in favour of cuid2.
- Rows in the tables of `docs/ComparisonWithRaoh.md` and `docs/ComparisonWithZod.md`.
- Extend the existing parameterized constraint tests instead of adding test classes.
