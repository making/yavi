/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import am.ik.yavi.message.MessageFormatter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Validates an input of type {@code T} and produces a value of type {@code X} when
 * validation succeeds.
 *
 * <p>
 * This is the applicative counterpart of {@link Validatable}: instead of stopping at the
 * first violation, the returned {@link Validated} carries all violations. Validators can
 * be chained with {@link #andThen(ValueValidator)}, mapped with {@link #map(Function)},
 * adapted to another input type with {@link #compose(Function)}, given an input
 * transformed within its type with {@link #preprocess(Function)}, and lifted to
 * collections, maps, optionals or {@link Presence} with {@code lift*} methods, and used
 * under another name with {@link #named(String)}. A {@code null} input can be accepted
 * with {@link #optional()}, {@link #nullable()} or {@link #defaultValue(Object)}, any
 * failure is replaced with a fallback with {@link #recover(Object)}, alternatives are
 * combined with {@link #anyOf(ValueValidator[])}, a validator that is not built yet is
 * referred to with {@link #lazy(Supplier)}, and a rule that spans several properties of
 * the produced value is added with {@link #constraintOnTarget}. What a validator expects
 * as input is available from {@link #description()}; the combinators carry it. An input
 * of unknown type, such as a node of a parsed JSON document, is validated with
 * {@link #untyped()}, or with {@link #readingWith(InputReader)} if it is not a tree of
 * JDK types. The generated {@code am.ik.yavi.arguments.ArgumentsNValidator} types extend
 * this interface to validate constructor arguments and create only valid objects.
 *
 * <p>
 * The type of the produced value tells whether it can be {@code null}: a validator
 * succeeds with {@code null} only if {@code X} is nullable, as after {@link #nullable()}.
 * The validators created by the builders of single values therefore require the value and
 * report {@code null} as a violation. An implementation that passes a {@code null} input
 * on declares {@code @Nullable} for {@code X}; with NullAway, write it as a class instead
 * of a lambda, whose nullable type argument NullAway does not see yet.
 *
 * @param <T> the type of the object to be validated
 * @param <X> the type of the validated and transformed result; nullable if the validator
 * succeeds with {@code null}
 * @since 0.8.0
 * @see ApplicativeValidator
 * @see Validated
 */
@FunctionalInterface
public interface ValueValidator<T, X extends @Nullable Object> {

	/**
	 * Validates the input and returns the validated value.
	 *
	 * <p>
	 * The input may be {@code null}; implementations decide whether {@code null} is
	 * accepted or reported as a violation. On failure, the returned {@link Validated}
	 * contains the constraint violations instead of a value.
	 * @param t the input to validate, possibly {@code null}
	 * @param locale the locale targeted for the violation messages
	 * @param constraintContext constraint context used by conditional constraints
	 * @return the validated value or the constraint violations
	 */
	Validated<X> validate(@Nullable T t, Locale locale, ConstraintContext constraintContext);

	/**
	 * Returns a description of the input that this validator expects.
	 *
	 * <p>
	 * The default implementation returns {@link ValueDescription.Unknown}, which is what
	 * a lambda is described as. The validators created by the builders describe
	 * themselves, and the combinators of this interface carry the description:
	 * {@link #map(Function)}, {@link #andThen(ValueValidator)},
	 * {@link #constraintOnTarget}, {@link #prefixed(String)} and
	 * {@link #preprocess(Function)} keep it because they do not change the type of the
	 * input, {@link #named(String)} changes the name in it, the {@code lift*} methods,
	 * {@link #optional()}, {@link #nullable()}, {@link #defaultValue(Object)},
	 * {@link #recover(Object)} and {@link #anyOf(ValueValidator[])} wrap it,
	 * {@link #lazy(Supplier)} refers to it, and {@link #compose(Function)} resets it to
	 * unknown because it changes the input.
	 * @return the description of the input
	 * @since 0.18.0
	 * @see #describedAs(ValueDescription)
	 */
	default ValueDescription description() {
		return new ValueDescription.Unknown();
	}

	/**
	 * Returns a validator that validates like this validator and describes its input with
	 * the given description. This is the way to describe a lambda or the result of
	 * {@link #compose(Function)}.
	 * @param description the description of the input
	 * @return a value validator with the given description
	 * @since 0.18.0
	 * @see #description()
	 */
	default ValueValidator<T, X> describedAs(ValueDescription description) {
		return described(this, () -> description, reader -> this.readingWith(reader).describedAs(description),
				transform -> this.preprocess(transform).describedAs(description),
				name -> this.named(name).describedAs(description.named(name)));
	}

	/**
	 * Returns a validator that accepts a tree of JDK types of unknown type, such as a
	 * parsed JSON document, checks that it represents the input type of this validator
	 * and then validates it with this validator. This is {@link #readingWith(InputReader)
	 * readingWith(InputReader.ofJdkTypes())}.
	 * @return a value validator for an untyped input
	 * @throws UnsupportedOperationException if the input type of this validator is
	 * unknown
	 * @since 0.18.0
	 * @see #readingWith(InputReader)
	 * @see #untyped(Class)
	 */
	default ValueValidator<Object, X> untyped() {
		return this.readingWith(InputReader.ofJdkTypes());
	}

	/**
	 * Returns a validator that accepts a node of unknown type read with the given reader,
	 * such as a node of the tree model of a JSON library, checks that it represents the
	 * input type of this validator and then validates it with this validator.
	 *
	 * <p>
	 * The validators created by the builders of single values know their input type, the
	 * combinators of this interface carry it as they carry the {@linkplain #description()
	 * description}, and the {@code lift*} methods expect an array or an object of the
	 * reader and check every element or value. The input type of a lambda and of the
	 * result of {@link #compose(Function)} is unknown; give it with
	 * {@link #untyped(Class)}.
	 *
	 * <p>
	 * The reader is applied to the whole validator: the nested validators read with it
	 * too, whatever reader they were built for, so the returned validator can itself be
	 * given another reader. That reader replaces the given one and is not combined with
	 * it: after {@code readingWith(reader.coercingStrings()).readingWith(reader)} no
	 * string is coerced. A null node of the reader is validated like {@code null}.
	 * @param reader the reader of the nodes
	 * @param <N> the type of the nodes
	 * @return a value validator for an untyped input
	 * @throws UnsupportedOperationException if the input type of this validator is
	 * unknown
	 * @since 0.18.0
	 * @see #untyped(Class)
	 */
	default <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
		throw new UnsupportedOperationException(
				"The input type of this validator is unknown. Use untyped(Class) to give it.");
	}

	/**
	 * Returns a validator that accepts an input of unknown type, checks that it
	 * represents the given type and then validates it with this validator. The returned
	 * validator knows its input type, so it reads the nodes of another tree model with
	 * {@link #readingWith(InputReader)}.
	 *
	 * <p>
	 * {@code null} is passed to this validator as is. Any other input must be an instance
	 * of the given type or its representation in a JSON document:
	 * <ul>
	 * <li>a number for a numeric type ({@code Integer}, {@code Long}, {@code Short},
	 * {@code Byte}, {@code BigInteger}, {@code BigDecimal}, {@code Double},
	 * {@code Float}), whatever the type of the number is, as long as the value is kept:
	 * an integral type takes only an integral value in its range (so {@code 1.0} is an
	 * {@code Integer} and {@code 1.5} is not), a floating-point type takes any value in
	 * its range; a number written with an exponent above 10000 is not taken as a
	 * {@code BigInteger}, whose digits would exhaust the memory,</li>
	 * <li>the name of a constant for an enum type,</li>
	 * <li>an ISO-8601 string for {@code LocalDate}, {@code LocalTime},
	 * {@code LocalDateTime}, {@code OffsetDateTime}, {@code ZonedDateTime},
	 * {@code Instant}, {@code Year} and {@code YearMonth}, or an integral number for
	 * {@code Year},</li>
	 * <li>an ISO-8601 string for {@code Duration} ({@code PT1H30M}) and {@code Period}
	 * ({@code P1Y2M}),</li>
	 * <li>the canonical string (8-4-4-4-12 hexadecimal digits) for {@code UUID}, and a
	 * string that {@link java.net.URI} parses, which may be a reference without a scheme,
	 * for {@code URI},</li>
	 * <li>a map whose keys are all strings for {@code Map}.</li>
	 * </ul>
	 * Nothing else is converted: {@code "1"} is not a number and {@code 1} is not a
	 * string. An input whose values are all strings, such as request parameters, is read
	 * with {@link CoercibleInputReader#coercingStrings()}, which takes {@code "1"} as a
	 * number where a number is expected. For any other conversion, write a validator that
	 * takes an {@code Object} and give it {@code Object.class}.
	 *
	 * <p>
	 * With another reader, the checked input is the
	 * {@linkplain InputReader#value(Object, Class) value} of the node.
	 *
	 * <p>
	 * Any other input is reported with the message key {@code object.type}, or
	 * {@code object.oneOf} with the constants for an enum type. The violation has an
	 * empty name, so it is reported on the validated value itself and takes the name of
	 * the property the validator is applied to.
	 * @param inputType the raw input type of this validator
	 * @return a value validator for an untyped input
	 * @since 0.18.0
	 * @see #untyped()
	 */
	default ValueValidator<Object, X> untyped(Class<T> inputType) {
		return UntypedInput.checked(InputReader.ofJdkTypes(), inputType, this);
	}

	private static <T, X extends @Nullable Object> ValueValidator<T, X> described(ValueValidator<T, X> validator,
			Supplier<ValueDescription> description, Function<InputReader<Object>, ValueValidator<Object, X>> untyped) {
		return new DescribedValueValidator<>(validator, description, untyped, null, null);
	}

	/**
	 * Returns a described validator whose input is not the one of another validator, and
	 * which reports the violations of the validated value under another name as the given
	 * function states.
	 */
	private static <T, X extends @Nullable Object> ValueValidator<T, X> describedNamed(ValueValidator<T, X> validator,
			Supplier<ValueDescription> description, Function<InputReader<Object>, ValueValidator<Object, X>> untyped,
			Function<String, ValueValidator<T, X>> named) {
		return new DescribedValueValidator<>(validator, description, untyped, null, named);
	}

	/**
	 * Returns a described validator that does not change the input of another validator,
	 * so a transform of its input is a transform of the input of that validator.
	 */
	private static <T, X extends @Nullable Object> ValueValidator<T, X> described(ValueValidator<T, X> validator,
			Supplier<ValueDescription> description, Function<InputReader<Object>, ValueValidator<Object, X>> untyped,
			Function<Function<? super T, ? extends @Nullable T>, ValueValidator<T, X>> preprocessed) {
		return new DescribedValueValidator<>(validator, description, untyped, preprocessed, null);
	}

	/**
	 * Returns a described validator that validates the value of another validator, so
	 * that another name of the value is another name of the value of that validator.
	 */
	private static <T, X extends @Nullable Object> ValueValidator<T, X> described(ValueValidator<T, X> validator,
			Supplier<ValueDescription> description, Function<InputReader<Object>, ValueValidator<Object, X>> untyped,
			Function<Function<? super T, ? extends @Nullable T>, ValueValidator<T, X>> preprocessed,
			Function<String, ValueValidator<T, X>> named) {
		return new DescribedValueValidator<>(validator, description, untyped, preprocessed, named);
	}

	/**
	 * Returns a described validator that accepts a {@code null} input without invoking
	 * another validator, whose input it does not change.
	 */
	private static <T, X extends @Nullable Object> NullAcceptingValueValidator<T, X> nullAccepting(
			ValueValidator<T, X> validator, Supplier<ValueDescription> description,
			Function<InputReader<Object>, ValueValidator<Object, X>> untyped,
			Function<Function<? super T, ? extends @Nullable T>, ValueValidator<T, X>> preprocessed,
			Function<String, NullAcceptingValueValidator<T, X>> named) {
		return new DescribedNullAcceptingValueValidator<>(validator, description, nullAware(untyped), preprocessed,
				named);
	}

	/**
	 * Returns the description of a value that is described by the given description
	 * unless it is {@code null}, for which the given value is produced. A description
	 * that accepts {@code null} itself is replaced: its default value is never produced.
	 */
	private static ValueDescription acceptingNull(ValueDescription description, @Nullable Object defaultValue) {
		return new ValueDescription.Optional(
				description instanceof ValueDescription.Optional optional ? optional.value() : description,
				defaultValue);
	}

	/**
	 * Returns the untyped counterpart of a combinator that treats {@code null}: the
	 * result of the given function, which additionally takes a null node of its reader as
	 * {@code null}.
	 */
	private static <X extends @Nullable Object> Function<InputReader<Object>, ValueValidator<Object, X>> nullAware(
			Function<InputReader<Object>, ValueValidator<Object, X>> untyped) {
		return reader -> nullAware(reader, untyped.apply(reader), untyped);
	}

	private static <X extends @Nullable Object> ValueValidator<Object, X> nullAware(InputReader<Object> reader,
			ValueValidator<Object, X> validator, Function<InputReader<Object>, ValueValidator<Object, X>> untyped) {
		return describedNamed(
				(node, locale, constraintContext) -> validator.validate(UntypedInput.orNull(reader, node), locale,
						constraintContext),
				validator::description, nullAware(untyped),
				name -> nullAware(reader, validator.named(name), other -> untyped.apply(other).named(name)));
	}

	/**
	 * Returns the untyped counterpart of a validator of iterables: the node must be an
	 * array of the reader, and every element is validated with the untyped counterpart of
	 * the given validator.
	 */
	private static <N, A1, R extends @Nullable Object, C extends Collection<R>> ValueValidator<N, C> untypedCollection(
			InputReader<N> reader, ValueValidator<? super A1, ? extends R> validator, Supplier<C> factory) {
		final ValueValidator<N, ? extends R> element = validator.readingWith(reader);
		return described((node, locale, constraintContext) -> {
			final N array = UntypedInput.orNull(reader, node);
			if (array != null && !reader.isArray(array)) {
				return Validated.failureWith(UntypedInput.mismatch("array", reader, array, locale));
			}
			return Validated.traverseIndexed(array == null ? null : reader.elements(array),
					(value, index) -> element.validate(UntypedInput.orNull(reader, value), locale, constraintContext)
						.prefixed(index),
					factory);
		}, () -> new ValueDescription.Elements(validator.description()),
				other -> untypedCollection(other, validator, factory));
	}

	/**
	 * Returns the untyped counterpart of a validator of a list that may be given as text:
	 * a text is split with the given pattern, and so is every text of an array. An
	 * element that was split off is text that stands for a value, whatever the reader
	 * takes a string for.
	 */
	private static <N, T, X extends @Nullable Object> ValueValidator<N, List<X>> untypedSeparated(InputReader<N> reader,
			ValueValidator<T, X> validator, Pattern separator) {
		final ValueValidator<N, X> element = validator.readingWith(reader);
		final InputReader<Object> textReader = InputReader.ofJdkTypes()
			.coercingStrings(reader instanceof StringCoercingInputReader<N>coercing ? coercing.coercion()
					: StringCoercion.strict());
		final ValueValidator<Object, X> text = validator.readingWith(textReader);
		return described((node, locale, constraintContext) -> {
			final N present = UntypedInput.orNull(reader, node);
			final List<X> results = new ArrayList<>();
			if (present == null) {
				return Validated.successWith(results);
			}
			final boolean array = reader.isArray(present);
			if (!array && !(reader.value(present, String.class) instanceof String)) {
				// a text is taken too, so the type that was expected is not an array only
				return Validated.failureWith(UntypedInput.mismatch("array or string", reader, present, locale));
			}
			final List<ConstraintViolation> violations = new ArrayList<>();
			int index = 0;
			for (N value : array ? reader.elements(present) : List.of(present)) {
				final N item = UntypedInput.orNull(reader, value);
				if (item != null && reader.value(item, String.class) instanceof String string) {
					for (String part : UntypedInput.split(string, separator)) {
						index = collect(text.validate(UntypedInput.orNull(textReader, part), locale, constraintContext),
								index, results, violations);
					}
				}
				else {
					index = collect(element.validate(item, locale, constraintContext), index, results, violations);
				}
			}
			return violations.isEmpty() ? Validated.successWith(results) : Validated.failureWith(violations);
		}, () -> new ValueDescription.Elements(validator.description(), List.of(), separator.pattern()),
				other -> untypedSeparated(other, validator, separator));
	}

	/**
	 * Adds the validated element at the given index to the results, or its violations
	 * under the index to the violations, and returns the next index.
	 */
	private static <X extends @Nullable Object> int collect(Validated<X> validated, int index, List<X> results,
			List<ConstraintViolation> violations) {
		if (validated.isValid()) {
			results.add(validated.value());
		}
		else {
			validated.errors().forEach(violation -> violations.add(violation.prefixed(index)));
		}
		return index + 1;
	}

	/**
	 * Returns the untyped counterpart of a validator of maps: the node must be an object
	 * of the reader, and every value is validated with the untyped counterpart of the
	 * given validator.
	 */
	private static <N, T, X extends @Nullable Object> ValueValidator<N, Map<String, X>> untypedMap(
			InputReader<N> reader, ValueValidator<T, X> validator) {
		final ValueValidator<N, X> element = validator.readingWith(reader);
		return described((node, locale, constraintContext) -> {
			final N object = UntypedInput.orNull(reader, node);
			if (object != null && !reader.isObject(object)) {
				return Validated.failureWith(UntypedInput.mismatch("object", reader, object, locale));
			}
			final Map<String, X> results = new LinkedHashMap<>();
			final List<ConstraintViolation> violations = new ArrayList<>();
			if (object != null) {
				for (Map.Entry<String, ? extends @Nullable N> property : reader.properties(object)) {
					final Validated<X> validated = element.validate(UntypedInput.orNull(reader, property.getValue()),
							locale, constraintContext);
					if (validated.isValid()) {
						results.put(property.getKey(), validated.value());
					}
					else {
						validated.errors().forEach(violation -> violations.add(violation.prefixed(property.getKey())));
					}
				}
			}
			return violations.isEmpty() ? Validated.successWith(results) : Validated.failureWith(violations);
		}, () -> new ValueDescription.Entries(validator.description()), other -> untypedMap(other, validator));
	}

	/**
	 * Returns the untyped counterpart of a validator of optionals: the value of the node
	 * must be an {@link Optional}, whose content is validated with the untyped
	 * counterpart of the given validator.
	 */
	@SuppressWarnings("unchecked")
	private static <N, A1, R extends @Nullable Object> ValueValidator<N, Optional<@NonNull R>> untypedOptional(
			InputReader<N> reader, ValueValidator<? super A1, ? extends R> validator) {
		return describedNamed(
				UntypedInput.checked(reader, (Class<Optional<? extends N>>) (Class<?>) Optional.class,
						ValueValidator.liftOptional(validator.readingWith(reader))),
				() -> new ValueDescription.Optional(validator.description(), null),
				other -> untypedOptional(other, validator), name -> untypedOptional(reader, validator.named(name)));
	}

	/**
	 * Returns the untyped counterpart of a validator of a value that may be absent: the
	 * node is the value of a property that is present.
	 */
	private static <N, T, X extends @Nullable Object> ValueValidator<N, Presence<@NonNull X>> untypedPresence(
			InputReader<N> reader, ValueValidator<T, X> validator) {
		final ValueValidator<N, X> present = validator.readingWith(reader);
		return describedNamed(
				(node, locale, constraintContext) -> present
					.validate(UntypedInput.orNull(reader, node), locale, constraintContext)
					.map(x -> Presence.<@NonNull X>ofNullable(x)),
				() -> new ValueDescription.Presence(validator.description()),
				other -> untypedPresence(other, validator), name -> untypedPresence(reader, validator.named(name)));
	}

	/**
	 * Returns a validator that refers to the validator of the given supplier, which is
	 * asked for it on first use. This is the way to refer to a validator that is not
	 * built yet, which a structure that contains itself needs:
	 *
	 * <pre>{@code
	 * record Comment(String body, List<Comment> replies) {
	 *     static final ObjectInputValidator<Comment> VALIDATOR = Yavi.schema()
	 *         ._string("body", c -> c.notBlank())
	 *         ._list("replies", ValueValidator.lazy(() -> Comment.VALIDATOR)).defaultValue(List.of())
	 *         .fromMap(Comment::new);
	 * }
	 * }</pre>
	 *
	 * <p>
	 * The supplier is not called while validators are built with the returned validator.
	 * It is called when the returned validator, or a validator built with it, validates
	 * for the first time or resolves its description, and the validator it returns is
	 * kept. The returned validator is used like the one it refers to:
	 * <ul>
	 * <li>{@link #readingWith(InputReader)} is passed on to it, on first use too. An
	 * unknown input type is therefore reported by the first validation, not when the
	 * reader is given.</li>
	 * <li>The {@linkplain #description() description} is a
	 * {@link ValueDescription.Reference} to its description, which
	 * {@code am.ik.yavi.jsonschema.JsonSchema} writes as {@code $ref}.</li>
	 * <li>A builder that inspects the description of a validator while it is built sees
	 * only the reference: a lazy validator has no name of its own, so an argument is
	 * added with a name, and {@code _embedded} embeds it as a validator whose properties
	 * are unknown.</li>
	 * </ul>
	 *
	 * <p>
	 * The depth of a recursive structure is the depth of the input. Limiting it against
	 * hostile input is the task of the parser that reads the input, and a map that
	 * contains itself is never validated to the end.
	 *
	 * <p>
	 * This is unrelated to {@code lazy()} of the {@code ArgumentsNValidator} types, which
	 * defers the creation of the validated object.
	 * @param supplier the supplier of the validator referred to; it may return
	 * {@code null} as long as the returned validator is not used
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @return a value validator that validates like the supplied one
	 * @throws IllegalStateException when the returned validator is used and the supplier
	 * returns {@code null}
	 * @since 0.18.0
	 * @see #recursive(Function)
	 */
	static <T, X extends @Nullable Object> ValueValidator<T, X> lazy(
			Supplier<? extends @Nullable ValueValidator<T, X>> supplier) {
		return lazy("", supplier);
	}

	/**
	 * Returns a validator like {@link #lazy(Supplier)} whose
	 * {@linkplain ValueDescription.Reference#name() reference} has the given name, under
	 * which a JSON Schema defines the structure in {@code $defs}.
	 * @param name the name of the structure that the supplied validator validates, such
	 * as {@code Comment}
	 * @param supplier the supplier of the validator referred to
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @return a value validator that validates like the supplied one
	 * @since 0.18.0
	 */
	static <T, X extends @Nullable Object> ValueValidator<T, X> lazy(String name,
			Supplier<? extends @Nullable ValueValidator<T, X>> supplier) {
		return new LazyValueValidator<>(name, supplier);
	}

	/**
	 * Returns the validator that the given function builds from a reference to that very
	 * validator. This is {@link #lazy(Supplier)} without a variable to refer to, so it
	 * also defines a recursive validator in a local variable:
	 *
	 * <pre>{@code
	 * ObjectInputValidator<Comment> commentValidator = ValueValidator.recursive(self -> Yavi.schema()
	 *     ._string("body", c -> c.notBlank())
	 *     ._list("replies", self).defaultValue(List.of())
	 *     .fromMap(Comment::new));
	 * }</pre>
	 *
	 * <p>
	 * The function is called once, at once. The reference it is given is a lazy
	 * validator, so it is to be built into the returned validator and not used before the
	 * function returns. The types are inferred from the type that the result is assigned
	 * to.
	 * @param definition the function that builds the validator from a reference to it
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @param <V> the type of the built validator
	 * @return the validator built by the function
	 * @since 0.18.0
	 * @see #lazy(Supplier)
	 */
	static <T, X extends @Nullable Object, V extends ValueValidator<T, X>> V recursive(
			Function<? super ValueValidator<T, X>, ? extends V> definition) {
		return recursive("", definition);
	}

	/**
	 * Returns a validator like {@link #recursive(Function)} whose
	 * {@linkplain ValueDescription.Reference#name() reference} to itself has the given
	 * name, under which a JSON Schema defines the structure in {@code $defs}.
	 * @param name the name of the structure that the built validator validates, such as
	 * {@code Comment}
	 * @param definition the function that builds the validator from a reference to it
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @param <V> the type of the built validator
	 * @return the validator built by the function
	 * @since 0.18.0
	 */
	static <T, X extends @Nullable Object, V extends ValueValidator<T, X>> V recursive(String name,
			Function<? super ValueValidator<T, X>, ? extends V> definition) {
		final AtomicReference<@Nullable V> built = new AtomicReference<>();
		final V validator = definition.apply(lazy(name, built::get));
		built.set(validator);
		return validator;
	}

	/**
	 * Returns a validator that always succeeds and returns the input unchanged, so its
	 * value is {@code null} for a {@code null} input.
	 * @param <X> target class
	 * @return value validator that always successes without validation
	 */
	static <X> ValueValidator<X, @Nullable X> passThrough() {
		// not a lambda: NullAway does not see the nullable type argument of its target
		return new ValueValidator<X, @Nullable X>() {
			@Override
			public Validated<@Nullable X> validate(@Nullable X x, Locale locale, ConstraintContext constraintContext) {
				return Validated.successWith(x);
			}
		};
	}

	/**
	 * Returns a validator that maps the value produced on success using the given
	 * function. The mapper is not applied when validation fails. The mapped value is
	 * {@code null} only if the function is declared to return a nullable type.
	 * @param mapper function to transform the validated value
	 * @param <X2> the type after transformation
	 * @return a value validator that applies the mapping function after validation
	 * @since 0.17.0
	 */
	default <X2 extends @Nullable Object> ValueValidator<T, X2> map(Function<? super X, ? extends X2> mapper) {
		return described(
				(t, locale, constraintContext) -> ValueValidator.this.validate(t, locale, constraintContext)
					.map(mapper),
				this::description, reader -> this.readingWith(reader).map(mapper),
				transform -> this.preprocess(transform).map(mapper), name -> this.named(name).map(mapper));
	}

	/**
	 * Returns a validator that maps the value produced on success using the given
	 * function, as {@link #map(Function)} does, and that goes back from the mapped value
	 * to the input with the other function. This is how a value object is read from and
	 * written back as the value it wraps:
	 *
	 * <pre>{@code
	 * ReversibleValueValidator<String, Email> emailValidator = Yavi._string("email", c -> c.notBlank().email())
	 *     .map(Email::new, Email::value);
	 * }</pre>
	 *
	 * <p>
	 * The reverse returns an input, not the value this validator produces, so it is the
	 * reverse of the whole validator whatever this validator does to its input. For every
	 * mapped value {@code x}, {@code validate(reverse.apply(x))} must succeed with a
	 * value equal to {@code x}.
	 * @param mapper function to transform the validated value
	 * @param reverse function that returns the input that the mapped value is read from
	 * @param <X2> the type after transformation
	 * @return a value validator that applies the mapping function after validation and
	 * knows its reverse
	 * @since 0.18.0
	 * @see ReversibleValueValidator
	 */
	default <X2 extends @Nullable Object> ReversibleValueValidator<T, X2> map(Function<? super X, ? extends X2> mapper,
			Function<? super X2, ? extends @Nullable T> reverse) {
		return ReversibleValueValidator.of(this.map(mapper), reverse);
	}

	/**
	 * Returns a validator that applies this validator and, if it succeeds, passes the
	 * produced value to the given validator. Violations of this validator are returned
	 * without invoking the next validator. The violations of the given validator are
	 * reported with the locale of the validation, also if it does not pass the locale on
	 * (see {@link Validated#localizedWith(Locale)}).
	 * @param validator the validator to apply to the validated value
	 * @param <X2> the type produced by the next validator
	 * @return a chained value validator
	 * @since 0.11.0
	 */
	default <X2 extends @Nullable Object> ValueValidator<T, X2> andThen(
			ValueValidator<? super @NonNull X, ? extends X2> validator) {
		return described(
				(t, locale, constraintContext) -> ValueValidator.this.validate(t, locale, constraintContext)
					.flatMap(v -> validator.validate(v, locale, constraintContext).localizedWith(locale)),
				this::description, reader -> this.readingWith(reader).andThen(validator),
				transform -> this.preprocess(transform).andThen(validator),
				name -> this.named(name).andThen(validator.named(name)));
	}

	/**
	 * Returns a validator that applies the given function to the input before validating
	 * it. The function is not validated and its result is passed to this validator. The
	 * {@linkplain #description() description} and the input type of the new validator are
	 * unknown because the input changes.
	 * @param mapper function that converts the input of the new validator
	 * @param <A> the input type of the new validator
	 * @return a value validator with the adapted input type
	 */
	default <A extends @Nullable Object> ValueValidator<A, X> compose(
			Function<@Nullable A, ? extends @Nullable T> mapper) {
		return describedNamed((a, locale, constraintContext) -> ValueValidator.this.validate(mapper.apply(a), locale,
				constraintContext), ValueDescription.Unknown::new, reader -> {
					throw new UnsupportedOperationException(
							"The input type of this validator is unknown. Use untyped(Class) to give it.");
				}, name -> this.named(name).compose(mapper));
	}

	/**
	 * Returns a validator that applies the given function to an input other than
	 * {@code null} and validates the result with this validator: the constraints, the
	 * violated value of a violation and the produced value see the transformed input.
	 * This is the place to trim a text, to convert it to lower case or to bring a value
	 * into its canonical form before it is constrained.
	 *
	 * <pre>{@code
	 * Yavi._bigDecimal("price", c -> c.positive()).preprocess(BigDecimal::stripTrailingZeros)
	 * }</pre>
	 *
	 * <p>
	 * Unlike {@link #compose(Function)}, the input type does not change, so the
	 * {@linkplain #description() description} is kept. The input type is kept too, and
	 * with {@link #readingWith(InputReader)} the function is applied to the typed value
	 * after the type check, by the validators created by the builders of single values
	 * and by {@link #map(Function)}, {@link #andThen(ValueValidator)},
	 * {@link #constraintOnTarget}, {@link #prefixed(String)},
	 * {@link #describedAs(ValueDescription)}, {@link #optional()}, {@link #nullable()},
	 * {@link #defaultValue(Object)} and {@link #recover(Object)} of them. The input type
	 * of any other validator is unknown afterwards; give it with {@link #untyped(Class)}.
	 *
	 * <p>
	 * A function added later is applied earlier: {@code v.preprocess(f).preprocess(g)}
	 * applies {@code g} and then {@code f}. A {@code String} is transformed where its
	 * constraints are written, in the order of writing, with the transforms of
	 * {@code am.ik.yavi.constraint.StringValueConstraint}.
	 * @param transform the function applied to an input other than {@code null}; it may
	 * return {@code null}
	 * @return a value validator that transforms its input first
	 * @since 0.18.0
	 */
	default ValueValidator<T, X> preprocess(Function<? super T, ? extends @Nullable T> transform) {
		return describedNamed((t, locale, constraintContext) -> ValueValidator.this
			.validate(t == null ? null : transform.apply(t), locale, constraintContext), this::description, reader -> {
				throw new UnsupportedOperationException(
						"The input type of this validator is unknown. Use untyped(Class) to give it.");
			}, name -> this.named(name).preprocess(transform));
	}

	/**
	 * Returns a validator that accepts a {@code null} input as an absent value.
	 *
	 * <p>
	 * A {@code null} input succeeds with an empty {@link Optional} without invoking this
	 * validator, so constraints such as {@code notNull()} are not applied to it. Any
	 * other input is validated by this validator and its value is wrapped in an
	 * {@link Optional}.
	 * @return a value validator that produces an optional value
	 * @since 0.18.0
	 * @see #nullable()
	 * @see #defaultValue(Object)
	 */
	default NullAcceptingValueValidator<T, Optional<@NonNull X>> optional() {
		return nullAccepting(
				(t, locale, constraintContext) -> t == null ? Validated.successWith(Optional.empty())
						: ValueValidator.this.validate(t, locale, constraintContext)
							.map(x -> Optional.<@NonNull X>ofNullable(x)),
				() -> acceptingNull(this.description(), null), reader -> this.readingWith(reader).optional(),
				transform -> this.preprocess(transform).optional(), name -> this.named(name).optional());
	}

	/**
	 * Returns a validator that accepts a {@code null} input and produces {@code null} for
	 * it. The type of the produced value says so: it is {@code @Nullable X}.
	 *
	 * <p>
	 * A {@code null} input succeeds with {@code null} without invoking this validator, so
	 * constraints such as {@code notNull()} are not applied to it. Any other input is
	 * validated by this validator.
	 * @return a value validator that produces {@code null} for a {@code null} input
	 * @since 0.18.0
	 * @see #optional()
	 */
	default NullAcceptingValueValidator<T, @Nullable X> nullable() {
		// not a lambda: NullAway does not see the nullable type argument of its target
		return nullAccepting(new ValueValidator<T, @Nullable X>() {
			@Override
			public Validated<@Nullable X> validate(@Nullable T t, Locale locale, ConstraintContext constraintContext) {
				if (t == null) {
					return Validated.successWith(null);
				}
				return ValueValidator.this.validate(t, locale, constraintContext).map(x -> x);
			}
		}, () -> acceptingNull(this.description(), null), reader -> this.readingWith(reader).nullable(),
				transform -> this.preprocess(transform).nullable(), name -> this.named(name).nullable());
	}

	/**
	 * Returns a validator that produces the given value for a {@code null} input.
	 *
	 * <p>
	 * A {@code null} input succeeds with the default value without invoking this
	 * validator; the default value itself is not validated. Any other input is validated
	 * by this validator.
	 * @param defaultValue the value produced for a {@code null} input
	 * @return a value validator that falls back to the default value
	 * @since 0.18.0
	 * @see #recover(Object)
	 */
	default NullAcceptingValueValidator<T, X> defaultValue(X defaultValue) {
		return nullAccepting(
				(t, locale, constraintContext) -> t == null ? Validated.successWith(defaultValue)
						: ValueValidator.this.validate(t, locale, constraintContext),
				() -> acceptingNull(this.description(), defaultValue),
				reader -> this.readingWith(reader).defaultValue(defaultValue),
				transform -> this.preprocess(transform).defaultValue(defaultValue),
				name -> this.named(name).defaultValue(defaultValue));
	}

	/**
	 * Returns a validator that never fails: it produces the given value for a
	 * {@code null} input and for an input that this validator rejects. This is for an
	 * input that must not break a request, such as the size of a page.
	 *
	 * <pre>{@code
	 * Yavi._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)
	 * }</pre>
	 *
	 * <p>
	 * A {@code null} input succeeds with the fallback without invoking this validator, as
	 * with {@link #defaultValue(Object)}; the fallback itself is not validated. Any other
	 * input is validated by this validator, and every violation is replaced with the
	 * fallback: those of the constraints, those of the nested validators and, with
	 * {@link #readingWith(InputReader)}, the one of an input that does not represent the
	 * input type. An exception thrown by this validator is not caught.
	 *
	 * <p>
	 * The input is {@linkplain #description() described} as with
	 * {@link #defaultValue(Object)}: what this validator expects, or nothing, in which
	 * case the fallback is produced.
	 * @param fallback the value produced instead of a failure
	 * @return a value validator that always succeeds
	 * @since 0.18.0
	 * @see #recoverWith(Function)
	 */
	default NullAcceptingValueValidator<T, X> recover(X fallback) {
		return nullAccepting(
				(t, locale, constraintContext) -> t == null ? Validated.successWith(fallback)
						: recovered(ValueValidator.this.validate(t, locale, constraintContext), violations -> fallback),
				() -> acceptingNull(this.description(), fallback), reader -> this.readingWith(reader).recover(fallback),
				transform -> this.preprocess(transform).recover(fallback), name -> this.named(name).recover(fallback));
	}

	/**
	 * Returns a validator that never fails: it produces the result of the given function
	 * for a {@code null} input and for an input that this validator rejects. This is
	 * {@link #recover(Object)} for a fallback that depends on the violations, or that is
	 * to be reported:
	 *
	 * <pre>{@code
	 * Yavi._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recoverWith(violations -> {
	 *     violations.forEach(violation -> log.debug("{}", violation.message()));
	 *     return 20;
	 * })
	 * }</pre>
	 *
	 * <p>
	 * The function is given the violations of the rejected input. For a {@code null}
	 * input this validator is not invoked and the function is given no violation. The
	 * {@linkplain #description() description} has no default value, which is not known
	 * before the validation.
	 * @param fallback the function that produces a value from the violations
	 * @return a value validator that always succeeds
	 * @since 0.18.0
	 * @see #recover(Object)
	 */
	default NullAcceptingValueValidator<T, X> recoverWith(
			Function<? super ConstraintViolations, ? extends X> fallback) {
		return nullAccepting(
				(t, locale, constraintContext) -> t == null
						? Validated.successWith(fallback.apply(new ConstraintViolations()))
						: recovered(ValueValidator.this.validate(t, locale, constraintContext), fallback),
				() -> acceptingNull(this.description(), null), reader -> this.readingWith(reader).recoverWith(fallback),
				transform -> this.preprocess(transform).recoverWith(fallback),
				name -> this.named(name).recoverWith(fallback));
	}

	private static <X extends @Nullable Object> Validated<X> recovered(Validated<X> validated,
			Function<? super ConstraintViolations, ? extends X> fallback) {
		return validated.isValid() ? validated : Validated.successWith(fallback.apply(validated.errors()));
	}

	/**
	 * Returns a validator that tries the given validators in order and produces the value
	 * of the first one that succeeds.
	 *
	 * <p>
	 * If every alternative fails, the result holds one violation with the message key
	 * {@code object.anyOf}. It has an empty name, so it is reported on the validated
	 * value itself and takes the name of the property the validator is applied to. The
	 * violations of the alternatives are not in the result: a list of them would not tell
	 * which alternative a violation belongs to and would show contradictory messages for
	 * one value. The violation is a {@link ConstraintViolation.NoAlternativeMatched},
	 * which keeps them per alternative, in the given order, in
	 * {@link ConstraintViolation.NoAlternativeMatched#alternatives() alternatives()};
	 * they are renamed and prefixed with it.
	 * @param validators the alternatives, tried in the given order
	 * @param <T> the type of the object to be validated
	 * @param <X> the common type of the values produced by the alternatives
	 * @return a value validator that succeeds if any alternative succeeds
	 * @throws IllegalArgumentException if no alternative is given
	 * @since 0.18.0
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	static <T, X extends @Nullable Object> ValueValidator<T, X> anyOf(
			ValueValidator<? super T, ? extends X>... validators) {
		if (validators.length == 0) {
			throw new IllegalArgumentException("'validators' must not be empty.");
		}
		final List<ValueValidator<? super T, ? extends X>> alternatives = List.of(validators);
		return ValueValidator.<T, X>alternatives(alternatives, violation -> violation);
	}

	/**
	 * Returns a validator of the given alternatives that locates the violation that no
	 * alternative matched with the given function. Under another name of the validated
	 * value, every alternative validates the value under that name.
	 */
	@SuppressWarnings("unchecked")
	private static <T, X extends @Nullable Object> ValueValidator<T, X> alternatives(
			List<ValueValidator<? super T, ? extends X>> alternatives,
			Function<ConstraintViolation, ConstraintViolation> located) {
		final Function<InputReader<Object>, ValueValidator<Object, X>> untyped = reader -> alternatives(
				alternatives.stream()
					.<ValueValidator<? super Object, ? extends X>>map(alternative -> alternative.readingWith(reader))
					.toList(),
				located);
		return ValueValidator.<T, X>describedNamed((t, locale, constraintContext) -> {
			final List<ConstraintViolations> violations = new ArrayList<>();
			for (ValueValidator<? super T, ? extends X> alternative : alternatives) {
				final Validated<? extends X> validated = alternative.validate(t, locale, constraintContext);
				if (validated.isValid()) {
					return Validated.successWith(validated.value());
				}
				violations.add(validated.errors());
			}
			return Validated.failureWith(located
				.apply(ConstraintViolation.of(ViolationMessage.Default.OBJECT_ANY_OF, t, locale, alternatives.size()))
				.withAlternatives(violations));
		}, () -> new ValueDescription.AnyOf(alternatives.stream().map(ValueValidator::description).toList()), untyped,
				name -> alternatives(alternatives.stream()
					.<ValueValidator<? super T, ? extends X>>map(
							alternative -> ((ValueValidator<T, X>) alternative).named(name))
					.toList(), violation -> located.apply(violation).prefixed(name)));
	}

	/**
	 * Returns a validator that additionally checks the value produced by this validator
	 * with the given predicate. This is the place for a rule that spans several
	 * properties of the produced value, such as a password confirmation or a date range.
	 *
	 * <p>
	 * The predicate is evaluated only when this validator succeeds. If it returns
	 * {@code false}, a violation with the given name and message is reported; its last
	 * message argument is the produced value.
	 *
	 * <p>
	 * Rules added one after another are all checked on the produced value, and every
	 * violated rule is reported, like the violations of the properties:
	 *
	 * <pre>{@code
	 * Yavi.schema()
	 *     ._string("currentPassword", c -> c.notBlank())
	 *     ._string("newPassword", c -> c.greaterThanOrEqual(8))
	 *     ._string("confirmPassword", c -> c.notBlank())
	 *     .fromMap(PasswordForm::new)
	 *     .constraintOnTarget(form -> form.newPassword().equals(form.confirm()), "confirmPassword",
	 *             ViolationMessage.of("password.mismatch", "\"{0}\" must match \"newPassword\""))
	 *     .constraintOnTarget(form -> !form.current().equals(form.newPassword()), "newPassword",
	 *             ViolationMessage.of("password.same", "\"{0}\" must differ from \"currentPassword\""));
	 * }</pre>
	 *
	 * A predicate therefore must not rely on the rules before it. A rule that is checked
	 * only when other rules hold is added after {@link #map(Function)} or
	 * {@link #andThen(ValueValidator)}, which are applied to a value that satisfies the
	 * rules before them, for example after {@code map(Function.identity())}.
	 * @param predicate the rule that the produced value must satisfy
	 * @param name the name of the reported violation
	 * @param violationMessage the message key and the default message format of the
	 * reported violation
	 * @return a value validator with the additional rule
	 * @since 0.18.0
	 * @see TargetConstraints
	 */
	default ValueValidator<T, X> constraintOnTarget(Predicate<? super X> predicate, String name,
			ViolationMessage violationMessage) {
		return TargetConstraints.<X>empty().and(predicate, name, violationMessage).constrain(this);
	}

	/**
	 * Validates the input with the default locale and {@link ConstraintGroup#DEFAULT}.
	 * @param t the input to validate, possibly {@code null}
	 * @return the validated value or the constraint violations
	 */
	default Validated<X> validate(@Nullable T t) {
		return this.validate(t, Locale.getDefault(), ConstraintGroup.DEFAULT);
	}

	/**
	 * Validates the input with the default locale and the given constraint context.
	 * @param t the input to validate, possibly {@code null}
	 * @param constraintContext constraint context used by conditional constraints
	 * @return the validated value or the constraint violations
	 */
	default Validated<X> validate(@Nullable T t, ConstraintContext constraintContext) {
		return this.validate(t, Locale.getDefault(), constraintContext);
	}

	/**
	 * Validates the input with the given locale and {@link ConstraintGroup#DEFAULT}.
	 * @param t the input to validate, possibly {@code null}
	 * @param locale the locale targeted for the violation messages
	 * @return the validated value or the constraint violations
	 */
	default Validated<X> validate(@Nullable T t, Locale locale) {
		return this.validate(t, locale, ConstraintGroup.DEFAULT);
	}

	/**
	 * Validates the input and returns the validated value, throwing if validation fails.
	 * @param t the input to validate, possibly {@code null}
	 * @return the validated value
	 * @throws ConstraintViolationsException if validation fails
	 */
	default X validated(@Nullable T t) throws ConstraintViolationsException {
		return this.validate(t).orElseThrow(ConstraintViolationsException::new);
	}

	/**
	 * Validates the input with the given constraint context and returns the validated
	 * value, throwing if validation fails.
	 * @param t the input to validate, possibly {@code null}
	 * @param constraintContext constraint context used by conditional constraints
	 * @return the validated value
	 * @throws ConstraintViolationsException if validation fails
	 */
	default X validated(@Nullable T t, ConstraintContext constraintContext) throws ConstraintViolationsException {
		return this.validate(t, constraintContext).orElseThrow(ConstraintViolationsException::new);
	}

	/**
	 * Validates the input with the given locale and returns the validated value, throwing
	 * if validation fails.
	 * @param t the input to validate, possibly {@code null}
	 * @param locale the locale targeted for the violation messages
	 * @return the validated value
	 * @throws ConstraintViolationsException if validation fails
	 */
	default X validated(@Nullable T t, Locale locale) throws ConstraintViolationsException {
		return this.validate(t, locale).orElseThrow(ConstraintViolationsException::new);
	}

	/**
	 * Validates the input with the given locale and constraint context and returns the
	 * validated value, throwing if validation fails.
	 * @param t the input to validate, possibly {@code null}
	 * @param locale the locale targeted for the violation messages
	 * @param constraintContext constraint context used by conditional constraints
	 * @return the validated value
	 * @throws ConstraintViolationsException if validation fails
	 */
	default X validated(@Nullable T t, Locale locale, ConstraintContext constraintContext)
			throws ConstraintViolationsException {
		return this.validate(t, locale, constraintContext).orElseThrow(ConstraintViolationsException::new);
	}

	/**
	 * Returns a validator that locates every violation under the property with the given
	 * name, so that the violations of a nested value can be told apart. For example the
	 * violation name {@code street} becomes {@code address.street}.
	 * @param name the name of the property that contains the validated value
	 * @return a validator with prefixed violation names and paths
	 * @since 0.18.0
	 * @see ConstraintViolation#prefixed(String)
	 */
	default ValueValidator<T, X> prefixed(String name) {
		return described(
				(t, locale, constraintContext) -> ValueValidator.this.validate(t, locale, constraintContext)
					.prefixed(name),
				this::description, reader -> this.readingWith(reader).prefixed(name),
				transform -> this.preprocess(transform).prefixed(name));
	}

	/**
	 * Returns a validator that reports the violations of the validated value under the
	 * given name instead of the name it was built with. This is how the rule of a value
	 * is defined once and used for several properties:
	 *
	 * <pre>{@code
	 * StringValidator<String> email = Yavi._string(c -> c.trim().notBlank().email());
	 *
	 * Yavi.schema()
	 *     ._string(email.named("email"))
	 *     ._string(email.named("backupEmail")).nullable()
	 *     .fromMap(Contact::new);
	 * }</pre>
	 *
	 * <p>
	 * The name is the one that a violation on the value itself has. It replaces the name
	 * of a validator of a single value, with or without a name, in the violations, in
	 * their messages and in the {@linkplain #description() description}, so the key of a
	 * property added to a schema with the validator alone is the given name. The
	 * violations of a value that has none, such as an object, a list, a map or the value
	 * of a lambda, are located under the name, as {@link #prefixed(String)} does:
	 * {@code street} becomes {@code address.street} and {@code [0]} becomes
	 * {@code tags[0]}.
	 *
	 * <p>
	 * The combinators carry the name to the validators that validate the same value:
	 * {@link #map(Function)}, {@link #preprocess(Function)}, {@link #compose(Function)},
	 * {@link #optional()}, {@link #nullable()}, {@link #defaultValue(Object)},
	 * {@link #recover(Object)}, {@link #constraintOnTarget}, {@link #lazy(Supplier)},
	 * {@link #liftOptional()} and {@link #liftPresence()} to the validator they are
	 * applied to, {@link #andThen(ValueValidator)} to both validators and
	 * {@link #anyOf(ValueValidator[])} to every alternative. It makes no difference
	 * whether the name is given before or after them, or before or after
	 * {@link #readingWith(InputReader)}. A name given later replaces the earlier one.
	 * @param name the name under which the violations of the value are reported; an empty
	 * name takes the name away
	 * @return a validator of the value under the given name
	 * @since 0.18.0
	 * @see #prefixed(String)
	 */
	default ValueValidator<T, X> named(String name) {
		return this.prefixed(name);
	}

	/**
	 * Returns a validator that formats the messages of every violation it reports with
	 * the given formatter, whichever validator created the violation: the validators of
	 * the nested values too, and the checks of a reader that report a missing value or a
	 * value of another type.
	 *
	 * <pre>{@code
	 * ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
	 *     ._string("email", c -> c.notBlank().email())
	 *     .fromMap(Signup::new)
	 *     .readingParameters()
	 *     .formattingWith(new MessageSourceMessageFormatter(messageSource::getMessage));
	 * }</pre>
	 *
	 * The outermost formatter decides: a validator nested in the returned one loses its
	 * own.
	 * @param messageFormatter the message formatter of the reported violations
	 * @return a validator whose violations are formatted with the given formatter
	 * @since 0.18.0
	 * @see ConstraintViolations#formattedWith(MessageFormatter)
	 */
	default ValueValidator<T, X> formattingWith(MessageFormatter messageFormatter) {
		return described(
				(t, locale,
						constraintContext) -> Validated.of(ValueValidator.this.validate(t, locale, constraintContext)
							.mapError(violation -> violation.formattedWith(messageFormatter))),
				this::description, reader -> this.readingWith(reader).formattingWith(messageFormatter),
				transform -> this.preprocess(transform).formattingWith(messageFormatter),
				name -> this.named(name).formattingWith(messageFormatter));
	}

	/**
	 * Returns a validator that validates every element of the input iterable with this
	 * validator and collects the validated values into a collection created by the given
	 * factory. The violations of an element are located under its index ({@code [0]},
	 * {@code [0].name}) and the violations of all failing elements are accumulated. The
	 * elements may be of a subtype of {@code T}, as for every {@code lift*} method: a
	 * validator of {@code Object} lifted to a list validates a {@code List<String>}.
	 * @param factory supplier of the collection that receives the validated values
	 * @param <C> the type of the resulting collection
	 * @return a value validator for iterables
	 */
	default <C extends Collection<X>> ValueValidator<Iterable<? extends T>, C> liftCollection(Supplier<C> factory) {
		return ValueValidator.liftCollection(this, factory);
	}

	/**
	 * Returns a validator that validates every element of the input iterable with this
	 * validator and collects the validated values into a {@link List}. The violations of
	 * an element are located under its index ({@code [0]}, {@code [0].name}) and the
	 * violations of all failing elements are accumulated. A {@code null} iterable
	 * validates successfully with an empty list. The list itself is constrained with
	 * {@code Yavi._list(elementValidator, constraints)}.
	 *
	 * <p>
	 * The elements may be of a subtype of {@code T}, so the rows of a CSV file are
	 * validated as they are:
	 *
	 * <pre>{@code
	 * ValueValidator<Map<String, ?>, Member> rowValidator = memberValidator.readingParameters();
	 * List<Map<String, String>> rows = ...;
	 * Validated<List<Member>> members = rowValidator.liftList().validate(rows);
	 * }</pre>
	 * @return a value validator for iterables
	 */
	default ValueValidator<Iterable<? extends T>, List<X>> liftList() {
		return ValueValidator.liftList(this);
	}

	/**
	 * Returns a validator like {@link #liftList()} that also reads the list from text
	 * whose elements are separated by the given pattern, such as {@code java,yavi} or one
	 * element per line. It matters to an untyped input ({@link #untyped()},
	 * {@link #readingWith(InputReader)}); a typed iterable holds the elements already and
	 * is validated as {@link #liftList()} does.
	 *
	 * <pre>{@code
	 * ValueValidator<Object, List<Integer>> ids = Yavi._integer(c -> c.positive())
	 *     .liftList(Pattern.compile(","))
	 *     .untyped();
	 * ids.validate("1,2,3"); // [1, 2, 3]
	 * ids.validate("1,x,-3"); // violations at [1] and [2]
	 * }</pre>
	 *
	 * <ul>
	 * <li>A node that is text is split, and so is every element of an array that is text,
	 * so a repeated request parameter gives the same list as one parameter:
	 * {@code tags=a,b&tags=c} is {@code [a, b, c]}. Any other element of an array is
	 * validated as it is.</li>
	 * <li>An element that was split off is text that stands for a value, as
	 * {@link CoercibleInputReader#coercingStrings()} states, whatever the reader takes a
	 * string for: {@code "1"} is an {@code Integer} where the element is one. The
	 * {@link StringCoercion} is the one of the reader if it coerces strings.</li>
	 * <li>Nothing is dropped and nothing is trimmed, so that a violation tells what was
	 * sent: {@code a,,b} has three elements and the second has no value, which the
	 * validator of the element decides about. Only a separator at the end of the text
	 * ends the last element and starts no other: {@code a,b,} is {@code [a, b]}, like the
	 * line break at the end of the last line. White space is trimmed by the separator
	 * ({@code \\s*,\\s*}), by the coercion or by the validator of the element.</li>
	 * <li>The violations of an element are located under its index in the list of all
	 * elements.</li>
	 * <li>A node that is neither text nor an array, such as a number or an object, is
	 * reported with {@code object.type} as one that is not of type
	 * {@code array or string}. A single value is not taken for a list of one
	 * element.</li>
	 * </ul>
	 * @param separator the pattern that separates the elements in a text
	 * @return a value validator for iterables and for text
	 * @throws IllegalArgumentException if the pattern matches the empty string
	 * @since 0.18.0
	 */
	default ValueValidator<Iterable<? extends T>, List<X>> liftList(Pattern separator) {
		if (separator.matcher("").matches()) {
			throw new IllegalArgumentException("The separator '" + separator.pattern() + "' matches the empty string.");
		}
		return described(
				(values, locale, constraintContext) -> Validated.traverseIndexed(values,
						(v, index) -> this.validate(v, locale, constraintContext).prefixed(index), ArrayList::new),
				() -> new ValueDescription.Elements(this.description(), List.of(), separator.pattern()),
				reader -> untypedSeparated(reader, this, separator));
	}

	/**
	 * Returns a validator that validates every element of the input iterable with this
	 * validator and collects the validated values into a {@link Set} that preserves the
	 * iteration order. The violations of an element are located under its index ({@code
	 * [0]}, {@code [0].name}) and the violations of all failing elements are accumulated.
	 * @return a value validator for iterables
	 */
	default ValueValidator<Iterable<? extends T>, Set<X>> liftSet() {
		return ValueValidator.liftSet(this);
	}

	/**
	 * Returns a validator that validates every value of the input map with this validator
	 * and collects the validated values into a {@link Map} with the same keys in
	 * iteration order. The violations of a value are located under its key and the
	 * violations of all failing values are accumulated. A {@code null} map validates
	 * successfully with an empty map. The values may be of a subtype of {@code T}. The
	 * map itself is constrained with {@code Yavi._map(valueValidator, constraints)}.
	 * @return a value validator for maps
	 * @since 0.18.0
	 */
	default ValueValidator<Map<String, ? extends T>, Map<String, X>> liftMap() {
		return described((values, locale, constraintContext) -> {
			final Map<String, X> results = new LinkedHashMap<>();
			final List<ConstraintViolation> violations = new ArrayList<>();
			if (values != null) {
				values.forEach((key, value) -> {
					final Validated<X> validated = this.validate(value, locale, constraintContext);
					if (validated.isValid()) {
						results.put(key, validated.value());
					}
					else {
						validated.errors().forEach(violation -> violations.add(violation.prefixed(key)));
					}
				});
			}
			return violations.isEmpty() ? Validated.successWith(results) : Validated.failureWith(violations);
		}, () -> new ValueDescription.Entries(this.description()), reader -> untypedMap(reader, this));
	}

	/**
	 * Returns a validator that validates the value contained in the input
	 * {@link Optional}, if any, and returns an {@link Optional} with the validated value.
	 * An empty optional validates successfully. The value may be of a subtype of
	 * {@code T}.
	 * @return a value validator for optionals
	 */
	default ValueValidator<Optional<? extends T>, Optional<@NonNull X>> liftOptional() {
		return ValueValidator.liftOptional(this);
	}

	/**
	 * Returns a validator that validates every element of the input iterable with the
	 * given validator and collects the validated values into a collection created by the
	 * given factory. The violations of an element are located under its index ({@code
	 * [0]}, {@code [0].name}) and the violations of all failing elements are accumulated.
	 * @param validator the validator applied to each element
	 * @param factory supplier of the collection that receives the validated values
	 * @param <A1> the type that the elements of the input iterable are a subtype of
	 * @param <R> the type produced for each element
	 * @param <C> the type of the resulting collection
	 * @return a value validator for iterables
	 * @since 0.8.1
	 */
	static <A1, R extends @Nullable Object, C extends Collection<R>> ValueValidator<Iterable<? extends A1>, C> liftCollection(
			ValueValidator<? super A1, ? extends R> validator, Supplier<C> factory) {
		return described(
				(values, locale, constraintContext) -> Validated.traverseIndexed(values,
						(v, index) -> validator.validate(v, locale, constraintContext).prefixed(index), factory),
				() -> new ValueDescription.Elements(validator.description()),
				reader -> untypedCollection(reader, validator, factory));
	}

	/**
	 * Returns a validator that validates every element of the input iterable with the
	 * given validator and collects the validated values into a {@link List}. The
	 * violations of an element are located under its index ({@code [0]}, {@code
	 * [0].name}) and the violations of all failing elements are accumulated.
	 * @param validator the validator applied to each element
	 * @param <A1> the element type of the input iterable
	 * @param <R> the type produced for each element
	 * @return a value validator for iterables
	 * @since 0.8.1
	 */
	static <A1, R extends @Nullable Object> ValueValidator<Iterable<? extends A1>, List<R>> liftList(
			ValueValidator<? super A1, ? extends R> validator) {
		return ValueValidator.liftCollection(validator, ArrayList::new);

	}

	/**
	 * Returns a validator that validates every element of the input iterable with the
	 * given validator and collects the validated values into a {@link Set} that preserves
	 * the iteration order. The violations of an element are located under its index
	 * ({@code [0]}, {@code [0].name}) and the violations of all failing elements are
	 * accumulated.
	 * @param validator the validator applied to each element
	 * @param <A1> the element type of the input iterable
	 * @param <R> the type produced for each element
	 * @return a value validator for iterables
	 * @since 0.8.1
	 */
	static <A1, R extends @Nullable Object> ValueValidator<Iterable<? extends A1>, Set<R>> liftSet(
			ValueValidator<? super A1, ? extends R> validator) {
		// Since Index is attached to the name of ConstraintViolation,
		// we need a consistent order. This is why we use LinkedHashSet.
		return ValueValidator.liftCollection(validator, LinkedHashSet::new);
	}

	/**
	 * Returns a validator that validates the value contained in the input
	 * {@link Optional}, if any, with the given validator.
	 * @param validator the validator applied to the contained value
	 * @param <A1> the contained type of the input optional
	 * @param <R> the type produced for the contained value
	 * @return a value validator for optionals
	 * @since 0.8.1
	 */
	static <A1, R extends @Nullable Object> ValueValidator<Optional<? extends A1>, Optional<@NonNull R>> liftOptional(
			ValueValidator<? super A1, ? extends R> validator) {
		return describedNamed(
				(value, locale, constraintContext) -> Validated.traverseOptional(value,
						v -> validator.validate(v, locale, constraintContext)),
				() -> new ValueDescription.Optional(validator.description(), null),
				reader -> untypedOptional(reader, validator), name -> liftOptional(validator.named(name)));
	}

	/**
	 * Returns a validator of a value that may be absent, which is told from {@code null}:
	 * {@link Presence.Absent} succeeds as it is without invoking this validator, and the
	 * value of a {@link Presence} that is present is validated by this validator, as
	 * {@code null} for {@link Presence.PresentNull}. This validator therefore decides
	 * whether {@code null} is accepted, and the produced value is given as
	 * {@link Presence#ofNullable(Object)}. A {@code null} input is taken as absent. The
	 * value may be of a subtype of {@code T}.
	 *
	 * <pre>{@code
	 * ValueValidator<Presence<? extends String>, Presence<String>> nickname = Yavi
	 *     ._string("nickname", c -> c.lessThanOrEqual(50))
	 *     .liftPresence();
	 * nickname.validate(Presence.absent()); // valid: Absent
	 * nickname.validate(Presence.presentNull()); // invalid: object.notNull
	 * }</pre>
	 *
	 * <p>
	 * The node of an untyped input is the value of a property that is present, so with
	 * {@link #readingWith(InputReader)} the validator never produces
	 * {@link Presence.Absent}. Whether an object has a property is told by the validator
	 * of the object: {@code presence()} of the builders returned by
	 * {@code Yavi.schema()}, or {@code ObjectInputValidator.PresenceProperty}.
	 * @return a value validator for a value that may be absent
	 * @since 0.18.0
	 */
	default ValueValidator<Presence<? extends T>, Presence<@NonNull X>> liftPresence() {
		return describedNamed(
				(presence, locale, constraintContext) -> presence == null || presence.isAbsent()
						? Validated.successWith(Presence.absent())
						: this.validate(presence.orElse(null), locale, constraintContext)
							.map(x -> Presence.<@NonNull X>ofNullable(x)),
				() -> new ValueDescription.Presence(this.description()), reader -> untypedPresence(reader, this),
				name -> this.named(name).liftPresence());
	}

	/**
	 * Converts the value validator to a {@link BiConsumer} that validates the first
	 * argument with the default locale and constraint group and passes each violation to
	 * the given error handler. The consumer does nothing when validation succeeds.
	 * @param errorHandler error handler
	 * @param <E> error type
	 * @return bi consumer
	 * @since 0.13.0
	 */
	default <E> BiConsumer<T, E> toBiConsumer(ErrorHandler<E> errorHandler) {
		return (target, errors) -> {
			final Validated<?> validated = ValueValidator.this.validate(target);
			if (!validated.isValid()) {
				final ConstraintViolations violations = validated.errors();
				violations.apply((name, messageKey, args, defaultMessage) -> errorHandler.handleError(errors, name,
						messageKey, args, defaultMessage));
			}
		};
	}

}
