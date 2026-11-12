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

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.PathSegment;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolatedValue;
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
 * {@code object.type}. Nothing is coerced.</li>
 * <li>A key that no property is defined for is reported with
 * {@code object.unknownProperty}, unless {@link #ignoringUnknownProperties()} is
 * applied.</li>
 * </ul>
 *
 * <p>
 * A {@code null} map is rejected with {@code object.notNull} and a map with a key that is
 * not a string with {@code object.type}. The validator describes itself as
 * {@link ValueDescription.Properties}.
 *
 * <p>
 * The same properties are read from the tree model of a JSON library, without copying it
 * to a map, with an {@link InputReader} implemented for that model:
 *
 * <pre>{@code
 * ValueValidator<JsonNode, User> nodeValidator = validator.untyped(jsonNodeReader);
 * Validated<User> validated = nodeValidator.validate(node);
 * }</pre>
 *
 * The reader is applied to the nested validators too, so they are built with
 * {@code fromMap} whatever input is read in the end.
 *
 * @param <X> the type of the created object
 * @since 0.18.0
 * @see DiscriminatedUnionValidator
 */
public final class MapInputValidator<X> implements ValueValidator<Map<String, Object>, X> {

	private final List<Property<?>> properties;

	private final Set<String> names;

	private final Function<? super List<@Nullable Object>, ? extends X> mapper;

	private final boolean ignoreUnknownProperties;

	private final Reading<Object> reading;

	/**
	 * A property read from the input map.
	 *
	 * @param name the key of the property in the map
	 * @param validator the validator of the value, which reports its violations under
	 * {@code name}; an absent key is validated as {@code null}. To read the property with
	 * another {@link InputReader}, the validator must know its input type (see
	 * {@link ValueValidator#untyped(InputReader)})
	 * @param <R> the type of the validated value
	 */
	public record Property<R> (String name, ValueValidator<Object, R> validator) {
	}

	/**
	 * Returns a validator that reads the given properties from a map and creates an
	 * object from their validated values. This is the way to define more properties than
	 * the builders support, or to define them at runtime.
	 * @param properties the properties to read
	 * @param mapper the function that creates the object from the validated values, which
	 * are given in the order of the properties
	 * @param <X> the type of the created object
	 * @return a validator that rejects unknown properties
	 * @throws IllegalArgumentException if two properties have the same name
	 */
	public static <X> MapInputValidator<X> of(List<? extends Property<?>> properties,
			Function<? super List<@Nullable Object>, ? extends X> mapper) {
		return new MapInputValidator<>(List.copyOf(properties), mapper, false);
	}

	private MapInputValidator(List<Property<?>> properties,
			Function<? super List<@Nullable Object>, ? extends X> mapper, boolean ignoreUnknownProperties) {
		final Set<String> names = new HashSet<>();
		for (Property<?> property : properties) {
			if (!names.add(property.name())) {
				throw new IllegalArgumentException("The property '" + property.name() + "' is defined twice.");
			}
		}
		this.properties = properties;
		this.names = names;
		this.mapper = mapper;
		this.ignoreUnknownProperties = ignoreUnknownProperties;
		this.reading = new Reading<>(InputReader.ofJdkTypes(),
				properties.stream().<ValueValidator<Object, ?>>map(Property::validator).toList());
	}

	/**
	 * Returns a validator that ignores the keys of the input map that no property is
	 * defined for, instead of reporting them with {@code object.unknownProperty}.
	 * @return a validator that ignores unknown properties
	 */
	public MapInputValidator<X> ignoringUnknownProperties() {
		return new MapInputValidator<>(this.properties, this.mapper, true);
	}

	@Override
	public Validated<X> validate(@Nullable Map<String, Object> input, Locale locale,
			ConstraintContext constraintContext) {
		return this.reading.validate(input, locale, constraintContext);
	}

	@Override
	public ValueDescription description() {
		return new ValueDescription.Properties(this.properties.stream()
			.map(property -> new ValueDescription.Property(property.name(), property.validator().description()))
			.toList());
	}

	/**
	 * Returns a validator that reads the same properties from an object of the given
	 * reader instead of a map, and rejects any other node with {@code object.type}. The
	 * reader is applied to the validators of the properties too.
	 * @param reader the reader of the nodes
	 * @param <N> the type of the nodes
	 * @return a value validator for an untyped input
	 * @throws UnsupportedOperationException if the reader is not the one of JDK types and
	 * the validator of a property does not know its input type
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <N> ValueValidator<N, X> untyped(InputReader<N> reader) {
		if (reader == InputReader.ofJdkTypes()) {
			return (ValueValidator<N, X>) this.reading;
		}
		return new Reading<>(reader,
				this.properties.stream()
					.<ValueValidator<N, ?>>map(property -> property.validator().untyped(reader))
					.toList());
	}

	/**
	 * Reads the properties from an object of a reader.
	 */
	private final class Reading<N> implements ValueValidator<N, X> {

		private final InputReader<N> reader;

		/** The validators of the properties, in their order. */
		private final List<ValueValidator<N, ?>> validators;

		private Reading(InputReader<N> reader, List<ValueValidator<N, ?>> validators) {
			this.reader = reader;
			this.validators = validators;
		}

		@Override
		public Validated<X> validate(@Nullable N input, Locale locale, ConstraintContext constraintContext) {
			if (input == null || this.reader.isNull(input)) {
				return Validated.failureWith(
						violation("", ViolationMessage.Default.OBJECT_NOT_NULL, new ViolatedValue(null), locale));
			}
			if (!this.reader.isObject(input)) {
				return Validated.failureWith(violation("", ViolationMessage.Default.OBJECT_TYPE,
						new ViolatedValue(this.reader.value(input)), locale, "object"));
			}
			final List<Property<?>> properties = MapInputValidator.this.properties;
			final List<@Nullable Object> values = new ArrayList<>(properties.size());
			final List<ConstraintViolation> violations = new ArrayList<>();
			for (int i = 0; i < properties.size(); i++) {
				final String name = properties.get(i).name();
				final N value = this.reader.property(input, name);
				final boolean present = value != null || this.reader.hasProperty(input, name);
				final Validated<?> validated = this.validators.get(i)
					.validate(value == null || this.reader.isNull(value) ? null : value, locale, constraintContext);
				if (validated.isValid()) {
					values.add(validated.value());
				}
				else if (present) {
					violations.addAll(validated.errors());
				}
				else {
					violations.add(
							violation(name, ViolationMessage.Default.OBJECT_REQUIRED, new ViolatedValue(null), locale));
				}
			}
			if (!MapInputValidator.this.ignoreUnknownProperties) {
				for (Map.Entry<String, ? extends @Nullable N> property : this.reader.properties(input)) {
					if (!MapInputValidator.this.names.contains(property.getKey())) {
						final N value = property.getValue();
						violations.add(violation(property.getKey(), ViolationMessage.Default.OBJECT_UNKNOWN_PROPERTY,
								new ViolatedValue(
										value == null || this.reader.isNull(value) ? null : this.reader.value(value)),
								locale));
					}
				}
			}
			return violations.isEmpty()
					? Validated.successWith(MapInputValidator.this.mapper.apply(Collections.unmodifiableList(values)))
					: Validated.failureWith(violations);
		}

		@Override
		public ValueDescription description() {
			return MapInputValidator.this.description();
		}

		@Override
		public <M> ValueValidator<M, X> untyped(InputReader<M> reader) {
			return MapInputValidator.this.untyped(reader);
		}

	}

	/**
	 * Returns a violation located at the given name, which is one path segment whatever
	 * characters it holds.
	 */
	static ConstraintViolation violation(String name, ViolationMessage message, ViolatedValue violatedValue,
			Locale locale, Object... args) {
		return ConstraintViolation.builder()
			.name(name)
			.messageKey(message.messageKey())
			.defaultMessageFormat(message.defaultMessageFormat())
			.argsWithPrependedNameAndAppendedViolatedValue(args, violatedValue)
			.path(PathSegment.of(name))
			.locale(locale)
			.build();
	}

}
