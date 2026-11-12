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
import java.util.Objects;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

/**
 * Converts an input of unknown type, such as a node of a parsed JSON document, to the
 * type a validator expects. The rules are stated in
 * {@link ValueValidator#untyped(Class)}.
 */
final class UntypedInput {

	/** The result of a conversion that the input does not allow. */
	private static final Object MISMATCH = new Object();

	private static final Map<Class<?>, Function<String, Object>> PARSERS = Map.of(LocalDate.class, LocalDate::parse,
			LocalTime.class, LocalTime::parse, LocalDateTime.class, LocalDateTime::parse, OffsetDateTime.class,
			OffsetDateTime::parse, ZonedDateTime.class, ZonedDateTime::parse, Instant.class, Instant::parse, Year.class,
			Year::parse, YearMonth.class, YearMonth::parse);

	private UntypedInput() {
	}

	/**
	 * Returns a validator that converts its input to the given type and passes it to the
	 * given validator. {@code null} is passed as is. It keeps the description of the
	 * given validator.
	 */
	static <T, X> ValueValidator<Object, X> checked(Class<T> type, ValueValidator<T, X> validator) {
		return new Checked<>(type, validator);
	}

	private record Checked<T, X> (Class<T> type, ValueValidator<T, X> validator) implements ValueValidator<Object, X> {

		@Override
		public Validated<X> validate(@Nullable Object value, Locale locale, ConstraintContext constraintContext) {
			final Object converted = value == null ? null : convert(value, this.type);
			if (converted == MISMATCH) {
				return Validated.failureWith(mismatch(Objects.requireNonNull(value), this.type, locale));
			}
			return this.validator.validate(this.type.cast(converted), locale, constraintContext);
		}

		@Override
		public ValueDescription description() {
			return this.validator.description();
		}

		@Override
		public ValueValidator<Object, X> untyped() {
			return this;
		}

	}

	private static ConstraintViolation mismatch(Object value, Class<?> type, Locale locale) {
		final Object[] constants = type.getEnumConstants();
		final ViolationMessage message = constants != null ? ViolationMessage.Default.OBJECT_ONE_OF
				: ViolationMessage.Default.OBJECT_TYPE;
		return ConstraintViolation.builder()
			.name("")
			.messageKey(message.messageKey())
			.defaultMessageFormat(message.defaultMessageFormat())
			.argsWithPrependedNameAndAppendedViolatedValue(
					new Object[] { constants != null ? List.of(constants) : typeName(type) }, new ViolatedValue(value))
			.locale(locale)
			.build();
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
				return decimal.toBigIntegerExact();
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
