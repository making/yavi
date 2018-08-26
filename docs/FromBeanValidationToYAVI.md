# From Bean Validation to YAVI

## Basic

```java
public class UserForm implements Serializable {

  private static final long serialVersionUID = 1L;

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

to

```java
Validator<UserForm> validator = Validator.builder(UserForm.class)
  .constraint(UserForm::getName, "name", c -> c.notNull().greaterThan(1).lessThan(20))
  .constraint(UserForm::getEmail, "email", c -> c.notNull().greaterThan(1).lessThan(50).email())
  .constraint(UserForm::getAge, "age", c -> c.notNull().greaterThan(0).lessThan(200))
  .build();
```

## Constraints 

### `@NotNull`

```java
public class Foo {
  @NotNull
  private String text;

  // omitted setter/getter
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getText, "text", c -> c.notNull())
  .build();
```

### `@Null`

```java
public class Foo {
  @NotNull
  private String text;

  // omitted setter/getter
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getText, "text", c -> c.isNull())
  .build();
```

### `@Pattern`

```java
public class Foo {
  @Pattern(regexp = "[0-9]+")
  private String text;
  
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getText, "text", c -> c.pattern("[0-9]+"))
  .build();
```

### `@Email`

```java
public class Foo {
  @Email
  private String text;
  
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getText, "text", c -> c.email())
  .build();
```

### `@URL`

```java
public class Foo {
  @URL
  private String text;
  
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getText, "text", c -> c.url())
  .build();
```

### `@Min`

```java
public class Foo {
  @Min(1)
  private Integer intValue;
  @Min(1)
  private Long longValue;
  // Byte, Character, Short, Double, Float, BigInteger, BigDecimal as well
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getIntValue, "intValue", c -> c.greaterThanOrEqual(1))
  .constraint(Foo::getLongValue, "longValue", c -> c.greaterThanOrEqual(1L))
  .build();
```


```java
public class Foo {
  @Min(value = 1, inclusive = false)
  private Integer intValue;
  @Min(value = 1, inclusive = false)
  private Long longValue;
  // Byte, Character, Short, Double, Float, BigInteger, BigDecimal as well
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getIntValue, "intValue", c -> c.greaterThan(1))
  .constraint(Foo::getLongValue, "longValue", c -> c.greaterThan(1L))
  .build();
```

### `@Max`

```java
public class Foo {
  @Max(100)
  private Integer intValue;
  @Max(100)
  private Long longValue;
  // Byte, Character, Short, Double, Float, BigInteger, BigDecimal as well
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getIntValue, "intValue", c -> c.lessThanOrEqual(100))
  .constraint(Foo::getLongValue, "longValue", c -> c.lessThanOrEqual(100L))
  .build();
```


```java
public class Foo {
  @Max(value = 1, inclusive = false)
  private Integer intValue;
  @Max(value = 1, inclusive = false)
  private Long longValue;
  // Byte, Character, Short, Double, Float, BigInteger, BigDecimal as well
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getIntValue, "intValue", c -> c.lessThan(100))
  .constraint(Foo::getLongValue, "longValue", c -> c.lessThan(100L))
  .build();
```

### `@Positive`


```java
public class Foo {
  @Positive
  private Integer intValue;
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getIntValue, "intValue", c -> c.greaterThan(0))
  .build();
```

### `@PositiveOrZero`


```java
public class Foo {
  @PositiveOrZero
  private Integer intValue;
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getIntValue, "intValue", c -> c.greaterThanOrEqual(0))
  .build();
```

### `@Negative`


```java
public class Foo {
  @Negative
  private Integer intValue;
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getIntValue, "intValue", c -> c.lessThan(0))
  .build();
```

### `@NegativeOrZero`


```java
public class Foo {
  @NegativeOrZero
  private Integer intValue;
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getIntValue, "intValue", c -> c.lessThanOrEqual(0))
  .build();
```

### `@DecimalMin`

```java
public class Foo {
  @DecimalMin("1")
  private BigDecimal bigDecimalValue;
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getBigDecimalValue, "bigDecimalValue", c -> c.greaterThanOrEqual(new BigDecimal("1")))
  .build();
```


```java
public class Foo {
  @DecimalMin(value = "1", inclusive = false)
  private BigDecimal bigDecimalValue;
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getBigDecimalValue, "bigDecimalValue", c -> c.greaterThan(new BigDecimal("1")))
  .build();
```

### `@DecimalMax`

```java
public class Foo {
  @DecimalMax("100")
  private BigDecimal bigDecimalValue;
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getBigDecimalValue, "bigDecimalValue", c -> c.lessThanOrEqual(new BigDecimal("100")))
  .build();
```


```java
public class Foo {
  @DecimalMax(value = "100", inclusive = false)
  private BigDecimal bigDecimalValue;
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getBigDecimalValue, "bigDecimalValue", c -> c.lessThan(new BigDecimal("100")))
  .build();
```

### `@Size`

```java
public class Foo {
  @Size(min = 4, max = 64)
  private String text;

  @Size(min = 1, max = 8)
  private Collection<String> texts;
  
  // Array, Map as well
  // omitted setter/getter
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getText, "text", c -> c.isGreaterThanOrEqual(4).isLessThanOrEqual(64))
  .constraint(Foo::getTexts, "texts", c -> c.isGreaterThanOrEqual(1).isLessThanOrEqual(8))
  .build();
```


### `@NotEmpty`

```java
public class Foo {
  @NotEmpty
  private String text;

  @NotEmpty
  private Collection<String> texts;
  
  // Array, Map as well
  // omitted setter/getter
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getText, "text", c -> c.notEmpty())
  .constraint(Foo::getTexts, "texts", c -> c.notEmpty())
  .build();
```

### `@NotBlank`

```java
public class Foo {
  @NotBlank
  private String text;

  // omitted setter/getter
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getText, "text", c -> c.notBlank())
  .build();
```

### `@Digits`

Not Implemented yet

### `@AssertTrue`

```java
public class Foo {
  @AssertTrue
  private boolean booleanValue;
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getBooleanValue, "booleanValue", c -> c.isTrue())
  .build();
```

### `@AssertFalse`

```java
public class Foo {
  @AssertFalse
  private boolean booleanValue;
  // omitted setter/getter 
}
```

to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getBooleanValue, "booleanValue", c -> c.isFalse())
  .build();
```

### `@Future`

Not Implemented yet

### `@Past`

Not Implemented yet

### `@FutureOrPresent`

Not Implemented yet

### `@PastOrPresent`

Not Implemented yet

## `@Valid`

### Nested Object

```java
public class AddressForm implements Serializable {
  @NotNull
  @Size(min = 1, max = 10)
  private String zipCode;

  @NotNull
  @Size(min = 1, max = 100)
  private String address;

  // omitted setter/getter
}

public class OrderForm implements Serializable {
  @NotNul // <-- !!
  @Valid
  private AddressForm address;
  
  // omitted setter/getter
}
```

to

```java
Validator<OrderForm> validator = Validator.builder(OrderForm.class)
  .constraintForNested(OrderForm::getAddress, "address", 
  b -> b.constraint(AddressForm::getZipCode, "zipCode", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(10))
    .constraint(AddressForm::getZipCode, "address", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(100)))
  .build();
```

or

```java
Validator<AddressForm> addressValidator = Validator.builder(AddressForm.class)
    .constraint(AddressForm::getZipCode, "zipCode", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(10))
    .constraint(AddressForm::getZipCode, "address", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(100))
    .build();

Validator<OrderForm> validator = Validator.builder(OrderForm.class)
  .constraintForNested(OrderForm::getAddress, "address", addressValidator)
  .build();
```

```java
public class OrderForm implements Serializable {
  @Valid
  private AddressForm address;
  
  // omitted setter/getter
}
```

to

```java
Validator<OrderForm> validator = Validator.builder(OrderForm.class)
  .constraintIfPresentForNested(OrderForm::getAddress, "address", 
  b -> b.constraint(AddressForm::getZipCode, "zipCode", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(10))
    .constraint(AddressForm::getZipCode, "address", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(100)))
  .build();
```

or

```java
Validator<OrderForm> validator = Validator.builder(OrderForm.class)
  .constraintIfPresentForNested(OrderForm::getAddress, "address", addressValidator)
  .build();
```


### Nested List/Array/Map

```java
public class AddressForm implements Serializable {
  @NotNull
  @Size(min = 1, max = 10)
  private String zipCode;

  @NotNull
  @Size(min = 1, max = 100)
  private String address;

  // omitted setter/getter
}

public class OrderForm implements Serializable {
  @NotNul // <-- !!
  @Valid
  private List<AddressForm> addresses;
  
  // omitted setter/getter
}
```

```java
Validator<OrderForm> validator = Validator.builder(OrderForm.class)
  .constraintForEach(OrderForm::getAddress, "addresses", 
  b -> b.constraint(AddressForm::getZipCode, "zipCode", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(10))
    .constraint(AddressForm::getZipCode, "address", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(100)))
  .build();
```

or

```java
Validator<AddressForm> addressValidator = Validator.builder(AddressForm.class)
    .constraint(AddressForm::getZipCode, "zipCode", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(10))
    .constraint(AddressForm::getZipCode, "address", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(100))
    .build();

Validator<OrderForm> validator = Validator.builder(OrderForm.class)
  .constraintForEach(OrderForm::getAddresses, "addresses", addressValidator)
  .build();
```


```java
public class AddressForm implements Serializable {
  @NotNull
  @Size(min = 1, max = 10)
  private String zipCode;

  @NotNull
  @Size(min = 1, max = 100)
  private String address;

  // omitted setter/getter
}

public class OrderForm implements Serializable {
  @Valid
  private List<AddressForm> addresses;
  
  // omitted setter/getter
}
```

```java
Validator<OrderForm> validator = Validator.builder(OrderForm.class)
  .constraintIfPresentForEach(OrderForm::getAddress, "addresses", 
  b -> b.constraint(AddressForm::getZipCode, "zipCode", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(10))
    .constraint(AddressForm::getZipCode, "address", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(100)))
  .build();
```

or

```java
Validator<AddressForm> addressValidator = Validator.builder(AddressForm.class)
    .constraint(AddressForm::getZipCode, "zipCode", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(10))
    .constraint(AddressForm::getZipCode, "address", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(100))
    .build();

Validator<OrderForm> validator = Validator.builder(OrderForm.class)
  .constraintIfPresenForEach(OrderForm::getAddresses, "addresses", addressValidator)
  .build();
```

```java
public class Foo { 
  @NotNull  
  private List<@Size(max = 2) String> texts;
  
  // omitted setter/getter
}
```

```java
Validator<String> stringValidator = Validator.builder(String.class)
  .constraint((ToCharSequence<String, String>) o -> o, "value", c -> c.lessThanOrEqual(2))
  .build();

Validator<Foo> validator = Validator.builder(Foo.class)
  .constraintForEach(Foo::getTexts, "texts", stringValidator)
  .build();
```

### Custom Constraint


```java
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = UpperCaseValidator.class)
@Documented
public @interface UpperCase {
    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    CaseMode value();

    @Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        UpperCase[] value();
    }
}
```

```java
public class UpperCaseValidator implements ConstraintValidator<UpperCase, String> {
  @Override
  public void initialize(UpperCase constraintAnnotation) {
  }

  @Override
  public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
    if (object == null) {
      return true;
    }
    return object.equals(object.toUpperCase());
  }
}
```

```java
public class Foo {
  @UpperCase
  private String text;

  // omitted setter/getter
}
```

to

```java
public enum UpperCaseConstraint implements CustomConstraint<String> {
    SINGLETON;

    @Override
    public boolean test(String s) {
        return s.equals(s.toUpperCase());
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


to

```java
Validator<Foo> validator = Validator.builder(Foo.class)
  .constraint(Foo::getText, "text", c -> c.predicate(UpperCaseConstraint.SINGLETON))
  .build();
```