/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

public class DoubleConstraint<T> extends NumericConstraintBase<T, Double, DoubleConstraint<T>> {

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
