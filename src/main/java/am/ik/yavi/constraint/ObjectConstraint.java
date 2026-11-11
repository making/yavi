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

/**
 * Constraints on an object value.
 *
 * <p>
 * In addition to the general constraints such as {@code notNull}, {@code equalTo} and
 * {@code oneOf}, this class can add password policy constraints with
 * {@link #password(Function)}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <E> the type of the value the constraints apply to
 * @since 0.0.1
 */
public class ObjectConstraint<T, E> extends ConstraintBase<T, E, ObjectConstraint<T, E>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public ObjectConstraint() {
	}

	@Override
	public ObjectConstraint<T, E> cast() {
		return this;
	}

	/**
	 * Adds the password policy constraints built by the given builder.
	 *
	 * <p>
	 * A violation is reported with one of the message keys of the used policy, for
	 * example {@code password.required}.
	 * </p>
	 * @param builder the function that configures the password policies
	 * @return this constraint
	 * @since 0.7.0
	 */
	public ObjectConstraint<T, E> password(
			Function<ObjectPasswordPoliciesBuilder<T, E>, List<ConstraintPredicate<E>>> builder) {
		final List<ConstraintPredicate<E>> predicates = builder.apply(new ObjectPasswordPoliciesBuilder<>());
		this.predicates().addAll(predicates);
		return this;
	}

}
