# Switch copyright headers to SPDX

Difficulty: Low

Blocks: 004-016 (every file header changes; doing it first keeps the javadoc todos free of header conflicts).

## Goal

Replace the 13-line Apache license header in every source file with:

```
/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
```

## Scope

- `src/etc/header.txt` (template used by `license-maven-plugin`, `strictCheck=true`); check whether `src/etc/header-definition.xml` still fits.
- `pom.xml` `license-maven-plugin` `<includes>`: add `src/**/*.kt` so Kotlin files are enforced too.
- All `src/main` and `src/test` `.java` / `.kt` files. Current headers have mixed years (2018-2022 in Kotlin files, 2018-2024 in `fn` outputs, 2018-2025 elsewhere); unify.
- Header heredocs in `scripts/generate-function.sh`, `scripts/generate-applicative.sh` (currently 2018-2024, out of sync with the committed outputs), `scripts/generate-args.sh`.
- `src/main/java/am/ik/yavi/jsr305/*.java` are copied from Spring (excluded from the license check): convert to `SPDX-FileCopyrightText: 2002-2018 the original author or authors` + `SPDX-License-Identifier: Apache-2.0`, keeping the original holder.
- Check `src/etc/` IDE settings and `CONTRIBUTING.md` for header templates that need the same change.

## Verification

- `./mvnw license:check` passes (needs network for the plugin's first run).
- Running all three generator scripts followed by `./mvnw spring-javaformat:apply` leaves `git status` clean.
- `./mvnw -q test` passes.
