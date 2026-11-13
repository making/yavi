/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintDescription;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.StringCoercion;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueDescription;
import am.ik.yavi.core.ValueValidator;
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
 * equal to their value. An alternative that is a
 * {@linkplain ValueValidator#lazy(java.util.function.Supplier) lazy validator} is
 * described by a reference to such an alternative, and is asked whether it defines the
 * discriminator when it is first used.
 *
 * <p>
 * The tree model of a JSON library is read with {@link #readingWith(InputReader)}, like a
 * {@link ObjectInputValidator}.
 *
 * @param <X> the common type of the objects created by the alternatives
 * @since 0.18.0
 * @see ObjectInputValidator
 */
public final class DiscriminatedUnionValidator<X> implements ValueValidator<Map<String, Object>, X> {

	private final String discriminator;

	private final Map<String, ValueValidator<? super Map<String, Object>, ? extends X>> alternatives;

	/**
	 * The descriptions of the alternatives that are lazy validators, by their value: a
	 * reference to the description of the alternative with the discriminator. It is one
	 * reference per alternative, by which a recursive description is recognized.
	 */
	private final Map<String, ValueDescription.Reference> references;

	private final List<TargetRule<X>> rules;

	private final Reading<Object> reading;

	/**
	 * Returns a validator without alternatives that selects by the given key. Add the
	 * alternatives with {@link #when(String, ValueValidator)}.
	 * @param discriminator the key whose value selects the alternative
	 * @param <X> the common type of the objects created by the alternatives
	 * @return a validator that rejects every input until an alternative is added
	 */
	public static <X> DiscriminatedUnionValidator<X> of(String discriminator) {
		return new DiscriminatedUnionValidator<>(discriminator, Map.of(), List.of());
	}

	private DiscriminatedUnionValidator(String discriminator,
			Map<String, ValueValidator<? super Map<String, Object>, ? extends X>> alternatives,
			List<TargetRule<X>> rules) {
		this.discriminator = discriminator;
		this.rules = rules;
		this.alternatives = alternatives;
		final Map<String, ValueDescription.Reference> references = new HashMap<>();
		final Map<String, ValueValidator<Object, ? extends X>> ofMaps = new LinkedHashMap<>();
		alternatives.forEach((value, alternative) -> {
			if (alternative.description() instanceof ValueDescription.Reference reference) {
				references.put(value, new ValueDescription.Reference(reference.name(),
						() -> this.describe(value, target(reference))));
			}
			ofMaps.put(value, this.ofMap(alternative));
		});
		this.references = references;
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
		return new DiscriminatedUnionValidator<>(this.discriminator, Collections.unmodifiableMap(alternatives),
				this.rules);
	}

	/**
	 * Returns a validator that additionally checks the object created by the selected
	 * alternative with the given predicate, as {@link ValueValidator#constraintOnTarget}
	 * states. The rule applies to every alternative, those added later included, and the
	 * returned validator can still be given alternatives and read parameters.
	 * @param predicate the rule that the created object must satisfy
	 * @param name the name of the reported violation
	 * @param violationMessage the message key and the default message format of the
	 * reported violation
	 * @return a validator with the additional rule
	 */
	@Override
	public DiscriminatedUnionValidator<X> constraintOnTarget(Predicate<? super X> predicate, String name,
			ViolationMessage violationMessage) {
		return new DiscriminatedUnionValidator<>(this.discriminator, this.alternatives,
				TargetRule.append(this.rules, new TargetRule<>(predicate, name, violationMessage)));
	}

	/**
	 * Returns a validator that selects and applies the same alternatives for a flat map
	 * of parameters, such as request parameters, as
	 * {@link ObjectInputValidator#readingParameters()} states.
	 * @param <M> the type of the map, whose values are strings, or iterables or arrays of
	 * strings
	 * @return a validator of a map of parameters
	 * @see ParameterTree
	 */
	public <M extends Map<String, ?>> ValueValidator<M, X> readingParameters() {
		return this.readingParameters(StringCoercion.strict());
	}

	/**
	 * Returns a validator like {@link #readingParameters()} that takes the text of a
	 * value by the given rules, as
	 * {@link ObjectInputValidator#readingParameters(StringCoercion)} states.
	 * @param coercion the rules by which a string is taken as text
	 * @param <M> the type of the map, whose values are strings, or iterables or arrays of
	 * strings
	 * @return a validator of a map of parameters
	 * @see InputReader#coercingStrings(StringCoercion)
	 */
	public <M extends Map<String, ?>> ValueValidator<M, X> readingParameters(StringCoercion coercion) {
		return ParameterTree.reading(this, coercion);
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
			final ValueDescription reference = this.references.get(value);
			descriptions.add(reference != null ? reference : this.describe(value, alternative.description()));
		});
		return new ValueDescription.AnyOf(descriptions);
	}

	/**
	 * Returns the description of the alternative of the given value: the given one, with
	 * the discriminator as a property equal to the value unless it is defined.
	 */
	private ValueDescription describe(String value, ValueDescription description) {
		if (!(description instanceof ValueDescription.Properties described) || defines(described, this.discriminator)) {
			return description;
		}
		final List<ValueDescription.Property> properties = new ArrayList<>();
		properties.add(new ValueDescription.Property(this.discriminator,
				new ValueDescription.Value(this.discriminator, String.class,
						List.of(new ConstraintDescription(ViolationMessage.Default.OBJECT_EQUAL_TO.messageKey(),
								List.of(value), true)))));
		properties.addAll(described.properties());
		return new ValueDescription.Properties(properties, described.rest());
	}

	/**
	 * Returns the description that the given one stands for: itself unless it is a
	 * reference.
	 */
	private static ValueDescription target(ValueDescription description) {
		ValueDescription target = description;
		while (target instanceof ValueDescription.Reference reference) {
			target = reference.target();
		}
		return target;
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
	 * map without the discriminator if it does not define it, which a lazy alternative
	 * tells on first use.
	 */
	private <Y extends X> ValueValidator<Object, Y> ofMap(ValueValidator<? super Map<String, Object>, Y> alternative) {
		return isLazy(alternative) ? ValueValidator.lazy(() -> this.ofMap(alternative, this.isHiddenFrom(alternative)))
				: this.ofMap(alternative, this.isHiddenFrom(alternative));
	}

	@SuppressWarnings("unchecked")
	private <Y extends X> ValueValidator<Object, Y> ofMap(ValueValidator<? super Map<String, Object>, Y> alternative,
			boolean hidden) {
		return (input, locale, constraintContext) -> {
			Map<String, Object> properties = (Map<String, Object>) input;
			if (hidden && properties != null) {
				properties = Projection.of(properties, this::isShown);
			}
			return alternative.validate(properties, locale, constraintContext);
		};
	}

	/**
	 * Returns the alternative as a validator of an object of the given reader. If it does
	 * not define the discriminator, which a lazy alternative tells on first use, it reads
	 * the object with a reader that hides it.
	 */
	private <N, Y extends X> ValueValidator<N, Y> of(InputReader<N> reader,
			ValueValidator<? super Map<String, Object>, Y> alternative) {
		return isLazy(alternative)
				? ValueValidator.lazy(() -> this.of(reader, alternative, this.isHiddenFrom(alternative)))
				: this.of(reader, alternative, this.isHiddenFrom(alternative));
	}

	private <N, Y extends X> ValueValidator<N, Y> of(InputReader<N> reader,
			ValueValidator<? super Map<String, Object>, Y> alternative, boolean hidden) {
		if (!hidden) {
			return alternative.readingWith(reader);
		}
		final ValueValidator<Object, Y> hiding = alternative.readingWith(new Projection.Reader<>(reader));
		return (input, locale, constraintContext) -> hiding
			.validate(new Projection<>(Objects.requireNonNull(input), this::isShown), locale, constraintContext);
	}

	private boolean isShown(String name) {
		return !this.discriminator.equals(name);
	}

	/**
	 * Returns whether the alternative refers to a validator that may not be built yet, so
	 * that its properties are known only when it is used.
	 */
	private static boolean isLazy(ValueValidator<?, ?> alternative) {
		return alternative.description() instanceof ValueDescription.Reference;
	}

	private boolean isHiddenFrom(ValueValidator<?, ?> alternative) {
		return target(alternative.description()) instanceof ValueDescription.Properties described
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
				return Validated.failureWith(
						ObjectInputValidator.violation("", ViolationMessage.Default.OBJECT_NOT_NULL, null, locale));
			}
			if (!this.reader.isObject(input)) {
				return Validated.failureWith(ObjectInputValidator.violation("", ViolationMessage.Default.OBJECT_TYPE,
						this.reader.value(input), locale, "object"));
			}
			final N node = this.reader.property(input, discriminator);
			if (node == null && !this.reader.hasProperty(input, discriminator)) {
				return Validated.failureWith(ObjectInputValidator.violation(discriminator,
						ViolationMessage.Default.OBJECT_REQUIRED, null, locale));
			}
			final Object value = node == null || this.reader.isNull(node) ? null : this.reader.value(node);
			final ValueValidator<N, ? extends X> alternative = value instanceof String ? this.alternatives.get(value)
					: null;
			if (alternative == null) {
				return Validated
					.failureWith(ObjectInputValidator.violation(discriminator, ViolationMessage.Default.OBJECT_ONE_OF,
							value, locale, List.copyOf(this.alternatives.keySet())));
			}
			return TargetRule.check(DiscriminatedUnionValidator.this.rules,
					Validated.of(alternative.validate(input, locale, constraintContext)), locale);
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

	private static boolean defines(ValueDescription.Properties properties, String name) {
		return properties.properties().stream().anyMatch(property -> property.name().equals(name));
	}

}
