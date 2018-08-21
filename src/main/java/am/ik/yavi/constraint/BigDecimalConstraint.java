package am.ik.yavi.constraint;

import java.math.BigDecimal;
import java.util.function.Predicate;

import am.ik.yavi.constraint.base.NumberConstraintBase;

public class BigDecimalConstraint<T>
		extends NumberConstraintBase<T, BigDecimal, BigDecimalConstraint<T>> {
	@Override
	protected Predicate<BigDecimal> isGreaterThan(BigDecimal min) {
		return x -> x.compareTo(min) > 0;
	}

	@Override
	protected Predicate<BigDecimal> isGreaterThanOrEquals(BigDecimal min) {
		return x -> x.compareTo(min) >= 0;
	}

	@Override
	protected Predicate<BigDecimal> isLessThan(BigDecimal max) {
		return x -> x.compareTo(max) < 0;
	}

	@Override
	protected Predicate<BigDecimal> isLessThanOrEquals(BigDecimal max) {
		return x -> x.compareTo(max) <= 0;
	}

	@Override
	public BigDecimalConstraint<T> cast() {
		return this;
	}
}
