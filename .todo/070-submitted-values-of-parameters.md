# Return the parameters as the schema read them, to show a form again

Difficulty: Medium

## Problem

When a form is not valid it is shown again with the text that was sent. A validator
built with `readingParameters()` returns the violations only, so the application copies
every field from the parameters by hand, repeating the names of the schema:

```java
record Signup(String name, Integer age, List<String> tags, Address address) {}

ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._integer("age")
        ._list("tags", Yavi._string()).defaultValue(List.of())
        ._object("address", addressValidator)
        .fromMap(Signup::new)
        .readingParameters(StringCoercion.strict().trimming());

// today, in the application, for every form
Map<String, Object> form = new LinkedHashMap<>();
form.put("name", first(params, "name"));
form.put("age", first(params, "age"));                      // "abc", not an Integer
form.put("tags", params.getOrDefault("tags", List.of()));   // and "tags[]", "tags[0]"?
form.put("address", Map.of("city", first(params, "address.city"))); // and "address[city]"?
```

The copy is also less than what the validator read:

* `address[city]`, `tags[]` and `items[0].name` are keys that `readingParameters()`
  understands. The hand-written copy, or the map of parameters given to a template as it
  is, does not. `docs/NewFeatures_0.18.0.md` (Showing the form again in Spring MVC) has
  two notes that exist only because of this: a value sent as `address[city]` is not shown
  again, and a key with several values needs a map built by hand.
* Whether a key is a list or a single value is known to the schema, not to the map: one
  checked checkbox is a `String` in a `Map<String, String>` and a list of one in a
  `Map<String, List<String>>`.
* A map of parameters given to a template shows keys that no property is defined for.

`ParameterTree` already resolves all of this for validation. It is not offered as a
result.

Found while moving the forms of a Spring MVC application with Mustache templates to
`Yavi.schema()`: the only hand-written code left per form was the copy above (15 fields
in the largest form).

## First step: decide the shape

The parameters as the schema read them: the text that was sent, before trimming and
coercion, for the properties that the schema defines, under the names of the violations.

```java
// a possible shape
SubmittedValues submitted = signupValidator.submitted(params);
submitted.asMap();     // {name=" Alice ", age="abc", tags=[a], address={city="Tokyo"}}
submitted.asFlatMap(); // {name=" Alice ", age="abc", tags=[a], address.city="Tokyo"}
```

Decide:

* Where it is asked for. A method of the validator that `readingParameters()` returns
  needs a type of its own, which must keep the combinators of `ValueValidator`
  (`formattingWith`, `constraintOnTarget`; see the type that `readingParameters()`
  returns today). The alternative is the result: `Validated.Invalid` holding the input
  changes a record that is public since 0.18.0.
* Only maps of JDK types are returned (`Map`, `List`, `String`), so that a template
  engine or a `MapBindingResult` of Spring takes them as they are. No type of a web
  framework is used.
* A list property is a `List` whatever was sent, also for one value and for `splitBy`
  (decide: the text as sent, or its elements).
* `_embedded` (the properties are those of the same map), `_rest`, `presence()` (absent:
  no entry), `Yavi.dynamicSchema()` and a `DiscriminatedUnionValidator`.
* A key that no property is defined for is left out, so that a page does not show a
  parameter that the application did not ask for.
* A property that must not be shown again, such as a password: leave it to the
  template, or a modifier of the property.
* Whether the same is offered for `readingWith(reader)` of a JSON tree. Probably not: an
  API response does not show the input again.

Not in scope: the map of an object that is already stored, for the first display of an
edit form. That is an encoder (041).

## If accepted

* Extend the tests of `readingParameters()`; no new test class.
* Use it in `docs/NewFeatures_0.18.0.md` (Showing the form again in Spring MVC) and drop
  the two notes there.
