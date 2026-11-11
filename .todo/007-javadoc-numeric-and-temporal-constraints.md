# Javadoc: numeric and temporal constraints

Difficulty: Low

Depends on: 003. Conventions: `.todo/javadoc-conventions.md`.

## Owned

- Numeric: `M/constraint/BigDecimalConstraint.java`, `BigIntegerConstraint.java`, `ByteConstraint.java`, `ShortConstraint.java`, `IntegerConstraint.java`, `LongConstraint.java`, `FloatConstraint.java`, `DoubleConstraint.java`, `CharacterConstraint.java`
- Temporal: `M/constraint/InstantConstraint.java`, `LocalDateConstraint.java`, `LocalDateTimeConstraint.java`, `LocalTimeConstraint.java`, `OffsetDateTimeConstraint.java`, `ZonedDateTimeConstraint.java`, `YearConstraint.java`, `YearMonthConstraint.java`

Doclint regex: `am/ik/yavi/constraint/(BigDecimal|BigInteger|Byte|Short|Integer|Long|Float|Double|Character|Instant|LocalDate|LocalDateTime|LocalTime|OffsetDateTime|ZonedDateTime|Year|YearMonth)Constraint\.java`

## Reference (read-only)

- `M/constraint/base/NumericConstraintBase.java`, `TemporalConstraintBase.java`, `ChronoLocalDateConstraintBase.java`, `ChronoLocalDateTimeConstraintBase.java`, `ChronoZonedDateTimeConstraintBase.java`, `ConstraintBase.java` (inherited methods; owned by 006)
- `M/core/ViolationMessage.java`
- Tests: `T/constraint/*Constraint*Test.java` for the owned types
