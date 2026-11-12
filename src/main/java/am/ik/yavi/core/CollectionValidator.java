/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Collection;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

/**
 * Describes the validation of a collection contained in a target object.
 *
 * <p>
 * It holds the function that extracts the collection from the target, the property name
 * used in violation names and the validator applied to each element. {@link Validator}
 * uses these values to validate the elements and to report their violations with indexed
 * names. Instances are created by {@link am.ik.yavi.builder.ValidatorBuilder} through the
 * {@code forEach*} methods.
 *
 * @param <T> the type of the target object
 * @param <N> the type of the collection
 * @param <E> the type of the collection elements
 * @since 0.0.5
 */
public class CollectionValidator<T, N extends Collection<E>, E> {

	private final String name;

	private final Function<T, @Nullable N> toCollection;

	private final Validator<E> validator;

	/**
	 * Creates a collection validator.
	 * @param toCollection function that extracts the collection from the target
	 * @param name the property name used in violation names
	 * @param validator the validator applied to each element
	 */
	public CollectionValidator(Function<T, @Nullable N> toCollection, String name, Validator<E> validator) {
		this.toCollection = toCollection;
		this.name = name;
		this.validator = validator;
	}

	/**
	 * Returns the property name used in violation names.
	 * @return the property name
	 */
	public String name() {
		return this.name;
	}

	/**
	 * Returns the function that extracts the collection from the target.
	 * @return the collection extractor
	 */
	public Function<T, @Nullable N> toCollection() {
		return this.toCollection;
	}

	/**
	 * Returns the validator applied to each element of the collection.
	 * @return the element validator
	 */
	public Validator<E> validator() {
		return this.validator;
	}

}
