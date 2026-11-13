# Decide whether `Yavi` takes a name without constraints

Difficulty: Low

## Problem

The validators of a single value in `Yavi` have three forms per type, and none of them
takes a name without constraints:

```java
Yavi._string()                       // no name, no constraints
Yavi._string(c -> c.notBlank())      // no name
Yavi._string("name", c -> ...)       // name and constraints
Yavi._string("name")                 // does not compile
Yavi._enum("role", Role.class)       // does not compile
Yavi._object("point", Point.class)   // does not compile
```

`Yavi.schema()` has the missing forms (`_string(name)`, `_enum(name, type)`,
`_object(name, type)`), and the documentation says that `Yavi._xxx` has the names of the
arguments of `Yavi.schema()`. The gap was found when
`Yavi._enum("role", Role.class).ignoringCase()` was written in `README.md`,
`docs/ComparisonWithRaoh.md`, `docs/ComparisonWithZod.md` and the description of #552
without compiling. Those documents now use `Yavi._enum("role", Role.class, c -> c)`.

`Yavi._list` and `Yavi._map` have the opposite gap: they always take constraints, so
the validator of a list without constraints is `elementValidator.liftList()` or
`Yavi._list(elementValidator, c -> c)`, while `Yavi.schema()` has
`_list(name, elementValidator)`. Decide `Yavi._list(elementValidator)` and
`Yavi._map(valueValidator)` together with the forms above.

## First step: decide whether to add them

Arguments for:

* The same call reads differently in the two entry points, and `c -> c` is noise. Three
  documents got it wrong, so users will too.
* A named validator without constraints is meaningful since 0.18.0: a value is required
  by default, and the name is what a modifier or a reader needs
  (`Yavi._enum("role", Role.class).ignoringCase()`, `Yavi._boolean("subscribed")`).

Arguments against:

* About 20 more static methods on `Yavi` (62 today), each with Javadoc, for something that
  `c -> c` already expresses.
* A named validator of a single value is mostly given to `Yavi.schema()`, where
  `._string("name")` exists. The case that needs the missing form is narrow: a modifier of
  the typed validator itself, which today is only `EnumValidator#ignoringCase()`. Adding
  only `_enum(name, type)` and `_object(name, type)` may be enough, but leaves the
  entry point irregular.

Check before deciding:

* `Yavi._string(null)` and the like become ambiguous between `_string(String)` and
  `_string(Function)`. Find out whether any caller passes `null`.
* `Yavi._object(String name)` cannot coexist with a useful meaning: without a type the
  validator cannot be read from a map. Decide whether it is left out.
* `Yavi` is written by hand, not by `scripts/generate-args.sh`.

## If accepted

* Add the overloads, delegating to the existing three-argument form with
  `Function.identity()`.
* Extend an existing test of `Yavi` to cover them; no new test class.
* Replace `c -> c` in `README.md`, `docs/NewFeatures_0.18.0.md`,
  `docs/ComparisonWithRaoh.md` and `docs/ComparisonWithZod.md`.

## If rejected

* State in the Javadoc of `Yavi` that a name is given together with constraints, with
  `c -> c` for none, and close this item as cancelled.
