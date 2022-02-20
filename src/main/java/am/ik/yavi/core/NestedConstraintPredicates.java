/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import java.util.Deque;
import java.util.function.Function;

import am.ik.yavi.jsr305.Nullable;

public class NestedConstraintPredicates<T, V, N> extends ConstraintPredicates<T, V> {
	private final Function<T, N> nested;

	public NestedConstraintPredicates(Function<T, V> toValue, String name,
			Deque<ConstraintPredicate<V>> constraintPredicates, Function<T, N> nested) {
		super(toValue, name, constraintPredicates);
		this.nested = nested;
	}

	@Nullable
	public N nestedValue(T target) {
		return nested.apply(target);
	}
}
