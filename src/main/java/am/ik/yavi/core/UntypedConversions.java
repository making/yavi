/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/**
 * The conversions by which {@link ValueValidator#untyped(Class)} reads an input of
 * unknown type as the types it names other than {@code Map}, one per type, to be given to
 * {@link ValueValidator#untyped(Class, Function)}.
 *
 * <p>
 * A conversion takes an input other than {@code null} and returns it as its type, or
 * {@code null} if the input does not represent that type, by the rules that
 * {@link ValueValidator#untyped(Class)} states. {@code untyped(Class)} chooses the
 * conversion by the given type when it is called, so it holds the conversion of every
 * type. A caller that knows the type names the conversion of that type instead, so an
 * application analyzed as a closed world, such as a native image, keeps only the
 * conversions it names. A conversion is also the base of a conversion of one's own:
 *
 * <pre>{@code
 * ValueValidator<Object, LocalDate> day = ValueValidator.<LocalDate>passThrough()
 *     .untyped(LocalDate.class, value -> value instanceof Long epochDay ? LocalDate.ofEpochDay(epochDay)
 *         : UntypedConversions.toLocalDate().apply(value));
 * }</pre>
 *
 * <p>
 * This class only provides static methods; it is not instantiated.
 *
 * @since 0.18.1
 */
public final class UntypedConversions {

	/**
	 * The largest exponent of a number taken as a {@code BigInteger}. Writing out the
	 * digits of {@code 1e1000000000} would exhaust the memory.
	 */
	private static final int MAX_INTEGER_EXPONENT = 10_000;

	private UntypedConversions() {
	}

	/**
	 * Returns the conversion that takes an instance of the given type and nothing else,
	 * which is the conversion of a type that {@link ValueValidator#untyped(Class)} does
	 * not name, such as {@code String} or {@code Boolean}.
	 * @param type the type
	 * @param <T> the type
	 * @return the conversion to the given type
	 */
	public static <T> Function<Object, @Nullable T> instanceOf(Class<T> type) {
		return value -> type.isInstance(value) ? type.cast(value) : null;
	}

	/**
	 * Returns the conversion to the given enum type: an instance of it, or the name of a
	 * constant.
	 * @param type the enum type
	 * @param <E> the enum type
	 * @return the conversion to the given enum type
	 */
	public static <E extends Enum<E>> Function<Object, @Nullable E> toEnum(Class<E> type) {
		return constants(type);
	}

	/**
	 * Returns the conversion to {@code Integer}: a number whose value is an integer in
	 * its range.
	 * @return the conversion to {@code Integer}
	 */
	public static Function<Object, @Nullable Integer> toInteger() {
		return value -> {
			if (value instanceof Integer integer) {
				return integer;
			}
			final Long integral = integral(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
			return integral == null ? null : integral.intValue();
		};
	}

	/**
	 * Returns the conversion to {@code Long}: a number whose value is an integer in its
	 * range.
	 * @return the conversion to {@code Long}
	 */
	public static Function<Object, @Nullable Long> toLong() {
		return value -> value instanceof Long integer ? integer : integral(value, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	/**
	 * Returns the conversion to {@code Short}: a number whose value is an integer in its
	 * range.
	 * @return the conversion to {@code Short}
	 */
	public static Function<Object, @Nullable Short> toShort() {
		return value -> {
			if (value instanceof Short integer) {
				return integer;
			}
			final Long integral = integral(value, Short.MIN_VALUE, Short.MAX_VALUE);
			return integral == null ? null : integral.shortValue();
		};
	}

	/**
	 * Returns the conversion to {@code Byte}: a number whose value is an integer in its
	 * range.
	 * @return the conversion to {@code Byte}
	 */
	public static Function<Object, @Nullable Byte> toByte() {
		return value -> {
			if (value instanceof Byte integer) {
				return integer;
			}
			final Long integral = integral(value, Byte.MIN_VALUE, Byte.MAX_VALUE);
			return integral == null ? null : integral.byteValue();
		};
	}

	/**
	 * Returns the conversion to {@code BigInteger}: a number whose value is an integer,
	 * written with an exponent of at most 10000.
	 * @return the conversion to {@code BigInteger}
	 */
	public static Function<Object, @Nullable BigInteger> toBigInteger() {
		return value -> {
			if (value instanceof BigInteger integer) {
				return integer;
			}
			if (isIntegral(value)) {
				return BigInteger.valueOf(((Number) value).longValue());
			}
			final BigDecimal decimal = decimal(value);
			if (decimal == null || decimal.scale() < -MAX_INTEGER_EXPONENT) {
				return null;
			}
			try {
				return decimal.toBigIntegerExact();
			}
			catch (ArithmeticException e) {
				return null;
			}
		};
	}

	/**
	 * Returns the conversion to {@code BigDecimal}: a finite number.
	 * @return the conversion to {@code BigDecimal}
	 */
	public static Function<Object, @Nullable BigDecimal> toBigDecimal() {
		return UntypedConversions::decimal;
	}

	/**
	 * Returns the conversion to {@code Double}: a number in its range.
	 * @return the conversion to {@code Double}
	 */
	public static Function<Object, @Nullable Double> toDouble() {
		return value -> {
			if (value instanceof Double number) {
				return number;
			}
			final BigDecimal decimal = decimal(value);
			if (decimal == null) {
				return null;
			}
			final double converted = decimal.doubleValue();
			return Double.isInfinite(converted) ? null : converted;
		};
	}

	/**
	 * Returns the conversion to {@code Float}: a number in its range.
	 * @return the conversion to {@code Float}
	 */
	public static Function<Object, @Nullable Float> toFloat() {
		return value -> {
			if (value instanceof Float number) {
				return number;
			}
			final BigDecimal decimal = decimal(value);
			if (decimal == null) {
				return null;
			}
			final float converted = decimal.floatValue();
			return Float.isInfinite(converted) ? null : converted;
		};
	}

	/**
	 * Returns the conversion to {@code LocalDate}: an ISO-8601 string, or a
	 * {@code java.sql.Date}.
	 * @return the conversion to {@code LocalDate}
	 */
	public static Function<Object, @Nullable LocalDate> toLocalDate() {
		return value -> {
			if (value instanceof LocalDate date) {
				return date;
			}
			if (value instanceof java.util.Date date) {
				return isSqlType(date) ? SqlTypes.toLocalDate(date) : null;
			}
			return parse(value, LocalDate::parse);
		};
	}

	/**
	 * Returns the conversion to {@code LocalTime}: an ISO-8601 string, or a
	 * {@code java.sql.Time}.
	 * @return the conversion to {@code LocalTime}
	 */
	public static Function<Object, @Nullable LocalTime> toLocalTime() {
		return value -> {
			if (value instanceof LocalTime time) {
				return time;
			}
			if (value instanceof java.util.Date date) {
				return isSqlType(date) ? SqlTypes.toLocalTime(date) : null;
			}
			return parse(value, LocalTime::parse);
		};
	}

	/**
	 * Returns the conversion to {@code LocalDateTime}: an ISO-8601 string, or a
	 * {@code java.sql.Timestamp}.
	 * @return the conversion to {@code LocalDateTime}
	 */
	public static Function<Object, @Nullable LocalDateTime> toLocalDateTime() {
		return value -> {
			if (value instanceof LocalDateTime dateTime) {
				return dateTime;
			}
			if (value instanceof java.util.Date date) {
				return isSqlType(date) ? SqlTypes.toLocalDateTime(date) : null;
			}
			return parse(value, LocalDateTime::parse);
		};
	}

	/**
	 * Returns the conversion to {@code OffsetDateTime}: an ISO-8601 string.
	 * @return the conversion to {@code OffsetDateTime}
	 */
	public static Function<Object, @Nullable OffsetDateTime> toOffsetDateTime() {
		return value -> value instanceof OffsetDateTime dateTime ? dateTime : parse(value, OffsetDateTime::parse);
	}

	/**
	 * Returns the conversion to {@code ZonedDateTime}: an ISO-8601 string.
	 * @return the conversion to {@code ZonedDateTime}
	 */
	public static Function<Object, @Nullable ZonedDateTime> toZonedDateTime() {
		return value -> value instanceof ZonedDateTime dateTime ? dateTime : parse(value, ZonedDateTime::parse);
	}

	/**
	 * Returns the conversion to {@code Instant}: an ISO-8601 string, or a
	 * {@code java.sql.Timestamp}.
	 * @return the conversion to {@code Instant}
	 */
	public static Function<Object, @Nullable Instant> toInstant() {
		return value -> {
			if (value instanceof Instant instant) {
				return instant;
			}
			if (value instanceof java.util.Date date) {
				return isSqlType(date) ? SqlTypes.toInstant(date) : null;
			}
			return parse(value, Instant::parse);
		};
	}

	/**
	 * Returns the conversion to {@code Year}: an ISO-8601 string, or a number whose value
	 * is an integer in the range of a year.
	 * @return the conversion to {@code Year}
	 */
	public static Function<Object, @Nullable Year> toYear() {
		return value -> {
			if (value instanceof Year year) {
				return year;
			}
			if (value instanceof Number) {
				final Long integral = integral(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
				try {
					return integral == null ? null : Year.of(integral.intValue());
				}
				catch (DateTimeException e) {
					return null;
				}
			}
			return parse(value, Year::parse);
		};
	}

	/**
	 * Returns the conversion to {@code YearMonth}: an ISO-8601 string.
	 * @return the conversion to {@code YearMonth}
	 */
	public static Function<Object, @Nullable YearMonth> toYearMonth() {
		return value -> value instanceof YearMonth yearMonth ? yearMonth : parse(value, YearMonth::parse);
	}

	/**
	 * Returns the conversion to {@code Duration}: an ISO-8601 string such as
	 * {@code PT1H30M}.
	 * @return the conversion to {@code Duration}
	 */
	public static Function<Object, @Nullable Duration> toDuration() {
		return value -> value instanceof Duration duration ? duration : parse(value, Duration::parse);
	}

	/**
	 * Returns the conversion to {@code Period}: an ISO-8601 string such as {@code P1Y2M}.
	 * @return the conversion to {@code Period}
	 */
	public static Function<Object, @Nullable Period> toPeriod() {
		return value -> value instanceof Period period ? period : parse(value, Period::parse);
	}

	/**
	 * Returns the conversion to {@code UUID}: the canonical string (8-4-4-4-12
	 * hexadecimal digits).
	 * @return the conversion to {@code UUID}
	 */
	public static Function<Object, @Nullable UUID> toUuid() {
		return value -> value instanceof UUID uuid ? uuid : parse(value, CanonicalUuid::parse);
	}

	/**
	 * Returns the conversion to {@code URI}: a string that {@link URI} parses, which may
	 * be a reference without a scheme.
	 * @return the conversion to {@code URI}
	 */
	public static Function<Object, @Nullable URI> toUri() {
		return value -> value instanceof URI uri ? uri : parse(value, URI::create);
	}

	/**
	 * Returns the conversion that {@link ValueValidator#untyped(Class)} applies for the
	 * given type.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T> Function<Object, ? extends @Nullable T> of(Class<T> type) {
		final Function<Object, ? extends @Nullable Object> conversion;
		if (type == Map.class) {
			conversion = value -> InputReader.ofJdkTypes().isObject(value) ? value : null;
		}
		else if (type == Integer.class) {
			conversion = toInteger();
		}
		else if (type == Long.class) {
			conversion = toLong();
		}
		else if (type == Short.class) {
			conversion = toShort();
		}
		else if (type == Byte.class) {
			conversion = toByte();
		}
		else if (type == BigInteger.class) {
			conversion = toBigInteger();
		}
		else if (type == BigDecimal.class) {
			conversion = toBigDecimal();
		}
		else if (type == Double.class) {
			conversion = toDouble();
		}
		else if (type == Float.class) {
			conversion = toFloat();
		}
		else if (type == LocalDate.class) {
			conversion = toLocalDate();
		}
		else if (type == LocalTime.class) {
			conversion = toLocalTime();
		}
		else if (type == LocalDateTime.class) {
			conversion = toLocalDateTime();
		}
		else if (type == OffsetDateTime.class) {
			conversion = toOffsetDateTime();
		}
		else if (type == ZonedDateTime.class) {
			conversion = toZonedDateTime();
		}
		else if (type == Instant.class) {
			conversion = toInstant();
		}
		else if (type == Year.class) {
			conversion = toYear();
		}
		else if (type == YearMonth.class) {
			conversion = toYearMonth();
		}
		else if (type == Duration.class) {
			conversion = toDuration();
		}
		else if (type == Period.class) {
			conversion = toPeriod();
		}
		else if (type == UUID.class) {
			conversion = toUuid();
		}
		else if (type == URI.class) {
			conversion = toUri();
		}
		else {
			conversion = type.getEnumConstants() != null ? constants((Class) type) : instanceOf(type);
		}
		return (Function<Object, ? extends @Nullable T>) conversion;
	}

	/**
	 * Returns the conversion that takes an instance of the type or the name of one of its
	 * constants.
	 */
	private static <E extends Enum<E>> Function<Object, @Nullable E> constants(Class<E> type) {
		final E @Nullable [] constants = type.getEnumConstants();
		return value -> {
			if (type.isInstance(value)) {
				return type.cast(value);
			}
			if (value instanceof String name && constants != null) {
				for (E constant : constants) {
					if (constant.name().equals(name)) {
						return constant;
					}
				}
			}
			return null;
		};
	}

	/**
	 * Returns the value that the parser reads from a string, or {@code null} if the value
	 * is not a string or the parser rejects it with a {@code DateTimeException} or an
	 * {@code IllegalArgumentException}.
	 */
	private static <T> @Nullable T parse(Object value, Function<String, T> parser) {
		if (!(value instanceof String string)) {
			return null;
		}
		try {
			return parser.apply(string);
		}
		catch (DateTimeException | IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Returns the value of a number that is an integer in the given range, or
	 * {@code null}. An integer of a primitive type is checked against the range as it is;
	 * any other number is checked through its decimal value.
	 */
	private static @Nullable Long integral(Object value, long min, long max) {
		final long integral;
		if (isIntegral(value)) {
			integral = ((Number) value).longValue();
		}
		else {
			final BigDecimal decimal = decimal(value);
			if (decimal == null) {
				return null;
			}
			try {
				integral = decimal.longValueExact();
			}
			catch (ArithmeticException e) {
				return null;
			}
		}
		return integral < min || integral > max ? null : integral;
	}

	private static boolean isIntegral(Object value) {
		return value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte;
	}

	/**
	 * Returns the exact decimal value of a number, or {@code null} if the value is not a
	 * number or is not finite. A floating-point number is the shortest decimal that
	 * identifies it: {@code 0.1f} is {@code 0.1}.
	 */
	private static @Nullable BigDecimal decimal(Object value) {
		if (value instanceof BigDecimal decimal) {
			return decimal;
		}
		if (value instanceof BigInteger integer) {
			return new BigDecimal(integer);
		}
		if (isIntegral(value)) {
			return BigDecimal.valueOf(((Number) value).longValue());
		}
		if (value instanceof Double || value instanceof Float) {
			return Double.isFinite(((Number) value).doubleValue()) ? new BigDecimal(value.toString()) : null;
		}
		return null;
	}

	/**
	 * Tells whether the date is of a type of {@code java.sql}, without loading a class of
	 * that module, which a run-time image may not have.
	 */
	private static boolean isSqlType(java.util.Date date) {
		for (Class<?> current = date.getClass(); current != null; current = current.getSuperclass()) {
			if (current.getName().startsWith("java.sql.")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Converts the dates and times that the row of a JDBC driver holds to the type of
	 * {@code java.time} that stands for the same column. The class is loaded only for a
	 * value of {@code java.sql}.
	 */
	private static final class SqlTypes {

		private static @Nullable LocalDate toLocalDate(java.util.Date value) {
			return value instanceof java.sql.Date date ? date.toLocalDate() : null;
		}

		private static @Nullable LocalTime toLocalTime(java.util.Date value) {
			return value instanceof java.sql.Time time ? time.toLocalTime() : null;
		}

		private static @Nullable LocalDateTime toLocalDateTime(java.util.Date value) {
			return value instanceof java.sql.Timestamp timestamp ? timestamp.toLocalDateTime() : null;
		}

		private static @Nullable Instant toInstant(java.util.Date value) {
			return value instanceof java.sql.Timestamp timestamp ? timestamp.toInstant() : null;
		}

	}

	/**
	 * Reads the canonical form of a {@code UUID}. The pattern is compiled only when a
	 * {@code UUID} is read.
	 */
	private static final class CanonicalUuid {

		// UUID.fromString also takes forms that are not canonical, such as 1-1-1-1-1
		private static final Pattern PATTERN = Pattern
			.compile("\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");

		private static UUID parse(String string) {
			if (!PATTERN.matcher(string).matches()) {
				throw new IllegalArgumentException("Not a canonical UUID");
			}
			return UUID.fromString(string);
		}

	}

}
