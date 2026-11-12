# Concise validators of a single value and messages for a value without a name

Difficulty: Medium

## Problem

A validator of a value that is not a property is the building block of Raoh
(`string().email()`, `int_().range(0, 150)`) and of Zod (`z.string().email()`). In YAVI:

```java
StringValidatorBuilder.of("", c -> c.notBlank().email()).build()
```

* One builder class per type has to be known and imported, and the empty name and
  `build()` are noise. `docs/ComparisonWithRaoh.md` repeats this form in most sections
  (elements of `_list`, values of `_map`, `untyped()` roots, `anyOf` alternatives).
* The message of a violation with an empty name is `"" must not be blank`. It reads well
  only after a prefix is applied (`"tags[0]" must not be blank`); at the root of
  `untyped()`, `liftList()` or `traverseIndexed` it does not.

## Goal

* An entry point next to `Yavi.arguments()`:

  ```java
  Yavi.string(c -> c.notBlank().email())            // StringValidator<String>
  Yavi.integer(c -> c.notNull().positive())
  Yavi.enumOf(Role.class), Yavi.localDate(...), ...
  ```

  Decide the names (`Yavi.string` vs `Yavi.value().string(...)`) and whether a name is an
  optional first parameter.
* A readable message for an empty name. Options: a placeholder name (`value`), or formats
  that drop the quoted name when it is empty. The message key and `args()` must not
  change.

## Existing shorter form

The arguments builder already builds a validator of one value, without a builder class
per type, and it knows its input type and description (verified with `apply`):

```java
Yavi.arguments()._string("", c -> c.notBlank().email()).get()            // Arguments1Validator<String, String>
Yavi.arguments()._string("", c -> c.notBlank().email()).apply(Email::new)
```

So `Yavi.string(c)` can be defined as `arguments()._string("", c).get()`, and
`Yavi.string(name, c)` likewise; no new validator type is needed. The return type is then
`Arguments1Validator<String, String>` rather than `StringValidator<String>`; check that
nothing documented depends on the latter.

## Notes

* `ValueValidatorBuilder` implementations are listed in `am.ik.yavi.builder`.

## Done when

Update the comparison documents in the same task:

* `docs/ComparisonWithRaoh.md`
  * Replace every `XxxValidatorBuilder.of("", ...).build()` / `.build(f)` in the examples
    with the new entry point.
  * "Primitive values": rewrite the introduction and the table of Raoh decoders
    (`string()`, `int_()`, ...) to map to the new entry point instead of the builder
    classes.
  * Update the messages shown for a value without a name (`"" must be of type integer`)
    to the new format.
  * Remove the `import am.ik.yavi.builder.*;` line if no example needs it any more.
* `docs/ComparisonWithZod.md`: the `z.string()`, `z.number()` ... row of "Schemas" and the
  `stringValidator` / `integerValidator` example of "A map or a value of unknown type".
* `README.md`: the element validators of `_list` in the map and request parameter
  examples (`StringValidatorBuilder.of("", c -> c.notBlank()).build()`).
* Compile and run the rewritten examples before committing; the current ones were
  verified that way.
