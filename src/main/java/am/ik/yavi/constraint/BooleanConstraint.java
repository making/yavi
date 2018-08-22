package am.ik.yavi.constraint;

import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ConstraintBase;
import am.ik.yavi.core.ConstraintHolder;

public class BooleanConstraint<T>
		extends ConstraintBase<T, Boolean, BooleanConstraint<T>> {

	public BooleanConstraint<T> isTrue() {
		this.holders().add(new ConstraintHolder<>(x -> x, "boolean.isTrue",
				"\"{0}\" must be true", () -> new Object[] {}, NULL_IS_VALID));
		return this;
	}

	public BooleanConstraint<T> isFalse() {
		this.holders().add(new ConstraintHolder<>(x -> !x, "boolean.isFalse",
				"\"{0}\" must be false", () -> new Object[] {}, NULL_IS_VALID));
		return this;
	}

	@Override
	public BooleanConstraint<T> cast() {
		return this;
	}
}
