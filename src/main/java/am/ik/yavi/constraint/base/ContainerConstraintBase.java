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

/**
 * Base class for constraints on containers such as collections, maps and arrays.
 *
 * <p>
 * The size of a value is obtained from {@link #size()}. Unless stated otherwise, a
 * {@code null} value is considered valid and the size checks are skipped.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <V> the type of the container the constraints apply to
 * @param <C> the concrete constraint type returned for method chaining
 * @since 0.0.1
 */
public abstract class ContainerConstraintBase<T, V, C extends Constraint<T, V, C>> extends ConstraintBase<T, V, C> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public ContainerConstraintBase() {
	}

	/**
	 * Requires the size of the value to be exactly {@code size}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code container.fixedSize} and the
	 * default message {@code The size of "{0}" must be {1}. The given size is {2}}, where
	 * {@code {1}} is {@code size} and {@code {2}} is the actual size.
	 * </p>
	 * @param size the required size
	 * @return this constraint
	 */
	public C fixedSize(int size) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(
					this.checkSizePredicate(x -> size().applyAsInt(x) == size, this.size()), CONTAINER_FIXED_SIZE,
					() -> new Object[] { size }, VALID));
		return cast();
	}

	/**
	 * Requires the size of the value to be greater than {@code min}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code container.greaterThan} and the
	 * default message {@code The size of "{0}" must be greater than {1}. The given size
	 * is {2}}, where {@code {1}} is {@code min} and {@code {2}} is the actual size.
	 * </p>
	 * @param min the exclusive lower bound of the size
	 * @return this constraint
	 */
	public C greaterThan(int min) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(
					this.checkSizePredicate(x -> size().applyAsInt(x) > min, this.size()), CONTAINER_GREATER_THAN,
					() -> new Object[] { min }, VALID));
		return cast();
	}

	/**
	 * Requires the size of the value to be greater than or equal to {@code min}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code container.greaterThanOrEqual}
	 * and the default message {@code The size of "{0}" must be greater than or equal to
	 * {1}. The given size is {2}}, where {@code {1}} is {@code min} and {@code {2}} is
	 * the actual size.
	 * </p>
	 * @param min the inclusive lower bound of the size
	 * @return this constraint
	 */
	public C greaterThanOrEqual(int min) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(
					this.checkSizePredicate(x -> size().applyAsInt(x) >= min, this.size()),
					CONTAINER_GREATER_THAN_OR_EQUAL, () -> new Object[] { min }, VALID));
		return cast();
	}

	/**
	 * Requires the size of the value to be less than {@code max}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code container.lessThan} and the
	 * default message {@code The size of "{0}" must be less than {1}. The given size is
	 * {2}}, where {@code {1}} is {@code max} and {@code {2}} is the actual size.
	 * </p>
	 * @param max the exclusive upper bound of the size
	 * @return this constraint
	 */
	public C lessThan(int max) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(
					this.checkSizePredicate(x -> size().applyAsInt(x) < max, this.size()), CONTAINER_LESS_THAN,
					() -> new Object[] { max }, VALID));
		return cast();
	}

	/**
	 * Requires the size of the value to be less than or equal to {@code max}.
	 *
	 * <p>
	 * A violation is reported with the message key {@code container.lessThanOrEqual} and
	 * the default message {@code The size of "{0}" must be less than or equal to {1}. The
	 * given size is {2}}, where {@code {1}} is {@code max} and {@code {2}} is the actual
	 * size.
	 * </p>
	 * @param max the inclusive upper bound of the size
	 * @return this constraint
	 */
	public C lessThanOrEqual(int max) {
		this.predicates()
			.add(ConstraintPredicate.withViolatedValue(
					this.checkSizePredicate(x -> size().applyAsInt(x) <= max, this.size()),
					CONTAINER_LESS_THAN_OR_EQUAL, () -> new Object[] { max }, VALID));
		return cast();
	}

	/**
	 * Requires the size of the value not to be zero.
	 *
	 * <p>
	 * A {@code null} value is reported as a violation. A violation is reported with the
	 * message key {@code container.notEmpty} and the default message {@code "{0}" must
	 * not be empty}.
	 * </p>
	 * @return this constraint
	 */
	public C notEmpty() {
		this.predicates()
			.add(ConstraintPredicate.of(x -> x != null && size().applyAsInt(x) != 0, CONTAINER_NOT_EMPTY,
					() -> new Object[] {}, INVALID));
		return cast();
	}

	/**
	 * Creates a function that returns the actual size as the violated value when the
	 * given predicate does not match.
	 * @param predicate the predicate that checks the size
	 * @param size the function that computes the size of the value
	 * @return a function that returns the violated size, or an empty optional when the
	 * predicate matches
	 */
	protected Function<V, Optional<ViolatedValue>> checkSizePredicate(Predicate<V> predicate, ToIntFunction<V> size) {
		return v -> {
			if (predicate.test(v)) {
				return Optional.empty();
			}
			int s = size.applyAsInt(v);
			return Optional.of(new ViolatedValue(s));
		};
	}

	/**
	 * Returns the function that computes the size of the value.
	 * @return the function that computes the size of the value
	 */
	protected abstract ToIntFunction<V> size();

}
