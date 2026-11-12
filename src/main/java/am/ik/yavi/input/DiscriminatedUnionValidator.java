/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintDescription;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolatedValue;
import am.ik.yavi.core.ViolationMessage;
import org.jspecify.annotations.Nullable;

/**
 * Validates a {@code Map<String, Object>} with the validator selected by the value of one
 * of its keys, the discriminator. This is the way to read polymorphic input, for example
 * the subtypes of a sealed interface told apart by a {@code type} key:
 *
 * <pre>{@code
 * ValueValidator<Map<String, Object>, Shape> shapeValidator = DiscriminatedUnionValidator
 *     .<Shape>of("type")
 *     .when("circle", circleValidator)
 *     .when("square", squareValidator);
 * }</pre>
 *
 * <p>
 * Only the selected validator is applied, so the violations are those of the alternative
 * the input claims to be; use {@link ValueValidator#anyOf(ValueValidator[])} to try
 * alternatives that have no discriminator. A discriminator that is absent is reported
 * with the message key {@code object.required}, and one that is not the value of an
 * alternative, including {@code null} and a value that is not a string, with
 * {@code object.oneOf} and the values of the alternatives.
 *
 * <p>
 * An alternative does not have to define the discriminator: the key is removed from the
 * map given to an alternative that describes its properties without it, so that it is not
 * an unknown property there. An alternative that defines it receives it.
 *
 * <p>
 * A {@code null} map is rejected with {@code object.notNull}. The validator describes
 * itself as {@link ValueDescription.AnyOf} whose alternatives hold the discriminator as a
 * property equal to their value.
 *
 * @param <X> the common type of the objects created by the alternatives
 * @since 0.18.0
 * @see MapInputValidator
 */
public final class DiscriminatedUnionValidator<X> implements ValueValidator<Map<String, Object>, X> {

	private final String discriminator;

	private final Map<String, ValueValidator<? super Map<String, Object>, ? extends X>> alternatives;

	/**
	 * Returns a validator without alternatives that selects by the given key. Add the
	 * alternatives with {@link #when(String, ValueValidator)}.
	 * @param discriminator the key whose value selects the alternative
	 * @param <X> the common type of the objects created by the alternatives
	 * @return a validator that rejects every input until an alternative is added
	 */
	public static <X> DiscriminatedUnionValidator<X> of(String discriminator) {
		return new DiscriminatedUnionValidator<>(discriminator, Map.of());
	}

	private DiscriminatedUnionValidator(String discriminator,
			Map<String, ValueValidator<? super Map<String, Object>, ? extends X>> alternatives) {
		this.discriminator = discriminator;
		this.alternatives = alternatives;
	}

	/**
	 * Returns a validator that additionally validates a map whose discriminator is the
	 * given value with the given validator.
	 * @param value the value of the discriminator that selects the alternative
	 * @param validator the validator of the alternative
	 * @return a validator with the additional alternative
	 * @throws IllegalArgumentException if an alternative is already defined for the value
	 */
	public DiscriminatedUnionValidator<X> when(String value,
			ValueValidator<? super Map<String, Object>, ? extends X> validator) {
		final Map<String, ValueValidator<? super Map<String, Object>, ? extends X>> alternatives = new LinkedHashMap<>(
				this.alternatives);
		if (alternatives.put(value, validator) != null) {
			throw new IllegalArgumentException("The alternative '" + value + "' is defined twice.");
		}
		return new DiscriminatedUnionValidator<>(this.discriminator, Collections.unmodifiableMap(alternatives));
	}

	@Override
	public Validated<X> validate(@Nullable Map<String, Object> input, Locale locale,
			ConstraintContext constraintContext) {
		if (input == null) {
			return Validated.failureWith(MapInputValidator.violation("", ViolationMessage.Default.OBJECT_NOT_NULL,
					new ViolatedValue(null), locale));
		}
		if (!input.containsKey(this.discriminator)) {
			return Validated.failureWith(MapInputValidator.violation(this.discriminator,
					ViolationMessage.Default.OBJECT_REQUIRED, new ViolatedValue(null), locale));
		}
		final Object value = input.get(this.discriminator);
		final ValueValidator<? super Map<String, Object>, ? extends X> alternative = value instanceof String
				? this.alternatives.get(value) : null;
		if (alternative == null) {
			return Validated
				.failureWith(MapInputValidator.violation(this.discriminator, ViolationMessage.Default.OBJECT_ONE_OF,
						new ViolatedValue(value), locale, List.copyOf(this.alternatives.keySet())));
		}
		Map<String, Object> properties = input;
		if (alternative.description() instanceof ValueDescription.Properties described
				&& !defines(described, this.discriminator)) {
			properties = new LinkedHashMap<>(input);
			properties.remove(this.discriminator);
		}
		return Validated.of(alternative.validate(properties, locale, constraintContext));
	}

	@Override
	public ValueDescription description() {
		final List<ValueDescription> descriptions = new ArrayList<>();
		this.alternatives.forEach((value, alternative) -> {
			final ValueDescription description = alternative.description();
			if (description instanceof ValueDescription.Properties described
					&& !defines(described, this.discriminator)) {
				final List<ValueDescription.Property> properties = new ArrayList<>();
				properties.add(new ValueDescription.Property(this.discriminator,
						new ValueDescription.Value(this.discriminator, String.class,
								List.of(new ConstraintDescription(ViolationMessage.Default.OBJECT_EQUAL_TO.messageKey(),
										List.of(value), true)))));
				properties.addAll(described.properties());
				descriptions.add(new ValueDescription.Properties(properties));
			}
			else {
				descriptions.add(description);
			}
		});
		return new ValueDescription.AnyOf(descriptions);
	}

	/**
	 * Returns a validator that accepts an untyped input and rejects anything but a map
	 * whose keys are strings with {@code object.type}.
	 * @return a value validator for an untyped input
	 */
	@Override
	public ValueValidator<Object, X> untyped() {
		return this.untyped(MapInputValidator.mapType());
	}

	private static boolean defines(ValueDescription.Properties properties, String name) {
		return properties.properties().stream().anyMatch(property -> property.name().equals(name));
	}

}
