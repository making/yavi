# Read the numbers of a JSON tree as written, and say what the parser must be told

Difficulty: Low

## Problem

Raoh 0.9 added `JsonDecoders.readTree`, because a tree of Jackson's `ObjectMapper` holds
a number with a fraction as a `double`: `0.10` loses its scale, more than 17 digits are
lost and `1e400` is refused. It also refuses a member name written twice.

YAVI leaves parsing to the caller, which is right, but the `JsonNodeReader` of
`README.md` and `docs/NewFeatures_0.18.0.md` returns `node.numberValue()`, and
`UntypedInput#decimal` converts a `Double` with `new BigDecimal(number.toString())`. So
with a default mapper a `_bigDecimal` property has the same loss, and neither document
says so. `fractionDigits(2)` then accepts `1.005000000000000000001` that was rounded by
the parser.

## Scope

- Verify with a test in the existing `JsonNode` reader test: a `_bigDecimal` property
  read from `readTree("{\"price\": 0.10}")` with and without
  `DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS`, and a long fraction against
  `fractionDigits`.
- In `JsonNodeReader#value`, return `node.decimalValue()` for a `DecimalNode` if
  `numberValue()` does not already (it should; confirm).
- Document next to the reader in `README.md`, `docs/NewFeatures_0.18.0.md` (and `.ja`):
  enable `USE_BIG_DECIMAL_FOR_FLOATS` for exact decimals, and
  `StreamReadFeature.STRICT_DUPLICATE_DETECTION` to refuse a duplicated key. Add the
  row to "Differences" of `docs/ComparisonWithRaoh.md`.
- Check the same for `JdkJsonReader` (`src/test/java28`): which Java type a JEP 540
  number gives.
