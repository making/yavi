package am.ik.yavi.constraint.time;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintPredicate;

import java.time.ZoneId;
import java.time.chrono.ChronoZonedDateTime;
import java.util.function.Predicate;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.*;

/**
 * This is the base class for constraints on ChronoZonedDateTime.
 *
 * @author Diego Krupitza
 */
public abstract class ChronoZonedDateTimeConstraintBase<T, V extends ChronoZonedDateTime<?>, C extends Constraint<T, V, C>>
		extends ComparableTemporalConstraintBase<T, V, C> {

	/**
	 * Is the given ChronoZonedDateTime in the same zone as {@code zoneId}
	 *
	 * @param zoneId the zone our object has to be too
	 */
	public C zone(ZoneId zoneId) {
		this.predicates().add(ConstraintPredicate.of(x -> x.getZone().equals(zoneId),
				ZONED_DATE_ZONE, () -> new Object[] { zoneId.toString() }, VALID));
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
