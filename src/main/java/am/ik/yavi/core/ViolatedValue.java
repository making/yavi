/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Objects;

import am.ik.yavi.jsr305.Nullable;

public class ViolatedValue {

	private final Object value;

	public ViolatedValue(@Nullable Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return Objects.toString(this.value, "");
	}

	@Nullable
	public Object value() {
		return this.value;
	}

}
