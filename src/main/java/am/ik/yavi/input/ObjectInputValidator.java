/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.InputReader;
import am.ik.yavi.core.Presence;
import am.ik.yavi.core.ReversibleValueValidator;
import am.ik.yavi.core.StringCoercion;
import am.ik.yavi.core.TargetConstraints;
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
 * created with {@code fromMap} of the builders returned by {@code Yavi.schema()}:
 *
 * <pre>{@code
 * ObjectInputValidator<User> validator = Yavi.schema()
 *     ._string("email", c -> c.notBlank().email())
  *     ._integer("age", c -> c.greaterThanOrEqual(0))
 *     ._string("nickname").optional()
 *     .fromMap(User::new);
 * Validated<User> validated = validator.validate(map);
 * }</pre>
 *
 * <ul>
 * <li>A property is required unless {@code optional()}, {@code nullable()} or
 * {@code defaultValue} is applied to it. A key that is absent is validated like
 * {@code null}, so such a property may be omitted. The absent key of a required property
 * is reported with the message key {@code object.required} instead of the violations for
 * {@code null}, and an explicit {@code null} with the violation of a constraint that
 * rejects it, or else {@code object.notNull}.</li>
 * <li>A property added with {@code presence()} of the builders tells an absent key from
 * {@code null}, which a partial update such as a PATCH request needs: an absent key is
 * {@link Presence.Absent} and is not validated, and a key that is present is validated,
 * {@code null} included, so the property decides whether it can be cleared.</li>
 * <li>A property that accepts {@code null} tells an absent key from {@code null} without
 * a {@link Presence} too, as its {@link Strictness} states: with {@code required()} of
 * the builders the key must be present although its value may be {@code null}, and with
 * {@code rejectingNull()} the key may be absent although its value must not be
 * {@code null}.</li>
 * <li>A value that does not represent the type of the property is reported with
 * {@code object.type}. Nothing is coerced unless the map is read with
 * {@link am.ik.yavi.core.CoercibleInputReader#coercingStrings()}.</li>
 * <li>The violations that report a missing value or a value of another type come from no
 * constraint. {@code label}, {@code required} and {@code typeMessage} of the builders say
 * how they read for one property, as {@link PropertyMessages} states.</li>
 * <li>A property added with {@code withheld()} of the builders is read as any other and
 * is held back from what shows the values of the object again, as
 * {@link am.ik.yavi.builder.Withholdable#withheld()} states: a password, a token.</li>
 * <li>A key that no property is defined for is ignored. With
 * {@link IgnoringUnknown#rejectingUnknownProperties()} it is reported with
 * {@code object.unknownProperty}, and with {@code _rest} of the builders it is validated
 * and given to the created object, as {@link Rest} states.</li>
 * </ul>
 *
 * <p>
 * A validator may be embedded in another with {@code _embedded} of the builders: it reads
 * the same map as the properties instead of the value of a key, which builds an object
 * from the columns of several tables in the row of a JOIN, or from more keys than a
 * builder has arguments.
 *
 * <pre>{@code
 * ObjectInputValidator<Employee> validator = Yavi.schema()
 *     ._integer("id")
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
 * ValueValidator<Map<String, ?>, User> formValidator = validator.readingParameters();
 * Validated<User> validated = formValidator.validate(requestParameters);
 * }</pre>
 *
 * <p>
 * A validator built with {@code fromMapReversible} of the builders is a
 * {@link Reversible}: it also writes an object back as the map that it reads the object
 * from, for example as the named parameters of a statement.
 *
 * <pre>{@code
 * ObjectInputValidator.Reversible<User> validator = Yavi.reversibleSchema()
 *     ._string("email", c -> c.notBlank().email())
 *     ._integer("age", c -> c.greaterThanOrEqual(0))
 *     ._string("nickname").optional()
 *     .fromMapReversible(User::new, User::email, User::age, User::nickname);
 * Map<String, Object> map = validator.toMap(user); // {email=..., age=30, nickname=null}
 * }</pre>
 *
 * @param <X> the type of the created object
 * @since 0.18.0
 * @see DiscriminatedUnionValidator
 * @see am.ik.yavi.arguments.ObjectValidator the validator of a typed object, which is not
 * read from an input
 */
public sealed class ObjectInputValidator<X extends @Nullable Object> implements
		ValueValidator<Map<String, Object>, X>permits ObjectInputValidator.IgnoringUnknown,ObjectInputValidator.Reversible {

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

	private final TargetConstraints<X> rules;

	private final ValueDescription description;

	private final Reading<Object> reading;

	/**
	 * A part of the object created by a {@link ObjectInputValidator}: the validated value
	 * of one property, of an embedded validator or of the rest of the properties.
	 *
	 * @param <R> the type of the validated value
	 */
	public sealed interface Part<R extends @Nullable Object> {

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
	 * {@code name}. To read the property with another {@link InputReader}, the validator
	 * must know its input type (see {@link ValueValidator#readingWith(InputReader)})
	 * @param strictness whether an absent key and an explicit {@code null} are validated
	 * as {@code null} or rejected
	 * @param messages what the property is called in a message, and what is said when it
	 * has no value or a value of another type
	 * @param withheld whether the property is described as
	 * {@linkplain ValueDescription.Property#withheld() withheld}: it is read as any
	 * other, and is held back from what shows the values of the object again
	 * @param <R> the type of the validated value
	 */
	public record Property<R extends @Nullable Object> (String name, ValueValidator<Object, R> validator,
			Strictness strictness, PropertyMessages messages, boolean withheld) implements Part<R> {

		/**
		 * Creates a property whose absent key is validated as {@code null}.
		 * @param name the key of the property in the map
		 * @param validator the validator of the value
		 */
		public Property(String name, ValueValidator<Object, R> validator) {
			this(name, validator, Strictness.ABSENT_AS_NULL);
		}

		/**
		 * Creates a property that is called by its name and reports the default messages.
		 * @param name the key of the property in the map
		 * @param validator the validator of the value
		 * @param strictness whether an absent key and an explicit {@code null} are
		 * validated as {@code null} or rejected
		 */
		public Property(String name, ValueValidator<Object, R> validator, Strictness strictness) {
			this(name, validator, strictness, PropertyMessages.none());
		}

		/**
		 * Creates a property that is not withheld.
		 * @param name the key of the property in the map
		 * @param validator the validator of the value
		 * @param strictness whether an absent key and an explicit {@code null} are
		 * validated as {@code null} or rejected
		 * @param messages what the property is called in a message, and what is said when
		 * it has no value or a value of another type
		 */
		public Property(String name, ValueValidator<Object, R> validator, Strictness strictness,
				PropertyMessages messages) {
			this(name, validator, strictness, messages, false);
		}
	}

	/**
	 * Whether a {@link Property} tells an absent key from an explicit {@code null}. It
	 * matters only to a property whose validator accepts {@code null}; any other property
	 * rejects both, an absent key with {@code object.required}.
	 */
	public enum Strictness {

		/**
		 * An absent key is validated as {@code null}, so a validator that accepts
		 * {@code null} accepts both.
		 */
		ABSENT_AS_NULL,

		/**
		 * An absent key is rejected with {@code object.required} without being validated,
		 * whatever the validator accepts. An explicit {@code null} is validated as
		 * {@code null}.
		 */
		REQUIRED,

		/**
		 * An explicit {@code null} is rejected with {@code object.notNull} without being
		 * validated, whatever the validator accepts. An absent key is validated as
		 * {@code null}.
		 */
		REJECTING_NULL;

		/**
		 * Returns the description of the value of a property of this strictness.
		 * @param description the description of the input of the validator of the
		 * property
		 * @return the given description for {@link #ABSENT_AS_NULL}, a
		 * {@link ValueDescription.Required} of it for {@link #REQUIRED}, and for
		 * {@link #REJECTING_NULL} a {@link ValueDescription.Presence} of a value that
		 * rejects {@code null} if the given description accepts {@code null}
		 */
		public ValueDescription describe(ValueDescription description) {
			return switch (this) {
				case ABSENT_AS_NULL -> description;
				case REQUIRED -> description instanceof ValueDescription.Required ? description
						: new ValueDescription.Required(description);
				case REJECTING_NULL -> switch (description) {
					case ValueDescription.Presence presence when !presence.acceptsNull() -> presence;
					case ValueDescription.Optional optional ->
						new ValueDescription.Presence(notNull(optional.value()), optional.defaultValue());
					default -> description.acceptsNull() ? new ValueDescription.Presence(notNull(description))
							: description;
				};
			};
		}

		private static ValueDescription notNull(ValueDescription description) {
			return description.acceptsNull() ? new ValueDescription.NotNull(description) : description;
		}

	}

	/**
	 * A property read from the input map whose absence is told from {@code null}. An
	 * absent key is {@link Presence.Absent} and no violation; the property is not
	 * required whatever its validator accepts.
	 *
	 * @param name the key of the property in the map
	 * @param validator the validator of the value of a key that is present, which reports
	 * its violations under {@code name}; an explicit {@code null} is validated as
	 * {@code null}. It is {@link ValueValidator#liftPresence()} of a validator that is
	 * read with a reader, or a validator of an untyped value mapped with
	 * {@link Presence#ofNullable(Object)}
	 * @param messages what the property is called in a message, and what is said when it
	 * has no value or a value of another type
	 * @param withheld whether the property is described as
	 * {@linkplain ValueDescription.Property#withheld() withheld}
	 * @param <R> the type of the validated value
	 */
	public record PresenceProperty<R> (String name, ValueValidator<Object, Presence<R>> validator,
			PropertyMessages messages, boolean withheld) implements Part<Presence<R>> {

		/**
		 * Creates a property that is called by its name and reports the default messages.
		 * @param name the key of the property in the map
		 * @param validator the validator of the value of a key that is present
		 */
		public PresenceProperty(String name, ValueValidator<Object, Presence<R>> validator) {
			this(name, validator, PropertyMessages.none());
		}

		/**
		 * Creates a property that is not withheld.
		 * @param name the key of the property in the map
		 * @param validator the validator of the value of a key that is present
		 * @param messages what the property is called in a message, and what is said when
		 * it has no value or a value of another type
		 */
		public PresenceProperty(String name, ValueValidator<Object, Presence<R>> validator, PropertyMessages messages) {
			this(name, validator, messages, false);
		}
	}

	/**
	 * A validator that reads the input map itself, next to the other parts, such as the
	 * columns of one table in the row of a JOIN.
	 *
	 * @param validator the validator of the map, which reports its violations as they
	 * are. What it {@linkplain ValueValidator#description() describes} decides how it is
	 * embedded, as {@link ObjectInputValidator} states
	 * @param <R> the type of the validated value
	 */
	public record Embedded<R extends @Nullable Object> (ValueValidator<Object, R> validator) implements Part<R> {
	}

	/**
	 * The properties of the input map that no other part defines.
	 *
	 * @param validator the validator of a map of those properties, which is empty if
	 * there is none, such as {@link ValueValidator#liftMap()} of a validator of their
	 * values
	 * @param <R> the type of the validated value
	 */
	public record Rest<R extends @Nullable Object> (ValueValidator<Object, R> validator) implements Part<R> {
	}

	/**
	 * Returns a validator that reads the given parts from a map and creates an object
	 * from their validated values. This is the way to define more properties than the
	 * builders support. Properties that are known only at run time are defined with
	 * {@code Yavi.dynamicSchema()}, which creates such a validator of a map of the
	 * validated values.
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
	public static <X extends @Nullable Object> IgnoringUnknown<X> of(List<? extends Part<?>> parts,
			Function<? super List<@Nullable Object>, ? extends X> mapper) {
		return new IgnoringUnknown<>(List.copyOf(parts), values -> Validated.successWith(mapper.apply(values)),
				TargetConstraints.empty());
	}

	/**
	 * Creates a validator from the given parts and a function that validates their values
	 * together. This is {@link #of} for a function that can fail: it returns the result
	 * or the violations of a rule across the parts.
	 *
	 * <p>
	 * The function is applied only when all parts are valid. A violation it reports is
	 * located relative to the map: one with an empty name is reported on the map itself
	 * and takes the name of the property the validator is applied to. The function is not
	 * given the locale of the validation, so its violations are reported with that
	 * locale, whichever locale they were created with.
	 * @param parts the parts in the order of the values
	 * @param mapper function that creates the result from the validated values, in the
	 * order of the parts, or reports violations
	 * @param <X> the type of the result
	 * @return a new validator
	 * @throws IllegalArgumentException as {@link #of} does
	 * @since 0.18.0
	 */
	public static <X extends @Nullable Object> IgnoringUnknown<X> flatOf(List<? extends Part<?>> parts,
			Function<? super List<@Nullable Object>, ? extends Validated<? extends X>> mapper) {
		return new IgnoringUnknown<>(List.copyOf(parts), mapper, TargetConstraints.empty());
	}

	/**
	 * A part with what writes it back: the function that returns the input that the
	 * validator of the part reads a value from, and the accessor that returns the value
	 * of the part from the created object. A list of these is what {@link #ofReversible}
	 * takes, so that a part that cannot be written back does not compile.
	 *
	 * @param part the part to read
	 * @param reverse returns the value that the validator of the part reads the given
	 * validated value from: a value of a JDK type, a map for a nested or an embedded
	 * object and for the rest, a list for a list, or {@code null} for no value
	 * @param accessor returns the validated value of the part from the created object
	 * @param <X> the type of the created object
	 * @param <R> the type of the validated value of the part
	 * @since 0.18.0
	 */
	public record ReversiblePart<X, R extends @Nullable Object> (Part<R> part,
			Function<? super R, ? extends @Nullable Object> reverse, Function<? super X, ? extends R> accessor) {

		private @Nullable Object value(X object) {
			return this.accessor.apply(object);
		}

		// the value is the one that the accessor returns, which is null only if R is
		@SuppressWarnings({ "unchecked", "NullAway" })
		private @Nullable Object write(@Nullable Object value) {
			return this.reverse.apply((R) value);
		}

	}

	/**
	 * Returns a validator like {@link #of} that also writes the created object back as
	 * the map that it is read from, as {@link Reversible} states. This is the way to
	 * define more properties than the builders support.
	 * @param parts the parts to read, each with its reverse and its accessor
	 * @param mapper the function that creates the object from the validated values, which
	 * are given in the order of the parts
	 * @param <X> the type of the created object
	 * @return a validator that ignores unknown properties unless a part is the rest
	 * @throws IllegalArgumentException as {@link #of} does
	 * @throws IllegalStateException as {@link #of} does
	 */
	public static <X> Reversible.IgnoringUnknown<X> ofReversible(List<? extends ReversiblePart<? super X, ?>> parts,
			Function<? super List<@Nullable Object>, ? extends X> mapper) {
		return new Reversible.IgnoringUnknown<>(List.copyOf(parts),
				values -> Validated.successWith(mapper.apply(values)), TargetConstraints.empty());
	}

	/**
	 * Returns a validator like {@link #flatOf} that also writes the created object back
	 * as the map that it is read from, as {@link Reversible} states.
	 * @param parts the parts in the order of the values, each with its reverse and its
	 * accessor
	 * @param mapper function that creates the result from the validated values, in the
	 * order of the parts, or reports violations
	 * @param <X> the type of the result
	 * @return a new validator
	 * @throws IllegalArgumentException as {@link #ofReversible} does
	 * @throws IllegalStateException as {@link #ofReversible} does
	 */
	public static <X> Reversible.IgnoringUnknown<X> flatOfReversible(List<? extends ReversiblePart<? super X, ?>> parts,
			Function<? super List<@Nullable Object>, ? extends Validated<? extends X>> mapper) {
		return new Reversible.IgnoringUnknown<>(List.copyOf(parts), mapper, TargetConstraints.empty());
	}

	private ObjectInputValidator(List<Part<?>> parts,
			Function<? super List<@Nullable Object>, ? extends Validated<? extends X>> mapper,
			boolean rejectUnknownProperties, TargetConstraints<X> rules) {
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
				case Property<?> property -> added = List.of(List.of(new ValueDescription.Property(property.name(),
						property.strictness().describe(described), property.withheld())));
				case PresenceProperty<?> property ->
					added = List.of(List.of(new ValueDescription.Property(property.name(),
							described instanceof ValueDescription.Presence ? described
									: new ValueDescription.Presence(described),
							property.withheld())));
				case Embedded<?> embedded -> {
					added = alternativesOf(described, false);
					if (added != null) {
						final Set<String> embeddedNames = new LinkedHashSet<>();
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
		this.rules = rules;
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
						.map(property -> property.value().acceptsAbsent() ? property
								: property.withValue(new ValueDescription.Optional(property.value(), null)))
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
			case ValueDescription.AnyOf anyOf -> anyOf.alternatives().stream().anyMatch(ObjectInputValidator::rejectsOthers);
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
	 * A validator that ignores the keys of the input map that no property is defined for,
	 * and that can be told to reject them with {@link #rejectingUnknownProperties()}.
	 * This is what {@code fromMap} and {@link ObjectInputValidator#of} return, unless the
	 * rest of the keys is read with {@code _rest}: no key is unknown then.
	 *
	 * @param <X> the type of the created object
	 * @since 0.18.0
	 */
	public static final class IgnoringUnknown<X extends @Nullable Object> extends ObjectInputValidator<X> {

		private IgnoringUnknown(List<Part<?>> parts,
				Function<? super List<@Nullable Object>, ? extends Validated<? extends X>> mapper,
				TargetConstraints<X> rules) {
			super(parts, mapper, false, rules);
		}

		/**
		 * Returns a validator that reports the keys of the input map that no property is
		 * defined for with {@code object.unknownProperty}, instead of ignoring them. The
		 * properties of the embedded validators are defined. It applies to this map only:
		 * a nested validator decides for its own map.
		 * @return a validator that rejects unknown properties
		 * @throws IllegalStateException if an embedded validator does not describe its
		 * properties, or if the validator was created from parts with a {@link Rest}
		 */
		public ObjectInputValidator<X> rejectingUnknownProperties() {
			return new ObjectInputValidator<>(super.parts, super.mapper, true, super.rules);
		}

		/**
		 * Returns a validator that additionally checks the created object with the given
		 * predicate, as {@link ValueValidator#constraintOnTarget} states. The returned
		 * validator can still be told to reject unknown properties.
		 * @param predicate the rule that the created object must satisfy
		 * @param name the name of the reported violation
		 * @param violationMessage the message key and the default message format of the
		 * reported violation
		 * @return a validator with the additional rule
		 */
		@Override
		public IgnoringUnknown<X> constraintOnTarget(Predicate<? super X> predicate, String name,
				ViolationMessage violationMessage) {
			return new IgnoringUnknown<>(super.parts, super.mapper, super.rules.and(predicate, name, violationMessage));
		}

	}

	/**
	 * A validator that also writes an object back as the map that it reads the object
	 * from. This is what {@code fromMapReversible} of the builders returns:
	 *
	 * <pre>{@code
	 * ObjectInputValidator.Reversible<Employee> validator = Yavi.reversibleSchema()
	 *     ._integer("id")
	 *     ._embedded(userNameValidator) // first_name, last_name
	 *     ._string(emailValidator) // Yavi._string("email", ...).map(Email::new, Email::value)
	 *     ._string("nickname").optional()
	 *     .fromMapReversible(Employee::new, Employee::id, Employee::name, Employee::email, Employee::nickname);
	 *
	 * validator.validate(row); // reads, as fromMap does
	 * validator.toMap(employee);
	 * // {id=1, first_name=Taro, last_name=Yamada, email=taro@example.com, nickname=null}
	 * }</pre>
	 *
	 * <p>
	 * {@link #toMap(Object)} is defined by the round trip: for every object {@code x}
	 * that the validator creates, {@code validate(toMap(x))} succeeds with an object
	 * equal to {@code x}, as long as the function that creates the object and the
	 * accessors agree and no property is withheld. The one exception is an empty list of
	 * a property that is read from text ({@code splitBy}) and does not accept
	 * {@code null}: it is written as {@code null}. The map holds the values of the JDK
	 * types that the properties are read from, as they are: nothing is converted to text.
	 * {@link ParameterTree#ofValues} holds them as the text of parameters.
	 *
	 * <ul>
	 * <li>A property is written under its name with the value that it is read from: the
	 * validated value of constraints, the value that a validator
	 * {@linkplain ReversibleValueValidator#reverse(Object) goes back} to, the map of a
	 * nested validator and the list or the map of the elements written in this way.</li>
	 * <li>A property that has no value ({@code optional()}, {@code nullable()}) is
	 * written with {@code null}, so that the map has the key of every property, which a
	 * named parameter of a statement needs. A property that rejects {@code null}
	 * ({@code rejectingNull()}) has no entry then, and neither has one that is
	 * {@link Presence.Absent}.</li>
	 * <li>The properties of an embedded validator are written into the same map. Those of
	 * one that has no value are written with {@code null}.</li>
	 * <li>The rest of the properties is written into the same map.</li>
	 * </ul>
	 *
	 * <p>
	 * A property can be written only if its validator knows its reverse, which its type
	 * tells: a schema of {@link am.ik.yavi.validator.Yavi#reversibleSchema()} takes a
	 * {@link ReversibleValueValidator} only, so a property that cannot be written back
	 * does not compile. A validator that maps its value states its reverse with
	 * {@link ValueValidator#map(Function, Function)}.
	 *
	 * <p>
	 * A property that is {@linkplain am.ik.yavi.builder.Withholdable#withheld() withheld}
	 * where it is defined, such as a password, is not written by {@link #toMap(Object)},
	 * at any depth, so that the map can be shown as it is. {@link #reverse(Object)}
	 * writes every property, and the round trip holds for it.
	 *
	 * <p>
	 * Some of the properties are written by a {@link PropertyView}, which
	 * {@link #including(String, String...)} and {@link #excluding(String, String...)}
	 * build: a list that shows a few of them, an {@code UPDATE} of the columns that may
	 * change. A view only writes, since what it writes is not read back. It writes a
	 * withheld property only if it names it.
	 *
	 * <p>
	 * The validator that reads another input ({@link #readingWith(InputReader)},
	 * {@link #readingParameters()}) is not reversible: what is written is a map.
	 *
	 * @param <X> the type of the created object
	 * @since 0.18.0
	 * @see PropertyView
	 */
	public static sealed class Reversible<X> extends ObjectInputValidator<X>
			implements ReversibleValueValidator<Map<String, Object>, X> {

		private final List<ReversiblePart<? super X, ?>> written;

		/** The properties that are not withheld, which is what {@link #toMap} writes. */
		private final PropertyView<X> shown;

		private Reversible(List<ReversiblePart<? super X, ?>> written,
				Function<? super List<@Nullable Object>, ? extends Validated<? extends X>> mapper,
				boolean rejectUnknownProperties, TargetConstraints<X> rules) {
			super(written.stream().<Part<?>>map(ReversiblePart::part).toList(), mapper, rejectUnknownProperties, rules);
			this.written = written;
			this.shown = PropertyView.of(this::write, this.description()).withoutWithheld();
		}

		/**
		 * Returns the map that this validator reads the given object from, without the
		 * properties that are {@linkplain am.ik.yavi.builder.Withholdable#withheld()
		 * withheld}, at any depth. This is {@link #reverse(Object)} for a validator that
		 * withholds nothing. The accessor of a withheld property of this validator is not
		 * called.
		 * @param object an object that this validator creates
		 * @return a new map of the properties in their order, which the caller may
		 * change, for example to add a parameter of a statement. A value is {@code null}
		 * for a property that has none
		 * @throws IllegalArgumentException if the object holds a property twice, as a
		 * rest that has a property of the validator does
		 */
		public Map<String, Object> toMap(X object) {
			return this.shown.toMap(object);
		}

		/**
		 * Returns a view that writes only the given properties of an object.
		 *
		 * <pre>{@code
		 * static final PropertyView<User> SUMMARY = userValidator.including("id", "name", "address.city");
		 *
		 * SUMMARY.toMap(user); // {id=1, name=Taro, address={city=Tokyo}}
		 * }</pre>
		 *
		 * The names are checked here, so build the view once, as a constant next to the
		 * validator. The rest of the properties is not written, and a property that is
		 * {@linkplain am.ik.yavi.builder.Withholdable#withheld() withheld} is written
		 * only if it is named.
		 * @param name the name of a property, or the path to a property of a nested
		 * object as {@link PropertyView} states
		 * @param names the names of the other properties
		 * @return a view of the given properties, which only writes
		 * @throws IllegalArgumentException if this validator does not define a property
		 * of a given name, or as {@link PropertyView#including(String, String...)} states
		 */
		public PropertyView<X> including(String name, String... names) {
			return PropertyView.of(this::write, this.description()).including(name, names);
		}

		/**
		 * Returns a view that writes the properties of an object except the given ones
		 * and those that are {@linkplain am.ik.yavi.builder.Withholdable#withheld()
		 * withheld}.
		 *
		 * <pre>{@code
		 * static final PropertyView<User> LISTED = userValidator.excluding("address");
		 *
		 * LISTED.toMap(user); // {id=1, name=Taro}
		 * }</pre>
		 *
		 * The names are checked here, so build the view once, as a constant next to the
		 * validator. The rest of the properties is written. A property that must never
		 * leave is withheld where it is defined instead of being excluded here: a view
		 * that excludes writes a property that is added to the schema later.
		 * @param name the name of a property, or the path to a property of a nested
		 * object as {@link PropertyView} states
		 * @param names the names of the other properties
		 * @return a view without the given properties, which only writes
		 * @throws IllegalArgumentException if this validator does not define a property
		 * of a given name, if the property is withheld, or as
		 * {@link PropertyView#excluding(String, String...)} states
		 */
		public PropertyView<X> excluding(String name, String... names) {
			return PropertyView.of(this::write, this.description()).excluding(name, names);
		}

		/**
		 * Returns the map of the given object. A property whose name the given predicate
		 * rejects may be left out, and its accessor is not called then.
		 */
		private Map<String, Object> write(X object, Predicate<String> names) {
			final List<Part<?>> parts = this.parts();
			final Map<String, @Nullable Object> map = new LinkedHashMap<>();
			for (int i = 0; i < parts.size(); i++) {
				final ReversiblePart<? super X, ?> written = this.written.get(i);
				switch (parts.get(i)) {
					case Property<?> property -> {
						if (!names.test(property.name())) {
							continue;
						}
						final Object input = written.write(written.value(object));
						if (input != null || property.strictness() != Strictness.REJECTING_NULL) {
							put(map, property.name(), input);
						}
					}
					case PresenceProperty<?> property -> {
						if (!names.test(property.name())) {
							continue;
						}
						final Object value = written.value(object);
						if (!(value instanceof Presence.Absent)) {
							put(map, property.name(), written.write(value));
						}
					}
					case Embedded<?> embedded -> {
						final Set<String> embeddedNames = this.embeddedNames(i);
						if (embeddedNames != null && embeddedNames.stream().noneMatch(names)) {
							continue;
						}
						final Object input = written.write(written.value(object));
						if (input instanceof Map<?, ?> properties) {
							properties.forEach((name, property) -> put(map, String.valueOf(name), property));
						}
						else if (input == null && embeddedNames != null) {
							embeddedNames.forEach(name -> put(map, name, null));
						}
						else if (input != null) {
							throw new IllegalArgumentException("The embedded validator at position " + (i + 1)
									+ " is not read from a map: " + input.getClass().getName());
						}
					}
					case Rest<?> rest -> {
						if (written.write(written.value(object)) instanceof Map<?, ?> properties) {
							properties.forEach((name, property) -> put(map, String.valueOf(name), property));
						}
					}
				}
			}
			return nonNullValues(map);
		}

		final List<ReversiblePart<? super X, ?>> written() {
			return this.written;
		}

		private static void put(Map<String, @Nullable Object> map, String name, @Nullable Object value) {
			if (map.containsKey(name)) {
				throw new IllegalArgumentException("The property '" + name + "' is written twice.");
			}
			map.put(name, value);
		}

		// the map that a validator reads may hold null, although its type does not say so
		@SuppressWarnings({ "unchecked", "NullAway" })
		private static Map<String, Object> nonNullValues(Map<String, @Nullable Object> map) {
			return (Map<String, Object>) (Map<String, ?>) map;
		}

		/**
		 * Returns the map that this validator reads the given object from, with every
		 * property: this is {@link #toMap(Object)} with the properties that are withheld,
		 * so that the round trip holds whatever is withheld. It is what a validator that
		 * nests this one writes its own {@code reverse} with.
		 * @param object an object that this validator creates
		 * @return a new map of the properties
		 * @throws IllegalArgumentException as {@link #toMap(Object)} does
		 */
		@Override
		public Map<String, Object> reverse(X object) {
			return this.write(object, name -> true);
		}

		/**
		 * Returns a validator that additionally checks the created object with the given
		 * predicate, as {@link ValueValidator#constraintOnTarget} states. The returned
		 * validator still writes the object back.
		 * @param predicate the rule that the created object must satisfy
		 * @param name the name of the reported violation
		 * @param violationMessage the message key and the default message format of the
		 * reported violation
		 * @return a validator with the additional rule
		 */
		@Override
		public Reversible<X> constraintOnTarget(Predicate<? super X> predicate, String name,
				ViolationMessage violationMessage) {
			return new Reversible<>(this.written, this.mapper(), this.rejectsUnknownProperties(),
					this.rules().and(predicate, name, violationMessage));
		}

		/**
		 * A {@link Reversible} validator that ignores the keys of the input map that no
		 * property is defined for, and that can be told to reject them with
		 * {@link #rejectingUnknownProperties()}, as
		 * {@link ObjectInputValidator.IgnoringUnknown} states. This is what
		 * {@code fromMapReversible} and {@link ObjectInputValidator#ofReversible} return,
		 * unless the rest of the keys is read with {@code _rest}.
		 *
		 * @param <X> the type of the created object
		 * @since 0.18.0
		 */
		public static final class IgnoringUnknown<X> extends Reversible<X> {

			private IgnoringUnknown(List<ReversiblePart<? super X, ?>> written,
					Function<? super List<@Nullable Object>, ? extends Validated<? extends X>> mapper,
					TargetConstraints<X> rules) {
				super(written, mapper, false, rules);
			}

			/**
			 * Returns a validator that reports the keys of the input map that no property
			 * is defined for with {@code object.unknownProperty}, instead of ignoring
			 * them, as
			 * {@link ObjectInputValidator.IgnoringUnknown#rejectingUnknownProperties()}
			 * states. The returned validator still writes the object back.
			 * @return a validator that rejects unknown properties
			 * @throws IllegalStateException if an embedded validator does not describe
			 * its properties, or if the validator was created from parts with a
			 * {@link Rest}
			 */
			public Reversible<X> rejectingUnknownProperties() {
				return new Reversible<>(this.written(), this.mapper(), true, this.rules());
			}

			/**
			 * Returns a validator that additionally checks the created object with the
			 * given predicate, as {@link ValueValidator#constraintOnTarget} states. The
			 * returned validator can still be told to reject unknown properties.
			 * @param predicate the rule that the created object must satisfy
			 * @param name the name of the reported violation
			 * @param violationMessage the message key and the default message format of
			 * the reported violation
			 * @return a validator with the additional rule
			 */
			@Override
			public IgnoringUnknown<X> constraintOnTarget(Predicate<? super X> predicate, String name,
					ViolationMessage violationMessage) {
				return new IgnoringUnknown<>(this.written(), this.mapper(),
						this.rules().and(predicate, name, violationMessage));
			}

		}

	}

	final List<Part<?>> parts() {
		return this.parts;
	}

	final Function<? super List<@Nullable Object>, ? extends Validated<? extends X>> mapper() {
		return this.mapper;
	}

	final TargetConstraints<X> rules() {
		return this.rules;
	}

	final boolean rejectsUnknownProperties() {
		return this.rejectUnknownProperties;
	}

	/**
	 * Returns the names of the properties of the embedded validator at the given
	 * position, in their order, or {@code null} if it does not describe them.
	 */
	final @Nullable Set<String> embeddedNames(int position) {
		final Group group = this.groups.get(position);
		return group == null ? null : group.names();
	}

	/**
	 * Returns a validator that additionally checks the created object with the given
	 * predicate, as {@link ValueValidator#constraintOnTarget} states. The returned
	 * validator still reads the properties, so it can be embedded and read parameters.
	 * @param predicate the rule that the created object must satisfy
	 * @param name the name of the reported violation
	 * @param violationMessage the message key and the default message format of the
	 * reported violation
	 * @return a validator with the additional rule
	 */
	@Override
	public ObjectInputValidator<X> constraintOnTarget(Predicate<? super X> predicate, String name,
			ViolationMessage violationMessage) {
		return new ObjectInputValidator<>(this.parts, this.mapper, this.rejectUnknownProperties,
				this.rules.and(predicate, name, violationMessage));
	}

	/**
	 * Returns a validator that reads the same properties from a flat map of parameters,
	 * such as request parameters or a submitted form. The map is read as the
	 * {@link ParameterTree} it stands for: a key such as {@code address.city} or
	 * {@code items[0].name} is a path to a nested property, a key may have several values
	 * and a value is text that stands for a value of the type of its property, as
	 * {@link am.ik.yavi.core.CoercibleInputReader#coercingStrings()} states. This applies
	 * to the nested validators too. The returned validator given another reader with
	 * {@link ValueValidator#readingWith(InputReader)} reads the nodes of that reader, not
	 * parameters.
	 *
	 * <p>
	 * The returned validator takes every form of such a map, a
	 * {@code Map<String, String>}, a {@code Map<String, List<String>>} and a
	 * {@code Map<String, String[]>}, so its type does not depend on what it is assigned
	 * to and any combinator can follow it:
	 *
	 * <pre>{@code
	 * ValueValidator<Map<String, ?>, SearchForm> validator = Yavi.schema()
	 *     ._string("q", c -> c.notBlank())
	 *     ._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
	 *     .fromMap(SearchForm::new)
	 *     .readingParameters()
	 *     .formattingWith(messageFormatter);
	 * }</pre>
	 *
	 * <p>
	 * {@link ParameterTree#toMap
	 * ParameterTree.of(parameters).toMap(validator.description())} returns the parameters
	 * as the validator reads them, to show a form again with the text that was sent.
	 * @return a validator of a map of parameters, whose values are strings, or iterables
	 * or arrays of strings
	 * @see ParameterTree
	 */
	public ValueValidator<Map<String, ?>, X> readingParameters() {
		return this.readingParameters(StringCoercion.strict());
	}

	/**
	 * Returns a validator like {@link #readingParameters()} that takes the text of a
	 * value by the given rules, for example to read {@code on}, which a checked checkbox
	 * of a form sends, as {@code true}:
	 *
	 * <pre>{@code
	 * validator.readingParameters(StringCoercion.strict().trimming().booleanWords());
	 * }</pre>
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
			final List<Part<?>> parts = ObjectInputValidator.this.parts;
			final Set<String> names = ObjectInputValidator.this.names;
			final List<@Nullable Object> values = new ArrayList<>(parts.size());
			final List<ConstraintViolation> violations = new ArrayList<>();
			for (int i = 0; i < parts.size(); i++) {
				final ValueValidator<Object, ?> validator = this.validators.get(i);
				final Validated<?> validated;
				switch (parts.get(i)) {
					case Property<?> property -> {
						final String name = property.name();
						final N value = this.reader.property(input, name);
						final boolean absent = value == null && !this.reader.hasProperty(input, name);
						final boolean isNull = value == null || this.reader.isNull(value);
						final boolean empty = value != null && isNull && this.reader.isEmptyText(value);
						final Validated<?> read = switch (property.strictness()) {
							case REQUIRED -> absent ? null : validator.validate(isNull ? null : value, locale, constraintContext);
							case REJECTING_NULL -> isNull && !absent
									? Validated.failureWith(violation(name,
											empty ? ViolationMessage.Default.OBJECT_REQUIRED
													: ViolationMessage.Default.OBJECT_NOT_NULL,
											null, locale))
									: validator.validate(isNull ? null : value, locale, constraintContext);
							case ABSENT_AS_NULL -> validator.validate(isNull ? null : value, locale, constraintContext);
						};
						// an absent key is required rather than null, and so is text without
						// a value unless a constraint has a message of its own for it
						validated = property.messages()
							.applyTo(name, read != null && (read.isValid() || !(absent || (empty && rejectsNull(read)))) ? read
									: Validated.failureWith(
											violation(name, ViolationMessage.Default.OBJECT_REQUIRED, null, locale)));
					}
					case PresenceProperty<?> property -> {
						final String name = property.name();
						final N value = this.reader.property(input, name);
						final boolean isNull = value == null || this.reader.isNull(value);
						final Validated<?> read = value == null && !this.reader.hasProperty(input, name)
								? Validated.successWith(Presence.absent())
								: validator.validate(isNull ? null : value, locale, constraintContext);
						validated = property.messages()
							.applyTo(name, value != null && isNull && this.reader.isEmptyText(value) && rejectsNull(read)
									? Validated.failureWith(
											violation(name, ViolationMessage.Default.OBJECT_REQUIRED, null, locale))
									: read);
					}
					case Embedded<?> embedded -> {
						final Group group = ObjectInputValidator.this.groups.get(i);
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
			if (ObjectInputValidator.this.rejectUnknownProperties) {
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
					? Validated.<X>of(ObjectInputValidator.this.mapper.apply(Collections.unmodifiableList(values)))
						.localizedWith(locale)
						.flatMap(x -> ObjectInputValidator.this.rules.check(x, locale))
					: Validated.failureWith(violations);
		}

		/**
		 * Returns whether the result is the violation that rejects {@code null} with the
		 * message of {@code object.notNull}.
		 */
		private static boolean rejectsNull(Validated<?> validated) {
			if (validated.isValid() || validated.errors().size() != 1) {
				return false;
			}
			final ConstraintViolation violation = validated.errors().get(0);
			final ViolationMessage notNull = ViolationMessage.Default.OBJECT_NOT_NULL;
			return violation.localizable() && notNull.messageKey().equals(violation.messageKey())
					&& notNull.defaultMessageFormat().equals(violation.defaultMessageFormat());
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
			return ObjectInputValidator.this.description();
		}

		@Override
		public <M> ValueValidator<M, X> readingWith(InputReader<M> reader) {
			return ObjectInputValidator.this.readingWith(reader);
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
