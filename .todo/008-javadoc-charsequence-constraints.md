# Javadoc: CharSequence, code point, emoji, URL, IP and password constraints

Difficulty: Medium

Depends on: 003. Conventions: `.todo/javadoc-conventions.md`.

Needs care: grapheme/emoji counting, variation selectors and normalization change what `size`/`lessThan` count; describe exactly what the implementation counts.

## Owned

- `M/constraint/CharSequenceConstraint.java`
- `M/constraint/charsequence/**/*.java` incl. the three `package-info.java` (`charsequence`, `codepoints`, `variant`)
- `M/constraint/inetaddress/*.java` incl. `package-info.java`
- `M/constraint/password/*.java` incl. `package-info.java`

Doclint regex: `am/ik/yavi/constraint/(CharSequenceConstraint|charsequence/[\w/-]+|inetaddress/[\w-]+|password/[\w-]+)\.java`

## Reference (read-only)

- `M/constraint/base/ContainerConstraintBase.java`, `ConstraintBase.java`
- `M/core/ViolationMessage.java`, `M/core/ConstraintPredicate.java`
- `M/builder/ValidatorBuilder.java` (`_string`, `_charSequence` entry points), `M/constraint/ObjectConstraint.java` (object password policies)
- Tests: `T/constraint/CharSequenceConstraintTest.java`, `T/constraint/charsequence/**`, `T/constraint/password/*`
