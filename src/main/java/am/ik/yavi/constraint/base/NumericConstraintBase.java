/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.constraint.base;

import java.util.function.Predicate;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.*;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintPredicate;

public abstract class NumericConstraintBase<T, V, C extends Constraint<T, V, C>>
		extends ConstraintBase<T, V, C> {

	public C greaterThan(V min) {
		this.predicates().add(ConstraintPredicate.of(this.isGreaterThan(min),
				NUMERIC_GREATER_THAN, () -> new Object[] { min }, VALID));
		return cast();
	}

	public C greaterThanOrEqual(V min) {
		this.predicates().add(ConstraintPredicate.of(this.isGreaterThanOrEqual(min),
				NUMERIC_GREATER_THAN_OR_EQUAL, () -> new Object[] { min }, VALID));
		return cast();
	}

	public C lessThan(V max) {
		this.predicates().add(ConstraintPredicate.of(this.isLessThan(max),
				NUMERIC_LESS_THAN, () -> new Object[] { max }, VALID));
		return cast();
	}

	public C lessThanOrEqual(V max) {
		this.predicates().add(ConstraintPredicate.of(this.isLessThanOrEqual(max),
				NUMERIC_LESS_THAN_OR_EQUAL, () -> new Object[] { max }, VALID));
		return cast();
	}

	/**
	 * @since 0.10.0
	 */
	public C positive() {
		this.predicates().add(ConstraintPredicate.of(this.isGreaterThan(zeroValue()),
				NUMERIC_POSITIVE, () -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * @since 0.10.0
	 */
	public C positiveOrZero() {
		this.predicates()
				.add(ConstraintPredicate.of(this.isGreaterThanOrEqual(zeroValue()),
						NUMERIC_POSITIVE_OR_ZERO, () -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * @since 0.10.0
	 */
	public C negative() {
		this.predicates().add(ConstraintPredicate.of(this.isLessThan(zeroValue()),
				NUMERIC_NEGATIVE, () -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * @deprecated in favour of {@link NumericConstraintBase#negativeOrZero}
	 * @since 0.10.0
	 */
	@Deprecated
	public C negaitveOrZero() {
		this.predicates().add(ConstraintPredicate.of(this.isLessThanOrEqual(zeroValue()),
				NUMERIC_NEGATIVE_OR_ZERO, () -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * @since 0.10.0
	 */
	public C negativeOrZero() {
		this.predicates().add(ConstraintPredicate.of(this.isLessThanOrEqual(zeroValue()),
				NUMERIC_NEGATIVE_OR_ZERO, () -> new Object[] {}, VALID));
		return cast();
	}

	protected abstract Predicate<V> isGreaterThan(V min);

	protected abstract Predicate<V> isGreaterThanOrEqual(V min);

	protected abstract Predicate<V> isLessThan(V max);

	protected abstract Predicate<V> isLessThanOrEqual(V max);

	protected abstract V zeroValue();
}
