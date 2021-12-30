package am.ik.yavi.constraint.temporal;

import java.time.LocalTime;
import java.util.function.Predicate;

/**
 * This is the actual class for constraints on LocalTime.
 *
 * @author Diego Krupitza
 */
public class LocalTimeConstraint<T>
		extends ComparableTemporalConstraintBase<T, LocalTime, LocalTimeConstraint<T>> {
	@Override
	protected Predicate<LocalTime> isBetween(LocalTime rangeFrom, LocalTime rangeTo) {
		return x -> {
			if (rangeFrom.isAfter(rangeTo)) {
				throw new IllegalArgumentException(
						"Parameter 'rangeFrom' has to be before 'rangeTo'");
			}
			return rangeFrom.isBefore(x) && rangeTo.isAfter(x);
		};
	}

	@Override
	public LocalTimeConstraint<T> cast() {
		return this;
	}
}
