/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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
import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_NOT_ONE_OF;
import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_ONE_OF;

public interface Constraint<T, V, C extends Constraint<T, V, C>> {

	C cast();

	default C isNull() {
		this.predicates()
			.add(ConstraintPredicate.of(Objects::isNull, OBJECT_IS_NULL, () -> new Object[] {}, NullAs.INVALID));
		return this.cast();
	}

	/**
	 * @since 0.10.0
	 */
	default C equalTo(@Nullable V other) {
		this.predicates()
			.add(ConstraintPredicate.of(Predicate.isEqual(other), OBJECT_EQUAL_TO, () -> new Object[] { other },
					NullAs.VALID));
		return this.cast();
	}

	/**
	 * @since 0.10.0
	 */
	default C oneOf(Collection<V> values) {
		this.predicates()
			.add(ConstraintPredicate.of(values::contains, OBJECT_ONE_OF, () -> new Object[] { values }, NullAs.VALID));
		return this.cast();
	}

	/**
	 * @since 0.14.1
	 */
	default C notOneOf(Collection<V> values) {
		Predicate<V> notOneOf = v -> !values.contains(v);
		this.predicates()
			.add(ConstraintPredicate.of(notOneOf, OBJECT_NOT_ONE_OF, () -> new Object[] { values }, NullAs.VALID));
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
		this.predicates()
			.add(ConstraintPredicate.of(Objects::nonNull, OBJECT_NOT_NULL, () -> new Object[] {}, NullAs.INVALID));
		return this.cast();
	}

	default C predicate(Predicate<V> predicate, ViolationMessage violationMessage) {
		final Supplier<Object[]> arguments = ViolatedArguments.supplyArguments(predicate);
		this.predicates().add(ConstraintPredicate.of(predicate, violationMessage, arguments, NullAs.VALID));
		return this.cast();
	}

	/**
	 * @since 0.8.2
	 */
	default C predicate(Predicate<V> predicate, String messageKey, String defaultMessageFormat) {
		return this.predicate(predicate, ViolationMessage.of(messageKey, defaultMessageFormat));
	}

	default C predicate(CustomConstraint<V> constraint) {
		return this.predicate(constraint, constraint);
	}

	default C predicateNullable(Predicate<V> predicate, ViolationMessage violationMessage) {
		final Supplier<Object[]> arguments = ViolatedArguments.supplyArguments(predicate);
		this.predicates().add(ConstraintPredicate.of(predicate, violationMessage, arguments, NullAs.INVALID));
		return this.cast();
	}

	/**
	 * @since 0.8.2
	 */
	default C predicateNullable(Predicate<V> predicate, String messageKey, String defaultMessageFormat) {
		return this.predicateNullable(predicate, ViolationMessage.of(messageKey, defaultMessageFormat));
	}

	default C predicateNullable(CustomConstraint<V> constraint) {
		return this.predicateNullable(constraint, constraint);
	}

	Deque<ConstraintPredicate<V>> predicates();

}
