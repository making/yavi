# Built-in Constraints

YAVI includes many commonly used built-in constraints for various types.

## String

Apply the constraints on the `String` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraint(/* Function from TargetClass to String  */, /* Field Name */, /* Constraints */)
    .build();
```

To avoid ambiguous type inferences, you can use explicit `_string` instead of `constraint` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._string(/* Function from TargetClass to String  */, /* Field Name */, /* Constraints */)
    .build();
```

If you want to apply constraints to the more generic `CharSequence`, use `_charSequence` method.

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._charSequence(/* Function from TargetClass to CharSequence  */, /* Field Name */, /* Constraints */)
    .build();
```

Unless stated otherwise, a `null` value is valid and the check is skipped in the following constraints.

### `notNull()`

Checks that the target value is not `null`

```java
Validator<Email> validator = ValidatorBuilder.<Email> of()
    .constraint(Email::asString, "email", c -> c.notNull())
    .build();

ConstraintViolations violations = validator.validate(new Email("yavi@example.com")); // Valid
ConstraintViolations violations = validator.validate(new Email(null)); // Invalid ("email" must not be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isNull()`

Checks that the target value is `null`

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(User::getId, "id", c -> c.isNull())
    .build();

ConstraintViolations violations = validator.validate(new User(null)); // Valid
ConstraintViolations violations = validator.validate(new User("1234")); // Invalid ("id" must be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.isNull` | `"{0}" must be null` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `notEmpty()`

Checks whether the target value is not `null` nor empty

```java
Validator<Email> validator = ValidatorBuilder.<Email> of()
    .constraint(Email::asString, "email", c -> c.notEmpty())
    .build();

ConstraintViolations violations = validator.validate(new Email("yavi@example.com")); // Valid
ConstraintViolations violations = validator.validate(new Email(null)); // Invalid ("email" must not be empty)
ConstraintViolations violations = validator.validate(new Email("")); // Invalid ("email" must not be empty)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.notEmpty` | `"{0}" must not be empty` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `notBlank()`

Checks that the target value is not `null`, and the trimmed userId is greater than `0`.
The difference to `notEmpty()` is that trailing white-spaces are ignored.

```java
Validator<Email> validator = ValidatorBuilder.<Email> of()
    .constraint(Email::asString, "email", c -> c.notBlank())
    .build();

ConstraintViolations violations = validator.validate(new Email("yavi@example.com")); // Valid
ConstraintViolations violations = validator.validate(new Email(null)); // Invalid ("email" must not be blank)
ConstraintViolations violations = validator.validate(new Email(" ")); // Invalid ("email" must not be blank)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.notBlank` | `"{0}" must not be blank` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `fixedSize(int)`

Checks if the target value's size is the specified size

```java
Validator<ZipCode> validator = ValidatorBuilder.<ZipCode> of()
    .constraint(ZipCode::asString, "zipCode", c -> c.fixedSize(7))
    .build();

ConstraintViolations violations = validator.validate(new ZipCode("1234567")); // Valid
ConstraintViolations violations = validator.validate(new ZipCode(null)); // Valid
ConstraintViolations violations = validator.validate(new ZipCode("123-4567")); // Invalid (The size of "zipCode" must be 7. The given size is 8)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.fixedSize` | `The size of "{0}" must be {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `greaterThan(int)`

Checks if the target value's size is greater than the specified size

```java
Validator<Country> validator = ValidatorBuilder.<Country> of()
    .constraint(Country::asString, "country", c -> c.greaterThan(2))
    .build();

ConstraintViolations violations = validator.validate(new Country("Japan")); // Valid
ConstraintViolations violations = validator.validate(new Country(null)); // Valid
ConstraintViolations violations = validator.validate(new Country("J")); // Invalid (The size of "country" must be greater than 2. The given size is 1)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.greaterThan` | `The size of "{0}" must be greater than {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `greaterThanOrEqual(int)`

Checks if the target value's size is greater than or equals to the specified size

```java
Validator<Country> validator = ValidatorBuilder.<Country> of()
    .constraint(Country::asString, "country", c -> c.greaterThanOrEqual(2))
    .build();

ConstraintViolations violations = validator.validate(new Country("Japan")); // Valid
ConstraintViolations violations = validator.validate(new Country(null)); // Valid
ConstraintViolations violations = validator.validate(new Country("J")); // Invalid (The size of "country" must be greater than or equal to 2. The given size is 1)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.greaterThanOrEqual` | `The size of "{0}" must be greater than or equal to {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `lessThan(int)`

Checks if the target value's size is less than the specified size

```java
Validator<Country> validator = ValidatorBuilder.<Country> of()
    .constraint(Country::asString, "country", c -> c.lessThan(4))
    .build();

ConstraintViolations violations = validator.validate(new Country("JP")); // Valid
ConstraintViolations violations = validator.validate(new Country(null)); // Valid
ConstraintViolations violations = validator.validate(new Country("Japan")); // Invalid (The size of "country" must be less than 4. The given size is 5)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.lessThan` | `The size of "{0}" must be less than {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `lessThanOrEqual(int)`

Checks if the target value's size is less than or equals to the specified size

```java
Validator<Country> validator = ValidatorBuilder.<Country> of()
    .constraint(Country::asString, "country", c -> c.lessThanOrEqual(4))
    .build();

ConstraintViolations violations = validator.validate(new Country("JP")); // Valid
ConstraintViolations violations = validator.validate(new Country(null)); // Valid
ConstraintViolations violations = validator.validate(new Country("Japan")); // Invalid (The size of "country" must be less than or equal to 4. The given size is 5)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.lessThanOrEqual` | `The size of "{0}" must be less than or equal to {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `contains(CharSequence)`

Checks if the target value contains the specified sequence of char values

```java
Validator<ZipCode> validator = ValidatorBuilder.<ZipCode> of()
    .constraint(ZipCode::asString, "zipCode", c -> c.contains("-"))
    .build();

ConstraintViolations violations = validator.validate(new ZipCode("123-4567")); // Valid
ConstraintViolations violations = validator.validate(new ZipCode(null)); // Valid
ConstraintViolations violations = validator.validate(new ZipCode("1234567")); // Invalid ("zipCode" must contain -)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.contains` | `"{0}" must contain {1}` | - `{0}` ... field name<br>- `{1}` ... the specified char sequence<br>- `{2}` ... the given value |

### `startsWith(CharSequence)`

Checks if the target value starts with the specified prefix

```java
Validator<Code> validator = ValidatorBuilder.<Code> of()
    .constraint(Code::asString, "code", c -> c.startsWith("JP-"))
    .build();

ConstraintViolations violations = validator.validate(new Code("JP-1234")); // Valid
ConstraintViolations violations = validator.validate(new Code(null)); // Valid
ConstraintViolations violations = validator.validate(new Code("US-1234")); // Invalid ("code" must start with "JP-")
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.startsWith` | `"{0}" must start with "{1}"` | - `{0}` ... field name<br>- `{1}` ... the specified prefix<br>- `{2}` ... the given value |

### `endsWith(CharSequence)`

Checks if the target value ends with the specified suffix

```java
Validator<Domain> validator = ValidatorBuilder.<Domain> of()
    .constraint(Domain::asString, "domain", c -> c.endsWith(".example.com"))
    .build();

ConstraintViolations violations = validator.validate(new Domain("www.example.com")); // Valid
ConstraintViolations violations = validator.validate(new Domain(null)); // Valid
ConstraintViolations violations = validator.validate(new Domain("example.com")); // Invalid ("domain" must end with ".example.com")
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.endsWith` | `"{0}" must end with "{1}"` | - `{0}` ... field name<br>- `{1}` ... the specified suffix<br>- `{2}` ... the given value |

### `pattern(String)`

Checks if the target value matches the specified regular expression

```java
Validator<ZipCode> validator = ValidatorBuilder.<ZipCode> of()
    .constraint(ZipCode::asString, "zipCode", c -> c.pattern("[0-9]{3}-[0-9]{4}"))
    .build();

ConstraintViolations violations = validator.validate(new ZipCode("123-4567")); // Valid
ConstraintViolations violations = validator.validate(new ZipCode(null)); // Valid
ConstraintViolations violations = validator.validate(new ZipCode("1234567")); // Invalid ("zipCode" must match [0-9]{3}-[0-9]{4})
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.pattern` | `"{0}" must match {1}` | - `{0}` ... field name<br>- `{1}` ... the specified regular expression<br>- `{2}` ... the given value |

### `pattern(Pattern)` / `pattern(Supplier<Pattern>)`

Checks if the target value matches the specified regular expression, in the same way as `pattern(String)`.
The `Supplier<Pattern>` overload invokes the supplier for every validation, which is intended for regular expressions that are built dynamically at validation time.

```java
Pattern zipCode = Pattern.compile("[0-9]{3}-[0-9]{4}");
Validator<ZipCode> validator = ValidatorBuilder.<ZipCode> of()
    .constraint(ZipCode::asString, "zipCode", c -> c.pattern(zipCode))
    .build();

Validator<ZipCode> dynamicValidator = ValidatorBuilder.<ZipCode> of()
    .constraint(ZipCode::asString, "zipCode", c -> c.pattern(() -> Pattern.compile("[0-9]{3}-[0-9]{4}")))
    .build();

ConstraintViolations violations = validator.validate(new ZipCode("123-4567")); // Valid
ConstraintViolations violations = validator.validate(new ZipCode(null)); // Valid
ConstraintViolations violations = validator.validate(new ZipCode("1234567")); // Invalid ("zipCode" must match [0-9]{3}-[0-9]{4})
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.pattern` | `"{0}" must match {1}` | - `{0}` ... field name<br>- `{1}` ... the specified regular expression<br>- `{2}` ... the given value |

### `email()`

Checks if the target value is a valid email address

```java
Validator<Email> validator = ValidatorBuilder.<Email> of()
    .constraint(Email::asString, "email", c -> c.email())
    .build();

ConstraintViolations violations = validator.validate(new Email("yavi@example.com")); // Valid
ConstraintViolations violations = validator.validate(new Email(null)); // Valid
ConstraintViolations violations = validator.validate(new Email("example.com")); // Invalid ("email" must be a valid email address)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.email` | `"{0}" must be a valid email address` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `uuid()`

Checks if the target value is a valid UUID in the 8-4-4-4-12 hexadecimal format. The version and the variant are not checked, and an empty value is valid.

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(User::getId, "id", c -> c.uuid())
    .build();

ConstraintViolations violations = validator.validate(new User("123e4567-e89b-12d3-a456-426614174000")); // Valid
ConstraintViolations violations = validator.validate(new User(null)); // Valid
ConstraintViolations violations = validator.validate(new User("")); // Valid
ConstraintViolations violations = validator.validate(new User("1234")); // Invalid ("id" must be a valid UUID)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.uuid` | `"{0}" must be a valid UUID` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `password(...)`

Check if the target value meets the specified password policy

```java
Validator<Password> validator = ValidatorBuilder.<Password> of()
		.constraint(Password::value, "password", c -> c.password(policy -> policy
				.uppercase()
				.lowercase()
				// or .required(PasswordPolicy.UPPERCASE, PasswordPolicy.LOWERCASE)
				.optional(1, PasswordPolicy.NUMBERS, PasswordPolicy.SYMBOLS)
				.build()))
		.build();

ConstraintViolations violations = validator.validate(new Password("Yavi123")); // Valid
ConstraintViolations violations = validator.validate(new Password(null)); // Valid
ConstraintViolations violations = validator.validate(new Password("yavi123")); // Invalid ("password" must meet Uppercase policy)
ConstraintViolations violations = validator.validate(new Password("yavi")); // Invalid ("password" must meet Uppercase policy, "password" must meet at least 1 policies from [Numbers, Symbols])
ConstraintViolations violations = validator.validate(new Password("")); // Invalid ("password" must meet Uppercase policy, "password" must meet Lowercase policy, "password" must meet at least 1 policies from [Numbers, Symbols])
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `password.required` | `"{0}" must meet {1} policy` | - `{0}` ... field name<br>- `{1}` ... the specified policy name<br>- `{2}` ... the given value |
| `password.optional` | `"{0}" must meet at least {1} policies from {2}` | - `{0}` ... field name<br>- `{1}` ... minimum requirement<br>- `{2}` ... the specified policy names<br>- `{3}` ... the given value |

Buit-in password policies are following:

* `am.ik.yavi.constraint.password.PasswordPolicy#UPPERCASE`
* `am.ik.yavi.constraint.password.PasswordPolicy#LOWERCASE`
* `am.ik.yavi.constraint.password.PasswordPolicy#ALPHABETS`
* `am.ik.yavi.constraint.password.PasswordPolicy#NUMBERS`
* `am.ik.yavi.constraint.password.PasswordPolicy#SYMBOLS`

You can specify the count of the pattern as follows:

```java
Validator<Password> validator = ValidatorBuilder.<Password> of()
		.constraint(Password::value, "password", c -> c.password(policy -> policy
				.uppercase(2) // at least 2 upper case characters are required
				.lowercase(2) // at least 2 lower case characters are required
				// or .required(PasswordPolicy.UPPERCASE.count(2), PasswordPolicy.LOWERCASE.count(2))
				.build()))
		.build();
```

You can define a custom password policy as bellow:

```java
PasswordPolicy<String> passwordPolicy = new PasswordPolicy<>() {
	@Override
	public String name() {
		return "DoNotIncludePassword";
	}

	@Override
	public boolean test(String s) {
		return !s.equalsIgnoreCase("password");
	}
};


Validator<Password> validator = ValidatorBuilder.<Password> of()
		.constraint(Password::value, "password", c -> c.password(policy -> policy
				.required(passwordPolicy)
				// ...
				.build()))
		.build();
```

### `ipv4()`

Check if the target value is a valid IPv4 address

```java
Validator<IpAddress> validator = ValidatorBuilder.<IpAddress> of()
    .constraint(IpAddress::asString, "ipAddress", c -> c.ipv4())
    .build();

ConstraintViolations violations = validator.validate(new IpAddress("192.0.2.1")); // Valid
ConstraintViolations violations = validator.validate(new IpAddress(null)); // Valid
ConstraintViolations violations = validator.validate(new IpAddress("example.com")); // Invalid ("ipAddress" must be a valid IPv4)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.ipv4` | `"{0}" must be a valid IPv4` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `ipv6()`

Check if the target value is a valid IPv6 address

```java
Validator<IpAddress> validator = ValidatorBuilder.<IpAddress> of()
    .constraint(IpAddress::asString, "ipAddress", c -> c.ipv6())
    .build();

ConstraintViolations violations = validator.validate(new IpAddress("2001:0db8:bd05:01d2:288a:1fc0:0001:10ee")); // Valid
ConstraintViolations violations = validator.validate(new IpAddress(null)); // Valid
ConstraintViolations violations = validator.validate(new IpAddress("192.0.2.1")); // Invalid ("ipAddress" must be a valid IPv6)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.ipv6` | `"{0}" must be a valid IPv6` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `url()`

Check if the target value is a valid URL. An empty value is valid.

> **NOTE:** Since 0.17.0 the value is parsed as a `java.net.URI` instead of the deprecated `URL(String)` constructor, which silently strips leading and trailing characters. Values that only passed because of that trimming, values that contain unencoded characters, and values with an empty authority are now rejected.

```java
Validator<Url> validator = ValidatorBuilder.<Url> of()
    .constraint(Url::asString, "url", c -> c.url())
    .build();

ConstraintViolations violations = validator.validate(new Url("https://yavi.ik.am")); // Valid
ConstraintViolations violations = validator.validate(new Url(null)); // Valid
ConstraintViolations violations = validator.validate(new Url("yavi.ik.am")); // Invalid ("url" must be a valid URL)
ConstraintViolations violations = validator.validate(new Url(" https://yavi.ik.am ")); // Invalid ("url" must be a valid URL)
ConstraintViolations violations = validator.validate(new Url("https://yavi.ik.am/path with space")); // Invalid ("url" must be a valid URL)
ConstraintViolations violations = validator.validate(new Url("https://")); // Invalid ("url" must be a valid URL)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.url` | `"{0}" must be a valid URL` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `url(...)`

Check if the target value is a valid URL and restrict its protocol, host and/or port.
The value is first required to be a valid URL, and an unexpected attribute is reported in addition to any URL violation.
This is the equivalent of Bean Validation's `@URL(protocol = "https", host = "example.com", port = 8443)`.
An empty value is valid.

Hosts are compared case-insensitively but matched as written, so `host("example.com")` does not accept `https://www.example.com`.
A URL without an explicit port is matched against the default port of its protocol, so `port(443)` accepts `https://example.com`.
If the value cannot be parsed as a URL, only `charSequence.url` is reported.

```java
Validator<Url> validator = ValidatorBuilder.<Url> of()
    .constraint(Url::asString, "url", c -> c.url(url -> url
            .protocol("https")
            .host("example.com")
            .port(8443)
            .build()))
    .build();

ConstraintViolations violations = validator.validate(new Url("https://example.com:8443/app")); // Valid
ConstraintViolations violations = validator.validate(new Url("http://example.com:8443/app")); // Invalid (The protocol of "url" must be one of the following values: [https])
ConstraintViolations violations = validator.validate(new Url("https://other.example.com:8443/app")); // Invalid (The host of "url" must be one of the following values: [example.com])
ConstraintViolations violations = validator.validate(new Url("https://example.com:9999/app")); // Invalid (The port of "url" must be one of the following values: [8443])
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.url` | `"{0}" must be a valid URL` | - `{0}` ... field name<br>- `{1}` ... the given value |
| `charSequence.url.protocol` | `The protocol of "{0}" must be one of the following values: {1}` | - `{0}` ... field name<br>- `{1}` ... the specified protocols<br>- `{2}` ... the given value |
| `charSequence.url.host` | `The host of "{0}" must be one of the following values: {1}` | - `{0}` ... field name<br>- `{1}` ... the specified hosts<br>- `{2}` ... the given value |
| `charSequence.url.port` | `The port of "{0}" must be one of the following values: {1}` | - `{0}` ... field name<br>- `{1}` ... the specified ports<br>- `{2}` ... the given value |

### `luhn()`

Checks if the digits within the target value pass the Luhn checksum algorithm (see also [Luhn algorithm](http://en.wikipedia.org/wiki/Luhn_algorithm)).

```java
Validator<CreditCard> validator = ValidatorBuilder.<CreditCard> of()
    .constraint(CreditCard::number, "creditCardNumber", c -> c.luhn())
    .build();

ConstraintViolations violations = validator.validate(new CreditCard("4111111111111111")); // Valid
ConstraintViolations violations = validator.validate(new CreditCard(null)); // Valid
ConstraintViolations violations = validator.validate(new CreditCard("4111111111111112")); // Invalid (the check digit for "creditCardNumber" is invalid, Luhn checksum failed)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.luhn` | `the check digit for "{0}" is invalid, Luhn checksum failed` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isByte()`

Check if the target value can be parsed as a `Byte` value

```java
Validator<UserId> validator = ValidatorBuilder.<UserId> of()
    .constraint(UserId::asString, "userId", c -> c.isByte())
    .build();

ConstraintViolations violations = validator.validate(UserId.valueOf("127")); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf("a")); // Invalid ("userId" must be a valid representation of a byte)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.byte` | `"{0}" must be a valid representation of a byte` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isShort()`

Check if the target value can be parsed as a `Short` value

```java
Validator<UserId> validator = ValidatorBuilder.<UserId> of()
    .constraint(UserId::asString, "userId", c -> c.isShort())
    .build();

ConstraintViolations violations = validator.validate(UserId.valueOf("32767")); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf("a")); // Invalid ("userId" must be a valid representation of a short)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.short` | `"{0}" must be a valid representation of a short` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isInteger()`

Check if the target value can be parsed as an `Integer` value

```java
Validator<UserId> validator = ValidatorBuilder.<UserId> of()
    .constraint(UserId::asString, "userId", c -> c.isInteger())
    .build();

ConstraintViolations violations = validator.validate(UserId.valueOf("2147483647")); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf("a")); // Invalid ("userId" must be a valid representation of an integer)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.integer` | `"{0}" must be a valid representation of an integer` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isLong()`

Check if the target value can be parsed as a `Long` value

```java
Validator<UserId> validator = ValidatorBuilder.<UserId> of()
    .constraint(UserId::asString, "userId", c -> c.isLong())
    .build();

ConstraintViolations violations = validator.validate(UserId.valueOf("9223372036854775807")); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf("a")); // Invalid ("userId" must be a valid representation of a long)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.long` | `"{0}" must be a valid representation of a long` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isFloat()`

Check if the target value can be parsed as a `Float` value

```java
Validator<Money> validator = ValidatorBuilder.<Money> of()
    .constraint(Money::asString, "money", c -> c.isFloat())
    .build();

ConstraintViolations violations = validator.validate(Money.valueOf("0.1")); // Valid
ConstraintViolations violations = validator.validate(Money.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(Money.valueOf("a")); // Invalid ("money" must be a valid representation of a float)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.float` | `"{0}" must be a valid representation of a float.` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isDouble()`

Check if the target value can be parsed as a `Double` value

```java
Validator<Money> validator = ValidatorBuilder.<Money> of()
    .constraint(Money::asString, "money", c -> c.isDouble())
    .build();

ConstraintViolations violations = validator.validate(Money.valueOf("0.1")); // Valid
ConstraintViolations violations = validator.validate(Money.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(Money.valueOf("a")); // Invalid ("money" must be a valid representation of a double)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.double` | `"{0}" must be a valid representation of a double` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isBigInteger()`

Check if the target value can be parsed as a `BigInteger` value

```java
Validator<UserId> validator = ValidatorBuilder.<UserId> of()
    .constraint(UserId::asString, "userId", c -> c.isBigInteger())
    .build();

ConstraintViolations violations = validator.validate(UserId.valueOf("127")); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf("a")); // Invalid ("userId" must be a valid representation of a big integer)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.biginteger` | `"{0}" must be a valid representation of a big integer` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isBigDecimal()`

Check if the target value can be parsed as a `BigDecimal` value

```java
Validator<Money> validator = ValidatorBuilder.<Money> of()
    .constraint(Money::asString, "money", c -> c.isBigDecimal())
    .build();

ConstraintViolations violations = validator.validate(Money.valueOf("50.0")); // Valid
ConstraintViolations violations = validator.validate(Money.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(Money.valueOf("a")); // Invalid ("money" must be a valid representation of a big decimal)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.bigdecimal` | `"{0}" must be a valid representation of a big decimal` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isoLocalDate()` / `localDate(String pattern)`

Checks if the target value is a valid local date in the given pattern, resolved with `ResolverStyle.STRICT`.
`isoLocalDate()` uses the pattern `uuuu-MM-dd`.
The formatter is created each time a value is validated.
An empty value is valid.

```java
Validator<BirthDate> validator = ValidatorBuilder.<BirthDate> of()
    .constraint(BirthDate::asString, "birthDate", c -> c.isoLocalDate())
    .build();
Validator<BirthDate> customValidator = ValidatorBuilder.<BirthDate> of()
    .constraint(BirthDate::asString, "birthDate", c -> c.localDate("dd/MM/uuuu"))
    .build();

ConstraintViolations violations = validator.validate(new BirthDate("2024-12-31")); // Valid
ConstraintViolations violations = validator.validate(new BirthDate(null)); // Valid
ConstraintViolations violations = validator.validate(new BirthDate("2024-02-30")); // Invalid ("birthDate" must be a valid representation of a local date using the pattern: uuuu-MM-dd. The give value is: 2024-02-30)
ConstraintViolations violations = customValidator.validate(new BirthDate("31/12/2024")); // Valid
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `charSequence.localdate` | `"{0}" must be a valid representation of a local date using the pattern: {1}. The give value is: {2}` | - `{0}` ... field name<br>- `{1}` ... the specified date pattern<br>- `{2}` ... the given value |

### `codePoints(...)`

Checks if the target value is in the specified set of code points.
Code points can be specified as allowed characters (whitelist) or prohibited characters (blacklist).

A set of code points is represented by `am.ik.yavi.constraint.charsequence.CodePoints` interface, and there is `am.ik.yavi.constraint.charsequence.CodePoints.CodePointsSet` interface that represents the set using `java.util.Set` and `am.ik.yavi.constraint.charsequence.CodePoints.CodePointsRanges` interface that represents a list of code point ranges.

For example, a code point set consisting of "A, B, C, D, a, b, c, d" is expressed as follows:

```java
CodePointsSet<String> codePoints = () -> Set.of(
		0x0041 /* A */, 0x0042 /* B */, 0x0043 /* C */, 0x0044 /* D */,
        0x0061 /* a */, 0x0062 /* b */, 0x0063 /* c */, 0x0064 /* d */);
```

Or:

```java
CodePointsRanges<String> codePoints = () -> List.of(
        Range.of(0x0041/* A */, 0x0044 /* D */),
        Range.of(0x0061/* a */, 0x0064 /* d */));
```

For consecutive code points, the latter is overwhelmingly more memory efficient.

If you want to regard the code point set as a white list (allowed characters), specify as follows:

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.codePoints(codePoints).asWhiteList())
    .build();

ConstraintViolations violations = validator.validate(new Message("aBCd")); // Valid
ConstraintViolations violations = validator.validate(new Message(null)); // Valid
ConstraintViolations violations = validator.validate(new Message("aBCe")); // Invalid ("[e]" is/are not allowed for "text")
```

If you want to regard the code point set as a blacklist (prohibited characters), specify as follows:

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.codePoints(codePoints).asBlackList())
    .build();

ConstraintViolations violations = validator.validate(new Message("hello")); // Valid
ConstraintViolations violations = validator.validate(new Message(null)); // Valid
ConstraintViolations violations = validator.validate(new Message("hallo")); // Invalid ("[a]" is/are not allowed for "text")
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `codePoints.asWhiteList` | `"{1}" is/are not allowed for "{0}"` | - `{0}` ... field name<br>- `{1}` ... the violated value |
| `codePoints.asBlackList` | `"{1}" is/are not allowed for "{0}"` | - `{0}` ... field name<br>- `{1}` ... the violated value |

The following is a set of built-in code points.

* `am.ik.yavi.constraint.charsequence.codepoints.AsciiCodePoints#ASCII_PRINTABLE_CHARS` ... ASCII printable characters
* `am.ik.yavi.constraint.charsequence.codepoints.AsciiCodePoints#ASCII_CONTROL_CHARS` ... ASCII control characters
* `am.ik.yavi.constraint.charsequence.codepoints.AsciiCodePoints#CRLF` ... `0x000A` (LINE FEED) and `0x000D` (CARRIAGE RETURN)
* `am.ik.yavi.constraint.charsequence.codepoints.UnicodeCodePoints#HIRAGANA` ... [Hiragana](https://www.unicode.org/charts/nameslist/c_3040.html) defined in Unicode (different from JIS X 0208 definition)
* `am.ik.yavi.constraint.charsequence.codepoints.UnicodeCodePoints#KATAKANA` ... [Katakana](https://www.unicode.org/charts/nameslist/c_30A0.html) and [Katakana Phonetic Extensions](https://www.unicode.org/charts/nameslist/c_31F0.html) defined in Unicode (different from JIS X 0208 definition)

You can also represent the union of multiple code point sets with `am.ik.yavi.constraint.charsequence.codepoints.CompositeCodePoints` class.

### Advanced character length check

YAVI counts the input characters as the number of characters as it looks in the constraints on the number of characters. Recently, as a result of defining various characters on Unicode, the visual size and the return value of `String#length` method are quite different.

YAVI supports the following character types:

* Surrogate pair
* Combining character
* Variation Selector
  * IVS (Ideographic Variation Sequence)
  * SVS (Standardized Variation Sequence)
  * FVS (Mongolian Free Variation Selector)
* Emoji

YAVI will perform a constraint check on surrogate pairs and combining characters with the number of characters as they look. (For Emoji, IVS, SVS and FVS, the size is not checked as it looks by default due to the performance.)

Let's look at a typical example.

#### Surrogate pair

`𠮷野屋` is an example of a surrogate pair. It looks like 3 characters, but the result of `length` method is 4 (`\uD842\uDFB7野屋`).

```java
System.out.println("𠮷野屋".length()); // 4 (\uD842\uDFB7野屋)
```

Since YAVI treats the character length as the code point length, this character string is regarded as 3 characters.

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.lessThanOrEqual(3))
    .build();

ConstraintViolations violations = validator.validate(new Message("𠮷野屋")); // Valid
```

#### Combining character

`モジ` is an example of a combining character. Although it looks like 2 characters, `シ` and dakuten(`゙`) are combined, and the result of `length()` is 3 (`モシ\u3099`).

```java
System.out.println("モジ".length()); // 3 (モシ\u3099)
```

YAVI considers this string to be 2 characters by default.

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.lessThanOrEqual(2))
    .build();

ConstraintViolations violations = validator.validate(new Message("モジ")); // Valid
```

YAVI uses `java.text.Normalizer` and normalizes with `java.text.Normalizer.Form#NFC` by default. This behavior can be changed as follows: (If `null` is set, it will not be normalized)

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.normalizer(normalizerForm)
				.lessThanOrEqual(2))
    .build();
```

#### Variation Selector

`𠮟󠄀` is an example of an Ideographic Variation Sequence. Variant selectors cannot be normalized with `Normalizer`.
It looks like 1 character, but when expressed in UTF-16, it is "D842 DF9F DB40 DD00", so the result of `length()` is 4.

```java
System.out.println("𠮟󠄀".length()); // 4 (\uD842\uDF9F\uDB40\uDD00󠄀)
```

YAVI does *not* consider this character as a single character by default to prevent performance degradation.

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
		.constraint(Message::getText, "text", c -> c.lessThanOrEqual(1))
		.build();
ConstraintViolations violations = validator.validate(new Message("𠮟󠄀")); // Invalid (The size of "text" must be less than or equal to 1. The given size is 2)
```

You can ignore (delete) the code point of `0xE0100`-`0xE01EF`, which is the range of IVS from the target string, as follows.
This way you can regard this string as just a surrogate pair.

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
		.constraint(Message::getText, "text", c -> c.variant(opts -> opts.ivs(IdeographicVariationSequence.IGNORE))
				.lessThanOrEqual(1))
		.build();
ConstraintViolations violations = validator.validate(new Message("𠮟󠄀")); // Valid
```

SVS and FVS can be handled in the same way.

#### Emoji

Emoji is crazy. The apparent number of characters and the number of code points are far away.

Let me give you examples.

```java
System.out.println("❤️".length()); // 2
System.out.println("🤴🏻".length()); // 4
System.out.println("👨‍👦".length()); // 5
System.out.println("️👨‍👨‍👧‍👦".length()); // 12
System.out.println("🏴󠁧󠁢󠁥󠁮󠁧󠁿".length()); // 14 (WTH!)
```

YAVI can try to count these Emojis as one character as much as possible. There is no guarantee, but all emojis defined in Emoji 12.0 have been tested.

This process is expensive and is *not* enabled by default. To enable this feature, specify the `emoji()` method as follows:

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.emoji().lessThanOrEqual(1))
    .build();
ConstraintViolations violations = validator.validate(new Message("❤️")); // Valid
violations = validator.validate(new Message("🤴🏻")); // Valid
violations = validator.validate(new Message("👨‍👦")); // Valid
violations = validator.validate(new Message("️👨‍👨‍👧‍👦")); // Valid
violations = validator.validate(new Message("🏴󠁧󠁢󠁥󠁮󠁧󠁿")); // Valid
```


### `variant(...)`

Returns a constraint that removes the selected variation selectors before the size is measured.
This does not change the value itself; it only affects how the size is counted.
See [Advanced character length check](#advanced-character-length-check) for the details of IVS, SVS and FVS.

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.variant(opts -> opts.fvs(MongolianFreeVariationSelector.IGNORE))
            .fixedSize(1))
    .build();

ConstraintViolations violations = validator.validate(new Message("ᠠ᠋")); // Valid
```

`variant(...)` is not a constraint itself and has no message of its own; the message of the size constraint applied to its result is reported.

### `asByteArray()`

If there is a discrepancy between the apparent character length and the actual code point length, the appearance restrictions are OK, but the size stored in the database may be exceeded. In YAVI, you can check the byte length in addition to the visual size.


```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.emoji().lessThanOrEqual(3)
            .asByteArray().lessThanOrEqual(16))
    .build();

ConstraintViolations violations = validator.validate(new Message("I❤️☕️️")); // Valid
violations = validator.validate(new Message("❤️❤️❤️️️")); // Invalid (The byte size of "text" must be less than or equal to 16. The given size is 24)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `byteSize.lessThan` | `The byte size of "{0}" must be less than {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |
| `byteSize.lessThanOrEqual` | `The byte size of "{0}" must be less than or equal to {1}. The given size is {2}"` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |
| `byteSize.greaterThan` | `The byte size of "{0}" must be greater than {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |
| `byteSize.greaterThanOrEqual` | `The byte size of "{0}" must be greater than or equal to {1}. The given size is {2}"` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |
| `byteSize.fixedSize` | `The byte size of "{0}" must be {1}. The given size is {2}"` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

## Integer/Short/Long/Character/Byte/Float/Long/BigInteger/BigDecimal

Apply the constraints on the `Integer` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraint(/* Function from TargetClass to Integer  */, /* Field Name */, /* Constraints */)
    .build();
```

To avoid ambiguous type inferences, you can use explicit `_integer` instead of `constraint` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._integer(/* Function from TargetClass to Integer  */, /* Field Name */, /* Constraints */)
    .build();
```

The same goes for `Short`/`Long`/`Character`/`Byte`/`Float`/`Long` / `BigInteger` / `BigDecimal`.

### `notNull()`

Checks that the target value is not `null`

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.notNull())
    .build();

ConstraintViolations violations = validator.validate(new Age(30)); // Valid
ConstraintViolations violations = validator.validate(new Age(null)); // Invalid ("age" must not be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isNull()`

Checks that the target value is `null`

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.isNull())
    .build();

ConstraintViolations violations = validator.validate(new Age(null)); // Valid
ConstraintViolations violations = validator.validate(new Age(30)); // Invalid ("age" must be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.isNull` | `"{0}" must be null` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `greaterThan(...)`

Checks if the target value is greater than the specified value

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.greaterThan(20))
    .build();

ConstraintViolations violations = validator.validate(new Age(21)); // Valid
ConstraintViolations violations = validator.validate(new Age(null)); // Valid
ConstraintViolations violations = validator.validate(new Age(20)); // Invalid ("age" must be greater than 20)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `numeric.greaterThan` | `"{0}" must be greater than {1}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given value |

### `greaterThanOrEqual(...)`

Checks if the target value is greater than or equals to the specified value

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.greaterThanOrEqual(20))
    .build();

ConstraintViolations violations = validator.validate(new Age(20)); // Valid
ConstraintViolations violations = validator.validate(new Age(null)); // Valid
ConstraintViolations violations = validator.validate(new Age(19)); // Invalid ("age" must be greater than or equal to 20)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `numeric.greaterThanOrEqual` | `"{0}" must be greater than or equal to {1}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given value |

### `lessThan(...)`

Checks if the target value is less than the specified value

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.lessThan(20))
    .build();

ConstraintViolations violations = validator.validate(new Age(19)); // Valid
ConstraintViolations violations = validator.validate(new Age(null)); // Valid
ConstraintViolations violations = validator.validate(new Age(20)); // Invalid ("age" must be less than 20)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `numeric.lessThan` | `"{0}" must be less than {1}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given value |

### `lessThanOrEqual(...)`

Checks if the target value is less than or equals to the specified value

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.lessThanOrEqual(20))
    .build();

ConstraintViolations violations = validator.validate(new Age(19)); // Valid
ConstraintViolations violations = validator.validate(new Age(null)); // Valid
ConstraintViolations violations = validator.validate(new Age(21)); // Invalid ("age" must be less than or equal to 20)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `numeric.lessThanOrEqual` | `"{0}" must be less than or equal to {1}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given value |

### `positive()` / `positiveOrZero()` / `negative()` / `negativeOrZero()`

Checks if the target value is positive, positive or zero, negative, or negative or zero, respectively.
These constraints apply to `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`, `BigInteger` and `BigDecimal`.

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.positive())
    .build();

ConstraintViolations violations = validator.validate(new Age(1)); // Valid
ConstraintViolations violations = validator.validate(new Age(null)); // Valid
ConstraintViolations violations = validator.validate(new Age(0)); // Invalid ("age" must be positive)
```

`positiveOrZero`, `negative` and `negativeOrZero` are used in the same way.

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `numeric.positive` | `"{0}" must be positive` | - `{0}` ... field name<br>- `{1}` ... the given value |
| `numeric.positiveOrZero` | `"{0}" must be positive or zero` | - `{0}` ... field name<br>- `{1}` ... the given value |
| `numeric.negative` | `"{0}" must be negative` | - `{0}` ... field name<br>- `{1}` ... the given value |
| `numeric.negativeOrZero` | `"{0}" must be negative or zero` | - `{0}` ... field name<br>- `{1}` ... the given value |

The constraints on this page do not have to be used one at a time. The program below combines string and numeric constraints on an account and prints the violations. Press Run to compile and execute it in your browser; the output appears below the cell.

```java runnable
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

public class Main {

    record Account(String username, String email, int age) {
    }

    static final Validator<Account> validator = ValidatorBuilder.<Account>of()
        .constraint(Account::username, "username",
                c -> c.notBlank().pattern("[a-z0-9_]{3,16}"))
        .constraint(Account::email, "email", c -> c.notBlank().email())
        .constraint(Account::age, "age", c -> c.greaterThanOrEqual(18).lessThanOrEqual(120))
        .build();

    public static void main(String[] args) {
        ConstraintViolations violations = validator
            .validate(new Account("A!", "not-an-email", 17));
        System.out.println("violations: " + violations.size());
        violations.forEach(v -> System.out.println("- " + v.message()));

        System.out.println("adult account is valid: "
                + validator.validate(new Account("john_doe", "jdoe@example.com", 30)).isValid());
    }
}
```
<!-- run: expected output
violations: 3
- "username" must match [a-z0-9_]{3,16}
- "email" must be a valid email address
- "age" must be greater than or equal to 18
adult account is valid: true
-->

## Temporal

YAVI provides temporal constraints for the following types, with the corresponding builder shortcuts:

| Type | Constraint | Builder shortcut |
| --- | --- | --- |
| `LocalDate` | `am.ik.yavi.constraint.LocalDateConstraint` | `_localDate` |
| `LocalTime` | `am.ik.yavi.constraint.LocalTimeConstraint` | `_localTime` |
| `LocalDateTime` | `am.ik.yavi.constraint.LocalDateTimeConstraint` | `_localDateTime` |
| `OffsetDateTime` | `am.ik.yavi.constraint.OffsetDateTimeConstraint` | `_offsetDateTime` |
| `Instant` | `am.ik.yavi.constraint.InstantConstraint` | `_instant` |
| `Year` | `am.ik.yavi.constraint.YearConstraint` | `_year` |
| `YearMonth` | `am.ik.yavi.constraint.YearMonthConstraint` | `_yearMonth` |
| `ZonedDateTime` | `am.ik.yavi.constraint.ZonedDateTimeConstraint` | `_zonedDateTime` |

Custom `TemporalAccessor` types can be supported by extending `TemporalConstraintBase` through `ChronoLocalDateConstraintBase`, `ChronoLocalDateTimeConstraintBase` or `ChronoZonedDateTimeConstraintBase`.
The Kotlin DSL provides matching property overloads (see the Kotlin Support section).

The following constraints apply to every type above:

```java
Validator<Event> validator = ValidatorBuilder.<Event> of()
    ._localDate(Event::getDate, "date", c -> c.past())
    .build();
```

A `null` value is valid and skips all temporal checks.

`before` and `after` reject a value that is equal to the boundary; use `beforeOrEqual` and `afterOrEqual` for inclusive checks.
The arguments are always suppliers.

`before`, `beforeOrEqual`, `after`, `afterOrEqual` and `between` have an overload that takes a `boolean memoize` argument.
When `memoize` is `true`, which is the default, the supplier is called at most once and the same value is used for both the comparison and the violation message.
When it is `false`, the supplier is called on every use, so a supplier that returns a different value on each call may compare against one value and report another in the message.
Use `false` for suppliers that must be evaluated at validation time.

The `past`, `pastOrPresent`, `future` and `futureOrPresent` methods also have overloads that take a `java.time.Clock`.
These overloads exist for deterministic tests and for validating against a time source other than the system default zone.

### `past()` / `pastOrPresent()` / `future()` / `futureOrPresent()`

Checks if the target value is in the past, in the past or present, in the future, or in the present or future, respectively.
The overloads take a `Clock` used as the current time.

```java
Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"));
Validator<Event> validator = ValidatorBuilder.<Event> of()
    ._localDate(Event::getDate, "date", c -> c.past(clock))
    .build();

ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2023, 12, 31))); // Valid
ConstraintViolations violations = validator.validate(new Event(null)); // Valid
ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 1, 1))); // Invalid ("date" must be a past date)
ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 1, 2))); // Invalid ("date" must be a past date)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `temporal.past` | `"{0}" must be a past date` | - `{0}` ... field name<br>- `{1}` ... the current date used for the comparison<br>- `{2}` ... the given value |
| `temporal.pastOrPresent` | `"{0}" must be a date in the past or in the present` | - `{0}` ... field name<br>- `{1}` ... the current date used for the comparison<br>- `{2}` ... the given value |
| `temporal.future` | `"{0}" must be a future date` | - `{0}` ... field name<br>- `{1}` ... the current date used for the comparison<br>- `{2}` ... the given value |
| `temporal.futureOrPresent` | `"{0}" must be a date in the present or in the future` | - `{0}` ... field name<br>- `{1}` ... the current date used for the comparison<br>- `{2}` ... the given value |

### `before(...)` / `beforeOrEqual(...)` / `after(...)` / `afterOrEqual(...)`

Checks if the target value is before, before or equal to, after, or after or equal to the temporal supplied by the given supplier.

```java
Validator<Event> validator = ValidatorBuilder.<Event> of()
    ._localDate(Event::getDate, "date", c -> c.before(() -> LocalDate.of(2024, 6, 1)))
    .build();

ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 5, 31))); // Valid
ConstraintViolations violations = validator.validate(new Event(null)); // Valid
ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 6, 1))); // Invalid ("date" has to be before 2024-06-01)
ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 6, 2))); // Invalid ("date" has to be before 2024-06-01)
```

The `beforeOrEqual`, `after` and `afterOrEqual` variants are used in the same way.

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `temporal.before` | `"{0}" has to be before {1}` | - `{0}` ... field name<br>- `{1}` ... the supplied boundary value<br>- `{2}` ... the given value |
| `temporal.beforeOrEqual` | `"{0}" has to be before or equals to {1}` | - `{0}` ... field name<br>- `{1}` ... the supplied boundary value<br>- `{2}` ... the given value |
| `temporal.after` | `"{0}" has to be after {1}` | - `{0}` ... field name<br>- `{1}` ... the supplied boundary value<br>- `{2}` ... the given value |
| `temporal.afterOrEqual` | `"{0}" has to be after or equals to {1}` | - `{0}` ... field name<br>- `{1}` ... the supplied boundary value<br>- `{2}` ... the given value |

### `between(...)`

Checks if the target value is between the temporals supplied by the given suppliers.
The range is exclusive on both ends: a value equal to either bound is invalid.
`IllegalArgumentException` is thrown at validation time if `rangeFrom` is after `rangeTo`.

```java
Validator<Event> validator = ValidatorBuilder.<Event> of()
    ._localDate(Event::getDate, "date", c -> c.between(
            () -> LocalDate.of(2024, 1, 1), () -> LocalDate.of(2024, 12, 31)))
    .build();

ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 6, 1))); // Valid
ConstraintViolations violations = validator.validate(new Event(null)); // Valid
ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 1, 1))); // Invalid ("date" has to be between 2024-01-01 and 2024-12-31)
ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2025, 1, 1))); // Invalid ("date" has to be between 2024-01-01 and 2024-12-31)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `temporal.between` | `"{0}" has to be between {1} and {2}` | - `{0}` ... field name<br>- `{1}` ... the supplied start of the range<br>- `{2}` ... the supplied end of the range<br>- `{3}` ... the given value |

### `fieldPredicate(...)`

Checks if the given predicate accepts the value of the given `java.time.temporal.TemporalField`.

```java
Validator<Event> validator = ValidatorBuilder.<Event> of()
    ._localDate(Event::getDate, "date", c -> c.fieldPredicate(ChronoField.DAY_OF_WEEK, d -> d >= 1 && d <= 5))
    .build();

ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 6, 3))); // Valid
ConstraintViolations violations = validator.validate(new Event(null)); // Valid
ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 6, 1))); // Invalid (The DayOfWeek of "date" is invalid)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `temporal.field` | `The {1} of "{0}" is invalid` | - `{0}` ... field name<br>- `{1}` ... the temporal field<br>- `{2}` ... the given value |

The program below validates booking dates against a fixed clock, so the result does not depend on when it is run. It prints the violations. Press Run to compile and execute it in your browser; the output appears below the cell.

```java runnable
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

public class Main {

    record Booking(LocalDate checkIn, LocalDate checkOut) {
    }

    static final Clock clock = Clock.fixed(Instant.parse("2024-06-01T00:00:00Z"),
            ZoneOffset.UTC);

    static final Validator<Booking> validator = ValidatorBuilder.<Booking>of()
        ._localDate(Booking::checkIn, "checkIn", c -> c.futureOrPresent(clock))
        ._localDate(Booking::checkOut, "checkOut",
                c -> c.after(() -> LocalDate.of(2024, 6, 1)))
        .build();

    public static void main(String[] args) {
        ConstraintViolations violations = validator
            .validate(new Booking(LocalDate.of(2024, 5, 31), LocalDate.of(2024, 5, 31)));
        System.out.println("violations: " + violations.size());
        violations.forEach(v -> System.out.println("- " + v.message()));

        System.out.println("June booking is valid: " + validator
            .validate(new Booking(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30)))
            .isValid());
    }
}
```
<!-- run: expected output
violations: 2
- "checkIn" must be a date in the present or in the future
- "checkOut" has to be after 2024-06-01
June booking is valid: true
-->

## Boolean

Apply the constraints on the `Boolean` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraint(/* Function from TargetClass to Boolean  */, /* Field Name */, /* Constraints */)
    .build();
```

To avoid ambiguous type inferences, you can use explicit `_boolean` instead of `constraint` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._boolean(/* Function from TargetClass to Boolean  */, /* Field Name */, /* Constraints */)
    .build();
```

### `notNull()`

Checks that the target value is not `null`

```java
Validator<Confirmation> validator = ValidatorBuilder.<Confirmation> of()
    .constraint(Confirmation::isConfirmed, "confirmed", c -> c.notNull())
    .build();

ConstraintViolations violations = validator.validate(new Confirmation(true)); // Valid
ConstraintViolations violations = validator.validate(new Confirmation(null)); // Invalid ("confirmed" must not be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isNull()`

Checks that the target value is `null`

```java
Validator<Confirmation> validator = ValidatorBuilder.<Confirmation> of()
    .constraint(Confirmation::isConfirmed, "confirmed", c -> c.isNull())
    .build();

ConstraintViolations violations = validator.validate(new Confirmation(null)); // Valid
ConstraintViolations violations = validator.validate(new Confirmation(true)); // Invalid ("confirmed" must be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.isNull` | `"{0}" must be null` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isTrue()`

Checks that the target value is `true`

```java
Validator<Confirmation> validator = ValidatorBuilder.<Confirmation> of()
    .constraint(Confirmation::isConfirmed, "confirmed", c -> c.isTrue())
    .build();

ConstraintViolations violations = validator.validate(new Confirmation(true)); // Valid
ConstraintViolations violations = validator.validate(new Confirmation(null)); // Valid
ConstraintViolations violations = validator.validate(new Confirmation(false)); // Invalid ("confirmed" must be true)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `boolean.isTrue` | `"{0}" must be true` | - `{0}` ... field name<br>- `{1}` ... the given value |


### `isFalse()`

Checks that the target value is `false`

```java
Validator<Rented> validator = ValidatorBuilder.<Rented> of()
    .constraint(Rented::isRented, "rented", c -> c.isFalse())
    .build();

ConstraintViolations violations = validator.validate(new Rented(false)); // Valid
ConstraintViolations violations = validator.validate(new Rented(null)); // Valid
ConstraintViolations violations = validator.validate(new Rented(true)); // Invalid ("rented" must be false)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `boolean.isFalse` | `"{0}" must be false` | - `{0}` ... field name<br>- `{1}` ... the given value |

## Collection

Apply the constraints on the `Collection` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraint(/* Function from TargetClass to Collection  */, /* Field Name */, /* Constraints */)
    .build();
```

To avoid ambiguous type inferences, you can use explicit `_collection` instead of `constraint` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._collection(/* Function from TargetClass to Collection  */, /* Field Name */, /* Constraints */)
    .build();
```

### `notNull()`

Checks that the target value is not `null`

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.notNull())
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Invalid ("histories" must not be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isNull()`

Checks that the target value is `null`

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
    .constraint(Histories::asList, "histories", c -> c.isNull())
    .build();

ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Invalid ("histories" must be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.isNull` | `"{0}" must be null` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `notEmpty()`

Checks that the target value is not empty

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.notEmpty())
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Invalid ("histories" must not be empty)
ConstraintViolations violations = validator.validate(new Histories(List.of())); // Invalid ("histories" must not be empty)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.notEmpty` | `"{0}" must not be empty` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `fixedSize(int)`

Checks if the target value's size is the specified size

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.fixedSize(2))
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History()))); // Invalid (The size of "histories" must be 2. The given size is 1)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.fixedSize` | `The size of "{0}" must be {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `greaterThan(int)`

Checks if the target value's size is greater than the specified size

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.greaterThan(1))
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History()))); // Invalid (The size of "histories" must be greater than 1. The given size is 1)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.greaterThan` | `The size of "{0}" must be greater than {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `greaterThanOrEqual(int)`

Checks if the target value's size is greater than or equals to the specified size

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.greaterThanOrEqual(2))
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History()))); // Invalid (The size of "histories" must be greater than or equal to 2. The given size is 1)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.greaterThanOrEqual` | `The size of "{0}" must be greater than or equal to {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `lessThan(int)`

Checks if the target value's size is less than the specified size

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.lessThan(3))
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History(), new History()))); // Invalid (The size of "histories" must be less than 3. The given size is 3)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.lessThan` | `The size of "{0}" must be less than {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `lessThanOrEqual(int)`

Checks if the target value's size is less than or equals to the specified size

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.lessThanOrEqual(2))
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History(), new History()))); // Invalid (The size of "histories" must be less than or equal to 2. The given size is 3)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.lessThanOrEqual` | `The size of "{0}" must be less than or equal to {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `contains(...)`

Checks if the target value contains the specified value

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.contains(new History(2)))
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(1), new History(2)))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History(3), new History(4), new History(5)))); // Invalid ("histories" must contain History{revision=2})
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `collection.contains` | `"{0}" must contain {1}` | - `{0}` ... field name<br>- `{1}` ... the specified value<br>- `{2}` ... the given value |

### `unique()`

Checks if the elements of the target value are unique.
The violation includes the duplicated elements.

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
    .constraint(Histories::asList, "histories", c -> c.unique())
    .build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(1), new History(2)))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History(1), new History(1)))); // Invalid ("histories" must be unique. [History{revision=1}] is/are duplicated.)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `collection.unique` | `"{0}" must be unique. {1} is/are duplicated.` | - `{0}` ... field name<br>- `{1}` ... the duplicated elements<br>- `{2}` ... the given value |

### `containsAll(Collection)`

Checks if the target value contains all of the given elements.
The violation includes the missing elements.
A `Set` and a non-`Set` collection are handled with the same semantics.

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
    .constraint(Histories::asList, "histories", c -> c.containsAll(List.of(new History(1), new History(2))))
    .build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(1), new History(2), new History(3)))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History(1)))); // Invalid ("histories" must be have of all of the following values: [History{revision=1}, History{revision=2}], missing values: [History{revision=2}])
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `collection.containsAll` | `"{0}" must be have of all of the following values: {1}, missing values: {2}` | - `{0}` ... field name<br>- `{1}` ... the required elements<br>- `{2}` ... the missing elements<br>- `{3}` ... the given value |

### `forEach(...)` / `forEachIfPresent(...)`

`forEach` applies an element validator to each element of a collection property, and `forEachIfPresent` does the same but treats a `null` collection as valid.
A `null` element inside the collection is reported with the element index in the property name, for example `histories[1]`, and the `object.notNull` message.

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
    .forEach(Histories::asList, "histories",
            ValidatorBuilder.<History> of()
                .constraint(History::getRevision, "revision", c -> c.notNull())
                .build())
    .build();

ConstraintViolations violations = validator.validate(new Histories(Arrays.asList(new History(1), null))); // Invalid ("histories[1]" must not be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... the indexed field name |

## Map

Apply the constraints on the `Map` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraint(/* Function from TargetClass to Map  */, /* Field Name */, /* Constraints */)
    .build();
```

To avoid ambiguous type inferences, you can use explicit `_map` instead of `constraint` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._map(/* Function from TargetClass to Map  */, /* Field Name */, /* Constraints */)
    .build();
```

### `notNull()`

Checks that the target value is not `null`

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.notNull())
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Invalid ("codeMap" must not be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `isNull()`

Checks that the target value is `null`

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
    .constraint(CodeMap::asMap, "codeMap", c -> c.isNull())
    .build();

ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Invalid ("codeMap" must be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.isNull` | `"{0}" must be null` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `notEmpty()`

Checks that the target value is not empty

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.notEmpty())
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Invalid ("codeMap" must not be empty)
ConstraintViolations violations = validator.validate(new CodeMap(Map.of())); // Invalid ("codeMap" must not be empty)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.notEmpty` | `"{0}" must not be empty` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `fixedSize(int)`

Checks if the target value's size is the specified size

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.fixedSize(2))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of())); // Invalid (The size of "codeMap" must be 2. The given size is 1)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.fixedSize` | `The size of "{0}" must be {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `greaterThan(int)`

Checks if the target value's size is greater than the specified size

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.greaterThan(1))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A"))); // Invalid (The size of "codeMap" must be greater than 1. The given size is 1)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.greaterThan` | `The size of "{0}" must be greater than {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `greaterThanOrEqual(int)`

Checks if the target value's size is greater than or equals to the specified size

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.greaterThanOrEqual(2))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A"))); // Invalid (The size of "codeMap" must be greater than or equal to 2. The given size is 1)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.greaterThanOrEqual` | `The size of "{0}" must be greater than or equal to {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `lessThan(int)`

Checks if the target value's size is less than the specified size

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.lessThan(3))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B", "c", "C"))); // Invalid (The size of "histories" must be less than 3. The given size is 3)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.lessThan` | `The size of "{0}" must be less than {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `lessThanOrEqual(int)`

Checks if the target value's size is less than or equals to the specified size

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.lessThanOrEqual(2))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B", "c", "C"))); // Invalid (The size of "histories" must be less than or equal to 2. The given size is 3)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `container.lessThanOrEqual` | `The size of "{0}" must be less than or equal to {1}. The given size is {2}` | - `{0}` ... field name<br>- `{1}` ... the specified size<br>- `{2}` ... the given size<br>- `{3}` ... the given value |

### `containsKey(...)`

Checks if the target value contains the specified key

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.containsKey("b"))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("c", "C"))); // Invalid ("codeMap" must contain key b)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `map.containsKey` | `"{0}" must contain key {1}` | - `{0}` ... field name<br>- `{1}` ... the specified key<br>- `{2}` ... the given value |

### `containsValue(...)`

Checks if the target value contains the specified value

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.containsValue("B"))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("c", "C"))); // Invalid ("codeMap" must contain value B)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `map.containsValue` | `"{0}" must contain value {1}` | - `{0}` ... field name<br>- `{1}` ... the specified value<br>- `{2}` ... the given value |

## Object

Apply the constraints on any `Object` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraint(/* Function from TargetClass to Object  */, /* Field Name */, /* Constraints */)
    .build();
```

To avoid ambiguous type inferences, you can use explicit `_object` instead of `constraint` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._object(/* Function from TargetClass to Object  */, /* Field Name */, /* Constraints */)
    .build();
```

If you want to apply constraints on target class itself (e.g. [Cross-field validation](using-yavi.md#cross-field-validation)), you can use `constraintOnTarget` as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraintOnTarget(/* Field Name */, /* Constraints */)
    .build();
```

It is equivalent to bellow:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._object(Function.identity(), /* Field Name */, /* Constraints */)
    .build();
```

### `notNull()`

Checks that the target value is not `null`

```java
Validator<CreatedAt> validator = ValidatorBuilder.<CreatedAt> of()
    .constraint(CreatedAt::asInstant, "createdAt", c -> c.notNull())
    .build();

ConstraintViolations violations = validator.validate(new CreatedAt(Instant.now())); // Valid
ConstraintViolations violations = validator.validate(new CreatedAt(null)); // Invalid ("createdAt" must not be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... field name<br>- `{1}` ... the given value |


### `isNull()`

Checks that the target value is `null`

```java
Validator<CreatedAt> validator = ValidatorBuilder.<CreatedAt> of()
    .constraint(CreatedAt::asInstant, "createdAt", c -> c.isNull())
    .build();


ConstraintViolations violations = validator.validate(new CreatedAt(null)); // Valid
ConstraintViolations violations = validator.validate(new CreatedAt(Instant.now())); // Invalid ("createdAt" must be null)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.isNull` | `"{0}" must be null` | - `{0}` ... field name<br>- `{1}` ... the given value |

### `equalTo(Object)`

Checks if the target value is equal to the specified value

```java
Validator<Box> validator = ValidatorBuilder.<Box> of()
    ._object(Box::getValue, "value", c -> c.equalTo("expected"))
    .build();

ConstraintViolations violations = validator.validate(new Box("expected")); // Valid
ConstraintViolations violations = validator.validate(new Box(null)); // Valid
ConstraintViolations violations = validator.validate(new Box("other")); // Invalid ("value" must be equal to expected)
```

### `oneOf(Collection)`

Checks if the target value is one of the specified values

```java
Validator<Box> validator = ValidatorBuilder.<Box> of()
    ._object(Box::getValue, "value", c -> c.oneOf(List.of("a", "b")))
    .build();

ConstraintViolations violations = validator.validate(new Box("a")); // Valid
ConstraintViolations violations = validator.validate(new Box(null)); // Valid
ConstraintViolations violations = validator.validate(new Box("c")); // Invalid ("value" must be one of the following values: [a, b])
```

### `notOneOf(Collection)`

Checks if the target value is not one of the specified values

```java
Validator<Box> validator = ValidatorBuilder.<Box> of()
    ._object(Box::getValue, "value", c -> c.notOneOf(List.of("a", "b")))
    .build();

ConstraintViolations violations = validator.validate(new Box("c")); // Valid
ConstraintViolations violations = validator.validate(new Box(null)); // Valid
ConstraintViolations violations = validator.validate(new Box("a")); // Invalid ("value" must not be one of the following values: [a, b])
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.equalTo` | `"{0}" must be equal to {1}` | - `{0}` ... field name<br>- `{1}` ... the specified value<br>- `{2}` ... the given value |
| `object.oneOf` | `"{0}" must be one of the following values: {1}` | - `{0}` ... field name<br>- `{1}` ... the allowed values<br>- `{2}` ... the given value |
| `object.notOneOf` | `"{0}" must not be one of the following values: {1}` | - `{0}` ... field name<br>- `{1}` ... the forbidden values<br>- `{2}` ... the given value |

### `password()`

Check if the target value meets the specified custom password policy (e.g. Cross-field check)

```java
PasswordPolicy<Account> passwordPolicy = PasswordPolicy.of("DoNotIncludeUsername",
		account -> {
			String username = account.getUsername();
			String password = account.getPassword();
			if (username == null || password == null) {
				return true;
			}
			return !password.toUpperCase().contains(username.toUpperCase());
		});
Validator<Account> validator = ValidatorBuilder.<Account> of()
		.constraintOnTarget("password", c -> c.password(policy -> policy
				.required(passwordPolicy)
				.build()))
		.build();

ConstraintViolations violations = validator.validate(new Account("foo", "Bar1234")); // Valid
ConstraintViolations violations = validator.validate(new Account(null, null)); // Valid
ConstraintViolations violations = validator.validate(new Account("foo", "Foo1234")); // Invalid ("password" must meet DoNotIncludeUsername policy)
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `password.required` | `"{0}" must meet {1} policy` | - `{0}` ... field name<br>- `{1}` ... the specified policy name<br>- `{2}` ... the given value |
| `password.optional` | `"{0}" must meet at least {1} policies from {2}` | - `{0}` ... field name<br>- `{1}` ... minimum requirement<br>- `{2}` ... the specified policy names<br>- `{3}` ... the given value |

## Enum

Apply constraints on an enum as follows:

```java
Validator<Account> validator = ValidatorBuilder.<Account> of()
    ._enum(Account::getStatus, "status", c -> c.oneOf(Status.ACTIVE))
    .build();

ConstraintViolations violations = validator.validate(new Account(Status.ACTIVE)); // Valid
ConstraintViolations violations = validator.validate(new Account(null)); // Valid
ConstraintViolations violations = validator.validate(new Account(Status.INACTIVE)); // Invalid ("status" must be one of the following values: [ACTIVE])
```

`_enum` is an alias of `constraint(ToEnum, String, Function)`.
The same `_enum` method is available on `ArgumentsValidatorBuilder` and `YaviArguments` to validate an enum argument.

`EnumConstraint#oneOf(E...)` requires the value to be one of the given enum constants.
An empty argument list is rejected with `IllegalArgumentException`.
A `null` value is valid unless `notNull()` is added.
The violation uses the `object.oneOf` message key.

A single enum value can be validated with `EnumValidatorBuilder` and `EnumValidator` as follows:

```java
EnumValidator<Status, Status> validator = EnumValidatorBuilder.<Status> of("status",
        c -> c.oneOf(Status.ACTIVE)).build();

Validated<Status> result = validator.validate(Status.INACTIVE); // Invalid ("status" must be one of the following values: [ACTIVE])
```

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `object.oneOf` | `"{0}" must be one of the following values: {1}` | - `{0}` ... field name<br>- `{1}` ... the allowed values<br>- `{2}` ... the given value |

## Array

Apply the constraints on arrays as follows:

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._objectArray(/* Function from TargetClass to an object array  */, /* Field Name */, /* Constraints */)
    .build();
```

The following constraints and builder shortcuts are available:

| Array type | Builder shortcut |
| --- | --- |
| `E[]` (`ObjectArrayConstraint`) | `_objectArray` |
| `boolean[]` (`BooleanArrayConstraint`) | `_booleanArray` |
| `byte[]` (`ByteArrayConstraint`) | `_byteArray` |
| `char[]` (`CharArrayConstraint`) | `_charArray` |
| `short[]` (`ShortArrayConstraint`) | `_shortArray` |
| `int[]` (`IntArrayConstraint`) | `_intArray` |
| `long[]` (`LongArrayConstraint`) | `_longArray` |
| `float[]` (`FloatArrayConstraint`) | `_floatArray` |
| `double[]` (`DoubleArrayConstraint`) | `_doubleArray` |

The constraints in `am.ik.yavi.constraint.array` provide `notNull`, `isNull`, `notEmpty`, `fixedSize`, `greaterThan`, `greaterThanOrEqual`, `lessThan`, `lessThanOrEqual` and `contains`.
A `null` array is valid and the checks are skipped, except for `notEmpty()`.

```java
Validator<Values> validator = ValidatorBuilder.<Values> of()
    ._intArray(Values::getNumbers, "numbers", c -> c.contains(3).fixedSize(3))
    .build();

ConstraintViolations violations = validator.validate(new Values(new int[] { 1, 2, 3 })); // Valid
ConstraintViolations violations = validator.validate(new Values(null)); // Valid
ConstraintViolations violations = validator.validate(new Values(new int[] { 1, 2 })); // Invalid ("numbers" must contain 3, The size of "numbers" must be 3. The given size is 2)
```

The size and null checks report the `object.notNull`, `object.isNull` and `container.*` message keys documented in the Collection section.
`contains` reports `array.contains`.

| Message Key | Default Message Format | Args |
| --- | --- | --- |
| `array.contains` | `"{0}" must contain {1}` | - `{0}` ... field name<br>- `{1}` ... the specified value<br>- `{2}` ... the given value |
