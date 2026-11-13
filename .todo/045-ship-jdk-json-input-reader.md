# Ship the InputReader of the JDK JSON API once it is final

Difficulty: Medium

## Background

028 verified that `InputReader` reads the tree model of [JEP 540](https://openjdk.org/jeps/540)
without a change to the interface. The reader, `JdkJsonReader`, lives in
`src/test/java28` and in the README only: the API is the incubator module
`jdk.incubator.json` of JDK 28, so its package will change and its methods may.

A reader in the main jar that calls the API by reflection was rejected: it could only be
an `InputReader<Object>` (typing it later is a breaking change), a rename of the API
breaks it at run time rather than at compile time, and it is slower on a path called for
every node.

## Trigger

Start when the API is final, that is no longer an incubator or a preview. The
`test-jdk-ea` job of the CI fails when the incubating API changes; keep the test reader
working until then.

## Scope

- Ship the reader in the main jar as a typed `InputReader` of the final `JsonValue`, in
  a multi-release jar entry (`META-INF/versions/<N>`) while the baseline is older than
  the JDK that has the API. No reflection.
- Decide the entry point: a public class, or a factory next to `InputReader.ofJdkTypes()`.
  A factory in `InputReader` itself cannot name the node type in the baseline version of
  the class.
- The release build then needs that JDK; keep `--release` of the baseline for the rest
  and check the javadoc and the source jar of the versioned entry.
- Move the test to the shipped reader: `Model.JDK_JSON` of `MapInputValidatorTest` no
  longer needs reflection for the reader. Remove `src/test/java28` and the reader from
  the README, and document the shipped one.
- Replace the `jdk-json` profile and the `test-jdk-ea` job by the setup of the versioned
  entry.

## Notes

- A number is read as `new BigDecimal(number.toString())`; check that the final API still
  returns the text of the number.
- A property is read with `asMap().get(name)`; `JsonValue#get(name)` throws for an absent
  member.
