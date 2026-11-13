/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import am.ik.yavi.core.ValueValidator;

/**
 * Builds a {@link ValueValidator} that validates a single value and produces a value from
 * it.
 *
 * <p>
 * Implementations exist for the supported value types, for example
 * {@link StringValidatorBuilder} and {@link IntegerValidatorBuilder}. They are used by
 * {@link YaviArguments} to create validators for constructor or method arguments. The
 * built validator reports constraint violations and produces no value when validation
 * fails.
 *
 * @param <A> the type of the value to validate
 * @param <R> the type of the value produced when validation succeeds
 * @since 0.14.0
 * @see am.ik.yavi.core.ValueValidator
 */
public interface ValueValidatorBuilder<A, R> {

	/**
	 * Builds the value validator. The produced value is the validated value itself or the
	 * value defined by the implementation.
	 * @return the value validator
	 */
	ValueValidator<? super A, ? extends R> build();

	/**
	 * Builds the value validator that maps the validated value with the given function.
	 * The function is not applied when validation fails.
	 * @param <T> the type of the mapped value
	 * @param mapper function that maps the validated value
	 * @return the value validator
	 */
	<T extends @Nullable Object> ValueValidator<? super A, ? extends T> build(Function<? super R, ? extends T> mapper);

}
