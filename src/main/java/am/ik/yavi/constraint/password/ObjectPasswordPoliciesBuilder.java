/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.password;

/**
 * @since 0.7.0
 */
public class ObjectPasswordPoliciesBuilder<T, E>
		extends PasswordPoliciesBuilder<T, E, ObjectPasswordPoliciesBuilder<T, E>> {

	@Override
	protected ObjectPasswordPoliciesBuilder<T, E> cast() {
		return this;
	}

}
