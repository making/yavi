# 組み込みの制約

YAVI には、型ごとによく使われる組み込みの制約が多数用意されています。

## String

`String` に制約を適用するには、次のように書きます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraint(/* Function from TargetClass to String  */, /* Field Name */, /* Constraints */)
    .build();
```

型推論が曖昧になる場合は、`constraint` の代わりに `_string` を明示的に使えます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._string(/* Function from TargetClass to String  */, /* Field Name */, /* Constraints */)
    .build();
```

より汎用的な `CharSequence` に制約を適用する場合は、`_charSequence` メソッドを使います。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._charSequence(/* Function from TargetClass to CharSequence  */, /* Field Name */, /* Constraints */)
    .build();
```

以下に示す制約では、特に断りのない限り `null` は有効な値として扱われ、検査はスキップされます。

### `notNull()`

対象の値が `null` でないことを検査します。

```java
Validator<Email> validator = ValidatorBuilder.<Email> of()
    .constraint(Email::asString, "email", c -> c.notNull())
    .build();

ConstraintViolations violations = validator.validate(new Email("yavi@example.com")); // Valid
ConstraintViolations violations = validator.validate(new Email(null)); // Invalid ("email" must not be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isNull()`

対象の値が `null` であることを検査します。

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(User::getId, "id", c -> c.isNull())
    .build();

ConstraintViolations violations = validator.validate(new User(null)); // Valid
ConstraintViolations violations = validator.validate(new User("1234")); // Invalid ("id" must be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.isNull` | `"{0}" must be null` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `notEmpty()`

対象の値が `null` でも空でもないことを検査します。

```java
Validator<Email> validator = ValidatorBuilder.<Email> of()
    .constraint(Email::asString, "email", c -> c.notEmpty())
    .build();

ConstraintViolations violations = validator.validate(new Email("yavi@example.com")); // Valid
ConstraintViolations violations = validator.validate(new Email(null)); // Invalid ("email" must not be empty)
ConstraintViolations violations = validator.validate(new Email("")); // Invalid ("email" must not be empty)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.notEmpty` | `"{0}" must not be empty` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `notBlank()`

対象の値が `null` でなく、前後の空白を除いた値の長さが `0` より大きいことを検査します。
`notEmpty()` との違いは、前後の空白が無視されることです。

```java
Validator<Email> validator = ValidatorBuilder.<Email> of()
    .constraint(Email::asString, "email", c -> c.notBlank())
    .build();

ConstraintViolations violations = validator.validate(new Email("yavi@example.com")); // Valid
ConstraintViolations violations = validator.validate(new Email(null)); // Invalid ("email" must not be blank)
ConstraintViolations violations = validator.validate(new Email(" ")); // Invalid ("email" must not be blank)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.notBlank` | `"{0}" must not be blank` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `fixedSize(int)`

対象の値のサイズが指定したサイズであることを検査します。

```java
Validator<ZipCode> validator = ValidatorBuilder.<ZipCode> of()
    .constraint(ZipCode::asString, "zipCode", c -> c.fixedSize(7))
    .build();

ConstraintViolations violations = validator.validate(new ZipCode("1234567")); // Valid
ConstraintViolations violations = validator.validate(new ZipCode(null)); // Valid
ConstraintViolations violations = validator.validate(new ZipCode("123-4567")); // Invalid (The size of "zipCode" must be 7. The given size is 8)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.fixedSize` | `The size of "{0}" must be {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `greaterThan(int)`

対象の値のサイズが指定したサイズより大きいことを検査します。

```java
Validator<Country> validator = ValidatorBuilder.<Country> of()
    .constraint(Country::asString, "country", c -> c.greaterThan(2))
    .build();

ConstraintViolations violations = validator.validate(new Country("Japan")); // Valid
ConstraintViolations violations = validator.validate(new Country(null)); // Valid
ConstraintViolations violations = validator.validate(new Country("J")); // Invalid (The size of "country" must be greater than 2. The given size is 1)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.greaterThan` | `The size of "{0}" must be greater than {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `greaterThanOrEqual(int)`

対象の値のサイズが指定したサイズ以上であることを検査します。

```java
Validator<Country> validator = ValidatorBuilder.<Country> of()
    .constraint(Country::asString, "country", c -> c.greaterThanOrEqual(2))
    .build();

ConstraintViolations violations = validator.validate(new Country("Japan")); // Valid
ConstraintViolations violations = validator.validate(new Country(null)); // Valid
ConstraintViolations violations = validator.validate(new Country("J")); // Invalid (The size of "country" must be greater than or equal to 2. The given size is 1)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.greaterThanOrEqual` | `The size of "{0}" must be greater than or equal to {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `lessThan(int)`

対象の値のサイズが指定したサイズより小さいことを検査します。

```java
Validator<Country> validator = ValidatorBuilder.<Country> of()
    .constraint(Country::asString, "country", c -> c.lessThan(4))
    .build();

ConstraintViolations violations = validator.validate(new Country("JP")); // Valid
ConstraintViolations violations = validator.validate(new Country(null)); // Valid
ConstraintViolations violations = validator.validate(new Country("Japan")); // Invalid (The size of "country" must be less than 4. The given size is 5)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.lessThan` | `The size of "{0}" must be less than {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `lessThanOrEqual(int)`

対象の値のサイズが指定したサイズ以下であることを検査します。

```java
Validator<Country> validator = ValidatorBuilder.<Country> of()
    .constraint(Country::asString, "country", c -> c.lessThanOrEqual(4))
    .build();

ConstraintViolations violations = validator.validate(new Country("JP")); // Valid
ConstraintViolations violations = validator.validate(new Country(null)); // Valid
ConstraintViolations violations = validator.validate(new Country("Japan")); // Invalid (The size of "country" must be less than or equal to 4. The given size is 5)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.lessThanOrEqual` | `The size of "{0}" must be less than or equal to {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `contains(CharSequence)`

対象の値が指定した文字列を含むことを検査します。

```java
Validator<ZipCode> validator = ValidatorBuilder.<ZipCode> of()
    .constraint(ZipCode::asString, "zipCode", c -> c.contains("-"))
    .build();

ConstraintViolations violations = validator.validate(new ZipCode("123-4567")); // Valid
ConstraintViolations violations = validator.validate(new ZipCode(null)); // Valid
ConstraintViolations violations = validator.validate(new ZipCode("1234567")); // Invalid ("zipCode" must contain -)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.contains` | `"{0}" must contain {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定した文字列<br>- `{2}` ... 与えられた値 |

### `startsWith(CharSequence)`

対象の値が指定した接頭辞で始まることを検査します。

```java
Validator<Code> validator = ValidatorBuilder.<Code> of()
    .constraint(Code::asString, "code", c -> c.startsWith("JP-"))
    .build();

ConstraintViolations violations = validator.validate(new Code("JP-1234")); // Valid
ConstraintViolations violations = validator.validate(new Code(null)); // Valid
ConstraintViolations violations = validator.validate(new Code("US-1234")); // Invalid ("code" must start with "JP-")
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.startsWith` | `"{0}" must start with "{1}"` | - `{0}` ... フィールド名<br>- `{1}` ... 指定した接頭辞<br>- `{2}` ... 与えられた値 |

### `endsWith(CharSequence)`

対象の値が指定した接尾辞で終わることを検査します。

```java
Validator<Domain> validator = ValidatorBuilder.<Domain> of()
    .constraint(Domain::asString, "domain", c -> c.endsWith(".example.com"))
    .build();

ConstraintViolations violations = validator.validate(new Domain("www.example.com")); // Valid
ConstraintViolations violations = validator.validate(new Domain(null)); // Valid
ConstraintViolations violations = validator.validate(new Domain("example.com")); // Invalid ("domain" must end with ".example.com")
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.endsWith` | `"{0}" must end with "{1}"` | - `{0}` ... フィールド名<br>- `{1}` ... 指定した接尾辞<br>- `{2}` ... 与えられた値 |

### `pattern(String)`

対象の値が指定した正規表現に一致することを検査します。

```java
Validator<ZipCode> validator = ValidatorBuilder.<ZipCode> of()
    .constraint(ZipCode::asString, "zipCode", c -> c.pattern("[0-9]{3}-[0-9]{4}"))
    .build();

ConstraintViolations violations = validator.validate(new ZipCode("123-4567")); // Valid
ConstraintViolations violations = validator.validate(new ZipCode(null)); // Valid
ConstraintViolations violations = validator.validate(new ZipCode("1234567")); // Invalid ("zipCode" must match [0-9]{3}-[0-9]{4})
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.pattern` | `"{0}" must match {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定した正規表現<br>- `{2}` ... 与えられた値 |

### `pattern(Pattern)` / `pattern(Supplier<Pattern>)`

対象の値が指定した正規表現に一致することを、`pattern(String)` と同じように検査します。
`Supplier<Pattern>` を受け取るオーバーロードは、検証のたびにサプライヤを呼び出します。検証時に動的に組み立てる正規表現を想定しています。

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

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.pattern` | `"{0}" must match {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定した正規表現<br>- `{2}` ... 与えられた値 |

### `email()`

対象の値が有効なメールアドレスであることを検査します。

```java
Validator<Email> validator = ValidatorBuilder.<Email> of()
    .constraint(Email::asString, "email", c -> c.email())
    .build();

ConstraintViolations violations = validator.validate(new Email("yavi@example.com")); // Valid
ConstraintViolations violations = validator.validate(new Email(null)); // Valid
ConstraintViolations violations = validator.validate(new Email("example.com")); // Invalid ("email" must be a valid email address)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.email` | `"{0}" must be a valid email address` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `uuid()`

対象の値が 8-4-4-4-12 の 16 進数形式の有効な UUID であることを検査します。バージョンとバリアントは検査せず、空の値は有効です。

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(User::getId, "id", c -> c.uuid())
    .build();

ConstraintViolations violations = validator.validate(new User("123e4567-e89b-12d3-a456-426614174000")); // Valid
ConstraintViolations violations = validator.validate(new User(null)); // Valid
ConstraintViolations violations = validator.validate(new User("")); // Valid
ConstraintViolations violations = validator.validate(new User("1234")); // Invalid ("id" must be a valid UUID)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.uuid` | `"{0}" must be a valid UUID` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `password(...)`

対象の値が指定したパスワードポリシーを満たすことを検査します。

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

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `password.required` | `"{0}" must meet {1} policy` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したポリシー名<br>- `{2}` ... 与えられた値 |
| `password.optional` | `"{0}" must meet at least {1} policies from {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 最低要件<br>- `{2}` ... 指定したポリシー名<br>- `{3}` ... 与えられた値 |

組み込みのパスワードポリシーは次のとおりです。

* `am.ik.yavi.constraint.password.PasswordPolicy#UPPERCASE`
* `am.ik.yavi.constraint.password.PasswordPolicy#LOWERCASE`
* `am.ik.yavi.constraint.password.PasswordPolicy#ALPHABETS`
* `am.ik.yavi.constraint.password.PasswordPolicy#NUMBERS`
* `am.ik.yavi.constraint.password.PasswordPolicy#SYMBOLS`

パターンの数を指定するには、次のように書きます。

```java
Validator<Password> validator = ValidatorBuilder.<Password> of()
		.constraint(Password::value, "password", c -> c.password(policy -> policy
				.uppercase(2) // at least 2 upper case characters are required
				.lowercase(2) // at least 2 lower case characters are required
				// or .required(PasswordPolicy.UPPERCASE.count(2), PasswordPolicy.LOWERCASE.count(2))
				.build()))
		.build();
```

独自のパスワードポリシーは次のように定義できます。

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

対象の値が有効な IPv4 アドレスであることを検査します。

```java
Validator<IpAddress> validator = ValidatorBuilder.<IpAddress> of()
    .constraint(IpAddress::asString, "ipAddress", c -> c.ipv4())
    .build();

ConstraintViolations violations = validator.validate(new IpAddress("192.0.2.1")); // Valid
ConstraintViolations violations = validator.validate(new IpAddress(null)); // Valid
ConstraintViolations violations = validator.validate(new IpAddress("example.com")); // Invalid ("ipAddress" must be a valid IPv4)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.ipv4` | `"{0}" must be a valid IPv4` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `ipv6()`

対象の値が有効な IPv6 アドレスであることを検査します。

```java
Validator<IpAddress> validator = ValidatorBuilder.<IpAddress> of()
    .constraint(IpAddress::asString, "ipAddress", c -> c.ipv6())
    .build();

ConstraintViolations violations = validator.validate(new IpAddress("2001:0db8:bd05:01d2:288a:1fc0:0001:10ee")); // Valid
ConstraintViolations violations = validator.validate(new IpAddress(null)); // Valid
ConstraintViolations violations = validator.validate(new IpAddress("192.0.2.1")); // Invalid ("ipAddress" must be a valid IPv6)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.ipv6` | `"{0}" must be a valid IPv6` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `url()`

対象の値が有効な URL であることを検査します。空の値は有効です。

> **注意:** 0.17.0 から、値は非推奨の `URL(String)` コンストラクタではなく `java.net.URI` として解析されます。`URL(String)` は先頭と末尾の文字を黙って取り除いていました。この取り除きによってのみ通っていた値、エンコードされていない文字を含む値、authority が空の値は、現在は拒否されます。

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

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.url` | `"{0}" must be a valid URL` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `url(...)`

対象の値が有効な URL であり、そのプロトコル、ホスト、ポートを制限することを検査します。
値はまず有効な URL である必要があり、想定外の属性は URL の違反に加えて報告されます。
これは Bean Validation の `@URL(protocol = "https", host = "example.com", port = 8443)` に相当します。
空の値は有効です。

ホストは大文字と小文字を区別せずに比較しますが、記述されたとおりに照合するため、`host("example.com")` は `https://www.example.com` を受け付けません。
ポートを明示しない URL は、そのプロトコルの既定のポートと照合するため、`port(443)` は `https://example.com` を受け付けます。
URL として解析できない値では、`charSequence.url` だけが報告されます。

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

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.url` | `"{0}" must be a valid URL` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |
| `charSequence.url.protocol` | `The protocol of "{0}" must be one of the following values: {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したプロトコル<br>- `{2}` ... 与えられた値 |
| `charSequence.url.host` | `The host of "{0}" must be one of the following values: {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したホスト<br>- `{2}` ... 与えられた値 |
| `charSequence.url.port` | `The port of "{0}" must be one of the following values: {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したポート<br>- `{2}` ... 与えられた値 |

### `luhn()`

対象の値の数字が Luhn チェックサムアルゴリズムを満たすことを検査します（[Luhn algorithm](http://en.wikipedia.org/wiki/Luhn_algorithm) も参照）。

```java
Validator<CreditCard> validator = ValidatorBuilder.<CreditCard> of()
    .constraint(CreditCard::number, "creditCardNumber", c -> c.luhn())
    .build();

ConstraintViolations violations = validator.validate(new CreditCard("4111111111111111")); // Valid
ConstraintViolations violations = validator.validate(new CreditCard(null)); // Valid
ConstraintViolations violations = validator.validate(new CreditCard("4111111111111112")); // Invalid (the check digit for "creditCardNumber" is invalid, Luhn checksum failed)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.luhn` | `the check digit for "{0}" is invalid, Luhn checksum failed` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isByte()`

対象の値が `Byte` の値として解析できることを検査します。

```java
Validator<UserId> validator = ValidatorBuilder.<UserId> of()
    .constraint(UserId::asString, "userId", c -> c.isByte())
    .build();

ConstraintViolations violations = validator.validate(UserId.valueOf("127")); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf("a")); // Invalid ("userId" must be a valid representation of a byte)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.byte` | `"{0}" must be a valid representation of a byte` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isShort()`

対象の値が `Short` の値として解析できることを検査します。

```java
Validator<UserId> validator = ValidatorBuilder.<UserId> of()
    .constraint(UserId::asString, "userId", c -> c.isShort())
    .build();

ConstraintViolations violations = validator.validate(UserId.valueOf("32767")); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf("a")); // Invalid ("userId" must be a valid representation of a short)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.short` | `"{0}" must be a valid representation of a short` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isInteger()`

対象の値が `Integer` の値として解析できることを検査します。

```java
Validator<UserId> validator = ValidatorBuilder.<UserId> of()
    .constraint(UserId::asString, "userId", c -> c.isInteger())
    .build();

ConstraintViolations violations = validator.validate(UserId.valueOf("2147483647")); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf("a")); // Invalid ("userId" must be a valid representation of an integer)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.integer` | `"{0}" must be a valid representation of an integer` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isLong()`

対象の値が `Long` の値として解析できることを検査します。

```java
Validator<UserId> validator = ValidatorBuilder.<UserId> of()
    .constraint(UserId::asString, "userId", c -> c.isLong())
    .build();

ConstraintViolations violations = validator.validate(UserId.valueOf("9223372036854775807")); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf("a")); // Invalid ("userId" must be a valid representation of a long)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.long` | `"{0}" must be a valid representation of a long` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isFloat()`

対象の値が `Float` の値として解析できることを検査します。

```java
Validator<Money> validator = ValidatorBuilder.<Money> of()
    .constraint(Money::asString, "money", c -> c.isFloat())
    .build();

ConstraintViolations violations = validator.validate(Money.valueOf("0.1")); // Valid
ConstraintViolations violations = validator.validate(Money.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(Money.valueOf("a")); // Invalid ("money" must be a valid representation of a float)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.float` | `"{0}" must be a valid representation of a float.` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isDouble()`

対象の値が `Double` の値として解析できることを検査します。

```java
Validator<Money> validator = ValidatorBuilder.<Money> of()
    .constraint(Money::asString, "money", c -> c.isDouble())
    .build();

ConstraintViolations violations = validator.validate(Money.valueOf("0.1")); // Valid
ConstraintViolations violations = validator.validate(Money.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(Money.valueOf("a")); // Invalid ("money" must be a valid representation of a double)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.double` | `"{0}" must be a valid representation of a double` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isBigInteger()`

対象の値が `BigInteger` の値として解析できることを検査します。

```java
Validator<UserId> validator = ValidatorBuilder.<UserId> of()
    .constraint(UserId::asString, "userId", c -> c.isBigInteger())
    .build();

ConstraintViolations violations = validator.validate(UserId.valueOf("127")); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(UserId.valueOf("a")); // Invalid ("userId" must be a valid representation of a big integer)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.biginteger` | `"{0}" must be a valid representation of a big integer` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isBigDecimal()`

対象の値が `BigDecimal` の値として解析できることを検査します。

```java
Validator<Money> validator = ValidatorBuilder.<Money> of()
    .constraint(Money::asString, "money", c -> c.isBigDecimal())
    .build();

ConstraintViolations violations = validator.validate(Money.valueOf("50.0")); // Valid
ConstraintViolations violations = validator.validate(Money.valueOf(null)); // Valid
ConstraintViolations violations = validator.validate(Money.valueOf("a")); // Invalid ("money" must be a valid representation of a big decimal)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.bigdecimal` | `"{0}" must be a valid representation of a big decimal` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isoLocalDate()` / `localDate(String pattern)`

対象の値が、指定したパターンの有効なローカル日付であることを検査します。解決には `ResolverStyle.STRICT` を使います。
`isoLocalDate()` はパターン `uuuu-MM-dd` を使います。
フォーマッタは値の検証のたびに作成されます。
空の値は有効です。

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

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `charSequence.localdate` | `"{0}" must be a valid representation of a local date using the pattern: {1}. The give value is: {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定した日付パターン<br>- `{2}` ... 与えられた値 |

### `codePoints(...)`

対象の値が指定したコードポイントの集合に含まれることを検査します。
コードポイントは、許可する文字（ホワイトリスト）または禁止する文字（ブラックリスト）として指定できます。

コードポイントの集合は `am.ik.yavi.constraint.charsequence.CodePoints` インタフェースで表します。`java.util.Set` で集合を表す `am.ik.yavi.constraint.charsequence.CodePoints.CodePointsSet` インタフェースと、コードポイントの範囲のリストを表す `am.ik.yavi.constraint.charsequence.CodePoints.CodePointsRanges` インタフェースもあります。

たとえば、"A, B, C, D, a, b, c, d" からなるコードポイントの集合は次のように表します。

```java
CodePointsSet<String> codePoints = () -> Set.of(
		0x0041 /* A */, 0x0042 /* B */, 0x0043 /* C */, 0x0044 /* D */,
        0x0061 /* a */, 0x0062 /* b */, 0x0063 /* c */, 0x0064 /* d */);
```

または、次のように表します。

```java
CodePointsRanges<String> codePoints = () -> List.of(
        Range.of(0x0041/* A */, 0x0044 /* D */),
        Range.of(0x0061/* a */, 0x0064 /* d */));
```

連続するコードポイントでは、後者のほうがメモリ効率がはるかに良くなります。

コードポイントの集合をホワイトリスト（許可する文字）として扱う場合は、次のように指定します。

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.codePoints(codePoints).asWhiteList())
    .build();

ConstraintViolations violations = validator.validate(new Message("aBCd")); // Valid
ConstraintViolations violations = validator.validate(new Message(null)); // Valid
ConstraintViolations violations = validator.validate(new Message("aBCe")); // Invalid ("[e]" is/are not allowed for "text")
```

コードポイントの集合をブラックリスト（禁止する文字）として扱う場合は、次のように指定します。

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.codePoints(codePoints).asBlackList())
    .build();

ConstraintViolations violations = validator.validate(new Message("hello")); // Valid
ConstraintViolations violations = validator.validate(new Message(null)); // Valid
ConstraintViolations violations = validator.validate(new Message("hallo")); // Invalid ("[a]" is/are not allowed for "text")
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `codePoints.asWhiteList` | `"{1}" is/are not allowed for "{0}"` | - `{0}` ... フィールド名<br>- `{1}` ... 違反した値 |
| `codePoints.asBlackList` | `"{1}" is/are not allowed for "{0}"` | - `{0}` ... フィールド名<br>- `{1}` ... 違反した値 |

組み込みのコードポイントの集合は次のとおりです。

* `am.ik.yavi.constraint.charsequence.codepoints.AsciiCodePoints#ASCII_PRINTABLE_CHARS` ... ASCII の印字可能文字
* `am.ik.yavi.constraint.charsequence.codepoints.AsciiCodePoints#ASCII_CONTROL_CHARS` ... ASCII の制御文字
* `am.ik.yavi.constraint.charsequence.codepoints.AsciiCodePoints#CRLF` ... `0x000A`（LINE FEED）と `0x000D`（CARRIAGE RETURN）
* `am.ik.yavi.constraint.charsequence.codepoints.UnicodeCodePoints#HIRAGANA` ... Unicode で定義された[ひらがな](https://www.unicode.org/charts/nameslist/c_3040.html)（JIS X 0208 の定義とは異なります）
* `am.ik.yavi.constraint.charsequence.codepoints.UnicodeCodePoints#KATAKANA` ... Unicode で定義された[カタカナ](https://www.unicode.org/charts/nameslist/c_30A0.html)と[カタカナ音声拡張](https://www.unicode.org/charts/nameslist/c_31F0.html)（JIS X 0208 の定義とは異なります）

複数のコードポイントの集合の和集合は、`am.ik.yavi.constraint.charsequence.codepoints.CompositeCodePoints` クラスでも表せます。

### 文字数の高度な検査

文字数に関する制約では、YAVI は入力された文字を、見た目どおりの文字数として数えます。Unicode に多様な文字が定義された結果、見た目の大きさと `String#length` メソッドの戻り値は大きく異なります。

YAVI は次の文字の種類に対応しています。

* サロゲートペア
* 結合文字
* 異体字セレクタ
  * IVS（Ideographic Variation Sequence）
  * SVS（Standardized Variation Sequence）
  * FVS（Mongolian Free Variation Selector）
* 絵文字

YAVI は、サロゲートペアと結合文字については、見た目どおりの文字数で制約を検査します（絵文字、IVS、SVS、FVS は、性能の理由から既定では見た目どおりのサイズを検査しません）。

典型的な例をいくつか示します。

#### サロゲートペア

`𠮷野屋` はサロゲートペアの例です。3 文字に見えますが、`length` メソッドの結果は 4 です（`\uD842\uDFB7野屋`）。

```java
System.out.println("𠮷野屋".length()); // 4 (\uD842\uDFB7野屋)
```

YAVI は文字の長さをコードポイントの長さとして扱うため、この文字列は 3 文字とみなされます。

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.lessThanOrEqual(3))
    .build();

ConstraintViolations violations = validator.validate(new Message("𠮷野屋")); // Valid
```

#### 結合文字

`モジ` は結合文字の例です。2 文字に見えますが、`シ` と濁点(`゙`) が結合しており、`length()` の結果は 3 です（`モシ\u3099`）。

```java
System.out.println("モジ".length()); // 3 (モシ\u3099)
```

YAVI はこの文字列を既定では 2 文字とみなします。

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.lessThanOrEqual(2))
    .build();

ConstraintViolations violations = validator.validate(new Message("モジ")); // Valid
```

YAVI は既定で `java.text.Normalizer` を使い、`java.text.Normalizer.Form#NFC` で正規化します。この動作は次のように変更できます（`null` を設定すると正規化しません）。

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.normalizer(normalizerForm)
				.lessThanOrEqual(2))
    .build();
```

#### 異体字セレクタ

`𠮟󠄀` は異体字シーケンス（Ideographic Variation Sequence）の例です。異体字セレクタは `Normalizer` で正規化できません。
1 文字に見えますが、UTF-16 で表すと "D842 DF9F DB40 DD00" になるため、`length()` の結果は 4 です。

```java
System.out.println("𠮟󠄀".length()); // 4 (\uD842\uDF9F\uDB40\uDD00󠄀)
```

YAVI は性能の低下を防ぐため、この文字を既定では 1 文字とみなしません。

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
		.constraint(Message::getText, "text", c -> c.lessThanOrEqual(1))
		.build();
ConstraintViolations violations = validator.validate(new Message("𠮟󠄀")); // Invalid (The size of "text" must be less than or equal to 1. The given size is 2)
```

次のようにすると、IVS の範囲である `0xE0100`-`0xE01EF` のコードポイントを対象の文字列から無視（削除）できます。
こうすると、この文字列を単なるサロゲートペアとして扱えます。

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
		.constraint(Message::getText, "text", c -> c.variant(opts -> opts.ivs(IdeographicVariationSequence.IGNORE))
				.lessThanOrEqual(1))
		.build();
ConstraintViolations violations = validator.validate(new Message("𠮟󠄀")); // Valid
```

SVS と FVS も同じように扱えます。

#### 絵文字

絵文字は、見た目の文字数とコードポイント数が大きく離れています。

例を示します。

```java
System.out.println("❤️".length()); // 2
System.out.println("🤴🏻".length()); // 4
System.out.println("👨‍👦".length()); // 5
System.out.println("️👨‍👨‍👧‍👦".length()); // 12
System.out.println("🏴󠁧󠁢󠁥󠁮󠁧󠁿".length()); // 14 (WTH!)
```

YAVI はこれらの絵文字をできるだけ 1 文字として数えようとします。保証はありませんが、Emoji 12.0 で定義されたすべての絵文字でテストしています。

この処理はコストが高く、既定では無効です。有効にするには、次のように `emoji()` メソッドを指定します。

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

サイズを測る前に、選択した異体字セレクタを削除する制約を返します。
値自体は変わりません。サイズの数え方だけに影響します。
IVS、SVS、FVS の詳細は[文字数の高度な検査](#advanced-character-length-check)を参照してください。

```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.variant(opts -> opts.fvs(MongolianFreeVariationSelector.IGNORE))
            .fixedSize(1))
    .build();

ConstraintViolations violations = validator.validate(new Message("ᠠ᠋")); // Valid
```

`variant(...)` は制約そのものではなく、独自のメッセージも持ちません。結果に適用したサイズの制約のメッセージが報告されます。

### `asByteArray()`

見た目の文字の長さと実際のコードポイントの長さが食い違うと、見た目の制限は満たしていても、データベースに格納できるサイズを超えることがあります。YAVI では、見た目のサイズに加えてバイト長を検査できます。


```java
Validator<Message> validator = ValidatorBuilder.<Message> of()
    .constraint(Message::getText, "text", c -> c.emoji().lessThanOrEqual(3)
            .asByteArray().lessThanOrEqual(16))
    .build();

ConstraintViolations violations = validator.validate(new Message("I❤️☕️️")); // Valid
violations = validator.validate(new Message("❤️❤️❤️️️")); // Invalid (The byte size of "text" must be less than or equal to 16. The given size is 24)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `byteSize.lessThan` | `The byte size of "{0}" must be less than {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |
| `byteSize.lessThanOrEqual` | `The byte size of "{0}" must be less than or equal to {1}. The given size is {2}"` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |
| `byteSize.greaterThan` | `The byte size of "{0}" must be greater than {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |
| `byteSize.greaterThanOrEqual` | `The byte size of "{0}" must be greater than or equal to {1}. The given size is {2}"` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |
| `byteSize.fixedSize` | `The byte size of "{0}" must be {1}. The given size is {2}"` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

## Integer/Short/Long/Character/Byte/Float/Long/BigInteger/BigDecimal

`Integer` に制約を適用するには、次のように書きます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraint(/* Function from TargetClass to Integer  */, /* Field Name */, /* Constraints */)
    .build();
```

型推論が曖昧になる場合は、`constraint` の代わりに `_integer` を明示的に使えます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._integer(/* Function from TargetClass to Integer  */, /* Field Name */, /* Constraints */)
    .build();
```

`Short`/`Long`/`Character`/`Byte`/`Float`/`Long` / `BigInteger` / `BigDecimal` も同様です。

### `notNull()`

対象の値が `null` でないことを検査します。

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.notNull())
    .build();

ConstraintViolations violations = validator.validate(new Age(30)); // Valid
ConstraintViolations violations = validator.validate(new Age(null)); // Invalid ("age" must not be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isNull()`

対象の値が `null` であることを検査します。

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.isNull())
    .build();

ConstraintViolations violations = validator.validate(new Age(null)); // Valid
ConstraintViolations violations = validator.validate(new Age(30)); // Invalid ("age" must be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.isNull` | `"{0}" must be null` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `greaterThan(...)`

対象の値が指定した値より大きいことを検査します。

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.greaterThan(20))
    .build();

ConstraintViolations violations = validator.validate(new Age(21)); // Valid
ConstraintViolations violations = validator.validate(new Age(null)); // Valid
ConstraintViolations violations = validator.validate(new Age(20)); // Invalid ("age" must be greater than 20)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `numeric.greaterThan` | `"{0}" must be greater than {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられた値 |

### `greaterThanOrEqual(...)`

対象の値が指定した値以上であることを検査します。

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.greaterThanOrEqual(20))
    .build();

ConstraintViolations violations = validator.validate(new Age(20)); // Valid
ConstraintViolations violations = validator.validate(new Age(null)); // Valid
ConstraintViolations violations = validator.validate(new Age(19)); // Invalid ("age" must be greater than or equal to 20)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `numeric.greaterThanOrEqual` | `"{0}" must be greater than or equal to {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられた値 |

### `lessThan(...)`

対象の値が指定した値より小さいことを検査します。

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.lessThan(20))
    .build();

ConstraintViolations violations = validator.validate(new Age(19)); // Valid
ConstraintViolations violations = validator.validate(new Age(null)); // Valid
ConstraintViolations violations = validator.validate(new Age(20)); // Invalid ("age" must be less than 20)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `numeric.lessThan` | `"{0}" must be less than {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられた値 |

### `lessThanOrEqual(...)`

対象の値が指定した値以下であることを検査します。

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.lessThanOrEqual(20))
    .build();

ConstraintViolations violations = validator.validate(new Age(19)); // Valid
ConstraintViolations violations = validator.validate(new Age(null)); // Valid
ConstraintViolations violations = validator.validate(new Age(21)); // Invalid ("age" must be less than or equal to 20)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `numeric.lessThanOrEqual` | `"{0}" must be less than or equal to {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられた値 |

### `positive()` / `positiveOrZero()` / `negative()` / `negativeOrZero()`

対象の値が正、正またはゼロ、負、負またはゼロであることを、それぞれ検査します。
これらの制約は `Byte`、`Short`、`Integer`、`Long`、`Float`、`Double`、`BigInteger`、`BigDecimal` に適用されます。

```java
Validator<Age> validator = ValidatorBuilder.<Age> of()
    .constraint(Age::asInt, "age", c -> c.positive())
    .build();

ConstraintViolations violations = validator.validate(new Age(1)); // Valid
ConstraintViolations violations = validator.validate(new Age(null)); // Valid
ConstraintViolations violations = validator.validate(new Age(0)); // Invalid ("age" must be positive)
```

`positiveOrZero`、`negative`、`negativeOrZero` も同じように使います。

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `numeric.positive` | `"{0}" must be positive` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |
| `numeric.positiveOrZero` | `"{0}" must be positive or zero` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |
| `numeric.negative` | `"{0}" must be negative` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |
| `numeric.negativeOrZero` | `"{0}" must be negative or zero` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

このページの制約は 1 つずつ使う必要はありません。次のプログラムはアカウントに対する文字列と数値の制約を組み合わせ、違反を出力します。Run を押すとブラウザでコンパイル・実行され、結果がセルの下に表示されます。

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

YAVI は次の型に対する時間の制約を、対応するビルダーのショートカットとともに提供します。

| 型 | 制約 | ビルダーのショートカット |
| --- | --- | --- |
| `LocalDate` | `am.ik.yavi.constraint.LocalDateConstraint` | `_localDate` |
| `LocalTime` | `am.ik.yavi.constraint.LocalTimeConstraint` | `_localTime` |
| `LocalDateTime` | `am.ik.yavi.constraint.LocalDateTimeConstraint` | `_localDateTime` |
| `OffsetDateTime` | `am.ik.yavi.constraint.OffsetDateTimeConstraint` | `_offsetDateTime` |
| `Instant` | `am.ik.yavi.constraint.InstantConstraint` | `_instant` |
| `Year` | `am.ik.yavi.constraint.YearConstraint` | `_year` |
| `YearMonth` | `am.ik.yavi.constraint.YearMonthConstraint` | `_yearMonth` |
| `ZonedDateTime` | `am.ik.yavi.constraint.ZonedDateTimeConstraint` | `_zonedDateTime` |

独自の `TemporalAccessor` 型は、`ChronoLocalDateConstraintBase`、`ChronoLocalDateTimeConstraintBase`、`ChronoZonedDateTimeConstraintBase` のいずれかを通じて `TemporalConstraintBase` を継承すると対応できます。
Kotlin DSL には対応するプロパティのオーバーロードがあります（Kotlin サポートの節を参照してください）。

次の制約は上記のすべての型に適用されます。

```java
Validator<Event> validator = ValidatorBuilder.<Event> of()
    ._localDate(Event::getDate, "date", c -> c.past())
    .build();
```

`null` は有効な値であり、時間の検査はすべてスキップされます。

`before` と `after` は境界と等しい値を拒否します。境界を含めて検査するには `beforeOrEqual` と `afterOrEqual` を使います。
引数は常にサプライヤです。

`before`、`beforeOrEqual`、`after`、`afterOrEqual`、`between` には、`boolean memoize` 引数を受け取るオーバーロードがあります。
`memoize` が `true`（既定値）のとき、サプライヤは最大 1 回だけ呼ばれ、比較と違反メッセージの両方に同じ値が使われます。
`false` のときは、使うたびにサプライヤが呼ばれるため、呼び出しごとに異なる値を返すサプライヤでは、一方の値で比較し、別の値をメッセージに報告することがあります。
検証時に評価する必要があるサプライヤには `false` を使います。

`past`、`pastOrPresent`、`future`、`futureOrPresent` メソッドにも、`java.time.Clock` を受け取るオーバーロードがあります。
これらのオーバーロードは、決定的なテストと、システムの既定のゾーン以外の時刻源に対する検証のためにあります。

### `past()` / `pastOrPresent()` / `future()` / `futureOrPresent()`

対象の値が過去、過去または現在、未来、現在または未来にあることを、それぞれ検査します。
オーバーロードは、現在時刻として使う `Clock` を受け取ります。

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

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `temporal.past` | `"{0}" must be a past date` | - `{0}` ... フィールド名<br>- `{1}` ... 比較に使う現在の日付<br>- `{2}` ... 与えられた値 |
| `temporal.pastOrPresent` | `"{0}" must be a date in the past or in the present` | - `{0}` ... フィールド名<br>- `{1}` ... 比較に使う現在の日付<br>- `{2}` ... 与えられた値 |
| `temporal.future` | `"{0}" must be a future date` | - `{0}` ... フィールド名<br>- `{1}` ... 比較に使う現在の日付<br>- `{2}` ... 与えられた値 |
| `temporal.futureOrPresent` | `"{0}" must be a date in the present or in the future` | - `{0}` ... フィールド名<br>- `{1}` ... 比較に使う現在の日付<br>- `{2}` ... 与えられた値 |

### `before(...)` / `beforeOrEqual(...)` / `after(...)` / `afterOrEqual(...)`

対象の値と、指定したサプライヤが供給する時間との関係が、前、前または等しい、後、後または等しいのいずれかであることを検査します。

```java
Validator<Event> validator = ValidatorBuilder.<Event> of()
    ._localDate(Event::getDate, "date", c -> c.before(() -> LocalDate.of(2024, 6, 1)))
    .build();

ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 5, 31))); // Valid
ConstraintViolations violations = validator.validate(new Event(null)); // Valid
ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 6, 1))); // Invalid ("date" has to be before 2024-06-01)
ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 6, 2))); // Invalid ("date" has to be before 2024-06-01)
```

`beforeOrEqual`、`after`、`afterOrEqual` も同じように使います。

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `temporal.before` | `"{0}" has to be before {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 供給された境界値<br>- `{2}` ... 与えられた値 |
| `temporal.beforeOrEqual` | `"{0}" has to be before or equals to {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 供給された境界値<br>- `{2}` ... 与えられた値 |
| `temporal.after` | `"{0}" has to be after {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 供給された境界値<br>- `{2}` ... 与えられた値 |
| `temporal.afterOrEqual` | `"{0}" has to be after or equals to {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 供給された境界値<br>- `{2}` ... 与えられた値 |

### `between(...)`

対象の値が、指定したサプライヤが供給する時間の間にあることを検査します。
範囲の両端は含みません。どちらかの境界と等しい値は無効です。
`rangeFrom` が `rangeTo` より後である場合は、検証時に `IllegalArgumentException` がスローされます。

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

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `temporal.between` | `"{0}" has to be between {1} and {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 供給された範囲の開始<br>- `{2}` ... 供給された範囲の終了<br>- `{3}` ... 与えられた値 |

### `fieldPredicate(...)`

指定した `java.time.temporal.TemporalField` の値を、指定した述語が受け入れることを検査します。

```java
Validator<Event> validator = ValidatorBuilder.<Event> of()
    ._localDate(Event::getDate, "date", c -> c.fieldPredicate(ChronoField.DAY_OF_WEEK, d -> d >= 1 && d <= 5))
    .build();

ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 6, 3))); // Valid
ConstraintViolations violations = validator.validate(new Event(null)); // Valid
ConstraintViolations violations = validator.validate(new Event(LocalDate.of(2024, 6, 1))); // Invalid (The DayOfWeek of "date" is invalid)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `temporal.field` | `The {1} of "{0}" is invalid` | - `{0}` ... フィールド名<br>- `{1}` ... 時間フィールド<br>- `{2}` ... 与えられた値 |

次のプログラムは固定したクロックに対して予約日を検証するため、実行した時刻によって結果が変わりません。違反を出力します。Run を押すとブラウザでコンパイル・実行され、結果がセルの下に表示されます。

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

`Boolean` に制約を適用するには、次のように書きます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraint(/* Function from TargetClass to Boolean  */, /* Field Name */, /* Constraints */)
    .build();
```

型推論が曖昧になる場合は、`constraint` の代わりに `_boolean` を明示的に使えます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._boolean(/* Function from TargetClass to Boolean  */, /* Field Name */, /* Constraints */)
    .build();
```

### `notNull()`

対象の値が `null` でないことを検査します。

```java
Validator<Confirmation> validator = ValidatorBuilder.<Confirmation> of()
    .constraint(Confirmation::isConfirmed, "confirmed", c -> c.notNull())
    .build();

ConstraintViolations violations = validator.validate(new Confirmation(true)); // Valid
ConstraintViolations violations = validator.validate(new Confirmation(null)); // Invalid ("confirmed" must not be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isNull()`

対象の値が `null` であることを検査します。

```java
Validator<Confirmation> validator = ValidatorBuilder.<Confirmation> of()
    .constraint(Confirmation::isConfirmed, "confirmed", c -> c.isNull())
    .build();

ConstraintViolations violations = validator.validate(new Confirmation(null)); // Valid
ConstraintViolations violations = validator.validate(new Confirmation(true)); // Invalid ("confirmed" must be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.isNull` | `"{0}" must be null` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isTrue()`

対象の値が `true` であることを検査します。

```java
Validator<Confirmation> validator = ValidatorBuilder.<Confirmation> of()
    .constraint(Confirmation::isConfirmed, "confirmed", c -> c.isTrue())
    .build();

ConstraintViolations violations = validator.validate(new Confirmation(true)); // Valid
ConstraintViolations violations = validator.validate(new Confirmation(null)); // Valid
ConstraintViolations violations = validator.validate(new Confirmation(false)); // Invalid ("confirmed" must be true)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `boolean.isTrue` | `"{0}" must be true` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |


### `isFalse()`

対象の値が `false` であることを検査します。

```java
Validator<Rented> validator = ValidatorBuilder.<Rented> of()
    .constraint(Rented::isRented, "rented", c -> c.isFalse())
    .build();

ConstraintViolations violations = validator.validate(new Rented(false)); // Valid
ConstraintViolations violations = validator.validate(new Rented(null)); // Valid
ConstraintViolations violations = validator.validate(new Rented(true)); // Invalid ("rented" must be false)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `boolean.isFalse` | `"{0}" must be false` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

## Collection

`Collection` に制約を適用するには、次のように書きます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraint(/* Function from TargetClass to Collection  */, /* Field Name */, /* Constraints */)
    .build();
```

型推論が曖昧になる場合は、`constraint` の代わりに `_collection` を明示的に使えます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._collection(/* Function from TargetClass to Collection  */, /* Field Name */, /* Constraints */)
    .build();
```

### `notNull()`

対象の値が `null` でないことを検査します。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.notNull())
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Invalid ("histories" must not be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isNull()`

対象の値が `null` であることを検査します。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
    .constraint(Histories::asList, "histories", c -> c.isNull())
    .build();

ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Invalid ("histories" must be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.isNull` | `"{0}" must be null` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `notEmpty()`

対象の値が空でないことを検査します。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.notEmpty())
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Invalid ("histories" must not be empty)
ConstraintViolations violations = validator.validate(new Histories(List.of())); // Invalid ("histories" must not be empty)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.notEmpty` | `"{0}" must not be empty` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `fixedSize(int)`

対象の値のサイズが指定したサイズであることを検査します。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.fixedSize(2))
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History()))); // Invalid (The size of "histories" must be 2. The given size is 1)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.fixedSize` | `The size of "{0}" must be {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `greaterThan(int)`

対象の値のサイズが指定したサイズより大きいことを検査します。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.greaterThan(1))
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History()))); // Invalid (The size of "histories" must be greater than 1. The given size is 1)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.greaterThan` | `The size of "{0}" must be greater than {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `greaterThanOrEqual(int)`

対象の値のサイズが指定したサイズ以上であることを検査します。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.greaterThanOrEqual(2))
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History()))); // Invalid (The size of "histories" must be greater than or equal to 2. The given size is 1)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.greaterThanOrEqual` | `The size of "{0}" must be greater than or equal to {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `lessThan(int)`

対象の値のサイズが指定したサイズより小さいことを検査します。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.lessThan(3))
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History(), new History()))); // Invalid (The size of "histories" must be less than 3. The given size is 3)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.lessThan` | `The size of "{0}" must be less than {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `lessThanOrEqual(int)`

対象の値のサイズが指定したサイズ以下であることを検査します。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.lessThanOrEqual(2))
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History()))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History(), new History(), new History()))); // Invalid (The size of "histories" must be less than or equal to 2. The given size is 3)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.lessThanOrEqual` | `The size of "{0}" must be less than or equal to {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `contains(...)`

対象の値が指定した値を含むことを検査します。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
		.constraint(Histories::asList, "histories", c -> c.contains(new History(2)))
		.build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(1), new History(2)))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History(3), new History(4), new History(5)))); // Invalid ("histories" must contain History{revision=2})
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `collection.contains` | `"{0}" must contain {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定した値<br>- `{2}` ... 与えられた値 |

### `unique()`

対象の値の要素が一意であることを検査します。
違反には、重複した要素が含まれます。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
    .constraint(Histories::asList, "histories", c -> c.unique())
    .build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(1), new History(2)))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History(1), new History(1)))); // Invalid ("histories" must be unique. [History{revision=1}] is/are duplicated.)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `collection.unique` | `"{0}" must be unique. {1} is/are duplicated.` | - `{0}` ... フィールド名<br>- `{1}` ... 重複した要素<br>- `{2}` ... 与えられた値 |

### `containsAll(Collection)`

対象の値が指定した要素をすべて含むことを検査します。
違反には、不足している要素が含まれます。
`Set` と `Set` 以外のコレクションは同じ意味で扱われます。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
    .constraint(Histories::asList, "histories", c -> c.containsAll(List.of(new History(1), new History(2))))
    .build();

ConstraintViolations violations = validator.validate(new Histories(List.of(new History(1), new History(2), new History(3)))); // Valid
ConstraintViolations violations = validator.validate(new Histories(null)); // Valid
ConstraintViolations violations = validator.validate(new Histories(List.of(new History(1)))); // Invalid ("histories" must be have of all of the following values: [History{revision=1}, History{revision=2}], missing values: [History{revision=2}])
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `collection.containsAll` | `"{0}" must be have of all of the following values: {1}, missing values: {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 必須の要素<br>- `{2}` ... 不足している要素<br>- `{3}` ... 与えられた値 |

### `forEach(...)` / `forEachIfPresent(...)`

`forEach` は、コレクションのプロパティの各要素に要素のバリデータを適用します。`forEachIfPresent` も同じですが、`null` のコレクションを有効として扱います。
コレクション内の `null` 要素は、プロパティ名に要素のインデックスを付けて（たとえば `histories[1]`）、`object.notNull` メッセージで報告されます。

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
    .forEach(Histories::asList, "histories",
            ValidatorBuilder.<History> of()
                .constraint(History::getRevision, "revision", c -> c.notNull())
                .build())
    .build();

ConstraintViolations violations = validator.validate(new Histories(Arrays.asList(new History(1), null))); // Invalid ("histories[1]" must not be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... インデックス付きのフィールド名 |

## Map

`Map` に制約を適用するには、次のように書きます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraint(/* Function from TargetClass to Map  */, /* Field Name */, /* Constraints */)
    .build();
```

型推論が曖昧になる場合は、`constraint` の代わりに `_map` を明示的に使えます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._map(/* Function from TargetClass to Map  */, /* Field Name */, /* Constraints */)
    .build();
```

### `notNull()`

対象の値が `null` でないことを検査します。

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.notNull())
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Invalid ("codeMap" must not be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `isNull()`

対象の値が `null` であることを検査します。

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
    .constraint(CodeMap::asMap, "codeMap", c -> c.isNull())
    .build();

ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Invalid ("codeMap" must be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.isNull` | `"{0}" must be null` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `notEmpty()`

対象の値が空でないことを検査します。

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.notEmpty())
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Invalid ("codeMap" must not be empty)
ConstraintViolations violations = validator.validate(new CodeMap(Map.of())); // Invalid ("codeMap" must not be empty)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.notEmpty` | `"{0}" must not be empty` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `fixedSize(int)`

対象の値のサイズが指定したサイズであることを検査します。

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.fixedSize(2))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of())); // Invalid (The size of "codeMap" must be 2. The given size is 1)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.fixedSize` | `The size of "{0}" must be {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `greaterThan(int)`

対象の値のサイズが指定したサイズより大きいことを検査します。

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.greaterThan(1))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A"))); // Invalid (The size of "codeMap" must be greater than 1. The given size is 1)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.greaterThan` | `The size of "{0}" must be greater than {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `greaterThanOrEqual(int)`

対象の値のサイズが指定したサイズ以上であることを検査します。

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.greaterThanOrEqual(2))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A"))); // Invalid (The size of "codeMap" must be greater than or equal to 2. The given size is 1)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.greaterThanOrEqual` | `The size of "{0}" must be greater than or equal to {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `lessThan(int)`

対象の値のサイズが指定したサイズより小さいことを検査します。

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.lessThan(3))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B", "c", "C"))); // Invalid (The size of "histories" must be less than 3. The given size is 3)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.lessThan` | `The size of "{0}" must be less than {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `lessThanOrEqual(int)`

対象の値のサイズが指定したサイズ以下であることを検査します。

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.lessThanOrEqual(2))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B", "c", "C"))); // Invalid (The size of "histories" must be less than or equal to 2. The given size is 3)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `container.lessThanOrEqual` | `The size of "{0}" must be less than or equal to {1}. The given size is {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したサイズ<br>- `{2}` ... 与えられたサイズ<br>- `{3}` ... 与えられた値 |

### `containsKey(...)`

対象の値が指定したキーを含むことを検査します。

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.containsKey("b"))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("c", "C"))); // Invalid ("codeMap" must contain key b)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `map.containsKey` | `"{0}" must contain key {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したキー<br>- `{2}` ... 与えられた値 |

### `containsValue(...)`

対象の値が指定した値を含むことを検査します。

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
		.constraint(CodeMap::asMap, "codeMap", c -> c.containsValue("B"))
		.build();

ConstraintViolations violations = validator.validate(new CodeMap(Map.of("a", "A", "b", "B"))); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(null)); // Valid
ConstraintViolations violations = validator.validate(new CodeMap(Map.of("c", "C"))); // Invalid ("codeMap" must contain value B)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `map.containsValue` | `"{0}" must contain value {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定した値<br>- `{2}` ... 与えられた値 |

## Object

任意の `Object` に制約を適用するには、次のように書きます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraint(/* Function from TargetClass to Object  */, /* Field Name */, /* Constraints */)
    .build();
```

型推論が曖昧になる場合は、`constraint` の代わりに `_object` を明示的に使えます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._object(/* Function from TargetClass to Object  */, /* Field Name */, /* Constraints */)
    .build();
```

対象のクラス自体に制約を適用する場合（[クロスフィールド検証](using-yavi.md#cross-field-validation)など）は、次のように `constraintOnTarget` を使えます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    .constraintOnTarget(/* Field Name */, /* Constraints */)
    .build();
```

これは次の記述と同じです。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._object(Function.identity(), /* Field Name */, /* Constraints */)
    .build();
```

### `notNull()`

対象の値が `null` でないことを検査します。

```java
Validator<CreatedAt> validator = ValidatorBuilder.<CreatedAt> of()
    .constraint(CreatedAt::asInstant, "createdAt", c -> c.notNull())
    .build();

ConstraintViolations violations = validator.validate(new CreatedAt(Instant.now())); // Valid
ConstraintViolations violations = validator.validate(new CreatedAt(null)); // Invalid ("createdAt" must not be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.notNull` | `"{0}" must not be null` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |


### `isNull()`

対象の値が `null` であることを検査します。

```java
Validator<CreatedAt> validator = ValidatorBuilder.<CreatedAt> of()
    .constraint(CreatedAt::asInstant, "createdAt", c -> c.isNull())
    .build();


ConstraintViolations violations = validator.validate(new CreatedAt(null)); // Valid
ConstraintViolations violations = validator.validate(new CreatedAt(Instant.now())); // Invalid ("createdAt" must be null)
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.isNull` | `"{0}" must be null` | - `{0}` ... フィールド名<br>- `{1}` ... 与えられた値 |

### `equalTo(Object)`

対象の値が指定した値に等しいことを検査します。

```java
Validator<Box> validator = ValidatorBuilder.<Box> of()
    ._object(Box::getValue, "value", c -> c.equalTo("expected"))
    .build();

ConstraintViolations violations = validator.validate(new Box("expected")); // Valid
ConstraintViolations violations = validator.validate(new Box(null)); // Valid
ConstraintViolations violations = validator.validate(new Box("other")); // Invalid ("value" must be equal to expected)
```

### `oneOf(Collection)`

対象の値が指定した値のいずれかであることを検査します。

```java
Validator<Box> validator = ValidatorBuilder.<Box> of()
    ._object(Box::getValue, "value", c -> c.oneOf(List.of("a", "b")))
    .build();

ConstraintViolations violations = validator.validate(new Box("a")); // Valid
ConstraintViolations violations = validator.validate(new Box(null)); // Valid
ConstraintViolations violations = validator.validate(new Box("c")); // Invalid ("value" must be one of the following values: [a, b])
```

### `notOneOf(Collection)`

対象の値が指定した値のいずれでもないことを検査します。

```java
Validator<Box> validator = ValidatorBuilder.<Box> of()
    ._object(Box::getValue, "value", c -> c.notOneOf(List.of("a", "b")))
    .build();

ConstraintViolations violations = validator.validate(new Box("c")); // Valid
ConstraintViolations violations = validator.validate(new Box(null)); // Valid
ConstraintViolations violations = validator.validate(new Box("a")); // Invalid ("value" must not be one of the following values: [a, b])
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.equalTo` | `"{0}" must be equal to {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定した値<br>- `{2}` ... 与えられた値 |
| `object.oneOf` | `"{0}" must be one of the following values: {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 許可される値<br>- `{2}` ... 与えられた値 |
| `object.notOneOf` | `"{0}" must not be one of the following values: {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 禁止される値<br>- `{2}` ... 与えられた値 |

### `password()`

対象の値が指定した独自のパスワードポリシーを満たすことを検査します（クロスフィールド検査など）。

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

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `password.required` | `"{0}" must meet {1} policy` | - `{0}` ... フィールド名<br>- `{1}` ... 指定したポリシー名<br>- `{2}` ... 与えられた値 |
| `password.optional` | `"{0}" must meet at least {1} policies from {2}` | - `{0}` ... フィールド名<br>- `{1}` ... 最低要件<br>- `{2}` ... 指定したポリシー名<br>- `{3}` ... 与えられた値 |

## Enum

enum に制約を適用するには、次のように書きます。

```java
Validator<Account> validator = ValidatorBuilder.<Account> of()
    ._enum(Account::getStatus, "status", c -> c.oneOf(Status.ACTIVE))
    .build();

ConstraintViolations violations = validator.validate(new Account(Status.ACTIVE)); // Valid
ConstraintViolations violations = validator.validate(new Account(null)); // Valid
ConstraintViolations violations = validator.validate(new Account(Status.INACTIVE)); // Invalid ("status" must be one of the following values: [ACTIVE])
```

`_enum` は `constraint(ToEnum, String, Function)` の別名です。
同じ `_enum` メソッドは、enum の引数を検証するために `ArgumentsValidatorBuilder` と `YaviArguments` でも使えます。

`EnumConstraint#oneOf(E...)` は、値が指定した enum 定数のいずれかであることを要求します。
引数のリストが空の場合は `IllegalArgumentException` で拒否されます。
`notNull()` を追加しない限り、`null` は有効な値です。
違反には `object.oneOf` メッセージキーが使われます。

単一の enum の値は、次のように `EnumValidatorBuilder` と `EnumValidator` で検証できます。

```java
EnumValidator<Status, Status> validator = EnumValidatorBuilder.<Status> of("status",
        c -> c.oneOf(Status.ACTIVE)).build();

Validated<Status> result = validator.validate(Status.INACTIVE); // Invalid ("status" must be one of the following values: [ACTIVE])
```

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `object.oneOf` | `"{0}" must be one of the following values: {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 許可される値<br>- `{2}` ... 与えられた値 |

## Array

配列に制約を適用するには、次のように書きます。

```java
Validator<TargetClass> validator = ValidatorBuilder.<TargetClass> of()
    ._objectArray(/* Function from TargetClass to an object array  */, /* Field Name */, /* Constraints */)
    .build();
```

次の制約とビルダーのショートカットが使えます。

| 配列の型 | ビルダーのショートカット |
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

`am.ik.yavi.constraint.array` の制約は `notNull`、`isNull`、`notEmpty`、`fixedSize`、`greaterThan`、`greaterThanOrEqual`、`lessThan`、`lessThanOrEqual`、`contains` を提供します。
`null` の配列は有効で、`notEmpty()` を除いて検査はスキップされます。

```java
Validator<Values> validator = ValidatorBuilder.<Values> of()
    ._intArray(Values::getNumbers, "numbers", c -> c.contains(3).fixedSize(3))
    .build();

ConstraintViolations violations = validator.validate(new Values(new int[] { 1, 2, 3 })); // Valid
ConstraintViolations violations = validator.validate(new Values(null)); // Valid
ConstraintViolations violations = validator.validate(new Values(new int[] { 1, 2 })); // Invalid ("numbers" must contain 3, The size of "numbers" must be 3. The given size is 2)
```

サイズと null の検査は、Collection の節で説明した `object.notNull`、`object.isNull`、`container.*` のメッセージキーを報告します。
`contains` は `array.contains` を報告します。

| メッセージキー | 既定のメッセージ形式 | 引数 |
| --- | --- | --- |
| `array.contains` | `"{0}" must contain {1}` | - `{0}` ... フィールド名<br>- `{1}` ... 指定した値<br>- `{2}` ... 与えられた値 |
