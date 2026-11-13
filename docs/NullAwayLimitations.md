# Limitations of NullAway

Since 0.18.0 the type of a validated value tells whether it can be `null`:
`ValueValidator<String, String>` never succeeds with `null`, and
`ValueValidator<String, @Nullable String>` may. [NullAway](https://github.com/uber/NullAway)
in JSpecify mode checks this, with the exceptions listed on this page.

All of them are false positives: NullAway reports code that is correct. None of them
lets a `null` pass unnoticed. Each has a way to write the code so that it is accepted,
without `@SuppressWarnings`.

The messages below were produced by NullAway 0.14.2 and 0.14.3 with
`-XepOpt:NullAway:JSpecifyMode=true` and `-XepOpt:NullAway:OnlyNullMarked=true`. The
Kotlin compiler with `-Xjspecify-annotations=strict` has none of these limitations.

* [A lambda that implements a validator of a nullable value](#a-lambda-that-implements-a-validator-of-a-nullable-value)
* [`Objects.requireNonNull` as the argument of a generic method](#objectsrequirenonnull-as-the-argument-of-a-generic-method)
* [`map` with a function that returns `null`, without a target type](#map-with-a-function-that-returns-null-without-a-target-type)
* [The parameter of a lambda that is a validator of a nullable value](#the-parameter-of-a-lambda-that-is-a-validator-of-a-nullable-value)

## A lambda that implements a validator of a nullable value

NullAway compares the value returned by the lambda with `Validated<X>` as the interface
declares it, without replacing `X` with the nullable type argument of the target.

```java
ValueValidator<String, @Nullable String> validator = (s, locale, context) -> Validated.successWith(s);
```

```
[NullAway] incompatible types: Validated<@Nullable String> cannot be converted to Validated<X>
```

Write the validator as a class, or build it from an existing one:

```java
ValueValidator<String, @Nullable String> validator = new ValueValidator<String, @Nullable String>() {
    @Override
    public Validated<@Nullable String> validate(@Nullable String s, Locale locale, ConstraintContext context) {
        return Validated.successWith(s);
    }
};

ValueValidator<String, @Nullable String> same = ValueValidator.passThrough();
ValueValidator<String, @Nullable String> trimmed = Yavi._string(c -> c.trim()).nullable();
```

A lambda that implements a validator of a value that is not `null` is not affected.

## `Objects.requireNonNull` as the argument of a generic method

The input of a validator may be `null`, so a lambda that passes it on has to check it.
NullAway infers the type argument of `successWith` from the type of the parameter, which
is nullable, and not from the result of `requireNonNull`.

```java
ValueValidator<String, String> validator = (s, locale, context) -> Validated.successWith(Objects.requireNonNull(s));
```

```
[NullAway] incompatible types: Validated<@Nullable String> cannot be converted to Validated<X>
```

Assign the checked value to a variable first, or handle `null` in the expression:

```java
ValueValidator<String, String> validator = (s, locale, context) -> {
    final String value = Objects.requireNonNull(s);
    return Validated.successWith(value);
};

ValueValidator<String, String> orEmpty = (s, locale, context) -> Validated.successWith(s == null ? "" : s);
```

## `map` with a function that returns `null`, without a target type

The type of the mapped value is inferred from the function. NullAway infers a nullable
type only if the result of `map` is assigned to a declared type. In the middle of a chain
it takes the type as not nullable and reports the function.

```java
record Country(@Nullable String name) {}

Yavi._object(Country.class).map(Country::name).validated(country);
Yavi._object(Country.class).map(c -> c.name()).validated(country);
```

```
[NullAway] referenced method returns @Nullable, but functional interface method java.util.function.Function.apply(Country) returns @NonNull
[NullAway] returning @Nullable expression from method with @NonNull return type
```

Give the type argument, or assign the validator to a declared type:

```java
Yavi._object(Country.class).<@Nullable String>map(Country::name).validated(country);

ValueValidator<Country, @Nullable String> name = Yavi._object(Country.class).map(Country::name);
name.validated(country);
```

This applies to `map` of `ValueValidator`, of the `ArgumentsNValidator` types and of the
validators of a single value such as `StringValidator`.

## The parameter of a lambda that is a validator of a nullable value

NullAway drops the nullable type argument of the parameter of a lambda that it infers
from a collection.

```java
void validateAll(List<ValueValidator<String, @Nullable String>> validators) {
    validators.forEach(validator -> {
        Validated<@Nullable String> validated = validator.validate("a");
    });
}
```

```
[NullAway] incompatible types: Validated<String> cannot be converted to Validated<@Nullable String>
```

Use `var` for the result, declare the type of the parameter, or use a loop:

```java
validators.forEach(validator -> {
    var validated = validator.validate("a");
});

for (ValueValidator<String, @Nullable String> validator : validators) {
    Validated<@Nullable String> validated = validator.validate("a");
}
```
