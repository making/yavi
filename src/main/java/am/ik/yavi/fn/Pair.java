/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.fn;

import java.util.Objects;

/**
 * An immutable pair of two values.
 *
 * <p>
 * Neither value is required to be non-{@code null}. Two pairs are equal if both of their
 * values are equal.
 *
 * @param <F> type of the first value
 * @param <S> type of the second value
 * @since 0.0.18
 */
public final class Pair<F, S> {

	private final F first;

	private final S second;

	/**
	 * Creates a pair of the given values.
	 * @param first the first value
	 * @param second the second value
	 */
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

	/**
	 * Returns the first value.
	 * @return the first value
	 */
	public F first() {
		return this.first;
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	/**
	 * Returns the second value.
	 * @return the second value
	 */
	public S second() {
		return this.second;
	}

	@Override
	public String toString() {
		return "Pair{" + "first=" + first + ", second=" + second + '}';
	}

}
