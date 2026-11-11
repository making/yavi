/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.base;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullAs.INVALID;
import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_FIXED_SIZE;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_GREATER_THAN;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_GREATER_THAN_OR_EQUAL;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_LESS_THAN;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_LESS_THAN_OR_EQUAL;
import static am.ik.yavi.core.ViolationMessage.Default.CONTAINER_NOT_EMPTY;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.ViolatedValue;

public abstract class ContainerConstraintBase<T, V, C extends Constraint<T, V, C>> extends ConstraintBase<T, V, C> {

	public C fixedSize(int size) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(
					this.checkSizePredicate(x -> size().applyAsInt(x) == size, this.size()), CONTAINER_FIXED_SIZE,
					() -> new Object[] { size }, VALID));
		return cast();
	}

	public C greaterThan(int min) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(
					this.checkSizePredicate(x -> size().applyAsInt(x) > min, this.size()), CONTAINER_GREATER_THAN,
					() -> new Object[] { min }, VALID));
		return cast();
	}

	public C greaterThanOrEqual(int min) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(
					this.checkSizePredicate(x -> size().applyAsInt(x) >= min, this.size()),
					CONTAINER_GREATER_THAN_OR_EQUAL, () -> new Object[] { min }, VALID));
		return cast();
	}

	public C lessThan(int max) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(
					this.checkSizePredicate(x -> size().applyAsInt(x) < max, this.size()), CONTAINER_LESS_THAN,
					() -> new Object[] { max }, VALID));
		return cast();
	}

	public C lessThanOrEqual(int max) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(
					this.checkSizePredicate(x -> size().applyAsInt(x) <= max, this.size()),
					CONTAINER_LESS_THAN_OR_EQUAL, () -> new Object[] { max }, VALID));
		return cast();
	}

	public C notEmpty() {
		this.predicates()
			.add(ConstraintPredicate.of(x -> x != null && size().applyAsInt(x) != 0, CONTAINER_NOT_EMPTY,
					() -> new Object[] {}, INVALID));
		return cast();
	}

	protected Function<V, Optional<ViolatedValue>> checkSizePredicate(Predicate<V> predicate, ToIntFunction<V> size) {
		return v -> {
			if (predicate.test(v)) {
				return Optional.empty();
			}
			int s = size.applyAsInt(v);
			return Optional.of(new ViolatedValue(s));
		};
	}

	protected abstract ToIntFunction<V> size();

}
