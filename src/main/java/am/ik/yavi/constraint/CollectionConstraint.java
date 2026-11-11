/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.ViolatedValue;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.COLLECTION_CONTAINS;
import static am.ik.yavi.core.ViolationMessage.Default.COLLECTION_CONTAINS_ALL;
import static am.ik.yavi.core.ViolationMessage.Default.COLLECTION_UNIQUE;

/**
 * Constraints on a collection value.
 *
 * <p>
 * Besides the content checks of this class, the size constraints of
 * {@link ContainerConstraintBase} are available. A {@code null} value is considered valid
 * and the checks are skipped, except for {@link ContainerConstraintBase#notEmpty()}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <L> the collection type the constraints apply to
 * @param <E> the element type of the collection
 * @since 0.0.1
 */
public class CollectionConstraint<T, L extends Collection<E>, E>
		extends ContainerConstraintBase<T, L, CollectionConstraint<T, L, E>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public CollectionConstraint() {
	}

	@Override
	public CollectionConstraint<T, L, E> cast() {
		return this;
	}

	/**
	 * Requires the collection to contain the given element.
	 *
	 * <p>
	 * A violation is reported with the message key {@code collection.contains} and the
	 * default message {@code "{0}" must contain {1}}, where {@code {1}} is the element.
	 * </p>
	 * @param s the element the collection has to contain
	 * @return this constraint
	 */
	public CollectionConstraint<T, L, E> contains(E s) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> x.contains(s), COLLECTION_CONTAINS, () -> new Object[] { s }, VALID));
		return this;
	}

	/**
	 * Requires the elements of the collection to be unique.
	 *
	 * <p>
	 * A violation is reported with the message key {@code collection.unique} and the
	 * default message {@code "{0}" must be unique. {1} is/are duplicated.}, where
	 * {@code {1}} is the set of duplicated elements.
	 * </p>
	 * @return this constraint
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

	/**
	 * Requires the collection to contain all of the given elements.
	 *
	 * <p>
	 * A violation is reported with the message key {@code collection.containsAll} and the
	 * default message {@code "{0}" must be have of all of the following values: {1},
	 * missing values: {2}}, where {@code {1}} is the given elements and {@code {2}} is
	 * the set of missing elements.
	 * </p>
	 * @param values the elements the collection has to contain
	 * @return this constraint
	 * @since 0.14.3
	 */
	public CollectionConstraint<T, L, E> containsAll(Collection<? extends E> values) {
		this.predicates().add(ConstraintPredicate.withViolatedValue(collection -> {
			final Set<E> missingValues = new HashSet<>(values);

			if (collection instanceof Set) {
				missingValues.removeIf(collection::contains);
			}
			else {
				missingValues.removeAll(new HashSet<>(collection));
			}

			return missingValues.isEmpty() ? Optional.empty() : Optional.of(new ViolatedValue(missingValues));
		}, COLLECTION_CONTAINS_ALL, () -> new Object[] { values.toString() }, VALID));

		return this;
	}

	@Override
	protected ToIntFunction<L> size() {
		return Collection::size;
	}

}
