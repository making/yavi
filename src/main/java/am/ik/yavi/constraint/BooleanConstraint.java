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

public class BooleanConstraint<T> extends ConstraintBase<T, Boolean, BooleanConstraint<T>> {

	@Override
	public BooleanConstraint<T> cast() {
		return this;
	}

	public BooleanConstraint<T> isFalse() {
		this.predicates().add(ConstraintPredicate.of(x -> !x, BOOLEAN_IS_FALSE, () -> new Object[] {}, VALID));
		return this;
	}

	public BooleanConstraint<T> isTrue() {
		this.predicates().add(ConstraintPredicate.of(x -> x, BOOLEAN_IS_TRUE, () -> new Object[] {}, VALID));
		return this;
	}

}
