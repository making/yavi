package am.ik.yavi.core;

import java.util.List;
import java.util.function.Function;

public class NestedConstraintHolders<T, V, N> extends ConstraintHolders<T, V> {
	private final Function<T, N> nested;

	public NestedConstraintHolders(Function<T, V> toValue, String name,
			List<ConstraintHolder<V>> constraintHolders, Function<T, N> nested) {
		super(toValue, name, constraintHolders);
		this.nested = nested;
	}

	public N nestedValue(T target) {
		return nested.apply(target);
	}
}
