/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.BOOLEAN_IS_FALSE;
import static am.ik.yavi.core.ViolationMessage.Default.BOOLEAN_IS_TRUE;

import am.ik.yavi.constraint.base.ConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

/**
 * Constraints on a {@link Boolean} value.
 *
 * <p>
 * A {@code null} value is considered valid and the checks are skipped.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.0.1
 */
public class BooleanConstraint<T> extends ConstraintBase<T, Boolean, BooleanConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public BooleanConstraint() {
	}

	@Override
	public BooleanConstraint<T> cast() {
		return this;
	}

	/**
	 * Requires the value to be {@code false}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code boolean.isFalse} and the
	 * default message {@code "{0}" must be false}.
	 * </p>
	 * @return this constraint
	 */
	public BooleanConstraint<T> isFalse() {
		this.predicates().add(ConstraintPredicate.of(x -> !x, BOOLEAN_IS_FALSE, () -> new Object[] {}, VALID));
		return this;
	}

	/**
	 * Requires the value to be {@code true}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code boolean.isTrue} and the default
	 * message {@code "{0}" must be true}.
	 * </p>
	 * @return this constraint
	 */
	public BooleanConstraint<T> isTrue() {
		this.predicates().add(ConstraintPredicate.of(x -> x, BOOLEAN_IS_TRUE, () -> new Object[] {}, VALID));
		return this;
	}

}
