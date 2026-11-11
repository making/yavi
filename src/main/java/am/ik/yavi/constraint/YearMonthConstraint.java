/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.YearMonth;

import am.ik.yavi.constraint.base.TemporalConstraintBase;

/**
 * Constraints on a {@link YearMonth} value.
 *
 * <p>
 * A {@code null} value is considered valid and the temporal checks are skipped. The
 * comparisons use the chronological ordering of {@link YearMonth}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.11.0
 */
public class YearMonthConstraint<T> extends TemporalConstraintBase<T, YearMonth, YearMonthConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public YearMonthConstraint() {
	}

	@Override
	protected boolean isAfter(YearMonth a, YearMonth b) {
		return a.isAfter(b);
	}

	@Override
	protected boolean isBefore(YearMonth a, YearMonth b) {
		return a.isBefore(b);
	}

	@Override
	protected YearMonth getNow(Clock clock) {
		return YearMonth.now(clock);
	}

	@Override
	public YearMonthConstraint<T> cast() {
		return this;
	}

}
