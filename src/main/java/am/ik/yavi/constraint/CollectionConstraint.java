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

import java.util.Collection;
import java.util.function.ToIntFunction;

import static am.ik.yavi.constraint.ViolationMessage.Default.COLLECTION_CONTAINS;
import static am.ik.yavi.core.NullValidity.NULL_IS_VALID;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

public class CollectionConstraint<T, L extends Collection<E>, E>
		extends ContainerConstraintBase<T, L, CollectionConstraint<T, L, E>> {

	@Override
	protected ToIntFunction<L> size() {
		return Collection::size;
	}

	@Override
	public CollectionConstraint<T, L, E> cast() {
		return this;
	}

	public CollectionConstraint<T, L, E> contains(E s) {
		this.predicates().add(ConstraintPredicate.of(x -> x.contains(s),
				COLLECTION_CONTAINS, () -> new Object[] { s }, NULL_IS_VALID));
		return this;
	}
}
