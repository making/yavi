/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

/**
 * Constraints on a {@link Double} value.
 *
 * <p>
 * A {@code null} value is considered valid and the checks are skipped. The comparisons
 * use the primitive {@code double} ordering, so {@link Double#NaN} satisfies none of the
 * bounds and is rejected by every check on this class.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.0.1
 */
public class DoubleConstraint<T> extends NumericConstraintBase<T, Double, DoubleConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public DoubleConstraint() {
	}

	@Override
	public DoubleConstraint<T> cast() {
		return this;
	}

	@Override
	protected Predicate<Double> isGreaterThan(Double min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Double> isGreaterThanOrEqual(Double min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Double> isLessThan(Double max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Double> isLessThanOrEqual(Double max) {
		return x -> x <= max;
	}

	@Override
	protected Double zeroValue() {
		return 0.0;
	}

}
