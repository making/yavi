/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Deque;
import java.util.function.Function;

public class ConstraintPredicates<T, V> {

	private final String name;

	private final Deque<ConstraintPredicate<V>> predicates;

	private final Function<T, V> toValue;

	public ConstraintPredicates(Function<T, V> toValue, String name, Deque<ConstraintPredicate<V>> predicates) {
		this.toValue = toValue;
		this.name = name;
		this.predicates = predicates;
	}

	public final String name() {
		return this.name;
	}

	public final Deque<ConstraintPredicate<V>> predicates() {
		return this.predicates;
	}

	public final Function<T, V> toValue() {
		return this.toValue;
	}

}
