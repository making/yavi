/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolatedValue;
import am.ik.yavi.core.ViolationMessage;
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
 * input with a reader; it fails with {@code UnsupportedOperationException} if the input
 * type of the argument is unknown. The node type is erased: the function is written for
 * any reader
 * @param source what the argument is read from when its validator reads a map
 * @param <A> the type of the argument
 * @param <R> the type of the validated value of the argument
 */
record ArgumentDefinition<A, R> (@Nullable String name, ValueValidator<A, R> validator,
		Function<InputReader<Object>, ValueValidator<Object, R>> untyped, Source source) {

	/**
	 * What an argument is read from when its validator reads a map.
	 */
	enum Source {

		/** The value of the key that is the name of the argument. */
		PROPERTY,

		/** The map itself. */
		OBJECT,

		/** The keys of the map that no other argument reads. */
		REST

	}

	ArgumentDefinition(@Nullable String name, ValueValidator<A, R> validator,
			Function<InputReader<Object>, ValueValidator<Object, R>> untyped) {
		this(name, validator, untyped, Source.PROPERTY);
	}

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
		return new ArgumentDefinition<>(null, described, type == Object.class || type == Enum.class
				? reader -> validator.readingWith(reader)
				: reader -> untyped(reader, describedName(described.description()), (Class<A>) type, described));
	}

	/**
	 * Returns the definition of a named argument whose input type is known only to the
	 * given validator.
	 */
	static <A, R> ArgumentDefinition<A, R> value(String name, ValueValidator<A, R> validator) {
		return new ArgumentDefinition<>(name, validator, reader -> validator.readingWith(reader));
	}

	static <A, R> ArgumentDefinition<A, R> value(String name, Class<A> type, ValueValidator<A, R> validator) {
		return new ArgumentDefinition<>(name, validator, reader -> untyped(reader, name, type, validator));
	}

	static <A, R> ArgumentDefinition<A, R> object(String name, ValueValidator<A, R> validator) {
		return new ArgumentDefinition<>(name, nested(name, validator, validator.description()),
				reader -> nested(name, validator.readingWith(reader), validator.description()));
	}

	static <E, R> ArgumentDefinition<List<E>, List<R>> list(String name, ValueValidator<E, R> elementValidator) {
		final ValueDescription description = new ValueDescription.Elements(elementValidator.description());
		return new ArgumentDefinition<>(name, nested(name, elements(elementValidator)::validate, description),
				reader -> nested(name, elements(reader, elementValidator.readingWith(reader)), description));
	}

	static <V, R> ArgumentDefinition<Map<String, V>, Map<String, R>> map(String name,
			ValueValidator<V, R> valueValidator) {
		final ValueValidator<Map<String, V>, Map<String, R>> lifted = valueValidator.liftMap();
		return new ArgumentDefinition<>(name, nested(name, lifted, lifted.description()),
				reader -> nested(name, lifted.readingWith(reader), lifted.description()));
	}

	/**
	 * Returns the definition of an argument that reads the same map as the other
	 * arguments. A validator that does not know its input type, such as a lambda, reads a
	 * map only.
	 */
	@SuppressWarnings("unchecked")
	static <R> ArgumentDefinition<Map<String, Object>, R> embedded(
			ValueValidator<? super Map<String, Object>, R> validator) {
		final ValueValidator<Map<String, Object>, R> ofMap = (ValueValidator<Map<String, Object>, R>) validator;
		Function<InputReader<Object>, ValueValidator<Object, R>> untyped;
		try {
			ofMap.readingWith(InputReader.ofJdkTypes());
			untyped = reader -> ofMap.readingWith(reader);
		}
		catch (UnsupportedOperationException e) {
			final ValueValidator<Object, R> checked = ofMap.untyped((Class<Map<String, Object>>) (Class<?>) Map.class);
			untyped = reader -> checked.readingWith(reader);
		}
		return new ArgumentDefinition<>(null, ofMap, untyped, Source.OBJECT);
	}

	/**
	 * Returns the definition of an argument that reads the keys of the map that no other
	 * argument reads. The violations of a value are located under its key.
	 */
	static <V, R> ArgumentDefinition<Map<String, V>, Map<String, R>> rest(ValueValidator<V, R> valueValidator) {
		final ValueValidator<Map<String, V>, Map<String, R>> lifted = valueValidator.liftMap();
		return new ArgumentDefinition<>(null, lifted, reader -> lifted.readingWith(reader), Source.REST);
	}

	/**
	 * Returns the definition of an argument that reads the keys of the map that no other
	 * argument reads and takes their values as they are.
	 */
	static ArgumentDefinition<Map<String, Object>, Map<String, Object>> rest() {
		return rest(ValueValidator.passThrough().untyped(Object.class));
	}

	ArgumentDefinition<A, Optional<R>> asOptional() {
		return new ArgumentDefinition<>(this.name, this.validator.optional(),
				reader -> this.untyped.apply(reader).optional(), this.source);
	}

	ArgumentDefinition<A, R> asNullable() {
		return new ArgumentDefinition<>(this.name, this.validator.nullable(),
				reader -> this.untyped.apply(reader).nullable(), this.source);
	}

	ArgumentDefinition<A, R> withDefault(R defaultValue) {
		return new ArgumentDefinition<>(this.name, this.validator.defaultValue(defaultValue),
				reader -> this.untyped.apply(reader).defaultValue(defaultValue), this.source);
	}

	/**
	 * Returns the part that reads this argument from a map. The key of a property is the
	 * name of the argument, or else the name under which its validator reports
	 * violations.
	 * @param position the position of the argument starting at 1
	 * @throws IllegalStateException if the argument is a property that has no name, or if
	 * its input type is unknown
	 */
	MapInputValidator.Part<R> part(int position) {
		final String name = this.name != null ? this.name : describedName(this.validator.description());
		if (this.source == Source.PROPERTY && name.isEmpty()) {
			throw new IllegalStateException("The argument at position " + position
					+ " has no name. Add it with a name to read it from a map, or with _embedded to read the map itself.");
		}
		final ValueValidator<Object, R> reading;
		try {
			reading = new Reading<>(this.untyped.apply(InputReader.ofJdkTypes()), this.untyped);
		}
		catch (UnsupportedOperationException e) {
			throw new IllegalStateException("The input type of the argument "
					+ (this.source == Source.PROPERTY ? "'" + name + "'" : "at position " + position)
					+ " is unknown. Add it with a type (for example _enum(name, type) or _object(name, type)),"
					+ " or with a validator that knows its input type (see ValueValidator#untyped).", e);
		}
		return switch (this.source) {
			case PROPERTY -> new MapInputValidator.Property<>(name, reading);
			case OBJECT -> new MapInputValidator.Embedded<>(reading);
			case REST -> new MapInputValidator.Rest<>(reading);
		};
	}

	/**
	 * The validator of an argument read from a tree of JDK types, which reads the nodes
	 * of another reader as the argument defines it.
	 */
	private record Reading<R> (ValueValidator<Object, R> validator,
			Function<InputReader<Object>, ValueValidator<Object, R>> reading) implements ValueValidator<Object, R> {

		@Override
		public Validated<R> validate(@Nullable Object value, Locale locale, ConstraintContext constraintContext) {
			return this.validator.validate(value, locale, constraintContext);
		}

		@Override
		public ValueDescription description() {
			return this.validator.description();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <N> ValueValidator<N, R> readingWith(InputReader<N> reader) {
			return (ValueValidator<N, R>) (ValueValidator<?, R>) this.reading
				.apply((InputReader<Object>) (InputReader<?>) reader);
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
	 * Returns a validator that validates every element of an array of the given reader
	 * like {@link #elements(ValueValidator)} and reports any other node with
	 * {@code object.type}.
	 */
	private static <N, R> ValueValidator<N, List<R>> elements(InputReader<N> reader,
			ValueValidator<N, R> elementValidator) {
		return (node, locale, constraintContext) -> {
			if (node == null || !reader.isArray(node)) {
				final ViolationMessage message = ViolationMessage.Default.OBJECT_TYPE;
				return Validated.failureWith(ConstraintViolation.builder()
					.name("")
					.messageKey(message.messageKey())
					.defaultMessageFormat(message.defaultMessageFormat())
					.argsWithPrependedNameAndAppendedViolatedValue(new Object[] { "array" },
							new ViolatedValue(node == null ? null : reader.value(node)))
					.locale(locale)
					.build());
			}
			return Validated.traverseIndexed(reader.elements(node),
					(value, index) -> elementValidator
						.validate(value == null || reader.isNull(value) ? null : value, locale, constraintContext)
						.prefixed(index));
		};
	}

	/**
	 * Returns a validator of an untyped input read with the given reader that reports an
	 * input not representing the given type under the given name and otherwise validates
	 * like the given validator.
	 */
	private static <N, A, R> ValueValidator<N, R> untyped(InputReader<N> reader, String name, Class<A> type,
			ValueValidator<A, R> validator) {
		return ValueValidator.<A>passThrough()
			.untyped(type)
			.readingWith(reader)
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
