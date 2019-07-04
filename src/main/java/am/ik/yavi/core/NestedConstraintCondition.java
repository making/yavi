package am.ik.yavi.core;

import java.util.function.Function;

public class NestedConstraintCondition<T, N> implements ConstraintCondition<T> {

	private final Function<T, N> nested;
	private final ConstraintCondition<N> constraintCondition;

	public NestedConstraintCondition(Function<T, N> nested,
			ConstraintCondition<N> constraintCondition) {
		this.nested = nested;
		this.constraintCondition = constraintCondition;
	}

	@Override
	public boolean test(T t, ConstraintGroup constraintGroup) {
		final N n = this.nested.apply(t);
		return this.constraintCondition.test(n, constraintGroup);
	}
}
