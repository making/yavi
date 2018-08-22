package am.ik.yavi.core;

import java.util.List;
import java.util.function.Function;

public class ConstraintPredicates<T, V> {
	private final Function<T, V> toValue;
	private final String name;
	private final List<ConstraintPredicate<V>> predicates;

	public ConstraintPredicates(Function<T, V> toValue, String name,
			List<ConstraintPredicate<V>> predicates) {
		this.toValue = toValue;
		this.name = name;
		this.predicates = predicates;
	}

	public final Function<T, V> toValue() {
		return this.toValue;
	}

	public final String name() {
		return this.name;
	}

	public final List<ConstraintPredicate<V>> predicates() {
		return this.predicates;
	}
}
