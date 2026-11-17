# Using YAVI

This section describes the basic usage of YAVI.

## Defining and obtaining a core `Validator` instance

The core validator `am.ik.yavi.core.Validator` can be defined and obtained via `am.ik.yavi.builder.ValidatorBuilder`.
Here is an example:

```java
Validator<User> validator = ValidatorBuilder.<User> of() // or ValidatorBuilder.of(User.class)
    .constraint(User::getName, "name", c -> c.notNull().lessThanOrEqual(20))
    .constraint(User::getEmail, "email", c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50).email())
    .constraint(User::getAge, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200))
    .build();

ConstraintViolations violations = validator.validate(user);
boolean isValid = violations.isValid();
violations.forEach(x -> System.out.println(x.message()));
```

> **Tip**
>
> YAVI accumulates all violation messages by default. If a violation is found during the validation, it will not be shortcut. If you want to return from the current validation as soon as the first constraint violation occurs, use "[fail fast mode](#fail-fast-mode)".

In order to avoid ambiguous type inferences, you can use explicit `_<type>` method per type instead of constraint as follows:

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    ._string(User::getName, "name", c -> c.notNull().lessThanOrEqual(20))
    ._string(User::getEmail, "email", c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50).email())
    ._integer(User::getAge, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200))
    .build();
```

The first argument of the `constraint` method does not have to be a "Getter" as long as it is a `java.util.function.Function`.

For example, If you want to create a `Validator` for [Records](https://openjdk.java.net/jeps/395), you can implement it straightforwardly like bellow:

```java
public record User(String name, String email, int age) {
}

Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(User::name, "name", c -> c.notNull().lessThanOrEqual(20))
    .constraint(User::email, "email", c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50).email())
    .constraint(User::age, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200))
    .build();
```

Or if you don't want your method to expose the target field, you can define the constraint like following:

```java
public class User {
    private final String name;

    public final Validator<User> validator = ValidatorBuilder.<User> of()
        ._string(x -> x.name, "name", c -> c.notNull().lessThanOrEqual(20))
        .build();

    public User(String name) {
        this.name = name;
    }
    // Omits others
}
```

See "[Built-in Constraints](built-in-constraints.md)" for other constraint rules.

> **Tip**
>
> If you don't like to specify the field name as a string literal, you can use "[Annotation Processor](annotation-processor.md)" to make it completely type-safe.

## Constraints on nested objects

You can use `nest` method to apply constraints to the nested fields.
You can delegate to the `Validator` for the nested field, or you can also define a set of constraints on the nested field inside.

```java
public class Address {
    private Country country;
    private City city;
    // Omits other fields and getters
}

Validator<Country> countryValidator = ValidatorBuilder.<Country> of()
    .constraint(Country::getName, "name", c -> c.notBlank().lessThanOrEqual(20))
    .build();
Validator<City> cityValidator = ValidatorBuilder.<City> of()
    .constraint(City::getName, "name", c -> c.notBlank().lessThanOrEqual(100))
    .build();

Validator<Address> validator = ValidatorBuilder.<Address> of()
    .nest(Address::getCountry, "country", countryValidator)
    .nest(Address::getCity, "city", cityValidator)
    .build();
```

Or:

```java
Validator<Address> validator = ValidatorBuilder.<Address> of()
    .nest(Address::getCountry, "country",
        b -> b.constraint(Country::getName, "name", c -> c.notBlank().lessThanOrEqual(20)))
    .nest(Address::getCity, "city",
        b -> b.constraint(City::getName, "name", c -> c.notBlank().lessThanOrEqual(100)))
    .build();
```

If the nested field is nullable, use `nestIfPresent` instead of `nest`.

The program below nests the country and city validators and prints the violation names, which carry the nested path. Press Run to compile and execute it in your browser; the output appears below the cell.

```java runnable
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

public class Main {

    record Country(String name) {
    }

    record City(String name) {
    }

    record Address(Country country, City city) {
    }

    static final Validator<Country> countryValidator = ValidatorBuilder.<Country>of()
        .constraint(Country::name, "name", c -> c.notBlank().lessThanOrEqual(20))
        .build();

    static final Validator<City> cityValidator = ValidatorBuilder.<City>of()
        .constraint(City::name, "name", c -> c.notNull().notBlank().lessThanOrEqual(100))
        .build();

    static final Validator<Address> validator = ValidatorBuilder.<Address>of()
        .nest(Address::country, "country", countryValidator)
        .nest(Address::city, "city", cityValidator)
        .build();

    public static void main(String[] args) {
        ConstraintViolations violations = validator
            .validate(new Address(new Country(""), new City(null)));
        System.out.println("violations: " + violations.size());
        violations.forEach(v -> System.out.println("- " + v.name() + ": " + v.message()));
    }
}
```
<!-- run: expected output
violations: 3
- country.name: "country.name" must not be blank
- city.name: "city.name" must not be null
- city.name: "city.name" must not be blank
-->

## Constraints on elements in a Collection / Map / Array

You can use `forEach` method to apply constraints to each element of Collection / Map / Array.
Like nested fields, You can delegate to the `Validator` for validating each element,
or you can also define a set of constraints on the elements inside.

```java
public class History {
	private final int revision;

	public History(int revision) {
		this.revision = revision;
	}

	public int getRevision() {
		return revision;
	}
}

public class Histories {
	private final List<History> value;

	public Histories(List<History> value) {
		this.value = value;
	}

	public List<History> asList() {
		return value;
	}
}

Validator<History> historyValidator = ValidatorBuilder.<History> of()
    .constraint(History::getRevision, "revision", c -> c.notNull().greaterThanOrEqual(1))
    .build();

Validator<Histories> validator = ValidatorBuilder.<Histories> of()
    .forEach(Histories::asList, "histories", historyValidator)
    .build();
```

Or:

```java
Validator<Histories> validator = ValidatorBuilder.<Histories> of()
    .forEach(Histories::asList, "histories",
        b -> b.constraint(History::getRevision, "revision", c -> c.notNull().greaterThanOrEqual(1)))
    .build();
```

If the colletion / map / array field is nullable, use `forEachIfPresent` instead of `forEach`.

> **Note**
>
> For the constraints on elements in a map, only values can be applied out of the box.
> If you want to apply constraints on keys in a map, you need to convert the map to key's `Set` and regard it as a collection as follows
>
> ```java
> ToCollection<CodeMap, Collection<String>, String> toCollection = codeMap -> codeMap.asMap().keySet();
> Validator<CodeMap> validator = ValidatorBuilder.<CodeMap> of()
>     .forEach(toCollection, "codeMap", b -> b._string(String::toString, "value", c -> c.notEmpty()))
>     .build();
> ```

The program below applies the element validator to a list and prints the violations, whose names include the element index. Press Run to compile and execute it in your browser; the output appears below the cell.

```java runnable
import java.util.List;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

public class Main {

    record History(int revision) {
    }

    record Histories(List<History> value) {
    }

    static final Validator<History> historyValidator = ValidatorBuilder.<History>of()
        .constraint(History::revision, "revision", c -> c.greaterThanOrEqual(1))
        .build();

    static final Validator<Histories> validator = ValidatorBuilder.<Histories>of()
        .forEach(Histories::value, "histories", historyValidator)
        .build();

    public static void main(String[] args) {
        ConstraintViolations violations = validator
            .validate(new Histories(List.of(new History(0), new History(1), new History(-1))));
        System.out.println("violations: " + violations.size());
        violations.forEach(v -> System.out.println("- " + v.name() + ": " + v.message()));
    }
}
```
<!-- run: expected output
violations: 2
- histories[0].revision: "histories[0].revision" must be greater than or equal to 1
- histories[2].revision: "histories[2].revision" must be greater than or equal to 1
-->

## Applying constraints only to specific conditions

You can apply constraints only to specific conditions with `am.ik.yavi.core.ConstraintCondition`.
Since 0.11.0 a condition receives an `am.ik.yavi.core.ConstraintContext` instead of a `ConstraintGroup`, so it can read arbitrary attributes of the validation call:

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraintOnCondition((user, context) -> !user.getName().isEmpty(),
        b -> b.constraint(User::getEmail, "email", c -> c.email().notEmpty()))
    .build();
```

The constraint above on email will only be activated if the name is not empty.

A `ConstraintContext` can be created from a `Map` or from a lookup function, and passed to the validation method:

```java
ConstraintContext context = ConstraintContext.from(Map.of("country", "IT"));
// or
ConstraintContext context = ConstraintContext.from(key -> lookupAttribute(key));

Validator<User> validator = ValidatorBuilder.<User> of()
    .constraintOnCondition((user, ctx) -> "IT".equals(ctx.attribute("country").value()),
        b -> b.constraint(User::getTaxId, "taxId", c -> c.notBlank()))
    .build();

ConstraintViolations violations = validator.validate(user, context);
```

`ConstraintCondition.hasAttribute(String)` and `ConstraintCondition.hasAttributeWithValue(String, Object)` are shortcuts for the common attribute checks:

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraintOnCondition(ConstraintCondition.hasAttributeWithValue("country", "IT"),
        b -> b.constraint(User::getTaxId, "taxId", c -> c.notBlank()))
    .build();
```

If the attribute is absent, `context.attribute("key").value()` returns `null`.

## Applying constraints only to specific groups

A `ConstraintGroup` is a `ConstraintContext` that only carries a name.
You can apply constraints only to specific groups as follows:

```java
enum Group implements ConstraintGroup {
    CREATE, UPDATE, DELETE
}

Validator<User> validator = ValidatorBuilder.<User> of()
    .constraintOnCondition(Group.CREATE.toCondition(), b -> b.constraint(User::getId, "id", c -> c.isNull()))
    .constraintOnCondition(Group.UPDATE.toCondition(), b -> b.constraint(User::getId, "id", c -> c.notNull()))
    .build();
```

A group can also be created from a name without declaring an enum:

```java
ConstraintGroup create = ConstraintGroup.of("CREATE");
```

The group to validate can be specified in `validate` method:

```java
ConstraintViolations violations = validator.validate(user, ConstraintGroup.of("CREATE"));
// or, with the enum above
violations = validator.validate(user, Group.CREATE);
```

You can use a shortcut `constraintOnGroup` method

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraintOnGroup(Group.CREATE, b -> b.constraint(User::getId, "id", c -> c.isNull()))
    .constraintOnGroup(Group.UPDATE, b -> b.constraint(User::getId, "id", c -> c.notNull()))
    .build();
```

> **Note**
>
> Note that all constraints without conditions will be validated for any constraint group.
> Also, if no group is specified in the `validate` method, `ConstraintGroup.DEFAULT` is used.
> Groups match by name; two groups are equal when their names are equal.

## Creating a custom constraint

If you want to apply constraints that are not in the "[Built-in Constraints](built-in-constraints.md)", you can create custom constraints by implementing `am.ik.yavi.core.CustomConstraint` interface as bellow:

```java
public class IsbnConstraint implements CustomConstraint<String> {

    @Override
    public boolean test(String s) {
        // Delegate processing to another method
        return ISBNValidator.isISBN13(s);
    }

    @Override
    public String messageKey() {
        return "string.isbn13";
    }

    @Override
    public String defaultMessageFormat() {
        return "\"{0}\" must be ISBN13 format";
    }
}
```

The created custom constraint can be specified by `predicate` method as follows:

```java
IsbnConstraint isbn = new IsbnConstraint();
Validator<Book> book = ValidatorBuilder.<Book> of()
    .constraint(Book::getTitle, "title", c -> c.notBlank().lessThanOrEqual(64))
    .constraint(Book::getIsbn, "isbn", c -> c.notBlank().predicate(isbn))
    .build();
```

You can also write constraint rules directly in the `predicate` method instead of defining the `CustomConstraint` class.

```java
Validator<Book> book = ValidatorBuilder.<Book> of()
    .constraint(Book::getTitle, "title", c -> c.notBlank().lessThanOrEqual(64))
    .constraint(Book::getIsbn, "isbn", c -> c.notBlank()
        .predicate(s -> ISBNValidator.isISBN13(s), "string.isbn13", "\"{0}\" must be ISBN13 format"))
    .build();
```

The first argument of the violation message is the field name. Also, the last argument is the violated value.

If you want to use other arguments, override `arguments` method as bellow:

```java
public class InstantRangeConstraint implements CustomConstraint<Instant> {

    private final Instant end;

    private final Instant start;

    InstantRangeConstraint(Instant start, Instant end) {
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
    }

    @Override
    public Object[] arguments(Instant violatedValue) {
        return new Object[] { this.start /* {1} */, this.end /* {2} */};
    }

    @Override
    public String defaultMessageFormat() {
        return "Instant value \"{0}\" must be between \"{1}\" and \"{2}\".";
    }

    @Override
    public String messageKey() {
        return "instant.range";
    }

    @Override
    public boolean test(Instant instant) {
        return instant.isAfter(this.start) && instant.isBefore(this.end);
    }
}
```

The program below implements an ISBN-13 check as a `CustomConstraint` and applies it with `predicate`. It prints the violation for an invalid checksum and accepts a valid ISBN. Press Run to compile and execute it in your browser; the output appears below the cell.

```java runnable
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.CustomConstraint;
import am.ik.yavi.core.Validator;

public class Main {

    record Book(String title, String isbn) {
    }

    static final class Isbn13Constraint implements CustomConstraint<String> {

        @Override
        public boolean test(String isbn) {
            if (isbn == null || !isbn.matches("[0-9]{13}")) {
                return false;
            }
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                int digit = isbn.charAt(i) - '0';
                sum += (i % 2 == 0) ? digit : digit * 3;
            }
            int checkDigit = (10 - sum % 10) % 10;
            return checkDigit == isbn.charAt(12) - '0';
        }

        @Override
        public String messageKey() {
            return "string.isbn13";
        }

        @Override
        public String defaultMessageFormat() {
            return "\"{0}\" must be ISBN13 format";
        }

    }

    static final Validator<Book> validator = ValidatorBuilder.<Book>of()
        .constraint(Book::title, "title", c -> c.notBlank().lessThanOrEqual(64))
        .constraint(Book::isbn, "isbn",
                c -> c.notBlank().predicate(new Isbn13Constraint()))
        .build();

    public static void main(String[] args) {
        ConstraintViolations violations = validator
            .validate(new Book("Effective Java", "9780306406158"));
        System.out.println("violations: " + violations.size());
        violations.forEach(v -> System.out.println("- " + v.message()));

        System.out.println("valid ISBN is accepted: "
                + validator.validate(new Book("Effective Java", "9780306406157")).isValid());
    }
}
```
<!-- run: expected output
violations: 1
- "isbn" must be ISBN13 format
valid ISBN is accepted: true
-->

## Cross-field validation

If you want to apply constraints on target class itself, you can use `constraintOnTarget`.
It can be used when you want to apply cross-field constraints as follows:

```java
Validator<Range> validator = ValidatorBuilder.<Range> of()
    .constraint(range::getFrom, "from", c -> c.greaterThan(0))
    .constraint(range::getTo, "to", c -> c.greaterThan(0))
    .constraintOnTarget(range -> range.getTo() > range.getFrom(), "to", "to.isGreaterThanFrom", "\"to\" must be greater than \"from\"")
    .build();
```

You can also create a custom constraint for the cross-field validation as follows:

```java
public class RangeConstraint implements CustomConstraint<Range> {
    @Override
    public String defaultMessageFormat() {
        return "\"to\" must be greater than \"from\"";
    }

    @Override
    public String messageKey() {
        return "to.isGreaterThanFrom";
    }

    @Override
    public boolean test(Range range) {
        return range.getTo() > range.getFrom();
    }
}

RangeConstraint range = new RangeConstraint();
Validator<Range> validator = ValidatorBuilder.<Range> of()
        .constraintOnTarget(range, "to")
        .build();
```

## Overriding violation messages

The default violation message for each constraint is defined in "[Built-in Constraints](built-in-constraints.md)".

If you want to customize the violation message, append `message` method on the target constraint as follows:

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(User::getName, "name", c -> c.notNull().message("{0} is required!")
        .greaterThanOrEqual(1).message("{0} is too small!")
        .lessThanOrEqual(20).message("{0} is too large!"))
    .build();
```

`message` also accepts an `am.ik.yavi.core.ViolationMessage` that carries the message key and the default message format, for example `ViolationMessage.of("string.isbn13", "\"{0}\" must be ISBN13 format")`.
Since 0.17.2, overriding the message of a constraint preserves the violated value in the message arguments.

The program below installs a custom `MessageFormatter` and overrides two constraint messages with `message` and `ViolationMessage`. It prints the resulting messages. Press Run to compile and execute it in your browser; the output appears below the cell.

```java runnable
import java.util.Locale;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import am.ik.yavi.core.ViolationMessage;
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;

public class Main {

    record User(String name, String email) {
    }

    static final class PrefixedMessageFormatter implements MessageFormatter {

        @Override
        public String format(String messageKey, String defaultMessageFormat, Object[] args,
                Locale locale) {
            String message = SimpleMessageFormatter.getInstance()
                .format(messageKey, defaultMessageFormat, args, locale);
            return "[ERR] " + message;
        }

    }

    static final Validator<User> validator = ValidatorBuilder.<User>of()
        .messageFormatter(new PrefixedMessageFormatter())
        .constraint(User::name, "name", c -> c.notNull().message("{0} is required!"))
        .constraint(User::email, "email", c -> c.notBlank()
            .email()
            .message(ViolationMessage.of("email.invalid", "\"{0}\" is not a valid address"))
            .lessThanOrEqual(50)
            .message("{0} is too long!"))
        .build();

    public static void main(String[] args) {
        ConstraintViolations violations = validator.validate(new User(null, "john@"));
        System.out.println("violations: " + violations.size());
        violations.forEach(v -> System.out.println("- " + v.message()));
    }
}
```
<!-- run: expected output
violations: 2
- [ERR] name is required!
- [ERR] "email" is not a valid address
-->

## Value validators

`am.ik.yavi.core.ValueValidator` validates an input and produces a validated value.
It is the applicative counterpart of `Validator`; see "[Combining Validators](combining-validators.md)" for combining value validators and "[Combining Validation Results](combining-validation-results.md)" for the `Validated` result type.

A value validator maps the validated value with `map`:

```java
ValueValidator<String, String> trimmed = ValueValidator.<String> passThrough().map(String::trim);
```

`andThen(Function)` is deprecated since 0.17.0; use `map(Function)` instead.
`andThen(ValueValidator)` is still supported and chains validators, applying the next validator to the value produced by the previous one:

```java
ValueValidator<String, Integer> length = trimmed
    .andThen(ValueValidator.<String> passThrough().map(String::length));
```

`validated(...)` validates the input and returns the value, throwing `am.ik.yavi.core.ConstraintViolationsException` when a violation exists.
The same behavior is available through `Validation#throwIfInvalid` and `ConstraintViolations#throwIfInvalid`:

```java
try {
    String value = trimmed.validated(input);
}
catch (ConstraintViolationsException e) {
    // handle the violations
}

ConstraintViolations violations = validator.validate(user);
violations.throwIfInvalid(ConstraintViolationsException::new);
```

A value validator can be converted to a regular `Validator` with `Validatable.from(ValueValidator)`, which discards the produced value and only reports the violations:

```java
Validatable<String> validator = Validatable.from(trimmed);
```

## Handling violation results

`ConstraintViolation` has a staged builder since 0.15.0.
The `name`, `messageKey` and `defaultMessageFormat` stages are required; the argument, formatter and locale methods are optional:

```java
ConstraintViolation violation = ConstraintViolation.builder()
    .name("email")
    .messageKey("email.invalid")
    .defaultMessageFormat("\"{0}\" must be a valid email address")
    .argsWithPrependedNameAndAppendedViolatedValue(new Object[] {}, new ViolatedValue(input))
    .messageFormatter(SimpleMessageFormatter.getInstance())
    .locale(Locale.JAPAN)
    .build();
```

`ConstraintViolation#violatedValue()` returns the value that violated the constraint.

`SimpleMessageFormatter` can be obtained with `SimpleMessageFormatter.getInstance()`; the public no-arg constructor is still available but is deprecated since 0.16.0.

## Message Formatter

YAVI provides `am.ik.yavi.message.MessageFormatter` interface for constructing violation messages.

By default, `am.ik.yavi.message.SimpleMessageFormatter` is used, which simply uses `java.text.MessageFormat` to interpolate the message.
A shared instance is available with `SimpleMessageFormatter.getInstance()`; the public no-arg constructor is kept for backward compatibility but is deprecated.
A list of message keys and default message formats is given in "[Built-in Constraints](built-in-constraints.md)".

> **Note**
>
> As a feature of error messages, the following are supported compared to Bean Validation:
>
> * Include field name in error message by default
> * Allows you to include the violated values in the error message
>
> The first placeholder `{0}` of the message is set to the field name, and the last placeholder is set to the violation value.
>
> Especially for the second one, since it is not supported by the general Validation library, for example, even if the error message "xyz should be 100 characters or less" is returned, what characters are actually entered now? Sometimes I try to cut the letters little by little because I don't know if they are counted. By default, the following message is displayed so that the user does not have to do this wasteful thing.
>
> ![](https://user-images.githubusercontent.com/106908/44784067-4b010600-abc7-11e8-8878-930d017405bb.png)

If you want to customize the message interpolation, implement `MessageFormatter`.
As an example, the implementation that reads messages in `messages.properties` is shown as follows:

```java
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import am.ik.yavi.message.MessageFormatter;

public enum ResourceBundleMessageFormatter implements MessageFormatter {
    SINGLETON;

    @Override
    public String format(String messageKey, String defaultMessageFormat, Object[] args,
            Locale locale) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", locale);
        String format;
        try {
            format = resourceBundle.getString(messageKey);
        }
        catch (MissingResourceException e) {
            format = defaultMessageFormat;
        }
        try {
            String target = resourceBundle.getString((String) args[0] /* field name */);
            args[0] = target;
        }
        catch (MissingResourceException e) {
        }
        return new MessageFormat(format, locale).format(args);
    }
}
```

If you want to replace the `MessageFormatter`, you can set it as follows.

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .messageFormatter(ResourceBundleMessageFormatter.SINGLETON)
    // ...
    .build();
```

## Fail fast mode

Using the fail fast mode, YAVI allows to return from the current validation as soon as the first constraint violation occurs.
This can be useful for the validation of large object graphs where you are only interested in a quick check whether there is any constraint violation at all.

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .constraint(User::getName, "name", c -> c.notNull().lessThanOrEqual(20))
    .constraint(User::getEmail, "email", c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50).email())
    .constraint(User::getAge, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200))
    .failFast(true) // <-- Enable the fail fast mode
    .build();
```

You can switch an existing `Validator` to the fail fast mode as follows:

```java
Validator<User> failFastValidator = validator.failFast(true);
```

The `failFast(boolean)` and `isFailFast()` methods are also declared on `am.ik.yavi.core.Validatable`, so the mode can be read and switched through that interface:

```java
boolean failFast = failFastValidator.isFailFast();
Validatable<User> validatable = validator.failFast(true);
```

Since 0.13.0 nested, collection and conditional validators honor the parent's fail-fast mode.
The mode is passed down to nested validators and element validators, so the validation stops at the first violation in those validators as well.

> **Note**
>
> This feature comes from [the fail fast mode of Hibernate Validator.](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-fail-fast)

## Resolving conflicting constraint names

When the same constraint name is registered more than once, `ValidatorBuilder#conflictStrategy(ConflictStrategy)` decides what happens when the validator is built.
The default, `am.ik.yavi.builder.ConflictStrategy.NOOP`, keeps the existing constraints and appends the new one, so the target is validated by all of them.
`ConflictStrategy.OVERRIDE` removes the existing constraints and keeps only the new one:

```java
Validator<User> validator = ValidatorBuilder.<User> of()
    .conflictStrategy(ConflictStrategy.OVERRIDE)
    .constraint(User::getName, "name", c -> c.notNull())
    .constraint(User::getName, "name", c -> c.notBlank()) // replaces the previous "name" constraint
    .build();
```

A custom strategy can mutate the list of existing predicates; the new predicate is appended after the strategy returns:

```java
ConflictStrategy keepFirst = new ConflictStrategy() {
    @Override
    public <T> void resolveConflict(List<ConstraintPredicates<T, ?>> predicatesList,
            ConstraintPredicates<T, ?> predicates) {
        while (predicatesList.size() > 1) {
            predicatesList.remove(predicatesList.size() - 1);
        }
    }
};
```

## Kotlin Support

If you are using Kotlin, you can define a `Validator` with DSL as follows.
The DSL obtains property names with Kotlin reflection, so `kotlin-reflect` must be on the runtime classpath.

```kotlin
val validator = validator<User> {
	User::name {
		notNull()
		lessThanOrEqual(20)
	}
	User::email {
		notNull()
		greaterThanOrEqual(5)
		lessThanOrEqual(50)
		email()
	}
	User::age {
		notNull()
		greaterThanOrEqual(0)
		lessThanOrEqual(100)
	}
}
```

Field names can be overridden like bellow:

```kotlin
val validator = validator<User> {
	(User::name)("Name") {
		notNull()
		lessThanOrEqual(20)
	}
	(User::email)("Email") {
		notNull()
		greaterThanOrEqual(5)
		lessThanOrEqual(50)
		email()
	}
	(User::age)("Age") {
		notNull()
		greaterThanOrEqual(0)
		lessThanOrEqual(100)
	}
}
```

`forEach` example:

```kotlin
val validator = validator<DemoForEachCollection> {
    DemoForEachCollection::x forEach {
        DemoString::x {
            greaterThan(1)
            lessThan(5)
        }
    }
}
```

`nest` example:

```kotlin
val validator = validator<DemoNested> {
    DemoNested::x nest {
        DemoString::x {
            greaterThan(1)
            lessThan(5)
        }
    }
}
```

`nest` delegate example:

```kotlin
val validator = validator<DemoNested> {
    DemoNested::x nest demoStringValidator
}
```

`nestIfPresent` and `forEachIfPresent` validate nullable nested objects, collections, maps and arrays only when they are present:

```kotlin
val validator = validator<Order> {
    Order::customer nestIfPresent {
        Customer::name {
            notBlank()
        }
    }
    Order::items forEachIfPresent {
        OrderItem::name {
            notBlank()
        }
    }
}
```

Temporal and enum properties have their own overloads.
For example, `LocalDate`, `LocalTime`, `LocalDateTime`, `ZonedDateTime`, `OffsetDateTime`, `Instant`, `Year` and `YearMonth` properties can use the date and time constraints, and enum properties can use `oneOf`.
See "[Built-in Constraints](built-in-constraints.md)" for the available constraints:

```kotlin
enum class Status { ACTIVE, INACTIVE }

val validator = validator<Account> {
    Account::createdAt {
        notNull()
        past()
    }
    Account::status {
        notNull()
        oneOf(Status.ACTIVE)
    }
}
```

`onCondition` and `onGroup` add conditional and group-specific constraints, and `unwrap` gives access to the underlying `ValidatorBuilder`:

```kotlin
val validator = validator<User> {
    onCondition({ _, context -> "IT".equals(context.attribute("country").value()) }) {
        User::taxId {
            notNull()
        }
    }
    onGroup(ConstraintGroup.of("CREATE")) {
        User::id {
            isNull()
        }
    }
    unwrap().failFast(true)
}
```

### Coroutines support

The extensions in `am.ik.yavi.fn.ValidationExtensions` suspend the validation result handling.
`awaitFold`, `awaitBimap`, `awaitMap`, `awaitMapErrors` and `awaitMapError` correspond to `fold`, `bimap`, `map`, `mapErrors` and `mapError`.
They require `kotlinx-coroutines-core`:

```kotlin
import am.ik.yavi.fn.awaitFold

suspend fun createUser(validated: Validated<User>): HttpStatus = validated.awaitFold(
    errorsMapper = { violations -> HttpStatus.BAD_REQUEST },
    mapper = { user -> HttpStatus.OK },
)
```
