package am.ik.yavi.constraint;

import am.ik.yavi.constraint.base.ConstraintBase;

public class ObjectConstraint<T, E> extends ConstraintBase<T, E, ObjectConstraint<T, E>> {

	@Override
	public ObjectConstraint<T, E> cast() {
		return this;
	}
}
