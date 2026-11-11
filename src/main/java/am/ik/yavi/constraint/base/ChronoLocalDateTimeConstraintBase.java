/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.base;

import java.time.chrono.ChronoLocalDateTime;

import am.ik.yavi.core.Constraint;

/**
 * Base class for constraints on {@link ChronoLocalDateTime} values.
 *
 * <p>
 * The comparisons are delegated to the {@code isAfter} and {@code isBefore} methods of
 * {@link ChronoLocalDateTime}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <V> the {@link ChronoLocalDateTime} type the constraints apply to
 * @param <C> the concrete constraint type returned for method chaining
 * @author Diego Krupitza
 * @since 0.10.0
 */
public abstract class ChronoLocalDateTimeConstraintBase<T, V extends ChronoLocalDateTime<?>, C extends Constraint<T, V, C>>
		extends TemporalConstraintBase<T, V, C> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public ChronoLocalDateTimeConstraintBase() {
	}

	@Override
	protected boolean isAfter(V a, V b) {
		return a.isAfter(b);
	}

	@Override
	protected boolean isBefore(V a, V b) {
		return a.isBefore(b);
	}

}
