/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Options that control which Unicode variation selectors are removed from a character
 * sequence before its size is measured.
 *
 * <p>
 * A variation selector changes the appearance of the preceding character without adding a
 * visible character. {@link am.ik.yavi.constraint.charsequence.variant.VariantOptions}
 * collects the settings for the standardized, ideographic and Mongolian free variation
 * selectors and is used by the size constraints of
 * {@link am.ik.yavi.constraint.CharSequenceConstraint} and
 * {@link am.ik.yavi.constraint.charsequence.EmojiConstraint}.
 * </p>
 *
 * @since 0.0.14
 */
@NullMarked
package am.ik.yavi.constraint.charsequence.variant;

import org.jspecify.annotations.NullMarked;
