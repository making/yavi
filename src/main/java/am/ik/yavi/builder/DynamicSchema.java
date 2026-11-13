/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import am.ik.yavi.core.NullAcceptingValueValidator;
import am.ik.yavi.core.Presence;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolationMessage;
import am.ik.yavi.input.ObjectInputValidator;
import org.jspecify.annotations.Nullable;

/**
 * A schema whose properties are known only at run time, such as the questions of a survey
 * or the custom fields of a record: describes the properties of an input by adding one at
 * a time, and builds a validator that reads them into a map of the validated values by
 * their names.
 *
 * <p>
 * Where {@link YaviSchema} gives the validated values to a constructor, whose parameters
 * are fixed when the code is compiled, this schema is a value that a loop extends.
 * Instances are immutable and obtained from
 * {@link am.ik.yavi.validator.Yavi#dynamicSchema()}:
 *
 * <pre>{@code
 * DynamicSchema<String> schema = Yavi.dynamicSchema();
 * for (Question question : survey.questions()) {
 *     StringValidator<String> answer = Yavi._string(c -> c.notBlank().lessThanOrEqual(question.maxLength()));
 *     schema = question.required() ? schema.property(question.id(), answer).label(question.title())
 *             : schema.property(question.id(), answer.defaultValue(""));
 * }
 * ObjectInputValidator<Map<String, String>> answersValidator = schema.build();
 * }</pre>
 *
 * <ul>
 * <li>A property is added with its name and the validator of its value, which is used
 * under that name as {@link ValueValidator#named(String)} states, whatever name it was
 * built with. A value is required unless its validator accepts {@code null}
 * ({@code nullable()}, {@code defaultValue}, {@code recover}), as in a schema of
 * {@code Yavi.schema()}.</li>
 * <li>The builder returned for a property has the modifiers that the reading of a key
 * needs and that a validator cannot express, with the types of {@link YaviSchema}: the
 * label and the messages of a missing value and of a value of another type,
 * {@code presence()}, and {@code required()} and {@code rejectingNull()} for a validator
 * that accepts {@code null}.</li>
 * <li>The built validator produces an unmodifiable map with one entry per property in the
 * order they were added, whose value is the validated value, {@code null} included. A
 * property added with {@code presence()} has no entry if its key is absent.</li>
 * <li>{@link #rest(ValueValidator)} validates the keys that no property is defined for
 * and adds them to the map. Without it they are ignored, or rejected with
 * {@code rejectingUnknownProperties()} of the built validator.</li>
 * </ul>
 *
 * <p>
 * The built validator is an {@link ObjectInputValidator} like the one of {@code fromMap}:
 * it is a property of another schema ({@code _object("answers", answersValidator)}),
 * describes its properties, and reads request parameters and the tree model of a JSON
 * library.
 *
 * @param <V> the type of the values of the produced map; nullable if a validator produces
 * {@code null}
 * @since 0.18.0
 */
public sealed class DynamicSchema<V extends @Nullable Object> permits DynamicSchema.Keyed,DynamicSchema.NullAccepting {

	final List<ArgumentDefinition<?, ?>> definitions;

	/**
	 * Creates a schema without properties.
	 *
	 * <p>
	 * Instances are normally obtained from
	 * {@link am.ik.yavi.validator.Yavi#dynamicSchema()}.
	 */
	public DynamicSchema() {
		this(List.of());
	}

	DynamicSchema(List<ArgumentDefinition<?, ?>> definitions) {
		this.definitions = definitions;
	}

	/**
	 * Adds a property with the given name whose value is validated by the given
	 * validator.
	 *
	 * <p>
	 * The value is read from the key {@code name} and its violations are reported under
	 * {@code name}: the validator is used under that name, as
	 * {@link ValueValidator#named(String)} states. The property is required: an absent
	 * key is reported with {@code object.required} and {@code null} as a violation.
	 * @param <A> type of the value validated by {@code validator}
	 * @param name the name of the property
	 * @param validator value validator for the property, which knows its input type
	 * @return a schema with the property, to which its modifiers apply
	 * @throws IllegalArgumentException if a property with the same name is defined
	 */
	public <A> Keyed<V> property(String name, ValueValidator<A, ? extends V> validator) {
		return new Keyed<>(this.with(name, ArgumentDefinition.object(name, validator)));
	}

	/**
	 * Adds a property with the given name whose value is validated by the given validator
	 * that accepts {@code null}.
	 *
	 * <p>
	 * This is {@link #property(String, ValueValidator)} for a validator that is known by
	 * its type to accept {@code null}: an absent key and {@code null} are given to the
	 * validator as {@code null}, which decides what it produces, and the returned schema
	 * offers {@code required()} and {@code rejectingNull()} to tell them apart.
	 * @param <A> type of the value validated by {@code validator}
	 * @param name the name of the property
	 * @param validator value validator for the property, which accepts {@code null}
	 * @return a schema with the property, to which its modifiers apply
	 * @throws IllegalArgumentException if a property with the same name is defined
	 */
	public <A> NullAccepting<V> property(String name, NullAcceptingValueValidator<A, ? extends V> validator) {
		return new NullAccepting<>(this.with(name, ArgumentDefinition.object(name, validator)));
	}

	/**
	 * Reads the properties that no property of this schema is defined for, validates
	 * their values with the given validator and adds them to the produced map after the
	 * defined properties. The violations of the value for the key {@code key} are located
	 * under {@code key}.
	 *
	 * <p>
	 * The rest is defined last: the returned builder only builds the validator, which
	 * cannot reject unknown properties, of which there is none.
	 * @param <A> type of the values validated by {@code valueValidator}
	 * @param valueValidator value validator for each value
	 * @return a builder that creates the validator
	 */
	public <A> Complete<V> rest(ValueValidator<A, ? extends V> valueValidator) {
		final List<ArgumentDefinition<?, ?>> definitions = new ArrayList<>(this.definitions);
		definitions.add(ArgumentDefinition.rest(valueValidator));
		return new Complete<>(definitions);
	}

	/**
	 * Creates the validator that reads the properties of this schema from a map and
	 * produces the map of their validated values, as the class states.
	 * @return a validator that ignores the keys that no property is defined for, and that
	 * can be told to reject them
	 * @throws IllegalStateException if the input type of the validator of a property is
	 * unknown (see {@link ValueValidator#untyped(Class)})
	 */
	public ObjectInputValidator.IgnoringUnknown<Map<String, V>> build() {
		return build(this.definitions);
	}

	private List<ArgumentDefinition<?, ?>> with(String name, ArgumentDefinition<?, ?> definition) {
		if (this.definitions.stream().anyMatch(defined -> name.equals(defined.name()))) {
			throw new IllegalArgumentException("The property '" + name + "' is defined twice.");
		}
		final List<ArgumentDefinition<?, ?>> definitions = new ArrayList<>(this.definitions);
		definitions.add(definition);
		return definitions;
	}

	/**
	 * Returns the definitions of this schema with the given one in place of the last.
	 */
	List<ArgumentDefinition<?, ?>> withLast(ArgumentDefinition<?, ?> definition) {
		final List<ArgumentDefinition<?, ?>> definitions = new ArrayList<>(this.definitions);
		definitions.set(definitions.size() - 1, definition);
		return definitions;
	}

	ArgumentDefinition<?, ?> last() {
		return this.definitions.get(this.definitions.size() - 1);
	}

	@SuppressWarnings("unchecked")
	private static <V extends @Nullable Object> ObjectInputValidator.IgnoringUnknown<Map<String, V>> build(
			List<ArgumentDefinition<?, ?>> definitions) {
		final List<ArgumentDefinition<?, ?>> defined = List.copyOf(definitions);
		final List<ObjectInputValidator.Part<?>> parts = new ArrayList<>(defined.size());
		for (int i = 0; i < defined.size(); i++) {
			parts.add(defined.get(i).part(i + 1));
		}
		return ObjectInputValidator.of(parts, values -> {
			final Map<String, V> map = new LinkedHashMap<>();
			for (int i = 0; i < defined.size(); i++) {
				final ArgumentDefinition<?, ?> definition = defined.get(i);
				final Object value = values.get(i);
				switch (definition.source()) {
					case PRESENCE -> {
						if (value instanceof Presence<?>presence && !presence.isAbsent()) {
							map.put(definition.name(), (V) presence.orElse(null));
						}
					}
					case REST -> map.putAll((Map<String, V>) value);
					default -> map.put(definition.name(), (V) value);
				}
			}
			return Collections.unmodifiableMap(map);
		});
	}

	/**
	 * The schema after a property that requires its value is added: the modifiers apply
	 * to that property. What {@code null} produces is decided by the validator of the
	 * property, so this stage has no {@code nullable()} or {@code defaultValue}.
	 *
	 * @param <V> the type of the values of the produced map
	 * @since 0.18.0
	 */
	public static final class Keyed<V extends @Nullable Object> extends DynamicSchema<V>
			implements MessageModifiers<Keyed<V>> {

		Keyed(List<ArgumentDefinition<?, ?>> definitions) {
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
		 * Reports a missing value of the last added property with the given text instead
		 * of the message of {@code object.required}. The text is the message: a message
		 * formatter that has a message for {@code object.required} does not replace it
		 * (see {@link ViolationMessage#withText(String)}). The message key and the
		 * arguments are kept: <code>{0}</code> is the name or the label of the property.
		 * @param messageFormat the text of the message of a missing value
		 * @return a schema whose last property has no further modifier
		 */
		public DynamicSchema<V> required(String messageFormat) {
			return this.required(ViolationMessage.Default.OBJECT_REQUIRED.withText(messageFormat));
		}

		/**
		 * Reports a missing value of the last added property with the given message
		 * instead of the one of {@code object.required}.
		 * @param message the message of a missing value
		 * @return a schema whose last property has no further modifier
		 */
		public DynamicSchema<V> required(ViolationMessage message) {
			return new DynamicSchema<>(
					this.withLast(this.last().withMessages(this.last().messages().withRequired(message))));
		}

		/**
		 * Tells the absent key of the last added property from {@code null}: the property
		 * has no entry in the produced map if its key is absent, and a key that is
		 * present is validated as before, so that {@code null} is rejected. This is a
		 * property of a partial update that is left unchanged unless it is sent.
		 * @return a schema whose last property may be absent
		 */
		public DynamicSchema<V> presence() {
			return new DynamicSchema<>(this.withLast(this.last().withPresence()));
		}

	}

	/**
	 * The schema after a property whose validator accepts {@code null} is added: the
	 * modifiers apply to that property and tell its absent key from {@code null}.
	 *
	 * @param <V> the type of the values of the produced map
	 * @since 0.18.0
	 */
	public static final class NullAccepting<V extends @Nullable Object> extends DynamicSchema<V>
			implements MessageModifiers<NullAccepting<V>> {

		NullAccepting(List<ArgumentDefinition<?, ?>> definitions) {
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
		 * Requires the key of the last added property although its value may be
		 * {@code null}: an absent key is reported with {@code object.required}.
		 * @return a schema whose last property requires its key
		 */
		public DynamicSchema<V> required() {
			return new DynamicSchema<>(
					this.withLast(this.last().withStrictness(ObjectInputValidator.Strictness.REQUIRED)));
		}

		/**
		 * Requires the key of the last added property, as {@link #required()} does, and
		 * reports a missing value with the given text instead of the message of
		 * {@code object.required}, which a message formatter does not replace (see
		 * {@link ViolationMessage#withText(String)}).
		 * @param messageFormat the text of the message of a missing value
		 * @return a schema whose last property requires its key
		 */
		public DynamicSchema<V> required(String messageFormat) {
			return this.required(ViolationMessage.Default.OBJECT_REQUIRED.withText(messageFormat));
		}

		/**
		 * Requires the key of the last added property, as {@link #required()} does, and
		 * reports a missing value with the given message instead of the one of
		 * {@code object.required}.
		 * @param message the message of a missing value
		 * @return a schema whose last property requires its key
		 */
		public DynamicSchema<V> required(ViolationMessage message) {
			final ArgumentDefinition<?, ?> required = this.last()
				.withStrictness(ObjectInputValidator.Strictness.REQUIRED);
			return new DynamicSchema<>(this.withLast(required.withMessages(required.messages().withRequired(message))));
		}

		/**
		 * Rejects an explicit {@code null} of the last added property with
		 * {@code object.notNull} although its key may be absent: only an absent key is
		 * given to the validator as {@code null}.
		 * @return a schema whose last property rejects an explicit {@code null}
		 */
		public DynamicSchema<V> rejectingNull() {
			return new DynamicSchema<>(
					this.withLast(this.last().withStrictness(ObjectInputValidator.Strictness.REJECTING_NULL)));
		}

		/**
		 * Tells the absent key of the last added property from {@code null}: the property
		 * has no entry in the produced map if its key is absent, and a key that is
		 * present is validated as before, so that {@code null} produces what the
		 * validator decides, such as {@code null} to clear a value.
		 * @return a schema whose last property may be absent
		 */
		public DynamicSchema<V> presence() {
			return new DynamicSchema<>(this.withLast(this.last().withPresence()));
		}

	}

	/**
	 * The schema after the rest of the properties is defined: no property can be added.
	 *
	 * @param <V> the type of the values of the produced map
	 * @since 0.18.0
	 */
	public static final class Complete<V extends @Nullable Object> {

		private final List<ArgumentDefinition<?, ?>> definitions;

		Complete(List<ArgumentDefinition<?, ?>> definitions) {
			this.definitions = definitions;
		}

		/**
		 * Creates the validator that reads the properties of the schema and the rest of
		 * the properties from a map and produces the map of their validated values.
		 * @return a validator to which no property is unknown
		 * @throws IllegalStateException if the input type of the validator of a property
		 * is unknown
		 */
		public ObjectInputValidator<Map<String, V>> build() {
			return DynamicSchema.build(this.definitions);
		}

	}

}
