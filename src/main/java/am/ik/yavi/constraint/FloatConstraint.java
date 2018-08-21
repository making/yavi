package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumberConstraintBase;

public class FloatConstraint<T>
		extends NumberConstraintBase<T, Float, FloatConstraint<T>> {
	@Override
	protected Predicate<Float> isGreaterThan(Float min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Float> isGreaterThanOrEquals(Float min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Float> isLessThan(Float max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Float> isLessThanOrEquals(Float max) {
		return x -> x <= max;
	}

	@Override
	public FloatConstraint<T> cast() {
		return this;
	}
}
