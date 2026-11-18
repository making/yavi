# Built-in Constraints

YAVI provides built-in constraints for each supported type. The same constraint object is used by the schema style and by the classic style, so each constraint is documented once on this page.

```java
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import am.ik.yavi.validator.Yavi;

// Schema style: the validator of a single value
var emailValidator = Yavi._string("email", c -> c.notBlank().email());

// Classic style: a property of an object
record Signup(String email) {}

Validator<Signup> signupValidator = ValidatorBuilder.<Signup>of()
        ._string(Signup::email, "email", c -> c.notBlank().email())
        .build();
```

The lambdas are identical. In the schema style, a property is also written with `Yavi.schema()._string("email", c -> ...)`; in the classic style, the typed method `_string` selects the type of the property and avoids an ambiguous inference, and `constraint` is the untyped alternative.

## Null values and required values

The schema style and the classic style differ in what `null` means.

* A property of `Yavi.schema()` and a validator of a single value built with `Yavi._string(...)` reject `null` by default with `object.notNull`. A value that may be `null` declares it with `nullable()` (or `optional()`, `defaultValue(...)`, `recover(...)`); see [Optional and Default Values](optional-and-defaults.md). `isNull()` is usable only after `nullable()`.
* A property of a `ValidatorBuilder` validator is valid when it is `null`, and every check is skipped, unless a constraint rejects `null`. `notNull()`, `notBlank()` and `notEmpty()` reject `null`; the other constraints on this page do not.
* An empty string is valid for every format of a string, whatever the style. A format tells what a given text looks like, not that a text is given. Add `notEmpty()` or `notBlank()` to require it.

The message key and the `params` names of each constraint are listed with it. `params` of a violation holds the arguments by name and does not include the name of the value or the violated value; the positional arguments remain available from `ConstraintViolation#args()`. See [Violations](violations.md#violationdetail).

## String and CharSequence

Use `_string` for a `String`; the classic style also has `_charSequence` for a `CharSequence`. The constraints are in `am.ik.yavi.constraint.CharSequenceConstraint`.

| Constraint | Method | Accepted values | Message key | `params` |
| --- | --- | --- | --- | --- |
| Not null | `notNull()` | any non-null value | `object.notNull` | - |
| Null | `isNull()` | only `null` | `object.isNull` | - |
| Not empty | `notEmpty()` | non-null, not empty | `container.notEmpty` | - |
| Not blank | `notBlank()` | non-null, not only white space | `charSequence.notBlank` | - |
| Fixed size | `fixedSize(int)` | the given number of characters | `container.fixedSize` | `size`, `actualSize` |
| Size greater than | `greaterThan(int)` | more than the given number | `container.greaterThan` | `min`, `actualSize` |
| Size greater than or equal | `greaterThanOrEqual(int)` | at least the given number | `container.greaterThanOrEqual` | `min`, `actualSize` |
| Size less than | `lessThan(int)` | fewer than the given number | `container.lessThan` | `max`, `actualSize` |
| Size less than or equal | `lessThanOrEqual(int)` | at most the given number | `container.lessThanOrEqual` | `max`, `actualSize` |
| Contains | `contains(CharSequence)` | text containing the sequence | `charSequence.contains` | `substring` |
| Starts with | `startsWith(CharSequence)` | text starting with the prefix | `charSequence.startsWith` | `prefix` |
| Ends with | `endsWith(CharSequence)` | text ending with the suffix | `charSequence.endsWith` | `suffix` |
| Pattern | `pattern(String)`, `pattern(Pattern)`, `pattern(Supplier<Pattern>)` | text matching the regular expression | `charSequence.pattern` | `pattern` |
| Email | `email()` | an email address | `charSequence.email` | - |
| UUID | `uuid()` | 8-4-4-4-12 hexadecimal; version and variant are not checked | `charSequence.uuid` | - |
| ULID | `ulid()` | 26 characters of Crockford Base32, first `0`-`7`, upper or lower case | `charSequence.ulid` | - |
| IP address | `ip()` | an IPv4 or IPv6 address | `charSequence.ip` | - |
| IPv4 | `ipv4()` | an IPv4 address | `charSequence.ipv4` | - |
| IPv6 | `ipv6()` | an IPv6 address, including a zone id and a prefix length | `charSequence.ipv6` | - |
| URI | `uri()` | an absolute URI with a scheme of any kind (`mailto:`, `urn:`, `s3://`); a character outside US-ASCII is rejected | `charSequence.uri` | - |
| URL | `url()` | a URL that `java.net.URI` parses | `charSequence.url` | - |
| URL with restrictions | `url(url -> url.protocol(...).host(...).port(...).build())` | a valid URL whose protocol, host and port are in the given sets | `charSequence.url`, `charSequence.url.protocol`, `charSequence.url.host`, `charSequence.url.port` | `values` |
| Luhn | `luhn()` | decimal digits that pass the Luhn checksum | `charSequence.luhn` | - |
| Password policy | `password(policy -> ...)` | text that meets the policies | `password.required`, `password.optional` | `policy` / `min`, `policies` |
| Byte representation | `isByte()` | text that parses as a `Byte` | `charSequence.byte` | - |
| Short representation | `isShort()` | text that parses as a `Short` | `charSequence.short` | - |
| Integer representation | `isInteger()` | text that parses as an `Integer` | `charSequence.integer` | - |
| Long representation | `isLong()` | text that parses as a `Long` | `charSequence.long` | - |
| Float representation | `isFloat()` | text that parses as a `Float` | `charSequence.float` | - |
| Double representation | `isDouble()` | text that parses as a `Double` | `charSequence.double` | - |
| BigInteger representation | `isBigInteger()` | text that parses as a `BigInteger` | `charSequence.biginteger` | - |
| BigDecimal representation | `isBigDecimal()` | text that parses as a `BigDecimal` | `charSequence.bigdecimal` | - |
| Local date | `isoLocalDate()`, `localDate(String pattern)` | a date in the pattern, resolved with `ResolverStyle.STRICT` | `charSequence.localdate` | `pattern` |

`ip()`, `uri()` and `ulid()` are new in 0.18. `url(builder)` restricts the parsed URL: `protocol` accepts protocol names, `host` accepts host names compared case-insensitively as written, and `port` accepts port numbers; a URL without an explicit port is matched against the default port of its protocol.

```java
var proxyValidator = Yavi._string("proxy", c -> c.notBlank().ip());
proxyValidator.validate("");                 // "proxy" must not be blank
proxyValidator.validate("192.0.2.1");        // Valid
proxyValidator.validate("2001:db8::1");      // Valid
proxyValidator.validate("example.com");      // Invalid (charSequence.ip)

var homepageValidator = Yavi._string("homepage", c -> c.uri());
homepageValidator.validate("mailto:a@example.com"); // Valid
homepageValidator.validate("/relative/path");       // Invalid
```

`pattern(Supplier<Pattern>)` calls the supplier for every validation, for regular expressions that are built at validation time.

Built-in password policies are `UPPERCASE`, `LOWERCASE`, `ALPHABETS`, `NUMBERS` and `SYMBOLS` in `am.ik.yavi.constraint.password.PasswordPolicy`. A policy is required with `required(...)` or made one of a minimum count with `optional(int, ...)`; `uppercase(2)` requires two upper case characters. A custom `PasswordPolicy` can be passed in the same way.

## Character counting and byte size

The size constraints of a character sequence count Unicode code points after normalization and after the ignored variation selectors are removed. The default normalizer is `Normalizer.Form.NFC`, so `モジ` written as `モシ` plus a voiced sound mark counts as two characters instead of three. The constraints that inspect the text itself (`contains`, `startsWith`, `endsWith`, `pattern`, the formats and `codePoints`) do not normalize the value.

| Constraint | Method | Effect | Message key | `params` |
| --- | --- | --- | --- | --- |
| Normalizer | `normalizer(Normalizer.Form)`, `normalizer(null)` | sets or disables the normalization applied before the size is measured | - | - |
| Emoji | `emoji()` | counts emoji sequences as one character, using extended grapheme clusters; it is off by default | - | - |
| Variant selectors | `variant(opts -> opts.ivs(...).svs(...).fvs(...))` | removes the selected variation selectors before the size is measured | - | - |
| Code points | `codePoints(...).asWhiteList()`, `.asBlackList()` | allows or forbids the given code points | `codePoints.asWhiteList`, `codePoints.asBlackList` | `notAllowed` |
| Byte size | `asByteArray()`, `asByteArray(Charset)` followed by `fixedSize`, `greaterThan`, `greaterThanOrEqual`, `lessThan`, `lessThanOrEqual` | measures the encoded byte length | `byteSize.fixedSize`, `byteSize.greaterThan`, `byteSize.greaterThanOrEqual`, `byteSize.lessThan`, `byteSize.lessThanOrEqual` | `size`, `actualSize` / `min`, `actualSize` / `max`, `actualSize` |

`emoji()` is not enabled by default because it is expensive. In 0.18 it counts extended grapheme clusters, so `"\r\n"` and a half-width katakana with a voiced sound mark count as one; the `Emoji` class is removed.

`normalizer`, `emoji()` and `variant(...)` are not constraints by themselves: the message of the size constraint applied to their result is reported. Code point sets are represented by `am.ik.yavi.constraint.charsequence.CodePoints`, and built-in sets are available from `AsciiCodePoints` and `UnicodeCodePoints`.

An emoji sequence is counted as one only when `emoji()` is added; otherwise each code point is counted. `asByteArray()` measures the UTF-8 byte length unless a charset is given, for a column whose byte length is limited.

```java
var messageValidator = Yavi._string("text", c -> c.emoji().lessThanOrEqual(3)
        .asByteArray().lessThanOrEqual(16));
messageValidator.validate("I❤️☕️️");        // Valid
messageValidator.validate("❤️❤️❤️️️");     // Invalid (byteSize.lessThanOrEqual)
```

## Integral numbers

The classic style uses `_byte`, `_short`, `_integer`, `_long`, `_character` and `_bigInteger` for the corresponding types; the schema style has `_short`, `_integer`, `_long` and `_bigInteger`. The constraints on `Character` compare the numeric value of the character; the zero value used by `positive` and the other sign constraints is `Character.MIN_VALUE`.

| Constraint | Method | Accepted values | Message key | `params` |
| --- | --- | --- | --- | --- |
| Not null | `notNull()` | any non-null value | `object.notNull` | - |
| Null | `isNull()` | only `null` | `object.isNull` | - |
| Greater than | `greaterThan(V)` | greater than the bound | `numeric.greaterThan` | `min` |
| Greater than or equal | `greaterThanOrEqual(V)` | at least the bound | `numeric.greaterThanOrEqual` | `min` |
| Less than | `lessThan(V)` | less than the bound | `numeric.lessThan` | `max` |
| Less than or equal | `lessThanOrEqual(V)` | at most the bound | `numeric.lessThanOrEqual` | `max` |
| Positive | `positive()` | greater than zero | `numeric.positive` | - |
| Positive or zero | `positiveOrZero()` | zero or greater | `numeric.positiveOrZero` | - |
| Negative | `negative()` | less than zero | `numeric.negative` | - |
| Negative or zero | `negativeOrZero()` | zero or less | `numeric.negativeOrZero` | - |
| Multiple | `multipleOf(V)` | a multiple of the divisor | `numeric.multipleOf` | `divisor` |

`multipleOf` is available for `Byte`, `Short`, `Integer`, `Long`, `BigInteger` and `BigDecimal`; `Character`, `Double` and `Float` do not have it.

```java
var quantityValidator = Yavi._integer("quantity", c -> c.multipleOf(12));
quantityValidator.validate(24);  // Valid
quantityValidator.validate(30);  // Invalid ("quantity" must be a multiple of 12)
```

## BigInteger and BigDecimal

`BigInteger` and `BigDecimal` have the constraints of the integral types, including `multipleOf`. `BigDecimal` additionally has the constraints on the number of digits.

| Constraint | Method | Accepted values | Message key | `params` |
| --- | --- | --- | --- | --- |
| Fraction digits | `fractionDigits(int)` | at most the given number of digits after the decimal point; trailing zeros are not counted | `numeric.fractionDigits` | `max` |
| Integer digits | `integerDigits(int)` | at most the given number of digits before the decimal point; the zero of `0.5` is not counted | `numeric.integerDigits` | `max` |

The comparisons use `BigDecimal#compareTo` and ignore the scale, so `1.0` and `1.00` are the same value and both have no fraction digit. The scale is not changed by the constraint; use `preprocess` or `map` when the produced value must have a fixed scale.

```java
var priceValidator = Yavi._bigDecimal("price", c -> c.fractionDigits(2).integerDigits(6));
priceValidator.validate(new BigDecimal("1.50"));  // Valid (1.5 has one fraction digit)
priceValidator.validate(new BigDecimal("1.505")); // Invalid (numeric.fractionDigits)
```

## Double and Float

`_double` and `_float` have `notNull`, `isNull`, `greaterThan`, `greaterThanOrEqual`, `lessThan`, `lessThanOrEqual`, `positive`, `positiveOrZero`, `negative` and `negativeOrZero`, with the same message keys as the integral types. They do not have `multipleOf`, because the remainder of a binary fraction is not exact. `NaN` satisfies none of the bounds and is rejected by every comparison.

## Temporal

The temporal constraints are available for the following types, with the classic builder shortcuts. The schema style uses the same names as property methods, for example `Yavi.schema()._localDate("date", c -> c.past())`.

| Type | Constraint class | Classic shortcut |
| --- | --- | --- |
| `LocalDate` | `LocalDateConstraint` | `_localDate` |
| `LocalTime` | `LocalTimeConstraint` | `_localTime` |
| `LocalDateTime` | `LocalDateTimeConstraint` | `_localDateTime` |
| `OffsetDateTime` | `OffsetDateTimeConstraint` | `_offsetDateTime` |
| `Instant` | `InstantConstraint` | `_instant` |
| `Year` | `YearConstraint` | `_year` |
| `YearMonth` | `YearMonthConstraint` | `_yearMonth` |
| `ZonedDateTime` | `ZonedDateTimeConstraint` | `_zonedDateTime` |

| Constraint | Method | Accepted values | Message key | `params` |
| --- | --- | --- | --- | --- |
| Past | `past()`, `past(Clock)` | before the current time | `temporal.past` | - |
| Past or present | `pastOrPresent()`, `pastOrPresent(Clock)` | at or before the current time | `temporal.pastOrPresent` | - |
| Future | `future()`, `future(Clock)` | after the current time | `temporal.future` | - |
| Future or present | `futureOrPresent()`, `futureOrPresent(Clock)` | at or after the current time | `temporal.futureOrPresent` | - |
| Before | `before(Supplier<T>)` | before the supplied boundary | `temporal.before` | `other` |
| Before or equal | `beforeOrEqual(Supplier<T>)` | at or before the supplied boundary | `temporal.beforeOrEqual` | `other` |
| After | `after(Supplier<T>)` | after the supplied boundary | `temporal.after` | `other` |
| After or equal | `afterOrEqual(Supplier<T>)` | at or after the supplied boundary | `temporal.afterOrEqual` | `other` |
| Between | `between(Supplier<T>, Supplier<T>)` | strictly between the supplied bounds | `temporal.between` | `rangeFrom`, `rangeTo` |
| Field | `fieldPredicate(TemporalField, LongPredicate)` | a value of the field accepted by the predicate | `temporal.field` | `field` |

`before`, `beforeOrEqual`, `after`, `afterOrEqual` and `between` have an overload with a `boolean memoize` argument. It is `true` by default, so the supplier is called at most once and the same value is used for the comparison and the message. `between` throws `IllegalArgumentException` when `rangeFrom` is after `rangeTo`. The `Clock` overloads exist for deterministic tests and for a time source other than the system default zone.

A `null` value is valid and skips every temporal check.

## Boolean

| Constraint | Method | Accepted values | Message key | `params` |
| --- | --- | --- | --- | --- |
| Not null | `notNull()` | any non-null value | `object.notNull` | - |
| Null | `isNull()` | only `null` | `object.isNull` | - |
| True | `isTrue()` | `true` | `boolean.isTrue` | - |
| False | `isFalse()` | `false` | `boolean.isFalse` | - |

## Collection and array

Use `_collection` for a `Collection` and `_objectArray`, `_booleanArray`, `_byteArray`, `_charArray`, `_shortArray`, `_intArray`, `_longArray`, `_floatArray` or `_doubleArray` for an array. The element or value validation of a list, a map and an array, including `_list`, `_map` and `forEach`, is described in [Objects and Collections](objects-and-collections.md#lists).

| Constraint | Method | Accepted values | Message key | `params` |
| --- | --- | --- | --- | --- |
| Not null | `notNull()` | any non-null value | `object.notNull` | - |
| Null | `isNull()` | only `null` | `object.isNull` | - |
| Not empty | `notEmpty()` | a non-empty collection or array | `container.notEmpty` | - |
| Fixed size | `fixedSize(int)` | the given number of elements | `container.fixedSize` | `size`, `actualSize` |
| Size greater than | `greaterThan(int)` | more than the given number | `container.greaterThan` | `min`, `actualSize` |
| Size greater than or equal | `greaterThanOrEqual(int)` | at least the given number | `container.greaterThanOrEqual` | `min`, `actualSize` |
| Size less than | `lessThan(int)` | fewer than the given number | `container.lessThan` | `max`, `actualSize` |
| Size less than or equal | `lessThanOrEqual(int)` | at most the given number | `container.lessThanOrEqual` | `max`, `actualSize` |
| Contains | `contains(E)` | a collection or array containing the element | `collection.contains`, `array.contains` | `element` |
| Unique | `unique()` | elements without duplicates | `collection.unique` | `duplicates` |
| Unique by key | `unique(Function<? super E, ?>)` | elements whose keys are unique | `collection.uniqueBy` | `duplicates` |
| Contains all | `containsAll(Collection<? extends E>)` | all of the given elements | `collection.containsAll` | `values`, `missing` |

`unique(key)` reports the duplicated keys. A `Set` and a non-`Set` collection have the same semantics for `containsAll`. A `null` array is valid and the checks are skipped, except `notEmpty()`.

```java
var productsValidator = Yavi.schema()._list("products", productValidator, c -> c.unique(Product::id));
// "products" must be unique. [APPLE] is/are duplicated.
```

## Map

Use `_map` for a `Map`.

| Constraint | Method | Accepted values | Message key | `params` |
| --- | --- | --- | --- | --- |
| Not null | `notNull()` | any non-null value | `object.notNull` | - |
| Null | `isNull()` | only `null` | `object.isNull` | - |
| Not empty | `notEmpty()` | a non-empty map | `container.notEmpty` | - |
| Fixed size | `fixedSize(int)` | the given number of entries | `container.fixedSize` | `size`, `actualSize` |
| Size greater than | `greaterThan(int)` | more than the given number | `container.greaterThan` | `min`, `actualSize` |
| Size greater than or equal | `greaterThanOrEqual(int)` | at least the given number | `container.greaterThanOrEqual` | `min`, `actualSize` |
| Size less than | `lessThan(int)` | fewer than the given number | `container.lessThan` | `max`, `actualSize` |
| Size less than or equal | `lessThanOrEqual(int)` | at most the given number | `container.lessThanOrEqual` | `max`, `actualSize` |
| Contains key | `containsKey(K)` | a map containing the key | `map.containsKey` | `key` |
| Contains value | `containsValue(V)` | a map containing the value | `map.containsValue` | `value` |

## Object

Use `_object` for any other object. In the classic style, `constraintOnTarget` applies the constraints to the validated object itself.

| Constraint | Method | Accepted values | Message key | `params` |
| --- | --- | --- | --- | --- |
| Not null | `notNull()` | any non-null value | `object.notNull` | - |
| Null | `isNull()` | only `null` | `object.isNull` | - |
| Equal to | `equalTo(Object)` | a value equal to the given value | `object.equalTo` | `value` |
| One of | `oneOf(Collection)` | one of the given values | `object.oneOf` | `values` |
| Not one of | `notOneOf(Collection)` | none of the given values | `object.notOneOf` | `values` |
| Password policy | `password(policy -> ...)` | a value that meets the policies; a policy can inspect the whole object | `password.required`, `password.optional` | `policy` / `min`, `policies` |

## Enum

Use `_enum` for an enum. The schema style and the classic style both take the enum type.

```java
var statusValidator = Yavi._enum("status", Status.class, c -> c.oneOf(Status.ACTIVE));
statusValidator.validate(Status.ACTIVE);   // Valid
statusValidator.validate(Status.INACTIVE); // Invalid ("status" must be one of the following values: [ACTIVE])
```

| Constraint | Method | Accepted values | Message key | `params` |
| --- | --- | --- | --- | --- |
| Not null | `notNull()` | any non-null value | `object.notNull` | - |
| Null | `isNull()` | only `null` | `object.isNull` | - |
| One of | `oneOf(E...)` | one of the given constants; the list must not be empty | `object.oneOf` | `values` |

A `null` enum value is valid in the classic style unless `notNull()` is added. `EnumValidatorBuilder` and `EnumValidator` validate a single enum value.
