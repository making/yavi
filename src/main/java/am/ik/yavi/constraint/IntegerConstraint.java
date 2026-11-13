/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.ExactNumericConstraintBase;

/**
 * Constraints on an {@link Integer} value.
 *
 * <p>
 * A {@code null} value is considered valid and the checks are skipped. The comparisons
 * use the primitive {@code int} ordering.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.0.1
 */
public class IntegerConstraint<T> extends ExactNumericConstraintBase<T, Integer, IntegerConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public IntegerConstraint() {
	}

	@Override
	public IntegerConstraint<T> cast() {
		return this;
	}

	@Override
	protected Predicate<Integer> isGreaterThan(Integer min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Integer> isGreaterThanOrEqual(Integer min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Integer> isLessThan(Integer max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Integer> isLessThanOrEqual(Integer max) {
		return x -> x <= max;
	}

	@Override
	protected Predicate<Integer> isMultipleOf(Integer divisor) {
		return x -> x % divisor == 0;
	}

	@Override
	protected Integer zeroValue() {
		return 0;
	}

}
