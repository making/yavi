/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.function.BiPredicate;

/**
 * Decides at validation time whether a conditional validator is applied.
 *
 * <p>
 * A condition receives the target object and the {@link ConstraintContext} supplied to
 * the validation call and returns {@code true} when the associated validator should be
 * applied. Conditions are registered with
 * {@link am.ik.yavi.builder.ValidatorBuilder#constraintOnCondition(ConstraintCondition, Validator)}.
 * The static factory methods create conditions that inspect attributes of the context.
 *
 * @param <T> the type of the target object
 * @since 0.0.18
 * @see ConstraintGroup
 * @see ConstraintContext
 */
@FunctionalInterface
public interface ConstraintCondition<T> extends BiPredicate<T, ConstraintContext> {

	/**
	 * Returns a condition that is satisfied when the context has an attribute with the
	 * given key and a non-{@code null} value.
	 * @param key the attribute key
	 * @param <T> the type of the target object
	 * @return a condition based on the presence of the attribute
	 * @since 0.11.0
	 */
	static <T> ConstraintCondition<T> hasAttribute(String key) {
		return (t, context) -> context.attribute(key).exists();
	}

	/**
	 * Returns a condition that is satisfied when the context has an attribute with the
	 * given key whose value is equal to the given value.
	 * @param key the attribute key
	 * @param value the expected attribute value
	 * @param <T> the type of the target object
	 * @return a condition based on the value of the attribute
	 * @since 0.11.0
	 */
	static <T> ConstraintCondition<T> hasAttributeWithValue(String key, Object value) {
		return (t, context) -> context.attribute(key).isEqualTo(value);
	}

}
