# Provide an InputReader for the JDK JSON API (JEP 540)

Difficulty: Low

## Background

026 added `am.ik.yavi.core.InputReader` and `ValueValidator#readingWith(InputReader)`, which
read the tree model of a JSON library without copying it to a map. YAVI ships only the
reader of JDK types; the README shows the one for Jackson 3 and
`src/test/java/am/ik/yavi/input/JsonNodeReader.java` tests it.

[JEP 540](https://openjdk.org/jeps/540) (Simple JSON API) is integrated in JDK 28 as an
incubator module, `jdk.incubator.json`. It is the first tree model YAVI could support
without a third-party dependency.

## Goal

A reader of `jdk.incubator.json.JsonValue` (later `java.util.json` or whatever the final
package is), usable as `userValidator.readingWith(<reader>)`.

Expected mapping, to be verified against the JDK in use:

| `InputReader` | JEP 540 |
| --- | --- |
| `isNull` | `node instanceof JsonNull` |
| `isObject` / `property` / `properties` | `JsonObject`, `asMap().get(name)`, `asMap().entrySet()` |
| `isArray` / `elements` | `JsonArray`, `asList()` |
| `value` | `JsonString#asString()`, `JsonBoolean#asBoolean()`, a `JsonNumber` as a `BigDecimal` of its text so that the value is kept |

## Design questions to settle

- Where it lives. The baseline is Java 21 and the API is incubating (its package will
  change when it is finalized), so the main jar cannot refer to it today. Options:
  documentation and a test only (as for Jackson), a multi-release jar entry, or a
  separate artifact. Shipping it in the main jar is reasonable only once the API is final.
- How a `JsonNumber` is read. The JEP offers `asInt`, `asLong` and `asDouble`; check
  whether the exact text is available so that `1e400` and a 30-digit integer are not
  lost, and what `toString()` returns.
- Whether the build can test it: it needs a JDK 28 toolchain and
  `--add-modules jdk.incubator.json`; the test must be skipped on older JDKs.

## Notes

- No change to `InputReader` is expected. If the mapping does not fit, fix the interface
  rather than work around it in the reader.
