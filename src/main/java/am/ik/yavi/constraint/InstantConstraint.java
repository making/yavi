package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.Instant;

import am.ik.yavi.constraint.base.TemporalConstraintBase;

/**
 * This is the actual class for constraints on Instant.
 * @since 0.10.0
 */
public class InstantConstraint<T>
		extends TemporalConstraintBase<T, Instant, InstantConstraint<T>> {
	@Override
	protected boolean isAfter(Instant a, Instant b) {
		return a.isAfter(b);
	}

	@Override
	protected boolean isBefore(Instant a, Instant b) {
		return a.isBefore(b);
	}

	@Override
	protected Instant getNow(Clock clock) {
		return Instant.now(clock);
	}

	@Override
	public InstantConstraint<T> cast() {
		return this;
	}
}
