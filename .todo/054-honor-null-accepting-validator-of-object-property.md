# Honor a validator that accepts `null` given to `_object(name, validator)`

Difficulty: Low

## Problem

`_object(name, validator)` rejects `null` itself before `validator` is applied, so the
`null` handling of a validator that accepts `null` is silently dead (run on the
branch of #563):

```java
Yavi.schema()._object("address", addressValidator.nullable()).fromMap(x -> x)
    .validate(Collections.singletonMap("address", null)); // "address" must not be null

Yavi.schema()._object("address", addressValidator.defaultValue(HOME)).fromMap(x -> x)
    .validate(Map.of());                                  // "address" is required
```

What works is `_object("address", addressValidator).nullable()`. The other `_xxx`
methods that take a validator only (`_string(validator)`, `_object(validator)`) do
honor it.

## Direction

Types can tell since #563: `nullable()` and so on return a
`NullAcceptingValueValidator`. Add the overload
`_object(String name, NullAcceptingValueValidator<T1, T2> validator)` that returns the
`NullAccepting` stage and gives `null` to the validator, as the single-argument
overloads do. The violations of other values stay located under `name`.

## Scope

- `ArgumentDefinition.object` and `scripts/generate-args.sh` (regenerate,
  `spring-javaformat:apply`).
- `_list(name, elementValidator)` and `_map(name, valueValidator)` are not affected:
  a validator that accepts `null` there is about the elements.
- Extend an existing test; `docs/NewFeatures_0.18.0.md` and its Japanese version.
