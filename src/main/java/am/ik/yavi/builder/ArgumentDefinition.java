/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import org.jspecify.annotations.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.NullAcceptingValueValidator;
import am.ik.yavi.core.Presence;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import am.ik.yavi.input.ObjectInputValidator;
import am.ik.yavi.input.PropertyMessages;
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
 * @param strictness whether the key of an argument read from a key is told from
 * {@code null}
 * @param neverNull whether the validated value is known never to be {@code null}: the
 * value of constraints, a list, a map or an {@code Optional}. It is unknown for a value
 * validator, whose type may be nullable
 * @param messages what an argument read from a key is called in a message, and what is
 * said when it has no value or a value of another type. The validator applies the label
 * itself; the part that reads the argument from a map applies the rest
 * @param separated returns the definition of this argument as a list that an untyped
 * input may give as text whose elements the given pattern separates; {@code null} unless
 * the argument is a list to which no modifier is applied yet
 * @param <A> the type of the argument
 * @param <R> the type of the validated value of the argument
 */
record ArgumentDefinition<A, R extends @Nullable Object> (@Nullable String name, ValueValidator<A, R> validator,
		Function<InputReader<Object>, ValueValidator<Object, R>> untyped, Source source,
		ObjectInputValidator.Strictness strictness, boolean neverNull, PropertyMessages messages,
		@Nullable Function<Pattern, ArgumentDefinition<A, R>> separated) {

	private static final String RECOVER_WITH_NULL = "Write nullable() before it, or recover with Optional.empty() after optional().";

	/**
	 * What an argument is read from when its validator reads a map.
	 */
	enum Source {

		/** The value of the key that is the name of the argument. */
		PROPERTY,

		/**
		 * The key that is the name of the argument: whether the map has it, and its
		 * value.
		 */
		PRESENCE,

		/** The map itself. */
		OBJECT,

		/** The keys of the map that no other argument reads. */
		REST

	}

	ArgumentDefinition(@Nullable String name, ValueValidator<A, R> validator,
			Function<InputReader<Object>, ValueValidator<Object, R>> untyped, Source source, boolean neverNull) {
		this(name, validator, untyped, source, ObjectInputValidator.Strictness.ABSENT_AS_NULL, neverNull,
				PropertyMessages.none(), null);
	}

	/**
	 * Returns the definition of this argument with the given validators, which stand for
	 * those of this definition: the key, its strictness and its messages are kept.
	 */
	private <A2, R2 extends @Nullable Object> ArgumentDefinition<A2, R2> derive(ValueValidator<A2, R2> validator,
			Function<InputReader<Object>, ValueValidator<Object, R2>> untyped, Source source, boolean neverNull) {
		return new ArgumentDefinition<>(this.name, validator, untyped, source, this.strictness, neverNull,
				this.messages, null);
	}

	/**
	 * Returns the definition of this argument with the given messages. The violations
	 * that the validator of a typed argument reports are labeled.
	 */
	ArgumentDefinition<A, R> withMessages(PropertyMessages messages) {
		return new ArgumentDefinition<>(this.name,
				messages.label() == null ? this.validator
						: new Labeled<>(this.keyName(), new PropertyMessages(messages.label(), null, null),
								unlabeled(this.validator)),
				this.untyped, this.source, this.strictness, this.neverNull, messages, null);
	}

	private static <A, R extends @Nullable Object> ValueValidator<A, R> unlabeled(ValueValidator<A, R> validator) {
		return validator instanceof Labeled<A, R>labeled ? labeled.validator() : validator;
	}

	/**
	 * The validator of a typed argument whose violations are labeled.
	 */
	private record Labeled<A, R extends @Nullable Object> (String name, PropertyMessages messages,
			ValueValidator<A, R> validator) implements ValueValidator<A, R> {

		@Override
		public Validated<R> validate(@Nullable A value, Locale locale, ConstraintContext constraintContext) {
			return this.messages.applyTo(this.name, this.validator.validate(value, locale, constraintContext));
		}

		@Override
		public ValueDescription description() {
			return this.validator.description();
		}

		@Override
		public <N> ValueValidator<N, R> readingWith(InputReader<N> reader) {
			return new Labeled<>(this.name, this.messages, this.validator.readingWith(reader));
		}

	}

	/**
	 * Returns the key of a property: the name of the argument, or else the name under
	 * which its validator reports violations.
	 */
	private String keyName() {
		return this.name != null ? this.name : describedName(this.validator.description());
	}

	/**
	 * Returns the definition of an argument added only with a value validator. If the
	 * validator does not describe itself, it is described as a value of the given type.
	 * The given type is the input type of the validator unless it is {@code Object} or
	 * {@code Enum}, which stand for a type that the builder method does not determine. An
	 * untyped input is read as the validator reads it, so what the validator says about
	 * reading, such as {@code untrimmed()} of a string, holds in a schema; a validator
	 * that does not know its input type, such as a lambda, is given a value of the given
	 * type.
	 */
	@SuppressWarnings("unchecked")
	static <A, R extends @Nullable Object> ArgumentDefinition<A, R> value(Class<?> type,
			ValueValidator<A, R> validator) {
		final boolean undescribed = validator.description() instanceof ValueDescription.Unknown && type != Object.class;
		final ValueValidator<A, R> described = undescribed
				? validator.describedAs(new ValueDescription.Value("", type, List.of())) : validator;
		return new ArgumentDefinition<>(null, described,
				type == Object.class || type == Enum.class ? reader -> validator.readingWith(reader) : reader -> {
					if (!undescribed) {
						try {
							return validator.readingWith(reader);
						}
						catch (UnsupportedOperationException e) {
							// the validator does not know its input type
						}
					}
					return untyped(reader, describedName(described.description()), (Class<A>) type, described);
				}, Source.PROPERTY, false);
	}

	/**
	 * Returns the definition of a named argument of the given type, validated by the
	 * given validator, which is built from constraints and so rejects {@code null}.
	 */
	static <A> ArgumentDefinition<A, A> value(String name, Class<A> type, ValueValidator<A, A> validator) {
		return new ArgumentDefinition<>(name, validator, reader -> untyped(reader, name, type, validator),
				Source.PROPERTY, true);
	}

	/**
	 * Returns the definition of a nested object that rejects {@code null} under the given
	 * name and otherwise reports the violations of the given validator under that name,
	 * as {@link ValueValidator#named(String)} states.
	 */
	static <A, R extends @Nullable Object> ArgumentDefinition<A, R> object(String name,
			ValueValidator<A, R> validator) {
		final ValueDescription description = validator.description().named(name);
		return new ArgumentDefinition<>(name, nested(name, validator, description),
				reader -> nested(name, validator.readingWith(reader), description), Source.PROPERTY, false);
	}

	/**
	 * Returns the definition of a nested object whose validator decides what {@code null}
	 * produces. The violations of any other value are reported under the given name.
	 */
	static <A, R extends @Nullable Object> ArgumentDefinition<A, R> object(String name,
			NullAcceptingValueValidator<A, R> validator) {
		return new ArgumentDefinition<>(name, validator.named(name),
				reader -> validator.readingWith(reader).named(name), Source.PROPERTY, false);
	}

	/**
	 * Returns the definition of a list whose elements are validated one by one and whose
	 * constraints are checked on the validated elements, only when all of them are valid.
	 */
	static <E, R extends @Nullable Object> ArgumentDefinition<List<? extends E>, List<R>> list(String name,
			ValueValidator<E, R> elementValidator, ContainerConstraints<List<R>> constraints) {
		return list(name, elementValidator, constraints, null);
	}

	/**
	 * Returns the definition of a list that an untyped input may give as text whose
	 * elements the given pattern separates, if any, as
	 * {@link ValueValidator#liftList(Pattern)} states. A typed list is validated as
	 * before: it holds the elements already.
	 */
	private static <E, R extends @Nullable Object> ArgumentDefinition<List<? extends E>, List<R>> list(String name,
			ValueValidator<E, R> elementValidator, ContainerConstraints<List<R>> constraints,
			@Nullable Pattern separator) {
		final ValueDescription description = new ValueDescription.Elements(elementValidator.description(),
				constraints.descriptions(), separator == null ? null : separator.pattern());
		final ValueValidator<List<? extends E>, List<R>> elements = elements(elementValidator)::validate;
		final ValueValidator<Iterable<? extends E>, List<R>> separated = separator == null ? null
				: elementValidator.liftList(separator);
		return new ArgumentDefinition<>(name, nested(name, constraints.constrain(elements), description),
				reader -> nested(name,
						constraints.constrain(separated == null ? elements(reader, elementValidator.readingWith(reader))
								: separated.readingWith(reader)),
						description),
				Source.PROPERTY, ObjectInputValidator.Strictness.ABSENT_AS_NULL, true, PropertyMessages.none(),
				separator == null ? pattern -> list(name, elementValidator, constraints, pattern) : null);
	}

	/**
	 * Returns the definition of this list as one that an untyped input may give as text
	 * whose elements the given pattern separates. The builders offer this only for a list
	 * to which no modifier is applied yet.
	 * @throws IllegalArgumentException if the pattern matches the empty string
	 */
	ArgumentDefinition<A, R> separatedBy(Pattern separator) {
		return Objects.requireNonNull(this.separated, "The argument is not a list.").apply(separator);
	}

	/**
	 * Returns the definition of a map whose values are validated one by one and whose
	 * constraints are checked on the validated values, only when all of them are valid.
	 */
	static <V, R extends @Nullable Object> ArgumentDefinition<Map<String, ? extends V>, Map<String, R>> map(String name,
			ValueValidator<V, R> valueValidator, ContainerConstraints<Map<String, R>> constraints) {
		final ValueValidator<Map<String, ? extends V>, Map<String, R>> lifted = valueValidator.liftMap();
		final ValueDescription description = new ValueDescription.Entries(valueValidator.description(),
				constraints.descriptions());
		return new ArgumentDefinition<>(name, nested(name, constraints.constrain(lifted), description),
				reader -> nested(name, constraints.constrain(lifted.readingWith(reader)), description), Source.PROPERTY,
				true);
	}

	/**
	 * Returns the definition of an argument that reads the same map as the other
	 * arguments. A validator that does not know its input type, such as a lambda, reads a
	 * map only.
	 */
	@SuppressWarnings("unchecked")
	static <R extends @Nullable Object> ArgumentDefinition<Map<String, Object>, R> embedded(
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
		return new ArgumentDefinition<>(null, ofMap, untyped, Source.OBJECT, false);
	}

	/**
	 * Returns the definition of an argument that reads the keys of the map that no other
	 * argument reads. The violations of a value are located under its key.
	 */
	static <V, R extends @Nullable Object> ArgumentDefinition<Map<String, ? extends V>, Map<String, R>> rest(
			ValueValidator<V, R> valueValidator) {
		final ValueValidator<Map<String, ? extends V>, Map<String, R>> lifted = valueValidator.liftMap();
		return new ArgumentDefinition<>(null, lifted, reader -> lifted.readingWith(reader), Source.REST, true);
	}

	/**
	 * Returns the definition of an argument that reads the keys of the map that no other
	 * argument reads and takes their values as they are.
	 */
	static ArgumentDefinition<Map<String, ?>, Map<String, @Nullable Object>> rest() {
		return rest(ValueValidator.passThrough().untyped(Object.class));
	}

	ArgumentDefinition<A, Optional<@NonNull R>> asOptional() {
		return this.derive(this.validator.optional(), reader -> this.untyped.apply(reader).optional(), this.source,
				true);
	}

	ArgumentDefinition<A, @Nullable R> asNullable() {
		return this.derive(this.validator.nullable(), reader -> this.untyped.apply(reader).nullable(), this.source,
				false);
	}

	/**
	 * @throws NullPointerException if the default value is {@code null} although the
	 * validated value is never {@code null}
	 */
	ArgumentDefinition<A, R> withDefault(R defaultValue) {
		final R checked = this.nonNull(defaultValue, "default value", "Write nullable() instead.");
		return this.derive(this.validator.defaultValue(checked),
				reader -> this.untyped.apply(reader).defaultValue(checked), this.source, this.neverNull);
	}

	/**
	 * @throws NullPointerException if the fallback is {@code null} although the validated
	 * value is never {@code null}
	 */
	ArgumentDefinition<A, R> recovering(R fallback) {
		final R checked = this.nonNull(fallback, "fallback", RECOVER_WITH_NULL);
		return this.derive(this.validator.recover(checked), reader -> this.untyped.apply(reader).recover(checked),
				this.source, this.neverNull);
	}

	/**
	 * Returns the definition of this argument with the given function as its fallback.
	 * The function fails with {@code NullPointerException} when it returns {@code null}
	 * although the validated value is never {@code null}.
	 */
	ArgumentDefinition<A, R> recoveringWith(Function<? super ConstraintViolations, ? extends R> fallback) {
		final Function<? super ConstraintViolations, ? extends R> checked = this.neverNull ? violations -> this
			.nonNull(fallback.apply(violations), "result of the fallback function", RECOVER_WITH_NULL) : fallback;
		return this.derive(this.validator.recoverWith(checked),
				reader -> this.untyped.apply(reader).recoverWith(checked), this.source, this.neverNull);
	}

	/**
	 * Returns the given value, which stands in for the validated value, unless it is
	 * {@code null} although the validated value is never {@code null}: the function that
	 * creates the result would be given a {@code null} that the type of its parameter
	 * excludes.
	 */
	private <V extends @Nullable Object> V nonNull(V value, String role, String hint) {
		if (this.neverNull && value == null) {
			throw new NullPointerException(
					"The " + role + " of the argument" + (this.name != null ? " '" + this.name + "'" : "")
							+ " is null, but its validated value is never null. " + hint);
		}
		return value;
	}

	/**
	 * Returns the definition of this argument as a value that may be absent. The untyped
	 * validator reads the value of a key that is present; the part tells an absent key.
	 * The builders offer this only for an argument that is read from a key, and no
	 * modifier after it.
	 */
	ArgumentDefinition<Presence<? extends A>, Presence<@NonNull R>> withPresence() {
		return this.derive(this.validator.liftPresence(), reader -> {
			final ValueValidator<Object, R> present = this.untyped.apply(reader);
			return present.<Presence<@NonNull R>>map(value -> Presence.<@NonNull R>ofNullable(value))
				.describedAs(new ValueDescription.Presence(present.description()));
		}, Source.PRESENCE, true);
	}

	/**
	 * Returns the definition of this argument with the given strictness of its key, which
	 * its description tells. The typed argument is validated as before: it has no absent
	 * state. The builders offer this only for an argument that is read from a key, and no
	 * modifier after it.
	 */
	ArgumentDefinition<A, R> withStrictness(ObjectInputValidator.Strictness strictness) {
		return new ArgumentDefinition<>(this.name,
				this.validator.describedAs(strictness.describe(this.validator.description())), this.untyped,
				this.source, strictness, this.neverNull, this.messages, null);
	}

	/**
	 * Returns the part that reads this argument from a map. The key of a property is the
	 * name of the argument, or else the name under which its validator reports
	 * violations.
	 * @param position the position of the argument starting at 1
	 * @throws IllegalStateException if the argument is a property that has no name, or if
	 * its input type is unknown
	 */
	@SuppressWarnings("unchecked")
	ObjectInputValidator.Part<R> part(int position) {
		final String name = this.keyName();
		if ((this.source == Source.PROPERTY || this.source == Source.PRESENCE) && name.isEmpty()) {
			throw new IllegalStateException("The argument at position " + position
					+ " has no name. Add it with a name to read it from a map, or with _embedded to read the map itself.");
		}
		final ValueValidator<Object, R> reading;
		try {
			reading = new Reading<>(this.untyped.apply(InputReader.ofJdkTypes()), this.untyped);
		}
		catch (UnsupportedOperationException e) {
			throw new IllegalStateException("The input type of the argument "
					+ (name.isEmpty() ? "at position " + position : "'" + name + "'")
					+ " is unknown. Add it with a validator that knows its input type (see ValueValidator#untyped).",
					e);
		}
		return switch (this.source) {
			case PROPERTY -> new ObjectInputValidator.Property<>(name, reading, this.strictness, this.messages);
			case PRESENCE -> (ObjectInputValidator.Part<R>) new ObjectInputValidator.PresenceProperty<>(name,
					(ValueValidator<Object, Presence<Object>>) (ValueValidator<Object, ?>) reading, this.messages);
			case OBJECT -> new ObjectInputValidator.Embedded<>(reading);
			case REST -> new ObjectInputValidator.Rest<>(reading);
		};
	}

	/**
	 * The validator of an argument read from a tree of JDK types, which reads the nodes
	 * of another reader as the argument defines it.
	 */
	private record Reading<R extends @Nullable Object> (ValueValidator<Object, R> validator,
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
	private static <E, R extends @Nullable Object> ValueValidator<Iterable<? extends E>, List<R>> elements(
			ValueValidator<E, R> elementValidator) {
		return (values, locale, constraintContext) -> Validated.traverseIndexed(values,
				(value, index) -> elementValidator.validate(value, locale, constraintContext).prefixed(index));
	}

	/**
	 * Returns a validator that validates every element of an array of the given reader
	 * like {@link #elements(ValueValidator)} and reports any other node with
	 * {@code object.type}.
	 */
	private static <N, R extends @Nullable Object> ValueValidator<N, List<R>> elements(InputReader<N> reader,
			ValueValidator<N, R> elementValidator) {
		return (node, locale, constraintContext) -> {
			if (node == null || !reader.isArray(node)) {
				return Validated.failureWith(ViolationMessage.Default.OBJECT_TYPE,
						node == null ? null : reader.value(node), locale, "array");
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
	private static <N, A, R extends @Nullable Object> ValueValidator<N, R> untyped(InputReader<N> reader, String name,
			Class<A> type, ValueValidator<A, R> validator) {
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
			case ValueDescription.Presence presence -> describedName(presence.value());
			case ValueDescription.Required required -> describedName(required.value());
			case ValueDescription.Value value -> value.name();
			default -> "";
		};
	}

	/**
	 * Returns the description of an object whose properties are the given arguments in
	 * order. An argument without a name is named as
	 * {@link ValueDescription.Properties#ofArguments} states.
	 * @throws IllegalArgumentException if two arguments have the same name
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
			final ValueDescription.Property property = name != null
					? new ValueDescription.Property(name, inferred.get(i).value()) : inferred.get(i);
			if (properties.stream().anyMatch(defined -> defined.name().equals(property.name()))) {
				throw new IllegalArgumentException("The property '" + property.name() + "' is defined twice.");
			}
			properties.add(property);
		}
		return new ValueDescription.Properties(properties);
	}

	/**
	 * Returns a validator that rejects {@code null} with {@code object.notNull} under the
	 * given name and otherwise reports the violations of the given validator under that
	 * name: those of a value that has no name of its own are located under it. The given
	 * description is the one of a value other than {@code null}.
	 */
	private static <A, R extends @Nullable Object> ValueValidator<A, R> nested(String name,
			ValueValidator<A, R> validator, ValueDescription description) {
		return ObjectValidatorBuilder.<A>of(name, ObjectConstraint::notNull)
			.build()
			.andThen(validator.named(name))
			.describedAs(new ValueDescription.NotNull(description));
	}

}
