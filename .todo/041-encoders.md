# Write a domain object back as the map that its validator reads

Difficulty: High

Whether this goes into 0.18.0 is not decided. Every API below is an addition, so nothing
is lost by releasing it later.

## Problem

Sections 29-32 of the Raoh tutorial are encoders, the reverse of decoders:
`object(property("id", Item::id, long_().contramap(ItemId::value)), ...)` produces a
`Map<String, Object>` for JDBC binding or JSON output. Zod 4 has codecs (`z.codec`,
`z.encode`). YAVI has nothing; `docs/ComparisonWithRaoh.md` lists it under
"Not available".

The main use is the parameters of a statement of `JdbcClient` of Spring JDBC. The second
is the first display of an edit form, which needs the text of each field of a stored
object under the names of the schema, as `ParameterTree#toMap` returns it for a form that
is shown again.

Without it the application writes the names of the schema a third time, for every
statement and every form:

```java
Map<String, Object> params = Map.of(
        "id", employee.id(),
        "first_name", employee.name().first(),
        "lastname", employee.name().last(),   // the schema says "last_name": fails only when the statement runs
        "email", employee.email(),            // an Email, not a String: the driver rejects it
        "nickname", employee.nickname());      // an Optional; a null value throws NullPointerException in Map.of
```

* A key that is renamed in the schema fails no compilation and no test.
* An `Optional`, a value object and the columns of a nested record are converted by hand.

## Scope

Only what a schema reads is written back. The definition is the round trip:
`validator.validate(validator.toMap(x))` is `Valid(x)` for every `x` that the validator
produces. This is what keeps it inside a validation library.

## Vocabulary

No type is called an encoder or a codec, and `ValueValidator` is not renamed. A validator
reads an input (`fromMap`, `readingWith`, `readingParameters`, `InputReader`), and the
reverse is already called writing (`ParameterTree#toMap`, `toParameters`, "Writing the
parameters back"). A validator that is `Writable` writes what it reads. The comparison
with Raoh says that `toMap` is what Raoh calls an encoder.

## API

```java
ObjectInputValidator.Writable<UserName> userNameValidator = Yavi.schema()
        ._string("first_name", c -> c.notBlank())
        ._string("last_name", c -> c.notBlank())
        .mapping(UserName::new, UserName::first, UserName::last);

ObjectInputValidator.Writable<Employee> employeeValidator = Yavi.schema()
        ._integer("id")
        ._embedded(userNameValidator)
        ._string(Email.VALIDATOR.map(Email::new, Email::value))
        ._string("nickname").optional()
        .mapping(Employee::new, Employee::id, Employee::name, Employee::email, Employee::nickname);

employeeValidator.validate(row);          // reads, as fromMap does
employeeValidator.toMap(employee);        // {id=1, first_name=Taro, last_name=Yamada, email=t@example.com, nickname=null}
employeeValidator.toParameters(employee); // {id="1", first_name="Taro", ..., nickname=""}

jdbcClient.sql("INSERT INTO employee (id, first_name, last_name, email, nickname) "
        + "VALUES (:id, :first_name, :last_name, :email, :nickname)")
        .params(employeeValidator.toMap(employee))
        .update();
```

| Reading (exists) | Writing (to add) |
| --- | --- |
| `fromMap(f)`, `flatFromMap(f)` | `mapping(f, accessor1..N)`, `flatMapping(f, accessor1..N)` |
| `validate(map)` | `toMap(x)`: a map of the JDK values as they are, for JDBC and Jackson |
| `readingParameters().validate(params)` | `toParameters(x)`: text under the names of the violations, for a `MapBindingResult` |
| `ObjectInputValidator<X>` | `ObjectInputValidator.Writable<X>` |
| `map(f)` | `map(f, g)` |
| `when(value, validator)` of a union | `when(value, type, writableValidator)` |

* `mapping` is one generated method per arity. Accessor `i` is a `Function<X, Ri>`, and
  `Ri` is the type of the constructor argument, so a record is written with method
  references and the types are checked by the compiler. No overload of a property is
  added.
* `Writable` is a subtype of `ObjectInputValidator`, so it nests, is embedded and reads
  with `readingWith` and `readingParameters` as before. It stays `Writable` after
  `rejectingUnknownProperties()` and `constraintOnTarget`.
* A union selects the alternative by the type of the object and adds the discriminator.
  It is `Writable` by its staged type only if every alternative is added with the type.
* The tree of text that Mustache wants needs no method: it should be
  `ParameterTree.of(validator.toParameters(x)).toMap(validator.description())`. Verify.

## What is written

| Property | `toMap` | `toParameters` |
| --- | --- | --- |
| a value of constraints, `defaultValue`, `recover` | the value | text, the reverse of `StringCoercion.strict()` |
| no value of `nullable()`, `optional()` | an entry with `null` | an empty string; no entry for a `Boolean` |
| no value after `rejectingNull()` | no entry | no entry |
| `presence()` | no entry for `Absent`, `null` for `PresentNull` | the same |
| `_embedded` | in the same map | the same |
| `_object` | a nested map | `address.city` |
| `_list`, `_map`, `_rest` | every element | the list of `tags`, `items[0].name` |
| `splitBy(",")` | the joined text | the same |
| `splitBy(Pattern)` | the list | the same |
| `map(f, g)` | the result of `g` | the same |

An entry with `null` is kept because a named parameter needs its key.

## What cannot be written

`map(f)` without the reverse, a lambda, `andThen`, `anyOf`, and `_object` or `_embedded`
with a validator that is not `Writable`. `mapping(...)` fails with an
`IllegalStateException` that names the property, at the time and in the way `fromMap`
fails for an argument without a name or with an unknown input type.

Rejecting it by type would need a second hierarchy of builders, which doubles the
generated code. The alternative that the compiler checks entirely is an accessor that
returns the input type `Ai`, but then the application writes
`u -> u.nickname().orElse(null)` and the like by hand. Not chosen.

## Packages

No new package. The dependencies stay `core <- input <- builder <- validator`, without a
cycle.

* `am.ik.yavi.input`: `ObjectInputValidator.Writable` and the writing side of the union.
  `ObjectInputValidator` is sealed and there is no `module-info`, so a subclass is in the
  same package.
* `am.ik.yavi.core`: the reverse of a single value (`map(f, g)`) and the rules of text.
* `am.ik.yavi.builder`: the generated `mapping` and the reverse in `ArgumentDefinition`.

## Phases

1. Everything above. Add Spring JDBC in the test scope and run `JdbcClient` with an enum,
   `null` and a row of `_embedded`.
2. The writing side of `Yavi.dynamicSchema()`, and an `OutputWriter<N>` that writes the
   tree of a JSON library, the counterpart of `InputReader<N>`.

## To verify while implementing

* An enum: `toMap` returns the constant. If Spring does not bind it by its name, add a
  target that writes an enum, a `URI` and the like as text.
* Reading a row: `listOfRows()` returns a `java.sql.Date` or a `Timestamp` for a date
  column. Check that `_localDate` and the like read it, for the round trip through a
  database.
* A `Boolean` as text is `"true"` or `"false"`, which is not the `on` of
  `booleanWords()`.
* A nested map cannot be bound, so the schema of a row uses `_embedded`. Whether
  `:address.city` is parsed as a named parameter is not known.

## When done

Add the feature to `docs/NewFeatures_0.18.0.md` (or the file of the release it goes
into), and update "Not available" of `docs/ComparisonWithRaoh.md` and
`docs/ComparisonWithZod.md`. The pull request shows the harm of not having it with the
example above.

If rejected, record the decision in `docs/ComparisonWithRaoh.md` ("Not available") and
`docs/ComparisonWithZod.md`, and close this item as cancelled.
