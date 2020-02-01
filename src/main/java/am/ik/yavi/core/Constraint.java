/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import java.util.Deque;
import java.util.Objects;
import java.util.function.Predicate;

public interface Constraint<T, V, C extends Constraint<T, V, C>> {

	default C isNull() {
		this.predicates()
				.add(ConstraintPredicate.of(Objects::isNull,
						IncludedViolationMessages.get().OBJECT_IS_NULL(),
						() -> new Object[] {}, NullAs.INVALID));
		return this.cast();
	}

	Deque<ConstraintPredicate<V>> predicates();

	C cast();

	default C message(String message) {
		ConstraintPredicate<V> predicate = this.predicates().pollLast();
		if (predicate == null) {
			throw new IllegalStateException("no constraint found to override!");
		}
		this.predicates().addLast(predicate.overrideMessage(message));
		return this.cast();
	}

	default C message(ViolationMessage message) {
		ConstraintPredicate<V> predicate = this.predicates().pollLast();
		if (predicate == null) {
			throw new IllegalStateException("no constraint found to override!");
		}
		this.predicates().addLast(predicate.overrideMessage(message));
		return this.cast();
	}

	default C notNull() {
		this.predicates()
				.add(ConstraintPredicate.of(Objects::nonNull,
						IncludedViolationMessages.get().OBJECT_NOT_NULL(),
						() -> new Object[] {}, NullAs.INVALID));
		return this.cast();
	}

	default C predicate(CustomConstraint<V> constraint) {
		return this.predicate(constraint, constraint);
	}

	default C predicate(Predicate<V> predicate, ViolationMessage violationMessage) {
		this.predicates().add(ConstraintPredicate.of(predicate, violationMessage,
				() -> new Object[] {}, NullAs.VALID));
		return this.cast();
	}

	default C predicateNullable(CustomConstraint<V> constraint) {
		return this.predicateNullable(constraint, constraint);
	}

	default C predicateNullable(Predicate<V> predicate,
			ViolationMessage violationMessage) {
		this.predicates().add(ConstraintPredicate.of(predicate, violationMessage,
				() -> new Object[] {}, NullAs.INVALID));
		return this.cast();
	}
}
