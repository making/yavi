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
 * A {@code null} map is rejected with {@code object.notNull}. The validator describes
 * itself as {@link ValueDescription.Properties}.
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

	/**
	 * A property read from the input map.
	 *
	 * @param name the key of the property in the map
	 * @param validator the validator of the value, which reports its violations under
	 * {@code name}; an absent key is validated as {@code null}
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
		if (input == null) {
			return Validated
				.failureWith(violation("", ViolationMessage.Default.OBJECT_NOT_NULL, new ViolatedValue(null), locale));
		}
		final List<@Nullable Object> values = new ArrayList<>(this.properties.size());
		final List<ConstraintViolation> violations = new ArrayList<>();
		for (Property<?> property : this.properties) {
			final String name = property.name();
			final boolean present = input.containsKey(name);
			final Validated<?> validated = property.validator()
				.validate(present ? input.get(name) : null, locale, constraintContext);
			if (validated.isValid()) {
				values.add(validated.value());
			}
			else if (present) {
				violations.addAll(validated.errors());
			}
			else {
				violations
					.add(violation(name, ViolationMessage.Default.OBJECT_REQUIRED, new ViolatedValue(null), locale));
			}
		}
		if (!this.ignoreUnknownProperties) {
			// a map that was not checked may hold a key that is not a string
			for (Map.Entry<?, ?> entry : input.entrySet()) {
				if (!(entry.getKey() instanceof String key && this.names.contains(key))) {
					violations
						.add(violation(String.valueOf(entry.getKey()), ViolationMessage.Default.OBJECT_UNKNOWN_PROPERTY,
								new ViolatedValue(entry.getValue()), locale));
				}
			}
		}
		return violations.isEmpty() ? Validated.successWith(this.mapper.apply(Collections.unmodifiableList(values)))
				: Validated.failureWith(violations);
	}

	@Override
	public ValueDescription description() {
		return new ValueDescription.Properties(this.properties.stream()
			.map(property -> new ValueDescription.Property(property.name(), property.validator().description()))
			.toList());
	}

	/**
	 * Returns a validator that accepts an untyped input and rejects anything but a map
	 * whose keys are strings with {@code object.type}.
	 * @return a value validator for an untyped input
	 */
	@Override
	public ValueValidator<Object, X> untyped() {
		return this.untyped(mapType());
	}

	@SuppressWarnings("unchecked")
	static Class<Map<String, Object>> mapType() {
		return (Class<Map<String, Object>>) (Class<?>) Map.class;
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
