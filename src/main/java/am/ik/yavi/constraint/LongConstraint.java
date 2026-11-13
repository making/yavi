/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.ExactNumericConstraintBase;

/**
 * Constraints on a {@link Long} value.
 *
 * <p>
 * A {@code null} value is considered valid and the checks are skipped. The comparisons
 * use the primitive {@code long} ordering.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.0.1
 */
public class LongConstraint<T> extends ExactNumericConstraintBase<T, Long, LongConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public LongConstraint() {
	}

	@Override
	public LongConstraint<T> cast() {
		return this;
	}

	@Override
	protected Predicate<Long> isGreaterThan(Long min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Long> isGreaterThanOrEqual(Long min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Long> isLessThan(Long max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Long> isLessThanOrEqual(Long max) {
		return x -> x <= max;
	}

	@Override
	protected Predicate<Long> isMultipleOf(Long divisor) {
		return x -> x % divisor == 0;
	}

	@Override
	protected Long zeroValue() {
		return 0L;
	}

}
