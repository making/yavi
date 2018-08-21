package am.ik.yavi.constraint;

import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumberConstraintBase;

public class ByteConstraint<T> extends NumberConstraintBase<T, Byte, ByteConstraint<T>> {
	@Override
	protected Predicate<Byte> isGreaterThan(Byte min) {
		return x -> x > min;
	}

	@Override
	protected Predicate<Byte> isGreaterThanOrEquals(Byte min) {
		return x -> x >= min;
	}

	@Override
	protected Predicate<Byte> isLessThan(Byte max) {
		return x -> x < max;
	}

	@Override
	protected Predicate<Byte> isLessThanOrEquals(Byte max) {
		return x -> x <= max;
	}

	@Override
	public ByteConstraint<T> cast() {
		return this;
	}
}
