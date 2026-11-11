/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.List;
import java.util.function.Function;

import am.ik.yavi.constraint.base.ConstraintBase;
import am.ik.yavi.constraint.password.ObjectPasswordPoliciesBuilder;
import am.ik.yavi.core.ConstraintPredicate;

public class ObjectConstraint<T, E> extends ConstraintBase<T, E, ObjectConstraint<T, E>> {

	@Override
	public ObjectConstraint<T, E> cast() {
		return this;
	}

	/**
	 * @since 0.7.0
	 */
	public ObjectConstraint<T, E> password(
			Function<ObjectPasswordPoliciesBuilder<T, E>, List<ConstraintPredicate<E>>> builder) {
		final List<ConstraintPredicate<E>> predicates = builder.apply(new ObjectPasswordPoliciesBuilder<>());
		this.predicates().addAll(predicates);
		return this;
	}

}
