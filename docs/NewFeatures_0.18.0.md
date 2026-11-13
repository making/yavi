# New features in 0.18.0

[日本語](NewFeatures_0.18.0.ja.md)

YAVI 0.18.0 adds `Yavi.schema()`, which describes an input once and reads it from typed
arguments, a map, a JSON tree or request parameters. It replaces `Yavi.arguments()`,
which is deprecated.

* [Validators of a single value](#validators-of-a-single-value)
* [Optional properties, nested objects, lists and maps](#optional-properties-nested-objects-lists-and-maps)
* [Validating a map such as a parsed JSON object](#validating-a-map-such-as-a-parsed-json-object)
* [Validating the tree model of a JSON library](#validating-the-tree-model-of-a-json-library)
* [Validating request parameters](#validating-request-parameters)
* [Handling the result](#handling-the-result)
* [Describing a validator and generating a JSON Schema](#describing-a-validator-and-generating-a-json-schema)
* [Nullness with JSpecify](#nullness-with-jspecify)

See [Breaking changes in 0.18.0](BreakingChanges_0.18.0.md) to migrate from 0.17.

The examples use these types:

```java
enum Role { ADMIN, MEMBER, GUEST }
record Address(String street, String city) {}
record User(String email, Optional<String> nickname, Integer age, Role role, Address address, List<String> tags) {}
```

## Validators of a single value

`Yavi._string`, `Yavi._integer`, `Yavi._localDate` and so on build the validator of one
value without a builder class and `build()`. They have the names of the properties of
`Yavi.schema()`.

```java
// before
StringValidator<String> emailValidator = StringValidatorBuilder.of("email", c -> c.notBlank().email()).build();

// after
StringValidator<String> emailValidator = Yavi._string("email", c -> c.notBlank().email());
```

* The name can be omitted for a value that is not a property, such as the element of a
  list: `Yavi._string(c -> c.notBlank())`. Its message says "The value" in place of the
  name (`The value must not be blank`).
* `Yavi._list(elementValidator, c -> c.notEmpty().unique())` and
  `Yavi._map(valueValidator, c -> c.notEmpty())` validate a list or map and constrain it.

### Transforming a value before its constraints

The transforms of a `String` are written before its constraints. Any other value is
transformed with `preprocess`. `null` is not transformed.

```java
StringValidator<String> emailValidator = Yavi._string("email", c -> c.trim().toLowerCase().notBlank().email());
emailValidator.validate("  USER@Example.COM "); // Valid[value=user@example.com]

BigDecimalValidator<BigDecimal> priceValidator = Yavi._bigDecimal("price", c -> c.positive())
        .preprocess(BigDecimal::stripTrailingZeros);
```

`c.trim()`, `c.toLowerCase()`, `c.toUpperCase()`, `c.toNormalForm(form)` and
`c.transform(f)` are available wherever the constraints of a `String` value are written,
such as `Yavi.schema()._string("email", c -> ...)`.

### Combinators

Every `ValueValidator` has:

| Method | Result |
| --- | --- |
| `nullable()`, `optional()`, `defaultValue(value)` | accepts `null` and produces `null`, `Optional.empty()` or the default value |
| `recover(fallback)`, `recoverWith(violations -> ...)` | never fails: produces the fallback for `null` and for an invalid value |
| `liftList()`, `liftSet()`, `liftMap()` | validates every element or value, located under its index or key |
| `prefixed(name)` | puts a name before the names of the violations (`orders[1].id`) |
| `ValueValidator.anyOf(a, b)` | the first alternative that is valid; otherwise `object.anyOf` followed by the violations of every alternative |
| `ValueValidator.lazy(supplier)`, `recursive(self -> ...)` | a reference to a validator that is not built yet |

### New constraints

`BigDecimalConstraint` has `fractionDigits(max)` and `integerDigits(max)`, the
counterpart of `@Digits` of Bean Validation. The digits of the value are counted, so
`1.500` has one fraction digit.

```java
Yavi._bigDecimal("price", c -> c.fractionDigits(2).integerDigits(6));
// 1.505: "price" must have at most 2 fraction digits
```

## Optional properties, nested objects, lists and maps

A property of `Yavi.schema()` is required unless a modifier after it says otherwise,
and can be an object, a list or a map.

```java
ObjectInputValidator<Address> addressValidator = Yavi.schema()
        ._string("street", c -> c.notBlank())
        ._string("city", c -> c.notBlank())
        .fromMap(Address::new); // see the next section

Arguments6Validator<String, String, Integer, Role, Map<String, Object>, List<String>, User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._string("nickname", c -> c.notBlank()).optional()       // Optional<String>
        ._integer("age", c -> c.greaterThanOrEqual(0)).defaultValue(20)
        ._enum("role", Role.class).recover(Role.GUEST)
        ._object("address", addressValidator)                    // address.street
        ._list("tags", Yavi._string(c -> c.notBlank()),          // tags[0]
                c -> c.notEmpty().lessThanOrEqual(10).unique())  // tags
        .apply(User::new)
        .constraintOnTarget(user -> user.age() >= 18 || user.role() != Role.ADMIN, "role",
                ViolationMessage.of("user.adultAdmin", "\"{0}\" must not be ADMIN for a minor"));
```

* `nullable()`, `optional()`, `defaultValue(...)`, `recover(...)` and `recoverWith(...)`
  apply to the last added property. `nullable()` changes the type of the value to
  `@Nullable`.
* `_object(name, validator)`, `_list(name, elementValidator)` and
  `_map(name, valueValidator)` locate their violations under the name, in `name()`
  (`address.street`, `tags[1]`, `prices.pen`) and in `path()`.
* A validator that accepts `null` keeps doing so under a name:
  `_object("address", addressValidator.nullable())` produces `null` for `null`, like
  `_object("address", addressValidator).nullable()`.
* The constraints of `_list` and `_map` are checked when every element is valid and are
  reported on the property: `"tags" must be unique. [a] is/are duplicated.`
* `constraintOnTarget` is a rule that spans several properties. It is checked when all
  properties are valid. After `fromMap` it returns an `ObjectInputValidator`, so
  `rejectingUnknownProperties()` and `readingParameters()` can follow it.
* `flatApply(f)` takes a function that returns a `Validated`, such as a factory method
  that validates.

## Validating a map such as a parsed JSON object

`fromMap` ends the same properties with a validator that reads them from a
`Map<String, Object>` by their names, checks their types and creates the object, without
a cast and without reflection.

```java
ObjectInputValidator<User> userValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._string("nickname", c -> c.notBlank()).optional()
        ._integer("age", c -> c.greaterThanOrEqual(0)).defaultValue(20)
        ._enum("role", Role.class)
        ._object("address", addressValidator)
        ._list("tags", Yavi._string(c -> c.notBlank())).defaultValue(List.of())
        .fromMap(User::new);

Validated<User> validated = userValidator.validate(Map.of(
        "email", "jd@example.com",
        "age", 30,
        "role", "ADMIN",
        "address", Map.of("street", "1-2-3", "city", "Tokyo"),
        "tags", List.of("java", "yavi")));
```

| Input | Result |
| --- | --- |
| A value that does not represent the type of its property (`"age": "30"`) | `object.type`: `"age" must be of type integer` |
| A number that keeps its value (`30L`, `30.0` for an `Integer`) | accepted; `30.5` is `object.type` |
| An enum constant, a temporal value | read from the name of the constant, from the ISO-8601 string |
| An absent key | validated like `null`: accepted by `optional()`, `nullable()`, `defaultValue(...)`, `recover(...)`, otherwise `object.required`; always `object.required` after `required()` |
| An explicit `null` | `object.notNull`, or the violation of a constraint that rejects `null`; always `object.notNull` after `rejectingNull()` |
| A key that no property is defined for | ignored; `rejectingUnknownProperties()` reports `object.unknownProperty` |

* An `_enum` or `_object` property needs its type to be read from a map:
  `_enum(name, type)`, `_object(name, type)`.
* `ignoringCase()` reads the name of an enum constant in any case:
  `._enum(Yavi._enum("role", Role.class, c -> c).ignoringCase())` takes `"admin"`.
* `flatFromMap(f)` takes a function that returns a `Validated`:
  `.flatFromMap(DateRange::parse)`.
* `untyped()` turns a validator into a `ValueValidator<Object, X>`, for example to
  combine alternatives of different types:
  `ValueValidator.anyOf(Yavi._integer(c -> c.positive()).untyped(), Yavi._string(c -> c.uuid()).untyped())`.

### A discriminated union

`DiscriminatedUnionValidator` selects the validator of a map by the value of a key, which
reads the subtypes of a sealed interface. Only the selected alternative is validated.

```java
DiscriminatedUnionValidator<Shape> shapeValidator = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Yavi.schema()._integer("radius", c -> c.positive()).fromMap(Circle::new))
        .when("square", Yavi.schema()._integer("side", c -> c.positive()).fromMap(Square::new));

shapeValidator.validate(Map.of("type", "circle", "radius", 2)); // Circle[radius=2]
shapeValidator.validate(Map.of("type", "x")); // "type" must be one of the following values: [circle, square]
```

`of("type")` alone is not a validator, because a union without alternatives would reject
every input: the first `when` returns the validator.

### Several objects from one map, and the remaining keys

`_embedded` reads the same map as the properties, which builds an object from the
columns of several tables in the row of a JOIN. `_rest` collects the keys that no
property is defined for.

```java
record UserName(String first, String last) {}
record Department(String name, String code) {}
record Employee(Integer id, UserName name, @Nullable Department dept, Map<String, String> attrs) {}

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
        ._rest(Yavi._string(c -> c.notBlank()))    // a Map<String, String> of the other keys
        .fromMap(Employee::new);

employeeValidator.validate(Map.of("id", 1, "first_name", "Taro", "last_name", "Yamada", "color", "red"));
// Employee[id=1, name=UserName[first=Taro, last=Yamada], dept=null, attrs={color=red}]
```

Embedding is also the way to read more than 16 keys. `_rest()` without a validator takes
the values as they are. The rest is an empty map if there is no other key, never `null`,
so it takes `recover(...)` only: `_rest().nullable()` and `_rest().defaultValue(...)` do
not compile. `_rest` is the last property of a schema, so it is written once, and the
validator of such a schema has no `rejectingUnknownProperties()`: no key is unknown.

### Telling an absent key from null

In a partial update, such as a PATCH request (JSON Merge Patch), an absent key leaves the
property unchanged and `null` clears it. `presence()` reads a property as a `Presence`,
which is `Present(value)`, `PresentNull` or `Absent`.

```java
record ProfilePatch(Presence<String> nickname, Presence<@Nullable String> bio) {}

ObjectInputValidator<ProfilePatch> patchValidator = Yavi.schema()
        ._string("nickname", c -> c.lessThanOrEqual(50)).presence()        // may be omitted, cannot be cleared
        ._string("bio", c -> c.lessThanOrEqual(500)).nullable().presence() // may be omitted or cleared
        .fromMap(ProfilePatch::new);

ProfilePatch patch = patchValidator.validated(Map.of("bio", "Hello")); // nickname=Absent, bio=Present[Hello]
patch.nickname().ifPresent(profile::setNickname);                      // not called
profile.setBio(patch.bio().orElse(profile.getBio()));                  // null if "bio" is null

switch (patch.bio()) {
    case Presence.Present<String>(String bio) -> profile.setBio(bio);
    case Presence.PresentNull<String> cleared -> profile.setBio(null);
    case Presence.Absent<String> unchanged -> { }
}
```

An absent key is not validated. A key that is present is validated as without
`presence()`: `{"nickname": null}` is `"nickname" must not be null`. `presence()` is the
last modifier of a property.

A contract that is not a partial update often accepts only one of the two. `required()`
and `rejectingNull()` say which, without changing the type of the value:

```java
record Account(@Nullable Instant deletedAt, Optional<String> bio, int page) {}

ObjectInputValidator<Account> accountValidator = Yavi.schema()
        ._instant("deletedAt").nullable().required()      // must be sent, as null if there is none
        ._string("bio").optional().rejectingNull()        // may be omitted, a text if it is sent
        ._integer("page").defaultValue(1).rejectingNull() // 1 only if omitted
        .fromMap(Account::new);

accountValidator.validate(Map.of());                                  // "deletedAt" is required
accountValidator.validate(Collections.singletonMap("deletedAt", null)); // Account[deletedAt=null, bio=Optional.empty, page=1]
```

| Property | `fromMap`: an absent key | `fromMap`: an explicit `null` | `apply`: a `null` argument | JSON Schema |
| --- | --- | --- | --- | --- |
| `_string("x")` | `object.required` | `object.notNull` | `object.notNull` | `required`, `"type": "string"` |
| `.nullable()`, `.optional()`, `.defaultValue(...)`, `.recover(...)` | accepted | accepted | accepted | `"type": ["string", "null"]` |
| followed by `.required()` | `object.required` | accepted | accepted | `required`, `"type": ["string", "null"]` |
| followed by `.rejectingNull()` | accepted | `object.notNull` | accepted | `"type": "string"` |
| `.presence()` | `Absent` | validated by the property | `Absent` (the argument is a `Presence`) | `"type": "string"` |

Both are the last modifier of a property, like `presence()`, and are written after
`optional()`, `nullable()`, `defaultValue(...)` or `recover(...)`: a property that does
not accept `null` rejects both anyway, so `_string("x").required()` does not compile. They tell an absent key
from `null`, so they apply to an input that has keys: a map, a tree read with
`readingWith(reader)` and parameters, where a parameter with an empty value is read as
`null` and is therefore present. Typed arguments (`apply`) have no absent state: `null`
stands for an absent value and is validated as without the modifier, also after
`rejectingNull()`. The JSON Schema is the same for `fromMap` and `apply`.

Each of `optional()`, `nullable()`, `defaultValue(...)` and `recover(...)` decides what
`null` produces before the property is validated, so of two of them only the last one
would. A chain with a part that has no effect does not compile:

| Chain | |
| --- | --- |
| `.optional().recover(Optional.empty())`, `.nullable().recover(null)` | compiles: an invalid value is recovered as an absent one |
| `.recover(0).nullable()`, `.recover(0).optional()`, `.recover(0).defaultValue(1)` | compiles: `null` produces something else than the fallback of an invalid value |
| `.nullable().optional()`, `.defaultValue(1).nullable()`, `.optional().defaultValue(...)` | does not compile: what the first one produces for `null` is never produced |
| `.defaultValue(1).recover(2)` | does not compile: `recover` replaces `null` too; write `.recover(2).defaultValue(1)` |
| `.recover(1).recover(2)` | does not compile: the first one never fails |

The same holds for an `_embedded` property. The combinators of a
`ValueValidator` are not restricted, and return a `NullAcceptingValueValidator`: a
validator that is known by its type to accept `null`. A property added with it takes
`required()`, `rejectingNull()` and `presence()`, and no modifier that accepts `null`
again:

```java
NullAcceptingValueValidator<Integer, Integer> page = Yavi._integer("page", c -> c.positive()).defaultValue(1);

Yavi.schema()._integer(page).rejectingNull() // 1 only if omitted
```

The other parts of a schema are staged in the same way, so that what would have no
effect, or would fail when the validator is built, does not compile:

| Chain | |
| --- | --- |
| `.optional().presence()` | does not compile: the `Optional` would be wrapped in a `Presence`; write `.nullable().presence()` |
| `._rest()._rest()`, `._rest()._string("x")` | does not compile: the rest is the last property |
| `._rest().fromMap(f).rejectingUnknownProperties()` | does not compile: no key is unknown |
| `.fromMap(f).rejectingUnknownProperties().rejectingUnknownProperties()` | does not compile: `fromMap` returns an `ObjectInputValidator.IgnoringUnknown`, which is told once |
| `Yavi._enum(Role.class).ignoringCase().ignoringCase()` | does not compile: `ignoringCase()` returns an `Arguments1Validator`, so it is written once |
| `._enum("role")`, `._object("address")` | do not exist: an enum and an object are added with their type, which `fromMap` needs |

`nullable()` is the only modifier that produces `null`. A `null` default value or
fallback of a value that is never `null` (the value of constraints, a list, a map, an
`Optional`) is what a nullness checker reports, and without one it is rejected with a
`NullPointerException` when the schema is built, instead of giving the function a `null`
that the type of its parameter excludes:

| Chain | |
| --- | --- |
| `._string("x").defaultValue(null)`, `._string("x").recover(null)` | `NullPointerException`: write `.nullable()`, or `.nullable().recover(null)` |
| `._string("x").optional().recover(null)` | `NullPointerException`: write `.optional().recover(Optional.empty())` |
| `._string("x").recoverWith(violations -> null)` | `NullPointerException` when the function returns `null` |
| `._string("x").nullable().recover(null)` | accepted |
| `._string(ValueValidator.passThrough()).defaultValue(null)` | accepted: the validator produces a `@Nullable String` itself |

### A structure that contains itself

`ValueValidator.recursive(self -> ...)` builds a validator from a reference to itself,
and `ValueValidator.lazy(() -> validator)` refers to a validator that is built later, for
validators that refer to each other.

```java
record Comment(String body, List<Comment> replies) {}

ObjectInputValidator<Comment> commentValidator = ValueValidator.recursive(self -> Yavi.schema()
        ._string("body", c -> c.notBlank())
        ._list("replies", self).defaultValue(List.of())
        .fromMap(Comment::new));
// a violation: replies[0].replies[0].body
```

## Validating the tree model of a JSON library

`readingWith(reader)` reads the tree model of a JSON library, such as Jackson's
`JsonNode`, as it is, with the same rules and violations as a map. The tree is not copied
to a map. `from(reader, f)` on the schema is `fromMap(f).readingWith(reader)`.

```java
ValueValidator<JsonNode, User> nodeValidator = userValidator.readingWith(JsonNodeReader.INSTANCE);

Validated<User> validated = nodeValidator.validate(jsonMapper.readTree(json));
```

The reader applies to the nested validators too, so `addressValidator` is built with
`fromMap` whatever input is read in the end. YAVI has no dependency on a JSON library: an
`InputReader` is a small stateless class written once per tree model. This is the one for
Jackson 3:

```java
public final class JsonNodeReader implements CoercibleInputReader<JsonNode> {

    public static final JsonNodeReader INSTANCE = new JsonNodeReader();

    @Override
    public boolean isNull(JsonNode node) {
        return node.isNull() || node.isMissingNode();
    }

    @Override
    public Object value(JsonNode node) { // a scalar as a JDK type
        if (node.isString()) {
            return node.stringValue();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return node;
    }

    @Override
    public boolean isObject(JsonNode node) {
        return node.isObject();
    }

    @Override
    public @Nullable JsonNode property(JsonNode object, String name) {
        return object.get(name); // null if absent
    }

    @Override
    public Iterable<Map.Entry<String, JsonNode>> properties(JsonNode object) {
        return object.properties();
    }

    @Override
    public boolean isArray(JsonNode node) {
        return node.isArray();
    }

    @Override
    public Iterable<JsonNode> elements(JsonNode array) {
        return array;
    }

}
```

The tree model of the JDK ([JEP 540](https://openjdk.org/jeps/540), an incubator module of
JDK 28) is read in the same way; the README shows its reader.

A tree whose scalars are all strings is read with `coercingStrings()`, a decorator of any
`CoercibleInputReader`, the interface a reader of a tree model implements: `JsonNodeReader.INSTANCE.coercingStrings()`, or
`InputReader.ofJdkTypes().coercingStrings()` for a tree of JDK types. The strings are read
by the rules of the next section. The reader that coerces strings is an `InputReader` and cannot be coerced again,
so `reader.coercingStrings(rules).coercingStrings()` does not compile: the rules are
given once, and so is each rule of a `StringCoercion`: `booleanWords(...).booleanWords()` does
not compile. A validator
reads with the last reader it is given: `readingWith(reader)` after
`readingWith(reader.coercingStrings())` or `readingParameters()` coerces no string.

## Validating request parameters

Request parameters and a submitted form are a flat map of strings, possibly several for
one key. `readingParameters()` of a validator built with `fromMap` reads such a map: a
string is text that stands for a value of the type of its property, and a key is the path
of a nested value.

```java
record SearchForm(String q, Integer page, Boolean includeArchived, @Nullable LocalDate since, List<String> tags,
        @Nullable Address address) {}

ValueValidator<Map<String, List<String>>, SearchForm> searchFormValidator = Yavi.schema()
        ._string("q", c -> c.notBlank())
        ._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
        ._boolean("includeArchived").defaultValue(false)
        ._localDate("since").nullable()
        ._list("tags", Yavi._string(c -> c.notBlank())).defaultValue(List.of())
        ._object("address", addressValidator).nullable()
        .fromMap(SearchForm::new)
        .readingParameters();

@GetMapping("/search")
Object search(@RequestParam MultiValueMap<String, String> params) {
    // ?q=yavi&page=2&since=&tags=a&tags=b&address.street=1-2-3&address.city=Tokyo
    Validated<SearchForm> validated = searchFormValidator.validate(params);
    // SearchForm[q=yavi, page=2, includeArchived=false, since=null, tags=[a, b], address=Address[street=1-2-3, city=Tokyo]]
}
```

The map is a `Map<String, String>`, a `Map<String, List<String>>` such as Spring's
`MultiValueMap`, or the `Map<String, String[]>` of the Servlet API, whichever the
validator is assigned to.

The values:

* An empty string is validated like `null`, whatever the type of its property is. Give
  `defaultValue("")` to a string property that must receive `""`.
* A number is a decimal number without surrounding white space or grouping separators:
  `"30"` and `"30.0"` are an `Integer`; `"30.5"`, `" 30"` and `"1,000"` are not. A
  boolean is `"true"` or `"false"`.
* A string that does not stand for the type is reported with `object.type`
  (`"page" must be of type integer`), never with an exception.

The keys:

* `address.city` and `address[city]` are the property `city` of `address`, and `tags[]`
  is `tags`.
* A key with several values is a list (`tags=a&tags=b`). Several values for a scalar
  property are reported with `object.type`.
* Keys with indexes are a list in the order of the indexes:
  `items[0].name=pen&items[1].name=ink`. A skipped index is a `null` element, so a
  violation keeps the index it was sent with. An index is at most 255
  (`ParameterTree.DEFAULT_MAX_INDEX`).
* An empty list cannot be sent: its key is absent. Give `defaultValue(List.of())` to a
  list that may be empty, such as checkboxes.

The rules are strict on purpose. `StringCoercion` relaxes them for text written by a
person, such as the fields of a form or the cells of a CSV file:

```java
record Signup(Integer age, Boolean subscribed) {}

StringCoercion form = StringCoercion.strict().trimming().booleanWords();

ValueValidator<Map<String, String>, Signup> signupValidator = Yavi.schema()
        ._integer("age")
        ._boolean("subscribed").defaultValue(false) // an unchecked checkbox sends nothing
        .fromMap(Signup::new)
        .readingParameters(form);

signupValidator.validate(Map.of("age", " 30 ", "subscribed", "on")); // Signup[age=30, subscribed=true]
```

* `trimming()`: a blank string is validated like `null`, and a string read as a number, a
  boolean, an enum constant or a temporal value is read without its surrounding white
  space. A `String` value is left as it is; `c.trim()` trims it.
* `booleanWords()`: `true`, `1`, `yes`, `on` and `false`, `0`, `no`, `off` in any case.

`recover` suits a parameter that must not fail a request:

```java
record PageRequest(Integer page, Integer size) {}

ValueValidator<Map<String, String>, PageRequest> pageValidator = Yavi.schema()
        ._integer("page", c -> c.greaterThanOrEqual(0)).recover(0)
        ._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
        .fromMap(PageRequest::new)
        .readingParameters();

pageValidator.validate(Map.of("page", "-1", "size", "9999")); // PageRequest[page=0, size=20]
```

## Handling the result

`Validated` is a sealed interface of the records `Valid` and `Invalid`, so a result is
handled with an exhaustive `switch`:

```java
// before
Validated<User> validated = userValidator.validate(map);
if (validated.isValid()) {
    save(validated.value());
}
else {
    validated.errors().forEach(violation -> report(violation.name(), violation.message()));
}

// after
switch (userValidator.validate(map)) {
    case Validated.Valid<User>(User user) -> save(user);
    case Validated.Invalid<User>(ConstraintViolations violations) ->
        violations.forEach(violation -> report(violation.name(), violation.message()));
}
```

`Arguments1` to `Arguments16` are records too:
`case Validated.Valid(Arguments2(var name, var age))`.

A violation knows where it is without parsing its name:

```java
violation.name();    // tags[1]
violation.path();    // [Property[name=tags], Index[index=1]]
violation.pointer(); // /tags/1, a JSON Pointer (RFC 6901)
```

`ConstraintViolations` has the forms that a form and an API response need:

```java
// for a form: the messages of each field
Map<String, List<String>> byName = violations.messagesByName();
// {email=["email" must not be blank], address.street=["address.street" must not be blank]}

// the same with JSON Pointers as the keys
Map<String, List<String>> byPointer = violations.messagesByPointer();
// {/email=[...], /address/street=[...]}

// for an API response: records that a JSON library serializes as they are
List<ViolationDetail> details = violations.details();
// [{"name": "address.street", "pointer": "/address/street", "messageKey": "charSequence.notBlank",
//   "message": "\"address.street\" must not be blank", "args": ["address.street", ""]}, ...]
```

`args` ends with the violated value, so map the details to a type of your own if that
value must not be sent back.

A hand-written step fails with one call:

```java
// before
return Validated.failureWith(ConstraintViolation.builder()
        .name("")
        .messageKey("json.invalid")
        .defaultMessageFormat("\"{0}\" cannot be parsed as JSON")
        .argsWithPrependedName()
        .locale(locale)
        .build());

// after
return Validated.failureWith(ViolationMessage.of("json.invalid", "\"{0}\" cannot be parsed as JSON"), value, locale);
```

## Describing a validator and generating a JSON Schema

A validator describes the input it expects. `description()` returns a read-only tree
(`ValueDescription`) of objects, lists, maps, alternatives and values with their
constraints, which tools can walk. `JsonSchema` is one of them and needs no JSON library.

```java
ValueDescription description = userValidator.description();
Map<String, Object> schema = JsonSchema.of(description);
String json = JsonSchema.toJson(description);
```

```json
{"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{
  "email":{"type":"string","minLength":1,"format":"email"},
  "nickname":{"type":["string","null"],"minLength":1},
  "age":{"type":["integer","null"],"minimum":0,"default":20},
  "role":{"type":"string","enum":["ADMIN","MEMBER","GUEST"]},
  "address":{"type":"object","properties":{"street":{"type":"string","minLength":1},"city":{"type":"string","minLength":1}},"required":["street","city"]},
  "tags":{"type":["array","null"],"items":{"type":"string","minLength":1},"default":[]}},
 "required":["email","role","address"]}
```

* A property is `required` unless it is `optional()`, `nullable()`, has a
  `defaultValue(...)` or a `recover(...)` without `required()`, or is read with
  `presence()`. Such a property allows the type `null` unless it ends with
  `rejectingNull()`.
* The constraints of a list or map are written as `minItems`, `maxItems`, `uniqueItems`,
  `minProperties` and `maxProperties`, `rejectingUnknownProperties()` as
  `additionalProperties: false`, and a recursive structure as `$ref`.
* A lambda and the result of `ValueValidator.compose` are unknown; describe them with
  `describedAs(...)`. Conditional constraints and `constraintOnTarget` are not described.
* A bean `Validator` built with `ValidatorBuilder` is not described.

## Nullness with JSpecify

YAVI is annotated with [JSpecify](https://jspecify.dev), so NullAway and Kotlin check the
nullness of its API. A value that passed validation is handed over with its declared
type, so the function given to `apply` needs no `@Nullable` parameters and no
`Objects.requireNonNull`:

```java
record Member(String name, @Nullable String nickname) {}

Arguments2Validator<String, @Nullable String, Member> memberValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._string("nickname", c -> c.lessThanOrEqual(20)).nullable()
        .apply(Member::new);
```

Only the value of a `nullable()` property is `@Nullable`. See
[Limitations of NullAway](NullAwayLimitations.md) if NullAway reports correct code.

## Related pull requests

| Feature | Pull request |
| --- | --- |
| Sealed `Validated`, `path()` | [#537](https://github.com/making/yavi/pull/537) |
| Combinators, `_object`, `_list`, `_map`, `constraintOnTarget` | [#538](https://github.com/making/yavi/pull/538) |
| `description()`, JSON Schema | [#539](https://github.com/making/yavi/pull/539) |
| `fromMap`, `untyped()`, `DiscriminatedUnionValidator` | [#540](https://github.com/making/yavi/pull/540) |
| `InputReader`, `readingWith` | [#541](https://github.com/making/yavi/pull/541) |
| `coercingStrings()`, `readingParameters()` | [#542](https://github.com/making/yavi/pull/542), [#543](https://github.com/making/yavi/pull/543) |
| `_embedded`, `_rest` | [#545](https://github.com/making/yavi/pull/545) |
| `Yavi._string` and so on | [#546](https://github.com/making/yavi/pull/546) |
| Transforms, `preprocess` | [#547](https://github.com/making/yavi/pull/547) |
| Constraints of `_list` and `_map` | [#548](https://github.com/making/yavi/pull/548) |
| `flatApply`, `flatFromMap`, `failureWith`, record `ArgumentsN` | [#549](https://github.com/making/yavi/pull/549) |
| `recover`, `recoverWith` | [#550](https://github.com/making/yavi/pull/550) |
| `messagesByName()`, `pointer()`, `ViolationDetail` | [#551](https://github.com/making/yavi/pull/551) |
| `ignoringCase()`, `StringCoercion`, `fractionDigits` | [#552](https://github.com/making/yavi/pull/552) |
| `lazy`, `recursive` | [#553](https://github.com/making/yavi/pull/553) |
| `presence()` | [#555](https://github.com/making/yavi/pull/555) |
| `required()`, `rejectingNull()` | [#562](https://github.com/making/yavi/pull/562) |
| Modifier chains without effect do not compile, `NullAcceptingValueValidator` | [#563](https://github.com/making/yavi/pull/563), [#564](https://github.com/making/yavi/pull/564), [#565](https://github.com/making/yavi/pull/565) |
| JSpecify, values without `@Nullable` | [#534](https://github.com/making/yavi/pull/534), [#556](https://github.com/making/yavi/pull/556), [#557](https://github.com/making/yavi/pull/557) |
