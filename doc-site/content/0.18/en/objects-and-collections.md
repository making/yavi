# Objects and Collections

A property can be a nested object, a list or a map. This page also covers properties that are read from the same map, unknown keys, schemas whose properties are known at run time, and recursive structures.

## Nested objects

A nested object is added with `_object(name, validator)`. The validator is built with `fromMap` or `from` and reads a nested map or node:

```java
ObjectInputValidator<Address> addressValidator = Yavi.schema()
        ._string("street", c -> c.notBlank())
        ._string("city", c -> c.notBlank())
        .fromMap(Address::new);

ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._object("address", addressValidator)
        .fromMap(User::new);
```

A violation of the nested value is located under the property name, in `name()` (`address.street`) and in `path()`. A nested validator can be reused in several schemas, and a validator defined for a map also serves a JSON tree when the outer validator is read with `readingWith(reader)`.

## Lists

`_list(name, elementValidator)` validates every element with the given validator. The violations of an element are located under its index:

```java
ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._list("roles", Yavi._string(c -> c.notBlank())) // roles[0]
        ._list("tags", Yavi._string(c -> c.notBlank()),
                c -> c.notEmpty().lessThanOrEqual(10).unique()) // tags
        .fromMap(User::new);
```

* The elements are required like any value. Give `elementValidator.nullable()` for a `List<@Nullable String>`.
* The constraints of `_list` are checked on the validated elements, only when every element is valid, and are reported on the property (`roles`).
* `unique(key)` makes the elements unique by a key of them, such as an identifier. The duplicated keys are in the message and in `params`.
* An element validator with an empty name reports a violation on the element itself rather than on a property of it.
* `_list` with a validator that reads an object reports `addresses[0].street`.

## Maps

`_map(name, valueValidator)` validates every value of a map. The keys are data, not property names, so a violation is located under the key (`scores.math`):

```java
ObjectInputValidator<Exam> examValidator = Yavi.schema()
        ._string("student")
        ._map("scores", Yavi._integer(c -> c.greaterThanOrEqual(0).lessThanOrEqual(100)),
                c -> c.notEmpty())
        .fromMap(Exam::new);
```

A key that is not a string is reported with `object.type`. The constraints of `_map` are checked on the validated values and reported on the property.

## Embedded objects

`_embedded(validator)` reads the same map as the properties. It builds an object from the columns of several tables in the row of a JOIN:

```java
ObjectInputValidator<UserName> userNameValidator = Yavi.schema()
        ._string("first_name", c -> c.notBlank())
        ._string("last_name", c -> c.notBlank())
        .fromMap(UserName::new);

ObjectInputValidator<Department> departmentValidator = Yavi.schema()
        ._string("dept_name", c -> c.notBlank())
        ._string("dept_code", c -> c.notBlank())
        .fromMap(Department::new);

ObjectInputValidator<Employee> employeeValidator = Yavi.schema()
        ._integer("id")
        ._embedded(userNameValidator)              // first_name, last_name
        ._embedded(departmentValidator).nullable() // null if dept_name and dept_code have no value
        .fromMap(Employee::new);
```

* The properties of an embedded validator are properties of the embedding one. They are described, are not unknown to `rejectingUnknownProperties()`, and a name defined twice is rejected when the validator is built.
* An embedded validator with `optional()`, `nullable()` or `defaultValue(...)` is given `null` when none of its properties has a value.
* Embedding is also the way to read more than 16 keys.
* A lambda may be embedded too. The properties it reads are unknown, so the validator can then neither reject unknown properties nor read the rest.

## Unknown properties

A key that no property is defined for is ignored. `rejectingUnknownProperties()` reports it with `object.unknownProperty` and writes `additionalProperties: false` in the JSON Schema:

```java
ObjectInputValidator<Signup> signupValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        .fromMap(Signup::new)
        .rejectingUnknownProperties();
```

The setting applies to that map only, not to the nested ones. It catches a typo in an optional property. A form that adds a hidden field, such as a checkbox, needs a property or the rest for that key.

## Rest properties

`_rest(valueValidator)` validates the keys that no property is defined for and gives them to the constructor as a map. `_rest()` takes the values as they are:

```java
record Item(String name, Map<String, String> attrs) {}

ObjectInputValidator<Item> itemValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._rest(Yavi._string(c -> c.notBlank()))
        .fromMap(Item::new);
```

* `_rest` is the last property of a schema, and a schema with `_rest` has no `rejectingUnknownProperties()`, because no key is unknown.
* The rest is an empty map if there is no other key, never `null`, so it takes `recover(...)` only: `_rest().nullable()` does not compile.
* A violation of the rest is located under its key.

## Schemas known at run time

`Yavi.dynamicSchema()` describes properties that come from configuration or a database, such as the questions of a survey or custom fields. Its validator produces a map of the validated values by their names:

```java
record Question(String id, String title, boolean required, int maxLength) {}

DynamicSchema<String> schema = Yavi.dynamicSchema();
for (Question question : survey.questions()) {
    StringValidator<String> answer = Yavi._string(c -> c.notBlank().lessThanOrEqual(question.maxLength()));
    schema = question.required() ? schema.property(question.id(), answer).label(question.title())
            : schema.property(question.id(), answer.defaultValue("")).label(question.title());
}
ObjectInputValidator<Map<String, String>> answersValidator = schema.build();

answersValidator.validate(Map.of("q1", "yavi")); // {q1=yavi}
```

A schema is immutable: `property(name, validator)` returns a new one. The validator is used under the name of the property, and the value is required unless the validator accepts `null`. The modifiers are `label`, `required(message)`, `typeMessage` and `presence()`, with the staged types of `Yavi.schema()`. `rest(valueValidator)` validates the keys that no property is defined for and adds them to the map. The result has `rejectingUnknownProperties()`, `readingParameters()` and `description()`, and it can be a property of another schema.

## Recursive structures

`ValueValidator.recursive(self -> ...)` builds a validator from a reference to itself, for a structure that contains itself:

```java
record Comment(String body, List<Comment> replies) {}

ObjectInputValidator<Comment> commentValidator = ValueValidator.recursive(self -> Yavi.schema()
        ._string("body", c -> c.notBlank())
        ._list("replies", self).defaultValue(List.of())
        .fromMap(Comment::new));
// a violation: replies[0].replies[0].body
```

`ValueValidator.lazy(() -> validator)` refers to a validator that is not built yet, for validators that refer to each other. Give the reference a name, `lazy("comment", ...)` or `recursive("comment", ...)`, when the description is written with `$defs`.

The depth of a recursive structure is the depth of the input. YAVI does not impose a limit, so a hostile document needs a limit of its own, such as a maximum depth in the parser.
