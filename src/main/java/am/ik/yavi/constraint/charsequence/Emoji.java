/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence;

import am.ik.yavi.constraint.charsequence.variant.StandardizedVariationSequence;
import am.ik.yavi.jsr305.Nullable;

/**
 * Best-effort counter for the number of emoji in a string.
 *
 * <p>
 * The counter is used by {@link EmojiConstraint}. It recognizes emoji built from several
 * code points, such as skin tone modifiers, keycaps, regional indicator pairs,
 * subdivision flags and ZWJ sequences, and counts each of them as one.
 * </p>
 *
 * @since 0.0.12
 */
public class Emoji {

	private static final String COMBINING_ENCLOSING_KEYCAP = new String(new int[] { 0x20E3 }, 0, 1);

	private static final String DUMMY_REPLACEMENT = "X";

	private static final String ELF = new String(new int[] { 0x1F9DD }, 0, 1);

	private static final String E140_SKIN_RANGE = new String(new int[] { 0x1FAF0 }, 0, 1) + "-"
			+ new String(new int[] { 0x1FAF6 }, 0, 1) + new String(new int[] { 0x1FAC3 }, 0, 1) + "-"
			+ new String(new int[] { 0x1FAC5 }, 0, 1);

	private static final String ENGLAND = new String(
			new int[] { 0x1F3F4, 0xE0067, 0xE0062, 0xE0065, 0xE006E, 0xE0067, 0xE007F }, 0, 7);

	private static final String PERSON = new String(new int[] { 0x1F9D1 }, 0, 1);

	private static final String REGIONAL_INDICATOR_SYMBOL_LETTER_RANGE = new String(new int[] { 0x1F1E6 }, 0, 1) + "-"
			+ new String(new int[] { 0x1F1FF }, 0, 1);

	private static final String SCOTLAND = new String(
			new int[] { 0x1F3F4, 0xE0067, 0xE0062, 0xE0073, 0xE0063, 0xE0074, 0xE007F }, 0, 7);

	private static final String SKIN_TONE_SELECTOR_RANGE = new String(new int[] { 0x1F3FB }, 0, 1) + "-"
			+ new String(new int[] { 0x1F3FF }, 0, 1);

	private static final String SKUL_AND_CROSSBONES = new String(new int[] { 0x2620 }, 0, 1);

	private static final String WALES = new String(
			new int[] { 0x1F3F4, 0xE0067, 0xE0062, 0xE0077, 0xE006C, 0xE0073, 0xE007F }, 0, 7);

	private static final String WHITE_UP_POINTING_INDEX = new String(new int[] { 0x261D }, 0, 1);

	private static final String ADHESIVE_BANDAGE = new String(new int[] { 0x1FA79 }, 0, 1);

	private static final String ZERO_WIDTH_JOINER = "\u200D";

	/**
	 * Creates an instance. The class has no state beyond the static counter.
	 */
	public Emoji() {
	}

	/**
	 * Tries to return the number of emoji in the given string.
	 *
	 * <p>
	 * The count is not guaranteed to be exact: emoji that are not known to this method,
	 * for instance newer ones, are counted by their remaining code points.
	 * </p>
	 * @param str the string to count, possibly {@code null}
	 * @return the best-effort number of emoji, zero for {@code null} or an empty string
	 * @see <a href="https://unicode.org/Public/emoji/12.0/emoji-test.txt">Emoji 12.0</a>
	 */
	public static int bestEffortCount(@Nullable String str) {
		if (str == null || str.isEmpty()) {
			return 0;
		}
		String s = str.replaceAll("[" + StandardizedVariationSequence.RANGE + COMBINING_ENCLOSING_KEYCAP + "]", "") //
			.replaceAll("([" + WHITE_UP_POINTING_INDEX + "-" + ELF + E140_SKIN_RANGE + "][" + SKIN_TONE_SELECTOR_RANGE
					+ "])", DUMMY_REPLACEMENT)
			.replaceAll("([" + ZERO_WIDTH_JOINER + "][" + SKUL_AND_CROSSBONES + "-" + PERSON + ADHESIVE_BANDAGE + "])",
					"") //
			.replaceAll("([" + REGIONAL_INDICATOR_SYMBOL_LETTER_RANGE + "]{2})", DUMMY_REPLACEMENT) //
			.replace(ENGLAND, DUMMY_REPLACEMENT) //
			.replace(SCOTLAND, DUMMY_REPLACEMENT) //
			.replace(WALES, DUMMY_REPLACEMENT) //
			// Support emojis that contains two skin tone selectors introduced in 12
			.replaceAll("(" + DUMMY_REPLACEMENT + ZERO_WIDTH_JOINER + DUMMY_REPLACEMENT + ")", DUMMY_REPLACEMENT);
		return s.codePointCount(0, s.length());
	}

}
