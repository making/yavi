package am.ik.yavi.constraint.base;

import java.util.ArrayList;
import java.util.List;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintHolder;

public abstract class ConstraintBase<T, V, C extends Constraint<T, V, C>>
		implements Constraint<T, V, C> {
	private final List<ConstraintHolder<V>> holders = new ArrayList<>();

	@Override
	public abstract C cast();

	@Override
	public List<ConstraintHolder<V>> holders() {
		return this.holders;
	}
}
