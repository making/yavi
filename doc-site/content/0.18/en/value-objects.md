# Value Objects and Transforms

A validated value can be turned into a domain type before it reaches the object. This page covers `map`, reversible mapping, `preprocess` and `compose`, and the reuse of a named validator.

## Mapping a property to a value object

`map(f)` after a property maps its validated value, for example to a value object. The property is still written in place:

```java
record Email(String value) {}
record Nickname(String value) {}
record Age(int value) {}
record User(Email email, Optional<Nickname> nickname, Age age) {}

ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email()).map(Email::new)
        ._string("nickname", c -> c.notBlank()).map(Nickname::new).optional()
        ._integer("age", c -> c.greaterThanOrEqual(0)).map(Age::new).defaultValue(new Age(20))
        .fromMap(User::new);
```

* The function is given a value that satisfies the constraints. It is never given `null`.
* The property is read, reported and described as before: its key, its type in `object.type`, its label and its JSON Schema are those of the value before `map`.
* `map` is written before the modifiers that decide what `null` produces, and those modifiers apply to the mapped value: `.optional()` produces an `Optional<Nickname>`, and a default value is a mapped value. `.optional().map(f)` and `.defaultValue(v).map(f)` do not compile.
* `label`, `typeMessage` and `withheld()` are written before or after `map`.
* The function may throw. A checked exception is wrapped by the caller; a validation that can fail is written with `flatFromMap`, described in [Cross-Field Rules](cross-field-rules.md).

## Reversible mapping

A schema that writes an object back needs the reverse function. `Yavi.reversibleSchema()` takes `map(f, g)`, where `g` returns the input of `f`:

```java
ReversibleValueValidator<String, Email> emailValidator =
        Yavi._string("email", c -> c.notBlank().email()).map(Email::new, Email::value);
```

The type tells whether a validator knows its reverse. `map(f)` is a `ValueValidator`, and `map(f, g)` is a `ReversibleValueValidator`, which a reversible schema takes and a plain schema also accepts. See [Writing an Object Back](writing-back.md) for `toMap`, `reverse` and views.

## Preprocessing

`preprocess(f)` transforms an input of the same type before the constraints. The constraints and the produced value see the transformed value, and `null` is not transformed:

```java
BigDecimalValidator<BigDecimal> priceValidator = Yavi._bigDecimal("price", c -> c.positive())
        .preprocess(BigDecimal::stripTrailingZeros);
```

A function added later is applied earlier: `validator.preprocess(f).preprocess(g)` applies `g` and then `f`. A `String` is usually transformed where its constraints are written, with `c.trim()`, `c.toLowerCase()` and the other transforms of [Defining a Schema](defining-a-schema.md).

## Composing validators

`compose(f)` adapts a validator to another input type. The function converts the input, and the result is validated as before:

```java
ValueValidator<byte[], String> tokenValidator = Yavi._string("token", c -> c.notBlank())
        .compose(bytes -> Base64.getEncoder().encodeToString(bytes));
```

The input type of a composed validator is unknown, so its description is unknown too. Give it with `untyped(type)` when it is used where the input type is required, such as the root of a document or an alternative of `anyOf`.

## Domain primitives

A value object with its own rule is defined once and used as a property, as an element or on its own:

```java
public final class Email {

    public static final StringValidator<Email> VALIDATOR =
            Yavi._string("email", c -> c.trim().notBlank().email()).map(Email::new);

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String value) {
        return VALIDATOR.validated(value);
    }

    public String value() {
        return value;
    }

}
```

In a schema, `._string(Email.VALIDATOR)` reads and validates the value and produces the value object, so no `map(Email::new)` is needed at the call site. The private constructor stays inside the class, and the rule is not repeated: a change to `Email.VALIDATOR` reaches every schema that uses it.

## Reusing named validators

A validator holds the name it was built with. `named(name)` returns the same validator under another name, so a rule is defined once and used under the key of each property:

```java
ObjectInputValidator<Contact> contactValidator = Yavi.schema()
        ._string(Email.VALIDATOR)                                  // the key and the name are "email"
        ._string(Email.VALIDATOR.named("backupEmail")).nullable()  // the key and the name are "backupEmail"
        ._object("replyTo", Email.VALIDATOR)                       // the same as named("replyTo")
        .fromMap(Contact::new);
```

The name replaces the one of the validator in the violations, in their messages and in `description()`. A value that has no name of its own, such as an object or a list, is located under the name (`home.street`). The combinators carry the name, so it is given before or after `map`, `nullable()` or `untyped()`.
