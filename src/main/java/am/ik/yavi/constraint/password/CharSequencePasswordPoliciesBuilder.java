/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.password;

import java.util.List;

import am.ik.yavi.core.ConstraintPredicate;

/**
 * Password policies builder for character sequence values.
 *
 * <p>
 * In addition to the required and optional policies of {@link PasswordPoliciesBuilder},
 * this builder provides the ready-made character class policies of {@link PasswordPolicy}
 * and the {@link #strong()} combination. It is used through
 * {@link am.ik.yavi.constraint.CharSequenceConstraint#password(java.util.function.Function)}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <E> the type of the character sequence
 * @since 0.7.0
 */
public final class CharSequencePasswordPoliciesBuilder<T, E extends CharSequence>
		extends PasswordPoliciesBuilder<T, E, CharSequencePasswordPoliciesBuilder<T, E>> {

	/**
	 * Creates a builder without any policy.
	 */
	public CharSequencePasswordPoliciesBuilder() {
	}

	/**
	 * Returns this builder.
	 * @return this builder
	 */
	@Override
	protected CharSequencePasswordPoliciesBuilder<T, E> cast() {
		return this;
	}

	/**
	 * Requires at least one uppercase ASCII letter.
	 * @return this builder
	 */
	public CharSequencePasswordPoliciesBuilder<T, E> uppercase() {
		return this.uppercase(1);
	}

	/**
	 * Requires at least {@code count} uppercase ASCII letters.
	 * @param count the minimum number of uppercase letters
	 * @return this builder
	 * @throws IllegalArgumentException if {@code count} is zero or negative
	 * @since 0.8.1
	 */
	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> uppercase(int count) {
		return super.required((PasswordPolicy<E>) PasswordPolicy.UPPERCASE.count(count));
	}

	/**
	 * Requires at least one lowercase ASCII letter.
	 * @return this builder
	 */
	public CharSequencePasswordPoliciesBuilder<T, E> lowercase() {
		return this.lowercase(1);
	}

	/**
	 * Requires at least {@code count} lowercase ASCII letters.
	 * @param count the minimum number of lowercase letters
	 * @return this builder
	 * @throws IllegalArgumentException if {@code count} is zero or negative
	 * @since 0.8.1
	 */
	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> lowercase(int count) {
		return super.required((PasswordPolicy<E>) PasswordPolicy.LOWERCASE.count(count));
	}

	/**
	 * Requires at least one ASCII letter.
	 * @return this builder
	 * @since 0.8.1
	 */
	public CharSequencePasswordPoliciesBuilder<T, E> alphabets() {
		return this.alphabets(1);
	}

	/**
	 * Requires at least {@code count} ASCII letters.
	 * @param count the minimum number of letters
	 * @return this builder
	 * @throws IllegalArgumentException if {@code count} is zero or negative
	 * @since 0.8.1
	 */
	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> alphabets(int count) {
		return super.required((PasswordPolicy<E>) PasswordPolicy.ALPHABETS.count(count));
	}

	/**
	 * Requires at least one ASCII digit.
	 * @return this builder
	 */
	public CharSequencePasswordPoliciesBuilder<T, E> numbers() {
		return this.numbers(1);
	}

	/**
	 * Requires at least {@code count} ASCII digits.
	 * @param count the minimum number of digits
	 * @return this builder
	 * @throws IllegalArgumentException if {@code count} is zero or negative
	 * @since 0.8.1
	 */
	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> numbers(int count) {
		return super.required((PasswordPolicy<E>) PasswordPolicy.NUMBERS.count(count));
	}

	/**
	 * Requires at least one ASCII symbol other than a letter, digit or space.
	 * @return this builder
	 */
	public CharSequencePasswordPoliciesBuilder<T, E> symbols() {
		return this.symbols(1);
	}

	/**
	 * Requires at least {@code count} ASCII symbols other than letters, digits or spaces.
	 * @param count the minimum number of symbols
	 * @return this builder
	 * @throws IllegalArgumentException if {@code count} is zero or negative
	 * @since 0.8.1
	 */
	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> symbols(int count) {
		return super.required((PasswordPolicy<E>) PasswordPolicy.SYMBOLS.count(count));
	}

	/**
	 * Builds the predicates that require at least one uppercase letter, one lowercase
	 * letter, one digit and one symbol. This is equivalent to
	 * {@code uppercase().lowercase().numbers().symbols().build()}.
	 * @return the constraint predicates
	 */
	public List<ConstraintPredicate<E>> strong() {
		return this.uppercase().lowercase().numbers().symbols().build();
	}

}
