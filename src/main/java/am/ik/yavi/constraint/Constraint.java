/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.constraint;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static am.ik.yavi.constraint.ViolationMessage.Default.OBJECT_IS_NULL;
import static am.ik.yavi.constraint.ViolationMessage.Default.OBJECT_NOT_NULL;

import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.CustomConstraint;
import am.ik.yavi.core.NullValidity;

public interface Constraint<T, V, C extends Constraint<T, V, C>> {

	List<ConstraintPredicate<V>> predicates();

	C cast();

	default C notNull() {
		this.predicates().add(new ConstraintPredicate<>(Objects::nonNull, OBJECT_NOT_NULL,
				() -> new Object[] {}, NullValidity.NULL_IS_INVALID));
		return this.cast();
	}

	default C isNull() {
		this.predicates().add(new ConstraintPredicate<>(Objects::isNull, OBJECT_IS_NULL,
				() -> new Object[] {}, NullValidity.NULL_IS_INVALID));
		return this.cast();
	}

	default C predicate(Predicate<V> predicate, ViolationMessage violationMessage) {
		this.predicates().add(new ConstraintPredicate<>(predicate, violationMessage,
				() -> new Object[] {}, NullValidity.NULL_IS_VALID));
		return this.cast();
	}

	default C predicateNullable(Predicate<V> predicate,
			ViolationMessage violationMessage) {
		this.predicates().add(new ConstraintPredicate<>(predicate, violationMessage,
				() -> new Object[] {}, NullValidity.NULL_IS_INVALID));
		return this.cast();
	}

	default C predicate(CustomConstraint<V> constraint) {
		return this.predicate(constraint.predicate(), constraint);
	}

	default C predicateNullable(CustomConstraint<V> constraint) {
		return this.predicateNullable(constraint.predicate(), constraint);
	}
}
