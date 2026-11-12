# Arguments that read the enclosing object: embedded validators and the remaining keys

Difficulty: High

## Problem

Found while writing the Raoh tutorial with YAVI (`docs/ComparisonWithRaoh.md`, "Flat data"
and "Known fields and the remaining fields").

1. Raoh's `combine(flat(a), flat(b))` builds one object from two decoders that read the
   same flat input (a JOIN row, a CSV row). In YAVI this is
   `ArgumentsValidators.combine(a, b).apply(f)`, whose result has no description and no
   input type:
   * `_object(name, combined)` and `_list(name, combined)` make `fromMap` throw
     `IllegalStateException` (input type unknown),
   * `combined.readingWith(reader)` / `untyped()` throw `UnsupportedOperationException`,
   * `readingParameters()` and `rejectingUnknownProperties()` do not exist on it,
   * `JsonSchema` describes it as unknown.
2. Collecting the keys that no argument defines (Zod `catchall` / `looseObject`, Jackson
   `@JsonAnySetter`) needs a lambda over the raw map, with the same losses. The Zod
   comparison lists it as having no counterpart.

## Goal

Both are arguments of the builder that are not bound to one key:

```java
Yavi.arguments()
    ._embedded(userNameValidator)        // reads first_name, last_name of the same object
    ._embedded(departmentValidator)
    .fromMap(Employee::new);

Yavi.arguments()
    ._string("name", c -> c.notBlank())
    ._rest(StringValidatorBuilder.of("", c -> c.notNull()).build())   // Map<String, String> of the other keys
    .fromMap(Item::new);
```

Names are placeholders. The result is a `MapInputValidator`, so every reader, request
parameters, the description and JSON Schema (`allOf` / merged properties,
`additionalProperties`) work.

## Design questions

* Unknown properties: with an embedded validator the known names are the union; an
  embedded validator that is not a `MapInputValidator` (a discriminated union, a lambda)
  makes the set unknowable, which is why Raoh refuses `strict` with `flat`. `_rest` and
  `rejectingUnknownProperties()` exclude each other.
* Name clashes between embedded validators and the own arguments: reject at build time.
* A nullable embedded group (LEFT JOIN: all columns `null` gives `null`) - in scope or a
  follow-up?
* Whether `ArgumentsValidators.combine(...)` should also carry the description and the
  input type when all its parts do.
* Generated builders (`Arguments1..16ValidatorBuilder`) are produced by a script under
  `scripts/`; change the template, not the files.
