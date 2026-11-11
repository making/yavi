/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence.codepoints;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;

import static am.ik.yavi.constraint.charsequence.codepoints.UnicodeCodePoints.HIRAGANA;
import static am.ik.yavi.constraint.charsequence.codepoints.UnicodeCodePoints.KATAKANA;
import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.constraint.charsequence.CodePoints;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsSet;

class CompositeCodePointsTest {

	@Test
	void codePointRange() {
		CodePoints<String> codePoints = new CompositeCodePoints<>(HIRAGANA, KATAKANA);
		assertThat(codePoints.allExcludedCodePoints("あ")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("い")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("ア")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("イ")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("あア")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("あいアイ")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("E")).containsOnly(0x0045);
		assertThat(codePoints.allExcludedCodePoints("EF")).contains(0x0045, 0x0046);
		assertThat(codePoints.allExcludedCodePoints("あいアイE")).containsOnly(0x0045);
		assertThat(codePoints.allExcludedCodePoints("あいアイEF")).contains(0x0045, 0x0046);
	}

	@Test
	void codePointsSet() {
		CodePointsSet<String> cp1 = () -> new LinkedHashSet<>(
				Arrays.asList(0x0041 /* A */, 0x0042 /* B */));
		CodePointsSet<String> cp2 = () -> new LinkedHashSet<>(
				Arrays.asList(0x0043 /* C */, 0x0044 /* D */));
		CodePoints<String> codePoints = new CompositeCodePoints<>(cp1, cp2);
		assertThat(codePoints.allExcludedCodePoints("A")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("B")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("C")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("D")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("BC")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("ABCD")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("E")).containsOnly(0x0045);
		assertThat(codePoints.allExcludedCodePoints("EF")).contains(0x0045, 0x0046);
		assertThat(codePoints.allExcludedCodePoints("ABCDE")).containsOnly(0x0045);
		assertThat(codePoints.allExcludedCodePoints("ABCDEF")).contains(0x0045, 0x0046);
	}

	@Test
	void mix() {
		CodePointsSet<String> cp1 = () -> new LinkedHashSet<>(
				Arrays.asList(0x0041 /* A */, 0x0042 /* B */));
		CodePointsSet<String> cp2 = () -> new LinkedHashSet<>(
				Arrays.asList(0x0043 /* C */, 0x0044 /* D */));
		CodePoints<String> codePoints = new CompositeCodePoints<>(cp1, cp2, HIRAGANA);
		assertThat(codePoints.allExcludedCodePoints("A")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("B")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("C")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("D")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("あ")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("い")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("BCあ")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("ABCDあい")).isEmpty();
		assertThat(codePoints.allExcludedCodePoints("E")).containsOnly(0x0045);
		assertThat(codePoints.allExcludedCodePoints("EF")).contains(0x0045, 0x0046);
		assertThat(codePoints.allExcludedCodePoints("ABCDEあい")).containsOnly(0x0045);
		assertThat(codePoints.allExcludedCodePoints("ABCDEFあい")).contains(0x0045, 0x0046);
	}

}
