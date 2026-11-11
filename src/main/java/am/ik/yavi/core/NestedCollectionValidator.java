/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Collection;
import java.util.function.Function;

import am.ik.yavi.jsr305.Nullable;

public class NestedCollectionValidator<T, C extends Collection<E>, E, N> extends CollectionValidator<T, C, E> {

	private final Function<T, N> nested;

	public NestedCollectionValidator(Function<T, C> toCollection, String name, Validator<E> validator,
			Function<T, N> nested) {
		super(toCollection, name, validator);
		this.nested = nested;
	}

	@Nullable
	public N nestedCollection(T target) {
		return nested.apply(target);
	}

}
