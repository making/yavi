/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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

public class ConstraintPredicates<T, V> {
	private final String name;

	private final Deque<ConstraintPredicate<V>> predicates;

	private final Function<T, V> toValue;

	public ConstraintPredicates(Function<T, V> toValue, String name,
			Deque<ConstraintPredicate<V>> predicates) {
		this.toValue = toValue;
		this.name = name;
		this.predicates = predicates;
	}

	public final String name() {
		return this.name;
	}

	public final Deque<ConstraintPredicate<V>> predicates() {
		return this.predicates;
	}

	public final Function<T, V> toValue() {
		return this.toValue;
	}
}
