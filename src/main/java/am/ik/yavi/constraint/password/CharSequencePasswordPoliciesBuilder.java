/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.constraint.password;

import java.util.List;

import am.ik.yavi.core.ConstraintPredicate;

/**
 * @since 0.7.0
 */
public final class CharSequencePasswordPoliciesBuilder<T, E extends CharSequence>
		extends PasswordPoliciesBuilder<T, E, CharSequencePasswordPoliciesBuilder<T, E>> {

	@Override
	protected CharSequencePasswordPoliciesBuilder<T, E> cast() {
		return this;
	}

	public CharSequencePasswordPoliciesBuilder<T, E> uppercase() {
		return this.uppercase(1);
	}

	/**
	 * @since 0.8.1
	 */
	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> uppercase(int count) {
		return super.required((PasswordPolicy<E>) PasswordPolicy.UPPERCASE.count(count));
	}

	public CharSequencePasswordPoliciesBuilder<T, E> lowercase() {
		return this.lowercase(1);
	}

	/**
	 * @since 0.8.1
	 */
	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> lowercase(int count) {
		return super.required((PasswordPolicy<E>) PasswordPolicy.LOWERCASE.count(count));
	}

	/**
	 * @since 0.8.1
	 */
	public CharSequencePasswordPoliciesBuilder<T, E> alphabets() {
		return this.alphabets(1);
	}

	/**
	 * @since 0.8.1
	 */
	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> alphabets(int count) {
		return super.required((PasswordPolicy<E>) PasswordPolicy.ALPHABETS.count(count));
	}

	public CharSequencePasswordPoliciesBuilder<T, E> numbers() {
		return this.numbers(1);
	}

	/**
	 * @since 0.8.1
	 */
	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> numbers(int count) {
		return super.required((PasswordPolicy<E>) PasswordPolicy.NUMBERS.count(count));
	}

	public CharSequencePasswordPoliciesBuilder<T, E> symbols() {
		return this.symbols(1);
	}

	/**
	 * @since 0.8.1
	 */
	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> symbols(int count) {
		return super.required((PasswordPolicy<E>) PasswordPolicy.SYMBOLS.count(count));
	}

	public List<ConstraintPredicate<E>> strong() {
		return this.uppercase().lowercase().numbers().symbols().build();
	}
}
