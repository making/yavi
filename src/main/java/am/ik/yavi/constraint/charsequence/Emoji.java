/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.constraint.charsequence;

public class Emoji {
	private static final String ZERO_WIDTH_JOINER = "\u200D";
	private static final String VARIATION_SELECTOR_RANGE = "\uFE00-\uFE0F";
	private static final String COMBINING_ENCLOSING_KEYCAP = new String(
			new int[] { 0x20E3 }, 0, 1);
	private static final String SKIN_TONE_SELECTOR_RANGE = new String(
			new int[] { 0x1F3FB }, 0, 1) + "-" + new String(new int[] { 0x1F3FF }, 0, 1);
	private static final String REGIONAL_INDICATOR_SYMBOL_LETTER_RANGE = new String(
			new int[] { 0x1F1E6 }, 0, 1) + "-" + new String(new int[] { 0x1F1FF }, 0, 1);
	private static final String WHITE_UP_POINTING_INDEX = new String(new int[] { 0x261D },
			0, 1);
	private static final String ELF = new String(new int[] { 0x1F9DD }, 0, 1);
	private static final String SKUL_AND_CROSSBONES = new String(new int[] { 0x2620 }, 0,
			1);
	private static final String EMOJI_COMPONENT_WHITE_HAIR = new String(
			new int[] { 0x1F9B3 }, 0, 1);
	private static final String ENGLAND = new String(
			new int[] { 0x1F3F4, 0xE0067, 0xE0062, 0xE0065, 0xE006E, 0xE0067, 0xE007F },
			0, 7);
	private static final String SCOTLAND = new String(
			new int[] { 0x1F3F4, 0xE0067, 0xE0062, 0xE0073, 0xE0063, 0xE0074, 0xE007F },
			0, 7);
	private static final String WALES = new String(
			new int[] { 0x1F3F4, 0xE0067, 0xE0062, 0xE0077, 0xE006C, 0xE0073, 0xE007F },
			0, 7);

	/**
	 * Try to return the length of the given string.<br>
	 * This method does not grantee the exact length.
	 * @see <a href="https://unicode.org/Public/emoji/11.0/emoji-test.txt">Emoji 11.0</a>
	 * @param str
	 * @return the length of the given string which may be true
	 */
	public static int bestEffortCount(String str) {
		if (str == null || str.isEmpty()) {
			return 0;
		}
		String s = str
				.replaceAll(
						"[" + VARIATION_SELECTOR_RANGE + COMBINING_ENCLOSING_KEYCAP + "]",
						"") //
				.replaceAll("([" + WHITE_UP_POINTING_INDEX + "-" + ELF + "]["
						+ SKIN_TONE_SELECTOR_RANGE + "])", "X")
				.replaceAll("([" + ZERO_WIDTH_JOINER + "][" + SKUL_AND_CROSSBONES + "-"
						+ EMOJI_COMPONENT_WHITE_HAIR + "])", "") //
				.replaceAll("([" + REGIONAL_INDICATOR_SYMBOL_LETTER_RANGE + "]{2})", "X") //
				.replace(ENGLAND, "X") //
				.replace(SCOTLAND, "X") //
				.replace(WALES, "X");
		return s.codePointCount(0, s.length());
	}
}
