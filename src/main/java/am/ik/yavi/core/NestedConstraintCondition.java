/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.function.Function;

import org.jspecify.annotations.Nullable;

/**
 * A {@link ConstraintCondition} that applies another condition to a value nested in the
 * target object.
 *
 * <p>
 * The nested value is extracted with the given function. If the extracted value is
 * {@code null}, the condition is not satisfied. Instances are created by
 * {@link am.ik.yavi.builder.ValidatorBuilder} when a conditional validator is nested in
 * another validator.
 *
 * @param <T> the type of the target object
 * @param <N> the type of the nested value
 * @since 0.2.4
 */
public class NestedConstraintCondition<T, N> implements ConstraintCondition<T> {

	private final Function<T, @Nullable N> nested;

	private final ConstraintCondition<N> constraintCondition;

	/**
	 * Creates a nested constraint condition.
	 * @param nested function that extracts the nested value from the target
	 * @param constraintCondition condition applied to the nested value
	 */
	public NestedConstraintCondition(Function<T, @Nullable N> nested, ConstraintCondition<N> constraintCondition) {
		this.nested = nested;
		this.constraintCondition = constraintCondition;
	}

	/**
	 * Tests the given condition against the nested value extracted from the target. A
	 * {@code null} nested value is not satisfied.
	 * @param t the target object
	 * @param constraintContext the constraint context
	 * @return whether the nested condition is satisfied
	 */
	@Override
	public boolean test(T t, ConstraintContext constraintContext) {
		final N n = this.nested.apply(t);
		if (n == null) {
			return false;
		}

		return this.constraintCondition.test(n, constraintContext);
	}

}
