# The Raoh tutorial in YAVI

This page is the [Raoh decoder practical guide](https://github.com/raoh-project/raoh-java/blob/main/docs/tutorial.md)
written with YAVI 0.18.0, section by section and with the same numbers, so that the two
can be read side by side. Every example was run with jshell; the comments show what it
printed, and `python3 docs/verify-examples.py` runs them again and compares.
[Comparison with Raoh](ComparisonWithRaoh.md) maps the two APIs in tables.

* [0. Setup](#0-setup)
* [1. Validating primitive values](#1-validating-primitive-values)
* [2. Adding constraints](#2-adding-constraints)
* [3. Mapping to domain primitives](#3-mapping-to-domain-primitives)
* [4. Validating objects](#4-validating-objects)
* [5. Nested objects](#5-nested-objects)
* [6. Structuring flat data](#6-structuring-flat-data)
* [7. Lists](#7-lists)
* [8. Maps](#8-maps)
* [9. Optional, nullable and three-state properties](#9-optional-nullable-and-three-state-properties)
* [10. Enums](#10-enums)
* [11. Union types](#11-union-types)
* [12. Cross-field validation](#12-cross-field-validation)
* [13. Conditional validation](#13-conditional-validation)
* [14. Combining results of different sources](#14-combining-results-of-different-sources)
* [15. Validating the rows of a list](#15-validating-the-rows-of-a-list)
* [16. Default values and recovery](#16-default-values-and-recovery)
* [17. Rejecting unknown properties](#17-rejecting-unknown-properties)
* [18. Recursive structures](#18-recursive-structures)
* [19. Handling violations](#19-handling-violations)
* [20. Scenario: a user registration API](#20-scenario-a-user-registration-api)
* [21. Converting a list to a map](#21-converting-a-list-to-a-map)
* [22. Known properties and the remaining keys](#22-known-properties-and-the-remaining-keys)
* [23. Password confirmation](#23-password-confirmation)
* [24. Pagination and query parameters](#24-pagination-and-query-parameters)
* [25. Amount and currency](#25-amount-and-currency)
* [26. CSV import](#26-csv-import)
* [27. Configuration files](#27-configuration-files)
* [28. Tuples](#28-tuples)
* [29. Writing an object back as a map](#29-writing-an-object-back-as-a-map)
* [30. Writing null and default values](#30-writing-null-and-default-values)
* [31. Writing nested objects](#31-writing-nested-objects)
* [32. Writing a tagged union](#32-writing-a-tagged-union)

## Why validators that produce a value?

Data from outside (a request body, a CSV file, the response of another API, a row of a
table) is not of the types the application expects. A validator of YAVI takes such an
input and returns either the domain object or all the violations with their paths:

```text
input (typed arguments / Map / JSON tree / request parameters)
    ↓  validator.validate(input)
Valid[domain object]  or  Invalid[violations with a name, a path, a message key and a message]
```

What Raoh calls a decoder is a `ValueValidator<I, T>` here, `decode` is `validate`, and
`Ok` / `Err` are `Validated.Valid` / `Validated.Invalid`.

## 0. Setup

Start jshell with the jar of YAVI, which needs no other library:

```bash
jshell --class-path yavi-0.18.0.jar
```

```java
import am.ik.yavi.arguments.*;
import am.ik.yavi.core.*;
import am.ik.yavi.input.*;
import am.ik.yavi.message.*;
import am.ik.yavi.validator.Yavi;
import java.time.*;
```

* jshell evaluates a snippet when a line completes it. A chain that is broken before a
  `.`, as the examples are, is pasted as one line.
* jshell wants a sealed interface and its subtypes in one snippet, so the examples
  declare a plain `interface`.

## 1. Validating primitive values

The smallest unit is the validator of one value: `Yavi._string()`, `Yavi._integer()`,
`Yavi._bigDecimal()`, `Yavi._boolean()` and so on.

```java
Yavi._string().validate("hello")
// ==> Valid[value=hello]

Yavi._integer().validate(42)
// ==> Valid[value=42]

Yavi._boolean().validate(true)
// ==> Valid[value=true]

Yavi._bigDecimal().validate(new BigDecimal("19.99"))
// ==> Valid[value=19.99]

Yavi._double().validate(3.14)
// ==> Valid[value=3.14]

Yavi._float().validate(2.5f)
// ==> Valid[value=2.5]

Yavi._object(byte[].class).validate(new byte[]{1, 2, 3})
// ==> Valid[value=[B@...]
```

Unlike a decoder of `ObjectDecoders`, these validators are typed:
`Yavi._integer().validate("not a number")` does not compile. `untyped()` returns the
validator of an `Object`, which checks the type first:

```java
Yavi._integer().untyped().validate("not a number")
// ==> Invalid[errors=[The value must be of type integer (object.type)]]

Yavi._integer().untyped().validate(42L)
// ==> Valid[value=42]  (a number is taken as long as its value is kept)

Yavi._bigDecimal().untyped().validate(19.99)
// ==> Valid[value=19.99]
```

A failed result shows each violation as it reads: the JSON Pointer of the value (none
for the validated value itself), the message and the message key. `untyped()` is rarely
written: a schema ([section 4](#4-validating-objects)) checks the types of its
properties.

## 2. Adding constraints

The constraints are a function given to the validator. A violated constraint fails the
validation.

### String constraints

```java
Yavi._string(c -> c.trim().toLowerCase().email()).validate("  USER@Example.COM  ")
// ==> Valid[value=user@example.com]

Yavi._string(c -> c.trim().toLowerCase().email()).validate("not-email")
// ==> Invalid[errors=[The value must be a valid email address (charSequence.email)]]

Yavi._string(c -> c.greaterThanOrEqual(3).lessThanOrEqual(20)).validate("ab")
// ==> Invalid[errors=[The size of the value must be greater than or equal to 3. The given size is 2 (container.greaterThanOrEqual)]]

Yavi._string(c -> c.pattern("^\\d{3}-\\d{4}$")).validate("123-4567")
// ==> Valid[value=123-4567]

Yavi._string(c -> c.uuid()).validate("550e8400-e29b-41d4-a716-446655440000")
// ==> Valid[value=550e8400-e29b-41d4-a716-446655440000]  (a String)

Yavi._object(UUID.class).untyped().validate("550e8400-e29b-41d4-a716-446655440000")
// ==> Valid[value=550e8400-e29b-41d4-a716-446655440000]  (a UUID)
```

* The transforms (`trim()`, `toLowerCase()`) are written before the first constraint;
  `c.email().trim()` does not compile.
* The length of a string is its size: `greaterThanOrEqual(3)` is `minLength(3)`,
  `lessThanOrEqual(20)` is `maxLength(20)` and `fixedSize(n)` is `fixedLength(n)`.
* A constraint keeps the type of the value. A value of another type is read by the
  validator of that type: `Yavi._object(UUID.class)` reads the canonical string.

### Normalization

The size constraints count the code points of the NFC form, so a decomposed が (U+304B
U+3099) is one character without a transform. The value is not changed;
`toNormalForm(form)` normalizes it.

```java
String nfd = "が";

Yavi._string(c -> c.lessThanOrEqual(1)).validate(nfd)
// ==> Valid[value=が]  (still two chars)

Yavi._string(c -> c.toNormalForm(java.text.Normalizer.Form.NFC).lessThanOrEqual(1)).validate(nfd).value().length()
// ==> 1

Yavi._string(c -> c.normalizer(null).lessThanOrEqual(1)).validate(nfd)
// ==> Invalid[errors=[The size of the value must be less than or equal to 1. The given size is 2 (container.lessThanOrEqual)]]
```

`c.normalizer(form)` selects the form for counting and `c.normalizer(null)` none.

### URI / URL validation

```java
Yavi._string(c -> c.uri()).validate("file:///tmp/data.csv")
// ==> Valid[value=file:///tmp/data.csv]

Yavi._string(c -> c.uri()).validate("foo/bar")
// ==> Invalid[errors=[The value must be a valid URI (charSequence.uri)]]

Yavi._string(c -> c.url()).validate("https://example.com/api")
// ==> Valid[value=https://example.com/api]

Yavi._string(c -> c.url()).validate("ftp://example.com")
// ==> Valid[value=ftp://example.com]

Yavi._string(c -> c.url(u -> u.protocol("http", "https").build())).validate("ftp://example.com")
// ==> Invalid[errors=[The protocol of the value must be one of the following values: [http, https] (charSequence.url.protocol)]]

Yavi._object(URI.class).untyped().validate("https://example.com/api")
// ==> Valid[value=https://example.com/api]  (a URI)
```

`c.uri()` needs a scheme, as `uri()` of Raoh does. `c.url()` takes every protocol that
`java.net.URL` handles unless the protocols are given. Both keep the `String`;
`Yavi._object(URI.class)` produces a `URI` and takes any URI reference, also a relative
one.

### Numeric constraints

```java
Yavi._integer(c -> c.greaterThanOrEqual(0).lessThanOrEqual(150)).validate(25)
// ==> Valid[value=25]

Yavi._integer(c -> c.greaterThanOrEqual(0).lessThanOrEqual(150)).validate(300)
// ==> Invalid[errors=[The value must be less than or equal to 150 (numeric.lessThanOrEqual)]]

Yavi._integer(c -> c.positive()).validate(-1)
// ==> Invalid[errors=[The value must be positive (numeric.positive)]]

Yavi._bigDecimal(c -> c.fractionDigits(4)).validate(new BigDecimal("0.1234"))
// ==> Valid[value=0.1234]
```

### Temporal constraints

A temporal validator reads an ISO-8601 string, so there is no `string().date()`.

```java
Yavi._localDate().untyped().validate("2025-06-15")
// ==> Valid[value=2025-06-15]

Yavi._instant().untyped().validate("2025-06-15T10:30:00Z")
// ==> Valid[value=2025-06-15T10:30:00Z]

Yavi._localDate(c -> c.after(() -> LocalDate.of(2020, 1, 1))).validate(LocalDate.of(2025, 6, 15))
// ==> Valid[value=2025-06-15]

Yavi._localDate(c -> c.before(() -> LocalDate.of(2030, 1, 1))).validate(LocalDate.of(2035, 1, 1))
// ==> Invalid[errors=[The value has to be before 2030-01-01 (temporal.before)]]

Yavi._localDate(c -> c.between(() -> LocalDate.of(2020, 1, 1), () -> LocalDate.of(2030, 12, 31))).validate(LocalDate.of(2025, 6, 15))
// ==> Valid[value=2025-06-15]
```

A bound is a supplier. Raoh has no `past()` and `future()` on purpose; YAVI has them and
takes a `Clock`, so the result is reproducible:

```java
var now = Instant.parse("2025-06-15T12:00:00Z");
Yavi._instant(c -> c.before(() -> now)).validate(Instant.parse("2025-06-15T11:00:00Z"))
// ==> Valid[value=2025-06-15T11:00:00Z]

var clock = Clock.fixed(now, ZoneOffset.UTC);
Yavi._instant(c -> c.past(clock)).validate(Instant.parse("2025-06-15T11:00:00Z"))
// ==> Valid[value=2025-06-15T11:00:00Z]
```

### String-to-type conversions (coerce)

In Raoh the conversion is a part of the decoder (`string().toInt().range(0, 150)`). In
YAVI it belongs to the input: the validator of an `Integer` stays the same, and the
reader takes a string as text that stands for the type.

```java
InputReader<Object> text = InputReader.ofJdkTypes().coercingStrings();

Yavi._integer().readingWith(text).validate("42")
// ==> Valid[value=42]

Yavi._integer(c -> c.greaterThanOrEqual(0).lessThanOrEqual(150)).readingWith(text).validate("25")
// ==> Valid[value=25]

Yavi._integer().readingWith(text).validate("abc")
// ==> Invalid[errors=[The value must be of type integer (object.type)]]

Yavi._long().readingWith(text).validate("9999999999")
// ==> Valid[value=9999999999]

Yavi._bigDecimal(c -> c.fractionDigits(2)).readingWith(text).validate("19.99")
// ==> Valid[value=19.99]

Yavi._bigDecimal().readingWith(text).validate("abc")
// ==> Invalid[errors=[The value must be of type number (object.type)]]
```

The rules are strict unless the reader is given others: a boolean is `true` or `false`,
and a number has no surrounding white space. `StringCoercion` relaxes them for text
written by a person:

```java
Yavi._boolean().readingWith(text).validate("true")
// ==> Valid[value=true]

Yavi._boolean().readingWith(text).validate("1")
// ==> Invalid[errors=[The value must be of type boolean (object.type)]]

InputReader<Object> form = InputReader.ofJdkTypes().coercingStrings(StringCoercion.strict().trimming().booleanWords());

Yavi._boolean().readingWith(form).validate("1")
// ==> Valid[value=true]

Yavi._boolean().readingWith(form).validate("no")
// ==> Valid[value=false]

Yavi._boolean().readingWith(form).validate("maybe")
// ==> Invalid[errors=[The value must be of type boolean (object.type)]]

// isTrue() enforces an agreement (e.g. terms of service)
Yavi._boolean(c -> c.isTrue()).readingWith(form).validate("false")
// ==> Invalid[errors=[The value must be true (boolean.isTrue)]]

Yavi._integer().readingWith(form).validate("  42  ")
// ==> Valid[value=42]
```

`booleanWords()` takes `true`/`1`/`yes`/`on` and `false`/`0`/`no`/`off` in any case, and
`trimming()` removes the white space around every text. A whole map of strings is read
with `readingParameters()` ([section 24](#24-pagination-and-query-parameters)).

## 3. Mapping to domain primitives

`map` converts a validated value into a domain type, so that only validated values of
domain types flow inside the application.

```java
record Email(String value) {}
record Age(int value) {}
record UserId(UUID value) {}

var emailValidator = Yavi._string(c -> c.trim().toLowerCase().notBlank().email()).map(Email::new);
var ageValidator = Yavi._integer(c -> c.greaterThanOrEqual(0).lessThanOrEqual(150)).map(Age::new);
var userIdValidator = Yavi._object(UUID.class).map(UserId::new);

emailValidator.validate("  ALICE@example.com  ")
// ==> Valid[value=Email[value=alice@example.com]]

ageValidator.validate(25)
// ==> Valid[value=Age[value=25]]

userIdValidator.untyped().validate("550e8400-e29b-41d4-a716-446655440000")
// ==> Valid[value=UserId[value=550e8400-e29b-41d4-a716-446655440000]]
```

`email()` accepts an empty string, as every format does, so `notBlank()` says that the
value is given.

## 4. Validating objects

`Yavi.schema()` names the properties of an object, and `fromMap` ends them with a
validator that reads a `Map<String, Object>` by these names, checks the types and calls
the constructor. `combine(field(...), ...).map(f)` of Raoh is this chain. The violations
of all properties are accumulated.

```java
record User(UserId id, Email email, Age age) {}

var userValidator = Yavi.schema()
        ._object("id", userIdValidator)
        ._object("email", emailValidator)
        ._object("age", ageValidator)
        .fromMap(User::new);

userValidator.validate(Map.of(
        "id", "550e8400-e29b-41d4-a716-446655440000",
        "email", "alice@example.com",
        "age", 30))
// ==> Valid[value=User[id=UserId[value=550e8400-e29b-41d4-a716-446655440000], email=Email[value=alice@example.com], age=Age[value=30]]]

userValidator.validate(Map.of("id", "not-uuid", "email", "invalid", "age", 300))
// ==> Invalid[errors=[/id: "id" must be of type UUID (object.type), /email: "email" must be a valid email address (charSequence.email), /age: "age" must be less than or equal to 150 (numeric.lessThanOrEqual)]]
```

`_object(name, validator)` uses a validator under a name. A property is also written in
place (`._string("email", c -> c.notBlank().email())`), and `map` after it creates the
domain type, as `field("email", string().email().map(Email::new))` of Raoh does:

```java
Yavi.schema()
        ._object("id", UUID.class).map(UserId::new)
        ._string("email", c -> c.trim().toLowerCase().notBlank().email()).map(Email::new)
        ._integer("age", c -> c.greaterThanOrEqual(0).lessThanOrEqual(150)).map(Age::new)
        .fromMap(User::new);
```

`map` is one of the modifiers of a property, written before those that decide what
`null` produces: `._string("nickname").map(Nickname::new).optional()` is an
`Optional<Nickname>`.

The schema is not bound to a map. The same properties end with `apply(User::new)` for
typed arguments, and the validator of `fromMap` reads a JSON tree with `readingWith` and
request parameters with `readingParameters`. Raoh chooses the input first, with the
static import of `MapDecoders` or `JsonDecoders`.

## 5. Nested objects

`nested(decoder)` is `_object(name, validator)`. The path of a violation is composed.

```java
record Address(String city, String zip) {}
record Customer(String name, Address address) {}

var addressValidator = Yavi.schema()
        ._string("city", c -> c.notBlank())
        ._string("zip", c -> c.pattern("^\\d{3}-\\d{4}$"))
        .fromMap(Address::new);

var customerValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._object("address", addressValidator)
        .fromMap(Customer::new);

customerValidator.validate(Map.of(
        "name", "Alice",
        "address", Map.of("city", "Tokyo", "zip", "100-0001")))
// ==> Valid[value=Customer[name=Alice, address=Address[city=Tokyo, zip=100-0001]]]

customerValidator.validate(Map.of(
        "name", "",
        "address", Map.of("city", "", "zip", "bad")))
// ==> Invalid[errors=[/name: "name" must not be blank (charSequence.notBlank), /address/city: "address.city" must not be blank (charSequence.notBlank), /address/zip: "address.zip" must match ^\d{3}-\d{4}$ (charSequence.pattern)]]
```

A violation has a pointer (`/address/city`) and a name (`address.city`), which its
message shows.

## 6. Structuring flat data

The result of a JOIN and a CSV file are flat. `_embedded` is `flat`: the embedded
validator reads the same map as the properties.

```java
record UserName(String first, String last) {}
record Department(String name, String code) {}
record Employee(UserName name, Department dept) {}

var userNameValidator = Yavi.schema()
        ._string("first_name", c -> c.notBlank())
        ._string("last_name", c -> c.notBlank())
        .fromMap(UserName::new);

var departmentValidator = Yavi.schema()
        ._string("dept_name", c -> c.notBlank())
        ._string("dept_code", c -> c.notBlank())
        .fromMap(Department::new);

var employeeValidator = Yavi.schema()
        ._embedded(userNameValidator)
        ._embedded(departmentValidator)
        .fromMap(Employee::new);

employeeValidator.validate(Map.of(
        "first_name", "Alice",
        "last_name", "Smith",
        "dept_name", "Engineering",
        "dept_code", "ENG"))
// ==> Valid[value=Employee[name=UserName[first=Alice, last=Smith], dept=Department[name=Engineering, code=ENG]]]
```

The properties of an embedded validator are known to the validator that embeds it, so it
can be strict ([section 17](#17-rejecting-unknown-properties)), which a combiner with
`flat` cannot. A name that two embedded validators define is rejected when the validator
is built; rename the columns with SQL aliases.

With a LEFT JOIN all columns of the joined side are `null` when there is no row. An
embedded validator that accepts `null` is given `null` when none of its properties has a
value, so the department is an `Optional`, not the `Presence` of one column:

```java
record EmployeeWithDept(UserName name, Optional<Department> dept) {}

var employeeWithDeptValidator = Yavi.schema()
        ._embedded(userNameValidator)
        ._embedded(departmentValidator).optional()
        .fromMap(EmployeeWithDept::new);

var row = new HashMap<String, Object>();
row.put("first_name", "Bob");
row.put("last_name", "Jones");
row.put("dept_name", null);
row.put("dept_code", null);

employeeWithDeptValidator.validate(row)
// ==> Valid[value=EmployeeWithDept[name=UserName[first=Bob, last=Jones], dept=Optional.empty]]

row.put("dept_name", "Engineering");
employeeWithDeptValidator.validate(row)
// ==> Invalid[errors=[/dept_code: "dept_code" must not be blank (charSequence.notBlank)]]
```

### One-to-many JOIN

Each row is validated with the parent and the child embedded, and the rows are grouped
by the parent afterwards. `liftList()` validates every row and `prefixed` locates the
violations ([section 15](#15-validating-the-rows-of-a-list)).

```java
record OrderHeader(String orderId, String customerName) {}
record OrderLine(String productId, int quantity, BigDecimal unitPrice) {}
record OrderWithLines(OrderHeader header, List<OrderLine> lines) {}

var headerValidator = Yavi.schema()
        ._string("order_id", c -> c.notBlank())
        ._string("customer_name", c -> c.notBlank())
        .fromMap(OrderHeader::new);

var lineValidator = Yavi.schema()
        ._string("product_id", c -> c.notBlank())
        ._integer("qty", c -> c.positive())
        ._bigDecimal("unit_price", c -> c.positive())
        .fromMap(OrderLine::new);

var rowValidator = Yavi.schema()
        ._embedded(headerValidator)
        ._embedded(lineValidator)
        .fromMap((header, line) -> Map.entry(header, line));

// SELECT o.order_id, o.customer_name, l.product_id, l.qty, l.unit_price
// FROM orders o JOIN order_lines l ON o.order_id = l.order_id
var joinRows = List.<Map<String, Object>>of(
        Map.of("order_id", "ORD-001", "customer_name", "Alice", "product_id", "A01", "qty", 2, "unit_price", 1500),
        Map.of("order_id", "ORD-001", "customer_name", "Alice", "product_id", "B02", "qty", 1, "unit_price", 3000),
        Map.of("order_id", "ORD-002", "customer_name", "Bob", "product_id", "A01", "qty", 5, "unit_price", 1500));

var orders = rowValidator.liftList().prefixed("rows").validate(joinRows).map(entries ->
        entries.stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        LinkedHashMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())))
                .entrySet().stream()
                .map(e -> new OrderWithLines(e.getKey(), e.getValue()))
                .toList());
orders
// ==> Valid[value=[OrderWithLines[header=OrderHeader[orderId=ORD-001, customerName=Alice], lines=[OrderLine[productId=A01, quantity=2, unitPrice=1500], OrderLine[productId=B02, quantity=1, unitPrice=3000]]], OrderWithLines[header=OrderHeader[orderId=ORD-002, customerName=Bob], lines=[OrderLine[productId=A01, quantity=5, unitPrice=1500]]]]]
```

## 7. Lists

`_list(name, elementValidator)` validates every element and locates a violation under
its index. The constraints of the list itself are the third argument.

```java
record OrderItem(String productId, int quantity) {}

var orderItemValidator = Yavi.schema()
        ._string("productId", c -> c.notBlank())
        ._integer("quantity", c -> c.positive())
        .fromMap(OrderItem::new);

var orderItemsValidator = Yavi.schema()
        ._list("items", orderItemValidator, c -> c.notEmpty())
        .fromMap();

orderItemsValidator.validate(Map.of("items", List.of(
        Map.of("productId", "A001", "quantity", 3),
        Map.of("productId", "", "quantity", -1))))
// ==> Invalid[errors=[/items/1/productId: "items[1].productId" must not be blank (charSequence.notBlank), /items/1/quantity: "items[1].quantity" must be positive (numeric.positive)]]
```

A schema of one property ends with `fromMap()` without a function: the value of the
property is the result. It takes the place of a single `field(...)` of Raoh.

```java
Yavi.schema()._list("tags", Yavi._string(), c -> c.greaterThanOrEqual(1).lessThanOrEqual(10)).fromMap()
        .validate(Map.of("tags", List.of("a", "b")))
// ==> Valid[value=[a, b]]

Yavi.schema()._list("tags", Yavi._string(), c -> c.unique()).fromMap()
        .validate(Map.of("tags", List.of("a", "b", "a")))
// ==> Invalid[errors=[/tags: "tags" must be unique. [a] is/are duplicated. (collection.unique)]]

Yavi.schema()._list("codes", Yavi._string(), c -> c.fixedSize(3)).fromMap()
        .validate(Map.of("codes", List.of("X", "Y")))
// ==> Invalid[errors=[/codes: The size of "codes" must be 3. The given size is 2 (container.fixedSize)]]
```

A custom message follows its constraint:

```java
Yavi.schema()._list("tags", Yavi._string(), c -> c.greaterThanOrEqual(1).message("select at least one tag")).fromMap()
        .validate(Map.of("tags", List.of()))
// ==> Invalid[errors=[/tags: select at least one tag (container.greaterThanOrEqual)]]
```

The constraints of a list are checked when every element is valid. A list that is not a
property is `Yavi._list(elementValidator, c -> ...)`.

## 8. Maps

`_map(name, valueValidator)` validates the values of a map with dynamic keys.

```java
var pricesValidator = Yavi.schema()
        ._map("prices", Yavi._bigDecimal(c -> c.positive()), c -> c.notEmpty())
        .fromMap();

pricesValidator.validate(Map.of("prices", Map.of("apple", 120, "banana", 80)))
// ==> Valid[value={banana=80, apple=120}]

pricesValidator.validate(Map.of("prices", Map.of("apple", -1)))
// ==> Invalid[errors=[/prices/apple: "prices.apple" must be positive (numeric.positive)]]
```

### JSON stored in a string

`pipe` is `andThen`. The parser is a validator that reports its failure as a violation.
This one uses Jackson, which is added to the class path of jshell.

```java
var mapper = new tools.jackson.databind.ObjectMapper();

ValueValidator<String, Map<String, Object>> parseJson = (s, locale, context) -> {
    try {
        @SuppressWarnings("unchecked")
        Map<String, Object> parsed = (Map<String, Object>) mapper.readValue(s, Map.class);
        return Validated.successWith(parsed);
    }
    catch (Exception e) {
        return Validated.failureWith(ViolationMessage.of("json.invalid", "\"{0}\" cannot be parsed as JSON"), s, locale);
    }
};

record Params(String kind, int count) {}

var paramsValidator = Yavi.schema()
        ._string("kind")
        ._integer("count")
        .fromMap(Params::new);

// "params" is a string column holding JSON
var validator = Yavi.schema()
        ._object("params", Yavi._string().andThen(parseJson).andThen(paramsValidator))
        .fromMap();

validator.validate(Map.of("params", "{\"kind\":\"like\",\"count\":3}"))
// ==> Valid[value=Params[kind=like, count=3]]

validator.validate(Map.of("params", "not json"))
// ==> Invalid[errors=[/params: "params" cannot be parsed as JSON (json.invalid)]]

validator.validate(Map.of("params", "{\"kind\":\"like\"}"))
// ==> Invalid[errors=[/params/count: "params.count" is required (object.required)]]
```

A hand-written validator is a lambda of the input, the locale and a context. A violation
is created from a message key and a message format, where `{0}` is the name that the
violation is reported under.

## 9. Optional, nullable and three-state properties

A property is required and rejects `null` unless a modifier after it says otherwise.

```java
// required, null not allowed
Yavi.schema()._string("email", c -> c.email()).fromMap().validate(Map.of("email", "a@b.com"))
// ==> Valid[value=a@b.com]

// null allowed: the value is a @Nullable String
Yavi.schema()._string("nickname").nullable().fromMap().validate(Collections.singletonMap("nickname", null))
// ==> Valid[value=null]

// optional: the value is an Optional<String>
Yavi.schema()._string("middleName").optional().fromMap().validate(Map.of())
// ==> Valid[value=Optional.empty]
```

`nullable()`, `optional()` and `defaultValue(...)` accept both an absent key and `null`.
Raoh keeps the two apart: `field(name, nullable(dec))` requires the key and
`optionalField(name, dec)` rejects `null`. The last modifier of a property says the same:

```java
Yavi.schema()._string("nickname").nullable().required().fromMap()
        .validate(Map.of())
// ==> Invalid[errors=[/nickname: "nickname" is required (object.required)]]

Yavi.schema()._string("middleName").optional().rejectingNull().fromMap()
        .validate(Collections.singletonMap("middleName", null))
// ==> Invalid[errors=[/middleName: "middleName" must not be null (object.notNull)]]
```

`presence()` reads a property as a `Presence`, which tells the three states apart.

```java
var bioValidator = Yavi.schema()._string("bio").nullable().presence().fromMap();

bioValidator.validate(Map.of("bio", "hello"))
// ==> Valid[value=Present[value=hello]]

var m = new HashMap<String, Object>(); m.put("bio", null);
bioValidator.validate(m)
// ==> Valid[value=PresentNull[]]

bioValidator.validate(Map.of())
// ==> Valid[value=Absent[]]
```

`presence()` adds only the absent state. Whether `null` is accepted is said by the
property, so a property may be omitted but not cleared without `nullable()`. This suits
a PATCH request:

```java
record ProfilePatch(Presence<String> nickname, Presence<String> bio) {}

var profilePatchValidator = Yavi.schema()
        ._string("nickname", c -> c.lessThanOrEqual(50)).presence()        // may be omitted, cannot be cleared
        ._string("bio", c -> c.lessThanOrEqual(500)).nullable().presence() // may be omitted or cleared
        .fromMap(ProfilePatch::new);

profilePatchValidator.validate(Map.of("nickname", "alice"))
// ==> Valid[value=ProfilePatch[nickname=Present[value=alice], bio=Absent[]]]

profilePatchValidator.validate(Collections.singletonMap("nickname", null))
// ==> Invalid[errors=[/nickname: "nickname" must not be null (object.notNull)]]
```

## 10. Enums

An enum constant is read from its name. The name is matched exactly; `ignoringCase()` of
the validator of the enum takes any case, as `enumOf` does.

```java
enum Role { ADMIN, MEMBER, GUEST }

Yavi.schema()._enum("role", Role.class).fromMap().validate(Map.of("role", "ADMIN"))
// ==> Valid[value=ADMIN]

Yavi.schema()._enum("role", Role.class).fromMap().validate(Map.of("role", "admin"))
// ==> Invalid[errors=[/role: "role" must be one of the following values: [ADMIN, MEMBER, GUEST] (object.oneOf)]]

Yavi.schema()._enum(Yavi._enum("role", Role.class).ignoringCase()).fromMap().validate(Map.of("role", "admin"))
// ==> Valid[value=ADMIN]

Yavi.schema()._enum("role", Role.class).defaultValue(Role.MEMBER).fromMap().validate(Map.of())
// ==> Valid[value=MEMBER]
```

## 11. Union types

`oneOf(a, b)` is `ValueValidator.anyOf(a, b)`: the first alternative that is valid.
`literal("email")` is the constraint `equalTo`.

```java
interface Contact {}
record EmailContact(String address) implements Contact {}
record PhoneContact(String number) implements Contact {}

ValueValidator<Map<String, Object>, Contact> contactValidator = ValueValidator.anyOf(
        Yavi.schema()
                ._string("kind", c -> c.equalTo("email"))
                ._string("value", c -> c.email())
                .fromMap((kind, value) -> new EmailContact(value)),
        Yavi.schema()
                ._string("kind", c -> c.equalTo("phone"))
                ._string("value", c -> c.pattern("^\\d{10,15}$"))
                .fromMap((kind, value) -> new PhoneContact(value)));

contactValidator.validate(Map.of("kind", "email", "value", "a@b.com"))
// ==> Valid[value=EmailContact[address=a@b.com]]

contactValidator.validate(Map.of("kind", "phone", "value", "09012345678"))
// ==> Valid[value=PhoneContact[number=09012345678]]

contactValidator.validate(Map.of("kind", "fax", "value", "123"))
// ==> Invalid[errors=[The value must satisfy at least one of the 2 alternatives (object.anyOf) alternatives=[[/kind: "kind" must be equal to email (object.equalTo), /value: "value" must be a valid email address (charSequence.email)], [/kind: "kind" must be equal to phone (object.equalTo), /value: "value" must match ^\d{10,15}$ (charSequence.pattern)]]]]
```

No cast to the supertype is needed, but the type of the variable is: `var` infers
`Record & Contact`. The one violation holds the violations of each alternative:

```java
if (contactValidator.validate(Map.of("kind", "fax", "value", "123")).errors().get(0) instanceof ConstraintViolation.NoAlternativeMatched unmatched) {
    unmatched.alternatives().forEach(violations -> System.out.println(violations.messagesByPointer()));
}
// {/kind=["kind" must be equal to email], /value=["value" must be a valid email address]}
// {/kind=["kind" must be equal to phone], /value=["value" must match ^\d{10,15}$]}
```

### A discriminated union

`discriminate("type", variant(...), ...)` is `DiscriminatedUnionValidator`. It reads the
discriminator first and validates only the selected alternative, which does not define
the discriminator.

```java
interface Shape {}
record Circle(double radius) implements Shape {}
record Rect(double width, double height) implements Shape {}

var shapeValidator = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Yavi.schema()._double("radius", c -> c.positive()).fromMap(Circle::new))
        .when("rect", Yavi.schema()
                ._double("width", c -> c.positive())
                ._double("height", c -> c.positive())
                .fromMap(Rect::new));

shapeValidator.validate(Map.of("type", "circle", "radius", 5.0))
// ==> Valid[value=Circle[radius=5.0]]

shapeValidator.validate(Map.of("type", "rect", "width", 3.0, "height", 4.0))
// ==> Valid[value=Rect[width=3.0, height=4.0]]

shapeValidator.validate(Map.of("type", "hexagon"))
// ==> Invalid[errors=[/type: "type" must be one of the following values: [circle, rect] (object.oneOf)]]
```

A schema of one property is a validator like any other, so no `asDecoder()` is needed.
Two alternatives of one tag fail with `IllegalArgumentException` when the validator is
built.

## 12. Cross-field validation

`flatMap` is `flatFromMap`: the function receives the validated properties and returns a
`Validated`, so a factory method that checks a rule of the domain is given as it is. It
is called only when all properties are valid.

```java
record DateRange(LocalDate start, LocalDate end) {
    static Validated<DateRange> parse(LocalDate start, LocalDate end) {
        if (!start.isBefore(end)) {
            return Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"));
        }
        return Validated.successWith(new DateRange(start, end));
    }
}

var dateRangeValidator = Yavi.schema()
        ._localDate("start")
        ._localDate("end")
        .flatFromMap(DateRange::parse);

dateRangeValidator.validate(Map.of("start", "2025-01-01", "end", "2025-12-31"))
// ==> Valid[value=DateRange[start=2025-01-01, end=2025-12-31]]

dateRangeValidator.validate(Map.of("start", "2025-12-31", "end", "2025-01-01"))
// ==> Invalid[errors=[start must be before end (dateRange.invalid)]]

Yavi.schema()._object("period", dateRangeValidator).fromMap()
        .validate(Map.of("period", Map.of("start", "2025-12-31", "end", "2025-01-01")))
// ==> Invalid[errors=[/period: start must be before end (dateRange.invalid)]]
```

The violation is reported on the object itself, and under `period` where the validator
is nested. `failureWith(message, args...)` is `Result.fail(code, message)`: the factory
method names neither a violated value nor a locale, which it is not given. The validator
reports the violation with the locale of the validation, like the violations of the
properties.

```java
dateRangeValidator.validate(Map.of("start", "2025-12-31", "end", "2025-01-01"), Locale.JAPANESE)
        .errors().get(0).locale()
// ==> ja
```

A rule that is a predicate on the created object needs no factory method:
`constraintOnTarget` takes the predicate, the name that locates the violation (the empty
name is the object itself) and the message.

```java
var dateRangeValidator2 = Yavi.schema()
        ._localDate("start")
        ._localDate("end")
        .fromMap(DateRange::new)
        .constraintOnTarget(range -> range.start().isBefore(range.end()), "end",
                ViolationMessage.of("dateRange.invalid", "\"{0}\" must be after \"start\""));

dateRangeValidator2.validate(Map.of("start", "2025-12-31", "end", "2025-01-01"))
// ==> Invalid[errors=[/end: "end" must be after "start" (dateRange.invalid)]]
```

## 13. Conditional validation

Validating the remaining properties by the value of one property is a discriminated
union. The lambda of the Raoh tutorial, which reads `method` and switches, is not
needed:

```java
interface Payment {}
record CreditCard(String number, String expiry) implements Payment {}
record BankTransfer(String bankCode, String accountNumber) implements Payment {}

var paymentValidator = DiscriminatedUnionValidator.<Payment>of("method")
        .when("credit_card", Yavi.schema()
                ._string("number", c -> c.notBlank())
                ._string("expiry", c -> c.pattern("^\\d{2}/\\d{2}$"))
                .fromMap(CreditCard::new))
        .when("bank_transfer", Yavi.schema()
                ._string("bankCode", c -> c.notBlank())
                ._string("accountNumber", c -> c.notBlank())
                .fromMap(BankTransfer::new));

paymentValidator.validate(Map.of("method", "credit_card", "number", "4111111111111111", "expiry", "12/26"))
// ==> Valid[value=CreditCard[number=4111111111111111, expiry=12/26]]

paymentValidator.validate(Map.of("method", "bank_transfer", "bankCode", "0001", "accountNumber", "1234567"))
// ==> Valid[value=BankTransfer[bankCode=0001, accountNumber=1234567]]

paymentValidator.validate(Map.of("method", "cash"))
// ==> Invalid[errors=[/method: "method" must be one of the following values: [credit_card, bank_transfer] (object.oneOf)]]
```

## 14. Combining results of different sources

A schema combines the properties of one input. Two results of different inputs are
combined with `combine`, which is `Result.map2`:

```java
record PersonalName(String first, String last) {}
record ContactInfo(String email, String phone) {}
record CustomerProfile(PersonalName name, ContactInfo contact) {}

var nameValidator = Yavi.schema()
        ._string("first", c -> c.notBlank())
        ._string("last", c -> c.notBlank())
        .fromMap(PersonalName::new);

var contactInfoValidator = Yavi.schema()
        ._string("email", c -> c.email())
        ._string("phone", c -> c.notBlank())
        .fromMap(ContactInfo::new);

var nameResult = nameValidator.validate(Map.of("first", "Alice", "last", "Smith"));
var contactResult = contactInfoValidator.validate(Map.of("email", "a@b.com", "phone", "090-1234-5678"));

nameResult.combine(contactResult).apply(CustomerProfile::new)
// ==> Valid[value=CustomerProfile[name=PersonalName[first=Alice, last=Smith], contact=ContactInfo[email=a@b.com, phone=090-1234-5678]]]

nameValidator.validate(Map.of("first", ""))
        .combine(contactInfoValidator.validate(Map.of("email", "x")))
        .apply(CustomerProfile::new)
// ==> Invalid[errors=[/first: "first" must not be blank (charSequence.notBlank), /last: "last" is required (object.required), /email: "email" must be a valid email address (charSequence.email), /phone: "phone" is required (object.required)]]
```

## 15. Validating the rows of a list

`Result.traverse(rows, decoder::decode, Path.of("orders"))` is `liftList()` with
`prefixed`: every row is validated and its violations are located under its index.

```java
record Order(String orderId, BigDecimal total) {}

var orderValidator = Yavi.schema()
        ._string("order_id", c -> c.notBlank())
        ._bigDecimal("total", c -> c.positiveOrZero())
        .fromMap(Order::new);

var rows = List.<Map<String, Object>>of(
        Map.of("order_id", "A001", "total", 1000),
        Map.of("order_id", "", "total", -500),
        Map.of("order_id", "A003", "total", 300));

orderValidator.liftList().prefixed("orders").validate(rows)
// ==> Invalid[errors=[/orders/1/order_id: "orders[1].order_id" must not be blank (charSequence.notBlank), /orders/1/total: "orders[1].total" must be positive or zero (numeric.positiveOrZero)]]
```

## 16. Default values and recovery

### defaultValue

`withDefault` is `defaultValue(...)`: the default applies to an absent key and to
`null`. A value that is present but invalid is a violation.

```java
var roleValidator = Yavi.schema()
        ._enum(Yavi._enum("role", Role.class).ignoringCase()).defaultValue(Role.MEMBER)
        .fromMap();

roleValidator.validate(Map.of())
// ==> Valid[value=MEMBER]

roleValidator.validate(Map.of("role", "admin"))
// ==> Valid[value=ADMIN]

roleValidator.validate(Map.of("role", "invalid"))
// ==> Invalid[errors=[/role: "role" must be one of the following values: [ADMIN, MEMBER, GUEST] (object.oneOf)]]
```

The modifier follows the property whatever the input is: the same schema defaults a
JSON `null`, a missing member and an empty request parameter.

### recover

`recover(fallback)` replaces any failure, an invalid value included.

```java
var pageSizeValidator = Yavi.schema()
        ._integer("pageSize", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
        .fromMap();

pageSizeValidator.validate(Map.of("pageSize", 999))
// ==> Valid[value=20]

pageSizeValidator.validate(Map.of())
// ==> Valid[value=20]
```

Use `defaultValue` for a property that is optional by concept, and `recover` for one
that must not break whatever arrives. `recoverWith(violations -> ...)` computes the
fallback from the violations, for example to log them.

## 17. Rejecting unknown properties

`strict(f)` is `rejectingUnknownProperties()` of the validator.

```java
record ApiRequest(String action, int amount) {}

var apiRequestValidator = Yavi.schema()
        ._string("action", c -> c.notBlank())
        ._integer("amount", c -> c.positive())
        .fromMap(ApiRequest::new)
        .rejectingUnknownProperties();

apiRequestValidator.validate(Map.of("action", "transfer", "amount", 100))
// ==> Valid[value=ApiRequest[action=transfer, amount=100]]

apiRequestValidator.validate(Map.of("action", "transfer", "amount", 100, "extra", true))
// ==> Invalid[errors=[/extra: "extra" is not a known property (object.unknownProperty)]]
```

## 18. Recursive structures

`ValueValidator.recursive(self -> ...)` gives the function that builds the validator a
reference to it, so no array holds the validator before it is assigned.

```java
record Comment(String body, List<Comment> replies) {}

ObjectInputValidator<Comment> commentValidator = ValueValidator.recursive(self -> Yavi.schema()
        ._string("body", c -> c.notBlank())
        ._list("replies", self).defaultValue(List.of())
        .fromMap(Comment::new));

commentValidator.validate(Map.of(
        "body", "top",
        "replies", List.of(
                Map.of("body", "reply1", "replies", List.of()),
                Map.of("body", "reply2", "replies", List.of(
                        Map.of("body", "nested", "replies", List.of()))))))
// ==> Valid[value=Comment[body=top, replies=[Comment[body=reply1, replies=[]], Comment[body=reply2, replies=[Comment[body=nested, replies=[]]]]]]]

commentValidator.validate(Map.of(
        "body", "top",
        "replies", List.of(
                Map.of("body", "reply1"),
                Map.of("body", "reply2", "replies", List.of(Map.of("body", ""))))))
// ==> Invalid[errors=[/replies/1/replies/0/body: "replies[1].replies[0].body" must not be blank (charSequence.notBlank)]]
```

The type of the variable is declared: `var` does not give the compiler the type of
`self`. `ValueValidator.lazy(() -> validator)` is `lazy` of Raoh, for validators that
refer to each other.

## 19. Handling violations

```java
var checkValidator = Yavi.schema()
        ._string("email", c -> c.email())
        ._integer("age", c -> c.greaterThanOrEqual(0).lessThanOrEqual(150))
        .fromMap((email, age) -> Map.<String, Object>of("email", email, "age", age));

var result = checkValidator.validate(Map.of("email", "bad", "age", 300));
```

`Validated` is a sealed interface of the records `Valid` and `Invalid`, so a result is
handled with a `switch`. `errors()` returns the violations of a failed result.

### For forms

`issues.flatten()` is `messagesByPointer()`, or `messagesByName()` with the names of the
fields of a form.

```java
switch (result) {
    case Validated.Invalid(var violations) -> System.out.println(violations.messagesByPointer());
    case Validated.Valid(var value) -> {}
}
// {/email=["email" must be a valid email address], /age=["age" must be less than or equal to 150]}

result.errors().messagesByName()
// ==> {email=["email" must be a valid email address], age=["age" must be less than or equal to 150]}
```

### For APIs

`issues.toJsonList()` is `details()`: a list of records that a JSON library serializes
as they are. `params` is `meta`.

```java
result.errors().details()
// ==> [Simple[name=email, pointer=/email, messageKey=charSequence.email, message="email" must be a valid email address, params={}], Simple[name=age, pointer=/age, messageKey=numeric.lessThanOrEqual, message="age" must be less than or equal to 150, params={max=150}]]
```

### Locale-aware messages

A `MessageFormatter` formats the messages. `ResourceBundleMessageFormatter` looks a
message up by its key in a resource bundle, here `messages_ja.properties` on the class
path, and is the `ResourceBundleMessageResolver` of Raoh:

```properties
charSequence.email="{0}" はメールアドレスの形式で入力してください
numeric.lessThanOrEqual="{0}" は {1} 以下で入力してください
```

```java
var formatter = new ResourceBundleMessageFormatter("messages");

checkValidator.formattingWith(formatter).validate(Map.of("email", "bad", "age", 300), Locale.JAPANESE)
// ==> Invalid[errors=[/email: "email" はメールアドレスの形式で入力してください (charSequence.email), /age: "age" は 150 以下で入力してください (numeric.lessThanOrEqual)]]
```

The formatter is given to the validator, or to the violations afterwards
(`violations.formattedWith(formatter)`). A key that the bundle does not have keeps its
default message. With Spring, `new MessageSourceMessageFormatter(messageSource::getMessage)`
reads a `MessageSource`.

## 20. Scenario: a user registration API

```java
interface ContactMethod {}
record EmailMethod(String address) implements ContactMethod {}
record PhoneMethod(String number) implements ContactMethod {}
record Password(String value) {}
record UserRegistration(Email email, Password password, Role role, List<ContactMethod> contacts) {}

ValueValidator<Map<String, Object>, ContactMethod> contactMethodValidator = ValueValidator.anyOf(
        Yavi.schema()
                ._string("kind", c -> c.equalTo("email"))
                ._string("value", c -> c.email())
                .fromMap((kind, value) -> new EmailMethod(value)),
        Yavi.schema()
                ._string("kind", c -> c.equalTo("phone"))
                ._string("value", c -> c.pattern("^\\d{10,15}$"))
                .fromMap((kind, value) -> new PhoneMethod(value)));

var userRegistrationValidator = Yavi.schema()
        ._object("email", emailValidator)
        ._string("password", c -> c.greaterThanOrEqual(8).lessThanOrEqual(128)).map(Password::new)
        ._enum("role", Role.class).defaultValue(Role.MEMBER)
        ._list("contacts", contactMethodValidator, c -> c.notEmpty())
        .fromMap(UserRegistration::new);

userRegistrationValidator.validate(Map.of(
        "email", "  ALICE@Example.com  ",
        "password", "secureP@ss1",
        "contacts", List.of(
                Map.of("kind", "email", "value", "alice@example.com"),
                Map.of("kind", "phone", "value", "09012345678"))))
// ==> Valid[value=UserRegistration[email=Email[value=alice@example.com], password=Password[value=secureP@ss1], role=MEMBER, contacts=[EmailMethod[address=alice@example.com], PhoneMethod[number=09012345678]]]]

userRegistrationValidator.validate(Map.of(
        "email", "bad",
        "password", "short",
        "contacts", List.of(
                Map.of("kind", "email", "value", "not-email"))))
// ==> Invalid[errors=[/email: "email" must be a valid email address (charSequence.email), /password: The size of "password" must be greater than or equal to 8. The given size is 5 (container.greaterThanOrEqual), /contacts/0: "contacts[0]" must satisfy at least one of the 2 alternatives (object.anyOf) alternatives=[[/contacts/0/value: "contacts[0].value" must be a valid email address (charSequence.email)], [/contacts/0/kind: "contacts[0].kind" must be equal to phone (object.equalTo), /contacts/0/value: "contacts[0].value" must match ^\d{10,15}$ (charSequence.pattern)]]]]
```

A controller that receives JSON reads the tree with the same validator; nothing is
imported differently. [New features in 0.18.0](NewFeatures_0.18.0.md#validating-the-tree-model-of-a-json-library)
shows `JsonNodeReader` and the features of the mapper that keep a number as it is
written, which Raoh gets from its own `readTree`.

```java
@PostMapping("/users")
ResponseEntity<?> register(@RequestBody JsonNode body) {
    return switch (userRegistrationValidator.readingWith(JsonNodeReader.INSTANCE).validate(body)) {
        case Validated.Valid<UserRegistration>(var registration) ->
            ResponseEntity.status(201).body(userService.register(registration));
        case Validated.Invalid<UserRegistration>(var violations) ->
            ResponseEntity.badRequest().body(violations.details());
    };
}
```

## 21. Converting a list to a map

The list is validated and `map` converts it.

```java
record Product(String id, BigDecimal price) {}

var productValidator = Yavi.schema()
        ._string("id", c -> c.notBlank())
        ._bigDecimal("price", c -> c.positive())
        .fromMap(Product::new);

var priceMapValidator = Yavi.schema()
        ._list("products", productValidator)
        .fromMap()
        .map(products -> products.stream().collect(Collectors.toMap(Product::id, Product::price)));

priceMapValidator.validate(Map.of("products", List.of(
        Map.of("id", "APPLE", "price", new BigDecimal("120")),
        Map.of("id", "BANANA", "price", new BigDecimal("80")))))
// ==> Valid[value={APPLE=120, BANANA=80}]
```

A duplicate id is the violation of a rule of the domain. `unique(key)` of the list
reports the duplicated keys, where Raoh checks them by hand in `flatMap`:

```java
var strictPriceMapValidator = Yavi.schema()
        ._list("products", productValidator, c -> c.unique(Product::id))
        .fromMap()
        .map(products -> products.stream().collect(Collectors.toMap(Product::id, Product::price)));

strictPriceMapValidator.validate(Map.of("products", List.of(
        Map.of("id", "APPLE", "price", new BigDecimal("120")),
        Map.of("id", "APPLE", "price", new BigDecimal("99")))))
// ==> Invalid[errors=[/products: "products" must be unique. [APPLE] is/are duplicated. (collection.uniqueBy)]]
```

## 22. Known properties and the remaining keys

`_rest` collects the keys that no property is defined for, which Raoh does with a
hand-written decoder and `Result.map2`. The values are validated and a violation is
located under its key.

```java
record Goods(String name, Map<String, String> attrs) {}

var goodsValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._rest(Yavi._string())
        .fromMap(Goods::new);

goodsValidator.validate(Map.of("name", "T-shirt", "color", "red", "size", "L"))
// ==> Valid[value=Goods[name=T-shirt, attrs={color=red, size=L}]]

goodsValidator.validate(Map.of("name", "", "color", 1))
// ==> Invalid[errors=[/name: "name" must not be blank (charSequence.notBlank), /color: "color" must be of type string (object.type)]]
```

`_rest()` without a validator takes the values as they are.

## 23. Password confirmation

The two rules are predicates on the three values. The confirmation is not kept, so the
rules read an intermediate record and `map` creates the result. Each violation is
located on a field of the form.

```java
record PasswordForm(String current, String newPassword, String confirm) {}
record PasswordChange(String current, String newPassword) {}

var passwordChangeValidator = Yavi.schema()
        ._string("currentPassword", c -> c.notBlank())
        ._string("newPassword", c -> c.greaterThanOrEqual(8).lessThanOrEqual(128))
        ._string("confirmPassword", c -> c.notBlank())
        .fromMap(PasswordForm::new)
        .constraintOnTarget(form -> form.newPassword().equals(form.confirm()), "confirmPassword",
                ViolationMessage.of("password.mismatch", "\"{0}\" must match \"newPassword\""))
        .constraintOnTarget(form -> !form.current().equals(form.newPassword()), "newPassword",
                ViolationMessage.of("password.same", "\"{0}\" must differ from \"currentPassword\""))
        .map(form -> new PasswordChange(form.current(), form.newPassword()));

passwordChangeValidator.validate(Map.of(
        "currentPassword", "oldSecret",
        "newPassword", "newSecret1",
        "confirmPassword", "newSecret1"))
// ==> Valid[value=PasswordChange[current=oldSecret, newPassword=newSecret1]]

passwordChangeValidator.validate(Map.of(
        "currentPassword", "oldSecret",
        "newPassword", "short",
        "confirmPassword", "different"))
// ==> Invalid[errors=[/newPassword: The size of "newPassword" must be greater than or equal to 8. The given size is 5 (container.greaterThanOrEqual)]]

passwordChangeValidator.validate(Map.of(
        "currentPassword", "newSecret1",
        "newPassword", "newSecret1",
        "confirmPassword", "different"))
// ==> Invalid[errors=[/confirmPassword: "confirmPassword" must match "newPassword" (password.mismatch), /newPassword: "newPassword" must differ from "currentPassword" (password.same)]]
```

The rules are checked only when all properties are valid, like the checks inside
`flatMap`. Unlike them, the rules added one after another are all checked: the last input
violates both rules and reports both, so the user corrects the form once.

A rule that makes sense only when another holds is added after `map` or `andThen`, which
are given a value that satisfies the rules before them. Here the rule on `newPassword` is
checked only when the confirmation matches:

```java
var stepwiseValidator = Yavi.schema()
        ._string("currentPassword", c -> c.notBlank())
        ._string("newPassword", c -> c.greaterThanOrEqual(8).lessThanOrEqual(128))
        ._string("confirmPassword", c -> c.notBlank())
        .fromMap(PasswordForm::new)
        .constraintOnTarget(form -> form.newPassword().equals(form.confirm()), "confirmPassword",
                ViolationMessage.of("password.mismatch", "\"{0}\" must match \"newPassword\""))
        .map(form -> new PasswordChange(form.current(), form.newPassword()))
        .constraintOnTarget(change -> !change.current().equals(change.newPassword()), "newPassword",
                ViolationMessage.of("password.same", "\"{0}\" must differ from \"currentPassword\""));

stepwiseValidator.validate(Map.of(
        "currentPassword", "newSecret1",
        "newPassword", "newSecret1",
        "confirmPassword", "different"))
// ==> Invalid[errors=[/confirmPassword: "confirmPassword" must match "newPassword" (password.mismatch)]]
```

## 24. Pagination and query parameters

`defaultValue` (when missing) and `recover` (from any failure) say per parameter how
much it tolerates.

```java
enum SortOrder { ASC, DESC }
record PageRequest(int page, int size, SortOrder order) {}

var pageRequestValidator = Yavi.schema()
        // page: 0 when absent, a negative number is a violation
        ._integer("page", c -> c.greaterThanOrEqual(0)).defaultValue(0)
        // size: 20 when absent or invalid
        ._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
        // sort: ASC when absent or invalid
        ._enum(Yavi._enum("sort", SortOrder.class).ignoringCase()).recover(SortOrder.ASC)
        .fromMap(PageRequest::new);

pageRequestValidator.validate(Map.of("page", 2, "size", 50, "sort", "desc"))
// ==> Valid[value=PageRequest[page=2, size=50, order=DESC]]

pageRequestValidator.validate(Map.of())
// ==> Valid[value=PageRequest[page=0, size=20, order=ASC]]

pageRequestValidator.validate(Map.of("size", 9999))
// ==> Valid[value=PageRequest[page=0, size=20, order=ASC]]

pageRequestValidator.validate(Map.of("page", -1))
// ==> Invalid[errors=[/page: "page" must be greater than or equal to 0 (numeric.greaterThanOrEqual)]]
```

`recover` covers an absent key too, so it is not combined with a default. Query
parameters are strings, which the map of the Raoh tutorial does not show.
`readingParameters()` reads them with the same schema:

```java
var queryValidator = pageRequestValidator.readingParameters();

queryValidator.validate(Map.of("page", "2", "size", "50", "sort", "desc"))
// ==> Valid[value=PageRequest[page=2, size=50, order=DESC]]

queryValidator.validate(Map.of("page", "x", "size", "many"))
// ==> Invalid[errors=[/page: "page" must be of type integer (object.type)]]
```

## 25. Amount and currency

The number of decimal places that an amount may have depends on its currency.

```java
enum Currency { JPY, USD, EUR }
record Money(BigDecimal amount, Currency currency) {}

var moneyValidator = Yavi.schema()
        ._bigDecimal("amount", c -> c.positiveOrZero())
        ._enum(Yavi._enum("currency", Currency.class).ignoringCase())
        .flatFromMap((amount, currency) -> {
            int maxScale = switch (currency) {
                case JPY -> 0;
                case USD, EUR -> 2;
            };
            if (amount.scale() > maxScale) {
                return Validated.failureWith(ViolationMessage.of("money.scale", "{1} does not allow more than {2} decimal places", "currency", "maxScale"),
                        currency, maxScale);
            }
            return Validated.successWith(new Money(amount, currency));
        });

moneyValidator.validate(Map.of("amount", new BigDecimal("1000"), "currency", "jpy"))
// ==> Valid[value=Money[amount=1000, currency=JPY]]

moneyValidator.validate(Map.of("amount", new BigDecimal("10.5"), "currency", "jpy"))
// ==> Invalid[errors=[JPY does not allow more than 0 decimal places (money.scale)]]

moneyValidator.validate(Map.of("amount", new BigDecimal("9.99"), "currency", "usd"))
// ==> Valid[value=Money[amount=9.99, currency=USD]]
```

The rule that does not need the currency is a constraint of the amount
(`positiveOrZero()`). A rule without arguments in its message is shorter as a predicate:

```java
        .fromMap(Money::new)
        .constraintOnTarget(money -> money.amount().scale() <= (money.currency() == Currency.JPY ? 0 : 2), "amount",
                ViolationMessage.of("money.scale", "\"{0}\" has too many decimal places for the currency"));
// ==> Invalid[errors=[/amount: "amount" has too many decimal places for the currency (money.scale)]]
```

## 26. CSV import

All rows are checked and the violations are reported with the index of their row.

```java
record MemberImport(String email, String name, int age) {}

var memberValidator = Yavi.schema()
        ._string("email", c -> c.trim().toLowerCase().notBlank().email())
        ._string("name", c -> c.trim().notBlank().lessThanOrEqual(100))
        ._integer("age", c -> c.greaterThanOrEqual(0).lessThanOrEqual(150))
        .fromMap(MemberImport::new);

var rows = List.<Map<String, Object>>of(
        Map.of("email", "alice@example.com", "name", "Alice", "age", 30),
        Map.of("email", "bad-email", "name", "", "age", 200),
        Map.of("email", "bob@example.com", "name", "Bob", "age", 25),
        Map.of("email", "also-bad", "name", "Carol", "age", -1));

switch (memberValidator.liftList().prefixed("rows").validate(rows)) {
    case Validated.Valid(var members) -> System.out.println("Imported: " + members.size());
    case Validated.Invalid(var violations) -> violations.messagesByPointer().forEach((pointer, messages) ->
            System.out.println(pointer + ": " + messages));
}
// /rows/1/email: ["rows[1].email" must be a valid email address]
// /rows/1/name: ["rows[1].name" must not be blank]
// /rows/1/age: ["rows[1].age" must be less than or equal to 150]
// /rows/3/email: ["rows[3].email" must be a valid email address]
// /rows/3/age: ["rows[3].age" must be greater than or equal to 0]
```

The cells of a real CSV file are strings. The same validator reads them with
`readingParameters`, and `trimming()` trims every cell, so no property repeats
`c.trim()`:

```java
var cells = List.of(
        Map.of("email", " Alice@Example.com ", "name", " Alice ", "age", "30"),
        Map.of("email", "bad-email", "name", "  ", "age", "abc"));

memberValidator.readingParameters(StringCoercion.strict().trimming()).liftList().prefixed("rows").validate(cells)
// ==> Invalid[errors=[/rows/1/email: "rows[1].email" must be a valid email address (charSequence.email), /rows/1/name: "rows[1].name" must not be blank (charSequence.notBlank), /rows/1/age: "rows[1].age" must be of type integer (object.type)]]
```

## 27. Configuration files

Required and optional sections of a configuration file that is parsed into a map.

```java
record DbConfig(String host, int port, String database) {}
record CacheConfig(String host, int port, int ttlSeconds) {}
record AppConfig(DbConfig db, CacheConfig cache, String logLevel) {}

var dbConfigValidator = Yavi.schema()
        ._string("host", c -> c.notBlank())
        ._integer("port", c -> c.greaterThanOrEqual(1).lessThanOrEqual(65535)).defaultValue(5432)
        ._string("database", c -> c.notBlank())
        .fromMap(DbConfig::new);

var cacheConfigValidator = Yavi.schema()
        ._string("host", c -> c.notBlank()).defaultValue("localhost")
        ._integer("port", c -> c.greaterThanOrEqual(1).lessThanOrEqual(65535)).defaultValue(6379)
        ._integer("ttlSeconds", c -> c.positive()).defaultValue(300)
        .fromMap(CacheConfig::new);

var appConfigValidator = Yavi.schema()
        ._object("db", dbConfigValidator)
        // the cache section itself is optional
        ._object("cache", cacheConfigValidator).defaultValue(new CacheConfig("localhost", 6379, 300))
        ._string("logLevel", c -> c.notBlank()).defaultValue("INFO")
        .fromMap(AppConfig::new);

appConfigValidator.validate(Map.of(
        "db", Map.of("host", "db.example.com", "database", "myapp")))
// ==> Valid[value=AppConfig[db=DbConfig[host=db.example.com, port=5432, database=myapp], cache=CacheConfig[host=localhost, port=6379, ttlSeconds=300], logLevel=INFO]]

appConfigValidator.validate(Map.of(
        "db", Map.of("host", "", "database", "myapp")))
// ==> Invalid[errors=[/db/host: "db.host" must not be blank (charSequence.notBlank)]]
```

## 28. Tuples

`Arguments::of` is `Tuple2::new`: `fromMap(Arguments::of)` produces an `Arguments2`, a
record that is deconstructed in a `switch`. There are `Arguments1` to `Arguments16`.

```java
var input = Map.<String, Object>of("name", "Alice", "age", 30);

var tupleValidator = Yavi.schema()
        ._string("name")
        ._integer("age")
        .fromMap(Arguments::of);

switch (tupleValidator.validate(input)) {
    case Validated.Valid(Arguments2(var name, var age)) -> System.out.println(name + " is " + age);
    case Validated.Invalid(var violations) -> System.out.println(violations.messagesByPointer());
}
// Alice is 30
```

The elements are `arg1()`, `arg2()`, ... without a pattern.

## 29. Writing an object back as a map

Raoh writes an encoder next to the decoder. In YAVI the schema that reads a map also
writes it: `Yavi.reversibleSchema()` ends with `fromMapReversible`, which is `fromMap`
with one accessor per property, and `toMap` is `encode`. The names are written once.

```java
record ItemId(long value) {}
record Item(ItemId id, String name, BigDecimal price) {}

var itemSchema = Yavi.reversibleSchema()
        ._long("id").map(ItemId::new, ItemId::value)
        ._string("name")
        ._bigDecimal("price")
        .fromMapReversible(Item::new, Item::id, Item::name, Item::price);

itemSchema.toMap(new Item(new ItemId(42L), "Widget", new BigDecimal("9.99")))
// ==> {id=42, name=Widget, price=9.99}

itemSchema.validate(itemSchema.toMap(new Item(new ItemId(42L), "Widget", new BigDecimal("9.99"))))
// ==> Valid[value=Item[id=ItemId[value=42], name=Widget, price=9.99]]
```

| Raoh encoder | YAVI |
| --- | --- |
| `property("name", T::name, string())` | `_string("name")` and the accessor `T::name` |
| `object(property(...), ...)` | `fromMapReversible(T::new, accessors...)` |
| `long_().contramap(ItemId::value)` | `._long("id").map(ItemId::new, ItemId::value)` |
| `nested(subEncoder)` | `_object(name, subSchema)` |
| `list(elementEncoder)` | `_list(name, elementSchema)` |
| `nullableProperty("x", T::x, enc)` | `.nullable()`, `.optional()` |
| `propertyWithDefault("x", T::x, enc, v)` | `.defaultValue(v)` |

`map(f, g)` is `map(f)` with its reverse. A property of a reversible schema has no
`map(f)` alone, and a validator without a reverse is not taken as a property: neither
compiles.

## 30. Writing null and default values

The modifiers of the property say what is written. A property without a value is
written as `null`, because a named parameter of a statement needs its key.

```java
record Tag(String name) {}
record Article(String title, Optional<String> description, List<Tag> tags) {}

var tagSchema = Yavi.reversibleSchema()
        ._string("name")
        .fromMapReversible(Tag::new, Tag::name);

var articleSchema = Yavi.reversibleSchema()
        ._string("title")
        ._string("description").optional()
        ._list("tags", tagSchema).defaultValue(List.of())
        .fromMapReversible(Article::new, Article::title, Article::description, Article::tags);

articleSchema.toMap(new Article("t", Optional.empty(), List.of()))
// ==> {title=t, description=null, tags=[]}

articleSchema.toMap(new Article("t", Optional.of("d"), List.of(new Tag("java"))))
// ==> {title=t, description=d, tags=[{name=java}]}
```

The object that is written was created by the validator, so `tags` is never `null`
here; `propertyWithDefault` of Raoh replaces a `null` of the getter.

## 31. Writing nested objects

A nested schema writes a nested map and a list of them a list of maps.

```java
record Buyer(String name) {}
record Purchase(long id, Buyer buyer, List<Item> items) {}

var buyerSchema = Yavi.reversibleSchema()
        ._string("name")
        .fromMapReversible(Buyer::new, Buyer::name);

var purchaseSchema = Yavi.reversibleSchema()
        ._long("id")
        ._object("buyer", buyerSchema)
        ._list("items", itemSchema)
        .fromMapReversible(Purchase::new, Purchase::id, Purchase::buyer, Purchase::items);

purchaseSchema.toMap(new Purchase(1L, new Buyer("Alice"), List.of(new Item(new ItemId(42L), "Widget", new BigDecimal("9.99")))))
// ==> {id=1, buyer={name=Alice}, items=[{id=42, name=Widget, price=9.99}]}
```

## 32. Writing a tagged union

An alternative that is added with the type of its objects is written back with the
discriminator. The union that reads is the union that writes.

```java
var circleSchema = Yavi.reversibleSchema()
        ._double("radius")
        .fromMapReversible(Circle::new, Circle::radius);

var rectSchema = Yavi.reversibleSchema()
        ._double("width")
        ._double("height")
        .fromMapReversible(Rect::new, Rect::width, Rect::height);

var shapeSchema = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Circle.class, circleSchema)
        .when("rect", Rect.class, rectSchema);

shapeSchema.toMap(new Circle(2.0))
// ==> {type=circle, radius=2.0}

shapeSchema.validate(shapeSchema.toMap(new Circle(2.0)))
// ==> Valid[value=Circle[radius=2.0]]
```

An object of a type that no alternative is defined for fails with
`IllegalArgumentException`, as in Raoh.

See [New features in 0.18.0](NewFeatures_0.18.0.md#writing-an-object-back-as-the-map-it-is-read-from)
for the views that write some of the properties and for the properties that are
withheld.
