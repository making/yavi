/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.array;

import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.ARRAY_CONTAINS;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

/**
 * Constraints on a {@code short[]} value.
 *
 * <p>
 * Besides {@link #contains(short)}, the size constraints of
 * {@link ContainerConstraintBase} are available. A {@code null} array is considered valid
 * and the checks are skipped, except for {@link ContainerConstraintBase#notEmpty()}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.0.1
 */
public class ShortArrayConstraint<T> extends ContainerConstraintBase<T, short[], ShortArrayConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public ShortArrayConstraint() {
	}

	@Override
	public ShortArrayConstraint<T> cast() {
		return this;
	}

	/**
	 * Requires the array to contain the given value.
	 *
	 * <p>
	 * A violation is reported with the message key {@code array.contains} and the default
	 * message {@code "{0}" must contain {1}}, where {@code {1}} is the value.
	 * </p>
	 * @param v the value the array has to contain
	 * @return this constraint
	 */
	public ShortArrayConstraint<T> contains(short v) {
		this.predicates().add(ConstraintPredicate.of(x -> {
			for (short e : x) {
				if (e == v) {
					return true;
				}
			}
			return false;
		}, ARRAY_CONTAINS, () -> new Object[] { v }, VALID));
		return this;
	}

	@Override
	protected ToIntFunction<short[]> size() {
		return x -> x.length;
	}

}
