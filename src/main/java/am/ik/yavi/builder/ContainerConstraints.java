/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.DefaultArguments1Validator;
import am.ik.yavi.constraint.CollectionConstraint;
import am.ik.yavi.constraint.MapConstraint;
import am.ik.yavi.core.ConstraintDescription;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.Validator;
import am.ik.yavi.core.ValueValidator;
import org.jspecify.annotations.Nullable;

/**
 * The constraints on a list of validated elements or on a map of validated values.
 *
 * @param validator the validator of the list or map, whose violations have an empty name;
 * {@code null} if there is no constraint
 * @param descriptions the descriptions of the constraints, which
 * {@linkplain ConstraintDescription#rejectsNull() reject null} if they reject an empty
 * list or map
 * @param <C> the type of the list or map
 */
record ContainerConstraints<C> (@Nullable ValueValidator<C, C> validator, List<ConstraintDescription> descriptions) {

	static <C> ContainerConstraints<C> none() {
		return new ContainerConstraints<>(null, List.of());
	}

	static <T> ContainerConstraints<List<T>> ofList(
			Function<CollectionConstraint<Arguments1<List<T>>, List<T>, T>, CollectionConstraint<Arguments1<List<T>>, List<T>, T>> constraints) {
		return of(ValidatorBuilder.<Arguments1<List<T>>>of().constraint(Arguments1::arg1, "", constraints).build(),
				List.of());
	}

	static <V> ContainerConstraints<Map<String, V>> ofMap(
			Function<MapConstraint<Arguments1<Map<String, V>>, String, V>, MapConstraint<Arguments1<Map<String, V>>, String, V>> constraints) {
		return of(
				ValidatorBuilder.<Arguments1<Map<String, V>>>of().constraint(Arguments1::arg1, "", constraints).build(),
				Map.of());
	}

	@SuppressWarnings("unchecked")
	private static <C> ContainerConstraints<C> of(Validator<Arguments1<C>> validator, C empty) {
		final List<ConstraintDescription> descriptions = new ArrayList<>();
		validator.forEachPredicates(predicates -> predicates.predicates()
			.forEach(predicate -> descriptions
				.add(ConstraintDescription.of((ConstraintPredicate<C>) predicate, empty))));
		return new ContainerConstraints<>(new DefaultArguments1Validator<>(validator, Objects::requireNonNull),
				descriptions);
	}

	/**
	 * Returns a validator that checks the constraints on the list or map that the given
	 * validator produces, only when it is valid.
	 */
	<A> ValueValidator<A, C> constrain(ValueValidator<A, C> validator) {
		return this.validator == null ? validator : validator.andThen(this.validator);
	}

}
