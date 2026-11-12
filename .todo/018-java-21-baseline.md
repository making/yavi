# Raise the Java baseline to 21

Difficulty: Medium

## Goal

Change the supported runtime and build baseline from Java 8 to Java 21 so that sealed
types, records, record patterns and pattern matching for `switch` can be used by the
Zod-like schema work in 020.

## Scope

- `pom.xml`: set `java.version` to 21 and compile with `--release` instead of
  `source`/`target`; set the Kotlin `jvmTarget` to 21.
- `.github/workflows/ci.yaml`: replace the 8/11/17/21 test matrix with 21 (add newer
  LTS only if it is worth the run time), and move the coverage and Javadoc jobs off
  JDK 8.
- Update the "Java 8+" prerequisite in `README.md` and any docs that state a version.
- Verify that the annotation processor, `compile-testing` tests and the Kotlin
  extensions build and run on 21.
- Keep the JSR 305 annotations and dependency in place; removing them is 019.

## Notes

- Java 21 is the first release where pattern matching for `switch` and record patterns
  are final. Java 17 is not enough for the ergonomics 020 depends on.
- This is a breaking change for consumers; it should land in a major/minor release with
  the version documented.
