package am.ik.yavi.constraint.temporal;

import java.time.chrono.ChronoLocalDate;

import am.ik.yavi.core.Constraint;

/**
 * This is the base class for constraints on ChronoLocalDate.
 *
 * @author Diego Krupitza
 * @since 0.10.0
 */
abstract class ChronoLocalDateConstraintBase<T, V extends ChronoLocalDate, C extends Constraint<T, V, C>>
		extends ComparableTemporalConstraintBase<T, V, C> {
	public ChronoLocalDateConstraintBase() {
		super(ChronoLocalDate::isAfter, ChronoLocalDate::isBefore);
	}
}
