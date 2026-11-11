/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.password;

/**
 * Password policies builder for an object value that is not a character sequence.
 *
 * <p>
 * It provides the required and optional policies of {@link PasswordPoliciesBuilder} and
 * is used through
 * {@link am.ik.yavi.constraint.ObjectConstraint#password(java.util.function.Function)}.
 * This is useful for a policy that compares the password with another property of the
 * validated object.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <E> the type of the value the policies test
 * @since 0.7.0
 */
public class ObjectPasswordPoliciesBuilder<T, E>
		extends PasswordPoliciesBuilder<T, E, ObjectPasswordPoliciesBuilder<T, E>> {

	/**
	 * Creates a builder without any policy.
	 */
	public ObjectPasswordPoliciesBuilder() {
	}

	/**
	 * Returns this builder.
	 * @return this builder
	 */
	@Override
	protected ObjectPasswordPoliciesBuilder<T, E> cast() {
		return this;
	}

}
