# Comparison with Raoh

[Raoh](https://github.com/raoh-project/raoh-java) is a Java library of decoders inspired by
Zod: a decoder takes untyped input and returns either a domain object or all the errors
with their paths. YAVI covers the same workflow since 0.18.0. This page writes the
sections of the [Raoh tutorial](https://github.com/raoh-project/raoh-java/blob/main/docs/tutorial.md)
with YAVI, in the same order, and lists what YAVI cannot do yet.

* [Vocabulary](#vocabulary)
* [Values](#values): primitives, constraints, coercion, domain primitives
* [Objects](#objects): nested, flat, lists, maps, optional, enum, strict, recursive
* [Unions](#unions)
* [Cross-field rules](#cross-field-rules)
* [Defaults and recovery](#defaults-and-recovery)
* [Several results and several rows](#several-results-and-several-rows)
* [Errors](#errors)
* [Scenarios](#scenarios)
* [Differences](#differences)
* [Not available](#not-available)

The examples assume these imports:

```java
import am.ik.yavi.arguments.*;
import am.ik.yavi.core.*;
import am.ik.yavi.input.*;
import am.ik.yavi.validator.Yavi;
```

## Vocabulary

| Raoh | YAVI |
| --- | --- |
| `Decoder<I, T>` | `ValueValidator<I, T>` |
| `decoder.decode(input)` returns `Result<T>` | `validator.validate(input)` returns `Validated<T>` |
| `Ok(value)` / `Err(issues)` | `Validated.Valid(value)` / `Validated.Invalid(violations)`, a sealed interface |
| `Issue` (`path`, `code`, `message`, `meta`) | `ConstraintViolation` (`path()` / `name()`, `messageKey()`, `message()`, `args()`) |
| path `/address/zip`, `/items/1/name` | `pointer()` `/address/zip`, `/items/1/name`; `name()` `address.zip`, `items[1].name` |
| `combine(field(...), ...).map(f)` | `Yavi.arguments()._xxx(...).fromMap(f)` (`MapInputValidator<X>`) |
| `ObjectDecoders` (input `Object`) | `validator.untyped()` |
| `MapDecoders` / `JsonDecoders` / `JooqRecordDecoders` | `fromMap(f)` / `from(reader, f)` with an `InputReader` |
| `decoder.map(f)` | `validator.map(f)` |
| `decoder.pipe(next)` | `validator.andThen(next)` |
| `decoder.flatMap(f)` | `validator.andThen(next)`, or `constraintOnTarget` for a predicate |
| `combine(field(...), ...).flatMap(f)` | `Yavi.arguments()._xxx(...).flatFromMap(f)` (`flatApply(f)` for arguments) |
| `Result.fail(code, message)` | `Validated.failureWith(ViolationMessage.of(key, format), value, locale)` |
| `result.map(f)` / `result.flatMap(f)` | `validated.map(f)` / `validated.flatMap(f)` |

In Raoh the kind of input is chosen first, with the static import (`MapDecoders.*` or
`JsonDecoders.*`). In YAVI the arguments are written once and the input is chosen last:
`fromMap(f)` for a map, `.readingWith(reader)` for a JSON tree, `.readingParameters()` for
request parameters, `apply(f)` for typed arguments.

## Values

### Primitive values

A validator of a single value is built with `Yavi._string(c -> ...)`, `Yavi._integer(c -> ...)`
and so on: the methods have the names of the arguments of `Yavi.arguments()` and take the
constraints. `untyped()` makes the validator accept an `Object` and check its type, which
is what `string()` and `int_()` of Raoh do.

```java
ValueValidator<Object, String> string = Yavi._string(c -> c.notNull()).untyped();
ValueValidator<Object, Integer> integer = Yavi._integer(c -> c.notNull()).untyped();

string.validate("hello"); // Valid[hello]
integer.validate(42); // Valid[42]
integer.validate(42L); // Valid[42], a number is taken as long as its value is kept
integer.validate("not a number"); // object.type: The value must be of type integer
```

| Raoh | YAVI |
| --- | --- |
| `string()` | `Yavi._string(c -> ...)` |
| `int_()`, `long_()`, `double_()`, `float_()`, `decimal()` | `Yavi._integer`, `Yavi._long`, `Yavi._double`, `Yavi._float`, `Yavi._bigDecimal` |
| `bool()` | `Yavi._boolean(c -> ...)` |
| `bytes()` | `Yavi._object(byte[].class, c -> c.notNull())` |
| `date()`, `iso8601()` | `Yavi._localDate`, `Yavi._instant` and the other temporal methods |
| `enumOf(Role.class)` | `Yavi._enum(Role.class)` |

A value is not required unless a constraint says so: a Raoh decoder rejects `null`, a YAVI
validator accepts it without `notNull()` or `notBlank()`.

A violation of such a validator has the empty name, and its message says "The value" where
a named one shows the name. The name is given by the place where the validator is used
(`tags[0]`, `prices.apple`), or as the first parameter: `Yavi._string("email", c -> ...)`.

### Constraints

| Raoh | YAVI |
| --- | --- |
| `string().email()` | `c.email()` |
| `minLength(3).maxLength(20)` | `c.greaterThanOrEqual(3).lessThanOrEqual(20)` |
| `fixedLength(n)` | `c.fixedSize(n)` |
| `nonBlank()` | `c.notBlank()` |
| `pattern(regex)` | `c.pattern(regex)` |
| `uuid()` (returns `UUID`) | `c.uuid()`, then `.map(UUID::fromString)` |
| `url()` (returns `URI`, `http` / `https` only) | `c.url()` (any protocol of `java.net.URL`), then `.map(URI::create)` |
| `int_().range(0, 150)` | `c.greaterThanOrEqual(0).lessThanOrEqual(150)` |
| `positive()`, `nonNegative()` | `c.positive()`, `c.positiveOrZero()` |
| `decimal().scale(4)` | `c.predicate(d -> d.scale() <= 4, key, message)` |
| `date().after(d)`, `before(d)`, `between(a, b)` | `c.after(() -> d)`, `c.before(() -> d)`, `c.between(() -> a, () -> b)` |
| a bound taken from a clock by the caller | `c.past()`, `c.future()`, `c.past(clock)` ... |
| `string().date()`, `string().iso8601()` | not needed: a temporal validator reads an ISO-8601 string |
| a trailing custom message | `.message("...")` after the constraint |

```java
ValueValidator<Object, LocalDate> date = Yavi._localDate(c -> c.notNull().after(() -> LocalDate.of(2020, 1, 1)))
        .untyped();

date.validate("2025-06-15"); // Valid[2025-06-15]
date.validate("2025-13-15"); // object.type: The value must be of type LocalDate
```

### Transforms and normalization

Raoh chains transforms with constraints (`string().trim().toLowerCase().email()`). So
does the validator of a `String` value in YAVI: the transforms are written before the
constraints and applied in that order, and the constraints and the produced value see the
transformed value.

```java
StringValidator<String> normalizedEmail = Yavi._string(c -> c.trim().toLowerCase().notBlank().email());

normalizedEmail.validate("  USER@Example.COM  "); // Valid[user@example.com]
```

| Raoh | YAVI |
| --- | --- |
| `trim()` | `c.trim()` (white space of `Character.isWhitespace`, as for `notBlank()`) |
| `toLowerCase()`, `toUpperCase()` | `c.toLowerCase()`, `c.toUpperCase()` (`Locale.ROOT`) |
| `normalize()` | `c.toNormalForm(Normalizer.Form.NFC)` |
| `map(f)` before the constraints | `c.transform(f)` |

* A transform is written before the first constraint; `c.notBlank().trim()` does not
  compile. `null` is not transformed.
* A value of another type is transformed with `preprocess` on its validator:
  `Yavi._bigDecimal("price", c -> c.positive()).preprocess(BigDecimal::stripTrailingZeros)`.
* A transform keeps the description and the input type, so the validator is an argument of
  `fromMap`, where the transform runs after the type check. `compose` keeps neither:
  `email.compose(String::trim)` must be given its type again with `untyped(String.class)`.
* The constraints of a property (`ValidatorBuilder#constraint`) have no transforms: that
  validator checks an object and does not produce a value.

The size constraints count code points of the NFC form by default, so `lessThanOrEqual(1)`
accepts a decomposed が (U+304B U+3099) without `normalize()`. `c.normalizer(form)` selects
another form for counting, and `c.normalizer(null)` none; neither changes the value.

### String-to-type conversions (coerce)

In Raoh the conversion is written per decoder (`string().toInt().range(0, 150)`). In YAVI
it is a property of the input: the validator of an `Integer` does not change, and the
reader takes strings as text that stands for the type.

```java
InputReader<Object> text = InputReader.ofJdkTypes().coercingStrings();
ValueValidator<Object, Integer> age = Yavi._integer(c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(150))
        .readingWith(text);

age.validate("25"); // Valid[25]
age.validate("abc"); // object.type
age.validate(""); // object.notNull, an empty string is null
```

For a whole map of strings use `readingParameters()`; see [Pagination](#pagination-and-query-parameters).
The rules are stricter than those of Raoh:

| Input | Raoh | YAVI |
| --- | --- | --- |
| `"1"`, `"yes"`, `"on"` for a boolean | `true` (`toBool()`) | `object.type`; only `"true"` and `"false"` are taken |
| `"  42  "` for a number | `string().trim().toInt()` | `object.type`; the text of a number is not trimmed |
| `""` | an error of `toInt()` | `null`, so `optional()` and `defaultValue` apply |

Other strings are taken by a reader that overrides `InputReader#value(node, type)`.

### Domain primitives

`map(f)` creates the domain type. A named validator is reused as an argument:

```java
record Email(String value) {}
record Age(int value) {}
record UserId(UUID value) {}

StringValidator<Email> emailValidator = Yavi._string("email", c -> c.notBlank().email()).map(Email::new);
IntegerValidator<Age> ageValidator = Yavi._integer("age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(150))
        .map(Age::new);
StringValidator<UserId> userIdValidator = Yavi._string("id", c -> c.notNull().uuid())
        .map(s -> new UserId(UUID.fromString(s)));
```

## Objects

### combine + field

```java
record User(UserId id, Email email, Age age) {}

MapInputValidator<User> userValidator = Yavi.arguments()
        ._string(userIdValidator)
        ._string(emailValidator)
        ._integer(ageValidator)
        .fromMap(User::new);

userValidator.validate(Map.of("id", "not-uuid", "email", "invalid", "age", 300));
// id: "id" must be a valid UUID
// email: "email" must be a valid email address
// age: "age" must be less than or equal to 150
```

An argument is also written in place
(`._string("email", c -> c.trim().toLowerCase().notBlank().email())`), and a validator
without a name is given one with `_object(name, validator)`:
`._object("email", normalizedEmail.map(Email::new))`.

### Nested objects

`nested(decoder)` is `_object(name, validator)`:

```java
record Address(String city, String zip) {}
record Customer(String name, Address address) {}

MapInputValidator<Address> addressValidator = Yavi.arguments()
        ._string("city", c -> c.notBlank())
        ._string("zip", c -> c.notNull().pattern("^\\d{3}-\\d{4}$"))
        .fromMap(Address::new);

MapInputValidator<Customer> customerValidator = Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._object("address", addressValidator)
        .fromMap(Customer::new);

customerValidator.validate(Map.of("name", "", "address", Map.of("city", "", "zip", "bad")));
// name, address.city, address.zip
```

### Flat data

`combine(flat(a), flat(b))` is `_embedded`: the embedded validators read the same map as
the other arguments.

```java
record UserName(String first, String last) {}
record Department(String name, String code) {}
record Employee(UserName name, Department dept) {}

MapInputValidator<UserName> userNameValidator = Yavi.arguments()
        ._string("first_name", c -> c.notBlank())
        ._string("last_name", c -> c.notBlank())
        .fromMap(UserName::new);

MapInputValidator<Department> departmentValidator = Yavi.arguments()
        ._string("dept_name", c -> c.notBlank())
        ._string("dept_code", c -> c.notBlank())
        .fromMap(Department::new);

MapInputValidator<Employee> employeeValidator = Yavi.arguments()
        ._embedded(userNameValidator)
        ._embedded(departmentValidator)
        .fromMap(Employee::new);
```

The result is a `MapInputValidator` like any other: it is nested, reads a JSON tree or
request parameters and describes the four properties. Unlike `flat`, it can be strict: the
properties of the embedded validators are known to `rejectingUnknownProperties()`. A name
that two of them define is rejected when the validator is built.

A LEFT JOIN whose columns are all `null` is an embedded validator that accepts `null`: it
is given `null` when none of its properties has a value.

```java
MapInputValidator<Employee> employeeValidator = Yavi.arguments()
        ._embedded(userNameValidator)
        ._embedded(departmentValidator).nullable()
        .fromMap(Employee::new);

employeeValidator.validate(Map.of("first_name", "Taro", "last_name", "Yamada"));
// Employee[name=UserName[first=Taro, last=Yamada], dept=null]
```

`ArgumentsValidators.combine(a, b)` combines validators of any input type, not only of a
map, and for that reason knows neither the input type nor the properties.

### Lists

`list(nested(decoder))` is `_list(name, elementValidator)`. Every element is validated and
a violation is located under its index.

```java
record OrderItem(String productId, int quantity) {}

MapInputValidator<OrderItem> orderItemValidator = Yavi.arguments()
        ._string("productId", c -> c.notBlank())
        ._integer("quantity", c -> c.notNull().positive())
        .fromMap(OrderItem::new);

MapInputValidator<List<OrderItem>> itemsValidator = Yavi.arguments()
        ._list("items", orderItemValidator)
        .fromMap(items -> items);

itemsValidator.validate(Map.of("items", List.of(
        Map.of("productId", "A001", "quantity", 3),
        Map.of("productId", "", "quantity", -1))));
// items[1].productId, items[1].quantity
```

The constraints of the list itself (`nonempty()`, `minSize`, `maxSize`, `unique()`) are
the third argument of `_list`. They are checked on the validated elements, only when
every element is valid, and reported on the list (`tags`):

```java
Yavi.arguments()
        ._list("tags", Yavi._string(c -> c.trim().notBlank()), c -> c.notEmpty().lessThanOrEqual(10).unique())
        .fromMap(tags -> tags);
```

A list that is not a property, such as the root of a document, is
`Yavi._list(elementValidator, c -> ...)`.

### Maps

`map(decoder)` is `_map(name, valueValidator)`; a violation is located under the key
(`prices.apple`). The constraints of the map (`minSize`) are the third argument.

```java
Yavi.arguments()
        ._map("prices", Yavi._bigDecimal(c -> c.notNull().positive()), c -> c.notEmpty())
        .fromMap(prices -> prices);
```

### JSON stored in a string

`pipe` is `andThen`. The parser is a validator that reports its failure as a violation,
and the violations of the inner validator are located under the property:

```java
ValueValidator<String, Map<String, Object>> parseJson = (s, locale, context) -> {
    try {
        return Validated.successWith(objectMapper.readValue(s, new TypeReference<>() {}));
    }
    catch (JacksonException e) {
        return Validated.failureWith(ViolationMessage.of("json.invalid", "\"{0}\" cannot be parsed as JSON"), s, locale);
    }
};

record Params(String kind, int count) {}

MapInputValidator<Params> paramsValidator = Yavi.arguments()
        ._string("kind", c -> c.notNull())
        ._integer("count", c -> c.notNull())
        .fromMap(Params::new);

MapInputValidator<Params> validator = Yavi.arguments()
        ._object("params", Yavi._string(c -> c.notNull()).andThen(parseJson).andThen(paramsValidator))
        .fromMap(params -> params);

validator.validate(Map.of("params", "not json")); // params: "params" cannot be parsed as JSON
validator.validate(Map.of("params", "{\"kind\":\"like\"}")); // params.count: "params.count" is required
```

### Optional and nullable

| Raoh | YAVI |
| --- | --- |
| `field("email", string().email())` | `_string("email", c -> c.notNull().email())` |
| `field("nickname", nullable(string()))` | `_string("nickname").nullable()` |
| `optionalField("middleName", string())` (`Optional<String>`) | `_string("middleName").optional()` |
| `field("role", withDefault(decoder, value))` | `_enum("role", Role.class).defaultValue(value)` |
| `field("size", recover(decoder, fallback))` | `_integer("size", c -> ...).recover(fallback)` |
| `optionalNullableField("bio", string())` (`Presence<String>`) | not available |

An absent key is validated like `null`, so `optional()`, `nullable()` and
`defaultValue(...)` accept both, and "clear the value" cannot be told from "leave
unchanged" in a PATCH request. Only the violation differs when the argument rejects
`null`: `object.required` for an absent key and `object.notNull` for an explicit `null`.

### Enums

```java
enum Role { ADMIN, MEMBER, GUEST }

MapInputValidator<Role> roleValidator = Yavi.arguments()
        ._enum("role", Role.class).defaultValue(Role.MEMBER)
        .fromMap(role -> role);

roleValidator.validate(Map.of("role", "ADMIN")); // Valid[ADMIN]
roleValidator.validate(Map.of()); // Valid[MEMBER]
roleValidator.validate(Map.of("role", "admin")); // object.oneOf: must be one of [ADMIN, MEMBER, GUEST]
```

The name of the constant is matched exactly, whereas `enumOf` ignores the ASCII case.

### Strict mode

`strict(f)` is `rejectingUnknownProperties()`:

```java
MapInputValidator<ApiRequest> apiRequestValidator = Yavi.arguments()
        ._string("action", c -> c.notBlank())
        ._integer("amount", c -> c.notNull().positive())
        .fromMap(ApiRequest::new)
        .rejectingUnknownProperties();

apiRequestValidator.validate(Map.of("action", "transfer", "amount", 100, "extra", true));
// extra: "extra" is not a known property
```

### Recursive structures

Raoh's `lazy(() -> decoder)` refers to a decoder that is not built yet. YAVI has no
counterpart: `lazy()` of `ArgumentsNValidator` is unrelated, it defers the creation of the
object (`Supplier<X>`), for a validator called from a constructor. A lambda that refers to
the validator serves a map, and must be given its input type:

```java
record Comment(String body, List<Comment> replies) {}

@SuppressWarnings("unchecked")
ValueValidator<Map<String, Object>, Comment>[] self = new ValueValidator[1];
ValueValidator<Map<String, Object>, Comment> lazy = (map, locale, context) -> self[0].validate(map, locale, context);
@SuppressWarnings({ "unchecked", "rawtypes" })
ValueValidator<Object, Comment> reply = lazy.untyped((Class) Map.class);

MapInputValidator<Comment> commentValidator = Yavi.arguments()
        ._string("body", c -> c.notBlank())
        ._list("replies", reply).defaultValue(List.of())
        .fromMap(Comment::new);
self[0] = commentValidator;
```

It reads a map and request parameters. It does not read a JSON tree
(`readingWith(reader)` reports `replies[0]` with `object.type`), and the JSON Schema of
`replies` has no item schema.

## Unions

### oneOf

`oneOf(a, b)` is `ValueValidator.anyOf(a, b)`, and `literal("email")` is the `equalTo`
constraint:

```java
sealed interface Contact {}
record EmailContact(String address) implements Contact {}
record PhoneContact(String number) implements Contact {}

ValueValidator<Map<String, Object>, Contact> contactValidator = ValueValidator.anyOf(
        Yavi.arguments()
            ._string("kind", c -> c.notNull().equalTo("email"))
            ._string("value", c -> c.notNull().email())
            .fromMap((kind, value) -> new EmailContact(value)),
        Yavi.arguments()
            ._string("kind", c -> c.notNull().equalTo("phone"))
            ._string("value", c -> c.notNull().pattern("^\\d{10,15}$"))
            .fromMap((kind, value) -> new PhoneContact(value)));
```

No cast to the supertype is needed. When no alternative matches, Raoh reports one issue
(`no variant matched`), and YAVI reports `object.anyOf` followed by the violations of
every alternative.

### discriminate

`discriminate("type", variant(...), ...)` is `DiscriminatedUnionValidator`. An alternative
does not define the discriminator.

```java
sealed interface Shape {}
record Circle(double radius) implements Shape {}
record Rect(double width, double height) implements Shape {}

ValueValidator<Map<String, Object>, Shape> shapeValidator = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Yavi.arguments()._double("radius", c -> c.notNull().positive()).fromMap(Circle::new))
        .when("rect", Yavi.arguments()
            ._double("width", c -> c.notNull().positive())
            ._double("height", c -> c.notNull().positive())
            .fromMap(Rect::new));

shapeValidator.validate(Map.of("type", "hexagon"));
// type: "type" must be one of the following values: [circle, rect]
```

The conditional decoding of the tutorial (a lambda that reads `method` and switches) is
the same validator: `DiscriminatedUnionValidator.<Payment>of("method").when("credit_card", ...).when("bank_transfer", ...)`.

## Cross-field rules

A rule that is a predicate on the created object is `constraintOnTarget`. It is evaluated
only when all arguments are valid, like `flatMap`, and its name locates the violation.

```java
record DateRange(LocalDate start, LocalDate end) {}

ValueValidator<Map<String, Object>, DateRange> dateRangeValidator = Yavi.arguments()
        ._localDate("start", c -> c.notNull())
        ._localDate("end", c -> c.notNull())
        .fromMap(DateRange::new)
        .constraintOnTarget(range -> range.start().isBefore(range.end()), "",
                ViolationMessage.of("dateRange.invalid", "start must be before end"));
```

With the empty name the violation is reported on the object itself, and under `period`
when the validator is used as `_object("period", dateRangeValidator)`.

Rules on values that are not kept in the result read an intermediate object, and `map`
creates the result:

```java
record PasswordForm(String current, String newPassword, String confirm) {}
record PasswordChange(String current, String newPassword) {}

ValueValidator<Map<String, Object>, PasswordChange> passwordChangeValidator = Yavi.arguments()
        ._string("currentPassword", c -> c.notBlank())
        ._string("newPassword", c -> c.notNull().greaterThanOrEqual(8).lessThanOrEqual(128))
        ._string("confirmPassword", c -> c.notBlank())
        .fromMap(PasswordForm::new)
        .constraintOnTarget(form -> form.newPassword().equals(form.confirm()), "confirmPassword",
                ViolationMessage.of("password.mismatch", "\"{0}\" must match \"newPassword\""))
        .constraintOnTarget(form -> !form.current().equals(form.newPassword()), "newPassword",
                ViolationMessage.of("password.same", "\"{0}\" must differ from \"currentPassword\""))
        .map(form -> new PasswordChange(form.current(), form.newPassword()));
```

The rule of amount and currency is the same:

```java
enum Currency { JPY, USD, EUR }
record Money(BigDecimal amount, Currency currency) {}

ValueValidator<Map<String, Object>, Money> moneyValidator = Yavi.arguments()
        ._bigDecimal("amount", c -> c.notNull().positiveOrZero())
        ._enum("currency", Currency.class, c -> c.notNull())
        .fromMap(Money::new)
        .constraintOnTarget(money -> money.amount().scale() <= (money.currency() == Currency.JPY ? 0 : 2), "amount",
                ViolationMessage.of("money.scale", "\"{0}\" has too many decimal places for the currency"));
```

A step that creates the result itself and can fail, such as a factory method, is given to
`flatFromMap` (`flatApply` for arguments) in place of `fromMap`. It receives the validated
arguments and returns a `Validated`:

```java
record DateRange(LocalDate start, LocalDate end) {

    static Validated<DateRange> parse(LocalDate start, LocalDate end) {
        return start.isBefore(end) ? Validated.successWith(new DateRange(start, end))
                : Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"), start,
                        Locale.getDefault());
    }
}

MapInputValidator<DateRange> dateRangeValidator = Yavi.arguments()
        ._localDate("start", c -> c.notNull())
        ._localDate("end", c -> c.notNull())
        .flatFromMap(DateRange::parse);
```

`Validated.failureWith(message, value, locale, args...)` creates a violation with the empty
name; `ConstraintViolation.of(message, value, locale).prefixed("end")` locates it on a
property. A step on a single value, or one that needs the locale of the validation, is a
validator given to `andThen`, as `parseJson` above.

## Defaults and recovery

`withDefault` is `defaultValue(...)`: it applies to an absent key and to `null`, and a
value that is present but invalid is a violation.

`recover(decoder, fallback)` is `recover(fallback)`: it applies to an absent key and to
`null` like `defaultValue(...)`, and to a value that is present but invalid, so the
argument never fails. It is for a parameter that must not break a request.

```java
MapInputValidator<Integer> pageSizeValidator = Yavi.arguments()
        ._integer("pageSize", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
        .fromMap(pageSize -> pageSize);

pageSizeValidator.validate(Map.of("pageSize", 50)); // Valid[50]
pageSizeValidator.validate(Map.of("pageSize", 999)); // Valid[20]
pageSizeValidator.validate(Map.of("pageSize", "many")); // Valid[20]
pageSizeValidator.validate(Map.of()); // Valid[20]
```

The value is read by the reader before it is recovered: with `readingParameters()` the
string `"50"` is the integer `50`, and with `readingWith(reader)` a node that is not a
number gives the fallback. `recover` is on every `ValueValidator`
(`Yavi._integer(c -> ...).recover(20)`, `addressValidator.recover(fallback)`), and it is
described like `defaultValue` (`default` in the JSON Schema).

`recoverWith(f)` computes the fallback from the violations, for example to log them. For
an absent key or `null` there is no violation:

```java
._integer("pageSize", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recoverWith(violations -> {
    violations.forEach(violation -> log.debug("{}", violation.message()));
    return 20;
})
```

## Several results and several rows

### Result.map2

Two results of different sources are combined with `combine`, which accumulates the
violations of both:

```java
Validated<UserName> name = userNameValidator.validate(nameRow);
Validated<Department> department = departmentValidator.validate(departmentRow);
Validated<Employee> employee = Validated.of(name.combine(department).apply(Employee::new));
```

### Result.traverse

`Result.traverse(rows, decoder::decode, Path.of("orders"))` is `liftList` with `prefixed`.
The violations of a row are located under its index:

```java
Validated<List<Order>> orders = orderValidator.liftList().prefixed("orders").validate(rows);
// orders[1].order_id, orders[1].total
```

A one-to-many JOIN is validated row by row and grouped after:

```java
Arguments1Validator<Map<String, Object>, Map.Entry<OrderHeader, OrderLine>> rowValidator = ArgumentsValidators
        .combine(headerValidator, lineValidator)
        .apply((header, line) -> Map.entry(header, line));

Validated<List<OrderWithLines>> orders = rowValidator.liftList()
        .prefixed("rows")
        .validate(joinRows)
        .map(entries -> entries.stream()
            .collect(Collectors.groupingBy(Map.Entry::getKey, LinkedHashMap::new,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toList())))
            .entrySet()
            .stream()
            .map(entry -> new OrderWithLines(entry.getKey(), entry.getValue()))
            .toList());
```

### Tuples

`combine(...).map(Tuple2::new)` is `fromMap(Arguments::of)`, which produces an
`Arguments2<String, Integer>` with `arg1()` and `arg2()`. It is a record, so it is
deconstructed in a `switch`:

```java
MapInputValidator<Arguments2<String, Integer>> validator = Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._integer("age", c -> c.notNull())
        .fromMap(Arguments::of);

switch (validator.validate(input)) {
    case Validated.Valid(Arguments2(var name, var age)) -> System.out.println(name + " is " + age);
    case Validated.Invalid(var violations) -> System.out.println(violations);
}
```

## Errors

```java
Validated<User> validated = userValidator.validate(input, Locale.JAPANESE);
ConstraintViolations violations = validated.errors();
```

| Raoh | YAVI |
| --- | --- |
| `issues.flatten()` | `violations.messagesByPointer()` (`/items/1/name`), `violations.messagesByName()` (`items[1].name`) |
| `issues.toJsonList()` | `violations.details()`, a list of `ViolationDetail` (`name`, `pointer`, `messageKey`, `message`, `args`) |
| `issues.resolve(resolver, locale)` | `validate(input, locale)`, and `violation.message(messageFormatter)` for another source of messages |

A message of YAVI holds the name of the property (`"age" must be less than or equal to
150`), a message of Raoh does not (`must be between 0 and 150`). The locale is given when
validating; the message key and `args()` are kept in the violation, so a message is also
formatted later.

## Scenarios

### User registration

```java
record Password(String value) {}
record UserRegistration(Email email, Password password, Role role, List<Contact> contacts) {}

MapInputValidator<UserRegistration> userRegistrationValidator = Yavi.arguments()
        ._object("email", normalizedEmail.map(Email::new))
        ._string(Yavi._string("password", c -> c.notNull().greaterThanOrEqual(8).lessThanOrEqual(128)).map(Password::new))
        ._enum("role", Role.class).defaultValue(Role.MEMBER)
        ._list("contacts", contactValidator)
        .fromMap(UserRegistration::new);

@PostMapping("/users")
ResponseEntity<?> register(@RequestBody JsonNode body) {
    return switch (userRegistrationValidator.readingWith(JsonNodeReader.INSTANCE).validate(body)) {
        case Validated.Valid<UserRegistration>(UserRegistration registration) ->
            ResponseEntity.status(201).body(userService.register(registration));
        case Validated.Invalid<UserRegistration>(ConstraintViolations violations) ->
            ResponseEntity.badRequest().body(violations.details());
    };
}
```

The same validator reads a map and a `JsonNode`; nothing is imported differently. The
README shows `JsonNodeReader`.

### Converting a list to a map

```java
record Product(String id, BigDecimal price) {}

ValueValidator<Map<String, Object>, Map<String, BigDecimal>> priceMapValidator = Yavi.arguments()
        ._list("products", productValidator)
        .fromMap(products -> products)
        .constraintOnTarget(products -> products.stream().map(Product::id).distinct().count() == products.size(),
                "products", ViolationMessage.of("products.duplicateIds", "\"{0}\" must not contain duplicate ids"))
        .map(products -> products.stream().collect(Collectors.toMap(Product::id, Product::price)));
```

### Known fields and the remaining fields

`_rest` is an argument that collects the keys no other argument is defined for. Its values
are validated, with a violation located under the key, which the lambda of Raoh does not
do:

```java
record Item(String name, Map<String, String> attrs) {}

MapInputValidator<Item> itemValidator = Yavi.arguments()
        ._string("name", c -> c.notBlank())
        ._rest(Yavi._string(c -> c.notBlank()))
        .fromMap(Item::new);

itemValidator.validate(Map.of("name", "pen", "color", "red")); // Item[name=pen, attrs={color=red}]
itemValidator.validate(Map.of("name", "pen", "color", "")); // color: "color" must not be blank
```

`_rest()` without a validator takes the values as they are.

### Pagination and query parameters

The tutorial of Raoh decodes a map that already holds numbers. Query parameters are
strings, which `readingParameters()` reads with the same arguments:

```java
enum SortOrder { ASC, DESC }
record PageRequest(int page, int size, SortOrder order) {}

ValueValidator<Map<String, String>, PageRequest> pageRequestValidator = Yavi.arguments()
        ._integer("page", c -> c.greaterThanOrEqual(0)).defaultValue(0)
        ._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).defaultValue(20)
        ._enum("sort", SortOrder.class).defaultValue(SortOrder.ASC)
        .fromMap(PageRequest::new)
        .readingParameters();

pageRequestValidator.validate(Map.of("page", "2", "size", "50", "sort", "DESC")); // PageRequest[page=2, size=50, order=DESC]
pageRequestValidator.validate(Map.of()); // PageRequest[page=0, size=20, order=ASC]
pageRequestValidator.validate(Map.of("page", "-1")); // page: "page" must be greater than or equal to 0
```

`size=9999` and `sort=desc` are violations here. With `recover` in place of
`defaultValue`, they fall back like an absent parameter:

```java
ValueValidator<Map<String, String>, PageRequest> lenientPageRequestValidator = Yavi.arguments()
        ._integer("page", c -> c.greaterThanOrEqual(0)).recover(0)
        ._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
        ._enum("sort", SortOrder.class).recover(SortOrder.ASC)
        .fromMap(PageRequest::new)
        .readingParameters();

lenientPageRequestValidator.validate(Map.of("page", "2", "size", "50", "sort", "DESC")); // PageRequest[page=2, size=50, order=DESC]
lenientPageRequestValidator.validate(Map.of("page", "-1", "size", "9999", "sort", "desc")); // PageRequest[page=0, size=20, order=ASC]
```

### CSV import

The cells of a CSV file are strings too, so a row is read with `readingParameters()`, and
the rows with `liftList`:

```java
record MemberImport(String email, String name, int age) {}

ValueValidator<Map<String, String>, MemberImport> rowValidator = Yavi.arguments()
        ._string("email", c -> c.notBlank().email())
        ._string("name", c -> c.notBlank().lessThanOrEqual(100))
        ._integer("age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(150))
        .fromMap(MemberImport::new)
        .readingParameters();

Validated<List<MemberImport>> members = rowValidator.liftList().prefixed("rows").validate(rows);
// rows[1].email, rows[1].name, rows[1].age, rows[3].email, rows[3].age
```

A header that holds `.` or `[` is read as a path, and a cell is not trimmed
(a `String` argument is, with `c.trim()`).

### Configuration files

```java
record DbConfig(String host, int port, String database) {}
record CacheConfig(String host, int port, int ttlSeconds) {}
record AppConfig(DbConfig db, CacheConfig cache, String logLevel) {}

MapInputValidator<DbConfig> dbConfigValidator = Yavi.arguments()
        ._string("host", c -> c.notBlank())
        ._integer("port", c -> c.greaterThanOrEqual(1).lessThanOrEqual(65535)).defaultValue(5432)
        ._string("database", c -> c.notBlank())
        .fromMap(DbConfig::new);

MapInputValidator<CacheConfig> cacheConfigValidator = Yavi.arguments()
        ._string("host", c -> c.notBlank()).defaultValue("localhost")
        ._integer("port", c -> c.greaterThanOrEqual(1).lessThanOrEqual(65535)).defaultValue(6379)
        ._integer("ttlSeconds", c -> c.positive()).defaultValue(300)
        .fromMap(CacheConfig::new);

MapInputValidator<AppConfig> appConfigValidator = Yavi.arguments()
        ._object("db", dbConfigValidator)
        ._object("cache", cacheConfigValidator).defaultValue(new CacheConfig("localhost", 6379, 300))
        ._string("logLevel", c -> c.notBlank()).defaultValue("INFO")
        .fromMap(AppConfig::new);

appConfigValidator.validate(Map.of("db", Map.of("host", "db.example.com", "database", "myapp")));
// AppConfig[db=DbConfig[host=db.example.com, port=5432, database=myapp],
//           cache=CacheConfig[host=localhost, port=6379, ttlSeconds=300], logLevel=INFO]
```

## Differences

* **Input.** Raoh has one module of decoders per input (`MapDecoders`, `JsonDecoders`,
  `JooqRecordDecoders`). YAVI has one definition and an `InputReader` per input, and reads
  request parameters with paths and string coercion. It has no reader for a jOOQ `Record`;
  `record.intoMap()` is validated as a map.
* **Typed arguments.** The arguments given to `fromMap(f)` also build a validator of typed
  arguments with `apply(f)`. Raoh has no counterpart.
* **Required.** A Raoh decoder rejects `null` and an absent field; a YAVI argument does so
  only with `notNull()` or `notBlank()`.
* **Arity.** Both take up to 16 typed fields. Beyond that, YAVI groups the fields with
  `_embedded`, or has `MapInputValidator.of(parts, mapper)`, whose mapper takes a list of
  values.
* **Description.** A YAVI validator describes its input (`description()`), from which
  `JsonSchema` generates a JSON Schema.
* **Clock.** Raoh has no `past()` and `future()` on purpose. YAVI has them, with a `Clock`
  for a reproducible result.

## Not available

* Three states of a field (`Presence`: absent, `null`, value).
* A reference to a validator that is not built yet (`lazy` of Raoh), for a recursive
  structure read from a JSON tree or described as a JSON Schema.
* Enum constants read without regard to case, booleans read from `1`, `yes`, `on`, and a
  `scale` constraint of a `BigDecimal`.
* Encoders (a domain object to a map).
