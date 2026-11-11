# Javadoc rewrite: shared conventions

Shared by todos 004-016. Not a todo itself; 017 deletes it.

Path abbreviations used in the todos: `M/` = `src/main/java/am/ik/yavi/`, `K/` = `src/main/kotlin/am/ik/yavi/`, `T/` = `src/test/java/am/ik/yavi/`, `TK/` = `src/test/kotlin/am/ik/yavi/`.

## Ownership

- Each todo lists **Owned** files (write) and **Reference** files (read-only).
- Edit only Owned files. Never touch another todo's Owned files, even to fix a wrong or missing comment there. Record such findings in the commit message body instead.
- Change comments only. No signature, behavior, import or formatting changes other than what `spring-javaformat:apply` produces. Exception: generator scripts, where only the javadoc text inside the templates changes.
- The package's `package-info.java` belongs to exactly one todo (stated in that todo).

## Generated sources

These files are generated. Never edit them by hand; edit the template in the script, then regenerate:

| Script | Outputs |
| --- | --- |
| `scripts/generate-function.sh` | `M/fn/Function1..16` |
| `scripts/generate-applicative.sh` | `M/fn/Combining1..16`, `M/fn/Validations`, `M/fn/Functions` |
| `scripts/generate-args.sh` | `M/arguments/Arguments1..16`, `Arguments1..16Validator`, `DefaultArguments1..16Validator`, `Arguments2..16Splitting`, `Arguments2..16Combining`, `ArgumentsValidators`; `M/builder/ArgumentsValidatorBuilder`, `Arguments1..16ValidatorBuilder`, `YaviArguments` |

Regenerate: `bash scripts/<script>.sh && ./mvnw spring-javaformat:apply`. Afterwards, `git diff` of outputs must contain only javadoc changes. Templates are unquoted heredocs: escape `$` and backticks in comment text (`{@code}` is safe).

## Content

- English. Rewrite fully: verify every existing comment against the implementation, keep what is accurate, fix or drop the rest.
- Cover every public and protected type, constructor, method, field, enum constant, type parameter, and `package-info.java`. Pure overrides with unchanged contract need no comment (javadoc inherits it); document an override only when its contract differs (e.g. covariant return with different semantics).
- First sentence: a summary fragment in third person ("Returns ...", "Validates ..."). Then semantics a user needs: null handling (most constraints treat `null` as valid; see `NullAs`), the violation message id (e.g. `charSequence.notBlank`) and default message from `M/core/ViolationMessage.java`, and the meaning of message arguments `{0}`, `{1}`, ..., exceptions thrown, immutability / thread-safety, fail-fast behavior where relevant.
- Block tags: `@param` (incl. `@param <T>`), `@return`, `@throws` everywhere they apply. Keep existing `@author`; do not add new ones.
- `@since`: keep existing values. Add `@since` to types that lack it, using the first tag containing the file's add commit (`git log --diff-filter=A --format=%H -- <file> | tail -1` then `git tag --contains <sha> | sort -V | head -1`). Add to members only when equally certain. Never guess.
- Examples: `<pre>{@code ... }</pre>` on entry-point types and non-obvious APIs. Examples must use the real API (check the tests under `T/`).
- HTML: `<p>` starts a paragraph (no `</p>`, no `<p/>`); lists with `<ul><li>`. Link with `{@link}`; `{@code}` for literals. Do not link to non-public types.
- Formatting follows spring-javaformat (it removes the blank line before block tags). Run `./mvnw spring-javaformat:apply` before committing.
- Reference docs for terminology and intended usage: `README.md`, `docs/*.md`.

## Verification

1. `./mvnw -q compile` succeeds.
2. Doclint is clean for Owned files:
   ```
   ./mvnw -q dependency:build-classpath -Dmdep.outputFile=target/cp.txt
   javadoc -J-Duser.language=en -Xdoclint:all -Xmaxwarns 100000 -quiet -cp "$(cat target/cp.txt)" \
     -d target/jd-check -sourcepath src/main/java -subpackages am.ik.yavi 2>&1 | grep -E ': (warning|error):' | grep -E '<owned path regex>'
   ```
   Expected output: nothing. (`maven-javadoc-plugin` caps warnings at 100 and caches results, so use `javadoc` directly.)
3. Tests for the touched packages pass (`./mvnw -q test -Dtest='<pattern>'`), as a guard against accidental code changes.

## Git

- Work on `develop`. One or more commits containing only Owned files (plus, for generator todos, the regenerated outputs). Message style per `CONTRIBUTING.md`, e.g. `Rewrite javadoc of am.ik.yavi.core violation types.`
- Before push: `git pull --rebase`. Generator todos 011 and 012 share `scripts/generate-args.sh` in disjoint regions; after a rebase, regenerate and commit any output difference.
- Closing the todo: remove the todo file and append the row to `.todo/.history.md` as two commits. On a rebase conflict in `.todo/.history.md`, keep both rows.
