package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumberConstraintBase;

public class LongConstraint<T> extends NumberConstraintBase<T, Long, LongConstraint<T>> {
	@Override
	protected Predicate<Long> isGreaterThan(Long min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Long> isGreaterThanOrEquals(Long min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Long> isLessThan(Long max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Long> isLessThanOrEquals(Long max) {
		return x -> x <= max;
	}

	@Override
	public LongConstraint<T> cast() {
		return this;
	}
}
