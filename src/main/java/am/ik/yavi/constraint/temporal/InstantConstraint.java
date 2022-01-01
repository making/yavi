package am.ik.yavi.constraint.temporal;

import java.time.Instant;

/**
 * This is the actual class for constraints on Instant.
 * @since 0.10.0
 */
public class InstantConstraint<T>
		extends ComparableTemporalConstraintBase<T, Instant, InstantConstraint<T>> {
	public InstantConstraint() {
		super(Instant::isAfter, Instant::isBefore);
	}

	@Override
	public InstantConstraint<T> cast() {
		return this;
	}
}
