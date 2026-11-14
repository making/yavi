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
* The constraints can be omitted too, with or without a name, as for a property of
  `Yavi.schema()`: `Yavi._boolean("subscribed")`, `Yavi._enum("role", Role.class)`,
  `Yavi._object("id", UUID.class)`, `Yavi._string()`.
* `Yavi._list(elementValidator, c -> c.notEmpty().unique())` and
  `Yavi._map(valueValidator, c -> c.notEmpty())` validate a list or map and constrain it;
  `Yavi._list(elementValidator)` and `Yavi._map(valueValidator)` only validate the
  elements or values.
* `named(name)` uses a validator under another name; see
  [below](#using-a-rule-under-another-name).

### Transforming a value before its constraints

The transforms of a `String` are written before its constraints. Any other value is
transformed with `preprocess`. `null` is not transformed.

```java
StringValidator<String> emailValidator = Yavi._string("email", c -> c.trim().toLowerCase().notBlank().email());
emailValidator.validate("  USER@Example.COM "); // Valid[value=user@example.com]

BigDecimalValidator<BigDecimal> priceValidator = Yavi._bigDecimal("price", c -> c.positive())
        .preprocess(BigDecimal::stripTrailingZeros);
```

The transforms of a `String` are `trim()`, `toLowerCase()`, `toUpperCase()`,
`toNormalForm(form)` and `transform(f)`.

### Using a rule under another name

A validator holds the name it was built with. `named(name)` returns the same validator
under another name, so the rule of a value is defined once.

```java
public final class Email {
    public static final StringValidator<String> VALIDATOR = Yavi._string("email", c -> c.trim().notBlank().email());
}

record Contact(String email, @Nullable String backupEmail, String replyTo) {}

ObjectInputValidator<Contact> contactValidator = Yavi.schema()
        ._string(Email.VALIDATOR)                                 // the key and the name are "email"
        ._string(Email.VALIDATOR.named("backupEmail")).nullable() // the key and the name are "backupEmail"
        ._object("replyTo", Email.VALIDATOR)                      // the same as named("replyTo")
        .fromMap(Contact::new);

contactValidator.validate(Map.of("email", "a", "backupEmail", "b", "replyTo", "c"));
// email:       "email" must be a valid email address
// backupEmail: "backupEmail" must be a valid email address
// replyTo:     "replyTo" must be a valid email address
```

* The name replaces the one of a validator of a single value in the violations, in
  their messages and in `description()`, so it is the key of a property added with the
  validator alone.
* A value that has no name of its own (an object, a list, a map, the value of a lambda)
  is located under the name, as with `prefixed(name)`: `addressValidator.named("home")`
  reports `home.street`.
* The combinators carry the name. `Email.VALIDATOR.map(Email::new).nullable().named("x")`
  and `Email.VALIDATOR.named("x").map(Email::new).nullable()` are the same, and so are
  `untyped().named("x")` and `named("x").untyped()`. `andThen` gives the name to both
  validators and `anyOf` to every alternative.
* `_object(name, validator)` of a schema uses the validator under `name`.

### Combinators

Every `ValueValidator` has:

| Method | Result |
| --- | --- |
| `nullable()`, `optional()`, `defaultValue(value)` | accepts `null` and produces `null`, `Optional.empty()` or the default value |
| `recover(fallback)`, `recoverWith(violations -> ...)` | never fails: produces the fallback for `null` and for an invalid value |
| `liftList()`, `liftSet()`, `liftMap()` | validates every element or value, located under its index or key; the elements may be of a subtype of the input, so a validator of `Object` lifted to a list validates a `List<String>` |
| `liftList(separator)` | `liftList()` that also reads the list from one text, such as `1,2,3` |
| `prefixed(name)` | puts a name before the names of the violations (`orders[1].id`) |
| `named(name)` | reports the violations of the value under another name |
| `ValueValidator.anyOf(a, b)` | the first alternative that is valid; otherwise one violation, `object.anyOf`, a `ConstraintViolation.NoAlternativeMatched` with the violations of each alternative in its `alternatives()` |
| `ValueValidator.lazy(supplier)`, `recursive(self -> ...)` | a reference to a validator that is not built yet |

### New constraints

`BigDecimalConstraint` has `fractionDigits(max)` and `integerDigits(max)`, the
counterpart of `@Digits` of Bean Validation. The digits of the value are counted, so
`1.500` has one fraction digit.

```java
Yavi._bigDecimal("price", c -> c.fractionDigits(2).integerDigits(6));
// 1.505: "price" must have at most 2 fraction digits
```

The constraints of the integral types, `BigInteger` and `BigDecimal` have
`multipleOf(divisor)`. `Double` and `Float` do not have it, because the remainder of a
binary fraction is not exact.

```java
Yavi._integer("quantity", c -> c.multipleOf(12));
// 30: "quantity" must be a multiple of 12
Yavi._bigDecimal("price", c -> c.multipleOf(new BigDecimal("0.05")));
```

`CharSequenceConstraint` has three more formats:

| Constraint | Accepts | Message key |
| --- | --- | --- |
| `ip()` | an IPv4 or an IPv6 address | `charSequence.ip` |
| `uri()` | a URI with a scheme of any kind (`mailto:a@example.com`, `urn:isbn:...`, `s3://bucket/key`), unlike `url()`, which needs a protocol that `java.net.URL` handles | `charSequence.uri` |
| `ulid()` | a ULID, in upper or lower case | `charSequence.ulid` |

`JsonSchema` writes `multipleOf` and `"format": "uri"` for them.

Every format accepts an empty string, as `email()` and `url()` always did: a format tells
what a given text looks like, and `notBlank()` or `notEmpty()` tells that it is given. So
an optional text has the same constraints whatever its format is. `ipv4()`, `ipv6()`,
`luhn()`, `isoLocalDate()` and `localDate(pattern)` rejected `""` in 0.17; see the
[breaking changes](BreakingChanges_0.18.0.md#the-formats-of-a-string-accept-an-empty-string).

```java
Yavi._string("homepage", c -> c.url()).validate("");       // valid
Yavi._string("proxy", c -> c.ip()).validate("");           // valid
Yavi._string("proxy", c -> c.notBlank().ip()).validate(""); // "proxy" must not be blank
```

## Optional properties, nested objects, lists and maps

A property of `Yavi.schema()` is required unless a modifier after it says otherwise,
and can be an object, a list or a map.

```java
ObjectInputValidator<Address> addressValidator = Yavi.schema()
        ._string("street", c -> c.notBlank())
        ._string("city", c -> c.notBlank())
        .fromMap(Address::new); // see the next section

Arguments6Validator<String, String, Integer, Role, Map<String, Object>, List<? extends String>, User> userValidator = Yavi.schema()
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
  apply to the last added property.
* `_object(name, validator)`, `_list(name, elementValidator)` and
  `_map(name, valueValidator)` locate their violations under the name, in `name()`
  (`address.street`, `tags[1]`, `prices.pen`) and in `path()`.
* The constraints of `_list` and `_map` are checked when every element is valid and are
  reported on the property: `"tags" must be unique. [a] is/are duplicated.`
* `constraintOnTarget` is a rule that spans several properties. It is checked when all
  properties are valid.
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
| A `UUID`, a `URI`, a `Duration`, a `Period` (`_object("id", UUID.class)`) | read from the canonical string of the UUID, the string of the URI, the ISO-8601 string (`PT1H30M`, `P1Y2M`); otherwise `object.type`: `"id" must be of type UUID` |
| An absent key | validated like `null`: accepted by `optional()`, `nullable()`, `defaultValue(...)`, `recover(...)`, otherwise `object.required`; always `object.required` after `required()` |
| An explicit `null` | `object.notNull`, or the violation of a constraint that rejects `null`; always `object.notNull` after `rejectingNull()` |
| A key that no property is defined for | ignored; `rejectingUnknownProperties()` reports `object.unknownProperty` |

* `ignoringCase()` reads the name of an enum constant in any case:
  `._enum(Yavi._enum("role", Role.class).ignoringCase())` takes `"admin"`.
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
the values as they are. `_rest` is the last property of a schema, and its value is an
empty map if there is no other key, never `null`.

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
`presence()`: `{"nickname": null}` is `"nickname" must not be null`.

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

| Property | An absent key | An explicit `null` |
| --- | --- | --- |
| `_string("x")` | `object.required` | `object.notNull` |
| `.nullable()`, `.optional()`, `.defaultValue(...)`, `.recover(...)` | accepted | accepted |
| followed by `.required()` | `object.required` | accepted |
| followed by `.rejectingNull()` | accepted | `object.notNull` |
| `.presence()` | `Absent` | validated by the property |

`required()` and `rejectingNull()` are written after `optional()`, `nullable()`,
`defaultValue(...)` or `recover(...)`. Typed arguments (`apply`) have no absent state, so
`null` is validated there as without them.

### A schema whose properties are known at run time

The properties of `Yavi.schema()` are fixed when the code is compiled, because their
values are given to a constructor. `Yavi.dynamicSchema()` describes an input whose
properties come from configuration or a database, such as the questions of a survey or
custom fields. Its validator produces a map of the validated values by their names.

```java
record Question(String id, String title, boolean required, int maxLength) {}

DynamicSchema<String> schema = Yavi.dynamicSchema();
for (Question question : survey.questions()) {
    StringValidator<String> answer = Yavi._string(c -> c.notBlank().lessThanOrEqual(question.maxLength()));
    schema = question.required() ? schema.property(question.id(), answer).label(question.title())
            : schema.property(question.id(), answer.defaultValue("")).label(question.title());
}
ObjectInputValidator<Map<String, String>> answersValidator = schema.build();

answersValidator.validate(Map.of("q1", "yavi")); // {q1=yavi, q2=}
answersValidator.validate(Map.of());             // q1: "Your name" is required
```

A schema is immutable: `property` returns a new one. The validator of a property is used
under the name of the property, as `named(name)` states.

| Property | In the map |
| --- | --- |
| `property(name, validator)` | the validated value; the value is required |
| `property(name, validator.nullable())`, `.defaultValue(...)`, `.recover(...)` | what the validator produces for an absent key and for `null` |
| followed by `.required()` or `.rejectingNull()` | as in `Yavi.schema()` |
| followed by `.presence()` | no entry if the key is absent |
| `rest(valueValidator)` | the validated values of the keys that no property is defined for |

* `label`, `required(message)` and `typeMessage` are those of
  [a property of a schema](#labels-and-messages-of-a-property).
* The modifiers have the staged types of `Yavi.schema()`: `required()` and
  `rejectingNull()` exist only for a validator that accepts `null`, and no modifier
  follows `presence()`.
* The type of the values is the one the schema is assigned to.
  `Yavi.<Object>dynamicSchema()` takes properties of several types.
* The validator is the one of `fromMap`: it has `rejectingUnknownProperties()`,
  `readingWith(reader)`, `readingParameters()` and `description()`, and it is a property
  of another schema.

```java
record Response(String email, Map<String, String> answers) {}

ValueValidator<Map<String, ?>, Response> responseValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        ._object("answers", answersValidator) // answers.q1=...&answers.q2=...
        .fromMap(Response::new)
        .readingParameters();
```

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

A reader returns a number as the tree holds it. A default `JsonMapper` holds a number with
a fraction as a `double`, in a tree and in a map alike: `_bigDecimal` receives `0.10` as
`0.1` and `12345678901234567.89` as `12345678901234568`, `_double` receives `1e400` as
`Infinity`, and the last of a key written twice replaces the others. Tell the mapper to
keep a number as it is written and to refuse a key written twice:

```java
JsonMapper jsonMapper = JsonMapper.builder()
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS) // 0.10 is the BigDecimal 0.10
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION) // {"role": "user", "role": "admin"} is refused
        .build();
```

The tree model of the JDK ([JEP 540](https://openjdk.org/jeps/540), an incubator module of
JDK 28) is read in the same way. Its reader, shown in the README, reads a number from its
text, and its parser refuses a key written twice.

A tree whose scalars are all strings is read with `coercingStrings()` of its reader:
`JsonNodeReader.INSTANCE.coercingStrings()`, or
`InputReader.ofJdkTypes().coercingStrings()` for a tree of JDK types. The strings are read
by the rules of the next section.

## Validating request parameters

Request parameters and a submitted form are a flat map of strings, possibly several for
one key. `readingParameters()` of a validator built with `fromMap` reads such a map: a
string is text that stands for a value of the type of its property, and a key is the path
of a nested value.

```java
record SearchForm(String q, Integer page, Boolean includeArchived, @Nullable LocalDate since, List<String> tags,
        @Nullable Address address) {}

ValueValidator<Map<String, ?>, SearchForm> searchFormValidator = Yavi.schema()
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

The validator takes every form of such a map: a `Map<String, String>`, a
`Map<String, List<String>>` such as Spring's `MultiValueMap`, and the
`Map<String, String[]>` of the Servlet API. Its type is `ValueValidator<Map<String, ?>, X>`
whatever it is assigned to, so any combinator follows `readingParameters()`:

```java
ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
        ._string("email", c -> c.notBlank().email())
        .fromMap(Signup::new)
        .readingParameters()
        .formattingWith(new MessageSourceMessageFormatter(messageSource::getMessage));
```

The values:

* An empty string is validated like `null`, whatever the type of its property is. Give
  `defaultValue("")` to a string property that must receive `""`.
* Text has no `null`, so a property that would report an empty string with
  `object.notNull` reports `object.required` (`"page" is required`), as it does for an
  absent key. The violation of a constraint that rejects `null`, such as `notBlank()` or
  `notNull().message(...)`, is reported as it is.
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
  `items[0].name=pen&items[1].name=ink`.
* An empty list cannot be sent: its key is absent. Give `defaultValue(List.of())` to a
  list that may be empty, such as checkboxes.
* `splitBy` reads a list from one value (`tags=a,b`); see
  [below](#a-list-in-one-parameter).

`StringCoercion` relaxes these rules for text written by a person, such as the fields
of a form or the cells of a CSV file:

```java
record Signup(Integer age, Boolean subscribed) {}

StringCoercion form = StringCoercion.strict().trimming().booleanWords();

ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
        ._integer("age")
        ._boolean("subscribed").defaultValue(false) // an unchecked checkbox sends nothing
        .fromMap(Signup::new)
        .readingParameters(form);

signupValidator.validate(Map.of("age", " 30 ", "subscribed", "on")); // Signup[age=30, subscribed=true]
```

* `trimming()`: every text is read without its surrounding white space, whatever its
  property is, and a blank string is validated like `null`.
* `booleanWords()`: `true`, `1`, `yes`, `on` and `false`, `0`, `no`, `off` in any case.

`trimming()` trims a `String` property too, so one switch makes every field "trimmed, and
not given if blank"; no property repeats `c.trim()`. A `String` whose white space is a
part of it, such as a password, says so with `untrimmed()` of its validator.

```java
record Contact(String name, @Nullable String company, String password) {}

ValueValidator<Map<String, ?>, Contact> contactValidator = Yavi.schema()
        ._string("name", c -> c.notBlank().lessThanOrEqual(50))
        ._string("company").nullable()
        ._string(Yavi._string("password", c -> c.greaterThanOrEqual(8)).untrimmed())
        .fromMap(Contact::new)
        .readingParameters(StringCoercion.strict().trimming());

contactValidator.validate(Map.of("name", " Alice ", "company", "  ", "password", " pass word "));
// Contact[name=Alice, company=null, password= pass word ]
```

* The constraints see the trimmed text: `" Alice "` has 5 characters.
* `untrimmed()` keeps the white space around a value. A blank value is still not given.
  It returns an `Arguments1Validator`, so it is written once, after `map` and `named`.
* A reader that does not trim (`readingParameters()`, a JSON tree) reads a `String` as it
  is given; `c.trim()` trims it there.

`recover` suits a parameter that must not fail a request:

```java
record PageRequest(Integer page, Integer size) {}

ValueValidator<Map<String, ?>, PageRequest> pageValidator = Yavi.schema()
        ._integer("page", c -> c.greaterThanOrEqual(0)).recover(0)
        ._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
        .fromMap(PageRequest::new)
        .readingParameters();

pageValidator.validate(Map.of("page", "-1", "size", "9999")); // PageRequest[page=0, size=20]
```

### A list in one parameter

One parameter often holds several values: `?tags=java,yavi`, `?ids=1,2,3`, one URI per
line of a `textarea`. `splitBy` after `_list` reads such a text as the list.

```java
record Article(String title, List<String> tags, List<Integer> related, List<URI> links) {}

ValueValidator<Map<String, ?>, Article> articleValidator = Yavi.schema()
        ._string("title", c -> c.notBlank())
        ._list("tags", Yavi._string(c -> c.notBlank().lessThanOrEqual(10)), c -> c.unique()).splitBy(",")
        ._list("related", Yavi._integer(c -> c.positive())).splitBy(",").defaultValue(List.of())
        ._list("links", Yavi._object(URI.class)).splitBy(Pattern.compile("\\R")).defaultValue(List.of())
        .fromMap(Article::new)
        .readingParameters();

articleValidator.validate(Map.of("title", "t", "tags", "java,yavi", "related", "1,2,3"));
// Article[title=t, tags=[java, yavi], related=[1, 2, 3], links=[]]

articleValidator.validate(Map.of("title", "t", "tags", "java,,toolongtagname", "related", "1,x"));
// tags[1]:    "tags[1]" must not be blank
// tags[2]:    The size of "tags[2]" must be less than or equal to 10. The given size is 14
// related[1]: "related[1]" must be of type integer
```

* The separator is a text taken literally, or a `Pattern`.
* The elements are text that stands for a value, like any parameter, and their
  violations are located under their index, like those of a repeated key.
* Every text is split, also the values of a repeated key: `tags=a,b&tags=c` is
  `[a, b, c]`.
* No element is dropped or trimmed. `a,,b` has three elements, and the second one has no
  value. Only a separator at the end starts no element: `a,b,` is `[a, b]`, like the line
  break after the last line. White space is removed by the pattern (`\\s*,\\s*`), by
  `StringCoercion.strict().trimming()` or by `c.trim()`.
* An empty parameter (`tags=`) has no value, like that of any property.
* `splitBy` is written directly after `_list`, before the other modifiers.
* Another input is read the same way: `{"tags": "java,yavi"}` and
  `{"tags": ["java", "yavi"]}` are the same list. The JSON Schema of the property has
  `"type": ["array", "string"]`.
* A value that is neither text nor a list, such as `{"tags": 1}`, is reported with
  `object.type` (`"tags" must be of type array or string`); it is not taken for a list of
  one element. An element of a list that is not text is validated as it is.
* A list that is not a property of a schema is split with
  `Yavi._list(elementValidator, constraints).splitBy(",")` or
  `elementValidator.liftList(pattern)`.
* Typed arguments (`apply`) hold the elements already and are validated as before.

### Labels and messages of a property

A form calls a field by its label, and some messages come from no constraint: a missing
value (`object.required`) and a value of another type (`object.type`) are reported
before any constraint is checked. A property says how they read:

```java
record Token(String name, Integer ttl, List<String> roles) {}

ValueValidator<Map<String, ?>, Token> tokenValidator = Yavi.schema()
        ._string("name", c -> c.notBlank()).label("Token name")
        ._integer("ttl", c -> c.positive()).typeMessage("\"{0}\" must be a number of seconds")
        ._list("roles", Yavi._string(c -> c.notBlank())).label("Roles").required("Select at least one role")
        .fromMap(Token::new)
        .readingParameters();

tokenValidator.validate(Map.of("name", List.of(""), "ttl", List.of("abc"))).errors().messagesByName();
// {name=["Token name" must not be blank], ttl=["ttl" must be a number of seconds], roles=[Select at least one role]}
```

| Modifier | What it changes |
| --- | --- |
| `label("Token name")` | `{0}` of every message of the property and of its elements (`roles`, `roles[1]`). The name, the path and the pointer of a violation are unchanged, so `messagesByName()` and a `ViolationDetail` still have `name`. `violation.label()` returns the label. |
| `typeMessage(text)` | The message of `object.type` of the property and of its elements. `{1}` is the name of the type and `{2}` the value. |
| `required(text)` | The message of `object.required`: an absent key, and text without a value. After `optional()`, `nullable()` or `defaultValue(...)` it also requires the key, as `required()` does. |

* A text is the message, as that of `message("...")` of a constraint is: a formatter that
  has a message for `object.required` does not replace it (see below).
* `typeMessage` and `required` take a `ViolationMessage` too, which replaces the message
  key as well, so that a resource bundle can localize it:
  `required(ViolationMessage.of("token.roles", "Select at least one role"))`.
* Write `label` and `typeMessage` right after the property is added, before `optional()`
  and the like. `required(...)` is the last modifier: `.required("...").optional()` does
  not compile.
* A violation of a nested property (`address.street`) is left to that property. An
  explicit `null` and an enum constant that does not exist are reported as before
  (`object.notNull`, `object.oneOf`).
* A property of `Yavi.dynamicSchema()` has the same modifiers.
* A property of `ObjectInputValidator.of(parts, mapper)` takes the same as a
  `PropertyMessages`.

The messages of the constraints come from a `MessageFormatter`, as those of
`ValidatorBuilder` do. A validator formats every violation it reports with
`formattingWith`, and violations are given another formatter or labels of a resource
bundle afterwards:

```java
MessageFormatter formatter = new MessageSourceMessageFormatter(messageSource::getMessage);

// every violation of the validator, those of nested validators and of the reader included
ValueValidator<Map<String, ?>, Token> localized = tokenValidator.formattingWith(formatter);

// or when the violations are shown
violations.labeled(violation -> messageSource.getMessage("label." + violation.name(), null, null, locale))
        .formattedWith(formatter)
        .messagesByName();
```

`labeled` leaves a violation as it is when the function returns `null`.

A formatter looks up a message for the key of a violation, but not for a text written in
code for one constraint or one property: `message("...")`, `required("...")` and
`typeMessage("...")`. The more specific text wins over a message that the bundle has for
every violation of the key, and the violation keeps the key:

```java
// messages.properties: object.required={0} is required.
tokenValidator.formattingWith(formatter).validate(Map.of()).errors().messagesByName();
// {name=[Token name is required.], ttl=[ttl is required.], roles=[Select at least one role]}
```

| API | What it does |
| --- | --- |
| `ViolationMessage#withText(text)` | The message with the key and the argument names of this one and the text as its format, which no formatter replaces. `OBJECT_REQUIRED.withText("...")` is what `required("...")` gives. |
| `ViolationMessage#localizable()`, `ConstraintViolation#localizable()` | `false` for a text. |
| `MessageFormatter#format(String, Object[], Locale)` | Formats a text. The default formats it with `MessageFormat`; override it to format a text as the formatter formats the other messages. |
| `Constraint#predicates(List)` | Adds several predicates as one constraint, as `url(url -> ...)` and `password(...)` do, so that `message(...)` right after replaces the message of each. |

### Writing the parameters back

A form that is not valid is shown again with the text that was sent. `ParameterTree`
returns the parameters as a validator reads them: the properties that the validator
describes with what was sent for them, in the shape of the schema. The application does
not copy the fields by hand.

```java
record Signup(String name, Integer age, List<String> tags, Address address) {}

ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
        ._string("name", c -> c.notBlank())
        ._integer("age")
        ._list("tags", Yavi._string()).defaultValue(List.of())
        ._object("address", addressValidator)
        .fromMap(Signup::new)
        .readingParameters(StringCoercion.strict().trimming());

// name=+Alice+&age=abc&tags=a&address[city]=Tokyo&utm_source=mail
ParameterTree tree = ParameterTree.of(params);
Map<String, Object> form = tree.toMap(signupValidator.description());
// {name=" Alice ", age="abc", tags=[a], address={city="Tokyo"}}
Map<String, Object> flat = tree.toParameters(signupValidator.description());
// {name=" Alice ", age="abc", tags=[a], address.city="Tokyo"}

// nothing was sent
ParameterTree.of(Map.of()).toMap(signupValidator.description());
// {name="", age="", tags=[], address={city=""}}
```

* A value is the text that was sent. It is not trimmed, coerced or validated.
* `toMap` returns a tree of `Map`, `List` and the values, for a template engine such as
  Mustache. `toParameters` returns the same values under the names of the violations
  (`address.city`, `items[0].name`), for a `MapBindingResult` of Spring.
* A list is a `List` also for one value. A list with `splitBy` that was sent as one text
  is that text, as a text field shows it.
* A key that no property is defined for (`utm_source`) is left out. A schema with `_rest`
  reads the other keys, so they are returned.
* The entries are the same for every request. A single value that was not sent is an
  empty string, as an empty text field sends it. A list or a map is empty and an object
  has its properties. The same is returned for a value that has not the shape of its
  property, such as two values for `age`; the violation reports it. A template that fails
  for an absent key, such as one of JMustache, does not fail for a request that lacks a
  parameter, and the same call gives the empty form.
* A `Boolean` that was not sent has no entry, because a checkbox that is not checked
  sends nothing and an empty string is not `false` to every template engine. A property
  with `presence()` and a structure that contains itself have no entry either.
* The returned maps and lists are new. Add what a page needs: `form.put("id", id)`.
* For a discriminated union the properties of all alternatives are returned.
* The description is the one of the validator that validates the parameters. Give
  `ParameterTree.of(params, maxIndex)` the same largest index if the validator reads a
  tree with another one.
* The map holds every property, a password included. The template decides what it shows.

`ParameterTree.parametersOf(map)` is the reverse of `ParameterTree.of(parameters)` for any
tree of `Map` and `List`:

```java
ParameterTree.parametersOf(Map.of("address", Map.of("city", "Tokyo"), "tags", List.of("a", "b")));
// {address.city=Tokyo, tags=[a, b]}
```

### Showing the form again in Spring MVC

Spring MVC creates a `BindingResult` only for a form object that it binds, not for a map
of parameters. A `@ModelAttribute` method of the controller puts a `MapBindingResult` of
the parameters into the model. A handler then takes the `BindingResult` as an argument,
and `th:object`, `th:field` and `#fields` of Thymeleaf work as they do with a form object.
No library is needed.

```java
@Controller
class SignupController {

    static final ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
            ._integer("age")
            ._boolean("subscribed").defaultValue(false)
            .fromMap(Signup::new)
            .readingParameters(StringCoercion.strict().trimming().booleanWords());

    @ModelAttribute
    void signupForm(@RequestParam MultiValueMap<String, String> params, Model model) {
        Map<String, Object> form = ParameterTree.of(params).toParameters(signupValidator.description());
        model.addAllAttributes(new MapBindingResult(form, "signupForm").getModel());
    }

    @GetMapping("/signup")
    String form() {
        return "signup";
    }

    @PostMapping("/signup")
    String signup(@RequestParam MultiValueMap<String, String> params, BindingResult result) {
        return switch (signupValidator.validate(params)) {
            case Validated.Valid<Signup>(Signup signup) -> {
                register(signup);
                yield "redirect:/";
            }
            case Validated.Invalid<Signup>(ConstraintViolations violations) -> {
                violations.apply(result::rejectValue);
                yield "signup";
            }
        };
    }

}
```

```html
<form method="post" th:action="@{/signup}" th:object="${signupForm}">
    <input type="text" th:field="*{age}">
    <span th:if="${#fields.hasErrors('age')}" th:errors="*{age}"></span>
    <input type="checkbox" th:field="*{subscribed}" value="on">
    <button type="submit">Sign up</button>
</form>
```

* The `@ModelAttribute` method runs before every handler of the controller, so the GET
  handler renders the empty form. Without the method a request fails with
  `An Errors/BindingResult argument is expected to be declared immediately after the model attribute`.
* Spring passes the `BindingResult` that is the last attribute of the model. Do not put
  an argument that adds a model attribute before it.
* A field shows the text that was sent again, even if it is not of the type of its
  property (`abc` for `age`).
* The code of a field error is the message key (`object.type`), so a `MessageSource`
  replaces the message as before. `{0}` of that message is the label of the property if
  it has one.
* Thymeleaf adds a hidden field `_subscribed` to a checkbox. No property is defined for
  that key, so such a form cannot be read with `rejectingUnknownProperties()`.
* Name a field as `th:field` does: `address.city`, `items[0].name`. These are the names
  of the violations and the keys that `MapBindingResult` looks up. `toParameters`
  returns these keys, so a value sent as `address[city]` is shown again too.
* The handler validates a `MultiValueMap`, so several values for one key are a list, or
  `object.type` for a scalar property. A `Map<String, String>` would drop all but the
  first.
* `toParameters` gives the text of a single value and the list of a list property, so a
  text field shows `30` and every checked checkbox (`tags=a&tags=b`) is checked again.
  The violation of an element is at `tags[1]`, which `#fields.hasErrors('tags*')` finds.

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
//   "message": "\"address.street\" must not be blank", "params": {}}, ...]
```

`params` of a detail holds the arguments of the violation by name, so that a client can
render a message of its own from `messageKey` without knowing which position of the
message format holds what. The value that violated the constraint is not in a detail: it
may be a password or a token, and the client has sent it. `ConstraintViolation` has both,
as `args()` and `violatedValue()`.

```java
// _string("password", c -> c.greaterThanOrEqual(8)) and _integer("age", c -> c.lessThanOrEqual(150))
List<ViolationDetail> details = violations.details();
// [{"name": "password", "pointer": "/password", "messageKey": "container.greaterThanOrEqual",
//   "message": "The size of \"password\" must be greater than or equal to 8. The given size is 6",
//   "params": {"min": 8, "actualSize": 6}},
//  {"name": "age", "pointer": "/age", "messageKey": "numeric.lessThanOrEqual",
//   "message": "\"age\" must be less than or equal to 150", "params": {"max": 150}}]

ConstraintViolation violation = violations.get(1);
violation.params();        // {max=150}
violation.args();          // ["age", 150, 300], the positional arguments of the message format
violation.violatedValue(); // 300
```

The names are those of the parameters of the constraint method. What a constraint computes
from the value has a name too (`actualSize`, `duplicates`, `missing`, `notAllowed`). A
message key that is not listed has no parameters:

| Message key | Names in `params` |
| --- | --- |
| `object.equalTo`, `map.containsValue` | `value` |
| `object.oneOf`, `object.notOneOf`, `charSequence.url.protocol`, `charSequence.url.host`, `charSequence.url.port` | `values` |
| `object.anyOf` | `count` |
| `object.type` | `type` |
| `numeric.greaterThan`, `numeric.greaterThanOrEqual` | `min` |
| `numeric.lessThan`, `numeric.lessThanOrEqual`, `numeric.fractionDigits`, `numeric.integerDigits` | `max` |
| `numeric.multipleOf` | `divisor` |
| `container.greaterThan`, `container.greaterThanOrEqual`, `byteSize.greaterThan`, `byteSize.greaterThanOrEqual` | `min`, `actualSize` |
| `container.lessThan`, `container.lessThanOrEqual`, `byteSize.lessThan`, `byteSize.lessThanOrEqual` | `max`, `actualSize` |
| `container.fixedSize`, `byteSize.fixedSize` | `size`, `actualSize` |
| `charSequence.contains` | `substring` |
| `charSequence.startsWith` | `prefix` |
| `charSequence.endsWith` | `suffix` |
| `charSequence.pattern`, `charSequence.localdate` | `pattern` |
| `collection.contains`, `array.contains` | `element` |
| `collection.unique` | `duplicates` |
| `collection.containsAll` | `values`, `missing` |
| `map.containsKey` | `key` |
| `codePoints.asWhiteList`, `codePoints.asBlackList` | `notAllowed` |
| `password.required` | `policy` |
| `password.optional` | `min`, `policies` |
| `temporal.before`, `temporal.beforeOrEqual`, `temporal.after`, `temporal.afterOrEqual` | `other` |
| `temporal.between` | `rangeFrom`, `rangeTo` |
| `temporal.field` | `field` |

A message of your own names its arguments from `{1}` on in `ViolationMessage.of`. Without
names the violation has no parameters. The message format keeps referring to the arguments
by position:

```java
ViolationMessage outOfStock = ViolationMessage.of("order.outOfStock", "\"{0}\" exceeds the stock of {1}", "stock");
return Validated.failureWith(outOfStock, quantity, locale, stock);
// "messageKey": "order.outOfStock", "params": {"stock": 3}
```

When no alternative of `ValueValidator.anyOf` matches, the result is one violation. The
violations of the alternatives are not mixed into the list, where nothing would tell
which alternative a violation belongs to. The violation is a
`ConstraintViolation.NoAlternativeMatched`, the only type that has `alternatives()`: one
`ConstraintViolations` per alternative, prefixed with the violation:

```java
ConstraintViolations violations = Yavi.schema()
    ._object("contact", ValueValidator.anyOf(emailContact, phoneContact))
    .fromMap(contact -> contact)
    .validate(Map.of("contact", Map.of("kind", "email", "value", "x")))
    .errors();

violations.messagesByName();
// {contact=["contact" must satisfy at least one of the 2 alternatives]}

if (violations.get(0) instanceof ConstraintViolation.NoAlternativeMatched unmatched) {
    unmatched.alternatives().get(0).messagesByName();
    // {contact.value=["contact.value" must be a valid email address]}
    unmatched.alternatives().get(1).messagesByName();
    // {contact.kind=["contact.kind" must be equal to phone], contact.value=["contact.value" must match ^\d{10,15}$]}

    // the alternative with the fewest violations, to show instead of the summary
    ConstraintViolations closest = unmatched.alternatives().stream()
        .min(Comparator.comparingInt(ConstraintViolations::size))
        .orElseThrow();
}

// a switch over the details needs no default: the two records are all there is
for (ViolationDetail detail : violations.details()) {
    List<String> messages = switch (detail) {
        case ViolationDetail.Simple simple -> List.of(simple.message());
        case ViolationDetail.NoAlternativeMatched unmatched -> unmatched.alternatives().stream()
            .flatMap(List::stream)
            .map(ViolationDetail::message)
            .toList();
    };
}
```

`ViolationDetail` is a sealed interface of two records. The detail of this violation is a
`ViolationDetail.NoAlternativeMatched`, which has `alternatives`, a list of the details of
each alternative; any other detail is a `ViolationDetail.Simple`, so its JSON has no such
property.

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

* A property is `required` unless it accepts an absent key.
* The constraints of a list or map are written as `minItems`, `uniqueItems` and so on,
  `rejectingUnknownProperties()` as `additionalProperties: false`, and a recursive
  structure as `$ref`.
* A lambda, a conditional constraint, `constraintOnTarget` and a bean `Validator` built
  with `ValidatorBuilder` are not described. `describedAs(...)` describes a lambda.

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
| `BindingResult` of request parameters in Spring MVC (documentation) | [#569](https://github.com/making/yavi/pull/569) |
| `label`, `required(message)`, `typeMessage`, `formattingWith`, `labeled`, `formattedWith` | [#571](https://github.com/making/yavi/pull/571) |
| A text written in code is not looked up, `ViolationMessage#withText`, `localizable()`, `Constraint#predicates(List)` | [#575](https://github.com/making/yavi/pull/575) |
| `ParameterTree#toMap`, `toParameters`, `parametersOf` | [#577](https://github.com/making/yavi/pull/577) |
| `object.required` for an empty text of a property, `InputReader#isEmptyText` | [#570](https://github.com/making/yavi/pull/570) |
| `named`, `splitBy`, `liftList(separator)`, `Yavi.dynamicSchema()` | [#572](https://github.com/making/yavi/pull/572) |
| A lifted validator, `_list` and `_map` take a container of a subtype | [#576](https://github.com/making/yavi/pull/576) |
| `trimming()` of a `String`, `untrimmed()`, a name without constraints (`Yavi._enum(name, type)`), `Yavi._list(elementValidator)`, an empty string for every format | [#573](https://github.com/making/yavi/pull/573) |
| `_embedded`, `_rest` | [#545](https://github.com/making/yavi/pull/545) |
| `Yavi._string` and so on | [#546](https://github.com/making/yavi/pull/546) |
| Transforms, `preprocess` | [#547](https://github.com/making/yavi/pull/547) |
| Constraints of `_list` and `_map` | [#548](https://github.com/making/yavi/pull/548) |
| `flatApply`, `flatFromMap`, `failureWith`, record `ArgumentsN` | [#549](https://github.com/making/yavi/pull/549) |
| `recover`, `recoverWith` | [#550](https://github.com/making/yavi/pull/550) |
| `messagesByName()`, `pointer()`, `ViolationDetail` | [#551](https://github.com/making/yavi/pull/551) |
| `params()` of a violation and of a detail, `ViolationMessage#paramNames()` | [#568](https://github.com/making/yavi/pull/568) |
| `ignoringCase()`, `StringCoercion`, `fractionDigits` | [#552](https://github.com/making/yavi/pull/552) |
| `lazy`, `recursive` | [#553](https://github.com/making/yavi/pull/553) |
| `presence()` | [#555](https://github.com/making/yavi/pull/555) |
| `required()`, `rejectingNull()` | [#562](https://github.com/making/yavi/pull/562) |
| `alternatives()` of `object.anyOf`, `multipleOf`, `ip()`, `uri()`, `ulid()`, `UUID`, `URI`, `Duration` and `Period` from a string | [#566](https://github.com/making/yavi/pull/566) |
| Modifier chains without effect do not compile, `NullAcceptingValueValidator` | [#563](https://github.com/making/yavi/pull/563), [#564](https://github.com/making/yavi/pull/564), [#565](https://github.com/making/yavi/pull/565) |
| JSpecify, values without `@Nullable` | [#534](https://github.com/making/yavi/pull/534), [#556](https://github.com/making/yavi/pull/556), [#557](https://github.com/making/yavi/pull/557) |
