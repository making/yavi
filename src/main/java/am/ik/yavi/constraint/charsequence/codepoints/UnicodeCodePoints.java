/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence.codepoints;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import am.ik.yavi.constraint.charsequence.CodePoints;

/**
 * Groups of Japanese kana code points for the code point constraints.
 *
 * <p>
 * Use a constant as the {@link CodePoints} argument of
 * {@link am.ik.yavi.constraint.CharSequenceConstraint#codePoints(CodePoints)}, for
 * example {@code c.codePoints(UnicodeCodePoints.HIRAGANA).asWhiteList()} to accept
 * hiragana only.
 * </p>
 *
 * @since 0.0.14
 */
public enum UnicodeCodePoints implements CodePoints<String> {

	/**
	 * Hiragana listed in
	 * <a href= "https://www.unicode.org/charts/nameslist/c_3040.html">the Unicode
	 * chart</a>, from U+3041 to U+309F.
	 *
	 * <p>
	 * This is a bit different from JIS X 0208's row 4 (Hiragana, 0x3041-0x3093).
	 * </p>
	 */
	HIRAGANA((CodePointsRanges<String>) () -> {
		return Collections.singletonList(Range.of(0x3041 /* ぁ */, 0x309F /* ゟ */));
	}), //
	/**
	 * Katakana and katakana phonetic extensions listed in
	 * <ul>
	 * <li><a href= "https://www.unicode.org/charts/nameslist/c_30A0.html">the Unicode
	 * chart for Katakana</a></li>
	 * <li><a href= "https://www.unicode.org/charts/nameslist/c_31F0.html">the Unicode
	 * chart for Katakana Phonetic Extensions</a></li>
	 * </ul>
	 * from U+30A0 to U+30FF and from U+31F0 to U+31FF.
	 *
	 * <p>
	 * This is different from JIS X 0208's row 5 (Katakana, 0x30A1-0x30F6).
	 * </p>
	 */
	KATAKANA((CodePointsRanges<String>) () -> {
		return Arrays.asList( //
				Range.of(0x30A0 /* ゠ */, 0x30FF /* ヿ */),
				Range.of(0x31F0 /* ㇰ */, 0x31FF /* ㇿ */));
	});

	private final CodePoints<String> delegate;

	UnicodeCodePoints(CodePoints<String> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Returns the code points of the given value that are not in this group.
	 * @param s the value, possibly {@code null}
	 * @return the code points outside of the group, empty if {@code s} is {@code null} or
	 * empty
	 */
	@Override
	public Set<Integer> allExcludedCodePoints(String s) {
		return this.delegate.allExcludedCodePoints(s);
	}

}
