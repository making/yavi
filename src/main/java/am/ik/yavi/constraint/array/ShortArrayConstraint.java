/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.constraint.array;

import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.ARRAY_CONTAINS;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

public class ShortArrayConstraint<T> extends ContainerConstraintBase<T, short[], ShortArrayConstraint<T>> {

	@Override
	public ShortArrayConstraint<T> cast() {
		return this;
	}

	public ShortArrayConstraint<T> contains(short v) {
		this.predicates().add(ConstraintPredicate.of(x -> {
			for (short e : x) {
				if (e == v) {
					return true;
				}
			}
			return false;
		}, ARRAY_CONTAINS, () -> new Object[] { v }, VALID));
		return this;
	}

	@Override
	protected ToIntFunction<short[]> size() {
		return x -> x.length;
	}

}
