/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.ViolatedValue;
import am.ik.yavi.core.ViolationMessage;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.jspecify.annotations.Nullable;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.COLLECTION_CONTAINS;
import static am.ik.yavi.core.ViolationMessage.Default.COLLECTION_CONTAINS_ALL;
import static am.ik.yavi.core.ViolationMessage.Default.COLLECTION_UNIQUE;
import static am.ik.yavi.core.ViolationMessage.Default.COLLECTION_UNIQUE_BY;

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
		return this.unique(Function.identity(), COLLECTION_UNIQUE);
	}

	/**
	 * Requires the elements of the collection to be unique by the given key, such as the
	 * id of an object: no two elements have keys that are equal.
	 *
	 * <p>
	 * A violation is reported with the message key {@code collection.uniqueBy} and the
	 * default message {@code "{0}" must be unique. {1} is/are duplicated.}, where
	 * {@code {1}} is the set of duplicated keys.
	 * </p>
	 * @param key the function that returns the key of an element; the key may be
	 * {@code null}
	 * @return this constraint
	 * @since 0.18.0
	 */
	public CollectionConstraint<T, L, E> unique(Function<? super E, ?> key) {
		return this.unique(Objects.requireNonNull(key, "'key' must not be null"), COLLECTION_UNIQUE_BY);
	}

	private CollectionConstraint<T, L, E> unique(Function<? super E, ?> key, ViolationMessage message) {
		this.predicates().add(ConstraintPredicate.withViolatedValue(collection -> {
			final Set<@Nullable Object> duplicates = new LinkedHashSet<>();
			final Set<@Nullable Object> keys = new HashSet<>(collection.size());
			for (E element : collection) {
				final Object k = key.apply(element);
				if (!keys.add(k)) {
					duplicates.add(k);
				}
			}
			if (duplicates.isEmpty()) {
				return Optional.empty();
			}
			else {
				return Optional.of(new ViolatedValue(duplicates));
			}
		}, message, () -> new Object[] {}, VALID));
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
