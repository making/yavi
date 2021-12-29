package am.ik.yavi.constraint.time;

import java.time.ZonedDateTime;

/**
 * This is the actual class for constraints on ZonedDateTime.
 *
 * @author Diego Krupitza
 */
public class ZonedDateTimeConstraint<T> extends
		ChronoZonedDateTimeConstraintBase<T, ZonedDateTime, ZonedDateTimeConstraint<T>> {

	@Override
	public ZonedDateTimeConstraint<T> cast() {
		return this;
	}
}
