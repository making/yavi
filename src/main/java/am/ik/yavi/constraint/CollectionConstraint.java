/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.IncludedViolationMessages;

import java.util.Collection;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullAs.VALID;

public class CollectionConstraint<T, L extends Collection<E>, E>
		extends ContainerConstraintBase<T, L, CollectionConstraint<T, L, E>> {

	@Override
	public CollectionConstraint<T, L, E> cast() {
		return this;
	}

	public CollectionConstraint<T, L, E> contains(E s) {
		this.predicates()
				.add(ConstraintPredicate.of(x -> x.contains(s),
						IncludedViolationMessages.get().COLLECTION_CONTAINS(),
						() -> new Object[] { s }, VALID));
		return this;
	}

	@Override
	protected ToIntFunction<L> size() {
		return Collection::size;
	}
}
