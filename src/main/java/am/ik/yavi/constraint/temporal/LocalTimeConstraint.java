package am.ik.yavi.constraint.temporal;

import java.time.LocalTime;

/**
 * This is the actual class for constraints on LocalTime.
 *
 * @author Diego Krupitza
 * @since 0.10.0
 */
public class LocalTimeConstraint<T>
		extends ComparableTemporalConstraintBase<T, LocalTime, LocalTimeConstraint<T>> {
	public LocalTimeConstraint() {
		super(LocalTime::isAfter, LocalTime::isBefore);
	}

	@Override
	public LocalTimeConstraint<T> cast() {
		return this;
	}
}
