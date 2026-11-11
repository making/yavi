/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Objects;
import java.util.function.Predicate;

import am.ik.yavi.jsr305.Nullable;

/**
 * A reusable constraint that combines a predicate with its own violation message and
 * message arguments.
 *
 * <p>
 * Pass an instance to {@link Constraint#predicate(CustomConstraint)} or
 * {@link am.ik.yavi.builder.ValidatorBuilder#constraintOnTarget(CustomConstraint, String)}.
 * </p>
 *
 * <pre>{@code
 * public enum IsbnConstraint implements CustomConstraint<String> {
 *
 *     SINGLETON;
 *
 *     &#64;Override
 *     public boolean test(String value) {
 *         return isISBN13(value);
 *     }
 *
 *     &#64;Override
 *     public String messageKey() {
 *         return "custom.isbn13";
 *     }
 *
 *     &#64;Override
 *     public String defaultMessageFormat() {
 *         return "\"{0}\" must be ISBN13 format";
 *     }
 *
 * }
 * }</pre>
 *
 * @param <V> the type of the value the constraint applies to
 * @since 0.0.1
 */
public interface CustomConstraint<V> extends Predicate<V>, ViolationMessage, ViolatedArguments<V> {

	/**
	 * An empty argument array.
	 */
	Objects[] EMPTY_ARRAY = new Objects[0];

	/**
	 * Returns the arguments used to build the violation message.
	 *
	 * <p>
	 * The default implementation returns {@link #EMPTY_ARRAY}. Override it to supply
	 * arguments for the message format.
	 * </p>
	 * @param violatedValue the value that violated the constraint
	 * @return the arguments for the message format; never {@code null}
	 */
	@Override
	default Object[] arguments(@Nullable V violatedValue) {
		return EMPTY_ARRAY;
	}

}
