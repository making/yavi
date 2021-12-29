package am.ik.yavi.constraint.temporal;

import java.time.ZonedDateTime;

/**
 * This is the actual class for constraints on ZonedDateTime.
 *
 * @author Diego Krupitza
 * @since 0.10.0
 */
public class ZonedDateTimeConstraint<T> extends
		ChronoZonedDateTimeConstraintBase<T, ZonedDateTime, ZonedDateTimeConstraint<T>> {

	@Override
	public ZonedDateTimeConstraint<T> cast() {
		return this;
	}
}
