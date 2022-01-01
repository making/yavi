package am.ik.yavi.constraint.temporal;

import java.time.OffsetDateTime;

/**
 * This is the actual class for constraints on OffsetDateTime.
 * @since 0.10.0
 */
public class OffsetDateTimeConstraint<T> extends
		ComparableTemporalConstraintBase<T, OffsetDateTime, OffsetDateTimeConstraint<T>> {
	public OffsetDateTimeConstraint() {
		super(OffsetDateTime::isAfter, OffsetDateTime::isBefore);
	}

	@Override
	public OffsetDateTimeConstraint<T> cast() {
		return this;
	}
}
