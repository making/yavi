/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

/**
 * Defines how a constraint treats a {@code null} value.
 *
 * <p>
 * This is used when a {@link ConstraintPredicate} is added: {@link #VALID} skips the
 * predicate for a {@code null} value, and {@link #INVALID} evaluates it.
 * </p>
 *
 * @since 0.0.18
 */
public enum NullAs {

	/**
	 * The predicate is evaluated for a {@code null} value, so {@code null} can be
	 * rejected.
	 */
	INVALID(false),

	/**
	 * The predicate is skipped for a {@code null} value, so {@code null} is considered
	 * valid.
	 */
	VALID(true);

	private final boolean skipNull;

	NullAs(boolean skipNull) {
		this.skipNull = skipNull;
	}

	/**
	 * Returns whether a constraint with this setting skips a {@code null} value.
	 * @return {@code true} if a {@code null} value is skipped and therefore valid
	 */
	public boolean skipNull() {
		return this.skipNull;
	}

}
