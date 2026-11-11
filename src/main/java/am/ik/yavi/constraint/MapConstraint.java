/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.Map;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.MAP_CONTAINS_KEY;
import static am.ik.yavi.core.ViolationMessage.Default.MAP_CONTAINS_VALUE;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

public class MapConstraint<T, K, V> extends ContainerConstraintBase<T, Map<K, V>, MapConstraint<T, K, V>> {

	@Override
	public MapConstraint<T, K, V> cast() {
		return this;
	}

	public MapConstraint<T, K, V> containsKey(K k) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> x.containsKey(k), MAP_CONTAINS_KEY, () -> new Object[] { k }, VALID));
		return this;
	}

	public MapConstraint<T, K, V> containsValue(V v) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> x.containsValue(v), MAP_CONTAINS_VALUE, () -> new Object[] { v }, VALID));
		return this;
	}

	@Override
	protected ToIntFunction<Map<K, V>> size() {
		return Map::size;
	}

}
