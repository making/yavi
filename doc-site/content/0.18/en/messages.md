# Messages and Localization

A violation has a message key and a message. The key identifies the rule, and the message is formatted with a `MessageFormatter` for the locale of the validation. A validator is given a formatter with `formattingWith`, and violations can be given another one when they are shown.

## MessageFormatter

```java
public interface MessageFormatter {
    String format(String messageKey, String defaultMessageFormat, Object[] args, Locale locale);

    default String format(String messageFormat, Object[] args, Locale locale) { ... }
}
```

The default implementation of the second method formats a text with `MessageFormat`. Override it to format a message written in code as the formatter formats the other messages. A key that the formatter does not know is formatted with the default message format, so a formatter replaces only the messages it has.

A formatter is called during validation and when a message is formatted. It must be thread-safe if a validator is shared.

## Resource bundles

Without Spring, `ResourceBundleMessageFormatter` looks a message up by its key in a resource bundle on the class path:

```java
// messages_ja.properties: numeric.lessThanOrEqual="{0}" は {1} 以下で入力してください
MessageFormatter formatter = new ResourceBundleMessageFormatter("messages");
validator.formattingWith(formatter).validate(input, Locale.JAPANESE);
```

The bundle is that of the locale of the validation (`messages_ja_JP`, then `messages_ja`), then the base bundle (`messages`). The bundle of the platform default locale is not a fallback, unlike `ResourceBundle.getBundle`: a validation in English on a Japanese server is not answered in Japanese. A key that the bundle does not have is formatted with the default message.

For Spring's `MessageSource`, use `MessageSourceMessageFormatter`; see [Spring](integrations-spring.md#messagesource).

## ViolationMessage and keys

A message of your own is a `ViolationMessage`: a message key, a format and the names of its arguments from `{1}` on:

```java
ViolationMessage outOfStock =
        ViolationMessage.of("order.outOfStock", "\"{0}\" exceeds the stock of {1}", "stock");
```

The message format keeps referring to the arguments by position: `{0}` is the name of the value and `{1}` the first argument. A violation built from it has `messageKey() = order.outOfStock` and `params() = {stock=3}`. A resource bundle can localize the key.

`ViolationMessage#withText(text)` keeps the key and the argument names and replaces the format with a text that no formatter replaces. `localizable()` tells a text from a message to look up.

## Messages written in code

A text given in code is the message of the violation. `MessageFormatter` no longer replaces it with a message it has for the message key; it only substitutes the arguments, and the key is kept.

* `message(String)` of a constraint is the message of the violation.
* `required(String)` and `typeMessage(String)` of a property are the messages of `object.required` and `object.type`.
* `message(...)` right after a constraint made of several predicates, such as `url(url -> ...)` or `password(...)`, replaces the message of each of them.

To localize the text, give it a key of its own with `ViolationMessage`:

```java
c -> c.notBlank().message(ViolationMessage.of("user.name.notBlank", "Enter your name"))
// messages_ja.properties: user.name.notBlank=名前を入力してください
```

`message(ViolationMessage)`, `required(ViolationMessage)` and `typeMessage(ViolationMessage)` are looked up by their own key as before.

## Overriding a message

`violations.apply(bindingResult::rejectValue)` of Spring MVC is not affected by the rule above: Spring looks the message key up again, so a message of the bundle still wins there. `violation.localizable()` tells the two cases apart.

A message is overridden where the rule or the property is defined, or when the violations are shown:

```java
ViolationMessage label = ViolationMessage.of("token.roles", "Select at least one role");
Yavi.schema()._list("roles", Yavi._string()).label("Roles").required(label)
        .fromMap(Token::new)
        .readingParameters()
        .formattingWith(new MessageSourceMessageFormatter(messageSource::getMessage));
```

`formattingWith` applies to every violation of the validator, those of nested validators and of the reader included. `ConstraintViolations#labeled(...).formattedWith(...)` does it when the violations are shown; `labeled` leaves a violation as it is when the function returns `null`.

## Locale

`validate(input, locale)` validates with the given locale, and `localizedWith(locale)` changes the locale of a result, a violation or a list of violations. A function given to `flatFromMap` or `flatApply` is not given the locale; its violations are reported with the locale of the validation.

The locale selects the resource bundle and formats the message with `MessageFormat`. A bundle that does not exist replaces no message, so a single base bundle localizes nothing by itself; give one bundle per locale.
