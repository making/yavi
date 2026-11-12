/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.List;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.constraint.CollectionConstraint;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import org.jspecify.annotations.Nullable;

/**
 * Builds a validator of a list that validates every element with a value validator and
 * then the list of the validated elements with constraints.
 *
 * <p>
 * The built validator is {@link ValueValidator#liftList()} of the element validator
 * followed by the constraints: the violations of an element are located under its index
 * ({@code [0]}, {@code [0].name}) and those of all elements are accumulated. The
 * constraints are checked only when every element is valid, on the produced elements, and
 * their violations have an empty name, so they are reported on the list itself and take
 * the name of the property the validator is applied to. A {@code null} list is validated
 * as an empty list.
 *
 * <p>
 * Example: <pre>{@code
 * ValueValidator<Iterable<String>, List<String>> validator = ListValidatorBuilder
 *     .of(Yavi._string(c -> c.trim().notBlank()), c -> c.notEmpty().lessThanOrEqual(10).unique())
 *     .build();
 * }</pre>
 *
 * @param <E> the type of the elements to validate
 * @param <T> the type of the validated elements
 * @since 0.18.0
 */
public final class ListValidatorBuilder<E, T> implements ValueValidatorBuilder<Iterable<E>, List<T>> {

	private final ValueValidator<E, T> elementValidator;

	private final ContainerConstraints<List<T>> constraints;

	/**
	 * Returns a builder that validates every element with the given validator and the
	 * list of the validated elements with the given constraints.
	 * @param elementValidator value validator for each element
	 * @param constraints the function that configures the constraints of the list
	 * @param <E> the type of the elements to validate
	 * @param <T> the type of the validated elements
	 * @return a new builder
	 */
	public static <E, T> ListValidatorBuilder<E, T> of(ValueValidator<E, T> elementValidator,
			Function<CollectionConstraint<Arguments1<List<T>>, List<T>, T>, CollectionConstraint<Arguments1<List<T>>, List<T>, T>> constraints) {
		return new ListValidatorBuilder<>(elementValidator, ContainerConstraints.ofList(constraints));
	}

	private ListValidatorBuilder(ValueValidator<E, T> elementValidator, ContainerConstraints<List<T>> constraints) {
		this.elementValidator = elementValidator;
		this.constraints = constraints;
	}

	/**
	 * Builds a validator that maps the validated list with the given function.
	 *
	 * <p>
	 * The function is applied only when validation succeeds.
	 * @param <T2> the type of the value produced after mapping
	 * @param mapper the function applied to the validated list
	 * @return a new validator
	 */
	@Override
	public <T2> ValueValidator<Iterable<E>, T2> build(Function<@Nullable List<T>, ? extends @Nullable T2> mapper) {
		return this.build().map(mapper);
	}

	/**
	 * Builds a validator whose result is the list of the validated elements.
	 * @return a new validator
	 */
	@Override
	public ValueValidator<Iterable<E>, List<T>> build() {
		return this.constraints.constrain(this.elementValidator.liftList())
			.describedAs(new ValueDescription.Elements(this.elementValidator.description(),
					this.constraints.descriptions()));
	}

}
