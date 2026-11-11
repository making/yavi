# Javadoc: factory, Yavi entry point, jsr305 annotations

Difficulty: Low

Depends on: 003. Conventions: `.todo/javadoc-conventions.md`.

## Owned

- `M/factory/*.java` incl. `package-info.java`
- `M/validator/Yavi.java`, `M/validator/package-info.java` (`Yavi` is the shortest entry point; give it a complete usage example)
- `M/jsr305/*.java`; add `M/jsr305/package-info.java` with a package comment (no `@NonNullApi` on it, matching the current setup)

Doclint regex: `am/ik/yavi/(factory|validator|jsr305)/[\w-]+\.java`

## Reference (read-only)

- `M/builder/ValidatorBuilder.java`, `YaviArguments.java`, `M/core/Validator.java`, `ErrorHandler.java`, `M/message/MessageFormatter.java`, `M/fn/Pair.java`
- Tests: `T/factory/*`, `T/core/BiConsumerTest.java`
- `README.md`
