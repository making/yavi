/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Ready-made sets and ranges of Unicode code points for the code point constraints of
 * {@link am.ik.yavi.constraint.CharSequenceConstraint#codePoints}.
 *
 * <p>
 * {@link am.ik.yavi.constraint.charsequence.codepoints.AsciiCodePoints} and
 * {@link am.ik.yavi.constraint.charsequence.codepoints.UnicodeCodePoints} are enums of
 * commonly used character groups, and
 * {@link am.ik.yavi.constraint.charsequence.codepoints.CompositeCodePoints} combines
 * several {@link am.ik.yavi.constraint.charsequence.CodePoints} sources into one whose
 * allowed code points are the union of the given sources.
 * </p>
 *
 * @since 0.0.10
 */
@NonNullApi
package am.ik.yavi.constraint.charsequence.codepoints;

import am.ik.yavi.jsr305.NonNullApi;
