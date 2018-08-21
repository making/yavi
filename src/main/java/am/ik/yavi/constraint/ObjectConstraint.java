package am.ik.yavi.constraint;

import am.ik.yavi.constraint.base.ConstraintBase;

public class ObjectConstraint<T> extends ConstraintBase<T, Object, ObjectConstraint<T>> {

	@Override
	public ObjectConstraint<T> cast() {
		return this;
	}
}
