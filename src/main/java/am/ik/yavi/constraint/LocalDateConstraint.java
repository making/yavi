/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.LocalDate;

import am.ik.yavi.constraint.base.ChronoLocalDateConstraintBase;

/**
 * Constraints on a {@link LocalDate} value.
 *
 * <p>
 * A {@code null} value is considered valid and the temporal checks are skipped. The
 * comparisons use the chronological ordering of {@link LocalDate}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @author Diego Krupitza
 * @since 0.10.0
 */
public class LocalDateConstraint<T> extends ChronoLocalDateConstraintBase<T, LocalDate, LocalDateConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public LocalDateConstraint() {
	}

	@Override
	public LocalDateConstraint<T> cast() {
		return this;
	}

	@Override
	protected LocalDate getNow(Clock clock) {
		return LocalDate.now(clock);
	}

}
