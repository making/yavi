package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumberConstraintBase;

public class DoubleConstraint<T>
		extends NumberConstraintBase<T, Double, DoubleConstraint<T>> {
	@Override
	protected Predicate<Double> isGreaterThan(Double min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Double> isGreaterThanOrEquals(Double min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Double> isLessThan(Double max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Double> isLessThanOrEquals(Double max) {
		return x -> x <= max;
	}

	@Override
	public DoubleConstraint<T> cast() {
		return this;
	}
}
