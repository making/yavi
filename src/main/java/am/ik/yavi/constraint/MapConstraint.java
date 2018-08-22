/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.constraint;

import java.util.Map;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

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
		this.predicates()
				.add(new ConstraintPredicate<>(x -> x.containsValue(v),
						"map.containsValue", "\"{0}\" must contain value {1}",
						() -> new Object[] { v }, NULL_IS_VALID));
		return this;
	}

	public MapConstraint<T, K, V> containsKey(K k) {
		this.predicates()
				.add(new ConstraintPredicate<>(x -> x.containsKey(k), "map.containsValue",
						"\"{0}\" must contain key {1}", () -> new Object[] { k },
						NULL_IS_VALID));
		return this;
	}
}
