/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

/**
 * Constraints on a {@link Float} value.
 *
 * <p>
 * A {@code null} value is considered valid and the checks are skipped. The comparisons
 * use the primitive {@code float} ordering, so {@link Float#NaN} satisfies none of the
 * bounds and is rejected by every check on this class.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.0.1
 */
public class FloatConstraint<T> extends NumericConstraintBase<T, Float, FloatConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public FloatConstraint() {
	}

	@Override
	public FloatConstraint<T> cast() {
		return this;
	}

	@Override
	protected Predicate<Float> isGreaterThan(Float min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Float> isGreaterThanOrEqual(Float min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Float> isLessThan(Float max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Float> isLessThanOrEqual(Float max) {
		return x -> x <= max;
	}

	@Override
	protected Float zeroValue() {
		return 0f;
	}

}
