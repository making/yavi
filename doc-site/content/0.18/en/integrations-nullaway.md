# NullAway

Since 0.18.0 the type of a validated value tells whether it can be `null`: `ValueValidator<String, String>` never succeeds with `null`, and `ValueValidator<String, @Nullable String>` may. NullAway in JSpecify mode checks this, so the compiler reports a forgotten `nullable()` before a `NullPointerException` can happen.

## JSpecify annotations

YAVI is annotated with [JSpecify](https://jspecify.dev) (`@NullMarked`, `@Nullable`). The package `am.ik.yavi.jsr305` is removed. YAVI does not bring `org.jspecify:jspecify`, so add it to your dependencies:

```xml
<dependency>
    <groupId>org.jspecify</groupId>
    <artifactId>jspecify</artifactId>
    <version>1.0.1</version>
</dependency>
```

A value that passed validation is handed over with its declared type, so the function given to `apply` needs no `@Nullable` parameter and no `Objects.requireNonNull`.

## Configuring NullAway

[`nullability-maven-plugin`](https://github.com/making/nullability-maven-plugin) configures Error Prone and NullAway, including `NullAway:JSpecifyMode` and `OnlyNullMarked`, by extending the compiler arguments of `maven-compiler-plugin`:

```xml
<plugin>
    <groupId>am.ik.maven</groupId>
    <artifactId>nullability-maven-plugin</artifactId>
    <version>0.5.2</version>
    <extensions>true</extensions>
    <executions>
        <execution>
            <goals>
                <goal>configure</goal>
                <goal>generate-package-info</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <!-- main or tests; tests checks the test sources too -->
        <checking>tests</checking>
    </configuration>
</plugin>
```

The plugin is the source of truth for the compiler flags; do not repeat them in `maven-compiler-plugin`. `generate-package-info` writes a `package-info.java` with `@NullMarked` for each package, so a class is checked without annotating it.

For Kotlin, the compiler option `-Xjspecify-annotations=strict` reads the same annotations; the Kotlin compiler has none of the limitations below.

## Common reports and fixes

A parameter that receives a `nullable()` property must be declared `@Nullable`:

```java
record Member(String name, @Nullable String nickname) {}

Arguments2Validator<String, @Nullable String, Member> validator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable()
        .apply(Member::new);
```

```
[NullAway] parameter nickname of referenced method is @NonNull, but parameter in functional interface method am.ik.yavi.fn.Function2.apply(String,@Nullable String) is @Nullable
```

A declared validator uses the nullable type argument:

```java
ValueValidator<String, @Nullable String> memo = Yavi._string("memo", c -> c.lessThanOrEqual(100)).nullable();
```

The other parameters of the function need no `@Nullable`; only a `nullable()` property produces a nullable value. `isNull()` needs `nullable()`, because `null` is a missing value otherwise.

## Limitations

All of the following are false positives: NullAway reports correct code. None of them lets a `null` pass unnoticed, and each has a way to write the code without `@SuppressWarnings`. The messages were produced by NullAway 0.14.2 and 0.14.3 with JSpecify mode and `OnlyNullMarked`. The Kotlin compiler with `-Xjspecify-annotations=strict` has none of these limitations.

* **A lambda that implements a validator of a nullable value.** NullAway compares `Validated<@Nullable String>` with `Validated<X>` without replacing `X`. Write the validator as an anonymous class, or build it from an existing one: `Yavi._string(c -> c.trim()).nullable()`.
* **`Objects.requireNonNull` as the argument of a generic method.** NullAway infers the type argument from the nullable parameter. Assign the checked value to a variable first, or handle `null` in the expression. A lambda that validates a value that is not `null` is not affected.
* **`map` with a function that returns `null`, without a target type.** In the middle of a chain the mapped type is taken as not nullable. Give the type argument (`.<@Nullable String>map(...)`) or assign the validator to a declared type.
* **The parameter of a lambda that is a validator of a nullable value, inferred from a collection.** NullAway drops the nullable type argument. Use `var` for the result, declare the parameter type, or use a loop.

If NullAway reports correct code outside these cases, the validator type is the first thing to check: `nullable()` changes the type argument, and it is the only modifier that produces `null`.
