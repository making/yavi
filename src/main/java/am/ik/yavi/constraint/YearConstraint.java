/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.Year;

import am.ik.yavi.constraint.base.TemporalConstraintBase;

/**
 * This is the actual class for constraints on Year.
 *
 * @since 0.11.0
 */
public class YearConstraint<T> extends TemporalConstraintBase<T, Year, YearConstraint<T>> {

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
