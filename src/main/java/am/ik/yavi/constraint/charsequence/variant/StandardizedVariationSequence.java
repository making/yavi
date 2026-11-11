/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.charsequence.variant;

public enum StandardizedVariationSequence {

	IGNORE(true), NOT_IGNORE(false);

	public static final String RANGE = "\uFE00-\uFE0F";

	private final boolean ignore;

	StandardizedVariationSequence(boolean ignore) {
		this.ignore = ignore;
	}

	public boolean ignore() {
		return this.ignore;
	}

}
