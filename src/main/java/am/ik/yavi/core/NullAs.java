/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

public enum NullAs {

	INVALID(false), VALID(true);

	private final boolean skipNull;

	NullAs(boolean skipNull) {
		this.skipNull = skipNull;
	}

	public boolean skipNull() {
		return this.skipNull;
	}

}
