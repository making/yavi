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
 * Constraints on a {@code char[]} value.
 *
 * <p>
 * Besides {@link #contains(char)}, the size constraints of
 * {@link ContainerConstraintBase} are available. A {@code null} array is considered valid
 * and the checks are skipped, except for {@link ContainerConstraintBase#notEmpty()}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.0.1
 */
public class CharArrayConstraint<T> extends ContainerConstraintBase<T, char[], CharArrayConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public CharArrayConstraint() {
	}

	@Override
	public CharArrayConstraint<T> cast() {
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
	public CharArrayConstraint<T> contains(char v) {
		this.predicates().add(ConstraintPredicate.of(x -> {
			for (char e : x) {
				if (e == v) {
					return true;
				}
			}
			return false;
		}, ARRAY_CONTAINS, () -> new Object[] { v }, VALID));
		return this;
	}

	@Override
	protected ToIntFunction<char[]> size() {
		return x -> x.length;
	}

}
