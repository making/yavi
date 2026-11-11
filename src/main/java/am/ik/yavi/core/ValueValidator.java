/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import am.ik.yavi.fn.Validation;
import am.ik.yavi.jsr305.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
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
 * collections or optionals with {@code lift*} methods. The generated
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
	default <X2> ValueValidator<T, X2> map(Function<? super X, ? extends X2> mapper) {
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
	default <A> ValueValidator<A, X> compose(Function<? super A, ? extends T> mapper) {
		return (a, locale, constraintContext) -> ValueValidator.this.validate(mapper.apply(a), locale,
				constraintContext);
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
