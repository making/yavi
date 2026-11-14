/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.ReversibleValueValidator;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * An {@link Arguments1Validator} that knows its reverse by its type, as a
 * {@link ReversibleValueValidator} does. This is the type of the validators of single
 * values that the builders build without a mapper, such as
 * {@link StringValidator.Reversible}: they can be combined as validators of an argument
 * and given where a reversible validator is needed.
 *
 * <p>
 * The validators derived with {@link #named(String)},
 * {@link #describedAs(ValueDescription)}, {@link #preprocess(Function)},
 * {@link #constraintOnTarget} and the lifts are of this type; the other methods of
 * {@link ReversibleValueValidator} return what they state there, and those of
 * {@link Arguments1Validator} that change the value, such as {@link #map(Function)} and
 * {@link #andThen(ValueValidator)}, return a validator that does not know its reverse.
 *
 * @param <A1> the type of the argument
 * @param <X> the type of the validated and transformed result
 * @since 0.18.0
 */
public interface ReversibleArguments1Validator<A1, X extends @Nullable Object>
		extends Arguments1Validator<A1, X>, ReversibleValueValidator<A1, X> {

	/**
	 * Returns the given reversible validator as a validator of an argument.
	 * @param validator the validator
	 * @param <A1> the type of the argument
	 * @param <X> the type of the validated and transformed result
	 * @return a validator of an argument that validates and goes back like the given one
	 */
	static <A1, X extends @Nullable Object> ReversibleArguments1Validator<A1, X> from(
			ReversibleValueValidator<A1, X> validator) {
		return new ReversibleArguments1Validator<A1, X>() {
			@Override
			public Validated<X> validate(@Nullable A1 a1, Locale locale, ConstraintContext constraintContext) {
				return validator.validate(a1, locale, constraintContext);
			}

			@Override
			public @Nullable A1 reverse(X value) {
				return validator.reverse(value);
			}

			@Override
			public ValueDescription description() {
				return validator.description();
			}

			@Override
			public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
				return validator.readingWith(reader);
			}

			@Override
			public ReversibleArguments1Validator<A1, X> describedAs(ValueDescription description) {
				return from(validator.describedAs(description));
			}

			@Override
			public ReversibleArguments1Validator<A1, X> preprocess(
					Function<? super A1, ? extends @Nullable A1> transform) {
				return from(validator.preprocess(transform));
			}

			@Override
			public ReversibleArguments1Validator<A1, X> named(String name) {
				return from(validator.named(name));
			}

			@Override
			public ReversibleArguments1Validator<A1, X> constraintOnTarget(Predicate<? super X> predicate, String name,
					ViolationMessage violationMessage) {
				return from(validator.constraintOnTarget(predicate, name, violationMessage));
			}
		};
	}

	@Override
	default ReversibleArguments1Validator<A1, X> describedAs(ValueDescription description) {
		return from(ReversibleValueValidator.super.describedAs(description));
	}

	@Override
	default ReversibleArguments1Validator<A1, X> preprocess(Function<? super A1, ? extends @Nullable A1> transform) {
		return from(ReversibleValueValidator.super.preprocess(transform));
	}

	@Override
	default ReversibleArguments1Validator<A1, X> named(String name) {
		return from(ReversibleValueValidator.super.named(name));
	}

	@Override
	default ReversibleArguments1Validator<A1, X> constraintOnTarget(Predicate<? super X> predicate, String name,
			ViolationMessage violationMessage) {
		return from(ReversibleValueValidator.super.constraintOnTarget(predicate, name, violationMessage));
	}

	@Override
	default <C extends Collection<X>> ReversibleArguments1Validator<Iterable<? extends A1>, C> liftCollection(
			Supplier<C> factory) {
		return from(ReversibleValueValidator.super.liftCollection(factory));
	}

	@Override
	default ReversibleArguments1Validator<Iterable<? extends A1>, List<X>> liftList() {
		return from(ReversibleValueValidator.super.liftList());
	}

	@Override
	default ReversibleArguments1Validator<Iterable<? extends A1>, Set<X>> liftSet() {
		return from(ReversibleValueValidator.super.liftSet());
	}

	@Override
	default ReversibleArguments1Validator<Optional<? extends A1>, Optional<@NonNull X>> liftOptional() {
		return from(ReversibleValueValidator.super.liftOptional());
	}

}
