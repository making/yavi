/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence.variant;

public enum MongolianFreeVariationSelector {

	IGNORE(true), NOT_IGNORE(false);

	public static final String RANGE = "\u180B-\u180D";

	private final boolean ignore;

	MongolianFreeVariationSelector(boolean ignore) {
		this.ignore = ignore;
	}

	public boolean ignore() {
		return this.ignore;
	}

}
