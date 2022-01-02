/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import am.ik.yavi.fn.Validation;

/**
 * @since 0.8.0
 */
@FunctionalInterface
public interface ValueValidator<T, X> {

	Validated<X> validate(T t, Locale locale, ConstraintGroup constraintGroup);

	/**
	 * Return {@link ValueValidator} instance that always successes without validation.
	 *
	 * @param <X> target class
	 * @return value validator that always successes without validation
	 */
	static <X> ValueValidator<X, X> passThrough() {
		return (x, locale, constraintGroup) -> Validated.of(Validation.success(x));
	}

	default <X2> ValueValidator<T, X2> andThen(Function<? super X, ? extends X2> mapper) {
		return (t, locale, constraintGroup) -> ValueValidator.this
				.validate(t, locale, constraintGroup).map(mapper);
	}

	default <A> ValueValidator<A, X> compose(Function<? super A, ? extends T> mapper) {
		return (a, locale, constraintGroup) -> ValueValidator.this
				.validate(mapper.apply(a), locale, constraintGroup);
	}

	default Validated<X> validate(T t) {
		return this.validate(t, Locale.getDefault(), ConstraintGroup.DEFAULT);
	}

	default Validated<X> validate(T t, ConstraintGroup constraintGroup) {
		return this.validate(t, Locale.getDefault(), constraintGroup);
	}

	default Validated<X> validate(T t, Locale locale) {
		return this.validate(t, locale, ConstraintGroup.DEFAULT);
	}

	default X validated(T t) throws ConstraintViolationsException {
		return this.validate(t).orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(T t, ConstraintGroup constraintGroup)
			throws ConstraintViolationsException {
		return this.validate(t, constraintGroup)
				.orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(T t, Locale locale) throws ConstraintViolationsException {
		return this.validate(t, locale).orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(T t, Locale locale, ConstraintGroup constraintGroup)
			throws ConstraintViolationsException {
		return this.validate(t, locale, constraintGroup)
				.orElseThrow(ConstraintViolationsException::new);
	}

	default ValueValidator<T, X> indexed(int index) {
		return (t, locale, constraintGroup) -> ValueValidator.this
				.validate(t, locale, constraintGroup).indexed(index);
	}

	default <C extends Collection<X>> ValueValidator<Iterable<T>, C> liftCollection(
			Supplier<C> factory) {
		return (values, locale, constraintGroup) -> Validated.traverseIndexed(values,
				(v, index) -> this.indexed(index).validate(v, locale, constraintGroup),
				factory);
	}

	default ValueValidator<Iterable<T>, List<X>> liftList() {
		return ValueValidator.liftList(this);
	}

	default ValueValidator<Iterable<T>, Set<X>> liftSet() {
		return ValueValidator.liftSet(this);
	}

	default ValueValidator<Optional<T>, Optional<X>> liftOptional() {
		return ValueValidator.liftOptional(this);
	}

	/**
	 * @since 0.8.1
	 */
	static <A1, R, C extends Collection<R>> ValueValidator<Iterable<A1>, C> liftCollection(
			ValueValidator<? super A1, ? extends R> validator, Supplier<C> factory) {
		return (values, locale, constraintGroup) -> Validated.traverseIndexed(values, (v,
				index) -> validator.indexed(index).validate(v, locale, constraintGroup),
				factory);
	}

	/**
	 * @since 0.8.1
	 */
	static <A1, R> ValueValidator<Iterable<A1>, List<R>> liftList(
			ValueValidator<? super A1, ? extends R> validator) {
		return ValueValidator.liftCollection(validator, ArrayList::new);

	}

	/**
	 * @since 0.8.1
	 */
	static <A1, R> ValueValidator<Iterable<A1>, Set<R>> liftSet(
			ValueValidator<? super A1, ? extends R> validator) {
		// Since Index is attached to the name of ConstraintViolation,
		// we need a consistent order. This is why we use LinkedHashSet.
		return ValueValidator.liftCollection(validator, LinkedHashSet::new);
	}

	/**
	 * @since 0.8.1
	 */
	static <A1, R> ValueValidator<Optional<A1>, Optional<R>> liftOptional(
			ValueValidator<? super A1, ? extends R> validator) {
		return (value, locale, constraintGroup) -> Validated.traverseOptional(value,
				v -> validator.validate(v, locale, constraintGroup));
	}
}
