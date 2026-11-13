/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.math.BigDecimal;
import java.util.function.Predicate;

import am.ik.yavi.constraint.base.ExactNumericConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.NUMERIC_FRACTION_DIGITS;
import static am.ik.yavi.core.ViolationMessage.Default.NUMERIC_INTEGER_DIGITS;

/**
 * Constraints on a {@link BigDecimal} value.
 *
 * <p>
 * A {@code null} value is considered valid and the checks are skipped. The comparisons
 * use {@link BigDecimal#compareTo(BigDecimal)} and therefore ignore the scale: for
 * example {@code 1.0} and {@code 1.00} are considered equal. So do the constraints on the
 * number of digits, which do not count trailing zeros of the fraction.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.0.1
 */
public class BigDecimalConstraint<T> extends ExactNumericConstraintBase<T, BigDecimal, BigDecimalConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public BigDecimalConstraint() {
	}

	@Override
	public BigDecimalConstraint<T> cast() {
		return this;
	}

	/**
	 * Requires the value to have at most {@code max} digits after the decimal point, that
	 * is to be a multiple of {@code 10}<sup>{@code -max}</sup>. Trailing zeros are not
	 * counted: {@code 1.50} has one fraction digit and {@code 2E+2} has none.
	 *
	 * <p>
	 * A violation is reported with the message key {@code numeric.fractionDigits} and the
	 * default message {@code "{0}" must have at most {1} fraction digits}, where
	 * {@code {1}} is {@code max}.
	 * </p>
	 *
	 * <p>
	 * The scale of the value is not changed; set it with {@code preprocess} of the
	 * validator or {@code map} if the produced value must have a fixed scale.
	 * </p>
	 * @param max the largest number of fraction digits
	 * @return this constraint
	 * @throws IllegalArgumentException if {@code max} is negative
	 * @since 0.18.0
	 */
	public BigDecimalConstraint<T> fractionDigits(int max) {
		requireNotNegative(max);
		this.predicates()
			.add(ConstraintPredicate.of(x -> x.stripTrailingZeros().scale() <= max, NUMERIC_FRACTION_DIGITS,
					() -> new Object[] { max }, VALID));
		return this;
	}

	/**
	 * Requires the value to have at most {@code max} digits before the decimal point,
	 * that is an absolute value less than {@code 10}<sup>{@code max}</sup>. The zero
	 * before the decimal point of {@code 0.5} is not counted.
	 *
	 * <p>
	 * A violation is reported with the message key {@code numeric.integerDigits} and the
	 * default message {@code "{0}" must have at most {1} integer digits}, where
	 * {@code {1}} is {@code max}.
	 * </p>
	 * @param max the largest number of integer digits
	 * @return this constraint
	 * @throws IllegalArgumentException if {@code max} is negative
	 * @since 0.18.0
	 */
	public BigDecimalConstraint<T> integerDigits(int max) {
		requireNotNegative(max);
		// precision - scale is the position of the most significant digit
		this.predicates()
			.add(ConstraintPredicate.of(x -> x.signum() == 0 || (long) x.precision() - x.scale() <= max,
					NUMERIC_INTEGER_DIGITS, () -> new Object[] { max }, VALID));
		return this;
	}

	private static void requireNotNegative(int max) {
		if (max < 0) {
			throw new IllegalArgumentException("'max' must not be negative.");
		}
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
	protected Predicate<BigDecimal> isMultipleOf(BigDecimal divisor) {
		return x -> x.remainder(divisor).signum() == 0;
	}

	@Override
	protected BigDecimal zeroValue() {
		return BigDecimal.ZERO;
	}

}
