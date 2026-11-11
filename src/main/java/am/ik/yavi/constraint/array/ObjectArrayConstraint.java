/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.array;

import java.util.Arrays;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.ARRAY_CONTAINS;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

/**
 * Constraints on an object array value.
 *
 * <p>
 * Besides {@link #contains(Object)}, the size constraints of
 * {@link ContainerConstraintBase} are available. A {@code null} array is considered valid
 * and the checks are skipped, except for {@link ContainerConstraintBase#notEmpty()}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <E> the element type of the array
 * @since 0.0.1
 */
public class ObjectArrayConstraint<T, E> extends ContainerConstraintBase<T, E[], ObjectArrayConstraint<T, E>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public ObjectArrayConstraint() {
	}

	@Override
	public ObjectArrayConstraint<T, E> cast() {
		return this;
	}

	/**
	 * Requires the array to contain the given element.
	 *
	 * <p>
	 * A violation is reported with the message key {@code array.contains} and the default
	 * message {@code "{0}" must contain {1}}, where {@code {1}} is the element.
	 * </p>
	 * @param s the element the array has to contain
	 * @return this constraint
	 */
	public ObjectArrayConstraint<T, E> contains(E s) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> Arrays.asList(x).contains(s), ARRAY_CONTAINS, () -> new Object[] { s },
					VALID));
		return this;
	}

	@Override
	protected ToIntFunction<E[]> size() {
		return x -> x.length;
	}

}
