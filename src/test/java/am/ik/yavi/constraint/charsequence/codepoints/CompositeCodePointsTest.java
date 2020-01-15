/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.constraint.charsequence.codepoints;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.junit.Test;

import static am.ik.yavi.constraint.charsequence.codepoints.UnicodeCodePoints.HIRAGANA;
import static am.ik.yavi.constraint.charsequence.codepoints.UnicodeCodePoints.KATAKANA;
import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.constraint.charsequence.CodePoints;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsSet;

public class CompositeCodePointsTest {

	@Test
	public void codePointRange() {
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
	public void codePointsSet() {
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
	public void mix() {
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
