package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumberConstraintBase;

public class ShortConstraint<T>
		extends NumberConstraintBase<T, Short, ShortConstraint<T>> {
	@Override
	protected Predicate<Short> isGreaterThan(Short min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Short> isGreaterThanOrEquals(Short min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Short> isLessThan(Short max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Short> isLessThanOrEquals(Short max) {
		return x -> x <= max;
	}

	@Override
	public ShortConstraint<T> cast() {
		return this;
	}
}
