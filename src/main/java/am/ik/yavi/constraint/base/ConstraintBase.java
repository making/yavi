/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.base;

import java.util.Deque;
import java.util.LinkedList;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintPredicate;

/**
 * Base implementation of {@link Constraint} that collects the added predicates in a
 * mutable queue.
 *
 * <p>
 * Subclasses only need to provide {@link #cast()} and, where applicable, the predicates
 * of their value type. An instance is not thread-safe; use it from a single thread while
 * building a validator.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <V> the type of the value the constraints apply to
 * @param <C> the concrete constraint type returned for method chaining
 * @since 0.0.1
 */
public abstract class ConstraintBase<T, V, C extends Constraint<T, V, C>> implements Constraint<T, V, C> {

	private final Deque<ConstraintPredicate<V>> predicates = new LinkedList<>();

	/**
	 * Creates a new constraint without any predicate.
	 */
	public ConstraintBase() {
	}

	/**
	 * Returns the mutable queue of predicates collected by this constraint.
	 * @return the mutable queue of predicates
	 */
	@Override
	public Deque<ConstraintPredicate<V>> predicates() {
		return this.predicates;
	}

}
