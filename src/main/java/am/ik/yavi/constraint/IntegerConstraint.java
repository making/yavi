/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

public class IntegerConstraint<T> extends NumericConstraintBase<T, Integer, IntegerConstraint<T>> {

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
	protected Integer zeroValue() {
		return 0;
	}

}
