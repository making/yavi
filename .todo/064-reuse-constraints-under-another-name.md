# Reuse the constraints of a value under another name

Difficulty: Low

## Problem

The validator of a single value holds its name, so the rule of a domain value can be
shared only under one name:

```java
final class Email {
    static final StringValidator<String> VALIDATOR = Yavi._string("email", c -> c.trim().notBlank().email());
}

Yavi.schema()
    ._string(Email.VALIDATOR)                 // the key and the name are "email"
    ._string(Email.VALIDATOR /* as "backupEmail"? */)
```

The alternative is to share the function of the constraints, whose type exposes the
internals of the builder:

```java
static final Function<? super StringValueConstraint<Arguments1<String>>,
        ? extends CharSequenceConstraint<Arguments1<String>, String>> EMAIL = c -> c.trim().notBlank().email();

Yavi.schema()._string("email", EMAIL)._string("backupEmail", EMAIL)
```

The same type is needed to build constraints at run time (a list of rules from
configuration folded over `CharSequenceConstraint<Arguments1<String>, String>`).

## First step: decide the shape

1. A validator can be renamed: `Email.VALIDATOR.named("backupEmail")`, and the schema
   takes a name with a validator: `._string("backupEmail", Email.VALIDATOR)`. Decide
   what it does to a validator that is not built from constraints (`map`, `andThen`,
   `anyOf`) and to the names of nested violations.
2. A short type for the function of the constraints of each kind of value, for example
   `StringConstraints`, an interface that extends the `Function` above. Source
   compatible for lambdas; check the overloads of `Yavi` and `YaviSchema` for ambiguity.

## If accepted

* Extend an existing test of `Yavi`; no new test class.
* Document it in `README.md`.
