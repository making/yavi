/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import org.jspecify.annotations.Nullable;

/**
 * One argument added to an arguments validator builder: its name, if any, and the
 * validator applied to it.
 *
 * <p>
 * The generated {@code ArgumentsNValidatorBuilder} classes keep one definition per
 * argument. Everything else that is known about an argument (its type, its constraints,
 * whether it is optional) is the {@linkplain ValueValidator#description() description} of
 * its validator, from which the public {@link ValueDescription.Property} of the argument
 * is derived when the validator is created.
 *
 * @param name the property name of the argument; {@code null} if the argument was added
 * only with a value validator
 * @param validator the validator applied to the argument
 * @param <A> the type of the argument
 * @param <R> the type of the validated value of the argument
 */
record ArgumentDefinition<A, R> (@Nullable String name, ValueValidator<A, R> validator) {

	/**
	 * Returns the definition of an argument added only with a value validator. If the
	 * validator does not describe itself, it is described as a value of the given type.
	 */
	static <A, R> ArgumentDefinition<A, R> value(Class<?> type, ValueValidator<A, R> validator) {
		final boolean undescribed = validator.description() instanceof ValueDescription.Unknown && type != Object.class;
		return new ArgumentDefinition<>(null,
				undescribed ? validator.describedAs(new ValueDescription.Value("", type, List.of())) : validator);
	}

	static <A, R> ArgumentDefinition<A, R> value(String name, ValueValidator<A, R> validator) {
		return new ArgumentDefinition<>(name, validator);
	}

	static <A, R> ArgumentDefinition<A, R> object(String name, ValueValidator<A, R> validator) {
		return new ArgumentDefinition<>(name, nested(name, validator, validator.description()));
	}

	static <E, R> ArgumentDefinition<List<E>, List<R>> list(String name, ValueValidator<E, R> elementValidator) {
		final ValueValidator<Iterable<E>, List<R>> lifted = elementValidator.liftList();
		return new ArgumentDefinition<>(name, nested(name, lifted::validate, lifted.description()));
	}

	static <V, R> ArgumentDefinition<Map<String, V>, Map<String, R>> map(String name,
			ValueValidator<V, R> valueValidator) {
		final ValueValidator<Map<String, V>, Map<String, R>> lifted = valueValidator.liftMap();
		return new ArgumentDefinition<>(name, nested(name, lifted, lifted.description()));
	}

	ArgumentDefinition<A, Optional<R>> asOptional() {
		return new ArgumentDefinition<>(this.name, this.validator.optional());
	}

	ArgumentDefinition<A, R> asNullable() {
		return new ArgumentDefinition<>(this.name, this.validator.nullable());
	}

	ArgumentDefinition<A, R> withDefault(R defaultValue) {
		return new ArgumentDefinition<>(this.name, this.validator.defaultValue(defaultValue));
	}

	/**
	 * Returns the description of an object whose properties are the given arguments in
	 * order. An argument without a name is named as
	 * {@link ValueDescription.Properties#ofArguments} states.
	 */
	static ValueDescription describe(ArgumentDefinition<?, ?>... definitions) {
		final List<ValueDescription.Property> inferred = ValueDescription.Properties
			.ofArguments(Arrays.stream(definitions)
				.map(definition -> definition.validator().description())
				.toArray(ValueDescription[]::new))
			.properties();
		final List<ValueDescription.Property> properties = new ArrayList<>(definitions.length);
		for (int i = 0; i < definitions.length; i++) {
			final String name = definitions[i].name();
			properties
				.add(name != null ? new ValueDescription.Property(name, inferred.get(i).value()) : inferred.get(i));
		}
		return new ValueDescription.Properties(properties);
	}

	/**
	 * Returns a validator that rejects {@code null} with {@code object.notNull} under the
	 * given name and otherwise locates the violations of the given validator under that
	 * name. The given description is the one of a value other than {@code null}.
	 */
	private static <A, R> ValueValidator<A, R> nested(String name, ValueValidator<A, R> validator,
			ValueDescription description) {
		return ObjectValidatorBuilder.<A>of(name, ObjectConstraint::notNull)
			.build()
			.andThen(validator.prefixed(name))
			.describedAs(new ValueDescription.NotNull(description));
	}

}
