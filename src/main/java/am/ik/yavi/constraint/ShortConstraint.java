/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

/**
 * Constraints on a {@link Short} value.
 *
 * <p>
 * A {@code null} value is considered valid and the checks are skipped. The comparisons
 * use the primitive {@code short} ordering.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.0.1
 */
public class ShortConstraint<T> extends NumericConstraintBase<T, Short, ShortConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public ShortConstraint() {
	}

	@Override
	public ShortConstraint<T> cast() {
		return this;
	}

	@Override
	protected Predicate<Short> isGreaterThan(Short min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Short> isGreaterThanOrEqual(Short min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Short> isLessThan(Short max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Short> isLessThanOrEqual(Short max) {
		return x -> x <= max;
	}

	@Override
	protected Short zeroValue() {
		return 0;
	}

}
