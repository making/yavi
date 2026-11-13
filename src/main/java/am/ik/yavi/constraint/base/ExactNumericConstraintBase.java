/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.base;

import java.util.function.Predicate;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.NUMERIC_MULTIPLE_OF;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintPredicate;

/**
 * Base class for constraints on numeric values that are represented exactly: the integral
 * types, {@link java.math.BigInteger} and {@link java.math.BigDecimal}.
 *
 * <p>
 * It adds the constraints that need an exact remainder to those of
 * {@link NumericConstraintBase}. {@code Double} and {@code Float} do not have them: the
 * remainder of a binary fraction is not exact, so {@code 0.3} is not a multiple of
 * {@code 0.1} there.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <V> the numeric type the constraints apply to
 * @param <C> the concrete constraint type returned for method chaining
 * @since 0.18.0
 */
public abstract class ExactNumericConstraintBase<T, V, C extends Constraint<T, V, C>>
		extends NumericConstraintBase<T, V, C> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public ExactNumericConstraintBase() {
	}

	/**
	 * Requires the value to be a multiple of {@code divisor}, that is to leave no
	 * remainder when divided by it. Zero is a multiple of every divisor.
	 *
	 * <p>
	 * A violation is reported with the message key {@code numeric.multipleOf} and the
	 * default message {@code "{0}" must be a multiple of {1}}, where {@code {1}} is
	 * {@code divisor}.
	 * </p>
	 * @param divisor the number that divides the value, greater than zero
	 * @return this constraint
	 * @throws IllegalArgumentException if {@code divisor} is not greater than zero
	 */
	public C multipleOf(V divisor) {
		if (!this.isGreaterThan(this.zeroValue()).test(divisor)) {
			throw new IllegalArgumentException("'divisor' must be greater than zero.");
		}
		this.predicates()
			.add(ConstraintPredicate.of(this.isMultipleOf(divisor), NUMERIC_MULTIPLE_OF, () -> new Object[] { divisor },
					VALID));
		return cast();
	}

	/**
	 * Returns a predicate that tests whether a value is a multiple of {@code divisor}.
	 * @param divisor the number that divides the value, greater than zero
	 * @return the predicate
	 */
	protected abstract Predicate<V> isMultipleOf(V divisor);

}
