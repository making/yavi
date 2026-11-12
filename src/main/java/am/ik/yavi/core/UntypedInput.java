/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

/**
 * Converts an input of unknown type, such as a node of a parsed JSON document, to the
 * type a validator expects. The node is read with an {@link InputReader} and the rules
 * are stated in {@link ValueValidator#untyped(Class)}.
 */
final class UntypedInput {

	/** The result of a conversion that the input does not allow. */
	private static final Object MISMATCH = new Object();

	/**
	 * The largest exponent of a number taken as a {@code BigInteger}. Writing out the
	 * digits of {@code 1e1000000000} would exhaust the memory.
	 */
	private static final int MAX_INTEGER_EXPONENT = 10_000;

	private static final Map<Class<?>, Function<String, Object>> PARSERS = Map.of(LocalDate.class, LocalDate::parse,
			LocalTime.class, LocalTime::parse, LocalDateTime.class, LocalDateTime::parse, OffsetDateTime.class,
			OffsetDateTime::parse, ZonedDateTime.class, ZonedDateTime::parse, Instant.class, Instant::parse, Year.class,
			Year::parse, YearMonth.class, YearMonth::parse);

	private UntypedInput() {
	}

	/**
	 * Returns a validator that reads its input with the given reader, converts the value
	 * to the given type and passes it to the given validator. A null node is passed as
	 * {@code null}. It keeps the description of the given validator.
	 */
	static <N, T, X> ValueValidator<N, X> checked(InputReader<N> reader, Class<T> type,
			ValueValidator<T, X> validator) {
		return new Checked<>(reader, type, validator);
	}

	private record Checked<N, T, X> (InputReader<N> reader, Class<T> type,
			ValueValidator<T, X> validator) implements ValueValidator<N, X> {

		@Override
		public Validated<X> validate(@Nullable N node, Locale locale, ConstraintContext constraintContext) {
			final N present = orNull(this.reader, node);
			final Object converted = present == null ? null : convert(this.reader.value(present, this.type), this.type);
			if (present != null && converted == MISMATCH) {
				// the violated value is the node as it is, not as it was read for the
				// type
				final Object value = this.reader.value(present);
				final Object[] constants = this.type.getEnumConstants();
				return Validated.failureWith(constants != null
						? violation(ViolationMessage.Default.OBJECT_ONE_OF, List.of(constants), value, locale)
						: mismatch(typeName(this.type), value, locale));
			}
			return this.validator.validate(this.type.cast(converted), locale, constraintContext);
		}

		@Override
		public ValueDescription description() {
			return this.validator.description();
		}

		@Override
		public <M> ValueValidator<M, X> readingWith(InputReader<M> reader) {
			return new Checked<>(reader, this.type, this.validator);
		}

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

	private static Object convert(Object value, Class<?> type) {
		if (type == Map.class) {
			return value instanceof Map<?, ?>map && map.keySet().stream().allMatch(String.class::isInstance) ? value
					: MISMATCH;
		}
		if (type.isInstance(value)) {
			return value;
		}
		if (value instanceof Number number) {
			return convertNumber(number, type);
		}
		if (value instanceof String string) {
			final Object[] constants = type.getEnumConstants();
			if (constants != null) {
				for (Object constant : constants) {
					if (((Enum<?>) constant).name().equals(string)) {
						return constant;
					}
				}
				return MISMATCH;
			}
			final Function<String, Object> parser = PARSERS.get(type);
			if (parser != null) {
				try {
					return parser.apply(string);
				}
				catch (DateTimeException e) {
					return MISMATCH;
				}
			}
		}
		return MISMATCH;
	}

	/**
	 * Converts a number to another numeric type if the value is kept: an integral type
	 * takes only an integral value in its range, a floating-point type takes any value in
	 * its range.
	 */
	private static Object convertNumber(Number number, Class<?> type) {
		final BigDecimal decimal = decimal(number);
		if (decimal == null) {
			return MISMATCH;
		}
		try {
			if (type == Integer.class) {
				return decimal.intValueExact();
			}
			if (type == Long.class) {
				return decimal.longValueExact();
			}
			if (type == Short.class) {
				return decimal.shortValueExact();
			}
			if (type == Byte.class) {
				return decimal.byteValueExact();
			}
			if (type == BigInteger.class) {
				return decimal.scale() < -MAX_INTEGER_EXPONENT ? MISMATCH : decimal.toBigIntegerExact();
			}
			if (type == Year.class) {
				return Year.of(decimal.intValueExact());
			}
		}
		catch (ArithmeticException | DateTimeException e) {
			return MISMATCH;
		}
		if (type == BigDecimal.class) {
			return decimal;
		}
		if (type == Double.class) {
			final double converted = decimal.doubleValue();
			return Double.isInfinite(converted) ? MISMATCH : converted;
		}
		if (type == Float.class) {
			final float converted = decimal.floatValue();
			return Float.isInfinite(converted) ? MISMATCH : converted;
		}
		return MISMATCH;
	}

	private static @Nullable BigDecimal decimal(Number number) {
		if (number instanceof BigDecimal decimal) {
			return decimal;
		}
		if (number instanceof BigInteger integer) {
			return new BigDecimal(integer);
		}
		if (number instanceof Integer || number instanceof Long || number instanceof Short || number instanceof Byte) {
			return BigDecimal.valueOf(number.longValue());
		}
		if (number instanceof Double || number instanceof Float) {
			// the shortest decimal that identifies the value: 0.1f is 0.1
			return Double.isFinite(number.doubleValue()) ? new BigDecimal(number.toString()) : null;
		}
		return null;
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
