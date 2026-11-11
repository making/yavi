/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.base;

import java.util.function.Predicate;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.*;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintPredicate;

/**
 * Base class for constraints on numeric values.
 *
 * <p>
 * A {@code null} value is considered valid and the comparisons are skipped. The actual
 * comparison is delegated to the abstract predicate factories of the concrete value type.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <V> the numeric type the constraints apply to
 * @param <C> the concrete constraint type returned for method chaining
 * @since 0.0.2
 */
public abstract class NumericConstraintBase<T, V, C extends Constraint<T, V, C>> extends ConstraintBase<T, V, C> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public NumericConstraintBase() {
	}

	/**
	 * Requires the value to be greater than {@code min}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code numeric.greaterThan} and the
	 * default message {@code "{0}" must be greater than {1}}, where {@code {1}} is
	 * {@code min}.
	 * </p>
	 * @param min the exclusive lower bound
	 * @return this constraint
	 */
	public C greaterThan(V min) {
		this.predicates()
			.add(ConstraintPredicate.of(this.isGreaterThan(min), NUMERIC_GREATER_THAN, () -> new Object[] { min },
					VALID));
		return cast();
	}

	/**
	 * Requires the value to be greater than or equal to {@code min}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code numeric.greaterThanOrEqual} and
	 * the default message {@code "{0}" must be greater than or equal to {1}}, where
	 * {@code {1}} is {@code min}.
	 * </p>
	 * @param min the inclusive lower bound
	 * @return this constraint
	 */
	public C greaterThanOrEqual(V min) {
		this.predicates()
			.add(ConstraintPredicate.of(this.isGreaterThanOrEqual(min), NUMERIC_GREATER_THAN_OR_EQUAL,
					() -> new Object[] { min }, VALID));
		return cast();
	}

	/**
	 * Requires the value to be less than {@code max}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code numeric.lessThan} and the
	 * default message {@code "{0}" must be less than {1}}, where {@code {1}} is
	 * {@code max}.
	 * </p>
	 * @param max the exclusive upper bound
	 * @return this constraint
	 */
	public C lessThan(V max) {
		this.predicates()
			.add(ConstraintPredicate.of(this.isLessThan(max), NUMERIC_LESS_THAN, () -> new Object[] { max }, VALID));
		return cast();
	}

	/**
	 * Requires the value to be less than or equal to {@code max}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code numeric.lessThanOrEqual} and
	 * the default message {@code "{0}" must be less than or equal to {1}}, where
	 * {@code {1}} is {@code max}.
	 * </p>
	 * @param max the inclusive upper bound
	 * @return this constraint
	 */
	public C lessThanOrEqual(V max) {
		this.predicates()
			.add(ConstraintPredicate.of(this.isLessThanOrEqual(max), NUMERIC_LESS_THAN_OR_EQUAL,
					() -> new Object[] { max }, VALID));
		return cast();
	}

	/**
	 * Requires the value to be greater than zero.
	 *
	 * <p>
	 * A violation is reported with the message key {@code numeric.positive} and the
	 * default message {@code "{0}" must be positive}.
	 * </p>
	 * @return this constraint
	 * @since 0.10.0
	 */
	public C positive() {
		this.predicates()
			.add(ConstraintPredicate.of(this.isGreaterThan(zeroValue()), NUMERIC_POSITIVE, () -> new Object[] {},
					VALID));
		return cast();
	}

	/**
	 * Requires the value to be greater than or equal to zero.
	 *
	 * <p>
	 * A violation is reported with the message key {@code numeric.positiveOrZero} and the
	 * default message {@code "{0}" must be positive or zero}.
	 * </p>
	 * @return this constraint
	 * @since 0.10.0
	 */
	public C positiveOrZero() {
		this.predicates()
			.add(ConstraintPredicate.of(this.isGreaterThanOrEqual(zeroValue()), NUMERIC_POSITIVE_OR_ZERO,
					() -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * Requires the value to be less than zero.
	 *
	 * <p>
	 * A violation is reported with the message key {@code numeric.negative} and the
	 * default message {@code "{0}" must be negative}.
	 * </p>
	 * @return this constraint
	 * @since 0.10.0
	 */
	public C negative() {
		this.predicates()
			.add(ConstraintPredicate.of(this.isLessThan(zeroValue()), NUMERIC_NEGATIVE, () -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * Requires the value to be less than or equal to zero.
	 *
	 * <p>
	 * A violation is reported with the message key {@code numeric.negativeOrZero} and the
	 * default message {@code "{0}" must be negative or zero}.
	 * </p>
	 * @return this constraint
	 * @since 0.10.0
	 */
	public C negativeOrZero() {
		this.predicates()
			.add(ConstraintPredicate.of(this.isLessThanOrEqual(zeroValue()), NUMERIC_NEGATIVE_OR_ZERO,
					() -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * Returns a predicate that tests whether a value is greater than {@code min}.
	 * @param min the exclusive lower bound
	 * @return the predicate
	 */
	protected abstract Predicate<V> isGreaterThan(V min);

	/**
	 * Returns a predicate that tests whether a value is greater than or equal to
	 * {@code min}.
	 * @param min the inclusive lower bound
	 * @return the predicate
	 */
	protected abstract Predicate<V> isGreaterThanOrEqual(V min);

	/**
	 * Returns a predicate that tests whether a value is less than {@code max}.
	 * @param max the exclusive upper bound
	 * @return the predicate
	 */
	protected abstract Predicate<V> isLessThan(V max);

	/**
	 * Returns a predicate that tests whether a value is less than or equal to
	 * {@code max}.
	 * @param max the inclusive upper bound
	 * @return the predicate
	 */
	protected abstract Predicate<V> isLessThanOrEqual(V max);

	/**
	 * Returns the zero value of the numeric type.
	 * @return the zero value
	 */
	protected abstract V zeroValue();

}
