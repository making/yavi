/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/**
 * Converts an input of unknown type, such as a node of a parsed JSON document, to the
 * type a validator expects. The node is read with an {@link InputReader}, converted with
 * one of {@link UntypedConversions} and the rules are stated in
 * {@link ValueValidator#untyped(Class)}.
 */
final class UntypedInput {

	private UntypedInput() {
	}

	/**
	 * Returns a validator that reads its input with the given reader, converts the value
	 * to the given type as {@link ValueValidator#untyped(Class)} states and passes it to
	 * the given validator. A null node is passed as {@code null}. It keeps the
	 * description of the given validator.
	 */
	static <N, T, X extends @Nullable Object> ValueValidator<N, X> checked(InputReader<N> reader, Class<T> type,
			ValueValidator<T, X> validator) {
		return checked(reader, type, UntypedConversions.of(type), validator);
	}

	/**
	 * Returns a validator that reads its input with the given reader, converts the value
	 * to the given type with the given conversion and passes it to the given validator. A
	 * null node is passed as {@code null}. It keeps the description of the given
	 * validator.
	 */
	static <N, T, X extends @Nullable Object> ValueValidator<N, X> checked(InputReader<N> reader, Class<T> type,
			Function<Object, ? extends @Nullable T> conversion, ValueValidator<T, X> validator) {
		return new Checked<>(reader, type, conversion, validator, "");
	}

	private record Checked<N, T, X extends @Nullable Object> (InputReader<N> reader, Class<T> type,
			Function<Object, ? extends @Nullable T> conversion, ValueValidator<T, X> validator,
			String name) implements ValueValidator<N, X> {

		@Override
		public Validated<X> validate(@Nullable N node, Locale locale, ConstraintContext constraintContext) {
			final N present = orNull(this.reader, node);
			final T converted = present == null ? null : this.conversion.apply(this.reader.value(present, this.type));
			if (present != null && converted == null) {
				// the violated value is the node as it is, not as it was read for the
				// type
				final Object value = this.reader.value(present);
				final Object[] constants = this.type.getEnumConstants();
				return Validated.failureWith((constants != null
						? violation(ViolationMessage.Default.OBJECT_ONE_OF, List.of(constants), value, locale)
						: mismatch(typeName(this.type), value, locale))
					.prefixed(this.name));
			}
			return this.validator.validate(this.type.cast(converted), locale, constraintContext);
		}

		@Override
		public ValueDescription description() {
			return this.validator.description();
		}

		@Override
		public <M> ValueValidator<M, X> readingWith(InputReader<M> reader) {
			return new Checked<>(reader, this.type, this.conversion, this.validator, this.name);
		}

		@Override
		public ValueValidator<N, X> named(String name) {
			return new Checked<>(this.reader, this.type, this.conversion, this.validator.named(name), name);
		}

	}

	/**
	 * Returns the elements of the given text that the given pattern separates. A
	 * separator at the end of the text ends the last element and starts no other.
	 */
	static List<String> split(String text, Pattern separator) {
		final String[] parts = separator.split(text, -1);
		final int length = parts.length > 1 && parts[parts.length - 1].isEmpty() ? parts.length - 1 : parts.length;
		return List.of(parts).subList(0, length);
	}

	/**
	 * Returns the node unless it is {@code null} or a null node of the reader.
	 */
	static <N> @Nullable N orNull(InputReader<N> reader, @Nullable N node) {
		return node == null || reader.isNull(node) ? null : node;
	}

	/**
	 * Returns the violation of a node that is not of the type with the given name. It is
	 * reported on the validated value itself.
	 */
	static <N> ConstraintViolation mismatch(String typeName, InputReader<N> reader, N node, Locale locale) {
		return mismatch(typeName, reader.value(node), locale);
	}

	private static ConstraintViolation mismatch(String typeName, @Nullable Object value, Locale locale) {
		return violation(ViolationMessage.Default.OBJECT_TYPE, typeName, value, locale);
	}

	private static ConstraintViolation violation(ViolationMessage message, Object arg, @Nullable Object value,
			Locale locale) {
		return ConstraintViolation.of(message, value, locale, arg);
	}

	private static String typeName(Class<?> type) {
		if (type == String.class) {
			return "string";
		}
		if (type == Boolean.class) {
			return "boolean";
		}
		if (type == Integer.class || type == Long.class || type == Short.class || type == Byte.class
				|| type == BigInteger.class) {
			return "integer";
		}
		if (Number.class.isAssignableFrom(type)) {
			return "number";
		}
		if (Map.class.isAssignableFrom(type)) {
			return "object";
		}
		if (Iterable.class.isAssignableFrom(type)) {
			return "array";
		}
		return type.getSimpleName();
	}

}
