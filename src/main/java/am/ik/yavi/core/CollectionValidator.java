/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Collection;
import java.util.function.Function;

public class CollectionValidator<T, N extends Collection<E>, E> {

	private final String name;

	private final Function<T, N> toCollection;

	private final Validator<E> validator;

	public CollectionValidator(Function<T, N> toCollection, String name, Validator<E> validator) {
		this.toCollection = toCollection;
		this.name = name;
		this.validator = validator;
	}

	public String name() {
		return this.name;
	}

	public Function<T, N> toCollection() {
		return this.toCollection;
	}

	public Validator<E> validator() {
		return this.validator;
	}

}
