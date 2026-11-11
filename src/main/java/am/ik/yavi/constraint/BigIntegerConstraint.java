/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.math.BigInteger;
import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

public class BigIntegerConstraint<T> extends NumericConstraintBase<T, BigInteger, BigIntegerConstraint<T>> {

	@Override
	public BigIntegerConstraint<T> cast() {
		return this;
	}

	@Override
	protected Predicate<BigInteger> isGreaterThan(BigInteger min) {
		return x -> x.compareTo(min) > 0;
	}

	@Override
	protected Predicate<BigInteger> isGreaterThanOrEqual(BigInteger min) {
		return x -> x.compareTo(min) >= 0;
	}

	@Override
	protected Predicate<BigInteger> isLessThan(BigInteger max) {
		return x -> x.compareTo(max) < 0;
	}

	@Override
	protected Predicate<BigInteger> isLessThanOrEqual(BigInteger max) {
		return x -> x.compareTo(max) <= 0;
	}

	@Override
	protected BigInteger zeroValue() {
		return BigInteger.ZERO;
	}

}
