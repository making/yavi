/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import am.ik.yavi.fn.Validation;
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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Validates an input of type {@code T} and produces a value of type {@code X} when
 * validation succeeds.
 *
 * <p>
 * This is the applicative counterpart of {@link Validatable}: instead of stopping at the
 * first violation, the returned {@link Validated} carries all violations. Validators can
 * be chained with {@link #andThen(ValueValidator)}, mapped with {@link #map(Function)},
 * adapted to another input type with {@link #compose(Function)}, and lifted to
 * collections, maps or optionals with {@code lift*} methods. A {@code null} input can be
 * accepted with {@link #optional()}, {@link #nullable()} or
 * {@link #defaultValue(Object)}, alternatives are combined with
 * {@link #anyOf(ValueValidator[])}, and a rule that spans several properties of the
 * produced value is added with {@link #constraintOnTarget}. What a validator expects as
 * input is available from {@link #description()}; the combinators carry it. An input of
 * unknown type, such as a node of a parsed JSON document, is validated with
 * {@link #untyped()}, or with {@link #untyped(InputReader)} if it is not a tree of JDK
 * types. The generated {@code am.ik.yavi.arguments.ArgumentsNValidator} types extend this
 * interface to validate constructor arguments and create only valid objects.
 *
 * @param <T> the type of the object to be validated
 * @param <X> the type of the validated and transformed result
 * @since 0.8.0
 * @see ApplicativeValidator
 * @see Validated
 */
@FunctionalInterface
public interface ValueValidator<T, X> {

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
	 * {@link #constraintOnTarget}, {@link #indexed(int)} and {@link #prefixed(String)}
	 * keep it because they do not change the input, the {@code lift*} methods,
	 * {@link #optional()}, {@link #nullable()}, {@link #defaultValue(Object)} and
	 * {@link #anyOf(ValueValidator[])} wrap it, and {@link #compose(Function)} resets it
	 * to unknown because it changes the input.
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
		return described(this, () -> description, reader -> this.untyped(reader).describedAs(description));
	}

	/**
	 * Returns a validator that accepts a tree of JDK types of unknown type, such as a
	 * parsed JSON document, checks that it represents the input type of this validator
	 * and then validates it with this validator. This is {@link #untyped(InputReader)
	 * untyped(InputReader.ofJdkTypes())}.
	 * @return a value validator for an untyped input
	 * @throws UnsupportedOperationException if the input type of this validator is
	 * unknown
	 * @since 0.18.0
	 * @see #untyped(InputReader)
	 * @see #untyped(Class)
	 */
	default ValueValidator<Object, X> untyped() {
		return this.untyped(InputReader.ofJdkTypes());
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
	 * given another reader. A null node of the reader is validated like {@code null}.
	 * @param reader the reader of the nodes
	 * @param <N> the type of the nodes
	 * @return a value validator for an untyped input
	 * @throws UnsupportedOperationException if the input type of this validator is
	 * unknown
	 * @since 0.18.0
	 * @see #untyped(Class)
	 */
	default <N> ValueValidator<N, X> untyped(InputReader<N> reader) {
		throw new UnsupportedOperationException(
				"The input type of this validator is unknown. Use untyped(Class) to give it.");
	}

	/**
	 * Returns a validator that accepts an input of unknown type, checks that it
	 * represents the given type and then validates it with this validator. The returned
	 * validator knows its input type, so it reads the nodes of another tree model with
	 * {@link #untyped(InputReader)}.
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
	 * its range,</li>
	 * <li>the name of a constant for an enum type,</li>
	 * <li>an ISO-8601 string for {@code LocalDate}, {@code LocalTime},
	 * {@code LocalDateTime}, {@code OffsetDateTime}, {@code ZonedDateTime},
	 * {@code Instant}, {@code Year} and {@code YearMonth}, or an integral number for
	 * {@code Year},</li>
	 * <li>a map whose keys are all strings for {@code Map}.</li>
	 * </ul>
	 * Nothing else is converted: {@code "1"} is not a number and {@code 1} is not a
	 * string. To accept such an input, write a validator that takes an {@code Object} and
	 * give it {@code Object.class}.
	 *
	 * <p>
	 * With another reader, the checked input is the {@linkplain InputReader#value value}
	 * of the node.
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

	private static <T, X> ValueValidator<T, X> described(ValueValidator<T, X> validator,
			Supplier<ValueDescription> description, Function<InputReader<Object>, ValueValidator<Object, X>> untyped) {
		return new DescribedValueValidator<>(validator, description, untyped);
	}

	/**
	 * Returns the untyped counterpart of a combinator that treats {@code null}: the
	 * result of the given function, which additionally takes a null node of its reader as
	 * {@code null}.
	 */
	private static <X> Function<InputReader<Object>, ValueValidator<Object, X>> nullAware(
			Function<InputReader<Object>, ValueValidator<Object, X>> untyped) {
		return reader -> {
			final ValueValidator<Object, X> validator = untyped.apply(reader);
			return described((node, locale, constraintContext) -> validator.validate(UntypedInput.orNull(reader, node),
					locale, constraintContext), validator::description, nullAware(untyped));
		};
	}

	/**
	 * Returns the untyped counterpart of a validator of iterables: the node must be an
	 * array of the reader, and every element is validated with the untyped counterpart of
	 * the given validator.
	 */
	private static <N, A1, R, C extends Collection<R>> ValueValidator<N, C> untypedCollection(InputReader<N> reader,
			ValueValidator<? super A1, ? extends R> validator, Supplier<C> factory) {
		final ValueValidator<N, ? extends R> element = validator.untyped(reader);
		return described((node, locale, constraintContext) -> {
			final N array = UntypedInput.orNull(reader, node);
			if (array != null && !reader.isArray(array)) {
				return Validated.failureWith(UntypedInput.mismatch("array", reader, array, locale));
			}
			return Validated.traverseIndexed(array == null ? null : reader.elements(array),
					(value, index) -> element.validate(UntypedInput.orNull(reader, value), locale, constraintContext)
						.indexed(index),
					factory);
		}, () -> new ValueDescription.Elements(validator.description()),
				other -> untypedCollection(other, validator, factory));
	}

	/**
	 * Returns the untyped counterpart of a validator of maps: the node must be an object
	 * of the reader, and every value is validated with the untyped counterpart of the
	 * given validator.
	 */
	private static <N, T, X> ValueValidator<N, Map<String, X>> untypedMap(InputReader<N> reader,
			ValueValidator<T, X> validator) {
		final ValueValidator<N, X> element = validator.untyped(reader);
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
	private static <N, A1, R> ValueValidator<N, Optional<R>> untypedOptional(InputReader<N> reader,
			ValueValidator<? super A1, ? extends R> validator) {
		return described(
				UntypedInput.checked(reader, (Class<Optional<N>>) (Class<?>) Optional.class,
						ValueValidator.liftOptional(validator.untyped(reader))),
				() -> new ValueDescription.Optional(validator.description(), null),
				other -> untypedOptional(other, validator));
	}

	/**
	 * Returns a validator that always succeeds and returns the input unchanged.
	 * @param <X> target class
	 * @return value validator that always successes without validation
	 */
	static <X> ValueValidator<X, X> passThrough() {
		return (x, locale, constraintContext) -> Validated.of(Validation.success(x));
	}

	/**
	 * Returns a validator that maps the validated value using the given function.
	 * @param mapper function to transform the validated value
	 * @param <X2> the type after transformation
	 * @return a value validator that applies the mapping function after validation
	 * @deprecated Use {@link #map(Function)} instead.
	 */
	@Deprecated
	default <X2> ValueValidator<T, X2> andThen(Function<? super X, ? extends X2> mapper) {
		return this.map(mapper);
	}

	/**
	 * Returns a validator that maps the value produced on success using the given
	 * function. The mapper is not applied when validation fails.
	 * @param mapper function to transform the validated value
	 * @param <X2> the type after transformation
	 * @return a value validator that applies the mapping function after validation
	 * @since 0.17.0
	 */
	default <X2> ValueValidator<T, X2> map(Function<? super X, ? extends @Nullable X2> mapper) {
		return described((t, locale, constraintContext) -> ValueValidator.this.validate(t, locale, constraintContext)
			.map(mapper), this::description, reader -> this.untyped(reader).map(mapper));
	}

	/**
	 * Returns a validator that applies this validator and, if it succeeds, passes the
	 * produced value to the given validator. Violations of this validator are returned
	 * without invoking the next validator.
	 * @param validator the validator to apply to the validated value
	 * @param <X2> the type produced by the next validator
	 * @return a chained value validator
	 * @since 0.11.0
	 */
	default <X2> ValueValidator<T, X2> andThen(ValueValidator<? super X, X2> validator) {
		return described(
				(t, locale, constraintContext) -> ValueValidator.this.validate(t, locale, constraintContext)
					.flatMap(v -> validator.validate(v, locale, constraintContext)),
				this::description, reader -> this.untyped(reader).andThen(validator));
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
		return (a, locale, constraintContext) -> ValueValidator.this.validate(mapper.apply(a), locale,
				constraintContext);
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
	default ValueValidator<T, Optional<X>> optional() {
		return described(
				(t, locale, constraintContext) -> t == null ? Validated.successWith(Optional.empty())
						: ValueValidator.this.validate(t, locale, constraintContext).map(Optional::ofNullable),
				() -> new ValueDescription.Optional(this.description(), null),
				nullAware(reader -> this.untyped(reader).optional()));
	}

	/**
	 * Returns a validator that accepts a {@code null} input and produces {@code null} for
	 * it.
	 *
	 * <p>
	 * A {@code null} input succeeds with {@code null} without invoking this validator, so
	 * constraints such as {@code notNull()} are not applied to it. Any other input is
	 * validated by this validator.
	 * @return a value validator that produces {@code null} for a {@code null} input
	 * @since 0.18.0
	 * @see #optional()
	 */
	default ValueValidator<T, X> nullable() {
		return described(
				(t, locale, constraintContext) -> t == null ? Validated.successWith(null)
						: ValueValidator.this.validate(t, locale, constraintContext),
				() -> new ValueDescription.Optional(this.description(), null),
				nullAware(reader -> this.untyped(reader).nullable()));
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
	 */
	default ValueValidator<T, X> defaultValue(X defaultValue) {
		return described(
				(t, locale, constraintContext) -> t == null ? Validated.successWith(defaultValue)
						: ValueValidator.this.validate(t, locale, constraintContext),
				() -> new ValueDescription.Optional(this.description(), defaultValue),
				nullAware(reader -> this.untyped(reader).defaultValue(defaultValue)));
	}

	/**
	 * Returns a validator that tries the given validators in order and produces the value
	 * of the first one that succeeds.
	 *
	 * <p>
	 * If every alternative fails, the result holds one violation with the message key
	 * {@code object.anyOf} followed by the violations of all alternatives in the given
	 * order. The leading violation has an empty name, so it is reported on the validated
	 * value itself and takes the name of the property the validator is applied to.
	 * @param validators the alternatives, tried in the given order
	 * @param <T> the type of the object to be validated
	 * @param <X> the common type of the values produced by the alternatives
	 * @return a value validator that succeeds if any alternative succeeds
	 * @throws IllegalArgumentException if no alternative is given
	 * @since 0.18.0
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	static <T, X> ValueValidator<T, X> anyOf(ValueValidator<? super T, ? extends X>... validators) {
		if (validators.length == 0) {
			throw new IllegalArgumentException("'validators' must not be empty.");
		}
		final List<ValueValidator<? super T, ? extends X>> alternatives = List.of(validators);
		final Function<InputReader<Object>, ValueValidator<Object, X>> untyped = reader -> ValueValidator.anyOf(
				alternatives.stream().map(alternative -> alternative.untyped(reader)).toArray(ValueValidator[]::new));
		return ValueValidator.<T, X>described((t, locale, constraintContext) -> {
			final List<ConstraintViolation> violations = new ArrayList<>();
			for (ValueValidator<? super T, ? extends X> alternative : alternatives) {
				final Validated<? extends X> validated = alternative.validate(t, locale, constraintContext);
				if (validated.isValid()) {
					return Validated.successWith(validated.value());
				}
				violations.addAll(validated.errors());
			}
			violations.add(0,
					ConstraintViolation.builder()
						.name("")
						.messageKey(ViolationMessage.Default.OBJECT_ANY_OF.messageKey())
						.defaultMessageFormat(ViolationMessage.Default.OBJECT_ANY_OF.defaultMessageFormat())
						.argsWithPrependedNameAndAppendedViolatedValue(new Object[] { alternatives.size() },
								new ViolatedValue(t))
						.locale(locale)
						.build());
			return Validated.failureWith(violations);
		}, () -> new ValueDescription.AnyOf(alternatives.stream().map(ValueValidator::description).toList()), untyped);
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
	 * @param predicate the rule that the produced value must satisfy
	 * @param name the name of the reported violation
	 * @param violationMessage the message key and the default message format of the
	 * reported violation
	 * @return a value validator with the additional rule
	 * @since 0.18.0
	 */
	default ValueValidator<T, X> constraintOnTarget(Predicate<? super X> predicate, String name,
			ViolationMessage violationMessage) {
		return this.andThen((x, locale, constraintContext) -> predicate.test(x) ? Validated.successWith(x)
				: Validated.failureWith(ConstraintViolation.builder()
					.name(name)
					.messageKey(violationMessage.messageKey())
					.defaultMessageFormat(violationMessage.defaultMessageFormat())
					.argsWithPrependedNameAndAppendedViolatedValue(new Object[0], new ViolatedValue(x))
					.locale(locale)
					.build()));
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
	 * Returns a validator that appends {@code [index]} to every violation name, so that
	 * violations of elements in a collection can be told apart.
	 * @param index the index to append to violation names
	 * @return a validator with indexed violation names
	 * @since 0.8.0
	 */
	default ValueValidator<T, X> indexed(int index) {
		return described((t, locale, constraintContext) -> ValueValidator.this.validate(t, locale, constraintContext)
			.indexed(index), this::description, reader -> this.untyped(reader).indexed(index));
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
		return described((t, locale, constraintContext) -> ValueValidator.this.validate(t, locale, constraintContext)
			.prefixed(name), this::description, reader -> this.untyped(reader).prefixed(name));
	}

	/**
	 * Returns a validator that validates every element of the input iterable with this
	 * validator indexed by its position and collects the validated values into a
	 * collection created by the given factory. All elements are validated and the
	 * violations of all failing elements are accumulated.
	 * @param factory supplier of the collection that receives the validated values
	 * @param <C> the type of the resulting collection
	 * @return a value validator for iterables
	 */
	default <C extends Collection<X>> ValueValidator<Iterable<T>, C> liftCollection(Supplier<C> factory) {
		return ValueValidator.liftCollection(this, factory);
	}

	/**
	 * Returns a validator that validates every element of the input iterable with this
	 * validator indexed by its position and collects the validated values into a
	 * {@link List}.
	 * @return a value validator for iterables
	 */
	default ValueValidator<Iterable<T>, List<X>> liftList() {
		return ValueValidator.liftList(this);
	}

	/**
	 * Returns a validator that validates every element of the input iterable with this
	 * validator indexed by its position and collects the validated values into a
	 * {@link Set} that preserves the iteration order.
	 * @return a value validator for iterables
	 */
	default ValueValidator<Iterable<T>, Set<X>> liftSet() {
		return ValueValidator.liftSet(this);
	}

	/**
	 * Returns a validator that validates every value of the input map with this validator
	 * and collects the validated values into a {@link Map} with the same keys in
	 * iteration order. The violations of a value are located under its key and the
	 * violations of all failing values are accumulated. A {@code null} map validates
	 * successfully with an empty map.
	 * @return a value validator for maps
	 * @since 0.18.0
	 */
	default ValueValidator<Map<String, T>, Map<String, X>> liftMap() {
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
	 * An empty optional validates successfully.
	 * @return a value validator for optionals
	 */
	default ValueValidator<Optional<T>, Optional<X>> liftOptional() {
		return ValueValidator.liftOptional(this);
	}

	/**
	 * Returns a validator that validates every element of the input iterable with the
	 * given validator indexed by its position and collects the validated values into a
	 * collection created by the given factory.
	 * @param validator the validator applied to each element
	 * @param factory supplier of the collection that receives the validated values
	 * @param <A1> the element type of the input iterable
	 * @param <R> the type produced for each element
	 * @param <C> the type of the resulting collection
	 * @return a value validator for iterables
	 * @since 0.8.1
	 */
	static <A1, R, C extends Collection<R>> ValueValidator<Iterable<A1>, C> liftCollection(
			ValueValidator<? super A1, ? extends R> validator, Supplier<C> factory) {
		return described(
				(values, locale, constraintContext) -> Validated.traverseIndexed(values,
						(v, index) -> validator.indexed(index).validate(v, locale, constraintContext), factory),
				() -> new ValueDescription.Elements(validator.description()),
				reader -> untypedCollection(reader, validator, factory));
	}

	/**
	 * Returns a validator that validates every element of the input iterable with the
	 * given validator indexed by its position and collects the validated values into a
	 * {@link List}.
	 * @param validator the validator applied to each element
	 * @param <A1> the element type of the input iterable
	 * @param <R> the type produced for each element
	 * @return a value validator for iterables
	 * @since 0.8.1
	 */
	static <A1, R> ValueValidator<Iterable<A1>, List<R>> liftList(ValueValidator<? super A1, ? extends R> validator) {
		return ValueValidator.liftCollection(validator, ArrayList::new);

	}

	/**
	 * Returns a validator that validates every element of the input iterable with the
	 * given validator indexed by its position and collects the validated values into a
	 * {@link Set} that preserves the iteration order.
	 * @param validator the validator applied to each element
	 * @param <A1> the element type of the input iterable
	 * @param <R> the type produced for each element
	 * @return a value validator for iterables
	 * @since 0.8.1
	 */
	static <A1, R> ValueValidator<Iterable<A1>, Set<R>> liftSet(ValueValidator<? super A1, ? extends R> validator) {
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
	static <A1, R> ValueValidator<Optional<A1>, Optional<R>> liftOptional(
			ValueValidator<? super A1, ? extends R> validator) {
		return described(
				(value, locale, constraintContext) -> Validated.traverseOptional(value,
						v -> validator.validate(v, locale, constraintContext)),
				() -> new ValueDescription.Optional(validator.description(), null),
				reader -> untypedOptional(reader, validator));
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
