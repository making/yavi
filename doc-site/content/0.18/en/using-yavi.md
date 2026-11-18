# Object Validation

`am.ik.yavi.builder.ValidatorBuilder` builds a `Validator<T>` that validates an object that already exists. A property of the object is valid when it is `null` unless a constraint such as `notNull()` rejects it; this is the difference from a property of `Yavi.schema()`.

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        .constraint(User::name, "name", c -> c.notNull().lessThanOrEqual(20))
        .constraint(User::email, "email", c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50).email())
        .constraint(User::age, "age", c -> c.greaterThanOrEqual(0).lessThanOrEqual(200))
        .build();

ConstraintViolations violations = validator.validate(user);
```

Use the typed method of a constraint rather than `constraint` when the type of the property would be ambiguous. `_string`, `_integer`, `_bigDecimal` and the other methods take a `Function` that reads the property, so a record accessor, a getter or a lambda works.

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        ._string(User::name, "name", c -> c.notNull().lessThanOrEqual(20))
        ._string(User::email, "email", c -> c.notNull().email())
        ._integer(User::age, "age", c -> c.greaterThanOrEqual(0))
        .build();
```

A validator is thread-safe and may be reused. All violations are accumulated by default; see [Fail-fast mode](#fail-fast-mode) to stop at the first one.

## Nested objects

`nest` applies a validator to a nested object. A nested validator can be passed in, or defined inline.

```java
Validator<Address> addressValidator = ValidatorBuilder.<Address>of()
        .nest(Address::country, "country", countryValidator)
        .nest(Address::city, "city",
                b -> b._string(City::name, "name", c -> c.notBlank().lessThanOrEqual(100)))
        .build();
```

The violations of a nested object are located under its name (`country.name`). `nestIfPresent` validates a nested object only when it is not `null`.

```java
Validator<Order> validator = ValidatorBuilder.<Order>of()
        .nestIfPresent(Order::customer, "customer", customerValidator)
        .build();
```

## Collection, map and array elements

`forEach` applies a validator to every element of a collection, map value or array. The name of an element is its index in the violation name (`histories[0].revision`).

```java
Validator<Histories> validator = ValidatorBuilder.<Histories>of()
        .forEach(Histories::value, "histories", historyValidator)
        .build();
```

An inline validator and `forEachIfPresent` are available as for nested objects.

```java
Validator<Histories> validator = ValidatorBuilder.<Histories>of()
        .forEach(Histories::value, "histories",
                b -> b._integer(History::revision, "revision", c -> c.greaterThanOrEqual(1)))
        .build();
```

Only map values are validated out of the box. To validate map keys, read them as a collection:

```java
Validator<CodeMap> validator = ValidatorBuilder.<CodeMap>of()
        .forEach(codeMap -> codeMap.asMap().keySet(), "codeMap",
                b -> b._string(k -> k, "value", c -> c.notEmpty()))
        .build();
```

A `ValidatorBuilder` with a message key separator other than `.` uses it in the names under `forEach` too:

```java
new ValidatorBuilder<Order>("/")
        .forEach(Order::addresses, "addresses", b -> b._string(Address::street, "street", c -> c.notBlank()))
        .build();
// addresses[0]/street
```

The program below nests a validator and applies an element validator, then prints the violation names.

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

## Conditional constraints

`constraintOnCondition` applies a validator only when a condition holds. A condition receives the target and the `ConstraintContext` of the validation call, so it can read the attributes of the request.

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        .constraintOnCondition((user, context) -> "IT".equals(context.attribute("country").value()),
                b -> b._string(User::taxId, "taxId", c -> c.notBlank()))
        .build();

ConstraintContext context = ConstraintContext.from(Map.of("country", "IT"));
ConstraintViolations violations = validator.validate(user, context);
```

`ConstraintCondition.hasAttribute` and `ConstraintCondition.hasAttributeWithValue` are shortcuts for the common attribute checks.

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        .constraintOnCondition(ConstraintCondition.hasAttributeWithValue("country", "IT"),
                b -> b._string(User::taxId, "taxId", c -> c.notBlank()))
        .build();
```

## Constraint groups

A `ConstraintGroup` is a `ConstraintContext` that carries a name. Constraints added with `constraintOnGroup` are applied only when that group is validated.

```java
enum Group implements ConstraintGroup {

    CREATE, UPDATE

}

Validator<User> validator = ValidatorBuilder.<User>of()
        .constraintOnGroup(Group.CREATE, b -> b._integer(User::id, "id", c -> c.isNull()))
        .constraintOnGroup(Group.UPDATE, b -> b._integer(User::id, "id", c -> c.notNull()))
        .build();

ConstraintViolations violations = validator.validate(user, Group.CREATE);
```

A group can be created without an enum, with `ConstraintGroup.of("CREATE")`. A constraint without a group is applied for every group, and `ConstraintGroup.DEFAULT` is used when `validate` is given no group. Groups match by name.

## Cross-field validation

`constraintOnTarget` adds a rule that spans several properties. The violation is reported under the given name.

```java
Validator<Range> validator = ValidatorBuilder.<Range>of()
        ._integer(Range::from, "from", c -> c.greaterThan(0))
        ._integer(Range::to, "to", c -> c.greaterThan(0))
        .constraintOnTarget(range -> range.to() > range.from(), "to", "to.isGreaterThanFrom",
                "\"to\" must be greater than \"from\"")
        .build();
```

The rule is evaluated after the properties. A `CustomConstraint<T>` can supply the predicate and the message too; see [Custom Constraints](custom-constraints.md).

## Overriding messages

`message` replaces the message of the constraint it follows. `{0}` is the name of the property, the other placeholders are the arguments of the constraint, and the last argument is the value that violated it.

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        ._string(User::name, "name", c -> c.notNull().message("{0} is required!")
                .greaterThanOrEqual(1).message("{0} is too small!")
                .lessThanOrEqual(20).message("{0} is too large!"))
        .build();
```

A text given to `message(String)` is the message. A `MessageFormatter` does not replace it with the message of a resource bundle; it only substitutes the arguments, and the violation keeps its message key. To let a bundle localize a message of your own, give it a key:

```java
c -> c.notBlank().message(ViolationMessage.of("user.name.notBlank", "Enter your name"))
```

Install a `MessageFormatter` on the builder to format the messages of the other constraints.

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        .messageFormatter(new MessageSourceMessageFormatter(messageSource::getMessage))
        ._string(User::name, "name", c -> c.notBlank())
        .build();
```

## Fail-fast mode

`failFast(true)` returns from the validation at the first violation. The mode is passed down to nested, element and conditional validators.

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        ._string(User::name, "name", c -> c.notNull().lessThanOrEqual(20))
        ._string(User::email, "email", c -> c.notNull().email())
        .failFast(true)
        .build();

Validator<User> failFastValidator = validator.failFast(true);
boolean failFast = failFastValidator.isFailFast();
```

## Conflicting constraint names

`conflictStrategy(ConflictStrategy)` decides what happens when the same constraint name is added more than once. The default, `ConflictStrategy.NOOP`, appends the new constraints. `ConflictStrategy.OVERRIDE` removes the existing ones.

```java
Validator<User> validator = ValidatorBuilder.<User>of()
        .conflictStrategy(ConflictStrategy.OVERRIDE)
        ._string(User::name, "name", c -> c.notNull())
        ._string(User::name, "name", c -> c.notBlank()) // replaces the previous "name" constraint
        .build();
```

A custom strategy mutates the list of existing predicates; the new predicate is appended after it returns.

## Handling violations

`validate` returns a `ConstraintViolations`, which is a `List<ConstraintViolation>` and is empty when the object is valid. Iterate it to read each violation.

```java
for (ConstraintViolation violation : violations) {
    String name = violation.name();          // tags[1]
    String key = violation.messageKey();     // charSequence.notBlank
    String message = violation.message();
    Object[] args = violation.args();
    Object value = violation.violatedValue();
    String pointer = violation.pointer();    // /tags/1
}
```

`ConstraintViolation` has no name when the value itself is validated; its message then says "The value".

`messagesByName` and `messagesByPointer` return the messages in the shape a form or an API response needs, and `details` returns records that a JSON library serializes as they are.

```java
Map<String, List<String>> byName = violations.messagesByName();
Map<String, List<String>> byPointer = violations.messagesByPointer();

List<ViolationDetail> details = violations.details();
// [{"name": "items[1].quantity", "pointer": "/items/1/quantity",
//   "messageKey": "numeric.lessThanOrEqual", "message": "...", "params": {"max": 99}}]
```

A `ViolationDetail` is a sealed interface of records. Its accessors are `name()`, `pointer()`, `messageKey()`, `message()` and `params()`; the positional arguments are on `ConstraintViolation#args()`. The value that violated the constraint is not in a detail, which makes a detail safe to return. `details()` returns an unmodifiable list.
