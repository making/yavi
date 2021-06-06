/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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

	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> uppercase() {
		return super.required((PasswordPolicy<E>) PasswordPolicy.UPPERCASE);
	}

	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> lowercase() {
		return super.required((PasswordPolicy<E>) PasswordPolicy.LOWERCASE);
	}

	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> numbers() {
		return super.required((PasswordPolicy<E>) PasswordPolicy.NUMBERS);
	}

	@SuppressWarnings("unchecked")
	public CharSequencePasswordPoliciesBuilder<T, E> symbols() {
		return super.required((PasswordPolicy<E>) PasswordPolicy.SYMBOLS);
	}

	public List<ConstraintPredicate<E>> strong() {
		return this.uppercase().lowercase().numbers().symbols().build();
	}
}
