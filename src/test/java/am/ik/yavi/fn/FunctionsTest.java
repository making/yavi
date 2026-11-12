/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.fn;

import java.util.Objects;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionsTest {

	@Test
	void curry() {
		final Function2<Integer, Integer, Integer> add = (x, y) -> Objects.requireNonNull(x)
				+ Objects.requireNonNull(y);
		final Function1<Integer, Integer> add10 = Functions.curry(add).apply(10);
		final Integer result = add10.apply(2);
		assertThat(result).isEqualTo(12);
	}

}