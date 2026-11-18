# Annotation Processor

The annotation processor generates a meta class for the members of a type annotated with `am.ik.yavi.meta.ConstraintTarget` or `am.ik.yavi.meta.ConstraintArguments`. A meta constant names a property and reads it with a `Function`, so a validator does not use a string literal for the name.

## Enabling the annotation processor

The processor is packaged in the `yavi` artifact. With JDK 21 and earlier, `mvn clean compile` discovers it on the compile classpath. JDK 23 and later do not run annotation processors found on the classpath implicitly, so declare the processor explicitly:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>am.ik.yavi</groupId>
                <artifactId>yavi</artifactId>
                <version>0.18.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

`@ConstraintTarget` and `@ConstraintArguments` have source retention, so they are not present at runtime.

## Annotating a target

`@ConstraintTarget` is written on a getter or on a constructor parameter. The processor generates `_UserFormMeta` for the type `UserForm`, with one constant per annotated member.

```java
public class UserForm {

    private String name;

    private Integer age;

    @ConstraintTarget
    public String getName() {
        return name;
    }

    @ConstraintTarget
    public Integer getAge() {
        return age;
    }

}
```

The constants are passed to `ValidatorBuilder#constraint`:

```java
Validator<UserForm> validator = ValidatorBuilder.<UserForm>of()
        .constraint(_UserFormMeta.NAME, c -> c.notBlank())
        .constraint(_UserFormMeta.AGE, c -> c.greaterThan(0))
        .build();
```

On a constructor parameter, `@ConstraintTarget(getter = false)` uses a method with the name of the parameter, which suits a record. `@ConstraintTarget(field = true)` reads the field directly; the field must be accessible from the generated class, which is in the same package.

```java
public record UserForm(@ConstraintTarget(getter = false) String name,
        @ConstraintTarget(getter = false) Integer age) {
}
```

## Annotating arguments

`@ConstraintArguments` is written on a constructor or on a method. A constructor of `User` generates `_UserArgumentsMeta`; a method `createUser` of `UserService` generates `_UserServiceCreateUserArgumentsMeta`, in which the receiver is the first argument.

```java
public class User {

    @ConstraintArguments
    public User(String name, String email, int age) {
        // ...
    }

}
```

The constants are passed to the builder of `ArgumentsValidatorBuilder#of`:

```java
Arguments3Validator<String, String, Integer, User> validator = ArgumentsValidatorBuilder
        .of(User::new)
        .builder(b -> b.constraint(_UserArgumentsMeta.NAME, c -> c.notBlank())
                .constraint(_UserArgumentsMeta.EMAIL, c -> c.notBlank().email())
                .constraint(_UserArgumentsMeta.AGE, c -> c.greaterThan(0)))
        .build();
```

For a method, the receiver is validated as the first argument:

```java
Arguments4Validator<UserService, String, String, Integer, User> validator = ArgumentsValidatorBuilder
        .of(UserService::createUser)
        .builder(b -> b.constraint(_UserServiceCreateUserArgumentsMeta.USERSERVICE, c -> c.notNull())
                .constraint(_UserServiceCreateUserArgumentsMeta.NAME, c -> c.notBlank())
                .constraint(_UserServiceCreateUserArgumentsMeta.EMAIL, c -> c.notBlank().email())
                .constraint(_UserServiceCreateUserArgumentsMeta.AGE, c -> c.greaterThan(0)))
        .build();
```

## Supported meta types

The processor generates a meta for the declared type of the annotated member. The following meta types are available in `am.ik.yavi.meta`:

* `StringConstraintMeta`
* `BooleanConstraintMeta`, `CharacterConstraintMeta`, `ByteConstraintMeta`, `ShortConstraintMeta`, `IntegerConstraintMeta`, `LongConstraintMeta`, `FloatConstraintMeta`, `DoubleConstraintMeta`
* `BigIntegerConstraintMeta`, `BigDecimalConstraintMeta`
* `ObjectConstraintMeta`
* `LocalDateConstraintMeta`, `LocalTimeConstraintMeta`, `LocalDateTimeConstraintMeta`, `OffsetDateTimeConstraintMeta`, `InstantConstraintMeta`, `YearConstraintMeta`, `YearMonthConstraintMeta`, `ZonedDateTimeConstraintMeta`

An enum target uses `ObjectConstraintMeta`.

## Changes in 0.18

* The generated code is annotated with JSpecify (`@org.jspecify.annotations.Nullable`), as the API is. Add `org.jspecify:jspecify` to the dependencies if the generated sources are compiled against the annotation.
* `Arguments1` to `Arguments16` are records, and the generated meta of a method refers to the container of the receiver, as shown above.
* Use the version of the YAVI artifact that matches the project, `0.18.0` here, in `annotationProcessorPaths`.
