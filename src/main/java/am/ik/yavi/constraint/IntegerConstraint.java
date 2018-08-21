package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumberConstraintBase;

public class IntegerConstraint<T>
		extends NumberConstraintBase<T, Integer, IntegerConstraint<T>> {
	@Override
	protected Predicate<Integer> isGreaterThan(Integer min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Integer> isGreaterThanOrEquals(Integer min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Integer> isLessThan(Integer max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Integer> isLessThanOrEquals(Integer max) {
		return x -> x <= max;
	}

	@Override
	public IntegerConstraint<T> cast() {
		return this;
	}
}
