/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.Year;

import am.ik.yavi.constraint.base.TemporalConstraintBase;

/**
 * Constraints on a {@link Year} value.
 *
 * <p>
 * A {@code null} value is considered valid and the temporal checks are skipped. The
 * comparisons use the chronological ordering of {@link Year}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.11.0
 */
public class YearConstraint<T> extends TemporalConstraintBase<T, Year, YearConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public YearConstraint() {
	}

	@Override
	protected boolean isAfter(Year a, Year b) {
		return a.isAfter(b);
	}

	@Override
	protected boolean isBefore(Year a, Year b) {
		return a.isBefore(b);
	}

	@Override
	protected Year getNow(Clock clock) {
		return Year.now(clock);
	}

	@Override
	public YearConstraint<T> cast() {
		return this;
	}

}
