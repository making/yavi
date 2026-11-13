# Decide whether a message written in code wins over the formatter

Difficulty: Low

## Problem

A message written for one property or one constraint as a text keeps the message key.
A `MessageFormatter` that knows the key replaces it, so the text in the code is silently
not shown as soon as the application has a message for that key:

```java
MessageFormatter formatter = new MessageSourceMessageFormatter(messageSource::getMessage);
// messages.properties: object.required={0} is required.

ValueValidator<Map<String, ?>, Token> tokenValidator = Yavi.schema()
        ._list("roles", Yavi._string()).required("Select at least one role")
        .fromMap(Token::new)
        .readingParameters()
        .formattingWith(formatter);

tokenValidator.validate(Map.of()); // roles is required.   not: Select at least one role
```

`required(ViolationMessage.of("token.roles", "Select at least one role"))` works, because
the key changes. The same holds for `typeMessage(String)` and `Constraint#message(String)`,
whose Javadoc says that the key is kept. That was harmless while messages came from one
place; with `label`, `required(message)` and `typeMessage` a form has both a formatter
for the common messages and texts for single properties, and the more specific one
loses.

A related surprise: `message(...)` replaces the message of the last predicate only, and
`url(url -> url.protocol("http", "https").build())` adds two. The message is shown for
`ftp://example.com` (`charSequence.url.protocol`) and not for `javascript:alert(1)`
(`charSequence.url`).

## First step: decide

* Whether a text given with `required(String)`, `typeMessage(String)` and
  `message(String)` is final, that is not passed to the formatter for lookup by key (the
  arguments are still substituted). This changes what a `MessageSource` can override;
  list it in `docs/BreakingChanges_0.18.0.md` if `message(String)` is included.
* Or keep the behaviour and say so where the three are documented, pointing to the
  `ViolationMessage` overloads.
* Whether `message(...)` after a constraint that adds several predicates applies to all
  of them.

## If changed

* Extend the tests of `ObjectInputValidator` and of `Constraint#message`; no new test
  class.
