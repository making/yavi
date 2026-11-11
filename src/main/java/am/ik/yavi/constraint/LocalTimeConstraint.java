/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.LocalTime;

import am.ik.yavi.constraint.base.TemporalConstraintBase;

/**
 * Constraints on a {@link LocalTime} value.
 *
 * <p>
 * A {@code null} value is considered valid and the temporal checks are skipped. The
 * comparisons use the time-of-day ordering of {@link LocalTime}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @author Diego Krupitza
 * @since 0.10.0
 */
public class LocalTimeConstraint<T> extends TemporalConstraintBase<T, LocalTime, LocalTimeConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public LocalTimeConstraint() {
	}

	@Override
	protected boolean isAfter(LocalTime a, LocalTime b) {
		return a.isAfter(b);
	}

	@Override
	protected boolean isBefore(LocalTime a, LocalTime b) {
		return a.isBefore(b);
	}

	@Override
	protected LocalTime getNow(Clock clock) {
		return LocalTime.now(clock);
	}

	@Override
	public LocalTimeConstraint<T> cast() {
		return this;
	}

}
