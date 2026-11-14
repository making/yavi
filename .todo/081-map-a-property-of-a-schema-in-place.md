# Map a property of a schema in place

Difficulty: Medium

## Problem

Found by writing the Raoh tutorial with YAVI (sections 4 and 20 of
`docs/RaohTutorialInYavi.md`). A property that is mapped to a value object in place
names the type twice and nests a validator in the method of the same name:

```java
._string(Yavi._string("email", c -> c.notBlank().email()).map(Email::new))
```

Raoh writes `field("email", string().email().map(Email::new))`. The other things said
about a property are modifiers after it (`optional()`, `defaultValue`, `label`,
`withheld()`); mapping is not.

## Proposal

`map(f)` as a modifier of the last property, before the modifiers that decide what
`null` produces:

```java
._string("email", c -> c.notBlank().email()).map(Email::new)
._string("nickname").map(Nickname::new).optional()   // Optional<Nickname>
```

In `Yavi.reversibleSchema()` the modifier is `map(f, g)` only, so a property that cannot
be written back still does not compile.

## To decide

* The staged types: where `map` may be written relative to `splitBy`, `label`,
  `typeMessage` and `withheld()`, and that it is not offered after `optional()` and the
  like.
* The builders are generated (`scripts/generate-args.sh`); the modifier changes the type
  argument of the last property as `optional()` does.
* `Yavi.dynamicSchema()` takes a validator per property and needs nothing.
