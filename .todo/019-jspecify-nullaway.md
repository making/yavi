# Replace JSR 305 with JSpecify and NullAway

Difficulty: High

## Goal

Remove the vendored `am.ik.yavi.jsr305` annotations and the
`com.google.code.findbugs:jsr305` dependency, annotate the packages with JSpecify
`@NullMarked`, and enforce nullness with NullAway so that a nullness mistake fails the
build instead of relying on documentation.

## Scope

- Delete `src/main/java/am/ik/yavi/jsr305` and replace the 144 `am.ik.yavi.jsr305`
  imports with `org.jspecify.annotations.Nullable` and `@NullMarked` package
  annotations.
- Add JSpecify as a compile-only dependency and remove the JSR 305 dependency and the
  license-plugin exclusion for the deleted package.
- Add NullAway (as an Error Prone plugin) to the Java compilation and fail the build on
  findings. Exclude generated sources and tests where appropriate.
- Enable JSpecify-aware nullness for Kotlin (`-Xjspecify-annotations` strict mode) and
  check the Kotlin extensions still compile.
- Update the Javadoc that refers to the removed annotations.

## Notes

- Depends on 018: NullAway and Error Prone require a modern JDK.
- Keep the runtime dependency set unchanged; both JSpecify and NullAway are build-time
  only.
- The annotations are part of the public API surface, so this is a breaking change for
  code that refers to the old annotation types.
