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
 * produced value is added with {@link #constraintOnTarget}. The generated
 * {@code am.ik.yavi.arguments.ArgumentsNValidator} types extend this interface to
 * validate constructor arguments and create only valid objects.
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
		return (t, locale, constraintContext) -> ValueValidator.this.validate(t, locale, constraintContext).map(mapper);
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
		return (t, locale, constraintContext) -> ValueValidator.this.validate(t, locale, constraintContext)
			.flatMap(v -> validator.validate(v, locale, constraintContext));
	}

	/**
	 * Returns a validator that applies the given function to the input before validating
	 * it. The function is not validated and its result is passed to this validator.
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
		return (t, locale, constraintContext) -> t == null ? Validated.successWith(Optional.empty())
				: ValueValidator.this.validate(t, locale, constraintContext).map(Optional::ofNullable);
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
		return (t, locale, constraintContext) -> t == null ? Validated.successWith(null)
				: ValueValidator.this.validate(t, locale, constraintContext);
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
		return (t, locale, constraintContext) -> t == null ? Validated.successWith(defaultValue)
				: ValueValidator.this.validate(t, locale, constraintContext);
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
	static <T, X> ValueValidator<T, X> anyOf(ValueValidator<? super T, ? extends X>... validators) {
		if (validators.length == 0) {
			throw new IllegalArgumentException("'validators' must not be empty.");
		}
		final List<ValueValidator<? super T, ? extends X>> alternatives = List.of(validators);
		return (t, locale, constraintContext) -> {
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
		};
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
		return (t, locale, constraintContext) -> ValueValidator.this.validate(t, locale, constraintContext)
			.indexed(index);
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
		return (t, locale, constraintContext) -> ValueValidator.this.validate(t, locale, constraintContext)
			.prefixed(name);
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
		return (values, locale, constraintContext) -> Validated.traverseIndexed(values,
				(v, index) -> this.indexed(index).validate(v, locale, constraintContext), factory);
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
		return (values, locale, constraintContext) -> {
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
		};
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
		return (values, locale, constraintContext) -> Validated.traverseIndexed(values,
				(v, index) -> validator.indexed(index).validate(v, locale, constraintContext), factory);
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
		return (value, locale, constraintContext) -> Validated.traverseOptional(value,
				v -> validator.validate(v, locale, constraintContext));
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
