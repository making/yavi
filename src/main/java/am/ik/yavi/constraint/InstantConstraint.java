/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.Instant;

import am.ik.yavi.constraint.base.TemporalConstraintBase;

/**
 * Constraints on an {@link Instant} value.
 *
 * <p>
 * A {@code null} value is considered valid and the temporal checks are skipped. The
 * comparisons use the chronological ordering of {@link Instant}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.10.0
 */
public class InstantConstraint<T> extends TemporalConstraintBase<T, Instant, InstantConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public InstantConstraint() {
	}

	@Override
	protected boolean isAfter(Instant a, Instant b) {
		return a.isAfter(b);
	}

	@Override
	protected boolean isBefore(Instant a, Instant b) {
		return a.isBefore(b);
	}

	@Override
	protected Instant getNow(Clock clock) {
		return Instant.now(clock);
	}

	@Override
	public InstantConstraint<T> cast() {
		return this;
	}

}
