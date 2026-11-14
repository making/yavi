# Small obstacles when a schema is made reversible

Difficulty: Low

## `withheld()` after `label`

```java
Yavi.schema()
        ._string("password", c -> c.greaterThanOrEqual(8)).label("Password").withheld()
// error: cannot find symbol: method withheld()
```

The order is documented, but the error does not tell it, and `label` before the other
modifiers is the usual order. Decide between accepting `withheld()` after `label` (still
once, still by type) and stating the order in the Javadoc of `label` and `withheld`.

## The type of a validator of a single value

`StringValidator<String>` becomes `StringValidator.Reversible`, without a type argument,
while `EnumValidator.Reversible<E>` and `ObjectInputValidator.Reversible<T>` have one. A
constant that is changed to the reversible type fails with "type Reversible does not
take parameters". List the types of the constants in the table of validator types in
`docs/NewFeatures_0.18.0.md`, with one declaration as an example:

```java
static final StringValidator.Reversible NAME = Yavi._string("name", c -> c.notBlank());
```
