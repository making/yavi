/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Collection;
import java.util.function.Function;

import am.ik.yavi.jsr305.Nullable;

/**
 * A {@link CollectionValidator} that also holds the function that extracts the object
 * containing the collection.
 *
 * <p>
 * This is used when a validator containing collection validators is nested in another
 * validator: the parent validator needs both the nested object and the collection inside
 * it. Instances are created by {@link am.ik.yavi.builder.ValidatorBuilder} when a nested
 * validator is merged into the parent validator.
 *
 * @param <T> the type of the target object
 * @param <C> the type of the collection
 * @param <E> the type of the collection elements
 * @param <N> the type of the object containing the collection
 * @since 0.4.1
 */
public class NestedCollectionValidator<T, C extends Collection<E>, E, N> extends CollectionValidator<T, C, E> {

	private final Function<T, N> nested;

	/**
	 * Creates a nested collection validator.
	 * @param toCollection function that extracts the collection from the target
	 * @param name the property name used in violation names
	 * @param validator the validator applied to each element
	 * @param nested function that extracts the object containing the collection from the
	 * target
	 */
	public NestedCollectionValidator(Function<T, C> toCollection, String name, Validator<E> validator,
			Function<T, N> nested) {
		super(toCollection, name, validator);
		this.nested = nested;
	}

	/**
	 * Returns the object containing the collection, extracted from the target.
	 * @param target the target object
	 * @return the nested object, or {@code null} if it does not exist
	 */
	@Nullable
	public N nestedCollection(T target) {
		return nested.apply(target);
	}

}
