/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import org.jspecify.annotations.Nullable;

/**
 * Validates a {@code Map<String, Object>}, such as a parsed JSON object, and creates an
 * object from its values.
 *
 * <p>
 * Every property is read from the map by its name, checked against the type it must
 * represent (see {@link ValueValidator#untyped(Class)}) and validated. All violations are
 * accumulated and the object is created only when there is none. Instances are usually
 * created with {@code fromMap} of the builders returned by {@code Yavi.arguments()}:
 *
 * <pre>{@code
 * MapInputValidator<User> validator = Yavi.arguments()
 *     ._string("email", c -> c.notBlank().email())
 *     ._integer("age", c -> c.notNull().greaterThanOrEqual(0))
 *     ._string("nickname").optional()
 *     .fromMap(User::new);
 * Validated<User> validated = validator.validate(map);
 * }</pre>
 *
 * <ul>
 * <li>A key that is absent is validated like {@code null}, so a property that accepts
 * {@code null} may be omitted and {@code optional()}, {@code nullable()} and
 * {@code defaultValue} apply to it. If the property does not accept {@code null}, the
 * absent key is reported with the message key {@code object.required} instead of the
 * violations for {@code null}. An explicit {@code null} is reported by the constraints of
 * the property, for example {@code object.notNull}.</li>
 * <li>A value that does not represent the type of the property is reported with
 * {@code object.type}. Nothing is coerced unless the map is read with
 * {@link InputReader#coercingStrings()}.</li>
 * <li>A key that no property is defined for is ignored. With
 * {@link #rejectingUnknownProperties()} it is reported with
 * {@code object.unknownProperty}, and with {@code _rest} of the builders it is validated
 * and given to the created object, as {@link Rest} states.</li>
 * </ul>
 *
 * <p>
 * A validator may be embedded in another with {@code _embedded} of the builders: it reads
 * the same map as the other arguments instead of the value of a key, which builds an
 * object from the columns of several tables in the row of a JOIN, or from more keys than
 * a builder has arguments.
 *
 * <pre>{@code
 * MapInputValidator<Employee> validator = Yavi.arguments()
 *     ._integer("id", c -> c.notNull())
 *     ._embedded(userNameValidator) // first_name, last_name
 *     ._embedded(departmentValidator).nullable() // dept_name, dept_code
 *     .fromMap(Employee::new);
 * }</pre>
 *
 * <ul>
 * <li>The properties of an embedded validator are those of its
 * {@linkplain ValueValidator#description() description}, which a validator built with
 * {@code fromMap} and a {@link DiscriminatedUnionValidator} of such validators give. They
 * are properties of the embedding validator: it describes them, does not take them for
 * unknown and does not accept another property of the same name.</li>
 * <li>An embedded validator that accepts {@code null} ({@code optional()},
 * {@code nullable()}, {@code defaultValue}) is given {@code null} when none of its
 * properties has a value, like the columns of a LEFT JOIN that matches no row.</li>
 * <li>The embedding validator alone decides what the keys that no property is defined for
 * are. An embedded validator that rejects unknown properties sees only its own
 * properties, so it reports none unless it is a union whose alternatives differ.</li>
 * <li>Any other validator is embedded as it is: it reads the whole map and may be a
 * lambda. The properties it reads are unknown, so the embedding validator can neither
 * reject unknown properties nor read the rest.</li>
 * </ul>
 *
 * <p>
 * A {@code null} map is rejected with {@code object.notNull} and a map with a key that is
 * not a string with {@code object.type}. The validator describes itself as
 * {@link ValueDescription.Properties}, or as {@link ValueDescription.AnyOf} of them if an
 * embedded validator has alternatives.
 *
 * <p>
 * The same properties are read from the tree model of a JSON library, without copying it
 * to a map, with an {@link InputReader} implemented for that model:
 *
 * <pre>{@code
 * ValueValidator<JsonNode, User> nodeValidator = validator.readingWith(jsonNodeReader);
 * Validated<User> validated = nodeValidator.validate(node);
 * }</pre>
 *
 * The reader is applied to the nested validators too, so they are built with
 * {@code fromMap} whatever input is read in the end.
 *
 * <p>
 * Request parameters or a submitted form are read with {@link #readingParameters()}:
 * {@code "30"} is then an {@code Integer}, {@code "true"} a {@code Boolean}, an empty
 * string {@code null} and {@code address.city} the property {@code city} of the property
 * {@code address}.
 *
 * <pre>{@code
 * ValueValidator<Map<String, String>, User> formValidator = validator.readingParameters();
 * Validated<User> validated = formValidator.validate(requestParameters);
 * }</pre>
 *
 * @param <X> the type of the created object
 * @since 0.18.0
 * @see DiscriminatedUnionValidator
 */
public final class MapInputValidator<X> implements ValueValidator<Map<String, Object>, X> {

	private final List<Part<?>> parts;

	/**
	 * What is known about the embedded validator of the part at the same position;
	 * {@code null} for any other part and for an embedded validator that does not
	 * describe its properties.
	 */
	private final List<@Nullable Group> groups;

	/** The names of the properties that the parts define. */
	private final Set<String> names;

	private final Function<? super List<@Nullable Object>, ? extends Validated<? extends X>> mapper;

	private final boolean rejectUnknownProperties;

	private final ValueDescription description;

	private final Reading<Object> reading;

	/**
	 * A part of the object created by a {@link MapInputValidator}: the validated value of
	 * one property, of an embedded validator or of the rest of the properties.
	 *
	 * @param <R> the type of the validated value
	 */
	public sealed interface Part<R> {

		/**
		 * Returns the validator of the part.
		 * @return the validator, which reads a tree of JDK types
		 */
		ValueValidator<Object, R> validator();

	}

	/**
	 * A property read from the input map.
	 *
	 * @param name the key of the property in the map
	 * @param validator the validator of the value, which reports its violations under
	 * {@code name}; an absent key is validated as {@code null}. To read the property with
	 * another {@link InputReader}, the validator must know its input type (see
	 * {@link ValueValidator#readingWith(InputReader)})
	 * @param <R> the type of the validated value
	 */
	public record Property<R> (String name, ValueValidator<Object, R> validator) implements Part<R> {
	}

	/**
	 * A validator that reads the input map itself, next to the other parts, such as the
	 * columns of one table in the row of a JOIN.
	 *
	 * @param validator the validator of the map, which reports its violations as they
	 * are. What it {@linkplain ValueValidator#description() describes} decides how it is
	 * embedded, as {@link MapInputValidator} states
	 * @param <R> the type of the validated value
	 */
	public record Embedded<R> (ValueValidator<Object, R> validator) implements Part<R> {
	}

	/**
	 * The properties of the input map that no other part defines.
	 *
	 * @param validator the validator of a map of those properties, which is empty if
	 * there is none, such as {@link ValueValidator#liftMap()} of a validator of their
	 * values
	 * @param <R> the type of the validated value
	 */
	public record Rest<R> (ValueValidator<Object, R> validator) implements Part<R> {
	}

	/**
	 * Returns a validator that reads the given parts from a map and creates an object
	 * from their validated values. This is the way to define more properties than the
	 * builders support, or to define them at runtime.
	 * @param parts the parts to read
	 * @param mapper the function that creates the object from the validated values, which
	 * are given in the order of the parts
	 * @param <X> the type of the created object
	 * @return a validator that ignores unknown properties unless a part is the rest
	 * @throws IllegalArgumentException if two parts define a property with the same name
	 * or are the rest
	 * @throws IllegalStateException if a part is the rest and an embedded validator does
	 * not describe its properties
	 */
	public static <X> MapInputValidator<X> of(List<? extends Part<?>> parts,
			Function<? super List<@Nullable Object>, ? extends X> mapper) {
		return new MapInputValidator<>(List.copyOf(parts), values -> Validated.successWith(mapper.apply(values)),
				false);
	}

	/**
	 * Creates a validator from the given parts and a function that validates their values
	 * together. This is {@link #of} for a function that can fail: it returns the result
	 * or the violations of a rule across the parts.
	 *
	 * <p>
	 * The function is applied only when all parts are valid. A violation it reports is
	 * located relative to the map: one with an empty name is reported on the map itself
	 * and takes the name of the property the validator is applied to.
	 * @param parts the parts in the order of the values
	 * @param mapper function that creates the result from the validated values, in the
	 * order of the parts, or reports violations
	 * @param <X> the type of the result
	 * @return a new validator
	 * @throws IllegalArgumentException as {@link #of} does
	 * @since 0.18.0
	 */
	public static <X> MapInputValidator<X> flatOf(List<? extends Part<?>> parts,
			Function<? super List<@Nullable Object>, ? extends Validated<? extends X>> mapper) {
		return new MapInputValidator<>(List.copyOf(parts), mapper, false);
	}

	private MapInputValidator(List<Part<?>> parts,
			Function<? super List<@Nullable Object>, ? extends Validated<? extends X>> mapper,
			boolean rejectUnknownProperties) {
		// the described object: the properties of every way to read it, which are several
		// if an embedded validator has alternatives
		List<List<ValueDescription.Property>> alternatives = List.of(List.of());
		final List<@Nullable Group> groups = new ArrayList<>(parts.size());
		final Set<String> names = new HashSet<>();
		boolean open = false;
		ValueDescription rest = rejectUnknownProperties ? new ValueDescription.Never() : null;
		boolean hasRest = false;
		for (Part<?> part : parts) {
			final ValueDescription described = part.validator().description();
			List<List<ValueDescription.Property>> added = null;
			Group group = null;
			switch (part) {
				case Property<?> property ->
					added = List.of(List.of(new ValueDescription.Property(property.name(), described)));
				case Embedded<?> embedded -> {
					added = alternativesOf(described, false);
					if (added != null) {
						final Set<String> embeddedNames = new HashSet<>();
						added.forEach(properties -> properties.forEach(property -> embeddedNames.add(property.name())));
						group = new Group(embeddedNames, described.acceptsNull(), rejectsOthers(described));
					}
					else {
						open = true;
					}
				}
				case Rest<?> other -> {
					if (hasRest) {
						throw new IllegalArgumentException("The rest of the properties is defined twice.");
					}
					hasRest = true;
					rest = described instanceof ValueDescription.Entries entries ? entries.value()
							: new ValueDescription.Unknown();
				}
			}
			groups.add(group);
			if (added != null) {
				alternatives = product(alternatives, added);
				added.forEach(properties -> properties.forEach(property -> names.add(property.name())));
			}
		}
		if (open && (hasRest || rejectUnknownProperties)) {
			throw new IllegalStateException("The properties that an embedded validator reads are unknown, so the "
					+ (hasRest ? "rest of the properties cannot be told" : "unknown properties cannot be rejected")
					+ ". Embed a validator that describes its properties, such as one built with fromMap.");
		}
		if (hasRest && rejectUnknownProperties) {
			throw new IllegalStateException(
					"No property is unknown to a validator that reads the rest of the properties.");
		}
		if (open) {
			rest = new ValueDescription.Unknown();
		}
		final ValueDescription otherProperties = rest;
		final List<ValueDescription> objects = alternatives.stream()
			.<ValueDescription>map(properties -> new ValueDescription.Properties(properties, otherProperties))
			.toList();
		this.parts = parts;
		this.groups = groups;
		this.names = names;
		this.mapper = mapper;
		this.rejectUnknownProperties = rejectUnknownProperties;
		this.description = objects.size() == 1 ? objects.get(0) : new ValueDescription.AnyOf(objects);
		this.reading = this.reading(InputReader.ofJdkTypes());
	}

	/**
	 * What is known about an embedded validator that describes its properties.
	 *
	 * @param names the names of the properties that the validator may read
	 * @param nullable whether the validator is given {@code null} when none of its
	 * properties has a value
	 * @param projected whether the validator must not see the other properties, which it
	 * would reject
	 */
	private record Group(Set<String> names, boolean nullable, boolean projected) {
	}

	/**
	 * Returns the properties of an object with the given description, once for every way
	 * to read it, or {@code null} if the properties that are read are not all described.
	 * @param optional whether the object may be absent, so that none of its properties is
	 * required
	 */
	private static @Nullable List<List<ValueDescription.Property>> alternativesOf(ValueDescription description,
			boolean optional) {
		return switch (description) {
			case ValueDescription.Properties properties when properties.isClosed() -> List.of(optional
					? properties.properties()
						.stream()
						.map(property -> property.value().acceptsNull() ? property
								: new ValueDescription.Property(property.name(),
										new ValueDescription.Optional(property.value(), null)))
						.toList()
					: properties.properties());
			case ValueDescription.NotNull notNull -> alternativesOf(notNull.value(), optional);
			case ValueDescription.Optional nullable -> alternativesOf(nullable.value(), true);
			case ValueDescription.AnyOf anyOf -> {
				final List<List<ValueDescription.Property>> alternatives = new ArrayList<>();
				for (ValueDescription alternative : anyOf.alternatives()) {
					final List<List<ValueDescription.Property>> described = alternativesOf(alternative, optional);
					if (described == null) {
						yield null;
					}
					alternatives.addAll(described);
				}
				yield alternatives;
			}
			default -> null;
		};
	}

	/**
	 * Returns whether an object with the given description rejects the properties that
	 * are not described.
	 */
	private static boolean rejectsOthers(ValueDescription description) {
		return switch (description) {
			case ValueDescription.Properties properties -> properties.rest() instanceof ValueDescription.Never;
			case ValueDescription.NotNull notNull -> rejectsOthers(notNull.value());
			case ValueDescription.Optional nullable -> rejectsOthers(nullable.value());
			case ValueDescription.AnyOf anyOf -> anyOf.alternatives().stream().anyMatch(MapInputValidator::rejectsOthers);
			default -> false;
		};
	}

	/**
	 * Returns every combination of the given ways to read an object with the given ways
	 * to read more of its properties.
	 * @throws IllegalArgumentException if a property is in both
	 */
	private static List<List<ValueDescription.Property>> product(List<List<ValueDescription.Property>> alternatives,
			List<List<ValueDescription.Property>> added) {
		final List<List<ValueDescription.Property>> product = new ArrayList<>(alternatives.size() * added.size());
		for (List<ValueDescription.Property> properties : alternatives) {
			for (List<ValueDescription.Property> more : added) {
				final List<ValueDescription.Property> all = new ArrayList<>(properties);
				for (ValueDescription.Property property : more) {
					if (properties.stream().anyMatch(defined -> defined.name().equals(property.name()))) {
						throw new IllegalArgumentException("The property '" + property.name() + "' is defined twice.");
					}
					all.add(property);
				}
				product.add(all);
			}
		}
		return product;
	}

	/**
	 * Returns a validator that reports the keys of the input map that no property is
	 * defined for with {@code object.unknownProperty}, instead of ignoring them. The
	 * properties of the embedded validators are defined. It applies to this map only: a
	 * nested validator decides for its own map.
	 * @return a validator that rejects unknown properties
	 * @throws IllegalStateException if the validator reads the rest of the properties, or
	 * if an embedded validator does not describe its properties
	 */
	public MapInputValidator<X> rejectingUnknownProperties() {
		return new MapInputValidator<>(this.parts, this.mapper, true);
	}

	/**
	 * Returns a validator that reads the same properties from a flat map of parameters,
	 * such as request parameters or a submitted form. The map is read as the
	 * {@link ParameterTree} it stands for: a key such as {@code address.city} or
	 * {@code items[0].name} is a path to a nested property, a key may have several values
	 * and a value is text that stands for a value of the type of its property, as
	 * {@link InputReader#coercingStrings()} states. This applies to the nested validators
	 * too. The type of the map is the one the result is assigned to:
	 *
	 * <pre>{@code
	 * ValueValidator<Map<String, String>, SearchForm> validator = Yavi.arguments()
	 *     ._string("q", c -> c.notBlank())
	 *     ._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
	 *     .fromMap(SearchForm::new)
	 *     .readingParameters();
	 * }</pre>
	 * @param <M> the type of the map, whose values are strings, or iterables or arrays of
	 * strings
	 * @return a validator of a map of parameters
	 * @see ParameterTree
	 */
	public <M extends Map<String, ?>> ValueValidator<M, X> readingParameters() {
		return ParameterTree.reading(this);
	}

	@Override
	public Validated<X> validate(@Nullable Map<String, Object> input, Locale locale,
			ConstraintContext constraintContext) {
		return this.reading.validate(input, locale, constraintContext);
	}

	@Override
	public ValueDescription description() {
		return this.description;
	}

	/**
	 * Returns a validator that reads the same properties from an object of the given
	 * reader instead of a map, and rejects any other node with {@code object.type}. The
	 * reader is applied to the validators of the parts too.
	 * @param reader the reader of the nodes
	 * @param <N> the type of the nodes
	 * @return a value validator for an untyped input
	 * @throws UnsupportedOperationException if the reader is not the one of JDK types and
	 * the validator of a part does not know its input type
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
		return reader == InputReader.ofJdkTypes() ? (ValueValidator<N, X>) this.reading : this.reading(reader);
	}

	/**
	 * Returns whether the part at the given position is given a {@link Projection} of the
	 * object instead of the object.
	 */
	private boolean isProjected(int position) {
		final Group group = this.groups.get(position);
		return this.parts.get(position) instanceof Rest || (group != null && group.projected());
	}

	@SuppressWarnings("unchecked")
	private <N> Reading<N> reading(InputReader<N> reader) {
		final boolean jdkTypes = reader == InputReader.ofJdkTypes();
		final List<ValueValidator<Object, ?>> validators = new ArrayList<>(this.parts.size());
		for (int i = 0; i < this.parts.size(); i++) {
			final ValueValidator<Object, ?> validator = this.parts.get(i).validator();
			// a projection of a map is a map, and one of any other object is read as the
			// object it shows
			validators
				.add(jdkTypes ? validator : this.isProjected(i) ? validator.readingWith(new Projection.Reader<>(reader))
						: (ValueValidator<Object, ?>) validator.readingWith(reader));
		}
		return new Reading<>(reader, validators);
	}

	/**
	 * Reads the parts from an object of a reader.
	 */
	private final class Reading<N> implements ValueValidator<N, X> {

		private final InputReader<N> reader;

		/**
		 * The validators of the parts, in their order. They take a node of the reader, or
		 * a {@link Projection} of the object if the part is projected.
		 */
		private final List<ValueValidator<Object, ?>> validators;

		private Reading(InputReader<N> reader, List<ValueValidator<Object, ?>> validators) {
			this.reader = reader;
			this.validators = validators;
		}

		@Override
		public Validated<X> validate(@Nullable N input, Locale locale, ConstraintContext constraintContext) {
			if (input == null || this.reader.isNull(input)) {
				return Validated.failureWith(
						violation("", ViolationMessage.Default.OBJECT_NOT_NULL, null, locale));
			}
			if (!this.reader.isObject(input)) {
				return Validated.failureWith(violation("", ViolationMessage.Default.OBJECT_TYPE,
						this.reader.value(input), locale, "object"));
			}
			final List<Part<?>> parts = MapInputValidator.this.parts;
			final Set<String> names = MapInputValidator.this.names;
			final List<@Nullable Object> values = new ArrayList<>(parts.size());
			final List<ConstraintViolation> violations = new ArrayList<>();
			for (int i = 0; i < parts.size(); i++) {
				final ValueValidator<Object, ?> validator = this.validators.get(i);
				final Validated<?> validated;
				switch (parts.get(i)) {
					case Property<?> property -> {
						final String name = property.name();
						final N value = this.reader.property(input, name);
						final Validated<?> read = validator.validate(
								value == null || this.reader.isNull(value) ? null : value, locale, constraintContext);
						// an absent key is required rather than null
						validated = read.isValid() || value != null || this.reader.hasProperty(input, name) ? read
								: Validated.failureWith(violation(name, ViolationMessage.Default.OBJECT_REQUIRED,
										null, locale));
					}
					case Embedded<?> embedded -> {
						final Group group = MapInputValidator.this.groups.get(i);
						final Object object = group == null ? input
								: group.nullable() && this.hasNoValue(input, group.names()) ? null
										: group.projected() ? this.projection(input, group.names()::contains) : input;
						validated = validator.validate(object, locale, constraintContext);
					}
					case Rest<?> rest -> validated = validator
						.validate(this.projection(input, name -> !names.contains(name)), locale, constraintContext);
				}
				if (validated.isValid()) {
					values.add(validated.value());
				}
				else {
					violations.addAll(validated.errors());
				}
			}
			if (MapInputValidator.this.rejectUnknownProperties) {
				for (Map.Entry<String, ? extends @Nullable N> property : this.reader.properties(input)) {
					if (!names.contains(property.getKey())) {
						final N value = property.getValue();
						violations.add(violation(property.getKey(), ViolationMessage.Default.OBJECT_UNKNOWN_PROPERTY,
								value == null || this.reader.isNull(value) ? null : this.reader.value(value),
								locale));
					}
				}
			}
			return violations.isEmpty()
					? Validated.of(MapInputValidator.this.mapper.apply(Collections.unmodifiableList(values)))
					: Validated.failureWith(violations);
		}

		/**
		 * Returns whether every property with one of the given names is absent or
		 * {@code null}.
		 */
		private boolean hasNoValue(N object, Set<String> names) {
			for (String name : names) {
				final N value = this.reader.property(object, name);
				if (value != null && !this.reader.isNull(value)) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Returns the object as it is shown to a part that sees only the properties with
		 * the given names.
		 */
		@SuppressWarnings("unchecked")
		private Object projection(N object, Predicate<String> names) {
			return this.reader == InputReader.ofJdkTypes() ? Projection.of((Map<String, Object>) object, names)
					: new Projection<>(object, names);
		}

		@Override
		public ValueDescription description() {
			return MapInputValidator.this.description();
		}

		@Override
		public <M> ValueValidator<M, X> readingWith(InputReader<M> reader) {
			return MapInputValidator.this.readingWith(reader);
		}

	}

	/**
	 * Returns a violation located at the given name, which is one path segment whatever
	 * characters it holds.
	 */
	static ConstraintViolation violation(String name, ViolationMessage message, @Nullable Object violatedValue,
			Locale locale, Object... args) {
		return ConstraintViolation.of(message, violatedValue, locale, args).prefixed(name);
	}

}
