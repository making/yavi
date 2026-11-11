/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import am.ik.yavi.constraint.charsequence.CodePoints.Range;

class RangeTest {

	@Test
	void ofString() {
		Range range = Range.of("a", "z");
		Assertions.assertThat(range).isEqualTo(Range.of(0x0061 /* a */, 0x007a /* z */));
	}

}
