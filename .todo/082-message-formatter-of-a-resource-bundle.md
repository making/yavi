# Ship a MessageFormatter that reads a resource bundle

Difficulty: Low

## Problem

Found by writing the Raoh tutorial with YAVI (section 19 of
`docs/RaohTutorialInYavi.md`). Raoh localizes with
`new ResourceBundleMessageResolver("messages")`. YAVI has `MessageSourceMessageFormatter`
for a `MessageSource` of Spring; without Spring the application writes the lookup:

```java
new MessageSourceMessageFormatter((code, args, defaultMessage, locale) -> {
    var bundle = ResourceBundle.getBundle("messages", locale);
    return bundle.containsKey(code) ? MessageFormat.format(bundle.getString(code), args) : defaultMessage;
});
```

## Scope

* `ResourceBundleMessageFormatter(baseName)` in `am.ik.yavi.message`: the message of the
  bundle for the key if there is one, the default message format otherwise, formatted
  with `MessageFormat` and the locale of the validation. A bundle that is missing for a
  locale falls back as `ResourceBundle` does and never throws.
* A text written in code (`message("...")`, `required("...")`) is not looked up, as
  `MessageFormatter#format(String, Object[], Locale)` states.
* Use it in section 19 of the tutorial and extend an existing test of the formatters.
