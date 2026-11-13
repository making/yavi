# Messages of a schema with the labels of a form

Difficulty: Medium

## Problem

A form shows a message with the label of the field (`Email address is required.`), not
with the name of the request parameter (`"email" is required`). A validator built with
`Yavi.schema()` has no way to say so:

* `Yavi.schema()` and `Yavi._string(...)` take no `MessageFormatter`. Only
  `ValidatorBuilder#messageFormatter` does.
* `ConstraintViolations#messagesByName()`, `messagesByPointer()`, `details()` and
  `apply(Callback)` format with the formatter of the violation. Only
  `ConstraintViolation#message(MessageFormatter)` and `detail(MessageFormatter)` take
  another one, one violation at a time.
* A `MessageFormatter` is given the arguments but not the violation, so the only way to
  put a label into a message is to replace `args[0]`, and to look the label up by a name
  such as `items[1].name`.

Found while moving the forms of a Spring MVC application from a hand-written validator
to `Yavi.schema()...readingParameters()`. Every form needed the helper below.

```java
record Signup(String email, List<String> interests) {}

ValueValidator<Map<String, List<String>>, Signup> signupValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._list("interests", Yavi._string(c -> c.notBlank()))
        .fromMap(Signup::new)
        .readingParameters();

// today: written by the application
Map<String, String> labels = Map.of("email", "Email address", "interests", "Interests");
Map<String, String> formats = Map.of("charSequence.notBlank", "{0} is required.",
        "object.required", "{0} is required.");
Map<String, List<String>> errors = new LinkedHashMap<>();
for (ConstraintViolation violation : violations) {
    String field = violation.path().get(0) instanceof PathSegment.Property(String name) ? name : violation.name();
    MessageFormatter formatter = (key, defaultFormat, args, locale) -> {
        Object[] labeled = args.clone();
        labeled[0] = labels.getOrDefault(field, field);
        return new MessageFormat(formats.getOrDefault(key, defaultFormat), locale).format(labeled);
    };
    errors.computeIfAbsent(field, k -> new ArrayList<>()).add(violation.message(formatter));
}
// {email=[Email address is required.]}
```

The default messages quote the name (`"{0}" must not be blank`), so a label alone gives
`"Email address" must not be blank`; an application that wants labels replaces the
formats too. That part is the job of a `MessageSource` and is not the gap.

## First step: decide the shape

Candidates, not exclusive:

1. Overloads that take a formatter: `messagesByName(MessageFormatter)`,
   `messagesByPointer(MessageFormatter)`, `details(MessageFormatter)`. Small, and removes
   the loop, but the formatter still cannot see the name or the path.
2. A label of a property, kept out of `name()` and `path()`:
   `._string("email", c -> ...).label("Email address")`, or a map given once
   (`violations.labeled(Map.of("email", "Email address"))`). `{0}` of the message becomes
   the label. Decide what the label of `items[1].name` is.
3. A formatter for the schema: `Yavi.schema().messageFormatter(f)` or
   `readingParameters(coercion).formattingWith(f)`. Check how it reaches the violations
   of nested validators that were built separately.

Check before deciding:

* What `ViolationDetail#name` and `params` hold when a label is set. A client of a JSON
  API needs the name; a label is for people.
* Whether the element of a list (`interests[1]`) is reported with the label of the list.
  A form shows it under the field of the list.
* `docs/NewFeatures_0.18.0.md` (Showing the form again in Spring MVC) passes the message
  key to `BindingResult#rejectValue`; a label must work there too, where `{0}` comes
  from the arguments.

## If accepted

* Extend the tests of `ConstraintViolations` and of `ObjectInputValidator`; no new test
  class.
* Document it in `README.md` and `docs/NewFeatures_0.18.0.md` next to
  `messagesByName()`.
