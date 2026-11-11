/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence.codepoints;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import am.ik.yavi.constraint.charsequence.CodePoints;

public enum UnicodeCodePoints implements CodePoints<String> {

	/**
	 * Hiragana listed in https://www.unicode.org/charts/nameslist/c_3040.html<br>
	 * Note that this is a bit different from JIS X 0208's row 4 (Hiragana, 0x3041-0x3093)
	 */
	HIRAGANA((CodePointsRanges<String>) () -> {
		return Collections.singletonList(Range.of(0x3041 /* ぁ */, 0x309F /* ゟ */));
	}), //
	/**
	 * Katakana and Katakana Phonetic Extensions listed in
	 * <ul>
	 * <li>https://www.unicode.org/charts/nameslist/c_30A0.html</li>
	 * <li>https://www.unicode.org/charts/nameslist/c_31F0.html</li>
	 * </ul>
	 * Note that this is different from JIS X 0208's row 5 (Katanaka, 0x30A1-0x30F6)
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

	@Override
	public Set<Integer> allExcludedCodePoints(String s) {
		return this.delegate.allExcludedCodePoints(s);
	}

}
