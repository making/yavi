# Javadoc: integration, overview page and doclint enforcement

Difficulty: Medium

Depends on: 004-016 (all merged).

## Work

1. Whole-tree doclint is clean (`javadoc ... -Xdoclint:all` per `.todo/javadoc-conventions.md`, without the path filter). Fix leftovers in any file.
2. Consistency pass across packages: terminology (validator, constraint, violation, message id/key, target), summary-sentence style, `@since` correctness, broken or missing cross-links between packages written by different todos.
3. Overview page: add `src/main/javadoc/overview.html` (what YAVI is, entry points `ValidatorBuilder` / `Yavi` / `ArgumentsValidatorBuilder`, package map) and wire it in `maven-javadoc-plugin`.
4. Enforce: configure `maven-javadoc-plugin` in `pluginManagement` with `doclint=all` and `failOnWarnings=true`, so CI (`./mvnw javadoc:jar` in `.github/workflows/ci.yaml`) and the `release` profile fail on regressions. Check the JDK 8 job of CI still works with these options.
5. Delete `.todo/javadoc-conventions.md`.

## Verification

- `./mvnw -q javadoc:jar` succeeds with enforcement on.
- `./mvnw -q test` passes.
- Generator scripts + `./mvnw spring-javaformat:apply` leave `git status` clean.
