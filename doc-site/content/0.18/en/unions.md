# Unions

A union accepts one of several shapes. YAVI provides a validator that tries alternatives and a validator that selects one by a discriminator.

## Alternatives

`ValueValidator.anyOf(a, b)` tries the alternatives in order and produces the value of the first one that is valid:

```java
ValueValidator<Object, Object> identifier = ValueValidator.anyOf(
        Yavi._string(c -> c.uuid()).untyped(),
        Yavi._integer(c -> c.positive()).untyped());
```

Alternatives of different input types are made untyped with `untyped()`. The alternatives share the output type; a common supertype such as a sealed interface is declared for the union, and each alternative produces one of its subtypes.

A union can be a property, an element or the root validator:

```java
ObjectInputValidator<Contact> contactValidator = Yavi.schema()
        ._object("contact", ValueValidator.anyOf(emailContact, phoneContact))
        .fromMap(contact -> contact);
```

An alternative does not need its own name. The union reports its violation under the place where it is used (`contact`).

## Discriminated unions

`DiscriminatedUnionValidator` selects the alternative by the value of a key. This is the validator for the subtypes of a sealed interface:

```java
ValueValidator<Map<String, Object>, Shape> shapeValidator = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Yavi.schema()._integer("radius", c -> c.positive()).fromMap(Circle::new))
        .when("square", Yavi.schema()._integer("side", c -> c.positive()).fromMap(Square::new));
```

* Only the selected alternative is validated. The keys of the other alternatives are not required.
* An absent discriminator is reported with `object.required` and an unexpected value with `object.oneOf`.
* The discriminator is read from its own key and is not a property of the alternative.
* A discriminated union is embedded in another schema in the same way as an object validator, and it can be lifted to a list with `liftList()` or `prefixed(name)`.

`DiscriminatedUnionValidator.Reversible` writes the selected object back with its discriminator:

```java
ObjectInputValidator.Reversible<Circle> circleValidator = Yavi.reversibleSchema()
        ._integer("radius", c -> c.positive())
        .fromMapReversible(Circle::new, Circle::radius);
ObjectInputValidator.Reversible<Square> squareValidator = Yavi.reversibleSchema()
        ._integer("side", c -> c.positive())
        .fromMapReversible(Square::new, Square::side);

DiscriminatedUnionValidator.Reversible<Shape> reversible =
        DiscriminatedUnionValidator.<Shape>of("type")
                .when("circle", Circle.class, circleValidator)
                .when("square", Square.class, squareValidator);

reversible.toMap(new Circle(1.5)); // {type=circle, radius=1.5}
```

## Reporting violations

When no alternative of `anyOf` matches, the result is one violation with the key `object.anyOf`. The violation is a `ConstraintViolation.NoAlternativeMatched`, and its `alternatives()` returns one `ConstraintViolations` per alternative, in the order the alternatives were tried:

```java
ConstraintViolations violations = contactValidator
        .validate(Map.of("contact", Map.of("kind", "email", "value", "x")))
        .errors();

violations.messagesByName();
// {contact=["contact" must satisfy at least one of the 2 alternatives]}

if (violations.get(0) instanceof ConstraintViolation.NoAlternativeMatched unmatched) {
    unmatched.alternatives().get(0).messagesByName();
    // {contact.value=["contact.value" must be a valid email address]}
    unmatched.alternatives().get(1).messagesByName();
    // {contact.kind=["contact.kind" must be equal to phone], ...}
}
```

The violations of the alternatives are not mixed into the top-level list, because nothing there would tell which alternative a violation belongs to. A client that shows a specific message can pick the alternative with the fewest violations:

```java
ConstraintViolations closest = unmatched.alternatives().stream()
        .min(Comparator.comparingInt(ConstraintViolations::size))
        .orElseThrow();
```

In JSON, `violations.details()` returns a `ViolationDetail.NoAlternativeMatched`, whose `alternatives` holds the details of each alternative. Any other detail is a `ViolationDetail.Simple`.

A discriminated union reports the violations of the selected alternative directly, under the names of its properties. An absent or unknown discriminator is a single violation on the union, not a list of alternatives.
