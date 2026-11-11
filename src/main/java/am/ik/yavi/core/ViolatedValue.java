/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Objects;

import am.ik.yavi.jsr305.Nullable;

/**
 * Wraps the value that violated a constraint.
 *
 * <p>
 * The wrapped value is appended to the message arguments of a violation. Use
 * {@link #value()} to access it; {@link #toString()} returns its string form or an empty
 * string when it is {@code null}.
 *
 * @since 0.0.10
 */
public class ViolatedValue {

	private final Object value;

	/**
	 * Creates a new instance.
	 * @param value the value that violated the constraint; may be {@code null}
	 */
	public ViolatedValue(@Nullable Object value) {
		this.value = value;
	}

	/**
	 * Returns the string form of the wrapped value, or an empty string when the value is
	 * {@code null}.
	 * @return the string form of the value
	 */
	@Override
	public String toString() {
		return Objects.toString(this.value, "");
	}

	/**
	 * Returns the value that violated the constraint.
	 * @return the violated value; may be {@code null}
	 */
	@Nullable
	public Object value() {
		return this.value;
	}

}
