/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.validator;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.BigDecimalValidator;
import am.ik.yavi.arguments.BigIntegerValidator;
import am.ik.yavi.arguments.BooleanValidator;
import am.ik.yavi.arguments.DoubleValidator;
import am.ik.yavi.arguments.EnumValidator;
import am.ik.yavi.arguments.FloatValidator;
import am.ik.yavi.arguments.InstantValidator;
import am.ik.yavi.arguments.IntegerValidator;
import am.ik.yavi.arguments.LocalDateTimeValidator;
import am.ik.yavi.arguments.LocalDateValidator;
import am.ik.yavi.arguments.LocalTimeValidator;
import am.ik.yavi.arguments.LongValidator;
import am.ik.yavi.arguments.ObjectValidator;
import am.ik.yavi.arguments.OffsetDateTimeValidator;
import am.ik.yavi.arguments.ShortValidator;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.arguments.YearMonthValidator;
import am.ik.yavi.arguments.YearValidator;
import am.ik.yavi.arguments.ZonedDateTimeValidator;
import am.ik.yavi.builder.BigDecimalValidatorBuilder;
import am.ik.yavi.builder.BigIntegerValidatorBuilder;
import am.ik.yavi.builder.BooleanValidatorBuilder;
import am.ik.yavi.builder.DoubleValidatorBuilder;
import am.ik.yavi.builder.EnumValidatorBuilder;
import am.ik.yavi.builder.FloatValidatorBuilder;
import am.ik.yavi.builder.InstantValidatorBuilder;
import am.ik.yavi.builder.IntegerValidatorBuilder;
import am.ik.yavi.builder.LocalDateTimeValidatorBuilder;
import am.ik.yavi.builder.LocalDateValidatorBuilder;
import am.ik.yavi.builder.LocalTimeValidatorBuilder;
import am.ik.yavi.builder.LongValidatorBuilder;
import am.ik.yavi.builder.ObjectValidatorBuilder;
import am.ik.yavi.builder.OffsetDateTimeValidatorBuilder;
import am.ik.yavi.builder.ShortValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.builder.YaviArguments;
import am.ik.yavi.builder.YearMonthValidatorBuilder;
import am.ik.yavi.builder.YearValidatorBuilder;
import am.ik.yavi.builder.ZonedDateTimeValidatorBuilder;
import am.ik.yavi.constraint.BigDecimalConstraint;
import am.ik.yavi.constraint.BigIntegerConstraint;
import am.ik.yavi.constraint.BooleanConstraint;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.constraint.DoubleConstraint;
import am.ik.yavi.constraint.EnumConstraint;
import am.ik.yavi.constraint.FloatConstraint;
import am.ik.yavi.constraint.InstantConstraint;
import am.ik.yavi.constraint.IntegerConstraint;
import am.ik.yavi.constraint.LocalDateConstraint;
import am.ik.yavi.constraint.LocalDateTimeConstraint;
import am.ik.yavi.constraint.LocalTimeConstraint;
import am.ik.yavi.constraint.LongConstraint;
import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.constraint.OffsetDateTimeConstraint;
import am.ik.yavi.constraint.ShortConstraint;
import am.ik.yavi.constraint.StringValueConstraint;
import am.ik.yavi.constraint.YearConstraint;
import am.ik.yavi.constraint.YearMonthConstraint;
import am.ik.yavi.constraint.ZonedDateTimeConstraint;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.function.Function;

/**
 * Entry point for building validators of arguments and of single values.
 *
 * <p>
 * {@link #arguments()} returns a builder that validates the arguments of a constructor or
 * factory method and produces the result only when every argument is valid. All
 * violations are accumulated, so an invalid input reports every failed constraint at
 * once. The following example builds a validator for a constructor with three arguments:
 *
 * <pre>{@code
 * Arguments3Validator<String, String, Integer, Car> validator = Yavi.arguments()
 *     ._string("manufacturer", c -> c.notNull())
 *     ._string("licensePlate", c -> c.notNull().greaterThanOrEqual(2).lessThanOrEqual(14))
 *     ._integer("seatCount", c -> c.greaterThanOrEqual(2))
 *     .apply(Car::new);
 * Validated<Car> validated = validator.validate("Toyota", "ABC-123", 5);
 * }</pre>
 *
 * <p>
 * Arguments can be composed: {@code _object(name, validator)} nests another validator,
 * {@code _list} and {@code _map} validate elements, and {@code optional()},
 * {@code nullable()} and {@code defaultValue} make the last added argument accept
 * {@code null}. The violations of nested values are located under the argument name, for
 * example {@code address.street} or {@code roles[0]}. A rule that spans several arguments
 * is added to the created validator with {@code constraintOnTarget}:
 *
 * <pre>{@code
 * Arguments4Validator<String, String, AddressForm, List<String>, User> validator = Yavi.arguments()
 *     ._string("email", c -> c.notBlank().email())
 *     ._string("nickname", c -> c.notBlank()).optional()
 *     ._object("address", addressValidator)
 *     ._list("roles", Yavi._string(c -> c.notBlank()))
 *     .apply(User::new)
 *     .constraintOnTarget(user -> !user.roles().isEmpty() || user.nickname().isEmpty(), "roles",
 *         ViolationMessage.of("user.roles", "\"{0}\" must not be empty when a nickname is given"));
 * }</pre>
 *
 * <p>
 * The other methods return a validator of a single value: {@code _string},
 * {@code _integer}, {@code _localDate} and so on have the names of the arguments of
 * {@link YaviArguments} and take the constraints, with or without a name. A validator
 * without a name is the element of a list, the value of a map, an alternative of
 * {@code anyOf} or a value validated by itself; its violations are named by the place
 * where it is used ({@code roles[0]}), or have the empty name and a message that says
 * "The value" in place of the name.
 *
 * <pre>{@code
 * StringValidator<String> email = Yavi._string(c -> c.notBlank().email());
 * StringValidator<Email> named = Yavi._string("email", c -> c.notBlank().email()).map(Email::new);
 * ValueValidator<Object, Integer> age = Yavi._integer(c -> c.notNull().positiveOrZero()).untyped();
 * }</pre>
 *
 * <p>
 * A value is brought into shape before it is constrained: a {@code String} with the
 * transforms written before its constraints, any other value with {@code preprocess}. The
 * constraints and the produced value see the transformed value.
 *
 * <pre>{@code
 * StringValidator<String> email = Yavi._string("email", c -> c.trim().toLowerCase().notBlank().email());
 * BigDecimalValidator<BigDecimal> price = Yavi._bigDecimal("price", c -> c.notNull().positive())
 *     .preprocess(BigDecimal::stripTrailingZeros);
 * }</pre>
 *
 * <p>
 * This class only provides the entry points; {@link YaviArguments}, the returned builders
 * and the builders of {@code am.ik.yavi.builder} do the work.
 *
 * @since 0.14.0
 */
public final class Yavi {

	/**
	 * Returns a new builder for an arguments validator.
	 *
	 * <p>
	 * Each {@code _xxx} method of the returned {@link YaviArguments} adds one argument
	 * with its name and constraints, and {@code apply} creates the validator. For
	 * example:
	 *
	 * <pre>{@code
	 * Arguments2Validator<String, Integer, Person> validator = Yavi.arguments()
	 *     ._string("name", c -> c.notBlank())
	 *     ._integer("age", c -> c.greaterThanOrEqual(0))
	 *     .apply(Person::new);
	 * }</pre>
	 *
	 * <p>
	 * Every call returns a new, empty builder.
	 * @return a new arguments validator builder
	 * @since 0.14.0
	 */
	public static YaviArguments arguments() {
		return new YaviArguments();
	}

	/**
	 * Returns a validator of a {@code BigDecimal} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_bigDecimal(String, Function)
	 */
	public static BigDecimalValidator<BigDecimal> _bigDecimal(
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		return _bigDecimal("", constraints);
	}

	/**
	 * Returns a validator of a {@code BigDecimal} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link BigDecimalConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static BigDecimalValidator<BigDecimal> _bigDecimal(String name,
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		return BigDecimalValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code BigInteger} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_bigInteger(String, Function)
	 */
	public static BigIntegerValidator<BigInteger> _bigInteger(
			Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
		return _bigInteger("", constraints);
	}

	/**
	 * Returns a validator of a {@code BigInteger} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link BigIntegerConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static BigIntegerValidator<BigInteger> _bigInteger(String name,
			Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
		return BigIntegerValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code Boolean} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_boolean(String, Function)
	 */
	public static BooleanValidator<Boolean> _boolean(
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		return _boolean("", constraints);
	}

	/**
	 * Returns a validator of a {@code Boolean} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link BooleanConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static BooleanValidator<Boolean> _boolean(String name,
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		return BooleanValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code Double} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_double(String, Function)
	 */
	public static DoubleValidator<Double> _double(
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		return _double("", constraints);
	}

	/**
	 * Returns a validator of a {@code Double} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link DoubleConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static DoubleValidator<Double> _double(String name,
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		return DoubleValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a constant of the given enum type without a name and without
	 * constraints.
	 * @param <E> the enum type of the value
	 * @param type the enum type of the value
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_enum(String, Class, Function)
	 */
	public static <E extends Enum<E>> EnumValidator<E, E> _enum(Class<E> type) {
		return _enum("", type, Function.identity());
	}

	/**
	 * Returns a validator of a constant of the given enum type without a name.
	 * @param <E> the enum type of the value
	 * @param type the enum type of the value
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_enum(String, Class, Function)
	 */
	public static <E extends Enum<E>> EnumValidator<E, E> _enum(Class<E> type,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return _enum("", type, constraints);
	}

	/**
	 * Returns a validator of a constant of the given enum type with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link EnumConstraint}. The validator knows its enum type: its description holds
	 * the constants, and {@code untyped()} reads the value from the name of a constant.
	 * @param <E> the enum type of the value
	 * @param name the name used in violation names and messages
	 * @param type the enum type of the value
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static <E extends Enum<E>> EnumValidator<E, E> _enum(String name, Class<E> type,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return EnumValidatorBuilder.of(name, type, constraints).build();
	}

	/**
	 * Returns a validator of a {@code Float} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_float(String, Function)
	 */
	public static FloatValidator<Float> _float(
			Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		return _float("", constraints);
	}

	/**
	 * Returns a validator of a {@code Float} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link FloatConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static FloatValidator<Float> _float(String name,
			Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		return FloatValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of an {@code Instant} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_instant(String, Function)
	 */
	public static InstantValidator<Instant> _instant(
			Function<InstantConstraint<Arguments1<Instant>>, InstantConstraint<Arguments1<Instant>>> constraints) {
		return _instant("", constraints);
	}

	/**
	 * Returns a validator of an {@code Instant} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link InstantConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static InstantValidator<Instant> _instant(String name,
			Function<InstantConstraint<Arguments1<Instant>>, InstantConstraint<Arguments1<Instant>>> constraints) {
		return InstantValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of an {@code Integer} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_integer(String, Function)
	 */
	public static IntegerValidator<Integer> _integer(
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
		return _integer("", constraints);
	}

	/**
	 * Returns a validator of an {@code Integer} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link IntegerConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static IntegerValidator<Integer> _integer(String name,
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
		return IntegerValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code LocalDateTime} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_localDateTime(String, Function)
	 */
	public static LocalDateTimeValidator<LocalDateTime> _localDateTime(
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		return _localDateTime("", constraints);
	}

	/**
	 * Returns a validator of a {@code LocalDateTime} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link LocalDateTimeConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static LocalDateTimeValidator<LocalDateTime> _localDateTime(String name,
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		return LocalDateTimeValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code LocalTime} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_localTime(String, Function)
	 */
	public static LocalTimeValidator<LocalTime> _localTime(
			Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
		return _localTime("", constraints);
	}

	/**
	 * Returns a validator of a {@code LocalTime} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link LocalTimeConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static LocalTimeValidator<LocalTime> _localTime(String name,
			Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
		return LocalTimeValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code LocalDate} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_localDate(String, Function)
	 */
	public static LocalDateValidator<LocalDate> _localDate(
			Function<LocalDateConstraint<Arguments1<LocalDate>>, LocalDateConstraint<Arguments1<LocalDate>>> constraints) {
		return _localDate("", constraints);
	}

	/**
	 * Returns a validator of a {@code LocalDate} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link LocalDateConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static LocalDateValidator<LocalDate> _localDate(String name,
			Function<LocalDateConstraint<Arguments1<LocalDate>>, LocalDateConstraint<Arguments1<LocalDate>>> constraints) {
		return LocalDateValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code Long} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_long(String, Function)
	 */
	public static LongValidator<Long> _long(
			Function<LongConstraint<Arguments1<Long>>, LongConstraint<Arguments1<Long>>> constraints) {
		return _long("", constraints);
	}

	/**
	 * Returns a validator of a {@code Long} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link LongConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static LongValidator<Long> _long(String name,
			Function<LongConstraint<Arguments1<Long>>, LongConstraint<Arguments1<Long>>> constraints) {
		return LongValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a value of the given type without a name.
	 * @param <T> the type of the value
	 * @param type the type of the value
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_object(String, Class, Function)
	 */
	public static <T> ObjectValidator<T, T> _object(Class<T> type,
			Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return _object("", type, constraints);
	}

	/**
	 * Returns a validator of a value of the given type with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link ObjectConstraint}. The validator knows its input type, so {@code untyped()}
	 * accepts an instance of {@code type} only.
	 * @param <T> the type of the value
	 * @param name the name used in violation names and messages
	 * @param type the type of the value
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static <T> ObjectValidator<T, T> _object(String name, Class<T> type,
			Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return ObjectValidatorBuilder.of(name, type, constraints).build();
	}

	/**
	 * Returns a validator of a value of a type that has no class literal, such as
	 * {@code List<String>}, without a name.
	 *
	 * <p>
	 * The validator does not know its input type; give it with {@code untyped(type)}
	 * where it is needed.
	 * @param <T> the type of the value
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_object(String, Function)
	 */
	public static <T> ObjectValidator<T, T> _object(
			Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return _object("", constraints);
	}

	/**
	 * Returns a validator of a value of a type that has no class literal, such as
	 * {@code List<String>}, with the given name.
	 *
	 * <p>
	 * The validator does not know its input type; give it with {@code untyped(type)}
	 * where it is needed.
	 * @param <T> the type of the value
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static <T> ObjectValidator<T, T> _object(String name,
			Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return ObjectValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of an {@code OffsetDateTime} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_offsetDateTime(String, Function)
	 */
	public static OffsetDateTimeValidator<OffsetDateTime> _offsetDateTime(
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		return _offsetDateTime("", constraints);
	}

	/**
	 * Returns a validator of an {@code OffsetDateTime} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link OffsetDateTimeConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static OffsetDateTimeValidator<OffsetDateTime> _offsetDateTime(String name,
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		return OffsetDateTimeValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code Short} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_short(String, Function)
	 */
	public static ShortValidator<Short> _short(
			Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		return _short("", constraints);
	}

	/**
	 * Returns a validator of a {@code Short} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link ShortConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static ShortValidator<Short> _short(String name,
			Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		return ShortValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code String} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_string(String, Function)
	 */
	public static StringValidator<String> _string(
			Function<? super StringValueConstraint<Arguments1<String>>, ? extends CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return _string("", constraints);
	}

	/**
	 * Returns a validator of a {@code String} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link CharSequenceConstraint}.
	 *
	 * <p>
	 * The value can be transformed before it is constrained with the transforms of
	 * {@link StringValueConstraint}, for example
	 * {@code c -> c.trim().toLowerCase().notBlank().email()}. The constraints and the
	 * produced value see the transformed value.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the transforms and the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static StringValidator<String> _string(String name,
			Function<? super StringValueConstraint<Arguments1<String>>, ? extends CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return StringValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code YearMonth} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_yearMonth(String, Function)
	 */
	public static YearMonthValidator<YearMonth> _yearMonth(
			Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
		return _yearMonth("", constraints);
	}

	/**
	 * Returns a validator of a {@code YearMonth} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link YearMonthConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static YearMonthValidator<YearMonth> _yearMonth(String name,
			Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
		return YearMonthValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code Year} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_year(String, Function)
	 */
	public static YearValidator<Year> _year(
			Function<YearConstraint<Arguments1<Year>>, YearConstraint<Arguments1<Year>>> constraints) {
		return _year("", constraints);
	}

	/**
	 * Returns a validator of a {@code Year} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link YearConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static YearValidator<Year> _year(String name,
			Function<YearConstraint<Arguments1<Year>>, YearConstraint<Arguments1<Year>>> constraints) {
		return YearValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code ZonedDateTime} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_zonedDateTime(String, Function)
	 */
	public static ZonedDateTimeValidator<ZonedDateTime> _zonedDateTime(
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		return _zonedDateTime("", constraints);
	}

	/**
	 * Returns a validator of a {@code ZonedDateTime} with the given name.
	 *
	 * <p>
	 * A {@code null} value is accepted unless the constraints require otherwise.
	 * Violations use {@code name} as their name and the message ids of
	 * {@link ZonedDateTimeConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static ZonedDateTimeValidator<ZonedDateTime> _zonedDateTime(String name,
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		return ZonedDateTimeValidatorBuilder.of(name, constraints).build();
	}

	private Yavi() {

	}

}
