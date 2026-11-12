/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

/**
 * The constraint predicates of one property of a target object.
 *
 * <p>
 * It holds the function that extracts the property value, the property name used in
 * violation names and the predicates to apply to that value. {@link Validator} iterates
 * these when validating a target. Instances are created by
 * {@link am.ik.yavi.builder.ValidatorBuilder}.
 *
 * @param <T> the type of the target object
 * @param <V> the type of the property value
 * @since 0.0.1
 * @see ConstraintPredicate
 */
public class ConstraintPredicates<T, V> {

	private final String name;

	private final List<PathSegment> path;

	private final Deque<ConstraintPredicate<V>> predicates;

	private final Function<T, ? extends @Nullable V> toValue;

	/**
	 * Creates the constraint predicates of a property.
	 * @param toValue function that extracts the property value from the target
	 * @param name the property name used in violation names
	 * @param predicates the predicates to apply to the property value
	 */
	public ConstraintPredicates(Function<T, ? extends @Nullable V> toValue, String name,
			Deque<ConstraintPredicate<V>> predicates) {
		this(toValue, name, PathSegment.of(name), predicates);
	}

	/**
	 * Creates the constraint predicates of a property that is reached through the given
	 * path.
	 * @param toValue function that extracts the property value from the target
	 * @param name the flat property name used in violation names
	 * @param path the structured path of the property, relative to the target
	 * @param predicates the predicates to apply to the property value
	 * @since 0.18.0
	 */
	public ConstraintPredicates(Function<T, ? extends @Nullable V> toValue, String name, List<PathSegment> path,
			Deque<ConstraintPredicate<V>> predicates) {
		this.toValue = toValue;
		this.name = name;
		this.path = path;
		this.predicates = predicates;
	}

	/**
	 * Returns the structured path of the property, relative to the target.
	 * @return the path
	 * @since 0.18.0
	 */
	public final List<PathSegment> path() {
		return this.path;
	}

	/**
	 * Returns the property name used in violation names.
	 * @return the property name
	 */
	public final String name() {
		return this.name;
	}

	/**
	 * Returns the predicates to apply to the property value. The returned deque is the
	 * one passed to the constructor, not a copy.
	 * @return the predicates
	 */
	public final Deque<ConstraintPredicate<V>> predicates() {
		return this.predicates;
	}

	/**
	 * Returns the function that extracts the property value from the target.
	 * @return the property value extractor
	 */
	public final Function<T, ? extends @Nullable V> toValue() {
		return this.toValue;
	}

}
