/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence.variant;

/**
 * Setting for the Unicode standardized variation selectors, from U+FE00 to U+FE0F.
 *
 * <p>
 * A standardized variation selector is a zero-width character that requests an
 * alternative glyph for the preceding character. It is removed from the value before the
 * size is measured when {@link #IGNORE} is used.
 * </p>
 *
 * @since 0.0.14
 */
public enum StandardizedVariationSequence {

	/**
	 * Removes standardized variation selectors before the size is measured.
	 */
	IGNORE(true), //
	/**
	 * Counts standardized variation selectors as characters.
	 */
	NOT_IGNORE(false);

	/**
	 * The standardized variation selectors as a range for a regular expression character
	 * class, from U+FE00 to U+FE0F.
	 */
	public static final String RANGE = "\uFE00-\uFE0F";

	private final boolean ignore;

	StandardizedVariationSequence(boolean ignore) {
		this.ignore = ignore;
	}

	/**
	 * Returns whether standardized variation selectors are removed before the size is
	 * measured.
	 * @return {@code true} for {@link #IGNORE}, {@code false} for {@link #NOT_IGNORE}
	 */
	public boolean ignore() {
		return this.ignore;
	}

}
