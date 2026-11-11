/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

/**
 * Constraints on a {@link Character} value.
 *
 * <p>
 * A {@code null} value is considered valid and the checks are skipped. The comparisons
 * use the numeric value of the underlying {@code char}. The zero value used by
 * {@code positive}, {@code positiveOrZero}, {@code negative} and {@code negativeOrZero}
 * is {@link Character#MIN_VALUE}, the NUL character.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.0.2
 */
public class CharacterConstraint<T> extends NumericConstraintBase<T, Character, CharacterConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public CharacterConstraint() {
	}

	@Override
	public CharacterConstraint<T> cast() {
		return this;
	}

	@Override
	protected Predicate<Character> isGreaterThan(Character min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Character> isGreaterThanOrEqual(Character min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Character> isLessThan(Character max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Character> isLessThanOrEqual(Character max) {
		return x -> x <= max;
	}

	@Override
	protected Character zeroValue() {
		return Character.MIN_VALUE;
	}

}
