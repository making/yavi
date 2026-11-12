# Small parity items: lenient enum and boolean readings, trimmed text, BigDecimal scale

Difficulty: Low

Found while writing the Raoh tutorial with YAVI (`docs/ComparisonWithRaoh.md`, tables in
"Constraints", "String-to-type conversions" and "Enums"). Each item is independent; drop
the ones judged out of scope and say so in the comparison.

1. **Enum constants without regard to case.** `_enum("role", Role.class)` takes `"ADMIN"`
   only; Raoh's `enumOf` takes `"admin"` too. Wanted as an option of the argument
   (`_enum(name, type).ignoringCase()`?) that works for every reader, not only for
   coerced strings. The description / JSON Schema `enum` keeps the constant names.
2. **Boolean words.** With `coercingStrings()` only `"true"` / `"false"` are booleans;
   Raoh's `toBool()` and Zod's `z.stringbool()` take `1/0`, `yes/no`, `on/off`
   (an unchecked HTML checkbox sends nothing and a checked one sends `on`). Today this
   needs a reader that overrides `InputReader#value(node, type)`. Provide it as a
   ready-made decorator (`coercingStrings(options)`?) rather than changing the default.
3. **Surrounding whitespace of coerced text.** `" 42 "` is `object.type`; Raoh writes
   `string().trim().toInt()`. Same decorator option as 2 (CSV cells, form fields).
4. **`BigDecimal` scale / precision.** No constraint for the number of fraction digits
   (Raoh `decimal().scale(4)`, Bean Validation `@Digits`). Add to `BigDecimalConstraint`
   with message keys and JSON Schema `multipleOf` where it applies.
