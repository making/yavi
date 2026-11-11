/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

/**
 * Constraints on a {@link Byte} value.
 *
 * <p>
 * A {@code null} value is considered valid and the checks are skipped. The comparisons
 * use the primitive {@code byte} ordering.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.0.1
 */
public class ByteConstraint<T> extends NumericConstraintBase<T, Byte, ByteConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public ByteConstraint() {
	}

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
