package am.ik.yavi.core;

import java.util.List;
import java.util.function.Function;

public class ConstraintHolders<T, V> {
	private final Function<T, V> toValue;
	private final String name;
	private final List<ConstraintHolder<V>> holders;

	public ConstraintHolders(Function<T, V> toValue, String name,
			List<ConstraintHolder<V>> holders) {
		this.toValue = toValue;
		this.name = name;
		this.holders = holders;
	}

	public final Function<T, V> toValue() {
		return this.toValue;
	}

	public final String name() {
		return this.name;
	}

	public final List<ConstraintHolder<V>> holders() {
		return this.holders;
	}
}
