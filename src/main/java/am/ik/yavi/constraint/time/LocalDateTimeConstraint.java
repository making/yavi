package am.ik.yavi.constraint.time;

import java.time.LocalDateTime;

/**
 * This is the actual class for constraints on LocalDateTime.
 *
 * @author Diego Krupitza
 */
public class LocalDateTimeConstraint<T> extends
		ChronoLocalDateTimeConstraintBase<T, LocalDateTime, LocalDateTimeConstraint<T>> {

	@Override
	public LocalDateTimeConstraint<T> cast() {
		return this;
	}
}
