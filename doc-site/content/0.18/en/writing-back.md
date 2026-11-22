# Writing an Object Back

A schema names the properties of an input once. `Yavi.reversibleSchema()` builds a validator that also writes the created object back as the map it is read from, so the names are not written again for the parameters of a statement or for a form that shows a stored object.

```java
record Employee(Integer id, UserName name, Email email, Optional<String> nickname, Role role) {}

ObjectInputValidator.Reversible<Employee> employeeValidator = Yavi.reversibleSchema()
        ._integer("id")
        ._embedded(userNameValidator) // first_name, last_name
        ._string(Yavi._string("email", c -> c.notBlank().email()).map(Email::new, Email::value))
        ._string("nickname").optional()
        ._enum("role", Role.class)
        .fromMapReversible(Employee::new, Employee::id, Employee::name, Employee::email,
                Employee::nickname, Employee::role);

employeeValidator.toMap(employee);
// {id=1, first_name=Taro, last_name=Yamada, email=taro@example.com, nickname=null, role=ADMIN}
```

The following complete program builds a reversible schema, creates an object, and prints the map that `toMap` writes back and validates again.

```java runnable
import java.util.Optional;
import java.util.TreeMap;

import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.validator.Yavi;

public class Main {

    record User(String name, String email, Optional<String> nickname) {
    }

    static final ObjectInputValidator.Reversible<User> validator = Yavi.reversibleSchema()
            ._string("name", c -> c.notBlank())
            ._string("email", c -> c.notBlank().email())
            ._string("nickname").optional()
            .fromMapReversible(User::new, User::name, User::email, User::nickname);

    public static void main(String[] args) {
        User user = new User("Taro", "taro@example.com", Optional.empty());
        var map = new TreeMap<>(validator.toMap(user));
        System.out.println(map);
        System.out.println("round trip valid: " + validator.validate(map).isValid());
    }

}
```
<!-- run: expected output
{email=taro@example.com, name=Taro, nickname=null}
round trip valid: true
-->

## Reversible schemas

`Yavi.reversibleSchema()` has the same methods as `Yavi.schema()`, but takes a `ReversibleValueValidator` where a schema takes a `ValueValidator`: a validator that knows its reverse by its type. A validator that maps its value without saying how to go back does not compile where it is added.

* `fromMapReversible(f, accessor1, ..., accessorN)` is `fromMap(f)` with one accessor per argument, which returns the validated value of that argument from the object.
* `map(f, g)` is `map(f)` with the function that goes from the mapped value back to the input. It returns a `ReversibleValueValidator`, whose `reverse(x)` is the input that `x` is read from.
* `flatFromMapReversible` and `flatOfReversible` are the counterparts of `flatFromMap` and `of`.
* `ReversibleValueValidator.recursive(self -> ...)` is `recursive` for a structure that contains itself.
* `Yavi.reversibleDynamicSchema()` writes back the map of validated values.
* `ObjectInputValidator.ofReversible(parts, mapper)` takes a list of `ReversiblePart`: a part with its reverse and its accessor.

`toMap(x)` writes the object; `reverse(x)` writes every property, including withheld ones. The validator reads input as `fromMap` does, and is nested, embedded and read with `readingWith` and `readingParameters` as before.

## Writing nested and optional values

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
| `splitBy(",")` | the joined text; an empty list is `null` |
| a validator with `map(f, g)` | the result of `g` |
| `withheld()` | no entry |

A list with `splitBy` is one field of a form, so it is written as the text of that field. A pattern does not tell the text that separates the elements, so the text that joins them is given with it:

```java
ObjectInputValidator.Reversible<Feed> feedSchema = Yavi.reversibleSchema()
        ._string("title")
        ._list("urls", Yavi._string(c -> c.url())).splitBy(Pattern.compile("\\s*\\R\\s*"), "\n")
        .defaultValue(List.of())
        .fromMapReversible(Feed::new, Feed::title, Feed::urls);

feedSchema.toMap(new Feed("News", List.of("https://a.example", "https://b.example")));
// {title=News, urls="https://a.example\nhttps://b.example"}
```

`splitBy(pattern)` without the joining text does not compile in `Yavi.reversibleSchema()`. The pattern must match the text: `splitBy(Pattern.compile("\\R"), ", ")` fails when the schema is built, because the written text would not be split where it was joined.

## Views

`including` and `excluding` build a `PropertyView` that writes the chosen properties. A view is built once, and its names are checked against what the validator describes when it is built: a name that the validator does not define is an `IllegalArgumentException`, where a key removed from the map that `toMap` returns is checked by nobody.

```java
static final PropertyView<AccountUser> SUMMARY = userValidator.including("id", "name", "address.city");
static final PropertyView<AccountUser> LISTED = userValidator.excluding("address");

SUMMARY.toMap(user); // {id=1, name=Taro, address={city=Tokyo}}
LISTED.toMap(user);  // {id=1, name=Taro}
```

* A property that is not in the view has no entry; it is not written as `null`. The accessor of a property that is not written is not called.
* A view only writes. It is not a validator and not a `ReversibleValueValidator`, so it is rejected where the round trip is needed.
* A view is narrowed with its own `including` and `excluding`.
* `renaming(function)` writes a property under another key, at every depth; `renaming(path, key)` renames one property. Only the key changes. A renamed view is for a response: it is not read back and not described by the JSON Schema.

## Withheld properties

A property that must not be shown is marked where it is defined:

```java
._string("password_hash").withheld()
```

`toMap` and a view that excludes do not write it, at any depth. A view that includes writes it only if it is named. `reverse` writes every property, so the round trip holds. The same mark keeps what was sent for a password out of a form that is shown again, and makes the property `writeOnly` in a JSON Schema. It does not change what is read, and a violation still holds the rejected value.

A password is often stored in another form, as a hash the application computes. The schema of the form withholds it, and the statement is given the hash as one more parameter.

## Parameters and Forms

`ParameterTree` returns parameters in the shape of the schema, and `ParameterTree.ofValues` holds the values that a validator writes as the text of parameters. The first display of a stored object and a form that is shown again are then one code path.

```java
ParameterTree tree = stored != null ? ParameterTree.ofValues(signupSchema.toMap(stored))
        : ParameterTree.of(request.getParameterMap());
Map<String, Object> form = tree.toMap(signupSchema.description());
// {name="Taro", age="30", birthday="2000-01-02", address={city="Tokyo", street=""}}
```

* `ParameterTree.of(parameters)` takes a `Map<String, String>`, a `Map<String, List<String>>` or a `Map<String, String[]>`, in the forms that `readingParameters()` validates.
* `toMap(description)` returns a tree of `Map`, `List` and the text that was sent, for a template engine. `toParameters(description)` returns the same values under the names of the violations (`address.city`, `items[0].name`), for a `MapBindingResult` of Spring.
* A value is not trimmed, coerced or validated. A value that was not sent is an empty string; a `Boolean` that was not sent has no entry.
* A key that no property is defined for is left out. A schema with `_rest` returns the other keys.
* A withheld property is written as one that was not sent.
* `ParameterTree.parametersOf(tree)` is the reverse of `ParameterTree.of(parameters)` for any tree of `Map` and `List`.
* `ParameterTree.of(parameters, maxIndex)` gives another index limit; `ParameterTree.DEFAULT_MAX_INDEX` is 255.

## JSON trees

`toMap` holds the values of the JDK types that the properties are read from, such as a `LocalDate`, so the tree that a JSON library makes of the map depends on how the library is configured. An `OutputWriter` (since 0.18.1), the counterpart of an `InputReader`, writes the map as the tree model of a JSON library, in the form that its reader reads back:

```java
JsonNode node = JsonNodeWriter.INSTANCE.write(employeeValidator.toMap(employee));
// {"id":1,"first_name":"Taro","last_name":"Yamada","email":"taro@example.com","nickname":null,"role":"ADMIN"}

employeeValidator.readingWith(JsonNodeReader.INSTANCE).validate(node); // Valid(employee)
```

* A writer implements `nullNode()`, `scalar(value)`, `object(properties)` and `array(elements)` for its tree model. [Jackson](integrations-jackson.md#jsonnodewriter) shows a complete writer for its `JsonNode`.
* `scalar` is given a `String`, a `Boolean` or a `Number`. An enum constant is written as its name and a `Year` as its number. A `LocalDate`, `LocalTime`, `LocalDateTime`, `OffsetDateTime`, `ZonedDateTime`, `Instant`, `YearMonth`, `Duration`, `Period`, `UUID` and `URI` are written as the text that the validator parses. Any other value is an `IllegalArgumentException`.
* For a tree model that has a node of its own for a type, override `value(Object)` and call `OutputWriter.super.value(value)` for the rest.
* `write` takes any tree of `Map`, `Iterable` and values, so the map of a view (`SUMMARY.toMap(user)`) and of `reverse` is written in the same way.

## Round trip

`validator.validate(validator.toMap(x))` is `Valid(x)` for every `x` that the validator creates, and so is `validator.readingWith(reader).validate(writer.write(validator.toMap(x)))` for a reader and a writer of the same tree model. `reverse` keeps the round trip for a withheld property, because it writes it. A validator that is not a `ReversibleValueValidator`, such as `map(f)` without a reverse, a lambda or `anyOf`, is rejected by the schema at compile time.

The map is new; add what a statement needs (`map.put("updated_by", user)`). A nested map cannot be bound to a statement: read the columns of a row with `_embedded`, or bind the map under the names of the violations with `ParameterTree.parametersOf`.
