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
import java.util.function.Supplier;

import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.input.MapInputValidator;
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
 * @param untyped the validator applied to the argument when it is read from an untyped
 * input; it fails with {@code UnsupportedOperationException} if the input type of the
 * argument is unknown
 * @param <A> the type of the argument
 * @param <R> the type of the validated value of the argument
 */
record ArgumentDefinition<A, R> (@Nullable String name, ValueValidator<A, R> validator,
		Supplier<ValueValidator<Object, R>> untyped) {

	/**
	 * Returns the definition of an argument added only with a value validator. If the
	 * validator does not describe itself, it is described as a value of the given type.
	 * The given type is the input type of the validator unless it is {@code Object} or
	 * {@code Enum}, which stand for a type that the builder method does not determine.
	 */
	@SuppressWarnings("unchecked")
	static <A, R> ArgumentDefinition<A, R> value(Class<?> type, ValueValidator<A, R> validator) {
		final boolean undescribed = validator.description() instanceof ValueDescription.Unknown && type != Object.class;
		final ValueValidator<A, R> described = undescribed
				? validator.describedAs(new ValueDescription.Value("", type, List.of())) : validator;
		return new ArgumentDefinition<>(null, described, type == Object.class || type == Enum.class ? validator::untyped
				: () -> untyped(describedName(described.description()), (Class<A>) type, described));
	}

	/**
	 * Returns the definition of a named argument whose input type is known only to the
	 * given validator.
	 */
	static <A, R> ArgumentDefinition<A, R> value(String name, ValueValidator<A, R> validator) {
		return new ArgumentDefinition<>(name, validator, validator::untyped);
	}

	static <A, R> ArgumentDefinition<A, R> value(String name, Class<A> type, ValueValidator<A, R> validator) {
		return new ArgumentDefinition<>(name, validator, () -> untyped(name, type, validator));
	}

	static <A, R> ArgumentDefinition<A, R> object(String name, ValueValidator<A, R> validator) {
		return new ArgumentDefinition<>(name, nested(name, validator, validator.description()),
				() -> nested(name, validator.untyped(), validator.description()));
	}

	@SuppressWarnings("unchecked")
	static <E, R> ArgumentDefinition<List<E>, List<R>> list(String name, ValueValidator<E, R> elementValidator) {
		final ValueDescription description = new ValueDescription.Elements(elementValidator.description());
		return new ArgumentDefinition<>(name, nested(name, elements(elementValidator)::validate, description),
				() -> nested(name, elements(elementValidator.untyped())
					.untyped((Class<Iterable<Object>>) (Class<?>) Iterable.class), description));
	}

	static <V, R> ArgumentDefinition<Map<String, V>, Map<String, R>> map(String name,
			ValueValidator<V, R> valueValidator) {
		final ValueValidator<Map<String, V>, Map<String, R>> lifted = valueValidator.liftMap();
		return new ArgumentDefinition<>(name, nested(name, lifted, lifted.description()),
				() -> nested(name, lifted.untyped(), lifted.description()));
	}

	ArgumentDefinition<A, Optional<R>> asOptional() {
		return new ArgumentDefinition<>(this.name, this.validator.optional(), () -> this.untyped.get().optional());
	}

	ArgumentDefinition<A, R> asNullable() {
		return new ArgumentDefinition<>(this.name, this.validator.nullable(), () -> this.untyped.get().nullable());
	}

	ArgumentDefinition<A, R> withDefault(R defaultValue) {
		return new ArgumentDefinition<>(this.name, this.validator.defaultValue(defaultValue),
				() -> this.untyped.get().defaultValue(defaultValue));
	}

	/**
	 * Returns the property that reads this argument from a map. The key is the name of
	 * the argument, or else the name under which its validator reports violations.
	 * @param position the position of the argument starting at 1
	 * @throws IllegalStateException if the argument has no name or its input type is
	 * unknown
	 */
	MapInputValidator.Property<R> property(int position) {
		final String name = this.name != null ? this.name : describedName(this.validator.description());
		if (name.isEmpty()) {
			throw new IllegalStateException(
					"The argument at position " + position + " has no name. Add it with a name to read it from a map.");
		}
		try {
			return new MapInputValidator.Property<>(name, this.untyped.get());
		}
		catch (UnsupportedOperationException e) {
			throw new IllegalStateException("The input type of the argument '" + name
					+ "' is unknown. Add it with a type (for example _enum(name, type) or _object(name, type)),"
					+ " or with a validator that knows its input type (see ValueValidator#untyped).", e);
		}
	}

	/**
	 * Returns a validator that validates every element with the given validator and
	 * locates its violations under the index of the element: {@code street} is reported
	 * as {@code [0].street} and an empty name as {@code [0]}.
	 */
	private static <E, R> ValueValidator<Iterable<E>, List<R>> elements(ValueValidator<E, R> elementValidator) {
		return (values, locale, constraintContext) -> Validated.traverseIndexed(values,
				(value, index) -> elementValidator.validate(value, locale, constraintContext).prefixed(index));
	}

	/**
	 * Returns a validator of an untyped input that reports an input not representing the
	 * given type under the given name and otherwise validates like the given validator.
	 */
	private static <A, R> ValueValidator<Object, R> untyped(String name, Class<A> type,
			ValueValidator<A, R> validator) {
		return ValueValidator.<A>passThrough()
			.untyped(type)
			.prefixed(name)
			.andThen(validator)
			.describedAs(validator.description());
	}

	/**
	 * Returns the name under which the violations of the described value are reported, or
	 * an empty string if it is unknown.
	 */
	private static String describedName(ValueDescription description) {
		return switch (description) {
			case ValueDescription.Optional optional -> describedName(optional.value());
			case ValueDescription.NotNull notNull -> describedName(notNull.value());
			case ValueDescription.Value value -> value.name();
			default -> "";
		};
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
