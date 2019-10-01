/*
 * Copyright (C) 2018-2019 Toshiaki Maki <makingx@gmail.com>
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
