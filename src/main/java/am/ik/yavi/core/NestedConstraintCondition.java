/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.function.Function;

public class NestedConstraintCondition<T, N> implements ConstraintCondition<T> {

	private final Function<T, N> nested;

	private final ConstraintCondition<N> constraintCondition;

	public NestedConstraintCondition(Function<T, N> nested, ConstraintCondition<N> constraintCondition) {
		this.nested = nested;
		this.constraintCondition = constraintCondition;
	}

	@Override
	public boolean test(T t, ConstraintContext constraintContext) {
		final N n = this.nested.apply(t);
		if (n == null) {
			return false;
		}

		return this.constraintCondition.test(n, constraintContext);
	}

}
