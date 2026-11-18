# Custom Constraints

A constraint that the built-in constraints do not cover is written as a predicate. The predicate is applied with `predicate` on the constraint of the value, and the violation is reported like that of a built-in constraint.

## A predicate constraint

`predicate` takes the predicate, a message key and a message format.

```java
Validator<Book> validator = ValidatorBuilder.<Book>of()
        ._string(Book::title, "title", c -> c.notBlank().lessThanOrEqual(64))
        ._string(Book::isbn, "isbn", c -> c.notBlank()
                .predicate(ISBNValidator::isISBN13, "string.isbn13", "\"{0}\" must be ISBN13 format"))
        .build();
```

`{0}` is the name of the property and the last placeholder is the value that violated the constraint. Override `arguments` of a custom constraint, described below, to add arguments between them.

`predicateNullable` is the same for a predicate that accepts `null`; it does not reject a `null` value before the predicate runs.

## A custom constraint class

`am.ik.yavi.core.CustomConstraint<V>` combines a predicate with its message key, its default message format and its arguments, so that one constraint is defined once and applied in several places.

```java
public final class Isbn13Constraint implements CustomConstraint<String> {

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
        return (10 - sum % 10) % 10 == isbn.charAt(12) - '0';
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

Apply it with `predicate`. The predicate is also usable in a rule on the target object, with the message of that rule:

```java
Validator<Book> validator = ValidatorBuilder.<Book>of()
        ._string(Book::isbn, "isbn", c -> c.notBlank().predicate(new Isbn13Constraint()))
        .build();

Validator<Book> checked = ValidatorBuilder.<Book>of()
        ._string(Book::isbn, "isbn", c -> c.notBlank())
        .constraintOnTarget(book -> new Isbn13Constraint().test(book.isbn()), "isbn",
                "string.isbn13", "\"{0}\" must be ISBN13 format")
        .build();
```

## Message keys and arguments

The message key identifies the violation. The default message format is used when no `MessageFormatter` has a message for that key, and `{0}` is the name of the property. A constraint that needs more arguments overrides `arguments`, which are inserted between the name and the violated value. The violated value is appended as the last argument.

```java
public final class InstantRangeConstraint implements CustomConstraint<Instant> {

    private final Instant start;

    private final Instant end;

    public InstantRangeConstraint(Instant start, Instant end) {
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
    }

    @Override
    public Object[] arguments(Instant violatedValue) {
        return new Object[] { this.start /* {1} */, this.end /* {2} */ };
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

A message of your own that a client reads from `ViolationDetail` names its arguments, so that a client can render the message from `messageKey` without knowing the positions. Override `paramNames` with the names of the arguments from `{1}` on; the violated value gets no name unless it is meant to be returned.

```java
@Override
public List<String> paramNames() {
    return List.of("start", "end");
}
```

A `ViolationMessage` created with `ViolationMessage.of(key, format, "start", "end")` names the arguments in the same way.

## Reusing a constraint across types

A `CustomConstraint` is typed by the value it tests. Write the class once and pass an instance wherever a value of that type is constrained: with `_string` in a validator of an object, with `Yavi._string` in a validator of a single value, and with `constraintOnTarget` when the value is the target. The message key and the arguments travel with the instance.

```java
private static final CustomConstraint<String> ISBN = new Isbn13Constraint();

Validator<Book> bookValidator = ValidatorBuilder.<Book>of()
        ._string(Book::isbn, "isbn", c -> c.notBlank().predicate(ISBN))
        .build();

StringValidator<String> isbnValidator = Yavi._string("isbn", c -> c.notBlank().predicate(ISBN));
```

When only the rule is shared, `predicate(Predicate, ViolationMessage)` applies the same predicate with the message of the property that uses it.
