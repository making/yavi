/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Password policies and the builders that turn them into constraints.
 *
 * <p>
 * A {@link am.ik.yavi.constraint.password.PasswordPolicy} is a named predicate on a
 * password value. {@link am.ik.yavi.constraint.password.PasswordPoliciesBuilder} collects
 * required and optional policies and produces the constraint predicates;
 * {@link am.ik.yavi.constraint.password.CharSequencePasswordPoliciesBuilder} adds ready
 * made character class policies for character sequences, and
 * {@link am.ik.yavi.constraint.password.ObjectPasswordPoliciesBuilder} is used for
 * policies on an object that is not a character sequence.
 * </p>
 *
 * <p>
 * Use them through
 * {@link am.ik.yavi.constraint.CharSequenceConstraint#password(java.util.function.Function)}
 * or
 * {@link am.ik.yavi.constraint.ObjectConstraint#password(java.util.function.Function)}.
 * </p>
 *
 * @since 0.7.0
 */
@NonNullApi
package am.ik.yavi.constraint.password;

import am.ik.yavi.jsr305.NonNullApi;
