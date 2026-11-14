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
import am.ik.yavi.arguments.ListValidator;
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
import am.ik.yavi.builder.DynamicSchema;
import am.ik.yavi.builder.EnumValidatorBuilder;
import am.ik.yavi.builder.FloatValidatorBuilder;
import am.ik.yavi.builder.InstantValidatorBuilder;
import am.ik.yavi.builder.IntegerValidatorBuilder;
import am.ik.yavi.builder.ListValidatorBuilder;
import am.ik.yavi.builder.LocalDateTimeValidatorBuilder;
import am.ik.yavi.builder.LocalDateValidatorBuilder;
import am.ik.yavi.builder.LocalTimeValidatorBuilder;
import am.ik.yavi.builder.LongValidatorBuilder;
import am.ik.yavi.builder.MapValidatorBuilder;
import am.ik.yavi.builder.ObjectValidatorBuilder;
import am.ik.yavi.builder.ReversibleDynamicSchema;
import am.ik.yavi.builder.ReversibleYaviSchema;
import am.ik.yavi.builder.OffsetDateTimeValidatorBuilder;
import am.ik.yavi.builder.ShortValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.builder.YaviSchema;
import am.ik.yavi.builder.YearMonthValidatorBuilder;
import am.ik.yavi.builder.YearValidatorBuilder;
import am.ik.yavi.builder.ZonedDateTimeValidatorBuilder;
import am.ik.yavi.constraint.BigDecimalConstraint;
import am.ik.yavi.constraint.BigIntegerConstraint;
import am.ik.yavi.constraint.BooleanConstraint;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.constraint.CollectionConstraint;
import am.ik.yavi.constraint.DoubleConstraint;
import am.ik.yavi.constraint.EnumConstraint;
import am.ik.yavi.constraint.FloatConstraint;
import am.ik.yavi.constraint.InstantConstraint;
import am.ik.yavi.constraint.IntegerConstraint;
import am.ik.yavi.constraint.LocalDateConstraint;
import am.ik.yavi.constraint.LocalDateTimeConstraint;
import am.ik.yavi.constraint.LocalTimeConstraint;
import am.ik.yavi.constraint.LongConstraint;
import am.ik.yavi.constraint.MapConstraint;
import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.constraint.OffsetDateTimeConstraint;
import am.ik.yavi.constraint.ShortConstraint;
import am.ik.yavi.constraint.StringValueConstraint;
import am.ik.yavi.constraint.YearConstraint;
import am.ik.yavi.constraint.YearMonthConstraint;
import am.ik.yavi.constraint.ZonedDateTimeConstraint;
import am.ik.yavi.core.ValueValidator;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

/**
 * Entry point for describing an input as a schema and for building validators of single
 * values.
 *
 * <p>
 * {@link #schema()} returns a builder that describes the properties of an input and
 * produces the result only when every property is valid. All violations are accumulated,
 * so an invalid input reports every failed constraint at once. {@code fromMap} ends the
 * schema with a validator that reads the properties from a {@code Map<String, Object>},
 * such as a parsed JSON object, by their names, checks their types and gives them to a
 * constructor or factory method:
 *
 * <pre>{@code
 * ObjectInputValidator<Car> validator = Yavi.schema()
 *     ._string("manufacturer")
 *     ._string("licensePlate", c -> c.greaterThanOrEqual(2).lessThanOrEqual(14))
 *     ._integer("seatCount", c -> c.greaterThanOrEqual(2))
 *     .fromMap(Car::new);
 * Validated<Car> validated = validator
 *     .validate(Map.of("manufacturer", "Toyota", "licensePlate", "ABC-123", "seatCount", 5));
 * }</pre>
 *
 * <p>
 * Properties can be composed: {@code _object(name, validator)} nests another validator,
 * {@code _list} and {@code _map} validate elements and, with constraints, the list or map
 * of the validated elements ({@code c -> c.notEmpty().unique()}), and {@code optional()},
 * {@code nullable()} and {@code defaultValue} make the last added property accept
 * {@code null} and an absent key, which are violations otherwise. The violations of
 * nested values are located under the property name, for example {@code address.street}
 * or {@code roles[0]}. A rule that spans several properties is added to the created
 * validator with {@code constraintOnTarget}:
 *
 * <pre>{@code
 * ObjectInputValidator<Address> addressValidator = Yavi.schema()
 *     ._string("street", c -> c.notBlank())
 *     ._string("city", c -> c.notBlank())
 *     .fromMap(Address::new);
 * ObjectInputValidator<User> validator = Yavi.schema()
 *     ._string("email", c -> c.notBlank().email())
 *     ._string("nickname", c -> c.notBlank()).optional()
 *     ._object("address", addressValidator)
 *     ._list("roles", Yavi._string(c -> c.notBlank())).defaultValue(List.of())
 *     .fromMap(User::new)
 *     .constraintOnTarget(user -> !user.roles().isEmpty() || user.nickname().isEmpty(), "roles",
 *         ViolationMessage.of("user.roles", "\"{0}\" must not be empty when a nickname is given"));
 * }</pre>
 *
 * <p>
 * The same schema reads other inputs: {@code readingWith(reader)} of the validator reads
 * the tree model of a JSON library and {@code readingParameters()} request parameters or
 * a submitted form, where {@code "5"} is an {@code Integer}.
 *
 * <pre>{@code
 * ValueValidator<JsonNode, Car> nodeValidator = validator.readingWith(jsonNodeReader);
 * ValueValidator<Map<String, ?>, Car> formValidator = validator.readingParameters();
 * }</pre>
 *
 * <p>
 * {@code apply} ends the schema with a validator of typed arguments instead, for values
 * that are already bound to their types:
 *
 * <pre>{@code
 * Arguments3Validator<String, String, Integer, Car> validator = Yavi.schema()
 *     ._string("manufacturer")
 *     ._string("licensePlate", c -> c.greaterThanOrEqual(2).lessThanOrEqual(14))
 *     ._integer("seatCount", c -> c.greaterThanOrEqual(2))
 *     .apply(Car::new);
 * Validated<Car> validated = validator.validate("Toyota", "ABC-123", 5);
 * }</pre>
 *
 * <p>
 * The other methods return a validator of a single value: {@code _string},
 * {@code _integer}, {@code _localDate} and so on have the names of the properties of
 * {@link YaviSchema} and take a name, the constraints, both or neither, as the properties
 * of a schema do. A validator without a name is the element of a list, the value of a
 * map, an alternative of {@code anyOf} or a value validated by itself; its violations are
 * named by the place where it is used ({@code roles[0]}), or have the empty name and a
 * message that says "The value" in place of the name. Such a validator requires its
 * value: {@code null} is a violation unless {@code nullable()}, {@code optional()} or
 * {@code defaultValue(...)} is applied to it, and the type of the validated value tells
 * which. {@code _list} and {@code _map} return the validator of a list or a map that is
 * not a property, with or without the constraints of the list or map.
 *
 * <pre>{@code
 * StringValidator<String> email = Yavi._string(c -> c.notBlank().email());
 * StringValidator<Email> named = Yavi._string("email", c -> c.notBlank().email()).map(Email::new);
 * ValueValidator<Object, Integer> age = Yavi._integer(c -> c.positiveOrZero()).untyped();
 * }</pre>
 *
 * <p>
 * A value is brought into shape before it is constrained: a {@code String} with the
 * transforms written before its constraints, any other value with {@code preprocess}. The
 * constraints and the produced value see the transformed value.
 *
 * <pre>{@code
 * StringValidator<String> email = Yavi._string("email", c -> c.trim().toLowerCase().notBlank().email());
 * BigDecimalValidator<BigDecimal> price = Yavi._bigDecimal("price", c -> c.positive())
 *     .preprocess(BigDecimal::stripTrailingZeros);
 * }</pre>
 *
 * <p>
 * The rule of a value is defined once and used under several names with {@code named},
 * and {@link #dynamicSchema()} describes an input whose properties are known only at run
 * time, such as the custom fields of a record, and produces a map of the validated
 * values.
 *
 * <pre>{@code
 * StringValidator<String> backupEmail = email.named("backupEmail");
 * }</pre>
 *
 * <p>
 * This class only provides the entry points; {@link YaviSchema}, the returned builders
 * and the builders of {@code am.ik.yavi.builder} do the work.
 *
 * @since 0.14.0
 */
public final class Yavi {

	/**
	 * Returns a new, empty schema: the description of the properties of an input.
	 *
	 * <p>
	 * Each {@code _xxx} method of the returned {@link YaviSchema} adds one property with
	 * its name and constraints. The schema becomes a validator when it is told how the
	 * input is given: {@code fromMap} creates a validator that reads the properties from
	 * a map and {@code apply} one of typed arguments. For example:
	 *
	 * <pre>{@code
	 * ObjectInputValidator<Person> validator = Yavi.schema()
	 *     ._string("name", c -> c.notBlank())
	 *     ._integer("age", c -> c.greaterThanOrEqual(0))
	 *     .fromMap(Person::new);
	 * Validated<Person> validated = validator.validate(Map.of("name", "Alice", "age", 20));
	 * }</pre>
	 *
	 * <p>
	 * Every call returns a new, empty schema.
	 * @return a new schema
	 * @since 0.18.0
	 */
	public static YaviSchema schema() {
		return new YaviSchema();
	}

	/**
	 * Returns a new, empty schema whose validator also writes the created object back as
	 * the map that it is read from.
	 *
	 * <p>
	 * The properties are added as to a schema of {@link #schema()}, and
	 * {@code fromMapReversible} takes the function that creates the object and the
	 * accessor of every property:
	 *
	 * <pre>{@code
	 * ObjectInputValidator.Reversible<Person> validator = Yavi.reversibleSchema()
	 *     ._string("name", c -> c.notBlank())
	 *     ._string(Yavi._string("email").map(Email::new, Email::value))
	 *     ._integer("age").optional()
	 *     .fromMapReversible(Person::new, Person::name, Person::email, Person::age);
	 * Map<String, Object> map = validator.toMap(person); // {name=Alice, email=alice@example.com, age=null}
	 * }</pre>
	 *
	 * <p>
	 * Every property must be one that can be written back, which the compiler checks: a
	 * property of constraints always is, and where {@link #schema()} takes any
	 * {@link ValueValidator}, this schema takes a
	 * {@link am.ik.yavi.core.ReversibleValueValidator} only. A validator that maps its
	 * value without saying how to go back, such as
	 * {@code Yavi._string("email").map(Email::new)}, is rejected where it is added.
	 * @return a new schema
	 * @since 0.18.0
	 * @see am.ik.yavi.input.ObjectInputValidator.Reversible
	 */
	public static ReversibleYaviSchema reversibleSchema() {
		return new ReversibleYaviSchema();
	}

	/**
	 * Returns a new, empty schema whose properties are known only at run time, such as
	 * the questions of a survey or the custom fields of a record.
	 *
	 * <p>
	 * A property is added with its name and the validator of its value, in a loop if need
	 * be, and {@code build()} creates a validator that reads the properties from a map
	 * and produces the map of the validated values by their names. The type of the values
	 * is the one the schema is assigned to, or given explicitly:
	 *
	 * <pre>{@code
	 * ObjectInputValidator<Map<String, String>> answersValidator = Yavi.<String>dynamicSchema()
	 *     .property("q1", Yavi._string(c -> c.notBlank()))
	 *     .property("q2", Yavi._string(c -> c.lessThanOrEqual(200)).defaultValue(""))
	 *     .property("q3", Yavi._string(c -> c.notBlank())).presence() // no entry if absent
	 *     .build();
	 * }</pre>
	 *
	 * <p>
	 * Every call returns a new, empty schema, and every schema is immutable.
	 * @param <V> the type of the values of the produced map; {@code Object} for
	 * properties of several types
	 * @return a new schema
	 * @since 0.18.0
	 * @see DynamicSchema
	 */
	public static <V extends @Nullable Object> DynamicSchema<V> dynamicSchema() {
		return new DynamicSchema<>();
	}

	/**
	 * Returns a new, empty schema whose properties are known only at run time, as
	 * {@link #dynamicSchema()} does, and whose validator also writes the produced map
	 * back as the map that it is read from. A property is added with a validator that
	 * knows its reverse, which the compiler checks.
	 * @param <V> the type of the values of the produced map
	 * @return a new schema
	 * @since 0.18.0
	 * @see am.ik.yavi.input.ObjectInputValidator.Reversible
	 */
	public static <V extends @Nullable Object> ReversibleDynamicSchema<V> reversibleDynamicSchema() {
		return new ReversibleDynamicSchema<>();
	}

	/**
	 * Returns a new, empty schema.
	 * @return a new schema
	 * @since 0.14.0
	 * @deprecated in favor of {@link #schema()}, which is the same under the name that
	 * says what the builder has become
	 */
	@Deprecated(since = "0.18.0")
	public static YaviSchema arguments() {
		return schema();
	}

	/**
	 * Returns a validator of a {@code BigDecimal} without a name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_bigDecimal(Function)
	 */
	public static BigDecimalValidator.Reversible _bigDecimal() {
		return _bigDecimal("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code BigDecimal} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_bigDecimal(String, Function)
	 */
	public static BigDecimalValidator.Reversible _bigDecimal(
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		return _bigDecimal("", constraints);
	}

	/**
	 * Returns a validator of a {@code BigDecimal} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_bigDecimal(String, Function)
	 */
	public static BigDecimalValidator.Reversible _bigDecimal(String name) {
		return _bigDecimal(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code BigDecimal} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link BigDecimalConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static BigDecimalValidator.Reversible _bigDecimal(String name,
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		return BigDecimalValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code BigInteger} without a name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_bigInteger(Function)
	 */
	public static BigIntegerValidator.Reversible _bigInteger() {
		return _bigInteger("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code BigInteger} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_bigInteger(String, Function)
	 */
	public static BigIntegerValidator.Reversible _bigInteger(
			Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
		return _bigInteger("", constraints);
	}

	/**
	 * Returns a validator of a {@code BigInteger} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_bigInteger(String, Function)
	 */
	public static BigIntegerValidator.Reversible _bigInteger(String name) {
		return _bigInteger(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code BigInteger} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link BigIntegerConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static BigIntegerValidator.Reversible _bigInteger(String name,
			Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
		return BigIntegerValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code Boolean} without a name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_boolean(Function)
	 */
	public static BooleanValidator.Reversible _boolean() {
		return _boolean("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code Boolean} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_boolean(String, Function)
	 */
	public static BooleanValidator.Reversible _boolean(
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		return _boolean("", constraints);
	}

	/**
	 * Returns a validator of a {@code Boolean} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_boolean(String, Function)
	 */
	public static BooleanValidator.Reversible _boolean(String name) {
		return _boolean(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code Boolean} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link BooleanConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static BooleanValidator.Reversible _boolean(String name,
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		return BooleanValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code Double} without a name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_double(Function)
	 */
	public static DoubleValidator.Reversible _double() {
		return _double("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code Double} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_double(String, Function)
	 */
	public static DoubleValidator.Reversible _double(
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		return _double("", constraints);
	}

	/**
	 * Returns a validator of a {@code Double} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_double(String, Function)
	 */
	public static DoubleValidator.Reversible _double(String name) {
		return _double(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code Double} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link DoubleConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static DoubleValidator.Reversible _double(String name,
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
	public static <E extends Enum<E>> EnumValidator.Reversible<E> _enum(Class<E> type) {
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
	public static <E extends Enum<E>> EnumValidator.Reversible<E> _enum(Class<E> type,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return _enum("", type, constraints);
	}

	/**
	 * Returns a validator of a constant of the given enum type with the given name and
	 * without constraints, which accepts every constant.
	 *
	 * <pre>{@code
	 * Arguments1Validator<Role, Role> role = Yavi._enum("role", Role.class).ignoringCase();
	 * }</pre>
	 * @param <E> the enum type of the value
	 * @param name the name used in violation names and messages
	 * @param type the enum type of the value
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_enum(String, Class, Function)
	 */
	public static <E extends Enum<E>> EnumValidator.Reversible<E> _enum(String name, Class<E> type) {
		return _enum(name, type, Function.identity());
	}

	/**
	 * Returns a validator of a constant of the given enum type with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link EnumConstraint}. The
	 * validator knows its enum type: its description holds the constants, and
	 * {@code untyped()} reads the value from the name of a constant.
	 * @param <E> the enum type of the value
	 * @param name the name used in violation names and messages
	 * @param type the enum type of the value
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static <E extends Enum<E>> EnumValidator.Reversible<E> _enum(String name, Class<E> type,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return EnumValidatorBuilder.of(name, type, constraints).build();
	}

	/**
	 * Returns a validator of a {@code Float} without a name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_float(Function)
	 */
	public static FloatValidator.Reversible _float() {
		return _float("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code Float} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_float(String, Function)
	 */
	public static FloatValidator.Reversible _float(
			Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		return _float("", constraints);
	}

	/**
	 * Returns a validator of a {@code Float} with the given name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_float(String, Function)
	 */
	public static FloatValidator.Reversible _float(String name) {
		return _float(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code Float} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link FloatConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static FloatValidator.Reversible _float(String name,
			Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		return FloatValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of an {@code Instant} without a name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_instant(Function)
	 */
	public static InstantValidator.Reversible _instant() {
		return _instant("", Function.identity());
	}

	/**
	 * Returns a validator of an {@code Instant} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_instant(String, Function)
	 */
	public static InstantValidator.Reversible _instant(
			Function<InstantConstraint<Arguments1<Instant>>, InstantConstraint<Arguments1<Instant>>> constraints) {
		return _instant("", constraints);
	}

	/**
	 * Returns a validator of an {@code Instant} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_instant(String, Function)
	 */
	public static InstantValidator.Reversible _instant(String name) {
		return _instant(name, Function.identity());
	}

	/**
	 * Returns a validator of an {@code Instant} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link InstantConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static InstantValidator.Reversible _instant(String name,
			Function<InstantConstraint<Arguments1<Instant>>, InstantConstraint<Arguments1<Instant>>> constraints) {
		return InstantValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of an {@code Integer} without a name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_integer(Function)
	 */
	public static IntegerValidator.Reversible _integer() {
		return _integer("", Function.identity());
	}

	/**
	 * Returns a validator of an {@code Integer} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_integer(String, Function)
	 */
	public static IntegerValidator.Reversible _integer(
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
		return _integer("", constraints);
	}

	/**
	 * Returns a validator of an {@code Integer} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_integer(String, Function)
	 */
	public static IntegerValidator.Reversible _integer(String name) {
		return _integer(name, Function.identity());
	}

	/**
	 * Returns a validator of an {@code Integer} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link IntegerConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static IntegerValidator.Reversible _integer(String name,
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
		return IntegerValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a list without a name that validates every element with the
	 * given validator, without constraints on the list itself.
	 * @param <E> the type of the elements to validate
	 * @param <T> the type of the validated elements
	 * @param elementValidator value validator for each element
	 * @return a validator of the list
	 * @since 0.18.0
	 * @see #_list(ValueValidator, Function)
	 */
	public static <E, T> ListValidator<E, T> _list(ValueValidator<E, T> elementValidator) {
		return _list(elementValidator, Function.identity());
	}

	/**
	 * Returns a validator of a list without a name that validates every element with the
	 * given validator and the list of the validated elements with the given constraints.
	 *
	 * <p>
	 * The elements are validated like {@link ValueValidator#liftList()} does: the
	 * violations of an element are located under its index, and a {@code null} list is
	 * validated as an empty list. The constraints are checked on the produced elements,
	 * only when every element is valid, and their violations are reported on the list
	 * itself. This is the validator of a list that is not a property, such as the root of
	 * a JSON document; a property is added with
	 * {@code _list(name, elementValidator, constraints)} of {@link YaviSchema}.
	 *
	 * <pre>{@code
	 * ValueValidator<Iterable<? extends String>, List<String>> tags = Yavi._list(Yavi._string(c -> c.trim().notBlank()),
	 *     c -> c.notEmpty().lessThanOrEqual(10).unique());
	 * }</pre>
	 * @param <E> the type of the elements to validate
	 * @param <T> the type of the validated elements
	 * @param elementValidator value validator for each element
	 * @param constraints function that configures the constraints of the list
	 * @return a validator of the list
	 * @since 0.18.0
	 */
	public static <E, T> ListValidator<E, T> _list(ValueValidator<E, T> elementValidator,
			Function<CollectionConstraint<Arguments1<List<T>>, List<T>, T>, CollectionConstraint<Arguments1<List<T>>, List<T>, T>> constraints) {
		return ListValidatorBuilder.of(elementValidator, constraints).build();
	}

	/**
	 * Returns a validator of a {@code LocalDateTime} without a name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_localDateTime(Function)
	 */
	public static LocalDateTimeValidator.Reversible _localDateTime() {
		return _localDateTime("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code LocalDateTime} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_localDateTime(String, Function)
	 */
	public static LocalDateTimeValidator.Reversible _localDateTime(
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		return _localDateTime("", constraints);
	}

	/**
	 * Returns a validator of a {@code LocalDateTime} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_localDateTime(String, Function)
	 */
	public static LocalDateTimeValidator.Reversible _localDateTime(String name) {
		return _localDateTime(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code LocalDateTime} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link LocalDateTimeConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static LocalDateTimeValidator.Reversible _localDateTime(String name,
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		return LocalDateTimeValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code LocalTime} without a name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_localTime(Function)
	 */
	public static LocalTimeValidator.Reversible _localTime() {
		return _localTime("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code LocalTime} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_localTime(String, Function)
	 */
	public static LocalTimeValidator.Reversible _localTime(
			Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
		return _localTime("", constraints);
	}

	/**
	 * Returns a validator of a {@code LocalTime} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_localTime(String, Function)
	 */
	public static LocalTimeValidator.Reversible _localTime(String name) {
		return _localTime(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code LocalTime} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link LocalTimeConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static LocalTimeValidator.Reversible _localTime(String name,
			Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
		return LocalTimeValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code LocalDate} without a name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_localDate(Function)
	 */
	public static LocalDateValidator.Reversible _localDate() {
		return _localDate("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code LocalDate} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_localDate(String, Function)
	 */
	public static LocalDateValidator.Reversible _localDate(
			Function<LocalDateConstraint<Arguments1<LocalDate>>, LocalDateConstraint<Arguments1<LocalDate>>> constraints) {
		return _localDate("", constraints);
	}

	/**
	 * Returns a validator of a {@code LocalDate} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_localDate(String, Function)
	 */
	public static LocalDateValidator.Reversible _localDate(String name) {
		return _localDate(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code LocalDate} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link LocalDateConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static LocalDateValidator.Reversible _localDate(String name,
			Function<LocalDateConstraint<Arguments1<LocalDate>>, LocalDateConstraint<Arguments1<LocalDate>>> constraints) {
		return LocalDateValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code Long} without a name and without constraints, which
	 * accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_long(Function)
	 */
	public static LongValidator.Reversible _long() {
		return _long("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code Long} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_long(String, Function)
	 */
	public static LongValidator.Reversible _long(
			Function<LongConstraint<Arguments1<Long>>, LongConstraint<Arguments1<Long>>> constraints) {
		return _long("", constraints);
	}

	/**
	 * Returns a validator of a {@code Long} with the given name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_long(String, Function)
	 */
	public static LongValidator.Reversible _long(String name) {
		return _long(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code Long} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link LongConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static LongValidator.Reversible _long(String name,
			Function<LongConstraint<Arguments1<Long>>, LongConstraint<Arguments1<Long>>> constraints) {
		return LongValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a map with string keys without a name that validates every
	 * value with the given validator, without constraints on the map itself.
	 * @param <V> the type of the values to validate
	 * @param <T> the type of the validated values
	 * @param valueValidator value validator for each value
	 * @return a validator of the map
	 * @since 0.18.0
	 * @see #_map(ValueValidator, Function)
	 */
	public static <V, T> ValueValidator<Map<String, ? extends V>, Map<String, T>> _map(
			ValueValidator<V, T> valueValidator) {
		return _map(valueValidator, Function.identity());
	}

	/**
	 * Returns a validator of a map with string keys without a name that validates every
	 * value with the given validator and the map of the validated values with the given
	 * constraints.
	 *
	 * <p>
	 * The values are validated like {@link ValueValidator#liftMap()} does: the violations
	 * of a value are located under its key, and a {@code null} map is validated as an
	 * empty map. The constraints are checked on the produced values, only when every
	 * value is valid, and their violations are reported on the map itself. A property is
	 * added with {@code _map(name, valueValidator, constraints)} of {@link YaviSchema}.
	 *
	 * <pre>{@code
	 * ValueValidator<Map<String, ? extends BigDecimal>, Map<String, BigDecimal>> prices = Yavi
	 *     ._map(Yavi._bigDecimal(c -> c.positive()), c -> c.notEmpty());
	 * }</pre>
	 * @param <V> the type of the values to validate
	 * @param <T> the type of the validated values
	 * @param valueValidator value validator for each value
	 * @param constraints function that configures the constraints of the map
	 * @return a validator of the map
	 * @since 0.18.0
	 */
	public static <V, T> ValueValidator<Map<String, ? extends V>, Map<String, T>> _map(
			ValueValidator<V, T> valueValidator,
			Function<MapConstraint<Arguments1<Map<String, T>>, String, T>, MapConstraint<Arguments1<Map<String, T>>, String, T>> constraints) {
		return MapValidatorBuilder.of(valueValidator, constraints).build();
	}

	/**
	 * Returns a validator of a value of the given type without a name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param <T> the type of the value
	 * @param type the type of the value
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_object(Class, Function)
	 */
	public static <T> ObjectValidator.Reversible<T> _object(Class<T> type) {
		return _object("", type, Function.identity());
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
	public static <T> ObjectValidator.Reversible<T> _object(Class<T> type,
			Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return _object("", type, constraints);
	}

	/**
	 * Returns a validator of a value of the given type with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 *
	 * <p>
	 * A value of a type that has no class literal has no such form: a validator that
	 * neither knows its type nor has a constraint validates nothing.
	 * @param <T> the type of the value
	 * @param name the name used in violation names and messages
	 * @param type the type of the value
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_object(String, Class, Function)
	 */
	public static <T> ObjectValidator.Reversible<T> _object(String name, Class<T> type) {
		return _object(name, type, Function.identity());
	}

	/**
	 * Returns a validator of a value of the given type with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link ObjectConstraint}. The
	 * validator knows its input type, so {@code untyped()} accepts an instance of
	 * {@code type} only.
	 * @param <T> the type of the value
	 * @param name the name used in violation names and messages
	 * @param type the type of the value
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static <T> ObjectValidator.Reversible<T> _object(String name, Class<T> type,
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
	public static <T> ObjectValidator.Reversible<T> _object(
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
	public static <T> ObjectValidator.Reversible<T> _object(String name,
			Function<ObjectConstraint<Arguments1<T>, T>, ObjectConstraint<Arguments1<T>, T>> constraints) {
		return ObjectValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of an {@code OffsetDateTime} without a name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_offsetDateTime(Function)
	 */
	public static OffsetDateTimeValidator.Reversible _offsetDateTime() {
		return _offsetDateTime("", Function.identity());
	}

	/**
	 * Returns a validator of an {@code OffsetDateTime} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_offsetDateTime(String, Function)
	 */
	public static OffsetDateTimeValidator.Reversible _offsetDateTime(
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		return _offsetDateTime("", constraints);
	}

	/**
	 * Returns a validator of an {@code OffsetDateTime} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_offsetDateTime(String, Function)
	 */
	public static OffsetDateTimeValidator.Reversible _offsetDateTime(String name) {
		return _offsetDateTime(name, Function.identity());
	}

	/**
	 * Returns a validator of an {@code OffsetDateTime} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link OffsetDateTimeConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static OffsetDateTimeValidator.Reversible _offsetDateTime(String name,
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		return OffsetDateTimeValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code Short} without a name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_short(Function)
	 */
	public static ShortValidator.Reversible _short() {
		return _short("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code Short} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_short(String, Function)
	 */
	public static ShortValidator.Reversible _short(
			Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		return _short("", constraints);
	}

	/**
	 * Returns a validator of a {@code Short} with the given name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_short(String, Function)
	 */
	public static ShortValidator.Reversible _short(String name) {
		return _short(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code Short} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link ShortConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static ShortValidator.Reversible _short(String name,
			Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		return ShortValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code String} without a name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_string(Function)
	 */
	public static StringValidator.Reversible _string() {
		return _string("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code String} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_string(String, Function)
	 */
	public static StringValidator.Reversible _string(
			Function<? super StringValueConstraint<Arguments1<String>>, ? extends CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return _string("", constraints);
	}

	/**
	 * Returns a validator of a {@code String} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_string(String, Function)
	 */
	public static StringValidator.Reversible _string(String name) {
		return _string(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code String} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link CharSequenceConstraint}.
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
	public static StringValidator.Reversible _string(String name,
			Function<? super StringValueConstraint<Arguments1<String>>, ? extends CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return StringValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code YearMonth} without a name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_yearMonth(Function)
	 */
	public static YearMonthValidator.Reversible _yearMonth() {
		return _yearMonth("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code YearMonth} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_yearMonth(String, Function)
	 */
	public static YearMonthValidator.Reversible _yearMonth(
			Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
		return _yearMonth("", constraints);
	}

	/**
	 * Returns a validator of a {@code YearMonth} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_yearMonth(String, Function)
	 */
	public static YearMonthValidator.Reversible _yearMonth(String name) {
		return _yearMonth(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code YearMonth} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link YearMonthConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static YearMonthValidator.Reversible _yearMonth(String name,
			Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
		return YearMonthValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code Year} without a name and without constraints, which
	 * accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_year(Function)
	 */
	public static YearValidator.Reversible _year() {
		return _year("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code Year} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_year(String, Function)
	 */
	public static YearValidator.Reversible _year(
			Function<YearConstraint<Arguments1<Year>>, YearConstraint<Arguments1<Year>>> constraints) {
		return _year("", constraints);
	}

	/**
	 * Returns a validator of a {@code Year} with the given name and without constraints,
	 * which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_year(String, Function)
	 */
	public static YearValidator.Reversible _year(String name) {
		return _year(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code Year} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link YearConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static YearValidator.Reversible _year(String name,
			Function<YearConstraint<Arguments1<Year>>, YearConstraint<Arguments1<Year>>> constraints) {
		return YearValidatorBuilder.of(name, constraints).build();
	}

	/**
	 * Returns a validator of a {@code ZonedDateTime} without a name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_zonedDateTime(Function)
	 */
	public static ZonedDateTimeValidator.Reversible _zonedDateTime() {
		return _zonedDateTime("", Function.identity());
	}

	/**
	 * Returns a validator of a {@code ZonedDateTime} without a name.
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_zonedDateTime(String, Function)
	 */
	public static ZonedDateTimeValidator.Reversible _zonedDateTime(
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		return _zonedDateTime("", constraints);
	}

	/**
	 * Returns a validator of a {@code ZonedDateTime} with the given name and without
	 * constraints, which accepts every value other than {@code null}.
	 * @param name the name used in violation names and messages
	 * @return a validator of the value
	 * @since 0.18.0
	 * @see #_zonedDateTime(String, Function)
	 */
	public static ZonedDateTimeValidator.Reversible _zonedDateTime(String name) {
		return _zonedDateTime(name, Function.identity());
	}

	/**
	 * Returns a validator of a {@code ZonedDateTime} with the given name.
	 *
	 * <p>
	 * A {@code null} value is a violation: the one of a constraint that rejects it, or
	 * else {@code object.notNull}. It is accepted with {@code nullable()},
	 * {@code optional()} or {@code defaultValue(...)} of the validator. Violations use
	 * {@code name} as their name and the message ids of {@link ZonedDateTimeConstraint}.
	 * @param name the name used in violation names and messages
	 * @param constraints function that configures the constraints
	 * @return a validator of the value
	 * @since 0.18.0
	 */
	public static ZonedDateTimeValidator.Reversible _zonedDateTime(String name,
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		return ZonedDateTimeValidatorBuilder.of(name, constraints).build();
	}

	private Yavi() {

	}

}
