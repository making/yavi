package am.ik.yavi.constraint.temporal;

import java.time.Instant;

/**
 * This is the actual class for constraints on Instant.
 * @since 0.10.0
 */
public class InstantConstraint<T>
		extends ComparableTemporalConstraintBase<T, Instant, InstantConstraint<T>> {
	@Override
	protected boolean isAfter(Instant a, Instant b) {
		return a.isAfter(b);
	}

	@Override
	protected boolean isBefore(Instant a, Instant b) {
		return a.isBefore(b);
	}

	@Override
	public InstantConstraint<T> cast() {
		return this;
	}
}
