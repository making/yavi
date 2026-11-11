/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import am.ik.yavi.constraint.base.ConstraintBase;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Constraints on an enum value.
 *
 * <p>
 * A {@code null} value is considered valid and the checks are skipped.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <E> the enum type the constraints apply to
 * @since 0.14.0
 */
public class EnumConstraint<T, E extends Enum<E>> extends ConstraintBase<T, E, EnumConstraint<T, E>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public EnumConstraint() {
	}

	@Override
	public EnumConstraint<T, E> cast() {
		return this;
	}

	/**
	 * Requires the value to be one of the given enum constants.
	 *
	 * <p>
	 * A violation is reported with the message key {@code object.oneOf} and the default
	 * message {@code "{0}" must be one of the following values: {1}}, where {@code {1}}
	 * is the set of the given constants.
	 * </p>
	 * @param values the allowed enum constants; at least one is required
	 * @return this constraint
	 * @throws IllegalArgumentException if {@code values} is empty
	 */
	@SafeVarargs
	public final EnumConstraint<T, E> oneOf(E... values) throws IllegalArgumentException {
		if (values.length == 0) {
			throw new IllegalArgumentException("oneOf must accept at least one value");
		}

		EnumSet<E> enumSet = EnumSet.copyOf(Arrays.asList(values));

		return super.oneOf(enumSet);
	}

}
