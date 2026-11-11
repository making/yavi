/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.util.Map;
import java.util.function.ToIntFunction;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.MAP_CONTAINS_KEY;
import static am.ik.yavi.core.ViolationMessage.Default.MAP_CONTAINS_VALUE;

import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.ConstraintPredicate;

/**
 * Constraints on a map value.
 *
 * <p>
 * Besides the content checks of this class, the size constraints of
 * {@link ContainerConstraintBase} are available. A {@code null} value is considered valid
 * and the checks are skipped, except for {@link ContainerConstraintBase#notEmpty()}.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <K> the key type of the map
 * @param <V> the value type of the map
 * @since 0.0.1
 */
public class MapConstraint<T, K, V> extends ContainerConstraintBase<T, Map<K, V>, MapConstraint<T, K, V>> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public MapConstraint() {
	}

	@Override
	public MapConstraint<T, K, V> cast() {
		return this;
	}

	/**
	 * Requires the map to contain the given key.
	 *
	 * <p>
	 * A violation is reported with the message key {@code map.containsKey} and the
	 * default message {@code "{0}" must contain key {1}}, where {@code {1}} is the key.
	 * </p>
	 * @param k the key the map has to contain
	 * @return this constraint
	 */
	public MapConstraint<T, K, V> containsKey(K k) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> x.containsKey(k), MAP_CONTAINS_KEY, () -> new Object[] { k }, VALID));
		return this;
	}

	/**
	 * Requires the map to contain the given value.
	 *
	 * <p>
	 * A violation is reported with the message key {@code map.containsValue} and the
	 * default message {@code "{0}" must contain value {1}}, where {@code {1}} is the
	 * value.
	 * </p>
	 * @param v the value the map has to contain
	 * @return this constraint
	 */
	public MapConstraint<T, K, V> containsValue(V v) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> x.containsValue(v), MAP_CONTAINS_VALUE, () -> new Object[] { v }, VALID));
		return this;
	}

	@Override
	protected ToIntFunction<Map<K, V>> size() {
		return Map::size;
	}

}
