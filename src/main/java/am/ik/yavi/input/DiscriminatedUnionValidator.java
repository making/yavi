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
import am.ik.yavi.core.ReversibleValueValidator;
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
 * An alternative does not have to define the discriminator, and must not
 * {@linkplain am.ik.yavi.builder.Withholdable#withheld() withhold} it, which is an
 * {@link IllegalArgumentException} when the alternative is added or, for a lazy one, when
 * it is first described: the key is hidden from an alternative that describes its
 * properties without it, so that it is not an unknown property there even if the
 * alternative rejects unknown properties. An alternative that defines it receives it.
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
 * <p>
 * A union whose alternatives are all added with the type of their objects is a
 * {@link Reversible}: it also writes an object back as the map that it is read from, with
 * the discriminator of the alternative of its type.
 *
 * <pre>{@code
 * DiscriminatedUnionValidator.Reversible<Shape> shapeValidator = DiscriminatedUnionValidator
 *     .<Shape>of("type")
 *     .when("circle", Circle.class, circleValidator)
 *     .when("square", Square.class, squareValidator);
 * shapeValidator.toMap(new Circle(1.5)); // {type=circle, radius=1.5}
 * }</pre>
 *
 * @param <X> the common type of the objects created by the alternatives
 * @since 0.18.0
 * @see ObjectInputValidator
 */
public sealed class DiscriminatedUnionValidator<X>
		implements ValueValidator<Map<String, Object>, X>permits DiscriminatedUnionValidator.Reversible {

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
	 * Returns the discriminator of a union that selects by the given key. It is not a
	 * validator yet: the first alternative, added with
	 * {@link Discriminator#when(String, ValueValidator)}, makes it one.
	 * @param discriminator the key whose value selects the alternative
	 * @param <X> the common type of the objects created by the alternatives
	 * @return the discriminator, to which the first alternative is added
	 */
	public static <X> Discriminator<X> of(String discriminator) {
		return new Discriminator<>(discriminator);
	}

	/**
	 * The key that selects the alternative of a union to which no alternative has been
	 * added. A union without alternatives would reject every input, so it is not a
	 * validator: {@link #when(String, ValueValidator)} returns the validator.
	 *
	 * @param <X> the common type of the objects created by the alternatives
	 * @since 0.18.0
	 */
	public static final class Discriminator<X> {

		private final String key;

		private Discriminator(String key) {
			this.key = key;
		}

		/**
		 * Returns a validator that validates a map whose discriminator is the given value
		 * with the given validator. Add the other alternatives with
		 * {@link DiscriminatedUnionValidator#when(String, ValueValidator)}.
		 * @param value the value of the discriminator that selects the alternative
		 * @param validator the validator of the alternative
		 * @return a validator with the alternative
		 */
		public DiscriminatedUnionValidator<X> when(String value,
				ValueValidator<? super Map<String, Object>, ? extends X> validator) {
			return new DiscriminatedUnionValidator<>(this.key, Map.of(value, validator), List.of());
		}

		/**
		 * Returns a validator that validates a map whose discriminator is the given value
		 * with the given validator, and that writes an object of the given type back with
		 * it. Add the other alternatives with
		 * {@link Reversible#when(String, Class, ReversibleValueValidator)}.
		 * @param value the value of the discriminator that selects the alternative
		 * @param type the type of the objects that the alternative creates
		 * @param validator the validator of the alternative, which knows its reverse
		 * @param <Y> the type of the objects that the alternative creates
		 * @return a validator with the alternative, which also writes an object back
		 */
		public <Y extends X> Reversible<X> when(String value, Class<Y> type,
				ReversibleValueValidator<Map<String, Object>, Y> validator) {
			return new Reversible<>(this.key, Map.of(value, validator), List.of(),
					List.of(new Alternative<>(value, type, validator)));
		}

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
			else {
				this.describe(value, alternative.description());
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
		return new DiscriminatedUnionValidator<>(this.discriminator, this.with(value, validator), this.rules);
	}

	/**
	 * An alternative that is written back: the objects of its type are read from a map
	 * whose discriminator is its value.
	 */
	private record Alternative<Y> (String value, Class<Y> type,
			ReversibleValueValidator<Map<String, Object>, Y> validator) {

		private Map<String, Object> write(Object object) {
			final Map<String, Object> map = this.validator.reverse(this.type.cast(object));
			if (map == null) {
				throw new IllegalArgumentException(
						"The alternative '" + this.value + "' is not read from a map: it writes null.");
			}
			return map;
		}

	}

	/**
	 * A union that also writes an object back as the map that it reads the object from:
	 * every alternative is added with the type of the objects it creates.
	 *
	 * <p>
	 * {@link #toMap(Object)} writes an object with the first alternative, in the order
	 * they were added, whose type the object is an instance of, and adds the
	 * discriminator with the value of that alternative unless the alternative writes it
	 * itself. The discriminator is the first key then.
	 *
	 * <p>
	 * An alternative added without its type
	 * ({@link DiscriminatedUnionValidator#when(String, ValueValidator)}) returns a union
	 * that does not write.
	 *
	 * @param <X> the common type of the objects created by the alternatives
	 * @since 0.18.0
	 */
	public static final class Reversible<X> extends DiscriminatedUnionValidator<X>
			implements ReversibleValueValidator<Map<String, Object>, X> {

		private final List<Alternative<? extends X>> written;

		private Reversible(String discriminator,
				Map<String, ValueValidator<? super Map<String, Object>, ? extends X>> alternatives,
				List<TargetRule<X>> rules, List<Alternative<? extends X>> written) {
			super(discriminator, alternatives, rules);
			this.written = written;
		}

		/**
		 * Returns a validator that additionally validates a map whose discriminator is
		 * the given value with the given validator, and that writes an object of the
		 * given type back with it.
		 * @param value the value of the discriminator that selects the alternative
		 * @param type the type of the objects that the alternative creates
		 * @param validator the validator of the alternative, which knows its reverse
		 * @param <Y> the type of the objects that the alternative creates
		 * @return a validator with the additional alternative
		 * @throws IllegalArgumentException if an alternative is already defined for the
		 * value
		 */
		public <Y extends X> Reversible<X> when(String value, Class<Y> type,
				ReversibleValueValidator<Map<String, Object>, Y> validator) {
			final List<Alternative<? extends X>> written = new ArrayList<>(this.written);
			written.add(new Alternative<>(value, type, validator));
			return new Reversible<>(this.discriminator(), this.with(value, validator), this.rules(),
					Collections.unmodifiableList(written));
		}

		/**
		 * Returns the map that this validator reads the given object from: the map that
		 * the alternative of the type of the object writes, with the discriminator.
		 * @param object an object that an alternative creates
		 * @return a new map, which the caller may change
		 * @throws IllegalArgumentException if the object is not an instance of the type
		 * of an alternative
		 */
		public Map<String, Object> toMap(X object) {
			final String discriminator = this.discriminator();
			for (Alternative<? extends X> alternative : this.written) {
				if (alternative.type().isInstance(object)) {
					final Map<String, Object> map = alternative.write(object);
					if (map.containsKey(discriminator)) {
						return map;
					}
					final Map<String, Object> discriminated = new LinkedHashMap<>();
					discriminated.put(discriminator, alternative.value());
					discriminated.putAll(map);
					return discriminated;
				}
			}
			throw new IllegalArgumentException(
					"No alternative is defined for an object of type " + object.getClass().getName() + ".");
		}

		/**
		 * Returns the view of the properties that are not
		 * {@linkplain am.ik.yavi.builder.Withholdable#withheld() withheld}, as
		 * {@link ObjectInputValidator.Reversible#view()} states. A property is withheld
		 * if an alternative that defines it withholds it.
		 * @return a view of the properties that are not withheld, which only writes and
		 * can be made narrower
		 */
		public PropertyView<X> view() {
			return this.all().withoutWithheld();
		}

		/**
		 * Returns a view that writes only the given properties of an object, and the
		 * discriminator. A property is one that an alternative defines; an object of an
		 * alternative that does not define it is written without it.
		 * @param name the name of a property, or the path to a property of a nested
		 * object as {@link PropertyView} states
		 * @param names the names of the other properties
		 * @return a view of the given properties, which only writes
		 * @throws IllegalArgumentException if no alternative defines a property of a
		 * given name, or as {@link PropertyView#including(String, String...)} states
		 */
		public PropertyView<X> including(String name, String... names) {
			return this.all().including(name, names);
		}

		/**
		 * Returns a view that writes the properties of an object except the given ones. A
		 * property is one that an alternative defines. The discriminator cannot be
		 * excluded.
		 * @param name the name of a property, or the path to a property of a nested
		 * object as {@link PropertyView} states
		 * @param names the names of the other properties
		 * @return a view without the given properties, which only writes
		 * @throws IllegalArgumentException if no alternative defines a property of a
		 * given name, or as {@link PropertyView#excluding(String, String...)} states
		 */
		public PropertyView<X> excluding(String name, String... names) {
			return this.all().excluding(name, names);
		}

		private PropertyView<X> all() {
			return PropertyView.of((object, names) -> this.toMap(object), this.description());
		}

		/**
		 * Returns the map that this validator reads the given object from. This is
		 * {@link #toMap(Object)}.
		 * @param object an object that an alternative creates
		 * @return a new map
		 */
		@Override
		public Map<String, Object> reverse(X object) {
			return this.toMap(object);
		}

		/**
		 * Returns a validator that additionally checks the object created by the selected
		 * alternative with the given predicate, as
		 * {@link DiscriminatedUnionValidator#constraintOnTarget} states. The returned
		 * validator still writes an object back.
		 * @param predicate the rule that the created object must satisfy
		 * @param name the name of the reported violation
		 * @param violationMessage the message key and the default message format of the
		 * reported violation
		 * @return a validator with the additional rule
		 */
		@Override
		public Reversible<X> constraintOnTarget(Predicate<? super X> predicate, String name,
				ViolationMessage violationMessage) {
			return new Reversible<>(this.discriminator(), this.alternatives(),
					TargetRule.append(this.rules(), new TargetRule<>(predicate, name, violationMessage)), this.written);
		}

	}

	final String discriminator() {
		return this.discriminator;
	}

	final Map<String, ValueValidator<? super Map<String, Object>, ? extends X>> alternatives() {
		return this.alternatives;
	}

	final List<TargetRule<X>> rules() {
		return this.rules;
	}

	/**
	 * Returns the alternatives with the given one.
	 * @throws IllegalArgumentException if an alternative is already defined for the value
	 */
	final Map<String, ValueValidator<? super Map<String, Object>, ? extends X>> with(String value,
			ValueValidator<? super Map<String, Object>, ? extends X> validator) {
		final Map<String, ValueValidator<? super Map<String, Object>, ? extends X>> alternatives = new LinkedHashMap<>(
				this.alternatives);
		if (alternatives.put(value, validator) != null) {
			throw new IllegalArgumentException("The alternative '" + value + "' is defined twice.");
		}
		return Collections.unmodifiableMap(alternatives);
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
	 * @return a validator of a map of parameters, whose values are strings, or iterables
	 * or arrays of strings
	 * @see ParameterTree
	 */
	public ValueValidator<Map<String, ?>, X> readingParameters() {
		return this.readingParameters(StringCoercion.strict());
	}

	/**
	 * Returns a validator like {@link #readingParameters()} that takes the text of a
	 * value by the given rules, as
	 * {@link ObjectInputValidator#readingParameters(StringCoercion)} states.
	 * @param coercion the rules by which a string is taken as text
	 * @return a validator of a map of parameters, whose values are strings, or iterables
	 * or arrays of strings
	 * @see am.ik.yavi.core.CoercibleInputReader#coercingStrings(StringCoercion)
	 */
	public ValueValidator<Map<String, ?>, X> readingParameters(StringCoercion coercion) {
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
		return new ValueDescription.AnyOf(descriptions, this.discriminator);
	}

	/**
	 * Returns the description of the alternative of the given value: the given one, with
	 * the discriminator as a property equal to the value unless it is defined.
	 * @throws IllegalArgumentException if the alternative withholds the discriminator
	 */
	private ValueDescription describe(String value, ValueDescription description) {
		if (!(description instanceof ValueDescription.Properties described)) {
			return description;
		}
		for (ValueDescription.Property property : described.properties()) {
			if (property.name().equals(this.discriminator)) {
				if (property.withheld()) {
					throw new IllegalArgumentException(
							"The alternative '" + value + "' withholds the discriminator '" + this.discriminator
									+ "', which tells the alternative of a written object and is always written.");
				}
				return description;
			}
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
			// the text of the discriminator as the reader takes a string, which may trim
			// it
			final ValueValidator<N, ? extends X> alternative = node != null && value instanceof String
					&& this.reader.value(node, String.class) instanceof String text ? this.alternatives.get(text)
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
