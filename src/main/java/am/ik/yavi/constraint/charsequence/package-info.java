/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Helpers for the constraints on {@code CharSequence} values that go beyond the fluent
 * methods of {@link am.ik.yavi.constraint.CharSequenceConstraint}.
 *
 * <p>
 * {@link am.ik.yavi.constraint.charsequence.CodePoints} supplies the code points for
 * {@link am.ik.yavi.constraint.CodePointsConstraint},
 * {@link am.ik.yavi.constraint.charsequence.codepoints.CompositeCodePoints} combines
 * several sources, and the
 * {@link am.ik.yavi.constraint.charsequence.variant.VariantOptions} decide which Unicode
 * variation selectors are removed before the value is counted.
 * {@link am.ik.yavi.constraint.charsequence.UrlConstraintBuilder} builds the URL
 * constraints exposed by {@link am.ik.yavi.constraint.CharSequenceConstraint#url()}.
 * </p>
 *
 * @since 0.0.10
 */
@NullMarked
package am.ik.yavi.constraint.charsequence;

import org.jspecify.annotations.NullMarked;
