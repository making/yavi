/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToIntFunction;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.ViolatedValue;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.COLLECTION_CONTAINS;
import static am.ik.yavi.core.ViolationMessage.Default.COLLECTION_UNIQUE;
import static am.ik.yavi.core.ViolationMessage.Default.OBJECT_ALL_OF;

public class CollectionConstraint<T, L extends Collection<E>, E>
		extends ContainerConstraintBase<T, L, CollectionConstraint<T, L, E>> {

	@Override
	public CollectionConstraint<T, L, E> cast() {
		return this;
	}

	public CollectionConstraint<T, L, E> contains(E s) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> x.contains(s), COLLECTION_CONTAINS, () -> new Object[] { s }, VALID));
		return this;
	}

	/**
	 * @since 0.8.3
	 */
	public CollectionConstraint<T, L, E> unique() {
		this.predicates().add(ConstraintPredicate.withViolatedValue(collection -> {
			final Set<E> duplicates = new LinkedHashSet<>();
			final Set<E> uniqElements = new HashSet<>(collection.size());
			for (E element : collection) {
				if (uniqElements.contains(element)) {
					duplicates.add(element);
				}
				else {
					uniqElements.add(element);
				}
			}
			if (duplicates.isEmpty()) {
				return Optional.empty();
			}
			else {
				return Optional.of(new ViolatedValue(duplicates));
			}
		}, COLLECTION_UNIQUE, () -> new Object[] {}, VALID));
		return this;
	}

	public CollectionConstraint<T, L, E> allOf(Collection<? extends E> values) {
		this.predicates().add(ConstraintPredicate.withViolatedValue(collection -> {
			final Set<E> missingValues = new HashSet<>(values);

			if (collection instanceof Set)
				missingValues.removeAll(collection);
			else
				missingValues.removeAll(new HashSet<>(collection));

			return missingValues.isEmpty() ? Optional.empty() : Optional.of(new ViolatedValue(missingValues));
		}, OBJECT_ALL_OF, () -> new Object[]{values.toString()}, VALID));

		return this;
	}


	@Override
	protected ToIntFunction<L> size() {
		return Collection::size;
	}

}
