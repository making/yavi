package am.ik.yavi.constraint.time;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintPredicate;

import java.time.chrono.ChronoLocalDate;
import java.util.function.Predicate;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.*;

/**
 * This is the base class for constraints on ChronoLocalDate.
 *
 * @author Diego Krupitza
 */
abstract class ChronoLocalDateConstraintBase<T, V extends ChronoLocalDate, C extends Constraint<T, V, C>>
		extends ComparableTemporalConstraintBase<T, V, C> {

	/** Is the given ChronoLocalDate a leap year */
	public C leapYear() {
		this.predicates().add(ConstraintPredicate.of(ChronoLocalDate::isLeapYear,
				TEMPORAL_LEAP_YEAR, () -> new Object[] {}, VALID));
		return cast();
	}

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
