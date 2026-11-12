/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.Map;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.constraint.MapConstraint;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import org.jspecify.annotations.Nullable;

/**
 * Builds a validator of a map with string keys that validates every value with a value
 * validator and then the map of the validated values with constraints.
 *
 * <p>
 * The built validator is {@link ValueValidator#liftMap()} of the value validator followed
 * by the constraints: the violations of a value are located under its key and those of
 * all values are accumulated. The constraints are checked only when every value is valid,
 * on the produced values, and their violations have an empty name, so they are reported
 * on the map itself and take the name of the property the validator is applied to. A
 * {@code null} map is validated as an empty map.
 *
 * <p>
 * Example: <pre>{@code
 * ValueValidator<Map<String, BigDecimal>, Map<String, BigDecimal>> validator = MapValidatorBuilder
 *     .of(Yavi._bigDecimal(c -> c.notNull().positive()), c -> c.notEmpty())
 *     .build();
 * }</pre>
 *
 * @param <V> the type of the values to validate
 * @param <T> the type of the validated values
 * @since 0.18.0
 */
public final class MapValidatorBuilder<V, T> implements ValueValidatorBuilder<Map<String, V>, Map<String, T>> {

	private final ValueValidator<V, T> valueValidator;

	private final ContainerConstraints<Map<String, T>> constraints;

	/**
	 * Returns a builder that validates every value with the given validator and the map
	 * of the validated values with the given constraints.
	 * @param valueValidator value validator for each value
	 * @param constraints the function that configures the constraints of the map
	 * @param <V> the type of the values to validate
	 * @param <T> the type of the validated values
	 * @return a new builder
	 */
	public static <V, T> MapValidatorBuilder<V, T> of(ValueValidator<V, T> valueValidator,
			Function<MapConstraint<Arguments1<Map<String, T>>, String, T>, MapConstraint<Arguments1<Map<String, T>>, String, T>> constraints) {
		return new MapValidatorBuilder<>(valueValidator, ContainerConstraints.ofMap(constraints));
	}

	private MapValidatorBuilder(ValueValidator<V, T> valueValidator, ContainerConstraints<Map<String, T>> constraints) {
		this.valueValidator = valueValidator;
		this.constraints = constraints;
	}

	/**
	 * Builds a validator that maps the validated map with the given function.
	 *
	 * <p>
	 * The function is applied only when validation succeeds.
	 * @param <T2> the type of the value produced after mapping
	 * @param mapper the function applied to the validated map
	 * @return a new validator
	 */
	@Override
	public <T2> ValueValidator<Map<String, V>, T2> build(
			Function<@Nullable Map<String, T>, ? extends @Nullable T2> mapper) {
		return this.build().map(mapper);
	}

	/**
	 * Builds a validator whose result is the map of the validated values.
	 * @return a new validator
	 */
	@Override
	public ValueValidator<Map<String, V>, Map<String, T>> build() {
		return this.constraints.constrain(this.valueValidator.liftMap())
			.describedAs(
					new ValueDescription.Entries(this.valueValidator.description(), this.constraints.descriptions()));
	}

}
