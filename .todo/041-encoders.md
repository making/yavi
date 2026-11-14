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
parameters back"). A validator that is `Reversible` writes what it reads: its validation
is gone back, which is the round trip. The comparison with Raoh says that `toMap` is what
Raoh calls an encoder.

## API

```java
ObjectInputValidator.Reversible<UserName> userNameValidator = Yavi.schema()
        ._string("first_name", c -> c.notBlank())
        ._string("last_name", c -> c.notBlank())
        .fromMapReversible(UserName::new, UserName::first, UserName::last);

ObjectInputValidator.Reversible<Employee> employeeValidator = Yavi.schema()
        ._integer("id")
        ._embedded(userNameValidator)
        ._string(Email.VALIDATOR.map(Email::new, Email::value))
        ._string("nickname").optional()
        .fromMapReversible(Employee::new, Employee::id, Employee::name, Employee::email,
                Employee::nickname);

employeeValidator.validate(row);          // reads, as fromMap does
employeeValidator.toMap(employee);        // {id=1, first_name=Taro, last_name=Yamada, email=t@example.com, nickname=null}

jdbcClient.sql("INSERT INTO employee (id, first_name, last_name, email, nickname) "
        + "VALUES (:id, :first_name, :last_name, :email, :nickname)")
        .params(employeeValidator.toMap(employee))
        .update();

// the first display of a stored object and the form that is shown again: one code path
ParameterTree tree = stored ? ParameterTree.ofValues(signupSchema.toMap(signup))
        : ParameterTree.of(params);
Map<String, Object> form = tree.toParameters(signupValidator.description());
```

| Reading (exists) | Writing (to add) |
| --- | --- |
| `fromMap(f)`, `flatFromMap(f)` | `fromMapReversible(f, accessor1..N)`, `flatFromMapReversible(f, accessor1..N)` |
| `validate(map)` | `toMap(x)`: a map of the JDK values as they are, for JDBC and Jackson |
| `ParameterTree.of(params)` | `ParameterTree.ofValues(validator.toMap(x))`: the values as text (the name is not decided) |
| `ObjectInputValidator<X>` | `ObjectInputValidator.Reversible<X>` |
| `map(f)` | `map(f, g)` |
| `when(value, validator)` of a union | `when(value, type, reversibleValidator)` |

* `fromMapReversible` is one generated method per arity. Accessor `i` is a
  `Function<X, Ri>`, and `Ri` is the type of the constructor argument, so a record is
  written with method references and the types are checked by the compiler. No overload
  of a property is added.
* `Reversible` is a subtype of `ObjectInputValidator`, so it nests, is embedded and reads
  with `readingWith` and `readingParameters` as before. It stays `Reversible` after
  `rejectingUnknownProperties()` and `constraintOnTarget`.
* A union selects the alternative by the type of the object and adds the discriminator.
  It is `Reversible` by its staged type only if every alternative is added with the type.
* `readingParameters()` returns a `ValueValidator<Map<String, ?>, X>`, which is not
  `Reversible`. A form keeps two fields, the schema and the validator of the parameters;
  a row of JDBC or jOOQ is read as a map and needs one:

  ```java
  static final ObjectInputValidator.Reversible<Signup> signupSchema = Yavi.schema()
          ._string("name", c -> c.notBlank())
          ._integer("age")
          .fromMapReversible(Signup::new, Signup::name, Signup::age);

  static final ValueValidator<Map<String, ?>, Signup> signupValidator = signupSchema
          .readingParameters(StringCoercion.strict().trimming());
  ```

  A type that reads parameters and is `Reversible` is one more type. Not chosen.
* `Reversible` is the name because "a validator that is writable" reads as one that is
  written to. A noun of its own (`ObjectMapping`) would be a wrapper that is not a
  validator, and `rejectingUnknownProperties()` would lose its staged types.
* `fromMapReversible` is `fromMap` with the accessors. The name is next to `fromMap` in
  a completion and says what is returned. An overload `fromMap(f, accessor1..N)` would
  report a wrong accessor as "no suitable method", and `mapping` does not tell that it
  is `fromMap`.
* `toMap(x)` is the only method that writes. A form is given the map through
  `ParameterTree`, see below.

## ParameterTree

`ParameterTree` stays. It reads the keys of parameters (`address.city`,
`items[0].name`), and it writes what was sent when there is no object: `age=abc` is in no
`Signup`. The two write different things, what was sent and what an object holds, in one
shape.

So the rules of the shape and of text are in `ParameterTree` only, and a validator has no
`toParameters(x)`:

* `ParameterTree.ofValues(tree)` takes a tree of values and holds them as text, the
  reverse of `StringCoercion.strict()`: `30` is `"30"`, a `LocalDate` its ISO-8601 text,
  an enum constant its name.
* `toMap(description)` and `toParameters(description)`, which exist, give the shape: an
  empty string for a single value that is absent, no entry for a `Boolean` that is, the
  names of the violations as keys. The first display and the form that is shown again
  cannot differ, and the tree that Mustache wants comes from the same call.
* `validator.toMap(x)` is a tree of values and `ParameterTree#toMap(description)` a tree
  of text. Both are the map that the validator reads; the same name is accepted.
* What `ParameterTree.of` does with a value that is not text is not checked. Decide
  whether `ofValues` is a method of its own or `of` takes values too.

## What is written

| Property | `toMap` |
| --- | --- |
| a value of constraints, `defaultValue`, `recover` | the value |
| no value of `nullable()`, `optional()` | an entry with `null` |
| no value after `rejectingNull()` | no entry |
| `presence()` | no entry for `Absent`, `null` for `PresentNull` |
| `_embedded` | in the same map |
| `_object` | a nested map |
| `_list`, `_map`, `_rest` | every element |
| `splitBy(",")` | the joined text |
| `splitBy(Pattern)` | the list |
| `map(f, g)` | the result of `g` |

An entry with `null` is kept because a named parameter needs its key.

## What cannot be written

`map(f)` without the reverse, a lambda, `andThen`, `anyOf`, and `_object` or `_embedded`
with a validator that is not `Reversible`. `fromMapReversible(...)` fails with an
`IllegalStateException` that names the property, at the time and in the way `fromMap`
fails for an argument without a name or with an unknown input type.

Rejecting it by type would need a second hierarchy of builders, which doubles the
generated code. The alternative that the compiler checks entirely is an accessor that
returns the input type `Ai`, but then the application writes
`u -> u.nickname().orElse(null)` and the like by hand. Not chosen.

## Packages

No new package. The dependencies stay `core <- input <- builder <- validator`, without a
cycle.

* `am.ik.yavi.input`: `ObjectInputValidator.Reversible` and the writing side of the union.
  `ObjectInputValidator` is sealed and there is no `module-info`, so a subclass is in the
  same package. `ParameterTree.ofValues` and the rules of text.
* `am.ik.yavi.core`: the reverse of a single value (`map(f, g)`).
* `am.ik.yavi.builder`: the generated `fromMapReversible` and the reverse in
  `ArgumentDefinition`.

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
