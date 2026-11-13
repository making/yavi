/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * A read-only description of the input that a validator expects.
 *
 * <p>
 * The description is a tree that tools can walk, for example to generate a JSON Schema
 * ({@code am.ik.yavi.jsonschema.JsonSchema}), a form or documentation. It is obtained
 * from {@link ValueValidator#description()} or from the {@code description()} method of
 * an {@code ArgumentsNValidator}. A validator that does not know its input, such as a
 * lambda, is described as {@link Unknown}; a description can be supplied with
 * {@link ValueValidator#describedAs(ValueDescription)}.
 *
 * <p>
 * The description of a recursive structure is not an endless tree: where the structure
 * refers to itself, the description holds a {@link Reference}, whose target is described
 * only when it is asked for.
 *
 * <p>
 * The description covers the constraints that always apply to the input. Constraints that
 * depend on a condition or a constraint group and rules on the produced value
 * ({@code constraintOnTarget}) are not part of it. The constraints of a list or a map are
 * checked on the validated elements or values, whose number is the one of the input.
 *
 * <p>
 * In YAVI a value accepts {@code null} unless a constraint rejects it;
 * {@link #acceptsNull()} tells whether the described input does, and
 * {@link #acceptsAbsent()} whether a property with the described value may be omitted.
 *
 * @since 0.18.0
 * @see ConstraintDescription
 */
public sealed interface ValueDescription {

	/**
	 * A single value validated by constraints.
	 *
	 * @param name the name under which the violations of the value are reported; empty if
	 * they are reported on the value itself
	 * @param type the raw type of the value as far as the validator determines it;
	 * {@code Object} if it is unknown
	 * @param constraints the constraints applied to the value, in the order they are
	 * checked; unmodifiable
	 */
	record Value(String name, Class<?> type, List<ConstraintDescription> constraints) implements ValueDescription {

		/**
		 * Creates the description of a single value.
		 */
		public Value {
			constraints = List.copyOf(constraints);
		}

		/**
		 * Returns the description of a single value of the given type validated by the
		 * constraints of the given validator.
		 *
		 * <p>
		 * The name is the one of the first constrained property of the validator.
		 * Conditional constraints and nested or collection validators are not described.
		 * @param type the raw type of the value
		 * @param validator the validator whose constraints are applied to the value
		 * @return the description of the value
		 */
		public static Value of(Class<?> type, Validator<?> validator) {
			final List<String> names = new ArrayList<>();
			final List<ConstraintDescription> constraints = new ArrayList<>();
			validator.forEachPredicates(predicates -> {
				names.add(predicates.name());
				predicates.predicates().forEach(predicate -> constraints.add(ConstraintDescription.of(predicate)));
			});
			return new Value(names.isEmpty() ? "" : names.get(0), type, constraints);
		}
	}

	/**
	 * An object with named properties, such as the arguments of an
	 * {@code ArgumentsNValidator} or the keys of a validated map.
	 *
	 * @param properties the properties in the order of the arguments; unmodifiable
	 * @param rest the description of the value of every other property of the object;
	 * {@code null} if the other properties are not read, {@link Never} if they are
	 * rejected and {@link Unknown} if they are read by a validator that does not describe
	 * them
	 */
	record Properties(List<Property> properties, @Nullable ValueDescription rest) implements ValueDescription {

		/**
		 * Creates the description of an object.
		 */
		public Properties {
			properties = List.copyOf(properties);
		}

		/**
		 * Creates the description of an object whose other properties are not read.
		 * @param properties the properties in the order of the arguments
		 */
		public Properties(List<Property> properties) {
			this(properties, null);
		}

		/**
		 * Returns whether the described properties are the only ones that the object is
		 * read for, so that a validator of the same object may read the others.
		 * @return {@code true} unless the other properties are validated or unknown
		 */
		public boolean isClosed() {
			return this.rest == null || this.rest instanceof Never;
		}

		/**
		 * Returns the description of an object whose properties are the given values in
		 * order, such as the arguments of an {@code ArgumentsNValidator}.
		 *
		 * <p>
		 * A property takes the name under which the violations of its value are reported
		 * (see {@link Value#name()}, also inside {@link Optional}, {@link NotNull} and
		 * {@link Presence}). If that name is empty or unknown, the property is named
		 * {@code argN}, where {@code N} is its position starting at 1.
		 * @param values the descriptions of the property values
		 * @return the description of the object
		 */
		public static Properties ofArguments(ValueDescription... values) {
			final List<Property> properties = new ArrayList<>(values.length);
			for (int i = 0; i < values.length; i++) {
				ValueDescription value = values[i];
				while (true) {
					if (value instanceof Optional optional) {
						value = optional.value();
					}
					else if (value instanceof NotNull notNull) {
						value = notNull.value();
					}
					else if (value instanceof Presence presence) {
						value = presence.value();
					}
					else {
						break;
					}
				}
				final String name = value instanceof Value v ? v.name() : "";
				properties.add(new Property(name.isEmpty() ? "arg" + (i + 1) : name, values[i]));
			}
			return new Properties(properties);
		}
	}

	/**
	 * A list or another iterable whose elements are validated one by one. The violations
	 * of an element are located under its index.
	 *
	 * @param element the description of every element
	 * @param constraints the constraints applied to the list of the validated elements,
	 * in the order they are checked; unmodifiable. A constraint
	 * {@linkplain ConstraintDescription#rejectsNull() rejects null} if it rejects an
	 * empty list, as which {@code null} is validated unless it is rejected before
	 */
	record Elements(ValueDescription element, List<ConstraintDescription> constraints) implements ValueDescription {

		/**
		 * Creates the description of a list.
		 */
		public Elements {
			constraints = List.copyOf(constraints);
		}

		/**
		 * Creates the description of a list without constraints.
		 * @param element the description of every element
		 */
		public Elements(ValueDescription element) {
			this(element, List.of());
		}
	}

	/**
	 * A map with string keys whose values are validated one by one. The violations of a
	 * value are located under its key.
	 *
	 * @param value the description of every value
	 * @param constraints the constraints applied to the map of the validated values, in
	 * the order they are checked; unmodifiable. A constraint
	 * {@linkplain ConstraintDescription#rejectsNull() rejects null} if it rejects an
	 * empty map, as which {@code null} is validated unless it is rejected before
	 */
	record Entries(ValueDescription value, List<ConstraintDescription> constraints) implements ValueDescription {

		/**
		 * Creates the description of a map.
		 */
		public Entries {
			constraints = List.copyOf(constraints);
		}

		/**
		 * Creates the description of a map without constraints.
		 * @param value the description of every value
		 */
		public Entries(ValueDescription value) {
			this(value, List.of());
		}
	}

	/**
	 * A value that must satisfy at least one of the alternatives.
	 *
	 * @param alternatives the alternatives in the order they are tried; unmodifiable
	 * @see ValueValidator#anyOf(ValueValidator[])
	 */
	record AnyOf(List<ValueDescription> alternatives) implements ValueDescription {

		/**
		 * Creates the description of alternatives.
		 */
		public AnyOf {
			alternatives = List.copyOf(alternatives);
		}
	}

	/**
	 * A value for which {@code null} is rejected with {@code object.notNull} before it is
	 * validated as described, whatever the described value accepts.
	 *
	 * @param value the description of a value other than {@code null}
	 */
	record NotNull(ValueDescription value) implements ValueDescription {
	}

	/**
	 * A value for which {@code null} is accepted without being validated, whatever the
	 * described value accepts. A validator that
	 * {@linkplain ValueValidator#recover(Object) recovers} is described this way too: the
	 * described value is the one it expects, although it produces the default value for
	 * any other.
	 *
	 * @param value the description of a value other than {@code null}
	 * @param defaultValue the value produced for {@code null}; {@code null} if there is
	 * no default value
	 * @see ValueValidator#optional()
	 * @see ValueValidator#nullable()
	 * @see ValueValidator#defaultValue(Object)
	 * @see ValueValidator#recover(Object)
	 */
	record Optional(ValueDescription value, @Nullable Object defaultValue) implements ValueDescription {
	}

	/**
	 * The value of a property that may be omitted whatever the described value accepts,
	 * and whose absence is told from {@code null}: an absent property is not validated,
	 * and one that is present is validated as described, {@code null} included.
	 *
	 * @param value the description of the value of a property that is present
	 * @see am.ik.yavi.core.Presence
	 * @see ValueValidator#liftPresence()
	 */
	record Presence(ValueDescription value) implements ValueDescription {
	}

	/**
	 * A value about which nothing is known.
	 */
	record Unknown() implements ValueDescription {
	}

	/**
	 * A value that is never accepted. As the {@linkplain Properties#rest() rest} of an
	 * object, it tells that a property that is not described is rejected.
	 */
	record Never() implements ValueDescription {
	}

	/**
	 * A value described elsewhere: the input of a validator that is referred to before it
	 * is built, as {@link ValueValidator#lazy(java.util.function.Supplier)} and
	 * {@link ValueValidator#recursive(java.util.function.Function)} do for a structure
	 * that contains itself. The target is described when it is asked for, so that the
	 * description of such a structure ends here instead of repeating itself.
	 *
	 * <p>
	 * Unlike the other descriptions, a reference is equal only to itself: the one
	 * reference of a lazy validator stands for that validator wherever its description
	 * appears, which is how a tool that walks the description recognizes a target it has
	 * already seen.
	 *
	 * @see ValueValidator#lazy(String, java.util.function.Supplier)
	 */
	final class Reference implements ValueDescription {

		private final String name;

		private final Supplier<? extends ValueDescription> target;

		/**
		 * Creates a reference.
		 * @param name the name of the target, for a tool that lists the targets by name,
		 * such as the {@code $defs} of a JSON Schema; empty if it has none
		 * @param target the supplier of the description of the target, called every time
		 * the target is asked for
		 */
		public Reference(String name, Supplier<? extends ValueDescription> target) {
			this.name = Objects.requireNonNull(name, "'name' must not be null.");
			this.target = Objects.requireNonNull(target, "'target' must not be null.");
		}

		/**
		 * Returns the name of the target.
		 * @return the name; empty if the target has none
		 */
		public String name() {
			return this.name;
		}

		/**
		 * Returns the description of the target, which may hold this reference again.
		 * @return the description of the target
		 * @throws IllegalStateException if the target is a validator that is not built
		 * yet
		 */
		public ValueDescription target() {
			return this.target.get();
		}

		@Override
		public String toString() {
			return "Reference[name=" + this.name + "]";
		}

	}

	/**
	 * A named property of an object.
	 *
	 * @param name the property name
	 * @param value the description of the property value
	 */
	record Property(String name, ValueDescription value) {
	}

	/**
	 * Returns whether the described input accepts {@code null}.
	 *
	 * <p>
	 * A {@link Value} accepts {@code null} unless one of its constraints rejects it,
	 * {@link Elements} and {@link Entries} accept {@code null} as an empty list or map
	 * unless one of their constraints rejects it, an object with {@link Properties} does
	 * not accept {@code null}, and a {@link Presence} and a {@link Reference} accept what
	 * their value or target accepts.
	 * @return {@code true} if {@code null} is accepted or nothing is known about the
	 * input
	 */
	default boolean acceptsNull() {
		return switch (this) {
			case Value value -> value.constraints().stream().noneMatch(ConstraintDescription::rejectsNull);
			case AnyOf anyOf -> anyOf.alternatives().stream().anyMatch(ValueDescription::acceptsNull);
			case NotNull notNull -> false;
			case Properties properties -> false;
			case Elements elements -> elements.constraints().stream().noneMatch(ConstraintDescription::rejectsNull);
			case Entries entries -> entries.constraints().stream().noneMatch(ConstraintDescription::rejectsNull);
			case Optional optional -> true;
			case Presence presence -> presence.value().acceptsNull();
			case Unknown unknown -> true;
			case Never never -> false;
			case Reference reference -> reference.target().acceptsNull();
		};
	}

	/**
	 * Returns whether a property with the described value may be omitted from its object:
	 * the value accepts {@code null}, as which an absent property is validated, or it is
	 * a {@link Presence}, which an absent property is not validated for.
	 * @return {@code true} if the property is not required
	 */
	default boolean acceptsAbsent() {
		return switch (this) {
			case Presence presence -> true;
			case AnyOf anyOf -> anyOf.alternatives().stream().anyMatch(ValueDescription::acceptsAbsent);
			case Reference reference -> reference.target().acceptsAbsent();
			default -> this.acceptsNull();
		};
	}

}
