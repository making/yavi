package am.ik.yavi.constraint;

import java.time.ZonedDateTime;

import am.ik.yavi.constraint.base.ChronoZonedDateTimeConstraintBase;

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
