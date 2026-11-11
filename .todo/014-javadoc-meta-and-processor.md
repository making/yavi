# Javadoc: meta annotations and the annotation processor

Difficulty: Low

Depends on: 003. Conventions: `.todo/javadoc-conventions.md`.

## Owned

- `M/meta/*.java` incl. `package-info.java` (`ConstraintTarget`, `ConstraintArguments`, `ConstraintMeta`, 20 `*ConstraintMeta` interfaces)
- `M/processor/*.java` incl. `package-info.java`

Doclint regex: `am/ik/yavi/(meta|processor)/[\w-]+\.java`

## Reference (read-only)

- `M/builder/ValidatorBuilder.java` (meta-based `constraint` overloads), `M/builder/ArgumentsValidatorBuilder.java`
- `src/main/resources/META-INF/services/` (processor registration)
- Tests: `T/meta/*`, `T/processor/*`
- `docs/AnnotationProcessor.md`
