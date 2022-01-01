package am.ik.yavi.constraint.base;

import java.time.chrono.ChronoLocalDateTime;

import am.ik.yavi.core.Constraint;

/**
 * This is the base class for constraints on ChronoLocalDateTime.
 *
 * @author Diego Krupitza
 * @since 0.10.0
 */
public abstract class ChronoLocalDateTimeConstraintBase<T, V extends ChronoLocalDateTime<?>, C extends Constraint<T, V, C>>
		extends TemporalConstraintBase<T, V, C> {
	@Override
	protected boolean isAfter(V a, V b) {
		return a.isAfter(b);
	}

	@Override
	protected boolean isBefore(V a, V b) {
		return a.isBefore(b);
	}
}
