package am.ik.yavi.constraint.time;

import am.ik.yavi.core.Constraint;

import java.time.chrono.ChronoZonedDateTime;
import java.util.function.Predicate;

/**
 * This is the base class for constraints on ChronoZonedDateTime.
 *
 * @author Diego Krupitza
 */
abstract class ChronoZonedDateTimeConstraintBase<T, V extends ChronoZonedDateTime<?>, C extends Constraint<T, V, C>>
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
