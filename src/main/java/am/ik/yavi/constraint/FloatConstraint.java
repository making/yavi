/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

public class FloatConstraint<T> extends NumericConstraintBase<T, Float, FloatConstraint<T>> {

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
