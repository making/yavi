/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.List;

import am.ik.yavi.core.ConstraintPredicates;

/**
 * Defines how a repeated constraint name is resolved when a validator is built.
 *
 * <p>
 * A strategy is set with {@link ValidatorBuilder#conflictStrategy(ConflictStrategy)} and
 * is applied by {@link ValidatorBuilder#build()} for each constraint whose name is
 * already present. The strategy may modify the list of existing predicates; the resulting
 * list is used for the built validator.
 *
 * @since 0.13.0
 * @see ValidatorBuilder#conflictStrategy(ConflictStrategy)
 */
@FunctionalInterface
public interface ConflictStrategy {

	/**
	 * Resolves a name conflict between existing predicates and a new predicate.
	 *
	 * <p>
	 * This method is called with the mutable list of existing predicates for the name.
	 * Implementations may clear it, replace its entries or leave it unchanged; the new
	 * predicate is appended after this method returns.
	 * @param <T> the type of the target object
	 * @param predicatesList the mutable list of existing predicates with the same name
	 * @param predicates the new predicate whose name conflicts
	 */
	<T> void resolveConflict(List<ConstraintPredicates<T, ?>> predicatesList, ConstraintPredicates<T, ?> predicates);

	/**
	 * Keeps the existing predicates and appends the new predicate.
	 */
	ConflictStrategy NOOP = new ConflictStrategy() {
		@Override
		public <T> void resolveConflict(List<ConstraintPredicates<T, ?>> constraintPredicates,
				ConstraintPredicates<T, ?> predicates) {
			// NOOP
		}
	};

	/**
	 * Removes the existing predicates so that only the new predicate is used.
	 */
	ConflictStrategy OVERRIDE = new ConflictStrategy() {
		@Override
		public <T> void resolveConflict(List<ConstraintPredicates<T, ?>> constraintPredicates,
				ConstraintPredicates<T, ?> predicates) {
			constraintPredicates.clear();
		}
	};

}
