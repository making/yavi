/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.core.ValueValidator;
import org.jspecify.annotations.Nullable;

/**
 * Describes one argument added to an arguments validator builder: how it is validated and
 * what is known about it.
 *
 * <p>
 * The generated {@code ArgumentsNValidatorBuilder} classes keep one definition per
 * argument, so that the name, the kind and the optionality of every argument are
 * available when the validator is created.
 *
 * @param name the property name of the argument; {@code null} if the argument was added
 * only with a value validator
 * @param kind the kind of the argument
 * @param type the raw type of the argument as far as the builder method determines it;
 * {@code Object} if it is unknown
 * @param optional whether a missing ({@code null}) value is accepted without consulting
 * the validator of the argument
 * @param validator the validator applied to the argument
 * @param <A> the type of the argument
 * @param <R> the type of the validated value of the argument
 */
record ArgumentDefinition<A, R> (@Nullable String name, Kind kind, Class<?> type, boolean optional,
		ValueValidator<A, R> validator) {

	/**
	 * The kind of an argument.
	 */
	enum Kind {

		/**
		 * A single value. Its violations are reported with the names given by its
		 * validator.
		 */
		VALUE,
		/**
		 * A nested object. Its violations are located under the argument name.
		 */
		OBJECT,
		/**
		 * A list. The violations of its elements are located under the argument name and
		 * the element index.
		 */
		LIST,
		/**
		 * A map. The violations of its values are located under the argument name and the
		 * key.
		 */
		MAP

	}

	static <A, R> ArgumentDefinition<A, R> value(Class<?> type, ValueValidator<A, R> validator) {
		return new ArgumentDefinition<>(null, Kind.VALUE, type, false, validator);
	}

	static <A, R> ArgumentDefinition<A, R> value(String name, Class<?> type, ValueValidator<A, R> validator) {
		return new ArgumentDefinition<>(name, Kind.VALUE, type, false, validator);
	}

	static <A, R> ArgumentDefinition<A, R> object(String name, ValueValidator<A, R> validator) {
		return new ArgumentDefinition<>(name, Kind.OBJECT, Object.class, false, nested(name, validator));
	}

	static <E, R> ArgumentDefinition<List<E>, List<R>> list(String name, ValueValidator<E, R> elementValidator) {
		final ValueValidator<Iterable<E>, List<R>> lifted = elementValidator.liftList();
		return new ArgumentDefinition<>(name, Kind.LIST, List.class, false, nested(name, lifted::validate));
	}

	static <V, R> ArgumentDefinition<Map<String, V>, Map<String, R>> map(String name,
			ValueValidator<V, R> valueValidator) {
		return new ArgumentDefinition<>(name, Kind.MAP, Map.class, false, nested(name, valueValidator.liftMap()));
	}

	ArgumentDefinition<A, Optional<R>> asOptional() {
		return new ArgumentDefinition<>(this.name, this.kind, this.type, true, this.validator.optional());
	}

	ArgumentDefinition<A, R> asNullable() {
		return new ArgumentDefinition<>(this.name, this.kind, this.type, true, this.validator.nullable());
	}

	ArgumentDefinition<A, R> withDefault(R defaultValue) {
		return new ArgumentDefinition<>(this.name, this.kind, this.type, true,
				this.validator.defaultValue(defaultValue));
	}

	/**
	 * Returns a validator that rejects {@code null} with {@code object.notNull} under the
	 * given name and otherwise locates the violations of the given validator under that
	 * name.
	 */
	private static <A, R> ValueValidator<A, R> nested(String name, ValueValidator<A, R> validator) {
		return ObjectValidatorBuilder.<A>of(name, ObjectConstraint::notNull).build().andThen(validator.prefixed(name));
	}

}
