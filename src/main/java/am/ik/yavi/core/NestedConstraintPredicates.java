/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Deque;
import java.util.function.Function;

import am.ik.yavi.jsr305.Nullable;

public class NestedConstraintPredicates<T, V, N> extends ConstraintPredicates<T, V> {

	private final Function<T, N> nested;

	private final boolean failFast;

	public NestedConstraintPredicates(Function<T, V> toValue, String name,
			Deque<ConstraintPredicate<V>> constraintPredicates, Function<T, N> nested, boolean failFast) {
		super(toValue, name, constraintPredicates);
		this.nested = nested;
		this.failFast = failFast;
	}

	@Nullable
	public N nestedValue(T target) {
		return nested.apply(target);
	}

	/**
	 * Returns whether it is failFast.
	 * @return whether it is failFast
	 * @since 0.13.0
	 */
	public boolean isFailFast() {
		return this.failFast;
	}

}
