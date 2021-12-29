package am.ik.yavi.constraint.temporal;

import am.ik.yavi.core.Constraint;

import java.time.chrono.ChronoLocalDateTime;
import java.util.function.Predicate;

/**
 * This is the base class for constraints on ChronoLocalDateTime.
 *
 * @author Diego Krupitza
 * @since 0.10.0
 */
abstract class ChronoLocalDateTimeConstraintBase<T, V extends ChronoLocalDateTime<?>, C extends Constraint<T, V, C>>
		extends ComparableTemporalConstraintBase<T, V, C> {

	@Override
	protected Predicate<V> isBetween(V rangeFrom, V rangeTo) {
		return x -> {
			if (rangeFrom.isAfter(rangeTo)) {
				throw new IllegalArgumentException(
						"Parameter 'rangeFrom' has to be before 'rangeTo'");
			}
			return rangeFrom.isBefore(x) && rangeTo.isAfter(x);
		};
	}
}
