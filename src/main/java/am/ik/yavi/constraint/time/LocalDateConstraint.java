package am.ik.yavi.constraint.time;

import java.time.LocalDate;

/**
 * This is the actual class for constraints on LocalDate.
 *
 * @author Diego Krupitza
 */
public class LocalDateConstraint<T>
		extends ChronoLocalDateConstraintBase<T, LocalDate, LocalDateConstraint<T>> {

	@Override
	public LocalDateConstraint<T> cast() {
		return this;
	}
}
