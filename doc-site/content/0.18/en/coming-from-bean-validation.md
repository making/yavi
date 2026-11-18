# Comparison with Bean Validation

[Bean Validation](https://beanvalidation.org/) declares constraints as annotations on a class and validates an object that already exists. YAVI expresses the same constraints as Java method calls. This page maps the annotations to the constraint API and lists the differences.

## Basic

A Bean Validation form object:

```java
public class UserForm {

  @NotNull
  @Size(min = 1, max = 20)
  private String name;

  @NotNull
  @Size(min = 1, max = 50)
  @Email
  private String email;

  @NotNull
  @Min(0)
  @Max(200)
  private Integer age;

  // omitted setter/getter

}
```

The counterpart validates the object with `ValidatorBuilder`:

```java
Validator<UserForm> validator = ValidatorBuilder.<UserForm>of()
        .constraint(UserForm::getName, "name", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(20))
        .constraint(UserForm::getEmail, "email", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(50).email())
        .constraint(UserForm::getAge, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200))
        .build();
```

`ValidatorBuilder` is the counterpart of Bean Validation because both validate an object that was created by something else, such as a framework or a deserializer. The primary style of 0.18 is different: `Yavi.schema()` describes the properties of an input that does not exist yet and creates the typed object only when every property is valid. See [Object Validation](using-yavi.md) for the classic API and [Getting Started](getting-started.md) for the schema-first API.

## Constraints

Each annotation maps to a constraint method of the property. `@Min` and `@Max` are inclusive, so they map to `greaterThanOrEqual` and `lessThanOrEqual`; the exclusive forms are used only when `inclusive = false` is given.

### `@NotNull`

```java
.constraint(Foo::getText, "text", c -> c.notNull())
```

### `@Null`

```java
.constraint(Foo::getText, "text", c -> c.isNull())
```

### `@Pattern`

```java
.constraint(Foo::getText, "text", c -> c.pattern("[0-9]+"))
```

### `@Email`

```java
.constraint(Foo::getEmail, "email", c -> c.email())
```

### `@URL`

```java
.constraint(Foo::getUrl, "url", c -> c.url())
```

`url(url -> url.protocol("https").host("example.com").port(8443).build())` restricts the protocol, host and port. `c.uri()` accepts a URI with a scheme of any kind, such as `mailto:` or `s3://`.

### `@Min`

```java
.constraint(Foo::getIntValue, "intValue", c -> c.greaterThanOrEqual(1))
.constraint(Foo::getLongValue, "longValue", c -> c.greaterThanOrEqual(1L))
```

`@Min(value = 1, inclusive = false)` maps to `c.greaterThan(1)`.

### `@Max`

```java
.constraint(Foo::getIntValue, "intValue", c -> c.lessThanOrEqual(100))
.constraint(Foo::getLongValue, "longValue", c -> c.lessThanOrEqual(100L))
```

`@Max(value = 100, inclusive = false)` maps to `c.lessThan(100)`.

### `@Positive`

```java
.constraint(Foo::getIntValue, "intValue", c -> c.greaterThan(0))
```

### `@PositiveOrZero`

```java
.constraint(Foo::getIntValue, "intValue", c -> c.greaterThanOrEqual(0))
```

### `@Negative`

```java
.constraint(Foo::getIntValue, "intValue", c -> c.lessThan(0))
```

### `@NegativeOrZero`

```java
.constraint(Foo::getIntValue, "intValue", c -> c.lessThanOrEqual(0))
```

### `@DecimalMin`

```java
.constraint(Foo::getBigDecimalValue, "bigDecimalValue", c -> c.greaterThanOrEqual(new BigDecimal("1")))
```

`@DecimalMin(value = "1", inclusive = false)` maps to `c.greaterThan(new BigDecimal("1"))`.

### `@DecimalMax`

```java
.constraint(Foo::getBigDecimalValue, "bigDecimalValue", c -> c.lessThanOrEqual(new BigDecimal("100")))
```

`@DecimalMax(value = "100", inclusive = false)` maps to `c.lessThan(new BigDecimal("100"))`.

### `@Size`

```java
.constraint(Foo::getText, "text", c -> c.greaterThanOrEqual(4).lessThanOrEqual(64))
.constraint(Foo::getTexts, "texts", c -> c.greaterThanOrEqual(1).lessThanOrEqual(8))
```

The same methods apply to a string, a collection, an array and a map. The bounds are inclusive.

### `@NotEmpty`

```java
.constraint(Foo::getTexts, "texts", c -> c.notEmpty())
```

### `@NotBlank`

```java
.constraint(Foo::getText, "text", c -> c.notBlank())
```

### `@Digits`

```java
.constraint(Foo::getPrice, "price", c -> c.integerDigits(6).fractionDigits(2))
```

The constraints are available for a `BigDecimal`. Trailing zeros of the fraction are not counted: `1.500` has one fraction digit.

### `@AssertTrue`

```java
.constraint(Foo::getBooleanValue, "booleanValue", c -> c.isTrue())
```

### `@AssertFalse`

```java
.constraint(Foo::getBooleanValue, "booleanValue", c -> c.isFalse())
```

### `@Future`

```java
.constraint(Event::getStartsAt, "startsAt", c -> c.future())
```

### `@Past`

```java
Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));
Validator<Event> validator = ValidatorBuilder.<Event>of()
        .constraint(Event::getCreatedAt, "createdAt", c -> c.past(clock))
        .build();
```

`past()` compares against the current time of the system default time zone. The `past(Clock)` overload reads the current time from the given `Clock`, so a test with `Clock.fixed` gives the same result on every run.

### `@FutureOrPresent`

```java
.constraint(Event::getStartsAt, "startsAt", c -> c.futureOrPresent())
```

### `@PastOrPresent`

```java
.constraint(Event::getCreatedAt, "createdAt", c -> c.pastOrPresent())
```

The temporal constraints are available for `Instant`, `LocalDate`, `LocalDateTime`, `LocalTime`, `OffsetDateTime`, `ZonedDateTime`, `Year` and `YearMonth`. The message keys are `temporal.future`, `temporal.past`, `temporal.futureOrPresent` and `temporal.pastOrPresent`.

## `@Valid`

### Nested Object

```java
public class OrderForm {

  @NotNull
  @Valid
  private AddressForm address;

  // omitted setter/getter

}
```

to

```java
Validator<AddressForm> addressValidator = ValidatorBuilder.<AddressForm>of()
        .constraint(AddressForm::getZipCode, "zipCode", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(10))
        .constraint(AddressForm::getAddress, "address", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(100))
        .build();

Validator<OrderForm> validator = ValidatorBuilder.<OrderForm>of()
        .nest(OrderForm::getAddress, "address", addressValidator)
        .build();
```

A violation of the nested object is located under its name, such as `address.zipCode`. `nestIfPresent` validates the nested object only when it is not `null`. In a schema, the same object is added with `_object`; see [Objects and Collections](objects-and-collections.md).

### Nested List, Array and Map

```java
public class OrderForm {

  @Valid
  private List<AddressForm> addresses;

  // omitted setter/getter

}
```

to

```java
Validator<OrderForm> validator = ValidatorBuilder.<OrderForm>of()
        .forEach(OrderForm::getAddresses, "addresses", addressValidator)
        .build();
```

`forEach` applies a validator to every element of a list, an array or a map value, and `forEachIfPresent` skips a `null` collection. The violations are located under the index (`addresses[0].zipCode`). A schema uses `_list`, `_map` and a nested `_object` instead.

## Custom Constraints

A Bean Validation `ConstraintValidator`:

```java
public class UpperCaseValidator implements ConstraintValidator<UpperCase, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || value.equals(value.toUpperCase());
  }

}
```

The counterpart is a `CustomConstraint`, which combines the predicate with its message key and default message:

```java
public enum UpperCaseConstraint implements CustomConstraint<String> {

    SINGLETON;

    @Override
    public boolean test(String value) {
        return value.equals(value.toUpperCase());
    }

    @Override
    public String messageKey() {
        return "custom.upperCase";
    }

    @Override
    public String defaultMessageFormat() {
        return "\"{0}\" must be upper case";
    }

}
```

```java
Validator<Foo> validator = ValidatorBuilder.<Foo>of()
        .constraint(Foo::getText, "text", c -> c.predicate(UpperCaseConstraint.SINGLETON))
        .build();
```

For a rule that is used once, `c.predicate(predicate, messageKey, defaultMessageFormat)` writes the predicate inline. See [Custom Constraints](custom-constraints.md).

## Differences

* There is no metadata layer and no annotations. A constraint is a Java expression, so the compiler and refactoring tools see it.
* There is no Bean Validation `Validation.buildDefaultValidatorFactory()` and no provider lookup. A validator is built with `ValidatorBuilder` or `Yavi.schema()`.
* A constraint is checked by the compiler: a method reference or a constraint method of the wrong type does not compile, while an annotation on a field of the wrong type is found at run time.
* Groups and conditional validation are available on `ValidatorBuilder` with `constraintOnGroup` and `constraintOnCondition`; see [Object Validation](using-yavi.md).
* A violation has a structured `path()`, a JSON `pointer()` and a `messageKey()`, so an API can return it directly; see [Violations](violations.md).
* Nullness is declared with JSpecify (`@Nullable`), and a nullness checker such as NullAway checks the use of a validated value; see [NullAway](integrations-nullaway.md).
* Schema-first input validation is the primary use case of 0.18. `Yavi.schema()` reads a map, a JSON tree or request parameters and creates the object only when every property is valid; `Yavi.arguments()` is deprecated in its favor. The classic `ValidatorBuilder` API is not deprecated.
