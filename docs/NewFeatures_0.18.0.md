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
* [Writing an object back as the map it is read from](#writing-an-object-back-as-the-map-it-is-read-from)
* [Handling the result](#handling-the-result)
* [Describing a validator and generating a JSON Schema](#describing-a-validator-and-generating-a-json-schema)
* [Nullness with JSpecify](#nullness-with-jspecify)
* [Appendix: a signup form, its row and its edit form](#appendix-a-signup-form-its-row-and-its-edit-form)

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

`CollectionConstraint` has `unique(key)`: the elements are unique by a key of them, such
as the id of an object. The duplicated keys are in the message and in `params`. A JSON
Schema has no keyword for it, so `uniqueItems` is written for `unique()` only.

```java
Yavi.schema()._list("products", productValidator, c -> c.unique(Product::id));
// "products" must be unique. [APPLE] is/are duplicated.
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
* `splitBy` is written after `_list`, before the other modifiers. Only `label` and
  `typeMessage` may be written before it.
* A schema that is written back (`Yavi.reversibleSchema()`) gives a pattern the text that
  joins the elements, `splitBy(pattern, "\n")`, because it writes the list as one text
  ([A list in one text field](#writing-an-object-back-as-the-map-it-is-read-from)).
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
* Write `label` and `typeMessage` after the property is added, before `optional()`
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

Without Spring, `ResourceBundleMessageFormatter` looks a message up by its key in a
resource bundle on the class path:

```java
// messages_ja.properties: numeric.lessThanOrEqual="{0}" は {1} 以下で入力してください
MessageFormatter formatter = new ResourceBundleMessageFormatter("messages");
validator.formattingWith(formatter).validate(input, Locale.JAPANESE);
```

* A key that the bundle does not have is formatted with the default message, so a bundle
  holds only the messages it replaces.
* The bundle is that of the locale of the validation, then the base bundle. The bundle of
  the default locale of the platform is not a fallback, unlike
  `ResourceBundle.getBundle`: a validation in English on a Japanese server is not
  answered in Japanese.

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
// the map that readingParameters() validates: the Servlet API here, or
// @RequestParam MultiValueMap<String, String> params of Spring
Map<String, String[]> params = request.getParameterMap();
ParameterTree tree = ParameterTree.of(params);
Map<String, Object> form = tree.toMap(signupValidator.description());
// {name=" Alice ", age="abc", tags=[a], address={city="Tokyo"}}
Map<String, Object> flat = tree.toParameters(signupValidator.description());
// {name=" Alice ", age="abc", tags=[a], address.city="Tokyo"}

// nothing was sent
ParameterTree.of(Map.of()).toMap(signupValidator.description());
// {name="", age="", tags=[], address={city=""}}
```

* The parameters are a `Map<String, ?>`, in every form that `readingParameters()`
  validates: a `Map<String, String>`, a `Map<String, List<String>>` such as the
  `MultiValueMap` of Spring and the `Map<String, String[]>` of the Servlet API. The tree
  of a JSON library (`readingWith(reader)`) is not written back.
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
* A property that is defined with `withheld()`, such as a password
  (`._string("password", c -> c.greaterThanOrEqual(8)).withheld()`), is written as one
  that was not sent: an empty string. A template that writes every field of the map does
  not send the password back. Any other property is written with what was sent.

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

## Writing an object back as the map it is read from

A schema names the properties of an input once. `fromMapReversible` of
`Yavi.reversibleSchema()` builds a validator that also writes the created object back as
the map it is read from, so that the names are not written again for the parameters of a
statement or for a form that shows a stored object.

```java
record Email(String value) {}
record UserName(String first, String last) {}
record Employee(Integer id, UserName name, Email email, Optional<String> nickname, Role role) {}

ReversibleValueValidator<String, Email> emailValidator = Yavi._string("email", c -> c.notBlank().email())
        .map(Email::new, Email::value);

ObjectInputValidator.Reversible<UserName> userNameValidator = Yavi.reversibleSchema()
        ._string("first_name", c -> c.notBlank())
        ._string("last_name", c -> c.notBlank())
        .fromMapReversible(UserName::new, UserName::first, UserName::last);

ObjectInputValidator.Reversible<Employee> employeeValidator = Yavi.reversibleSchema()
        ._integer("id")
        ._embedded(userNameValidator)
        ._string(emailValidator)
        ._string("nickname").optional()
        ._enum("role", Role.class)
        .fromMapReversible(Employee::new, Employee::id, Employee::name, Employee::email,
                Employee::nickname, Employee::role);

employeeValidator.validate(row); // reads, as fromMap does
employeeValidator.toMap(employee);
// {id=1, first_name=Taro, last_name=Yamada, email=taro@example.com, nickname=null, role=ADMIN}

jdbcClient.sql("INSERT INTO employee (id, first_name, last_name, email, nickname, role) "
        + "VALUES (:id, :first_name, :last_name, :email, :nickname, :role)")
        .params(employeeValidator.toMap(employee))
        .update();
```

* `Yavi.reversibleSchema()` is `Yavi.schema()` for an object that is written back. It has
  the same methods, but where `Yavi.schema()` takes a `ValueValidator`, it takes a
  `ReversibleValueValidator`: a validator that knows its reverse by its type. The other
  difference is `splitBy` of a pattern, which is given the text that joins the elements.
* `fromMapReversible(f, accessor1, ..., accessorN)` is `fromMap(f)` with one accessor per
  argument, which returns the validated value of that argument from the object. The
  compiler checks its type, so a record is given its constructor and its accessors.
  `flatFromMapReversible` is the same for `flatFromMap`.
* A schema of one property has `fromMap()` and `fromMapReversible()` without arguments:
  the validated value is the result, as with `get()`. One property is defined once in
  this way, with its constraints and modifiers, and embedded in several schemas
  (`_embedded(passwordSchema)`, `_embedded(passwordSchema).optional()`). A schema whose
  only property is the rest has them too: `Yavi.schema()._rest(Yavi._integer()).fromMap()`
  is a validator of a `Map<String, Integer>`.
* `toMap(x)` is defined by the round trip: `validator.validate(validator.toMap(x))` is
  `Valid(x)` for every `x` that the validator creates. A property that is defined with
  `withheld()` is not written by `toMap`; `reverse(x)` writes it and keeps the round trip
  (see [Writing some of the properties](#writing-some-of-the-properties)).
* The returned validator is an `ObjectInputValidator`: it is nested, embedded and read
  with `readingWith` and `readingParameters` as before, and stays `Reversible` after
  `rejectingUnknownProperties()` and `constraintOnTarget`.
* The map is new. Add what a statement needs: `map.put("updated_by", user)`.

| Property | `toMap` |
| --- | --- |
| a value of constraints, `defaultValue`, `recover` | the value |
| an enum constant | its name |
| no value of `nullable()`, `optional()` | an entry with `null`, because a named parameter needs its key |
| no value after `rejectingNull()` | no entry |
| `presence()` | no entry for `Absent`, `null` for `PresentNull` |
| `_embedded` | in the same map; `null` for every property if it has no value |
| `_object` | a nested map |
| `_list`, `_map`, `_rest` | every element |
| `splitBy(",")` | the joined text. An empty list is `null`, because text without an element is not sent: the property needs `defaultValue(List.of())` or `nullable()` to be read back |
| `splitBy(pattern, "\n")` | the text joined by the second argument, with the same rule for an empty list |
| a validator with `map(f, g)` | the result of `g` |

The values are those of the JDK types that the properties are read from, as a JDBC driver
and a JSON library take them. A `URI`, a `Year` and the like are written as they are;
convert what a driver does not take.

**A list in one text field.** A list with `splitBy` is one field of a form, such as a
`textarea` with one value per line, so it is written as the text of that field. A pattern
does not tell the text that it separates, so the text that joins the elements is given
with it:

```java
ObjectInputValidator.Reversible<Feed> feedSchema = Yavi.reversibleSchema()
        ._string("title")
        ._list("urls", Yavi._string(c -> c.url())).splitBy(Pattern.compile("\\s*\\R\\s*"), "\n")
        .defaultValue(List.of())
        .fromMapReversible(Feed::new, Feed::title, Feed::urls);

feedSchema.toMap(new Feed("News", List.of("https://a.example", "https://b.example")));
// {title=News, urls="https://a.example\nhttps://b.example"}
```

* `splitBy(pattern)` without the text does not compile in `Yavi.reversibleSchema()`: the
  list would be read from one text and written as a `List`, which a `textarea` shows as
  `[https://a.example, https://b.example]`. `Yavi.schema()` has it, as before.
* The pattern must match the text: `splitBy(Pattern.compile("\\R"), ", ")` fails with
  `IllegalArgumentException` when the schema is built, because the written text would not
  be split where it was joined. `splitBy(",")` is `splitBy(Pattern.compile(","), ",")`.
* An element whose text contains what the pattern matches is not read back as one
  element.

**A value object.** `map(f, g)` is `map(f)` with the function that goes back from the
mapped value to the input. It returns a `ReversibleValueValidator`, whose `reverse(x)` is
the input that `x` is read from. The type tells whether a validator knows its reverse:

| Validator | Type |
| --- | --- |
| `Yavi._string("name")` and the other validators of single values, `XxxValidatorBuilder#build()` | `StringValidator.Reversible` and the like, which are `ReversibleValueValidator`s |
| `map(f, g)`, `ReversibleValueValidator.of(validator, g)` | `ReversibleValueValidator` |
| `named`, `describedAs`, `preprocess`, `prefixed`, `formattingWith`, `constraintOnTarget`, `untrimmed()`, `ignoringCase()` of one | `ReversibleValueValidator` |
| `optional()`, `nullable()`, `defaultValue`, `recover`, `recoverWith` of one | `ReversibleValueValidator.NullAccepting`, which is also a `NullAcceptingValueValidator` |
| `liftList()`, `liftSet()`, `liftCollection(factory)`, `liftMap()`, `liftOptional()`, `liftPresence()` of one | `ReversibleValueValidator` of the container |
| `ReversibleValueValidator.lazy(supplier)`, `recursive(self -> ...)` | `ReversibleValueValidator` |
| `fromMapReversible`, a `DiscriminatedUnionValidator.Reversible` | `ReversibleValueValidator` of a map |
| `map(f)`, `andThen`, `anyOf`, a lambda, `Yavi._list`, `Yavi._map`, `fromMap` | `ValueValidator` |

A constant is declared with these types. `StringValidator.Reversible` and the other
validators of a single value take no type argument, because the validated value is the
input (`StringValidator<String>` before); `EnumValidator.Reversible<E>`,
`ObjectValidator.Reversible<T>` and `ObjectInputValidator.Reversible<T>` take the type of
the value:

```java
static final StringValidator.Reversible NAME = Yavi._string("name", c -> c.notBlank());
static final IntegerValidator.Reversible AGE = Yavi._integer("age", c -> c.positive());
static final EnumValidator.Reversible<Role> ROLE = Yavi._enum("role", Role.class);
static final ReversibleValueValidator<String, Email> EMAIL = Yavi._string("email").map(Email::new, Email::value);
```

**What cannot be written.** A validator that is not a `ReversibleValueValidator`: `map(f)`
without the reverse, a lambda, `andThen`, `anyOf`. A schema of `Yavi.reversibleSchema()`
does not take one, neither as a property nor in `_object`, `_embedded`, `_list`, `_map` or
`_rest`, so the property that cannot be written back does not compile:

```java
Yavi.reversibleSchema()
        ._string("name")
        ._string(Yavi._string("email").map(Email::new)) // does not compile: no reverse
        .fromMapReversible(Contact::new, Contact::name, Contact::email);
// error: no suitable method found for _string(StringValidator<Email>)

Yavi.reversibleSchema()
        ._string("name")
        ._string(Yavi._string("email").map(Email::new, Email::value)) // compiles
        .fromMapReversible(Contact::new, Contact::name, Contact::email);
```

`Yavi.schema()` has no `fromMapReversible`. A schema that is not written back is not
affected: it takes any validator, as before.

**A structure that contains itself.** `ReversibleValueValidator.recursive(self -> ...)`
is `ValueValidator.recursive` with a reference that knows its reverse:

```java
ObjectInputValidator.Reversible<Tree> treeValidator = ReversibleValueValidator.recursive(self -> Yavi.reversibleSchema()
        ._string("name")
        ._list("children", self).defaultValue(List.of())
        .fromMapReversible(Tree::new, Tree::name, Tree::children));
```

**A nested object.** A nested map cannot be bound to a statement. Read the columns of a
row with `_embedded`, or bind the map under the names of the violations with
`ParameterTree.parametersOf`:

```java
// {id=1, address={city=Tokyo, street=null}} -> {id=1, address.city=Tokyo, address.street=null}
jdbcClient.sql("INSERT INTO venue (id, city, street) VALUES (:id, :address.city, :address.street)")
        .params(ParameterTree.parametersOf(venueValidator.toMap(venue)))
        .update();
```

**A row.** The row that `listOfRows()` returns is read with the same validator. A
`java.sql.Date` is read as a `LocalDate`, a `java.sql.Time` as a `LocalTime` and a
`java.sql.Timestamp` as a `LocalDateTime` or an `Instant`, which is what a driver returns
for such a column.

A column that a driver holds as another type is given both directions with `map(f, g)`.
The driver of SQLite stores a `java.sql.Timestamp` as the milliseconds of the epoch and
returns a `Long` for it. `_instant("created_at")` reports such a row with `object.type`
and writes an `Instant`, which that driver stores as text, so that the table holds both.
A test that inserts its rows with the same validator does not find it. Read and write the
`Long`:

```java
._long(Yavi._long("created_at").map(Instant::ofEpochMilli, Instant::toEpochMilli))
```

**A discriminated union.** An alternative added with the type of its objects is written
back, with the discriminator. The union is `Reversible` if every alternative is added in
this way:

```java
DiscriminatedUnionValidator.Reversible<Shape> shapeValidator = DiscriminatedUnionValidator.<Shape>of("type")
        .when("circle", Circle.class, circleValidator)
        .when("square", Square.class, squareValidator);
shapeValidator.toMap(new Circle(1.5)); // {type=circle, radius=1.5}
```

**A schema whose properties are known at run time.** `Yavi.reversibleDynamicSchema()` is
`Yavi.dynamicSchema()` whose `property` and `rest` take a `ReversibleValueValidator`. Its
`build()` returns a validator that writes the map of validated values back.

**More properties than a schema supports.** `ObjectInputValidator.ofReversible` takes a
list of `ObjectInputValidator.ReversiblePart`: a part with its reverse and its accessor.

### Writing some of the properties

`toMap` writes every property that is not withheld. A list shows a few of them, an
`UPDATE` binds the columns that may change. `including` and `excluding` of the validator
build a `PropertyView` that writes the chosen properties:

```java
ObjectInputValidator.Reversible<User> userValidator = Yavi.reversibleSchema()
        ._integer("id")
        ._string("name")
        ._string("password_hash").withheld()
        ._object("address", addressValidator) // city, street
        .fromMapReversible(User::new, User::id, User::name, User::passwordHash, User::address);

// built once, next to the validator
static final PropertyView<User> SUMMARY = userValidator.including("id", "name", "address.city");
static final PropertyView<User> LISTED = userValidator.excluding("address");

userValidator.toMap(user); // {id=1, name=Taro, address={city=Tokyo, street=1-1}}
SUMMARY.toMap(user);       // {id=1, name=Taro, address={city=Tokyo}}
LISTED.toMap(user);        // {id=1, name=Taro}

userValidator.excluding("adress");
// IllegalArgumentException: The property 'adress' is not defined by the validator.
// The properties are [id, name, password_hash, address].
```

* **The names are checked when the view is built**, against what the validator describes,
  not when an object is written. A name that the validator does not define, that the view
  does not write, or that is given twice is an `IllegalArgumentException`. A view that is
  a constant next to its validator therefore fails when the class is loaded, on every
  path and in every test that uses the validator. Removing a key from the map that
  `toMap` returns (`map.remove("adress")`) is checked by nobody.
* **The view is chosen where the object is written.** The use case holds the view it
  needs and calls its `toMap`; the schema does not know the views.
* **A view only writes.** It is not a validator and not a `ReversibleValueValidator`: a
  map without a required property is not read back, so the compiler rejects a view where
  the round trip is needed, such as a property of `Yavi.reversibleSchema()`. What the
  validator reads does not change.
* A view is made narrower with its own `including` and `excluding`:
  `userValidator.including("id", "address").excluding("address.street")`.

A property is named as its violations are:

| Property | Name |
| --- | --- |
| of the validator | `name` |
| of a nested object (`_object`) | `address.city`; `address` stands for all of them |
| of the elements of `_list` and the values of `_map` | `addresses.city`, without an index or a key |
| of an `_embedded` validator | its own name, as it is in the same map |
| of a union | a name that an alternative defines; an alternative without it is written without it |

What is written:

* A property that is not in the view has **no entry**; it is not written as `null`. A
  view is for a statement with fewer parameters, not a way to bind `null`.
* The accessor of a property of the validator that is not written is not called, so a
  view can leave out what is expensive to get.
* The rest (`_rest`), whose names are not known when the view is built, is written by
  `excluding` and not by `including`.
* The discriminator of a union is always written, by the view of a union and for a union
  that is nested, and is neither included nor excluded. The discriminator of an
  `_embedded` union is a property of the object that embeds it and is named as any other.
* A path names the properties at its depth: `children.memo` of a validator that contains
  itself is the `memo` of the children, not of their children.
* `Yavi.reversibleDynamicSchema()` and `DiscriminatedUnionValidator.Reversible` build
  views in the same way, and `ParameterTree.ofValues(view.toMap(x))` shows a stored
  object in a form without its secret.

**A property that is withheld.** A map that is written is shown: in a response, in a
form, in a log. A property that must not be shown, such as a password, would have to be
left out by every caller, also by the callers of every validator that nests the schema.
It is therefore marked once, where it is defined, with `withheld()`:

```java
Yavi.reversibleSchema()
        ._integer("id")
        ._string("name")
        ._string("password_hash").withheld()
        .fromMapReversible(User::new, User::id, User::name, User::passwordHash);

userValidator.toMap(user);                                  // {id=1, name=Taro}
userValidator.excluding("name").toMap(user);                // {id=1}
userValidator.including("id", "password_hash").toMap(user); // {id=1, password_hash=...}
userValidator.reverse(user);                                // {id=1, name=Taro, password_hash=...}
orderValidator.toMap(order);                                // {id=7, memo=gift, user={id=1, name=Taro}}
orderValidator.including("user", "user.password_hash");     // the user, and its hash too
userValidator.excluding("password_hash");                   // IllegalArgumentException: no effect
```

* `toMap` and a view that excludes do not write a withheld property, at any depth: not
  that of a nested object, of the elements of a list or of an `_embedded` validator.
* `reverse` writes every property, so `validate(reverse(x))` is `Valid(x)` whatever is
  withheld. It is what a validator that nests the schema is written back with, and what
  an `INSERT` of a row with a stored secret binds.
* A view that includes writes it only if it is named. The name of a nested object stands
  for its properties that are not withheld; the path to a withheld one is given next to
  it.
* A property of a union is withheld if an alternative that defines it withholds it. The
  discriminator cannot be withheld.
* `withheld()` is written once, before the modifiers that decide what `null` produces
  and after `splitBy` of a list. `label` and `typeMessage` are written before it or after
  it (`.label("Password").withheld()`). `.withheld().withheld()`,
  `.optional().withheld()`, and `withheld()` of `_rest` or `_embedded` do not compile.
  `Yavi.schema()` and the dynamic schemas have it too.
* The same mark keeps what was sent out of a form that is shown again
  ([Writing the parameters back](#writing-the-parameters-back)) and is `writeOnly` in a
  JSON Schema. It does not change what is read, and a violation still holds the rejected
  value (`ConstraintViolation#violatedValue()`; a `ViolationDetail` does not).
* The name says what happens, not why: a value that is large or internal is withheld in
  the same way as a secret.

**A value that is stored in another form.** A password is read as it is sent and stored
as a hash, which the application computes: it is not a value that goes back, so it is not
part of what the validator writes. The schema of the form withholds the password, so
`toMap` leaves it out, and the statement is given the hash as one more parameter:

```java
// ._string("password", c -> c.greaterThanOrEqual(8)).withheld()
jdbcClient.sql("INSERT INTO users (id, name, password_hash) VALUES (:id, :name, :password_hash)")
        .params(signupValidator.toMap(signup)) // {id=1, name=Taro, address={...}}: no password
        .param("password_hash", passwordEncoder.encode(signup.password()))
        .update();
```

The properties that the form and the row share are defined once, as a validator that
both embed (`_embedded(profileValidator)`), and the stored user is shown in the form with
`ParameterTree.ofValues(userValidator.toMap(user)).toMap(editValidator.description())`:
the password field is empty. The whole flow is in the
[appendix](#appendix-a-signup-form-its-row-and-its-edit-form).

To keep the names in one place, share a constant between the schema and the view
(`._object(ADDRESS, addressValidator)` and `excluding(ADDRESS)`).

### Showing a stored object in a form

`ParameterTree.ofValues` takes the map that a validator writes and holds its values as
the text of parameters. The first display of a stored object and the form that is shown
again are then one code path:

```java
ObjectInputValidator.Reversible<Signup> signupSchema = Yavi.reversibleSchema()
        ._string("name", c -> c.notBlank())
        ._integer("age")
        ._boolean("agreed").defaultValue(false)
        ._localDate("birthday")
        ._object("address", addressValidator)
        .fromMapReversible(Signup::new, Signup::name, Signup::age, Signup::agreed, Signup::birthday,
                Signup::address);
ValueValidator<Map<String, ?>, Signup> signupValidator = signupSchema
        .readingParameters(StringCoercion.strict().trimming());

ParameterTree tree = stored != null ? ParameterTree.ofValues(signupSchema.toMap(stored))
        : ParameterTree.of(request.getParameterMap());
Map<String, Object> form = tree.toMap(signupSchema.description());
// {name="Taro", age="30", birthday="2000-01-02", address={city="Tokyo", street=""}}
```

* A value is the text that is read as it (`StringCoercion.textOf`): `30` is `"30"`, a
  `LocalDate` its ISO-8601 text. `signupValidator` reads `tree.toParameters(...)` as the
  stored object.
* A property that is `null` or `false` is not sent, like an empty field and a checkbox
  that is not checked. `toMap(description)` writes it as it writes a property that was
  not sent: an empty string, or no entry for a `Boolean`.
* The validator that reads parameters is not `Reversible`, because what is written is a
  map. A form keeps the schema and the validator of its parameters, as above.
* A list with `splitBy` is written as its text, so a `textarea` shows the stored list as
  it shows what was sent.
* A new form is given its initial values as a map of some of the properties. The others
  are written as properties that were not sent:

  ```java
  ParameterTree.ofValues(Map.of("age", 20, "agreed", true)).toMap(signupSchema.description());
  // {name="", age="20", agreed="true", birthday="", address={city="", street=""}}
  ```

**A form whose type is not the stored type.** A form is read as
`ArticleInput(title, body)` and the stored object is `Article(id, title, body, createdAt)`
or a class of a library. The schema of the form writes an `ArticleInput`, so the
application creates one from the stored object:

```java
ParameterTree.ofValues(articleInputSchema.toMap(new ArticleInput(article.title(), article.body())))
        .toMap(articleInputSchema.description());
```

What the schema saves is the text of each value, `null` and the checkboxes, not the
mapping between the two types. The mapping is not written where the stored type holds
what the form shares with it, as a property that both schemas embed
(`_embedded(profileSchema)` in the
[appendix](#appendix-a-signup-form-its-row-and-its-edit-form)).

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

A failed result and a violation print as they read, in a log, in jshell and in the
message of a failed assertion: the pointer, the message and the message key.

```java
userValidator.validate(Map.of("email", "invalid", "age", 300)).toString();
// Invalid[errors=[/email: "email" must be a valid email address (charSequence.email),
//                 /age: "age" must be less than or equal to 150 (numeric.lessThanOrEqual)]]
Yavi._integer(c -> c.positive()).validate(-1).toString();
// Invalid[errors=[The value must be positive (numeric.positive)]]
```

The violated value is not printed unless the message shows it.

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
| `collection.unique`, `collection.uniqueBy` | `duplicates` |
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
* A property that is defined with `withheld()` is `writeOnly`
  (`ValueDescription.Property#withheld()`).
* The constraints of a list or map are written as `minItems`, `uniqueItems` and so on,
  `rejectingUnknownProperties()` as `additionalProperties: false`, and a recursive
  structure as `$ref`.
* The alternatives of a `DiscriminatedUnionValidator` are an `AnyOf` whose
  `discriminator()` is the name of the property that selects one.
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
| `fromMapReversible`, `toMap`, `map(f, g)`, `ReversibleValueValidator`, `ParameterTree.ofValues`, `java.sql` dates of a row | [#578](https://github.com/making/yavi/pull/578) |
| `Yavi.reversibleSchema()`, `Yavi.reversibleDynamicSchema()`, `StringValidator.Reversible` and the like, `ReversibleValueValidator.NullAccepting`, `ReversibleArguments1Validator`, `ObjectInputValidator.ReversiblePart` | [#579](https://github.com/making/yavi/pull/579) |
| `PropertyView`, `including` and `excluding` of a reversible validator, `ValueDescription.AnyOf#discriminator` | [#580](https://github.com/making/yavi/pull/580) |
| `withheld()` of a property, `ValueDescription.Property#withheld`, `fromMap()` and `fromMapReversible()` of one property | [#581](https://github.com/making/yavi/pull/581), [#582](https://github.com/making/yavi/pull/582) |
| `object.required` for an empty text of a property, `InputReader#isEmptyText` | [#570](https://github.com/making/yavi/pull/570) |
| `named`, `splitBy`, `liftList(separator)`, `Yavi.dynamicSchema()` | [#572](https://github.com/making/yavi/pull/572) |
| `splitBy(pattern, delimiter)` of a reversible schema, `label` and `typeMessage` before `withheld()` and `splitBy` | [#583](https://github.com/making/yavi/pull/583) |
| `unique(key)` of a collection, `ResourceBundleMessageFormatter`, `toString()` of a violation | [#584](https://github.com/making/yavi/pull/584) |
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

## Appendix: a signup form, its row and its edit form

A form sends a password, which is checked and never shown again; the table holds its
hash, which is computed outside the validator; and the stored account is shown in an edit
form with an empty password field. The properties are defined once and embedded.

```java
record Profile(String name, String email) {}
record Signup(Profile profile, String password) {}
record Account(Integer id, Profile profile, String passwordHash) {}
record AccountEdit(Profile profile, Optional<String> password) {}

// what the forms and the row share, defined once
static final ObjectInputValidator.Reversible<Profile> profileSchema = Yavi.reversibleSchema()
        ._string("name", c -> c.notBlank().lessThanOrEqual(50))
        ._string("email", c -> c.notBlank().email())
        .fromMapReversible(Profile::new, Profile::name, Profile::email);

// the password as it is sent: its constraints and withheld(), defined once
static final ObjectInputValidator.Reversible<String> passwordSchema = Yavi.reversibleSchema()
        ._string("password", c -> c.greaterThanOrEqual(8)).withheld()
        .fromMapReversible();

// the signup form
static final ObjectInputValidator.Reversible<Signup> signupSchema = Yavi.reversibleSchema()
        ._embedded(profileSchema)
        ._embedded(passwordSchema)
        .fromMapReversible(Signup::new, Signup::profile, Signup::password);

// the row
static final ObjectInputValidator.Reversible<Account> accountSchema = Yavi.reversibleSchema()
        ._integer("id")
        ._embedded(profileSchema)
        ._string("password_hash").withheld()
        .fromMapReversible(Account::new, Account::id, Account::profile, Account::passwordHash);

// the edit form: an empty password leaves it unchanged
static final ObjectInputValidator<AccountEdit> editSchema = Yavi.schema()
        ._embedded(profileSchema)
        ._embedded(passwordSchema).optional()
        .fromMap(AccountEdit::new);

static final ValueValidator<Map<String, ?>, Signup> signupForm = signupSchema.readingParameters();
static final ValueValidator<Map<String, ?>, AccountEdit> editForm = editSchema.readingParameters();
```

**Signing up.** A password that is too short is a violation, and the form that is shown
again does not hold it. A valid form is written with `toMap`, which leaves the password
out, and the statement is given the hash as one more parameter.

```java
Map<String, String> parameters = Map.of("name", "Taro", "email", "taro@example.com", "password", "short");
signupForm.validate(parameters).errors();
// The size of "password" must be greater than or equal to 8. The given size is 5
ParameterTree.of(parameters).toParameters(signupForm.description());
// {name=Taro, email=taro@example.com, password=}

parameters = Map.of("name", "Taro", "email", "taro@example.com", "password", "long-enough");
Signup signup = signupForm.validated(parameters);
signupSchema.toMap(signup); // {name=Taro, email=taro@example.com}
jdbcClient.sql("INSERT INTO account (id, name, email, password_hash) VALUES (:id, :name, :email, :password_hash)")
        .params(signupSchema.toMap(signup))
        .param("id", id)
        .param("password_hash", passwordEncoder.encode(signup.password()))
        .update();
```

**Reading the row and showing it.** The row is read as an `Account`. `toMap` is what a
response, a log or a form is given: it does not hold the hash.

```java
Map<String, Object> row = jdbcClient.sql("SELECT * FROM account WHERE id = :id").param("id", id).query().singleRow();
Account account = accountSchema.validated(row);
// Account[id=1, profile=Profile[name=Taro, email=taro@example.com], passwordHash={bcrypt}...]
accountSchema.toMap(account); // {id=1, name=Taro, email=taro@example.com}

// the edit form of the stored account
ParameterTree.ofValues(accountSchema.toMap(account)).toParameters(editForm.description());
// {name=Taro, email=taro@example.com, password=}
```

**Editing.** A password that is left empty is `Optional.empty()`, so only the profile is
updated; one that is sent is checked like that of the signup form and its hash is stored.

```java
Map<String, String> parameters = Map.of("name", "Jiro", "email", "jiro@example.com", "password", "");
AccountEdit edit = editForm.validated(parameters);
// AccountEdit[profile=Profile[name=Jiro, email=jiro@example.com], password=Optional.empty]
jdbcClient.sql("UPDATE account SET name = :name, email = :email WHERE id = :id")
        .params(profileSchema.toMap(edit.profile()))
        .param("id", account.id())
        .update();
edit.password().ifPresent(password ->
        jdbcClient.sql("UPDATE account SET password_hash = :password_hash WHERE id = :id")
                .param("password_hash", passwordEncoder.encode(password))
                .param("id", account.id())
                .update());
```

**The whole row.** Where the hash is needed, it is asked for: `reverse` writes every
property and is read back, and a view writes the withheld property that it names.

```java
accountSchema.reverse(account);
// {id=1, name=Taro, email=taro@example.com, password_hash={bcrypt}...}
accountSchema.validate(accountSchema.reverse(account)); // Valid(account)
accountSchema.including("id", "password_hash").toMap(account);
// {id=1, password_hash={bcrypt}...}
```

* `name` and `email` are defined in `profileSchema` only, and the constraints of the
  password and its `withheld()` in `passwordSchema` only, a schema of one property whose
  value is the result (`fromMapReversible()` without arguments). The mark of an embedded
  property is kept, also by `optional()`.
* The hash is not a value that goes back to the password, so it is passed by the
  application (`.param("password_hash", ...)`).
* A record that holds the embedded object (`Signup(Profile profile, String password)`) is
  given its constructor and its accessors. A flat record (`Signup(String name, String
  email, String password)`) works too, with a function that takes the profile apart and
  an accessor that puts it together again.
