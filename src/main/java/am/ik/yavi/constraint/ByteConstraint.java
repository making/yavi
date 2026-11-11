/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

public class ByteConstraint<T> extends NumericConstraintBase<T, Byte, ByteConstraint<T>> {

	@Override
	public ByteConstraint<T> cast() {
		return this;
	}

	@Override
	protected Predicate<Byte> isGreaterThan(Byte min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Byte> isGreaterThanOrEqual(Byte min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Byte> isLessThan(Byte max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Byte> isLessThanOrEqual(Byte max) {
		return x -> x <= max;
	}

	@Override
	protected Byte zeroValue() {
		return 0;
	}

}
