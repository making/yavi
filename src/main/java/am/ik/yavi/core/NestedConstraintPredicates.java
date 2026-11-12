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
 * The constraint predicates of a property that belongs to an object nested in the target.
 *
 * <p>
 * In addition to the property value extractor and predicates of
 * {@link ConstraintPredicates}, it holds the function that extracts the nested object.
 * When the nested object is {@code null}, its constraints are skipped. Instances are
 * created by {@link am.ik.yavi.builder.ValidatorBuilder} when a nested validator is
 * merged into the parent validator.
 *
 * @param <T> the type of the target object
 * @param <V> the type of the property value
 * @param <N> the type of the nested object
 * @since 0.0.1
 * @see ConstraintPredicates
 */
public class NestedConstraintPredicates<T, V, N> extends ConstraintPredicates<T, V> {

	private final Function<T, ? extends @Nullable N> nested;

	private final boolean failFast;

	/**
	 * Creates the constraint predicates of a property of a nested object.
	 * @param toValue function that extracts the property value from the target
	 * @param name the flat property name used in violation names
	 * @param path the structured path of the property, relative to the target
	 * @param constraintPredicates the predicates to apply to the property value
	 * @param nested function that extracts the nested object from the target
	 * @param failFast whether the validator of the nested object is in fail-fast mode
	 */
	public NestedConstraintPredicates(Function<T, ? extends @Nullable V> toValue, String name, List<PathSegment> path,
			Deque<ConstraintPredicate<V>> constraintPredicates, Function<T, ? extends @Nullable N> nested,
			boolean failFast) {
		super(toValue, name, path, constraintPredicates);
		this.nested = nested;
		this.failFast = failFast;
	}

	/**
	 * Returns the nested object extracted from the target.
	 * @param target the target object
	 * @return the nested object, or {@code null} if it does not exist
	 */
	@Nullable public N nestedValue(T target) {
		return nested.apply(target);
	}

	/**
	 * Returns whether the validator of the nested object is in fail-fast mode.
	 * @return whether it is failFast
	 * @since 0.13.0
	 */
	public boolean isFailFast() {
		return this.failFast;
	}

}
