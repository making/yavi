/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.math.BigDecimal;
import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

public class BigDecimalConstraint<T> extends NumericConstraintBase<T, BigDecimal, BigDecimalConstraint<T>> {

	@Override
	public BigDecimalConstraint<T> cast() {
		return this;
	}

	@Override
	protected Predicate<BigDecimal> isGreaterThan(BigDecimal min) {
		return x -> x.compareTo(min) > 0;
	}

	@Override
	protected Predicate<BigDecimal> isGreaterThanOrEqual(BigDecimal min) {
		return x -> x.compareTo(min) >= 0;
	}

	@Override
	protected Predicate<BigDecimal> isLessThan(BigDecimal max) {
		return x -> x.compareTo(max) < 0;
	}

	@Override
	protected Predicate<BigDecimal> isLessThanOrEqual(BigDecimal max) {
		return x -> x.compareTo(max) <= 0;
	}

	@Override
	protected BigDecimal zeroValue() {
		return BigDecimal.ZERO;
	}

}
