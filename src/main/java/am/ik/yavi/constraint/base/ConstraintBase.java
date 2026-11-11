/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.base;

import java.util.Deque;
import java.util.LinkedList;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintPredicate;

public abstract class ConstraintBase<T, V, C extends Constraint<T, V, C>> implements Constraint<T, V, C> {

	private final Deque<ConstraintPredicate<V>> predicates = new LinkedList<>();

	@Override
	public Deque<ConstraintPredicate<V>> predicates() {
		return this.predicates;
	}

}
