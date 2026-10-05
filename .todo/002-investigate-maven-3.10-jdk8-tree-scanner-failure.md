# Investigate Maven 3.10.0 JDK 8 surefire failure (NoClassDefFound: TreeScanner)

Difficulty: Medium

## Context

The Maven wrapper bump 3.9.16 -> 3.10.0 (#517, commit 301665c) broke the `test (8.x)`
CI job. With Maven 3.10.0, `ConstraintMetaProcessorTest` fails on JDK 8 with:

    java.lang.NoClassDefFoundError: com/sun/source/util/TreeScanner
        at com.google.testing.compile.JavaSourcesSubject$CompilationClause.parsesAs(JavaSourcesSubject.java:160)
    Caused by: java.lang.ClassNotFoundException: com.sun.source.util.TreeScanner

On JDK 8, `com.sun.source` lives in `$JAVA_HOME/lib/tools.jar`, and nothing in the
build puts tools.jar on the surefire classpath -- yet it worked with 3.9.16, so
Maven 3.10.0 changes how the forked surefire booter classpath is assembled
(candidate: a change in how the booter manifest / plexus class realm is built, or a
regression in tools.jar detection).

The wrapper was reverted to 3.9.16 in a3bce84 as a workaround; the CI `paths`
filter now covers `.mvn/**` (cec4a82) so the next bump attempt gets CI coverage.

## Task

1. Reproduce locally or on CI: Maven 3.10.0 wrapper + JDK 8 (Liberica, as in CI)
   running `./mvnw test -Dtest=ConstraintMetaProcessorTest`.
2. Identify why `com.sun.source.util.TreeScanner` is loadable under 3.9.16 but not
   3.10.0 (compare the surefire booter classpath / dump the system classloader
   URLs in each case).
3. Fix properly (e.g. add the missing classpath entry for JDK 8, or pin/patch
   whatever regressed) and re-apply the 3.10.0 wrapper bump.
4. Confirm the full matrix (8/11/17/21) is green on GitHub Actions.

## References

- Failed run: https://github.com/making/yavi/actions/runs/37256686811 (8.x, Maven 3.10.0)
- Passing run: https://github.com/making/yavi/actions/runs/37256470699 (8.x, Maven 3.9.16, same day)
- compile-testings 0.19 is the only direct consumer of `com.sun.source` (test scope)
