/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

public class LongConstraint<T> extends NumericConstraintBase<T, Long, LongConstraint<T>> {

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
	protected Long zeroValue() {
		return 0L;
	}

}
