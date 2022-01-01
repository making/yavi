package am.ik.yavi.constraint.temporal;

import java.time.chrono.ChronoZonedDateTime;

import am.ik.yavi.core.Constraint;

/**
 * This is the base class for constraints on ChronoZonedDateTime.
 *
 * @author Diego Krupitza
 * @since 0.10.0
 */
abstract class ChronoZonedDateTimeConstraintBase<T, V extends ChronoZonedDateTime<?>, C extends Constraint<T, V, C>>
		extends ComparableTemporalConstraintBase<T, V, C> {
	public ChronoZonedDateTimeConstraintBase() {
		super(ChronoZonedDateTime::isAfter, ChronoZonedDateTime::isBefore);
	}
}
