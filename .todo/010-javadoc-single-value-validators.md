# Javadoc: typed single-value validators and their builders

Difficulty: Low

Depends on: 003. Conventions: `.todo/javadoc-conventions.md`.

19 hand-written validator/builder pairs with identical structure. Write one pair carefully (`StringValidator` / `StringValidatorBuilder`), then apply the same shape to the rest.

## Owned

- `M/arguments/{BigDecimal,BigInteger,Boolean,Double,Enum,Float,Instant,Integer,LocalDateTime,LocalDate,LocalTime,Long,Object,OffsetDateTime,Short,String,YearMonth,Year,ZonedDateTime}Validator.java`
- `M/builder/{BigDecimal,BigInteger,Boolean,Double,Enum,Float,Instant,Integer,LocalDateTime,LocalDate,LocalTime,Long,Object,OffsetDateTime,Short,String,YearMonth,Year,ZonedDateTime}ValidatorBuilder.java`

Doclint regex: `am/ik/yavi/(arguments|builder)/(BigDecimal|BigInteger|Boolean|Double|Enum|Float|Instant|Integer|LocalDateTime|LocalDate|LocalTime|Long|Object|OffsetDateTime|Short|String|YearMonth|Year|ZonedDateTime)Validator(Builder)?\.java`

## Reference (read-only)

- `M/arguments/DefaultArguments1Validator.java`, `Arguments1Validator.java`, `Arguments1.java` (generated superclasses; owned by 011)
- `M/builder/ValidatorBuilder.java`, `ValueValidatorBuilder.java`, `YaviArguments.java`
- `M/constraint/*Constraint.java` (constraint type each builder exposes)
- `M/core/ValueValidator.java`, `Validated.java`
- Tests: `T/arguments/*ValidatorTest.java`, `T/arguments/RangeValidatorTest.java`
