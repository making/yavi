/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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

import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import am.ik.yavi.jsr305.Nullable;

import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_EQUAL_TO;
import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_IS_NULL;
import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_NOT_NULL;
import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_ONE_OF;

public interface Constraint<T, V, C extends Constraint<T, V, C>> {

	C cast();

	default C isNull() {
		this.predicates().add(ConstraintPredicate.of(Objects::isNull, OBJECT_IS_NULL,
				() -> new Object[] {}, NullAs.INVALID));
		return this.cast();
	}

	default C equalTo(@Nullable V other) {
		this.predicates().add(ConstraintPredicate.of(Predicate.isEqual(other), OBJECT_EQUAL_TO,
				() -> new Object[] { other }, NullAs.INVALID));
		return this.cast();
	}

	default C oneOf(Collection<V> values) {
		this.predicates().add(ConstraintPredicate.of(values::contains, OBJECT_ONE_OF,
				() -> new Object[] { values.toArray() }, NullAs.INVALID));
		return this.cast();
	}

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
		this.predicates().add(ConstraintPredicate.of(Objects::nonNull, OBJECT_NOT_NULL,
				() -> new Object[] {}, NullAs.INVALID));
		return this.cast();
	}

	default C predicate(Predicate<V> predicate, ViolationMessage violationMessage) {
		final Supplier<Object[]> arguments = (predicate instanceof ViolatedArguments) ?
				((ViolatedArguments) predicate)::arguments : () -> new Object[] {};
		this.predicates().add(ConstraintPredicate.of(predicate, violationMessage,
				arguments, NullAs.VALID));
		return this.cast();
	}

	/**
	 * @since 0.8.2
	 */
	default C predicate(Predicate<V> predicate, String messageKey,
			String defaultMessageFormat) {
		return this.predicate(predicate,
				ViolationMessage.of(messageKey, defaultMessageFormat));
	}

	default C predicate(CustomConstraint<V> constraint) {
		return this.predicate(constraint, constraint);
	}

	default C predicateNullable(Predicate<V> predicate,
			ViolationMessage violationMessage) {
		final Supplier<Object[]> arguments = (predicate instanceof ViolatedArguments) ?
				((ViolatedArguments) predicate)::arguments : () -> new Object[] {};
		this.predicates().add(ConstraintPredicate.of(predicate, violationMessage,
				arguments, NullAs.INVALID));
		return this.cast();
	}

	/**
	 * @since 0.8.2
	 */
	default C predicateNullable(Predicate<V> predicate, String messageKey,
			String defaultMessageFormat) {
		return this.predicateNullable(predicate,
				ViolationMessage.of(messageKey, defaultMessageFormat));
	}

	default C predicateNullable(CustomConstraint<V> constraint) {
		return this.predicateNullable(constraint, constraint);
	}

	Deque<ConstraintPredicate<V>> predicates();
}
