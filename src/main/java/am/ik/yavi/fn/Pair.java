/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.fn;

import java.util.Objects;

public final class Pair<F, S> {

	private final F first;

	private final S second;

	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
	}

	public F first() {
		return this.first;
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	public S second() {
		return this.second;
	}

	@Override
	public String toString() {
		return "Pair{" + "first=" + first + ", second=" + second + '}';
	}

}
