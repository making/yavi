/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import am.ik.yavi.core.Presence;
import am.ik.yavi.core.ReversibleValueValidator;
import am.ik.yavi.core.ViolationMessage;
import am.ik.yavi.input.ObjectInputValidator;
import org.jspecify.annotations.Nullable;

/**
 * A {@link DynamicSchema} whose validator also writes the map of the validated values
 * back as the map that it is read from. Instances are obtained from
 * {@link am.ik.yavi.validator.Yavi#reversibleDynamicSchema()}:
 *
 * <pre>{@code
 * ObjectInputValidator.Reversible<Map<String, Object>> validator = Yavi.<Object>reversibleDynamicSchema()
 *     .property("email", Yavi._string(c -> c.email()).map(Email::new, Email::value))
 *     .property("memo", Yavi._string().nullable())
 *     .build();
 * Map<String, Object> values = validator.validated(Map.of("email", "a@example.com")); // {email=Email[...], memo=null}
 * validator.toMap(values); // {email=a@example.com, memo=null}
 * }</pre>
 *
 * <p>
 * The schema is built as a {@link DynamicSchema} is, with the same modifiers, but a
 * property is added with a {@link ReversibleValueValidator}: a validator that does not
 * know its reverse does not compile. What is written for a property is stated in
 * {@link ObjectInputValidator.Reversible}.
 *
 * @param <V> the type of the values of the produced map; nullable if a validator produces
 * {@code null}
 * @since 0.18.0
 */
public sealed class ReversibleDynamicSchema<V extends @Nullable Object> permits ReversibleDynamicSchema.Keyed,ReversibleDynamicSchema.NullAccepting {

	final List<ReversibleArgumentDefinition<?, ?>> definitions;

	/**
	 * Creates a schema without properties.
	 *
	 * <p>
	 * Instances are normally obtained from
	 * {@link am.ik.yavi.validator.Yavi#reversibleDynamicSchema()}.
	 */
	public ReversibleDynamicSchema() {
		this(List.of());
	}

	ReversibleDynamicSchema(List<ReversibleArgumentDefinition<?, ?>> definitions) {
		this.definitions = definitions;
	}

	/**
	 * Adds a property with the given name whose value is validated by the given
	 * validator, as
	 * {@link DynamicSchema#property(String, am.ik.yavi.core.ValueValidator)} does, and
	 * written back with the reverse of the validator.
	 * @param <A> type of the value validated by {@code validator}
	 * @param name the name of the property
	 * @param validator value validator for the property, which knows its input type and
	 * its reverse
	 * @return a schema with the property, to which its modifiers apply
	 * @throws IllegalArgumentException if a property with the same name is defined
	 */
	public <A> Added<V> property(String name, ReversibleValueValidator<A, ? extends V> validator) {
		return new Added<>(this.with(name, ReversibleArgumentDefinition.object(name, validator)));
	}

	/**
	 * Adds a property with the given name whose value is validated by the given validator
	 * that accepts {@code null}, as
	 * {@link DynamicSchema#property(String, am.ik.yavi.core.NullAcceptingValueValidator)}
	 * does, and written back with the reverse of the validator.
	 * @param <A> type of the value validated by {@code validator}
	 * @param name the name of the property
	 * @param validator value validator for the property, which accepts {@code null} and
	 * knows its reverse
	 * @return a schema with the property, to which its modifiers apply
	 * @throws IllegalArgumentException if a property with the same name is defined
	 */
	public <A> AddedNullAccepting<V> property(String name,
			ReversibleValueValidator.NullAccepting<A, ? extends V> validator) {
		return new AddedNullAccepting<>(this.with(name, ReversibleArgumentDefinition.object(name, validator)));
	}

	/**
	 * Reads the properties that no property of this schema is defined for, as
	 * {@link DynamicSchema#rest(am.ik.yavi.core.ValueValidator)} does, and writes them
	 * back into the same map as the defined properties.
	 * @param <A> type of the value validated by {@code valueValidator}
	 * @param valueValidator value validator for the value of every other key, which knows
	 * its reverse
	 * @return a builder that builds the validator
	 */
	public <A> Complete<V> rest(ReversibleValueValidator<A, ? extends V> valueValidator) {
		final List<ReversibleArgumentDefinition<?, ?>> definitions = new ArrayList<>(this.definitions);
		definitions.add(ReversibleArgumentDefinition.rest(valueValidator));
		return new Complete<>(definitions);
	}

	/**
	 * Builds the validator that reads the defined properties from a map, as
	 * {@link DynamicSchema#build()} does, and that writes the produced map back.
	 * @return a validator that ignores the keys that no property is defined for
	 * @throws IllegalStateException as {@link DynamicSchema#build()} does
	 */
	public ObjectInputValidator.Reversible.IgnoringUnknown<Map<String, V>> build() {
		return build(this.definitions);
	}

	private List<ReversibleArgumentDefinition<?, ?>> with(String name, ReversibleArgumentDefinition<?, ?> definition) {
		if (this.definitions.stream().anyMatch(defined -> name.equals(defined.name()))) {
			throw new IllegalArgumentException("The property '" + name + "' is defined twice.");
		}
		final List<ReversibleArgumentDefinition<?, ?>> definitions = new ArrayList<>(this.definitions);
		definitions.add(definition);
		return definitions;
	}

	List<ReversibleArgumentDefinition<?, ?>> withLast(ReversibleArgumentDefinition<?, ?> definition) {
		final List<ReversibleArgumentDefinition<?, ?>> definitions = new ArrayList<>(this.definitions);
		definitions.set(definitions.size() - 1, definition);
		return definitions;
	}

	ReversibleArgumentDefinition<?, ?> last() {
		return this.definitions.get(this.definitions.size() - 1);
	}

	private static <V extends @Nullable Object> ObjectInputValidator.Reversible.IgnoringUnknown<Map<String, V>> build(
			List<ReversibleArgumentDefinition<?, ?>> definitions) {
		final List<ReversibleArgumentDefinition<?, ?>> defined = List.copyOf(definitions);
		final List<ArgumentDefinition<?, ?>> read = defined.stream()
			.<ArgumentDefinition<?, ?>>map(ReversibleArgumentDefinition::definition)
			.toList();
		final Set<@Nullable String> names = new HashSet<>();
		defined.forEach(definition -> names.add(definition.name()));
		final List<ObjectInputValidator.ReversiblePart<Map<String, V>, ?>> parts = new ArrayList<>(defined.size());
		for (int i = 0; i < defined.size(); i++) {
			parts.add(part(defined.get(i), i + 1, names));
		}
		return ObjectInputValidator.ofReversible(parts, values -> DynamicSchema.mapOf(read, values));
	}

	// the produced map holds the validated value of every property under its name
	@SuppressWarnings("unchecked")
	private static <V extends @Nullable Object, R extends @Nullable Object> ObjectInputValidator.ReversiblePart<Map<String, V>, R> part(
			ReversibleArgumentDefinition<?, R> definition, int position, Set<@Nullable String> names) {
		final String name = definition.name();
		final Function<Map<String, V>, @Nullable Object> accessor = switch (definition.source()) {
			case PRESENCE -> map -> map.containsKey(name) ? Presence.ofNullable(map.get(name)) : Presence.absent();
			case REST -> map -> {
				final Map<String, V> rest = new LinkedHashMap<>();
				map.forEach((key, value) -> {
					if (!names.contains(key)) {
						rest.put(key, value);
					}
				});
				return rest;
			};
			default -> map -> map.get(name);
		};
		return definition.part(position, (Function<Map<String, V>, R>) (Function<?, ?>) accessor);
	}

	/**
	 * The schema after a property is added with a validator that is not known to accept
	 * {@code null}, with the modifiers of {@link DynamicSchema.Keyed}.
	 *
	 * @param <V> the type of the values of the produced map
	 * @since 0.18.0
	 */
	public static sealed class Keyed<V extends @Nullable Object> extends ReversibleDynamicSchema<V>
			implements MessageModifiers<Keyed<V>>permits Added {

		Keyed(List<ReversibleArgumentDefinition<?, ?>> definitions) {
			super(definitions);
		}

		@Override
		public Keyed<V> label(String label) {
			return new Keyed<>(this.withLast(this.last().withMessages(this.last().messages().withLabel(label))));
		}

		@Override
		public Keyed<V> typeMessage(ViolationMessage message) {
			return new Keyed<>(this.withLast(this.last().withMessages(this.last().messages().withType(message))));
		}

		/**
		 * Sets the message of the violation reported when the key is absent, as
		 * {@link DynamicSchema.Keyed#required(String)} does.
		 * @param messageFormat the message format
		 * @return a schema whose last property reports the given message
		 */
		public ReversibleDynamicSchema<V> required(String messageFormat) {
			return this.required(ViolationMessage.Default.OBJECT_REQUIRED.withText(messageFormat));
		}

		/**
		 * Sets the message of the violation reported when the key is absent, as
		 * {@link DynamicSchema.Keyed#required(ViolationMessage)} does.
		 * @param message the message key and the default message format
		 * @return a schema whose last property reports the given message
		 */
		public ReversibleDynamicSchema<V> required(ViolationMessage message) {
			return new ReversibleDynamicSchema<>(
					this.withLast(this.last().withMessages(this.last().messages().withRequired(message))));
		}

		/**
		 * Tells an absent key from {@code null}, as
		 * {@link DynamicSchema.Keyed#presence()} does: the property has no entry in the
		 * produced map if its key is absent, and no entry is written for it then.
		 * @return a schema whose last property may be absent
		 */
		public ReversibleDynamicSchema<V> presence() {
			return new ReversibleDynamicSchema<>(this.withLast(this.last().withPresence()));
		}

	}

	/**
	 * The schema after a property is added with a validator that accepts {@code null},
	 * with the modifiers of {@link DynamicSchema.NullAccepting}.
	 *
	 * @param <V> the type of the values of the produced map
	 * @since 0.18.0
	 */
	public static sealed class NullAccepting<V extends @Nullable Object> extends ReversibleDynamicSchema<V>
			implements MessageModifiers<NullAccepting<V>>permits AddedNullAccepting {

		NullAccepting(List<ReversibleArgumentDefinition<?, ?>> definitions) {
			super(definitions);
		}

		@Override
		public NullAccepting<V> label(String label) {
			return new NullAccepting<>(
					this.withLast(this.last().withMessages(this.last().messages().withLabel(label))));
		}

		@Override
		public NullAccepting<V> typeMessage(ViolationMessage message) {
			return new NullAccepting<>(
					this.withLast(this.last().withMessages(this.last().messages().withType(message))));
		}

		/**
		 * Requires the key, as {@link DynamicSchema.NullAccepting#required()} does.
		 * @return a schema whose last property requires its key
		 */
		public ReversibleDynamicSchema<V> required() {
			return new ReversibleDynamicSchema<>(
					this.withLast(this.last().withStrictness(ObjectInputValidator.Strictness.REQUIRED)));
		}

		/**
		 * Requires the key and sets the message of the violation, as
		 * {@link DynamicSchema.NullAccepting#required(String)} does.
		 * @param messageFormat the message format
		 * @return a schema whose last property requires its key
		 */
		public ReversibleDynamicSchema<V> required(String messageFormat) {
			return this.required(ViolationMessage.Default.OBJECT_REQUIRED.withText(messageFormat));
		}

		/**
		 * Requires the key and sets the message of the violation, as
		 * {@link DynamicSchema.NullAccepting#required(ViolationMessage)} does.
		 * @param message the message key and the default message format
		 * @return a schema whose last property requires its key
		 */
		public ReversibleDynamicSchema<V> required(ViolationMessage message) {
			final ReversibleArgumentDefinition<?, ?> required = this.last()
				.withStrictness(ObjectInputValidator.Strictness.REQUIRED);
			return new ReversibleDynamicSchema<>(
					this.withLast(required.withMessages(required.messages().withRequired(message))));
		}

		/**
		 * Rejects {@code null} of a present key, as
		 * {@link DynamicSchema.NullAccepting#rejectingNull()} does. No entry is written
		 * for the property if it has no value.
		 * @return a schema whose last property rejects {@code null}
		 */
		public ReversibleDynamicSchema<V> rejectingNull() {
			return new ReversibleDynamicSchema<>(
					this.withLast(this.last().withStrictness(ObjectInputValidator.Strictness.REJECTING_NULL)));
		}

		/**
		 * Tells an absent key from {@code null}, as
		 * {@link DynamicSchema.NullAccepting#presence()} does.
		 * @return a schema whose last property may be absent
		 */
		public ReversibleDynamicSchema<V> presence() {
			return new ReversibleDynamicSchema<>(this.withLast(this.last().withPresence()));
		}

	}

	/**
	 * The schema after a property that requires its value is added, until a modifier
	 * other than {@code label} and {@code typeMessage} is written: before the other
	 * modifiers of {@link Keyed}, {@link #withheld()} holds the value of the property
	 * back from what shows the values of an object again. It is said once, so the schema
	 * that it returns does not have it.
	 *
	 * @param <V> the type of the values of the produced map
	 * @since 0.18.0
	 */
	public static final class Added<V extends @Nullable Object> extends Keyed<V> implements Withholdable<Keyed<V>> {

		Added(List<ReversibleArgumentDefinition<?, ?>> definitions) {
			super(definitions);
		}

		@Override
		public Keyed<V> withheld() {
			return new Keyed<>(this.withLast(this.last().asWithheld()));
		}

		@Override
		public Added<V> label(String label) {
			return new Added<>(this.withLast(this.last().withMessages(this.last().messages().withLabel(label))));
		}

		@Override
		public Added<V> typeMessage(ViolationMessage message) {
			return new Added<>(this.withLast(this.last().withMessages(this.last().messages().withType(message))));
		}

	}

	/**
	 * The schema directly after a property that accepts {@code null} is added: before the
	 * modifiers of {@link NullAccepting}, {@link #withheld()} holds the value of the
	 * property back from what shows the values of an object again. It is said once, so
	 * the schema that it returns does not have it; {@code label} and {@code typeMessage}
	 * return this schema.
	 *
	 * @param <V> the type of the values of the produced map
	 * @since 0.18.0
	 */
	public static final class AddedNullAccepting<V extends @Nullable Object> extends NullAccepting<V>
			implements Withholdable<NullAccepting<V>> {

		AddedNullAccepting(List<ReversibleArgumentDefinition<?, ?>> definitions) {
			super(definitions);
		}

		@Override
		public NullAccepting<V> withheld() {
			return new NullAccepting<>(this.withLast(this.last().asWithheld()));
		}

		@Override
		public AddedNullAccepting<V> label(String label) {
			return new AddedNullAccepting<>(
					this.withLast(this.last().withMessages(this.last().messages().withLabel(label))));
		}

		@Override
		public AddedNullAccepting<V> typeMessage(ViolationMessage message) {
			return new AddedNullAccepting<>(
					this.withLast(this.last().withMessages(this.last().messages().withType(message))));
		}

	}

	/**
	 * The schema after the rest of the properties is defined, which only builds the
	 * validator.
	 *
	 * @param <V> the type of the values of the produced map
	 * @since 0.18.0
	 */
	public static final class Complete<V extends @Nullable Object> {

		private final List<ReversibleArgumentDefinition<?, ?>> definitions;

		Complete(List<ReversibleArgumentDefinition<?, ?>> definitions) {
			this.definitions = definitions;
		}

		/**
		 * Builds the validator that reads the defined properties and the rest from a map
		 * and writes the produced map back.
		 * @return a validator that reads every key
		 * @throws IllegalStateException as {@link DynamicSchema#build()} does
		 */
		public ObjectInputValidator.Reversible<Map<String, V>> build() {
			return ReversibleDynamicSchema.build(this.definitions);
		}

	}

}
