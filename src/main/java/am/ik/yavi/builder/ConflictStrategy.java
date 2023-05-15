/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	 *
	 * @param predicatesList existing predicates list
	 * @param predicates new (conflicting) predicate
	 * @param <T> target type
	 */
	<T> void resolveConflict(List<ConstraintPredicates<T, ?>> predicatesList,
			ConstraintPredicates<T, ?> predicates);

	/**
	 * Do nothing if a conflict occurs. That means simply appending the predicate.
	 */
	ConflictStrategy NOOP = new ConflictStrategy() {
		@Override
		public <T> void resolveConflict(
				List<ConstraintPredicates<T, ?>> constraintPredicates,
				ConstraintPredicates<T, ?> predicates) {
			// NOOP
		}
	};

	/**
	 * If there is a conflict, remove the existing predicates and adopt the new predicate.
	 */
	ConflictStrategy OVERRIDE = new ConflictStrategy() {
		@Override
		public <T> void resolveConflict(
				List<ConstraintPredicates<T, ?>> constraintPredicates,
				ConstraintPredicates<T, ?> predicates) {
			constraintPredicates.clear();
		}
	};
}
