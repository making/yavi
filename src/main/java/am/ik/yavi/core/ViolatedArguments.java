/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * Supplies the message arguments of a custom constraint.
 *
 * <p>
 * Implemented by custom constraints that need arguments beyond the property name and the
 * violated value. The array returned by {@link #arguments(Object)} is inserted between
 * these two: the property name is prepended as argument {@code {0}} and the value that
 * violated the constraint is appended as the last argument.
 *
 * @param <T> type of the validated value
 * @since 0.2.7
 */
@FunctionalInterface
public interface ViolatedArguments<T> extends Supplier<Object[]> {

	/**
	 * Returns the arguments that are used to build the violation message.
	 *
	 * <p>
	 * The returned arguments are accessible as {@code {1}}, {@code {2}}, ... in the
	 * message format. {@code {0}} is reserved for the property name and the last index is
	 * reserved for the value that violated the constraint, so an implementation must
	 * include neither.
	 * @param violatedValue the value that violated the constraint; may be {@code null}
	 * @return the arguments, excluding the property name and the violated value
	 */
	Object[] arguments(@Nullable T violatedValue);

	/**
	 * Returns the arguments for a constraint with no violated value.
	 * @return the result of {@link #arguments(Object)} with {@code null}
	 */
	@Override
	default Object[] get() {
		return this.arguments(null);
	}

	/**
	 * Extracts the argument supplier from the given predicate.
	 * @param predicate the constraint predicate
	 * @param <T> type of the validated value
	 * @return the predicate itself when it implements {@link ViolatedArguments},
	 * otherwise a supplier of an empty array
	 */
	@SuppressWarnings("unchecked")
	static <T> Supplier<Object[]> supplyArguments(Predicate<T> predicate) {
		return (predicate instanceof ViolatedArguments) ? (ViolatedArguments<T>) predicate : () -> new Object[] {};
	}

}
