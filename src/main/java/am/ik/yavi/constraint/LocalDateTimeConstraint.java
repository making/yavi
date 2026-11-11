/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.LocalDateTime;

import am.ik.yavi.constraint.base.ChronoLocalDateTimeConstraintBase;

/**
 * Constraints on a {@link LocalDateTime} value.
 *
 * <p>
 * A {@code null} value is considered valid and the temporal checks are skipped. The
 * comparisons use the chronological ordering of {@link LocalDateTime}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @author Diego Krupitza
 * @since 0.10.0
 */
public class LocalDateTimeConstraint<T>
		extends ChronoLocalDateTimeConstraintBase<T, LocalDateTime, LocalDateTimeConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public LocalDateTimeConstraint() {
	}

	@Override
	public LocalDateTimeConstraint<T> cast() {
		return this;
	}

	@Override
	protected LocalDateTime getNow(Clock clock) {
		return LocalDateTime.now(clock);
	}

}
