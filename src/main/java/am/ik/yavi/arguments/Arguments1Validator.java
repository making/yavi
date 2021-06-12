/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.arguments;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValidatorSubset;
import am.ik.yavi.core.ValueValidator;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.3.0
 */
@FunctionalInterface
public interface Arguments1Validator<A1, X> extends ValueValidator<A1, X> {

	/**
	 * Convert {@link ValidatorSubset} instance into {@link Arguments1Validator}
	 *
	 * @param validator core validator
	 * @param <X> target class
	 * @return arguments1 validator
	 * @since 0.8.0
	 */
	static <X> Arguments1Validator<X, X> from(ValidatorSubset<X> validator) {
		return Arguments1Validator.from(validator.applicative());
	}

	/**
	 * Convert {@link ValueValidator} instance into {@link Arguments1Validator}
	 *
	 * @param valueValidator value validator
	 * @param <X> target class
	 * @return arguments1 validator
	 * @since 0.8.0
	 */
	static <X> Arguments1Validator<X, X> from(ValueValidator<X, X> valueValidator) {
		return valueValidator::validate;
	}

	/**
	 * @since 0.7.0
	 */
	@Override
	default <X2> Arguments1Validator<A1, X2> andThen(
			Function<? super X, ? extends X2> mapper) {
		return (a1, locale, constraintGroup) -> Arguments1Validator.this
				.validate(a1, locale, constraintGroup).map(mapper);
	}

	/**
	 * @since 0.7.0
	 */
	@Override
	default <A> Arguments1Validator<A, X> compose(
			Function<? super A, ? extends A1> mapper) {
		return (a, locale, constraintGroup) -> Arguments1Validator.this
				.validate(mapper.apply(a), locale, constraintGroup);
	}

	/**
	 * @since 0.7.0
	 */
	default <A2, Y> Arguments2Splitting<A1, A2, X, Y> split(
			ValueValidator<A2, Y> validator) {
		return new Arguments2Splitting<>(this, validator);
	}

	/**
	 * @since 0.7.0
	 */
	default <Y> Arguments2Combining<A1, X, Y> combine(ValueValidator<A1, Y> validator) {
		return new Arguments2Combining<>(this, validator);
	}

	/**
	 * @since 0.7.0
	 */
	@Override
	default Arguments1Validator<A1, X> indexed(int index) {
		return (a1, locale, constraintGroup) -> Arguments1Validator.this
				.validate(a1, locale, constraintGroup).indexed(index);
	}

	/**
	 * @since 0.8.0
	 */
	default <C extends Collection<X>> Arguments1Validator<Iterable<A1>, C> liftCollection(
			Supplier<C> factory) {
		return ArgumentsValidators.liftCollection(this, factory);
	}

	/**
	 * @since 0.8.0
	 */
	default Arguments1Validator<Iterable<A1>, List<X>> liftList() {
		return ArgumentsValidators.liftList(this);
	}

	/**
	 * @since 0.8.0
	 */
	default Arguments1Validator<Iterable<A1>, Set<X>> liftSet() {
		return ArgumentsValidators.liftSet(this);
	}

	/**
	 * @since 0.8.0
	 */
	default Arguments1Validator<Optional<A1>, Optional<X>> listOptional() {
		return ArgumentsValidators.liftOptional(this);
	}

	/**
	 * Use {@link #validate(Object)} instead
	 */
	@Deprecated
	default Validated<X> validateArgs(A1 a1) {
		return this.validate(a1);
	}

	/**
	 * Use {@link #validate(Object, ConstraintGroup)} instead
	 */
	@Deprecated
	default Validated<X> validateArgs(A1 a1, ConstraintGroup constraintGroup) {
		return this.validate(a1, constraintGroup);
	}

	/**
	 * Use {@link #validate(Object, Locale)} instead
	 */
	@Deprecated
	default Validated<X> validateArgs(A1 a1, Locale locale) {
		return this.validate(a1, locale);
	}

	/**
	 * Use {@link #validate(Object, Locale, ConstraintGroup)} instead
	 */
	@Deprecated
	default Validated<X> validateArgs(A1 a1, Locale locale,
			ConstraintGroup constraintGroup) {
		return this.validate(a1, locale, constraintGroup);
	}

	/**
	 * Consider using {@link #validate(Object, ConstraintGroup)} instead
	 */
	@Deprecated
	default void validateAndThrowIfInvalid(A1 a1, ConstraintGroup constraintGroup) {
		throw new UnsupportedOperationException(
				"validateAndThrowIfInvalid is not supported. Consider using validate method instead.");
	}

	/**
	 * Consider using {@link #validate(Object)} instead
	 */
	@Deprecated
	default void validateAndThrowIfInvalid(A1 a1) {
		this.validateAndThrowIfInvalid(a1, ConstraintGroup.DEFAULT);
	}
}
