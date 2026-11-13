/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import org.jspecify.annotations.Nullable;

/**
 * A {@link ValueValidator} that is known by its type to accept a {@code null} input: the
 * result of {@link ValueValidator#optional()}, {@link ValueValidator#nullable()},
 * {@link ValueValidator#defaultValue(Object)}, {@link ValueValidator#recover(Object)} and
 * {@link ValueValidator#recoverWith(java.util.function.Function)}.
 *
 * <p>
 * The type tells a schema what the type of the validated value cannot: a property added
 * with such a validator already accepts {@code null}, so the builders returned by
 * {@link am.ik.yavi.validator.Yavi#schema()} offer {@code required()} and
 * {@code rejectingNull()} for it and no modifier that would accept {@code null} again.
 *
 * <pre>{@code
 * NullAcceptingValueValidator<Integer, Integer> page = Yavi._integer("page", c -> c.positive()).defaultValue(1);
 *
 * Yavi.schema()._integer(page).rejectingNull() // 1 only for an absent key
 * }</pre>
 *
 * <p>
 * A validator derived from this one, for example with
 * {@link ValueValidator#prefixed(String)}, is a plain {@link ValueValidator}, unless it
 * is only given another name with {@link #named(String)}. The interface is sealed, and
 * therefore not a functional interface: a lambda is never taken for a validator that
 * accepts {@code null}.
 *
 * @param <T> the type of the object to be validated
 * @param <X> the type of the validated and transformed result
 * @since 0.18.0
 */
public sealed interface NullAcceptingValueValidator<T, X extends @Nullable Object>
		extends ValueValidator<T, X>permits DescribedNullAcceptingValueValidator {

	/**
	 * Returns a validator that reports the violations of the validated value under the
	 * given name, as {@link ValueValidator#named(String)} states, and that still accepts
	 * {@code null}.
	 * @param name the name under which the violations of the value are reported
	 * @return a validator of the value under the given name
	 */
	@Override
	NullAcceptingValueValidator<T, X> named(String name);

}
