package am.ik.yavi.constraint.base;

import java.util.function.Predicate;

import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintHolder;

public abstract class NumberConstraintBase<T, V extends Number, C extends Constraint<T, V, C>>
		extends ConstraintBase<T, V, C> {

	protected abstract Predicate<V> isGreaterThan(V min);

	protected abstract Predicate<V> isGreaterThanOrEquals(V min);

	protected abstract Predicate<V> isLessThan(V max);

	protected abstract Predicate<V> isLessThanOrEquals(V max);

	public C greaterThan(V min) {
		this.holders()
				.add(new ConstraintHolder<>(this.isGreaterThan(min), "number.greaterThan",
						"\"{0}\" must not be greater than {1}",
						() -> new Object[] { min }, NULL_IS_VALID));
		return cast();
	}

	public C greaterThanOrEquals(V min) {
		this.holders().add(new ConstraintHolder<>(this.isGreaterThanOrEquals(min),
				"number.greaterThanOrEquals", "\"{0}\" must not be greater than {1}",
				() -> new Object[] { min }, NULL_IS_VALID));
		return cast();
	}

	public C lessThan(V max) {
		this.holders()
				.add(new ConstraintHolder<>(this.isLessThan(max), "number.lessThan",
						"\"{0}\" must not be less than {1}", () -> new Object[] { max },
						NULL_IS_VALID));
		return cast();
	}

	public C lessThanOrEquals(V max) {
		this.holders()
				.add(new ConstraintHolder<>(this.isLessThanOrEquals(max),
						"number.lessThanOrEquals", "\"{0}\" must not be less than {1}",
						() -> new Object[] { max }, NULL_IS_VALID));
		return cast();
	}
}
