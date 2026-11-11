/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.List;

import am.ik.yavi.core.ConstraintPredicates;

/**
 * Conflict Strategy that defines the behavior when a constraint name conflicts when
 * adding a constraint.
 *
 * @since 0.13.0
 */
@FunctionalInterface
public interface ConflictStrategy {

	/**
	 * Define how to resolve conflicts of a constraint name when adding a constraint.
	 * @param predicatesList existing predicates list
	 * @param predicates new (conflicting) predicate
	 * @param <T> target type
	 */
	<T> void resolveConflict(List<ConstraintPredicates<T, ?>> predicatesList, ConstraintPredicates<T, ?> predicates);

	/**
	 * Do nothing if a conflict occurs. That means simply appending the predicate.
	 */
	ConflictStrategy NOOP = new ConflictStrategy() {
		@Override
		public <T> void resolveConflict(List<ConstraintPredicates<T, ?>> constraintPredicates,
				ConstraintPredicates<T, ?> predicates) {
			// NOOP
		}
	};

	/**
	 * If there is a conflict, remove the existing predicates and adopt the new predicate.
	 */
	ConflictStrategy OVERRIDE = new ConflictStrategy() {
		@Override
		public <T> void resolveConflict(List<ConstraintPredicates<T, ?>> constraintPredicates,
				ConstraintPredicates<T, ?> predicates) {
			constraintPredicates.clear();
		}
	};

}
