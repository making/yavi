/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.ZonedDateTime;

import am.ik.yavi.constraint.base.ChronoZonedDateTimeConstraintBase;

/**
 * Constraints on a {@link ZonedDateTime} value.
 *
 * <p>
 * A {@code null} value is considered valid and the temporal checks are skipped. The
 * comparisons use the instant-based ordering of {@link ZonedDateTime}: the zone and the
 * offset are taken into account.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @author Diego Krupitza
 * @since 0.10.0
 */
public class ZonedDateTimeConstraint<T>
		extends ChronoZonedDateTimeConstraintBase<T, ZonedDateTime, ZonedDateTimeConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public ZonedDateTimeConstraint() {
	}

	@Override
	protected ZonedDateTime getNow(Clock clock) {
		return ZonedDateTime.now(clock);
	}

	@Override
	public ZonedDateTimeConstraint<T> cast() {
		return this;
	}

}
