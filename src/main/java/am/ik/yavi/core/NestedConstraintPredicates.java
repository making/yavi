package am.ik.yavi.core;

import java.util.List;
import java.util.function.Function;

public class NestedConstraintPredicates<T, V, N> extends ConstraintPredicates<T, V> {
	private final Function<T, N> nested;

	public NestedConstraintPredicates(Function<T, V> toValue, String name,
			List<ConstraintPredicate<V>> constraintPredicates, Function<T, N> nested) {
		super(toValue, name, constraintPredicates);
		this.nested = nested;
	}

	public N nestedValue(T target) {
		return nested.apply(target);
	}
}
