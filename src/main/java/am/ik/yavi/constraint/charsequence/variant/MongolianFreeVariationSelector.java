/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence.variant;

/**
 * Setting for the Mongolian free variation selectors, U+180B to U+180D.
 *
 * <p>
 * A Mongolian free variation selector is a zero-width character that selects a glyph
 * variant of the preceding Mongolian letter. It is removed from the value before the size
 * is measured when {@link #IGNORE} is used.
 * </p>
 *
 * @since 0.0.14
 */
public enum MongolianFreeVariationSelector {

	/**
	 * Removes Mongolian free variation selectors before the size is measured.
	 */
	IGNORE(true), //
	/**
	 * Counts Mongolian free variation selectors as characters.
	 */
	NOT_IGNORE(false);

	/**
	 * The Mongolian free variation selectors as a range for a regular expression
	 * character class, from U+180B to U+180D.
	 */
	public static final String RANGE = "\u180B-\u180D";

	private final boolean ignore;

	MongolianFreeVariationSelector(boolean ignore) {
		this.ignore = ignore;
	}

	/**
	 * Returns whether Mongolian free variation selectors are removed before the size is
	 * measured.
	 * @return {@code true} for {@link #IGNORE}, {@code false} for {@link #NOT_IGNORE}
	 */
	public boolean ignore() {
		return this.ignore;
	}

}
