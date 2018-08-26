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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsRanges;
import am.ik.yavi.constraint.charsequence.CodePoints.CodePointsSet;
import am.ik.yavi.constraint.charsequence.CodePoints.Range;
import am.ik.yavi.core.ConstraintPredicate;

public class CodePointsConstraintTest {

	@Test
	public void allIncludedSet() {
		CodePointsSet<String> whiteList = () -> new HashSet<>(
				Arrays.asList(0x0041 /* A */, 0x0042 /* B */, 0x0043 /* C */,
						0x0044 /* D */, 0x0045 /* E */, 0x0046 /* F */, 0x0047 /* G */,
						0x0048 /* H */, 0x0049 /* I */, 0x004A /* J */, 0x004B /* K */,
						0x004C /* L */, 0x004D /* M */, 0x004E /* N */, 0x004F /* O */,
						0x0050 /* P */, 0x0051 /* Q */, 0x0052 /* R */, 0x0053 /* S */,
						0x0054 /* T */, 0x0055 /* U */, 0x0056 /* V */, 0x0057 /* W */,
						0x0058 /* X */, 0x0059 /* Y */, 0x005A /* Z */, //
						0x0061 /* a */, 0x0062 /* b */, 0x0063 /* c */, 0x0064 /* d */,
						0x0065 /* e */, 0x0066 /* f */, 0x0067 /* g */, 0x0068 /* h */,
						0x0069 /* i */, 0x006A /* j */, 0x006B /* k */, 0x006C /* l */,
						0x006D /* m */, 0x006E /* n */, 0x006F /* o */, 0x0070 /* p */,
						0x0071 /* q */, 0x0072 /* r */, 0x0073 /* s */, 0x0074 /* t */,
						0x0075 /* u */, 0x0076 /* v */, 0x0077 /* w */, 0x0078 /* x */,
						0x0079 /* y */, 0x007A /* z */));

		ConstraintPredicate<String> predicate = new CharSequenceConstraint<String, String>()
				.codePoints(whiteList).allIncluded().predicates().get(0);

		assertThat(predicate.violatedValue("ABCD").isPresent()).isFalse();
		assertThat(predicate.violatedValue("ABcD").isPresent()).isFalse();
		assertThat(predicate.violatedValue("AbcD").isPresent()).isFalse();
		assertThat(predicate.violatedValue("AbCＤ").get().value())
				.isEqualTo(Collections.singletonList("Ｄ"));
		assertThat(predicate.violatedValue("AbあCＤ").get().value())
				.isEqualTo(Arrays.asList("あ", "Ｄ"));
	}

	@Test
	public void allIncludedRange() {
		CodePointsRanges<String> whiteList = () -> Arrays.asList(
				Range.of(0x0041/* A */, 0x005A /* Z */),
				Range.of(0x0061/* a */, 0x007A /* z */));

		ConstraintPredicate<String> predicate = new CharSequenceConstraint<String, String>()
				.codePoints(whiteList).allIncluded().predicates().get(0);

		assertThat(predicate.violatedValue("ABCD").isPresent()).isFalse();
		assertThat(predicate.violatedValue("ABcD").isPresent()).isFalse();
		assertThat(predicate.violatedValue("AbcD").isPresent()).isFalse();
		assertThat(predicate.violatedValue("AbCＤ").get().value())
				.isEqualTo(Collections.singletonList("Ｄ"));
		assertThat(predicate.violatedValue("AbあCＤ").get().value())
				.isEqualTo(Arrays.asList("あ", "Ｄ"));
	}

	@Test
	public void notIncludedSet() {
		CodePointsSet<String> blackList = () -> new HashSet<>(
				Arrays.asList(0x0041 /* A */, 0x0042 /* B */));

		ConstraintPredicate<String> predicate = new CharSequenceConstraint<String, String>()
				.codePoints(blackList).notIncluded().predicates().get(0);

		assertThat(predicate.violatedValue("CD").isPresent()).isFalse();
		assertThat(predicate.violatedValue("ab").isPresent()).isFalse();
		assertThat(predicate.violatedValue("abCD").isPresent()).isFalse();
		assertThat(predicate.violatedValue("AbCD").get().value())
				.isEqualTo(Collections.singletonList("A"));
		assertThat(predicate.violatedValue("ABCD").get().value())
				.isEqualTo(Arrays.asList("A", "B"));
	}

	@Test
	public void notIncludedRange() {
		CodePointsRanges<String> blackList = () -> Arrays.asList(
				Range.of(0x0041/* A */, 0x0042 /* B */),
				Range.of(0x0061/* a */, 0x0062 /* b */));

		ConstraintPredicate<String> predicate = new CharSequenceConstraint<String, String>()
				.codePoints(blackList).notIncluded().predicates().get(0);

		assertThat(predicate.violatedValue("CD").isPresent()).isFalse();
		assertThat(predicate.violatedValue("cd").isPresent()).isFalse();
		assertThat(predicate.violatedValue("ABCD").get().value())
				.isEqualTo(Arrays.asList("A", "B"));
		assertThat(predicate.violatedValue("AbCD").get().value())
				.isEqualTo(Arrays.asList("A", "b"));
	}
}