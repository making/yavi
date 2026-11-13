# Decide whether `ip()` accepts an empty string like the other formats

Difficulty: Low

## Problem

The format constraints of `CharSequenceConstraint` disagree about an empty string:

| Constraint | `""` |
| --- | --- |
| `email()`, `url()`, `uri()`, `uuid()`, `ulid()` | valid |
| `ip()`, `ipv4()`, `ipv6()`, `luhn()` | not valid |

Each Javadoc says which, but an optional text property behaves differently depending on
its format:

```java
Yavi._string("homepage", c -> c.url()).validate("");  // valid
Yavi._string("proxy", c -> c.ip()).validate("");      // "proxy" must be a valid IP address
```

`ip()`, `uri()` and `ulid()` are new in 0.18.0 and were added together; `uri()` and
`ulid()` accept `""` and `ip()` does not, because it delegates to `ipv4()` and `ipv6()`.
Under `readingParameters()` the difference is hidden, since an empty string is `null`. It
shows when a map or a JSON tree holds `""`.

## First step: decide

* Whether `ip()` accepts `""` before 0.18.0 is released, as its siblings do.
* Whether `ipv4()`, `ipv6()` and `luhn()` follow in the same release. That changes the
  result of existing validators; list it in `docs/BreakingChanges_0.18.0.md`.

If nothing changes, add the table above to the Javadoc of `CharSequenceConstraint`.
