/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.ArrayList;
import java.util.List;

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
 * The description covers the constraints that always apply to the input. Constraints that
 * depend on a condition or a constraint group and rules on the produced value
 * ({@code constraintOnTarget}) are not part of it.
 *
 * <p>
 * In YAVI a value accepts {@code null} unless a constraint rejects it;
 * {@link #acceptsNull()} tells whether the described input does.
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
	 * {@code ArgumentsNValidator}.
	 *
	 * @param properties the properties in the order of the arguments; unmodifiable
	 */
	record Properties(List<Property> properties) implements ValueDescription {

		/**
		 * Creates the description of an object.
		 */
		public Properties {
			properties = List.copyOf(properties);
		}

		/**
		 * Returns the description of an object whose properties are the given values in
		 * order, such as the arguments of an {@code ArgumentsNValidator}.
		 *
		 * <p>
		 * A property takes the name under which the violations of its value are reported
		 * (see {@link Value#name()}, also inside {@link Optional} and {@link NotNull}).
		 * If that name is empty or unknown, the property is named {@code argN}, where
		 * {@code N} is its position starting at 1.
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
	 */
	record Elements(ValueDescription element) implements ValueDescription {
	}

	/**
	 * A map with string keys whose values are validated one by one. The violations of a
	 * value are located under its key.
	 *
	 * @param value the description of every value
	 */
	record Entries(ValueDescription value) implements ValueDescription {
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
	 * described value accepts.
	 *
	 * @param value the description of a value other than {@code null}
	 * @param defaultValue the value produced for {@code null}; {@code null} if there is
	 * no default value
	 * @see ValueValidator#optional()
	 * @see ValueValidator#nullable()
	 * @see ValueValidator#defaultValue(Object)
	 */
	record Optional(ValueDescription value, @Nullable Object defaultValue) implements ValueDescription {
	}

	/**
	 * A value about which nothing is known.
	 */
	record Unknown() implements ValueDescription {
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
	 * Returns whether the described input accepts {@code null}. A property whose value
	 * accepts {@code null} may be omitted.
	 *
	 * <p>
	 * A {@link Value} accepts {@code null} unless one of its constraints rejects it,
	 * {@link Elements} and {@link Entries} accept {@code null} as an empty list or map,
	 * and an object with {@link Properties} does not accept {@code null}.
	 * @return {@code true} if {@code null} is accepted or nothing is known about the
	 * input
	 */
	default boolean acceptsNull() {
		return switch (this) {
			case Value value -> value.constraints().stream().noneMatch(ConstraintDescription::rejectsNull);
			case AnyOf anyOf -> anyOf.alternatives().stream().anyMatch(ValueDescription::acceptsNull);
			case NotNull notNull -> false;
			case Properties properties -> false;
			case Elements elements -> true;
			case Entries entries -> true;
			case Optional optional -> true;
			case Unknown unknown -> true;
		};
	}

}
