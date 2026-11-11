/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumericConstraintBase;

public class CharacterConstraint<T> extends NumericConstraintBase<T, Character, CharacterConstraint<T>> {

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
