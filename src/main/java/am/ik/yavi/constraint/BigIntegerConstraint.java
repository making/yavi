package am.ik.yavi.constraint;

import java.math.BigInteger;
import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumberConstraintBase;

public class BigIntegerConstraint<T>
		extends NumberConstraintBase<T, BigInteger, BigIntegerConstraint<T>> {
	@Override
	protected Predicate<BigInteger> isGreaterThan(BigInteger min) {
		return x -> x.compareTo(min) > 0;
	}

	@Override
	protected Predicate<BigInteger> isGreaterThanOrEquals(BigInteger min) {
		return x -> x.compareTo(min) >= 0;
	}

	@Override
	protected Predicate<BigInteger> isLessThan(BigInteger max) {
		return x -> x.compareTo(max) < 0;
	}

	@Override
	protected Predicate<BigInteger> isLessThanOrEquals(BigInteger max) {
		return x -> x.compareTo(max) <= 0;
	}

	@Override
	public BigIntegerConstraint<T> cast() {
		return this;
	}
}
