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
import java.util.Objects;
import java.util.stream.StreamSupport;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintDescription;
import am.ik.yavi.core.InputReader;
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
 * An alternative does not have to define the discriminator: the key is hidden from an
 * alternative that describes its properties without it, so that it is not an unknown
 * property there even if the alternative rejects unknown properties. An alternative that
 * defines it receives it.
 *
 * <p>
 * A {@code null} map is rejected with {@code object.notNull} and a map with a key that is
 * not a string with {@code object.type}. The validator describes itself as
 * {@link ValueDescription.AnyOf} whose alternatives hold the discriminator as a property
 * equal to their value.
 *
 * <p>
 * The tree model of a JSON library is read with {@link #readingWith(InputReader)}, like a
 * {@link MapInputValidator}.
 *
 * @param <X> the common type of the objects created by the alternatives
 * @since 0.18.0
 * @see MapInputValidator
 */
public final class DiscriminatedUnionValidator<X> implements ValueValidator<Map<String, Object>, X> {

	private final String discriminator;

	private final Map<String, ValueValidator<? super Map<String, Object>, ? extends X>> alternatives;

	private final Reading<Object> reading;

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
		final Map<String, ValueValidator<Object, ? extends X>> ofMaps = new LinkedHashMap<>();
		alternatives.forEach((value, alternative) -> ofMaps.put(value, this.ofMap(alternative)));
		this.reading = new Reading<>(InputReader.ofJdkTypes(), ofMaps);
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
		return this.reading.validate(input, locale, constraintContext);
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
	 * Returns a validator that selects the alternative of an object of the given reader
	 * instead of a map, and rejects any other node with {@code object.type}. The reader
	 * is applied to the alternatives too.
	 * @param reader the reader of the nodes
	 * @param <N> the type of the nodes
	 * @return a value validator for an untyped input
	 * @throws UnsupportedOperationException if the reader is not the one of JDK types and
	 * an alternative does not know its input type
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
		if (reader == InputReader.ofJdkTypes()) {
			return (ValueValidator<N, X>) this.reading;
		}
		final Map<String, ValueValidator<N, ? extends X>> alternatives = new LinkedHashMap<>();
		this.alternatives.forEach((value, alternative) -> alternatives.put(value, this.of(reader, alternative)));
		return new Reading<>(reader, alternatives);
	}

	/**
	 * Returns the alternative as a validator of a checked map. It is given a copy of the
	 * map without the discriminator if it does not define it.
	 */
	@SuppressWarnings("unchecked")
	private ValueValidator<Object, ? extends X> ofMap(
			ValueValidator<? super Map<String, Object>, ? extends X> alternative) {
		final boolean hidden = this.isHiddenFrom(alternative);
		return (input, locale, constraintContext) -> {
			Map<String, Object> properties = (Map<String, Object>) input;
			if (hidden && properties != null) {
				properties = new LinkedHashMap<>(properties);
				properties.remove(this.discriminator);
			}
			return Validated.of(alternative.validate(properties, locale, constraintContext));
		};
	}

	/**
	 * Returns the alternative as a validator of an object of the given reader. If it does
	 * not define the discriminator, it reads the object with a reader that hides it.
	 */
	private <N> ValueValidator<N, ? extends X> of(InputReader<N> reader,
			ValueValidator<? super Map<String, Object>, ? extends X> alternative) {
		if (!this.isHiddenFrom(alternative)) {
			return alternative.readingWith(reader);
		}
		final ValueValidator<Object, ? extends X> hiding = alternative
			.readingWith(new HidingReader<>(reader, this.discriminator));
		return (input, locale, constraintContext) -> Validated
			.of(hiding.validate(new Hidden<>(Objects.requireNonNull(input)), locale, constraintContext));
	}

	private boolean isHiddenFrom(ValueValidator<?, ?> alternative) {
		return alternative.description() instanceof ValueDescription.Properties described
				&& !defines(described, this.discriminator);
	}

	/**
	 * Selects the alternative of an object of a reader.
	 */
	private final class Reading<N> implements ValueValidator<N, X> {

		private final InputReader<N> reader;

		private final Map<String, ValueValidator<N, ? extends X>> alternatives;

		private Reading(InputReader<N> reader, Map<String, ValueValidator<N, ? extends X>> alternatives) {
			this.reader = reader;
			this.alternatives = alternatives;
		}

		@Override
		public Validated<X> validate(@Nullable N input, Locale locale, ConstraintContext constraintContext) {
			final String discriminator = DiscriminatedUnionValidator.this.discriminator;
			if (input == null || this.reader.isNull(input)) {
				return Validated.failureWith(MapInputValidator.violation("", ViolationMessage.Default.OBJECT_NOT_NULL,
						new ViolatedValue(null), locale));
			}
			if (!this.reader.isObject(input)) {
				return Validated.failureWith(MapInputValidator.violation("", ViolationMessage.Default.OBJECT_TYPE,
						new ViolatedValue(this.reader.value(input)), locale, "object"));
			}
			final N node = this.reader.property(input, discriminator);
			if (node == null && !this.reader.hasProperty(input, discriminator)) {
				return Validated.failureWith(MapInputValidator.violation(discriminator,
						ViolationMessage.Default.OBJECT_REQUIRED, new ViolatedValue(null), locale));
			}
			final Object value = node == null || this.reader.isNull(node) ? null : this.reader.value(node);
			final ValueValidator<N, ? extends X> alternative = value instanceof String ? this.alternatives.get(value)
					: null;
			if (alternative == null) {
				return Validated
					.failureWith(MapInputValidator.violation(discriminator, ViolationMessage.Default.OBJECT_ONE_OF,
							new ViolatedValue(value), locale, List.copyOf(this.alternatives.keySet())));
			}
			return Validated.of(alternative.validate(input, locale, constraintContext));
		}

		@Override
		public ValueDescription description() {
			return DiscriminatedUnionValidator.this.description();
		}

		@Override
		public <M> ValueValidator<M, X> readingWith(InputReader<M> reader) {
			return DiscriminatedUnionValidator.this.readingWith(reader);
		}

	}

	/**
	 * An object given to an alternative that does not see one of its properties.
	 */
	private record Hidden<N> (N object) {
	}

	/**
	 * Reads a {@link Hidden} object without the hidden property and any other node with
	 * the given reader.
	 */
	private record HidingReader<N> (InputReader<N> reader, String hidden) implements InputReader<Object> {

		@SuppressWarnings("unchecked")
		private N node(Object node) {
			return (N) (node instanceof Hidden<?>object ? object.object() : node);
		}

		@Override
		public boolean isNull(Object node) {
			return this.reader.isNull(this.node(node));
		}

		@Override
		public Object value(Object node) {
			return this.reader.value(this.node(node));
		}

		@Override
		public boolean isObject(Object node) {
			return this.reader.isObject(this.node(node));
		}

		@Override
		public @Nullable Object property(Object object, String name) {
			return this.hides(object, name) ? null : this.reader.property(this.node(object), name);
		}

		@Override
		public boolean hasProperty(Object object, String name) {
			return !this.hides(object, name) && this.reader.hasProperty(this.node(object), name);
		}

		@Override
		public Iterable<? extends Map.Entry<String, ? extends @Nullable Object>> properties(Object object) {
			final Iterable<? extends Map.Entry<String, ? extends @Nullable N>> properties = this.reader
				.properties(this.node(object));
			return object instanceof Hidden ? StreamSupport.stream(properties.spliterator(), false)
				.filter(property -> !this.hidden.equals(property.getKey()))
				.toList() : properties;
		}

		@Override
		public boolean isArray(Object node) {
			return this.reader.isArray(this.node(node));
		}

		@Override
		public Iterable<? extends @Nullable Object> elements(Object array) {
			return this.reader.elements(this.node(array));
		}

		private boolean hides(Object object, String name) {
			return object instanceof Hidden && this.hidden.equals(name);
		}

	}

	private static boolean defines(ValueDescription.Properties properties, String name) {
		return properties.properties().stream().anyMatch(property -> property.name().equals(name));
	}

}
