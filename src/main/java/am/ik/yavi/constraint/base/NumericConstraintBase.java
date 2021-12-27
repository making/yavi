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
	 * Is the given value positve. Positive means it is greater than 0.
	 *
	 * <pre>
	 *     0 -> false
	 *     1 -> true
	 *     n where n > 0 -> true
	 *     n where n < 0 -> false
	 * </pre>
	 *
	 * @since 0.10.0
	 */
	public C positive() {
		this.predicates().add(ConstraintPredicate.of(this.isGreaterThan(zeroValue()),
				NUMERIC_POSITIVE, () -> new Object[] {}, VALID));
		return cast();
	}

	/** @since 0.10.0 */
	public C positiveOrZero() {
		this.predicates()
				.add(ConstraintPredicate.of(this.isGreaterThanOrEqual(zeroValue()),
						NUMERIC_POSITIVE_OR_ZERO, () -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * Is the given value negative. Negative means it is less than 0.
	 *
	 * <pre>
	 *     0 -> false
	 *     1 -> false
	 *     -1 -> true
	 *     n where n > 0 -> false
	 *     n where n < 0 -> true
	 * </pre>
	 *
	 * @since 0.10.0
	 */
	public C negative() {
		this.predicates().add(ConstraintPredicate.of(this.isLessThan(zeroValue()),
				NUMERIC_NEGATIVE, () -> new Object[] {}, VALID));
		return cast();
	}

	/** @since 0.10.0 */
	public C negaitveOrZero() {
		this.predicates().add(ConstraintPredicate.of(this.isLessThanOrEqual(zeroValue()),
				NUMERIC_NEGATIVE_OR_ZERO, () -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * Is the given value equal to the zero representation of its type. The exact
	 * representation of <i>zero</i> can be found in {@link #zeroValue()}.
	 */
	public C isZero() {
		return this.equalTo(zeroValue());
	}

	/**
	 * Is the given value equal to the <i>one</i> representation of its type The exact
	 * representation of <i>one</i> can be found in {@link #oneValue()}.
	 */
	public C isOne() {
		return this.equalTo(oneValue());
	}

	protected abstract Predicate<V> isGreaterThan(V min);

	protected abstract Predicate<V> isGreaterThanOrEqual(V min);

	protected abstract Predicate<V> isLessThan(V max);

	protected abstract Predicate<V> isLessThanOrEqual(V max);

	/**
	 * The value that represents zero
	 *
	 * @return the numeric value zero
	 */
	protected abstract V zeroValue();

	/**
	 * The value that represents one
	 *
	 * @return the numeric value one
	 */
	protected abstract V oneValue();
}
