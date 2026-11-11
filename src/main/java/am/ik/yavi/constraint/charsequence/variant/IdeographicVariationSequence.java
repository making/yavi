/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence.variant;

/**
 * Setting for the Unicode ideographic variation selectors, from U+E0100 to U+E01EF.
 *
 * <p>
 * An ideographic variation selector is a zero-width character that requests an
 * alternative glyph for the preceding ideograph. It is removed from the value before the
 * size is measured when {@link #IGNORE} is used.
 * </p>
 *
 * @since 0.0.14
 */
public enum IdeographicVariationSequence {

	/**
	 * Removes ideographic variation selectors before the size is measured.
	 */
	IGNORE(true), //
	/**
	 * Counts ideographic variation selectors as characters.
	 */
	NOT_IGNORE(false);

	/**
	 * The ideographic variation selectors as a range for a regular expression character
	 * class, from U+E0100 to U+E01EF.
	 */
	public static final String RANGE = new String(new int[] { 0xE0100 }, 0, 1) + "-"
			+ new String(new int[] { 0xE01EF }, 0, 1);

	private final boolean ignore;

	IdeographicVariationSequence(boolean ignore) {
		this.ignore = ignore;
	}

	/**
	 * Returns whether ideographic variation selectors are removed before the size is
	 * measured.
	 * @return {@code true} for {@link #IGNORE}, {@code false} for {@link #NOT_IGNORE}
	 */
	public boolean ignore() {
		return this.ignore;
	}

}
