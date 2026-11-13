# A schema whose properties are known at run time

Difficulty: Medium

## Problem

`Yavi.schema()` fixes the properties at compile time. A form whose fields come from
configuration or a database (the questions of a survey, custom fields, the attributes of
a directory entry) is built with `ObjectInputValidator.of(parts, mapper)`. It works, with
nested names, parameters and `Presence`, but it is not written like the rest of the API:

```java
// name -> validator, from the definition of the survey
Map<String, ValueValidator<String, String>> questions = ...;

List<ObjectInputValidator.Part<?>> parts = new ArrayList<>();
List<String> names = new ArrayList<>();
questions.forEach((name, validator) -> {
    names.add(name);
    parts.add(required(name)
            ? new ObjectInputValidator.Property<>(name, validator.untyped())
            : new ObjectInputValidator.PresenceProperty<>(name, validator.liftPresence().untyped()));
});
ObjectInputValidator<Map<String, String>> answersValidator = ObjectInputValidator.of(parts, values -> {
    Map<String, String> answers = new LinkedHashMap<>();
    for (int i = 0; i < values.size(); i++) {
        switch (values.get(i)) {
            case String value -> answers.put(names.get(i), value);
            case Presence.Present<?>(Object value) -> answers.put(names.get(i), (String) value);
            case null, default -> { }
        }
    }
    return answers;
});
ValueValidator<Map<String, List<String>>, Map<String, String>> surveyValidator = Yavi.schema()
        ._object("answers", answersValidator)   // answers[q1]=...&answers[q2]=...
        .fromMap(answers -> answers)
        .readingParameters();
```

* The values arrive as a `List<@Nullable Object>` by position. The caller keeps the names
  in a second list and casts.
* `Property`, `PresenceProperty`, `untyped()` and `liftPresence().untyped()` are the
  internals of the builders. Which one to use for "leave unchanged if absent" is found
  only in a test (`ObjectInputValidatorTest`).
* `ObjectInputValidator.of` is mentioned in its Javadoc and in one line of `README.md`
  (Presence), not in `docs/NewFeatures_0.18.0.md`.

## First step: decide the shape

A builder that produces a `Map<String, Object>` (or `Map<String, V>` when all validators
produce `V`) keyed by the names, for example:

```java
ObjectInputValidator<Map<String, String>> answersValidator = Yavi.dynamicSchema(String.class)
        .property("q1", Yavi._string("q1", c -> c.notBlank()))
        .property("q2", Yavi._string("q2", c -> c.lessThanOrEqual(200)).defaultValue(""))
        .property("q3", q3Validator).presence()   // not in the map if absent
        .build();
```

Decide:

* The name of the entry point and whether it lives on `Yavi` or on `ObjectInputValidator`.
* Whether the modifiers of a property (`nullable()`, `defaultValue`, `required()`,
  `rejectingNull()`, `presence()`) are offered with the same staged types as in
  `Yavi.schema()`, so that chains without effect do not compile.
* What an absent `presence()` property is in the produced map: no entry.
* `rejectingUnknownProperties()`, `_rest` and `description()` must work as for a static
  schema.

## If accepted

* Extend `ObjectInputValidatorTest`; no new test class.
* Document it in `README.md` and `docs/NewFeatures_0.18.0.md`.
