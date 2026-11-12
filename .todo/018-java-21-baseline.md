# Raise the Java baseline to 21

Difficulty: Medium

## Background

The goal of 020-022 is to make YAVI usable in the way Zod is used: define a composable
schema, parse untrusted input, and handle a discriminated result with structured
issues. That design needs sealed types, records, record patterns and pattern matching
for `switch`. Pattern matching for `switch` and record patterns are final in Java 21;
Java 17 is not enough. Raising the baseline has been approved for this work.

Order of work: 018 (this), 019 (JSpecify/NullAway), then 020-022.

## Current state

- `pom.xml` sets `<java.version>1.8</java.version>` and uses `maven.compiler.source` /
  `maven.compiler.target`. The Kotlin plugin uses the same property for `jvmTarget`.
- `.github/workflows/ci.yaml` tests Java 8/11/17/21 and runs coverage and
  `javadoc:jar` only on JDK 8. The deploy job also uses JDK 8.
- `README.md` states "Java Runtime >= 8" and "Required: Java 8+".
- `src/main/java/am/ik/yavi/jsr305` is vendored and remains in place for this todo;
  removing it is 019.

## Scope

- `pom.xml`: set `java.version` to 21, compile with `--release 21` (`maven.compiler.release`)
  instead of source/target, and set the Kotlin `jvmTarget` to 21. Do not enable preview
  features.
- `.github/workflows/ci.yaml`: replace the 8/11/17/21 matrix with 21 (optionally add the
  next LTS), and move the coverage/Javadoc job and the deploy job off JDK 8.
- Update the Java version statements in `README.md` and elsewhere.
- Verify the annotation processor tests (`com.google.testing.compile`), the Kotlin
  extensions and the Spring Java Format check still build and run on 21.
- Confirm `mvnw` and the Maven plugins in use support a 21 build.

## Verification

- `./mvnw clean test`
- `./mvnw javadoc:jar` (doclint is configured to fail on warnings)
- CI green on the new matrix.

## Notes

- Java 21 makes sealed classes (17), records (16), record patterns and `switch` pattern
  matching (21) available. 020 depends on the last two.
- This is a breaking change for consumers and should be called out in the release notes.
