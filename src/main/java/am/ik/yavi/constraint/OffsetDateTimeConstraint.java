/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.OffsetDateTime;

import am.ik.yavi.constraint.base.TemporalConstraintBase;

/**
 * Constraints on an {@link OffsetDateTime} value.
 *
 * <p>
 * A {@code null} value is considered valid and the temporal checks are skipped. The
 * comparisons use the instant-based ordering of {@link OffsetDateTime}: the offset is
 * taken into account.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @since 0.10.0
 */
public class OffsetDateTimeConstraint<T>
		extends TemporalConstraintBase<T, OffsetDateTime, OffsetDateTimeConstraint<T>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public OffsetDateTimeConstraint() {
	}

	@Override
	protected boolean isAfter(OffsetDateTime a, OffsetDateTime b) {
		return a.isAfter(b);
	}

	@Override
	protected boolean isBefore(OffsetDateTime a, OffsetDateTime b) {
		return a.isBefore(b);
	}

	@Override
	protected OffsetDateTime getNow(Clock clock) {
		return OffsetDateTime.now(clock);
	}

	@Override
	public OffsetDateTimeConstraint<T> cast() {
		return this;
	}

}
