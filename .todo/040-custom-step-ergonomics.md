# Make a hand-written validation step short to write

Difficulty: Medium

Found while writing the Raoh tutorial with YAVI (`docs/ComparisonWithRaoh.md`). Raoh
writes a custom step as `Result.fail("code", "message")` inside `flatMap`. The YAVI
counterparts are longer than they need to be:

1. **Creating a violation.** A lambda validator that fails needs

   ```java
   Validated.failureWith(ConstraintViolation.builder()
       .name("").messageKey("json.invalid").defaultMessageFormat("\"{0}\" cannot be parsed as JSON")
       .argsWithPrependedName().locale(locale).build());
   ```

   Wanted: a short factory that takes the `ViolationMessage`, the violated value and the
   locale, e.g. `Validated.failureWith(ViolationMessage.of(key, format), value, locale)`,
   with the empty name so that the enclosing argument names it.
2. **`flatMap` with the validated arguments.** A cross-field step that creates the result
   itself (`DateRange.parse(start, end)` returning a `Validated`) needs an intermediate
   record or `fromMap(Arguments::of)` followed by `andThen`. Wanted on the builders next
   to `fromMap(f)` / `apply(f)`: a variant whose function returns `Validated<X>`.
3. **`Validated#map` / `flatMap` as an argument.** They are inherited from `Validation`
   with a result type inferred from the target (`<T2, V extends Validation<E, T2>> V`), so
   `show(validated.map(f))` does not compile without assigning to a `Validated<T2>` first.
   Override them in `Validated` to return `Validated<T2>` if binary compatibility allows.
4. **`ArgumentsN` in a `switch`.** `fromMap(Arguments::of)` is the counterpart of Raoh's
   `Tuple2..8`, but `Arguments2` is a class, so
   `case Validated.Valid(Arguments2(var name, var age))` is not possible. The baseline is
   Java 21; evaluate turning the generated `ArgumentsN` into records (their constructors
   are package-private today and `arg1()` accessors exist).
