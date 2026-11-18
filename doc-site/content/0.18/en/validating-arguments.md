# Argument Validation

An arguments validator validates the arguments of a constructor or a factory method before the object is created, so an object that does not satisfy its rules never exists.

```java
public final class Person {

    public static final Arguments3Validator<String, String, Integer, Person> validator = Yavi.schema()
            ._string("name", c -> c.notBlank().lessThanOrEqual(100))
            ._string("email", c -> c.notBlank().lessThanOrEqual(100).email())
            ._integer("age", c -> c.greaterThanOrEqual(0).lessThan(200))
            .apply(Person::new);

    private Person(String name, String email, int age) {
        // ...
    }

}
```

```java
Validated<Person> validated = Person.validator.validate("John Doe", "jdoe@example.com", 30);
```

`Arguments1Validator` to `Arguments16Validator` are available. The maximum number of arguments is 16.

`Yavi.arguments()` is the old name of `Yavi.schema()`. It still works but is deprecated; use `Yavi.schema()` for an arguments validator too. The deprecated call is the only difference: the builder and the arguments validator are the same.

```java
// deprecated
Yavi.arguments();

// current
Yavi.schema();
```

The type of the builder is `YaviSchema`; the former name `YaviArguments` is gone.

## Available argument kinds

An argument is added with a method for its type:

* `_string`
* `_boolean`
* `_short`, `_integer`, `_long`, `_float`, `_double`
* `_bigInteger`, `_bigDecimal`
* `_localDate`, `_localTime`, `_localDateTime`, `_offsetDateTime`, `_instant`, `_year`, `_yearMonth`, `_zonedDateTime`
* `_enum`
* `_object`
* `_list`, `_map`

`_object` accepts any reference type, including a collection or a map. `_list` and `_map` validate the elements or values of a list or a map.

```java
Arguments1Validator<LocalDate, LocalDate> dateValidator = Yavi.schema()
        ._localDate("date", c -> c.notNull())
        .apply(date -> date);

Arguments1Validator<List<? extends String>, List<String>> tagsValidator = Yavi.schema()
        ._list("tags", Yavi._string(c -> c.notBlank()))
        .apply(tags -> tags);
```

In 0.18 an `_enum` or `_object` argument takes the type, `_enum("role", Role.class)` and `_object("id", UUID.class)`, so that the argument can also be read from a map.

## Validating method arguments

`ArgumentsValidatorBuilder.of` validates the arguments of a method, including the receiver as the first argument. Each `_xxx` method takes an accessor for the argument position (`Arguments3::arg1` and so on) instead of a name.

```java
Arguments3Validator<UserService, String, String, User> validator = ArgumentsValidatorBuilder
        .of(UserService::createUser)
        .builder(b -> b._object(Arguments3::arg1, "userService", c -> c.notNull())
                ._string(Arguments3::arg2, "email", c -> c.email())
                ._string(Arguments3::arg3, "name", c -> c.notBlank()))
        .build();

Validated<User> validated = validator.validate(userService, "jdoe@example.com", "John Doe");
```

The `_xxx` methods of the builder take the accessor because `Arguments1Validator` to `Arguments16Validator` do not inherit from each other; use the accessor that matches the arity. This is also the form that the annotation processor generates; see [Annotation Processor](annotation-processor.md).

## Validators of a single value

A validator of one value is built with `Yavi._string`, `Yavi._integer` and the other methods of `Yavi`, or with the builder of its type.

```java
StringValidator<String> nameValidator = Yavi._string("name", c -> c.notBlank().lessThanOrEqual(100));

IntegerValidator<Integer> ageValidator = IntegerValidatorBuilder
        .of("age", c -> c.greaterThanOrEqual(0).lessThan(200))
        .build();

Validated<String> nameValidated = nameValidator.validate("John Doe");
```

`map` converts the validated value, for example to a value object. `build(Function)` of the builder of a single value does the same.

```java
StringValidator<Name> nameValidator = Yavi._string("name", c -> c.notBlank().lessThanOrEqual(100))
        .map(Name::new);
```

In 0.18 a single value is required unless `nullable()` says otherwise, and `build()` returns a type that knows how to write the value back (`StringValidator.Reversible` and the like). A declared variable of `StringValidator<String>` still compiles.

## Composing and adapting

`compose` validates a value of another type by converting it first:

```java
Arguments1Validator<Integer, Name> nameFromIdValidator = nameValidator.compose(Object::toString);
```

`wrap` turns an arguments validator into one that validates an `ArgumentsN` container, and `unwrap` turns it back:

```java
Arguments1Validator<Arguments1<String>, Name> wrapped = nameValidator.wrap();
Arguments1Validator<String, Name> unwrapped = Arguments1Validator.unwrap(wrapped);
```

`lazy()` returns a validator that produces a `Supplier` of the validated value, so the value is created only when the supplier is used.

```java
Arguments1Validator<String, Supplier<Name>> lazyValidator = nameValidator.lazy();
Supplier<Name> nameSupplier = lazyValidator.validated("John Doe");
```

`prefixed(String)` locates the violations of the value under a name. A lifted validator locates the violations of an element under its index; see [Combining Validators](combining-validators.md).

```java
ValueValidator<Iterable<? extends String>, List<Email>> emails = emailValidator.liftList();
```

`indexed(int)` is removed in 0.18. Use `prefixed(int)` on a `Validated` or a `ConstraintViolation` to put the index before the name:

```java
Validated<Email> validated = emailValidator.validate("yavi@example.com").prefixed(0);
// [0].email
```

`split` and `combine` combine validators; they are described in [Combining Validators](combining-validators.md).

## Arguments containers

`Arguments1` to `Arguments16` are immutable records that hold validated arguments. `Arguments.of(...)` creates one.

```java
Arguments3<String, String, Integer> arguments = Arguments.of("John Doe", "jdoe@example.com", 30);
String name = arguments.arg1();
String email = arguments.arg2();
Integer age = arguments.arg3();
```

`argN()` returns the argument at position `N`. `firstN()` and `lastN()` return the leading or trailing `N` arguments, and `append`, `prepend` and `reverse` return a modified copy. `toString()` returns `Arguments3[arg1=..., arg2=..., arg3=...]`.

```java
Arguments2<String, String> firstTwo = arguments.first2();
Arguments4<String, String, Integer, String> appended = arguments.append("extra");
Arguments3<Integer, String, String> reversed = arguments.reverse();
```

An `ArgumentsN` does not inherit from `ArgumentsM`, and the concrete container class is part of equality: two containers are equal only when they have the same type and the same argument values.
