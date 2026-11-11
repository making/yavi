/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.LocalDate;

import am.ik.yavi.constraint.base.ChronoLocalDateConstraintBase;

/**
 * This is the actual class for constraints on LocalDate.
 *
 * @author Diego Krupitza
 * @since 0.10.0
 */
public class LocalDateConstraint<T> extends ChronoLocalDateConstraintBase<T, LocalDate, LocalDateConstraint<T>> {

	@Override
	public LocalDateConstraint<T> cast() {
		return this;
	}

	@Override
	protected LocalDate getNow(Clock clock) {
		return LocalDate.now(clock);
	}

}
