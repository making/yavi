package am.ik.yavi.constraint;

import java.util.Map;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.Nullable.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintHolder;

public class MapConstraint<T, K, V>
		extends ContainerConstraintBase<T, Map<K, V>, MapConstraint<T, K, V>> {

	@Override
	protected ToIntFunction<Map<K, V>> size() {
		return Map::size;
	}

	@Override
	public MapConstraint<T, K, V> cast() {
		return this;
	}

	public MapConstraint<T, K, V> containsValue(V v) {
		this.holders()
				.add(new ConstraintHolder<>(x -> x.containsValue(v), "map.containsValue",
						"\"{0}\" must contain value {1}", () -> new Object[] { v },
						NULL_IS_VALID));
		return this;
	}

	public MapConstraint<T, K, V> containsKey(K k) {
		this.holders()
				.add(new ConstraintHolder<>(x -> x.containsKey(k), "map.containsValue",
						"\"{0}\" must contain key {1}", () -> new Object[] { k },
						NULL_IS_VALID));
		return this;
	}
}
