# Opt-in coercion of strings for map input (request parameters)

Difficulty: High

## Background

022 added `fromMap` (`am.ik.yavi.input.MapInputValidator`) and
`ValueValidator.untyped()`. The type check is exact: a value must be an instance of the
type of its argument or its JSON representation (a number for a numeric type, the name of
a constant for an enum, an ISO-8601 string for a temporal type). `"30"` is not a number
and `"true"` is not a boolean; both are reported with `object.type`. The rules are in
`am.ik.yavi.core.UntypedInput` and stated in the Javadoc of
`ValueValidator#untyped(Class)`.

A map does not always come from a JSON parser. Spring MVC binds request parameters to
`@RequestParam Map<String, String>` (or `MultiValueMap<String, String>`), and form posts,
query strings, CSV rows, environment variables and `.properties` files are maps of
strings too. With such a map, every `_integer`, `_long`, `_double`, `_bigDecimal` and
`_boolean` argument fails today, so `fromMap` cannot be used without converting the map
by hand. String, enum and temporal arguments already work.

Today the only way is a hand-written validator per argument:

```java
ValueValidator<Object, Integer> coerced = (value, locale, context) -> ValueValidator.<Integer>passThrough()
    .untyped(Integer.class)
    .validate(value instanceof String s ? Integer.valueOf(s) : value, locale, context);
```

It does not report an unparseable string as a violation (`Integer.valueOf` throws) and has
to be repeated for every argument.

## Goal

An explicit, opt-in mode in which a string is converted to the type of the argument, so a
map of strings is validated with the same arguments as a JSON map:

```java
MapInputValidator<SearchForm> validator = Yavi.arguments()
    ._string("q", c -> c.notBlank())
    ._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
    ._boolean("includeArchived").defaultValue(false)
    .fromMap(SearchForm::new)
    .coercingStrings(); // placeholder name

@GetMapping("/search")
Object search(@RequestParam Map<String, Object> params) {
    return validator.validate(params);
}
```

The default stays exact. Names are placeholders; use YAVI's vocabulary.

## Scope

- Convert a string to `Integer`, `Long`, `Short`, `Byte`, `BigInteger`, `BigDecimal`,
  `Double`, `Float` and `Boolean` when the mode is on. A string that does not represent
  the type is reported as a violation, never as an exception.
- Apply the mode to everything under the validator: nested `_object`, the elements of
  `_list` and the values of `_map`.
- Keep the absent-key, unknown-key and `null` rules of `MapInputValidator` unchanged.
- Document the mode in the README section "Validating a map such as a parsed JSON
  object" with a request-parameter example, and in `docs/ComparisonWithZod.md` next to
  `z.coerce`.
- Extend `MapInputValidatorTest` (the parameterized `untypedInputMustRepresentTheType` and
  one of the map tests) instead of adding a parallel test class.

## Design questions to settle

- How the mode reaches the type checks. A validator is bound to an
  `am.ik.yavi.core.InputReader` with `untyped(reader)`, and the reader reaches every
  nested validator, including one that was built before (026). The type check reads a
  node with `InputReader#value(node)` and does not tell the reader the type it checks, so
  a reader alone cannot convert `"30"` for an `Integer` argument and leave it for a
  `String` argument. Options: a type-directed method on the reader (a default method that
  returns the value as is), or a mode of `UntypedInput` selected by the reader.
- Whether the mode is all-or-nothing or a set of conversions, and whether it also covers
  the opposite direction (`30` for a `_string` argument). Zod's `z.coerce` is per schema.
- Which strings are a boolean (`true` / `false` only, or also `on`, `1`, `yes` as HTML
  checkboxes and Spring's conversion accept) and how numbers are parsed (surrounding
  whitespace, a leading `+`, locale-specific separators). Prefer the strict JDK parsers and
  state the rule.
- An empty string. An HTML form submits `""` for an empty field; decide whether the mode
  treats it as absent for a non-string argument so that `optional()` and `defaultValue`
  apply.
- The message key of a string that cannot be converted: `object.type` as today, or the
  existing `charSequence.integer` / `charSequence.boolean` family.
- Request parameters are flat and may be multi-valued. Decide whether this item also
  covers `MultiValueMap` (a list for `_list`, the first value otherwise) and nested keys
  (`address.street`, `tags[0]`), or leaves them to a follow-up; Spring's `DataBinder`
  already solves them for typed forms.
- New classes must not introduce a dependency cycle between packages: `core` depends only
  on `fn` and `message`, and `input` only on `core`. Do not add a dependency on Spring.

## Notes

- Depends on 022 and 026 (done). `MapInputValidator` and `DiscriminatedUnionValidator`
  take the fast path of the JDK reader by identity (`reader == InputReader.ofJdkTypes()`);
  a coercing reader takes the general path, in which every property and alternative must
  know its input type.
