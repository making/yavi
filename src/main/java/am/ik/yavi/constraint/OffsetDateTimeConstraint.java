package am.ik.yavi.constraint;

import java.time.OffsetDateTime;

import am.ik.yavi.constraint.base.TemporalConstraintBase;

/**
 * This is the actual class for constraints on OffsetDateTime.
 * @since 0.10.0
 */
public class OffsetDateTimeConstraint<T>
		extends TemporalConstraintBase<T, OffsetDateTime, OffsetDateTimeConstraint<T>> {
	@Override
	protected boolean isAfter(OffsetDateTime a, OffsetDateTime b) {
		return a.isAfter(b);
	}

	@Override
	protected boolean isBefore(OffsetDateTime a, OffsetDateTime b) {
		return a.isBefore(b);
	}

	@Override
	public OffsetDateTimeConstraint<T> cast() {
		return this;
	}
}
