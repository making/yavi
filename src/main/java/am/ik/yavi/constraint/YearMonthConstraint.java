/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.YearMonth;

import am.ik.yavi.constraint.base.TemporalConstraintBase;

/**
 * This is the actual class for constraints on YearMonth.
 *
 * @since 0.11.0
 */
public class YearMonthConstraint<T> extends TemporalConstraintBase<T, YearMonth, YearMonthConstraint<T>> {

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
