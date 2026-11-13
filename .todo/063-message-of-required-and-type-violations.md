# The message of `object.required` and `object.type` of one property

Difficulty: Medium

## Problem

`.message(...)` replaces the message of a constraint. The violations that the reader
reports before any constraint is checked (`object.required`, `object.notNull`,
`object.type`, `object.unknownProperty`) have no such hook, so one property cannot say
what its value should have been:

```java
record Token(Integer ttl, List<String> roles) {}

ValueValidator<Map<String, String>, Token> tokenValidator = Yavi.schema()
        ._integer("ttl", c -> c.positive().message("\"{0}\" must be a positive number of seconds"))
        ._list("roles", Yavi._string())
        .fromMap(Token::new)
        .readingParameters();

tokenValidator.validate(Map.of("ttl", "abc"));
// "ttl" must be of type integer      wanted: "ttl" must be a number of seconds
// "roles" is required                wanted: Select at least one role
```

A group of checkboxes without a checked box sends no key, so "select at least one" is
always `object.required`, never `notEmpty()`. The only way today is a `MessageFormatter`
that looks at the message key and the name in `args[0]`, applied to each violation (see
060).

## First step: decide the shape

1. Modifiers of the property, after the constraints:
   `._integer("ttl", c -> c.positive()).typeMessage(ViolationMessage.of(...))`,
   `._list("roles", ...).requiredMessage("Select at least one role")`. Check that they
   fit the staged types of `Arguments1ValidatorBuilder` (`Keyed`, `NullAccepting`, ...)
   and that `Yavi._integer(...)` outside a schema can carry them.
2. Leave it to the message source, and make that easy: a message key per property
   (`object.type.ttl`) that the formatter tries first. Needs the name in the formatter
   (see 060).

Decide whether the key changes with the message, as `message(ViolationMessage)` does for
a constraint.

## If accepted

* Extend the tests of `ObjectInputValidator`; no new test class.
* Document it in `docs/NewFeatures_0.18.0.md` (the table of inputs and results).
