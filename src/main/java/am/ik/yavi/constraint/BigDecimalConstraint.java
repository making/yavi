/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.math.BigDecimal;
import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

/**
 * Constraints on a {@link BigDecimal} value.
 *
 * <p>
 * A {@code null} value is considered valid and the checks are skipped. The comparisons
 * use {@link BigDecimal#compareTo(BigDecimal)} and therefore ignore the scale: for
 * example {@code 1.0} and {@code 1.00} are considered equal.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.0.1
 */
public class BigDecimalConstraint<T> extends NumericConstraintBase<T, BigDecimal, BigDecimalConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public BigDecimalConstraint() {
	}

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
