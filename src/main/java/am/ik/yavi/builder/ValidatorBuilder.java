/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import am.ik.yavi.constraint.BigDecimalConstraint;
import am.ik.yavi.constraint.BigIntegerConstraint;
import am.ik.yavi.constraint.BooleanConstraint;
import am.ik.yavi.constraint.ByteConstraint;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.constraint.CharacterConstraint;
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
import am.ik.yavi.constraint.YearConstraint;
import am.ik.yavi.constraint.YearMonthConstraint;
import am.ik.yavi.constraint.ZonedDateTimeConstraint;
import am.ik.yavi.constraint.array.BooleanArrayConstraint;
import am.ik.yavi.constraint.array.ByteArrayConstraint;
import am.ik.yavi.constraint.array.CharArrayConstraint;
import am.ik.yavi.constraint.array.DoubleArrayConstraint;
import am.ik.yavi.constraint.array.FloatArrayConstraint;
import am.ik.yavi.constraint.array.IntArrayConstraint;
import am.ik.yavi.constraint.array.LongArrayConstraint;
import am.ik.yavi.constraint.array.ObjectArrayConstraint;
import am.ik.yavi.constraint.array.ShortArrayConstraint;
import am.ik.yavi.core.CollectionValidator;
import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintCondition;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.ConstraintPredicates;
import am.ik.yavi.core.CustomConstraint;
import am.ik.yavi.core.NestedCollectionValidator;
import am.ik.yavi.core.NestedConstraintCondition;
import am.ik.yavi.core.NestedConstraintPredicates;
import am.ik.yavi.core.NestedValidator;
import am.ik.yavi.core.NullAs;
import am.ik.yavi.core.PathSegment;
import am.ik.yavi.core.Validatable;
import am.ik.yavi.core.Validator;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.core.ViolatedArguments;
import am.ik.yavi.core.ViolationMessage;
import am.ik.yavi.fn.Pair;
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;
import am.ik.yavi.meta.BigDecimalConstraintMeta;
import am.ik.yavi.meta.BigIntegerConstraintMeta;
import am.ik.yavi.meta.BooleanConstraintMeta;
import am.ik.yavi.meta.ByteConstraintMeta;
import am.ik.yavi.meta.CharacterConstraintMeta;
import am.ik.yavi.meta.DoubleConstraintMeta;
import am.ik.yavi.meta.FloatConstraintMeta;
import am.ik.yavi.meta.InstantConstraintMeta;
import am.ik.yavi.meta.IntegerConstraintMeta;
import am.ik.yavi.meta.LocalDateConstraintMeta;
import am.ik.yavi.meta.LocalDateTimeConstraintMeta;
import am.ik.yavi.meta.LocalTimeConstraintMeta;
import am.ik.yavi.meta.LongConstraintMeta;
import am.ik.yavi.meta.ObjectConstraintMeta;
import am.ik.yavi.meta.OffsetDateTimeConstraintMeta;
import am.ik.yavi.meta.ShortConstraintMeta;
import am.ik.yavi.meta.StringConstraintMeta;
import am.ik.yavi.meta.YearConstraintMeta;
import am.ik.yavi.meta.YearMonthConstraintMeta;
import am.ik.yavi.meta.ZonedDateTimeConstraintMeta;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * Builds a {@link Validator} for a target type with a fluent API.
 *
 * <p>
 * Constraints are added per property. A property is selected with a method reference or
 * lambda, a name used in violation names and messages, and a function that configures the
 * constraint. For example: <pre>{@code
 * Validator<Person> validator = ValidatorBuilder.<Person>of()
 *     .constraint(Person::getName, "name", c -> c.notBlank().lessThanOrEqual(20))
 *     ._integer(Person::getAge, "age", c -> c.greaterThanOrEqual(0).lessThanOrEqual(120))
 *     .build();
 * }</pre>
 *
 * <p>
 * For most value types the overloaded {@code constraint} methods add the constraints, and
 * the methods prefixed with {@code _}, such as {@link #_string}, are equivalent aliases
 * that name the value type in the method name. The prefixed methods can make a call
 * clearer where the overloads would otherwise be ambiguous. The constraint types and
 * their checks and message ids are described in {@code am.ik.yavi.constraint}.
 *
 * <p>
 * A constraint treats a {@code null} property value as valid unless a check that rejects
 * {@code null} is added, such as {@link am.ik.yavi.core.Constraint#notNull()}. Each check
 * reports its violation with the message id and default message of the constraint type,
 * for example {@code charSequence.notBlank}, and the property name is supplied as message
 * argument {@code {0}}.
 *
 * <p>
 * Besides property constraints, the builder supports nested object validation
 * ({@link #nest(Function, String, Validator)}), element validation for collections, maps
 * and arrays ({@link #forEach(ToCollection, String, Validator)}), conditional validation
 * ({@link #constraintOnCondition(ConstraintCondition, Validator)}), constraint groups
 * ({@link #constraintOnGroup(ConstraintGroup, Validator)}), validation of the target
 * itself ({@link #constraintOnTarget(Predicate, String, ViolationMessage)}), custom
 * message formatting ({@link #messageFormatter(MessageFormatter)}), fail-fast mode
 * ({@link #failFast(boolean)}) and conflict handling for repeated property names
 * ({@link #conflictStrategy(ConflictStrategy)}).
 *
 * <p>
 * A builder is mutable and not thread-safe. {@link #build()} creates an immutable
 * {@link Validator} from the current state, and the builder can be used again afterwards.
 *
 * @param <T> the type of the target object to validate
 * @author Toshiaki Maki
 * @since 0.1.0
 * @see Validator
 */
public class ValidatorBuilder<T> {

	private static final String DEFAULT_SEPARATOR = ".";

	final List<CollectionValidator<T, ?, ?>> collectionValidators = new ArrayList<>();

	final List<Pair<ConstraintCondition<T>, Validatable<T>>> conditionalValidators = new ArrayList<>();

	final String messageKeySeparator;

	final List<ConstraintPredicates<T, ?>> predicatesList = new ArrayList<>();

	MessageFormatter messageFormatter = SimpleMessageFormatter.getInstance();

	boolean failFast = false;

	ConflictStrategy conflictStrategy = ConflictStrategy.NOOP;

	/**
	 * Creates a builder that uses {@code "."} as the message key separator.
	 * @see #ValidatorBuilder(String)
	 */
	public ValidatorBuilder() {
		this(DEFAULT_SEPARATOR);
	}

	/**
	 * Creates a builder that uses the given message key separator.
	 *
	 * <p>
	 * The separator joins the names of nested properties, for example
	 * {@code address.street}, in violation names and in the message keys used for
	 * localization.
	 * @param messageKeySeparator the separator used for nested property names
	 */
	public ValidatorBuilder(String messageKeySeparator) {
		this.messageKeySeparator = messageKeySeparator;
	}

	/**
	 * Creates a builder initialized with the state of the given builder.
	 *
	 * <p>
	 * The property predicates, collection validators, conditional validators, message
	 * formatter and fail-fast setting are copied. The conflict strategy is not copied and
	 * is reset to {@link ConflictStrategy#NOOP}.
	 * @param cloningSource the builder whose state is copied
	 * @since 0.10.0
	 */
	public ValidatorBuilder(ValidatorBuilder<T> cloningSource) {
		this(cloningSource.messageKeySeparator);
		this.collectionValidators.addAll(cloningSource.collectionValidators);
		this.conditionalValidators.addAll(cloningSource.conditionalValidators);
		this.predicatesList.addAll(cloningSource.predicatesList);
		this.messageFormatter = cloningSource.messageFormatter;
		this.failFast = cloningSource.failFast;
	}

	/**
	 * Casts this builder to a builder for a subtype of {@code T}.
	 *
	 * <p>
	 * This is an unchecked cast: the constraints are not checked against the subtype and
	 * the class argument is not used. It is useful to reuse a builder for a supertype
	 * when validating a subtype.
	 * @param <S> the subtype of {@code T}
	 * @param clazz the subtype class; used only to specify {@code S}
	 * @return this builder, cast to {@code ValidatorBuilder<S>}
	 */
	@SuppressWarnings("unchecked")
	public <S extends T> ValidatorBuilder<S> cast(Class<S> clazz) {
		return (ValidatorBuilder<S>) this;
	}

	/**
	 * Casts this builder to a builder for a subtype of {@code T}, without a class
	 * argument.
	 *
	 * <p>
	 * This is an unchecked cast: the constraints are not checked against the subtype.
	 * @param <S> the subtype of {@code T}
	 * @return this builder, cast to {@code ValidatorBuilder<S>}
	 * @see #cast(Class)
	 */
	@SuppressWarnings("unchecked")
	public <S extends T> ValidatorBuilder<S> cast() {
		return (ValidatorBuilder<S>) this;
	}

	/**
	 * Creates a new builder. The class argument specifies the type parameter and is not
	 * used otherwise.
	 *
	 * <p>
	 * Example: <pre>{@code
	 * Validator<Person> validator = ValidatorBuilder.of(Person.class)
	 *     .constraint(Person::getName, "name", c -> c.notBlank())
	 *     .build();
	 * }</pre>
	 * @param <X> the type of the object to validate
	 * @param clazz the class of the type to validate; not used
	 * @return a new builder
	 * @see #of()
	 */
	public static <X> ValidatorBuilder<X> of(Class<X> clazz) {
		return new ValidatorBuilder<>();
	}

	/**
	 * Creates a new builder using type inference for the target type.
	 * @param <X> the type of the object to validate
	 * @return a new builder
	 * @see #of(Class)
	 */
	public static <X> ValidatorBuilder<X> of() {
		return new ValidatorBuilder<>();
	}

	/**
	 * Builds a {@link Validator} from the constraints and settings of this builder.
	 *
	 * <p>
	 * Constraints with the same property name are resolved with the configured
	 * {@link ConflictStrategy} before the validator is created. The returned validator is
	 * immutable and thread-safe; later changes to this builder do not affect it.
	 * @return a new validator
	 * @see #conflictStrategy(ConflictStrategy)
	 */
	public Validator<T> build() {
		return new Validator<>(messageKeySeparator,
				new PredicatesList<>(this.conflictStrategy, this.predicatesList).toList(), this.collectionValidators,
				this.conditionalValidators, this.messageFormatter, this.failFast);
	}

	/**
	 * Adds a constraint for a {@code String} property.
	 *
	 * <p>
	 * The checks configured by {@code c} are evaluated against the value extracted by
	 * {@code f} each time the built validator validates a target. Violations use the
	 * message ids of {@link CharSequenceConstraint}, for example
	 * {@code charSequence.notBlank}, with the property name as argument {@code {0}}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint; may add several checks
	 * @return this builder
	 * @see #_string(ToCharSequence, String, Function)
	 * @see #constraint(StringConstraintMeta, Function)
	 */
	public ValidatorBuilder<T> constraint(ToCharSequence<T, String> f, String name,
			Function<CharSequenceConstraint<T, String>, CharSequenceConstraint<T, String>> c) {
		return this.constraint(f, name, c, CharSequenceConstraint::new);
	}

	/**
	 * Adds a constraint for an enum property.
	 *
	 * <p>
	 * The checks configured by {@code c} are evaluated against the value extracted by
	 * {@code f}. Violations use the message ids of {@link EnumConstraint}, for example
	 * {@code object.oneOf}. A {@code null} value is considered valid.
	 * @param <E> the enum type of the property
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.14.0
	 * @see #_enum(toEnum, String, Function)
	 */
	public <E extends Enum<E>> ValidatorBuilder<T> constraint(toEnum<T, E> f, String name,
			Function<EnumConstraint<T, E>, EnumConstraint<T, E>> c) {
		return this.constraint(f, name, c, EnumConstraint::new);
	}

	/**
	 * Adds a constraint for an enum property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(toEnum, String, Function)}.
	 * @param <E> the enum type of the property
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.14.0
	 */
	public <E extends Enum<E>> ValidatorBuilder<T> _enum(toEnum<T, E> f, String name,
			Function<EnumConstraint<T, E>, EnumConstraint<T, E>> c) {
		return this.constraint(f, name, c);
	}

	/**
	 * Adds a constraint for a {@code String} property selected by a generated meta
	 * object. The property name and the extractor are taken from {@code meta}.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.4.0
	 * @see #constraint(ToCharSequence, String, Function)
	 */
	public ValidatorBuilder<T> constraint(StringConstraintMeta<T> meta,
			Function<CharSequenceConstraint<T, String>, CharSequenceConstraint<T, String>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, CharSequenceConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code CharSequence} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToCharSequence, String, Function)} for a
	 * generic {@code CharSequence} subtype.
	 * @param <E> the {@code CharSequence} subtype of the property
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public <E extends CharSequence> ValidatorBuilder<T> _charSequence(ToCharSequence<T, E> f, String name,
			Function<CharSequenceConstraint<T, E>, CharSequenceConstraint<T, E>> c) {
		return this.constraint(f, name, c, CharSequenceConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code String} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToCharSequence, String, Function)}. For
	 * example: <pre>{@code
	 * Validator<Person> validator = ValidatorBuilder.<Person>of()
	 *     ._string(Person::getName, "name", c -> c.notBlank().lessThanOrEqual(100))
	 *     .build();
	 * }</pre>
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _string(ToCharSequence<T, String> f, String name,
			Function<CharSequenceConstraint<T, String>, CharSequenceConstraint<T, String>> c) {
		return this.constraint(f, name, c, CharSequenceConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Boolean} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link BooleanConstraint}, for example
	 * {@code boolean.isTrue}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_boolean(ToBoolean, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToBoolean<T> f, String name,
			Function<BooleanConstraint<T>, BooleanConstraint<T>> c) {
		return this.constraint(f, name, c, BooleanConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Boolean} property selected by a generated meta
	 * object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.4.0
	 * @see #constraint(ToBoolean, String, Function)
	 */
	public ValidatorBuilder<T> constraint(BooleanConstraintMeta<T> meta,
			Function<BooleanConstraint<T>, BooleanConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, BooleanConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Boolean} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToBoolean, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _boolean(ToBoolean<T> f, String name,
			Function<BooleanConstraint<T>, BooleanConstraint<T>> c) {
		return this.constraint(f, name, c, BooleanConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Character} property.
	 *
	 * <p>
	 * The constraint provides the numeric comparisons of {@link CharacterConstraint},
	 * whose violations use the {@code numeric.*} message ids. A {@code null} value is
	 * considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_character(ToCharacter, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToCharacter<T> f, String name,
			Function<CharacterConstraint<T>, CharacterConstraint<T>> c) {
		return this.constraint(f, name, c, CharacterConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Character} property selected by a generated meta
	 * object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.4.0
	 * @see #constraint(ToCharacter, String, Function)
	 */
	public ValidatorBuilder<T> constraint(CharacterConstraintMeta<T> meta,
			Function<CharacterConstraint<T>, CharacterConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, CharacterConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Character} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToCharacter, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _character(ToCharacter<T> f, String name,
			Function<CharacterConstraint<T>, CharacterConstraint<T>> c) {
		return this.constraint(f, name, c, CharacterConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Byte} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link ByteConstraint}, for example
	 * {@code numeric.greaterThan}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_byte(ToByte, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToByte<T> f, String name, Function<ByteConstraint<T>, ByteConstraint<T>> c) {
		return this.constraint(f, name, c, ByteConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Byte} property selected by a generated meta object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.4.0
	 * @see #constraint(ToByte, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ByteConstraintMeta<T> meta,
			Function<ByteConstraint<T>, ByteConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, ByteConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Byte} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToByte, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _byte(ToByte<T> f, String name, Function<ByteConstraint<T>, ByteConstraint<T>> c) {
		return this.constraint(f, name, c, ByteConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Short} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link ShortConstraint}, for example
	 * {@code numeric.greaterThan}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_short(ToShort, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToShort<T> f, String name,
			Function<ShortConstraint<T>, ShortConstraint<T>> c) {
		return this.constraint(f, name, c, ShortConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Short} property selected by a generated meta object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.4.0
	 * @see #constraint(ToShort, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ShortConstraintMeta<T> meta,
			Function<ShortConstraint<T>, ShortConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, ShortConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Short} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToShort, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _short(ToShort<T> f, String name, Function<ShortConstraint<T>, ShortConstraint<T>> c) {
		return this.constraint(f, name, c, ShortConstraint::new);
	}

	/**
	 * Adds a constraint for an {@code Integer} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link IntegerConstraint}, for example
	 * {@code numeric.greaterThan}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_integer(ToInteger, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToInteger<T> f, String name,
			Function<IntegerConstraint<T>, IntegerConstraint<T>> c) {
		return this.constraint(f, name, c, IntegerConstraint::new);
	}

	/**
	 * Adds a constraint for an {@code Integer} property selected by a generated meta
	 * object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.4.0
	 * @see #constraint(ToInteger, String, Function)
	 */
	public ValidatorBuilder<T> constraint(IntegerConstraintMeta<T> meta,
			Function<IntegerConstraint<T>, IntegerConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, IntegerConstraint::new);
	}

	/**
	 * Adds a constraint for an {@code Integer} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToInteger, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _integer(ToInteger<T> f, String name,
			Function<IntegerConstraint<T>, IntegerConstraint<T>> c) {
		return this.constraint(f, name, c, IntegerConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Long} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link LongConstraint}, for example
	 * {@code numeric.greaterThan}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_long(ToLong, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToLong<T> f, String name, Function<LongConstraint<T>, LongConstraint<T>> c) {
		return this.constraint(f, name, c, LongConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Long} property selected by a generated meta object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.4.0
	 * @see #constraint(ToLong, String, Function)
	 */
	public ValidatorBuilder<T> constraint(LongConstraintMeta<T> meta,
			Function<LongConstraint<T>, LongConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, LongConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Long} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToLong, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _long(ToLong<T> f, String name, Function<LongConstraint<T>, LongConstraint<T>> c) {
		return this.constraint(f, name, c, LongConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Float} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link FloatConstraint}, for example
	 * {@code numeric.greaterThan}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_float(ToFloat, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToFloat<T> f, String name,
			Function<FloatConstraint<T>, FloatConstraint<T>> c) {
		return this.constraint(f, name, c, FloatConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Float} property selected by a generated meta object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.4.0
	 * @see #constraint(ToFloat, String, Function)
	 */
	public ValidatorBuilder<T> constraint(FloatConstraintMeta<T> meta,
			Function<FloatConstraint<T>, FloatConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, FloatConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Float} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToFloat, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _float(ToFloat<T> f, String name, Function<FloatConstraint<T>, FloatConstraint<T>> c) {
		return this.constraint(f, name, c, FloatConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Double} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link DoubleConstraint}, for example
	 * {@code numeric.greaterThan}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_double(ToDouble, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToDouble<T> f, String name,
			Function<DoubleConstraint<T>, DoubleConstraint<T>> c) {
		return this.constraint(f, name, c, DoubleConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Double} property selected by a generated meta
	 * object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.4.0
	 * @see #constraint(ToDouble, String, Function)
	 */
	public ValidatorBuilder<T> constraint(DoubleConstraintMeta<T> meta,
			Function<DoubleConstraint<T>, DoubleConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, DoubleConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Double} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToDouble, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _double(ToDouble<T> f, String name,
			Function<DoubleConstraint<T>, DoubleConstraint<T>> c) {
		return this.constraint(f, name, c, DoubleConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code BigInteger} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link BigIntegerConstraint}, for example
	 * {@code numeric.greaterThan}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_bigInteger(ToBigInteger, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToBigInteger<T> f, String name,
			Function<BigIntegerConstraint<T>, BigIntegerConstraint<T>> c) {
		return this.constraint(f, name, c, BigIntegerConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code BigInteger} property selected by a generated meta
	 * object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.4.0
	 * @see #constraint(ToBigInteger, String, Function)
	 */
	public ValidatorBuilder<T> constraint(BigIntegerConstraintMeta<T> meta,
			Function<BigIntegerConstraint<T>, BigIntegerConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, BigIntegerConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code BigInteger} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToBigInteger, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _bigInteger(ToBigInteger<T> f, String name,
			Function<BigIntegerConstraint<T>, BigIntegerConstraint<T>> c) {
		return this.constraint(f, name, c, BigIntegerConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code BigDecimal} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link BigDecimalConstraint}, for example
	 * {@code numeric.greaterThan}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_bigDecimal(ToBigDecimal, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToBigDecimal<T> f, String name,
			Function<BigDecimalConstraint<T>, BigDecimalConstraint<T>> c) {
		return this.constraint(f, name, c, BigDecimalConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code BigDecimal} property selected by a generated meta
	 * object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.4.0
	 * @see #constraint(ToBigDecimal, String, Function)
	 */
	public ValidatorBuilder<T> constraint(BigDecimalConstraintMeta<T> meta,
			Function<BigDecimalConstraint<T>, BigDecimalConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, BigDecimalConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code BigDecimal} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToBigDecimal, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _bigDecimal(ToBigDecimal<T> f, String name,
			Function<BigDecimalConstraint<T>, BigDecimalConstraint<T>> c) {
		return this.constraint(f, name, c, BigDecimalConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code LocalTime} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link LocalTimeConstraint}, for example
	 * {@code temporal.past}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 * @see #_localTime(ToLocalTimeConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToLocalTimeConstraint<T> f, String name,
			Function<LocalTimeConstraint<T>, LocalTimeConstraint<T>> c) {
		return this.constraint(f, name, c, LocalTimeConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code LocalTime} property selected by a generated meta
	 * object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 * @see #constraint(ToLocalTimeConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(LocalTimeConstraintMeta<T> meta,
			Function<LocalTimeConstraint<T>, LocalTimeConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, LocalTimeConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code LocalTime} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToLocalTimeConstraint, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> _localTime(ToLocalTimeConstraint<T> f, String name,
			Function<LocalTimeConstraint<T>, LocalTimeConstraint<T>> c) {
		return this.constraint(f, name, c, LocalTimeConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code LocalDate} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link LocalDateConstraint}, for example
	 * {@code temporal.past}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 * @see #_localDate(ToLocalDateConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToLocalDateConstraint<T> f, String name,
			Function<LocalDateConstraint<T>, LocalDateConstraint<T>> c) {
		return this.constraint(f, name, c, LocalDateConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code LocalDate} property selected by a generated meta
	 * object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 * @see #constraint(ToLocalDateConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(LocalDateConstraintMeta<T> meta,
			Function<LocalDateConstraint<T>, LocalDateConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, LocalDateConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code LocalDate} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToLocalDateConstraint, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> _localDate(ToLocalDateConstraint<T> f, String name,
			Function<LocalDateConstraint<T>, LocalDateConstraint<T>> c) {
		return this.constraint(f, name, c, LocalDateConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code LocalDateTime} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link LocalDateTimeConstraint}, for example
	 * {@code temporal.past}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 * @see #_localDateTime(ToLocalDateTimeConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToLocalDateTimeConstraint<T> f, String name,
			Function<LocalDateTimeConstraint<T>, LocalDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, LocalDateTimeConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code LocalDateTime} property selected by a generated meta
	 * object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 * @see #constraint(ToLocalDateTimeConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(LocalDateTimeConstraintMeta<T> meta,
			Function<LocalDateTimeConstraint<T>, LocalDateTimeConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, LocalDateTimeConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code LocalDateTime} property.
	 *
	 * <p>
	 * This is an alias of
	 * {@link #constraint(ToLocalDateTimeConstraint, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> _localDateTime(ToLocalDateTimeConstraint<T> f, String name,
			Function<LocalDateTimeConstraint<T>, LocalDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, LocalDateTimeConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code ZonedDateTime} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link ZonedDateTimeConstraint}, for example
	 * {@code temporal.past}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 * @see #_zonedDateTime(ToZonedDateTimeConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToZonedDateTimeConstraint<T> f, String name,
			Function<ZonedDateTimeConstraint<T>, ZonedDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, ZonedDateTimeConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code ZonedDateTime} property selected by a generated meta
	 * object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 * @see #constraint(ToZonedDateTimeConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ZonedDateTimeConstraintMeta<T> meta,
			Function<ZonedDateTimeConstraint<T>, ZonedDateTimeConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, ZonedDateTimeConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code ZonedDateTime} property.
	 *
	 * <p>
	 * This is an alias of
	 * {@link #constraint(ToZonedDateTimeConstraint, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> _zonedDateTime(ToZonedDateTimeConstraint<T> f, String name,
			Function<ZonedDateTimeConstraint<T>, ZonedDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, ZonedDateTimeConstraint::new);
	}

	/**
	 * Adds a constraint for an {@code OffsetDateTime} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link OffsetDateTimeConstraint}, for example
	 * {@code temporal.past}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 * @see #_offsetDateTime(ToOffsetDateTimeConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToOffsetDateTimeConstraint<T> f, String name,
			Function<OffsetDateTimeConstraint<T>, OffsetDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, OffsetDateTimeConstraint::new);
	}

	/**
	 * Adds a constraint for an {@code OffsetDateTime} property selected by a generated
	 * meta object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 * @see #constraint(ToOffsetDateTimeConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(OffsetDateTimeConstraintMeta<T> meta,
			Function<OffsetDateTimeConstraint<T>, OffsetDateTimeConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, OffsetDateTimeConstraint::new);
	}

	/**
	 * Adds a constraint for an {@code OffsetDateTime} property.
	 *
	 * <p>
	 * This is an alias of
	 * {@link #constraint(ToOffsetDateTimeConstraint, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> _offsetDateTime(ToOffsetDateTimeConstraint<T> f, String name,
			Function<OffsetDateTimeConstraint<T>, OffsetDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, OffsetDateTimeConstraint::new);
	}

	/**
	 * Adds a constraint for an {@code Instant} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link InstantConstraint}, for example
	 * {@code temporal.past}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 * @see #_instant(ToInstantConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToInstantConstraint<T> f, String name,
			Function<InstantConstraint<T>, InstantConstraint<T>> c) {
		return this.constraint(f, name, c, InstantConstraint::new);
	}

	/**
	 * Adds a constraint for an {@code Instant} property selected by a generated meta
	 * object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 * @see #constraint(ToInstantConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(InstantConstraintMeta<T> meta,
			Function<InstantConstraint<T>, InstantConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, InstantConstraint::new);
	}

	/**
	 * Adds a constraint for an {@code Instant} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToInstantConstraint, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> _instant(ToInstantConstraint<T> f, String name,
			Function<InstantConstraint<T>, InstantConstraint<T>> c) {
		return this.constraint(f, name, c, InstantConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code YearMonth} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link YearMonthConstraint}, for example
	 * {@code temporal.past}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.11.0
	 * @see #_yearMonth(ToYearMonthConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToYearMonthConstraint<T> f, String name,
			Function<YearMonthConstraint<T>, YearMonthConstraint<T>> c) {
		return this.constraint(f, name, c, YearMonthConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code YearMonth} property selected by a generated meta
	 * object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.11.0
	 * @see #constraint(ToYearMonthConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(YearMonthConstraintMeta<T> meta,
			Function<YearMonthConstraint<T>, YearMonthConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, YearMonthConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code YearMonth} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToYearMonthConstraint, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.11.0
	 */
	public ValidatorBuilder<T> _yearMonth(ToYearMonthConstraint<T> f, String name,
			Function<YearMonthConstraint<T>, YearMonthConstraint<T>> c) {
		return this.constraint(f, name, c, YearMonthConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Year} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link YearConstraint}, for example
	 * {@code temporal.past}. A {@code null} value is considered valid.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.11.0
	 * @see #_year(ToYearConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToYearConstraint<T> f, String name,
			Function<YearConstraint<T>, YearConstraint<T>> c) {
		return this.constraint(f, name, c, YearConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Year} property selected by a generated meta object.
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.11.0
	 * @see #constraint(ToYearConstraint, String, Function)
	 */
	public ValidatorBuilder<T> constraint(YearConstraintMeta<T> meta,
			Function<YearConstraint<T>, YearConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, YearConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code Year} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToYearConstraint, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.11.0
	 */
	public ValidatorBuilder<T> _year(ToYearConstraint<T> f, String name,
			Function<YearConstraint<T>, YearConstraint<T>> c) {
		return this.constraint(f, name, c, YearConstraint::new);
	}

	/**
	 * Adds a constraint for a collection property.
	 *
	 * <p>
	 * Violations use the message ids of {@link CollectionConstraint}, for example
	 * {@code collection.contains} and {@code container.notEmpty}. Most checks consider a
	 * {@code null} collection valid; {@code notEmpty} rejects it.
	 * @param <L> the collection type of the property
	 * @param <E> the element type of the collection
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_collection(ToCollection, String, Function)
	 */
	public <L extends Collection<E>, E> ValidatorBuilder<T> constraint(ToCollection<T, L, E> f, String name,
			Function<CollectionConstraint<T, L, E>, CollectionConstraint<T, L, E>> c) {
		return this.constraint(f, name, c, CollectionConstraint::new);
	}

	/**
	 * Adds a constraint for a collection property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToCollection, String, Function)}.
	 * @param <L> the collection type of the property
	 * @param <E> the element type of the collection
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public <L extends Collection<E>, E> ValidatorBuilder<T> _collection(ToCollection<T, L, E> f, String name,
			Function<CollectionConstraint<T, L, E>, CollectionConstraint<T, L, E>> c) {
		return this.constraint(f, name, c, CollectionConstraint::new);
	}

	/**
	 * Adds a constraint for a map property.
	 *
	 * <p>
	 * Violations use the message ids of {@link MapConstraint}, for example
	 * {@code map.containsKey} and {@code map.containsValue}. A {@code null} map is
	 * considered valid.
	 * @param <K> the key type of the map
	 * @param <V> the value type of the map
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_map(ToMap, String, Function)
	 */
	public <K, V> ValidatorBuilder<T> constraint(ToMap<T, K, V> f, String name,
			Function<MapConstraint<T, K, V>, MapConstraint<T, K, V>> c) {
		return this.constraint(f, name, c, MapConstraint::new);
	}

	/**
	 * Adds a constraint for a map property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToMap, String, Function)}.
	 * @param <K> the key type of the map
	 * @param <V> the value type of the map
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public <K, V> ValidatorBuilder<T> _map(ToMap<T, K, V> f, String name,
			Function<MapConstraint<T, K, V>, MapConstraint<T, K, V>> c) {
		return this.constraint(f, name, c, MapConstraint::new);
	}

	/**
	 * Adds a constraint for an object array property.
	 *
	 * <p>
	 * Violations use the message ids of {@link ObjectArrayConstraint}, for example
	 * {@code array.contains} and {@code container.notEmpty}. Most checks consider a
	 * {@code null} array valid; {@code notEmpty} rejects it.
	 * @param <E> the element type of the array
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_objectArray(ToObjectArray, String, Function)
	 */
	public <E> ValidatorBuilder<T> constraint(ToObjectArray<T, E> f, String name,
			Function<ObjectArrayConstraint<T, E>, ObjectArrayConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectArrayConstraint::new);
	}

	/**
	 * Adds a constraint for an object array property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToObjectArray, String, Function)}.
	 * @param <E> the element type of the array
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public <E> ValidatorBuilder<T> _objectArray(ToObjectArray<T, E> f, String name,
			Function<ObjectArrayConstraint<T, E>, ObjectArrayConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code boolean[]} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link BooleanArrayConstraint}, for example
	 * {@code container.notEmpty}. Most checks consider a {@code null} array valid;
	 * {@code notEmpty} rejects it.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_booleanArray(ToBooleanArray, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToBooleanArray<T> f, String name,
			Function<BooleanArrayConstraint<T>, BooleanArrayConstraint<T>> c) {
		return this.constraint(f, name, c, BooleanArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code boolean[]} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToBooleanArray, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _booleanArray(ToBooleanArray<T> f, String name,
			Function<BooleanArrayConstraint<T>, BooleanArrayConstraint<T>> c) {
		return this.constraint(f, name, c, BooleanArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code char[]} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link CharArrayConstraint}, for example
	 * {@code container.notEmpty}. Most checks consider a {@code null} array valid;
	 * {@code notEmpty} rejects it.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_charArray(ToCharArray, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToCharArray<T> f, String name,
			Function<CharArrayConstraint<T>, CharArrayConstraint<T>> c) {
		return this.constraint(f, name, c, CharArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code char[]} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToCharArray, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _charArray(ToCharArray<T> f, String name,
			Function<CharArrayConstraint<T>, CharArrayConstraint<T>> c) {
		return this.constraint(f, name, c, CharArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code byte[]} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link ByteArrayConstraint}, for example
	 * {@code container.notEmpty}. Most checks consider a {@code null} array valid;
	 * {@code notEmpty} rejects it.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_byteArray(ToByteArray, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToByteArray<T> f, String name,
			Function<ByteArrayConstraint<T>, ByteArrayConstraint<T>> c) {
		return this.constraint(f, name, c, ByteArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code byte[]} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToByteArray, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _byteArray(ToByteArray<T> f, String name,
			Function<ByteArrayConstraint<T>, ByteArrayConstraint<T>> c) {
		return this.constraint(f, name, c, ByteArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code short[]} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link ShortArrayConstraint}, for example
	 * {@code container.notEmpty}. Most checks consider a {@code null} array valid;
	 * {@code notEmpty} rejects it.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_shortArray(ToShortArray, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToShortArray<T> f, String name,
			Function<ShortArrayConstraint<T>, ShortArrayConstraint<T>> c) {
		return this.constraint(f, name, c, ShortArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code short[]} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToShortArray, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _shortArray(ToShortArray<T> f, String name,
			Function<ShortArrayConstraint<T>, ShortArrayConstraint<T>> c) {
		return this.constraint(f, name, c, ShortArrayConstraint::new);
	}

	/**
	 * Adds a constraint for an {@code int[]} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link IntArrayConstraint}, for example
	 * {@code container.notEmpty}. Most checks consider a {@code null} array valid;
	 * {@code notEmpty} rejects it.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_intArray(ToIntArray, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToIntArray<T> f, String name,
			Function<IntArrayConstraint<T>, IntArrayConstraint<T>> c) {
		return this.constraint(f, name, c, IntArrayConstraint::new);
	}

	/**
	 * Adds a constraint for an {@code int[]} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToIntArray, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _intArray(ToIntArray<T> f, String name,
			Function<IntArrayConstraint<T>, IntArrayConstraint<T>> c) {
		return this.constraint(f, name, c, IntArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code long[]} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link LongArrayConstraint}, for example
	 * {@code container.notEmpty}. Most checks consider a {@code null} array valid;
	 * {@code notEmpty} rejects it.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_longArray(ToLongArray, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToLongArray<T> f, String name,
			Function<LongArrayConstraint<T>, LongArrayConstraint<T>> c) {
		return this.constraint(f, name, c, LongArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code long[]} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToLongArray, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _longArray(ToLongArray<T> f, String name,
			Function<LongArrayConstraint<T>, LongArrayConstraint<T>> c) {
		return this.constraint(f, name, c, LongArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code float[]} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link FloatArrayConstraint}, for example
	 * {@code container.notEmpty}. Most checks consider a {@code null} array valid;
	 * {@code notEmpty} rejects it.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_floatArray(ToFloatArray, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToFloatArray<T> f, String name,
			Function<FloatArrayConstraint<T>, FloatArrayConstraint<T>> c) {
		return this.constraint(f, name, c, FloatArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code float[]} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToFloatArray, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _floatArray(ToFloatArray<T> f, String name,
			Function<FloatArrayConstraint<T>, FloatArrayConstraint<T>> c) {
		return this.constraint(f, name, c, FloatArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code double[]} property.
	 *
	 * <p>
	 * Violations use the message ids of {@link DoubleArrayConstraint}, for example
	 * {@code container.notEmpty}. Most checks consider a {@code null} array valid;
	 * {@code notEmpty} rejects it.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_doubleArray(ToDoubleArray, String, Function)
	 */
	public ValidatorBuilder<T> constraint(ToDoubleArray<T> f, String name,
			Function<DoubleArrayConstraint<T>, DoubleArrayConstraint<T>> c) {
		return this.constraint(f, name, c, DoubleArrayConstraint::new);
	}

	/**
	 * Adds a constraint for a {@code double[]} property.
	 *
	 * <p>
	 * This is an alias of {@link #constraint(ToDoubleArray, String, Function)}.
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 */
	public ValidatorBuilder<T> _doubleArray(ToDoubleArray<T> f, String name,
			Function<DoubleArrayConstraint<T>, DoubleArrayConstraint<T>> c) {
		return this.constraint(f, name, c, DoubleArrayConstraint::new);
	}

	/**
	 * Adds a value validator that is applied only when the given condition is met.
	 *
	 * <p>
	 * The condition is evaluated with the target object and the constraint context
	 * supplied to the validation call. When it returns {@code true}, the value validator
	 * is applied to the target; the value it produces is discarded and only its
	 * violations are reported.
	 * @param <R> the type of the value produced by the value validator
	 * @param condition the condition that decides whether the validator is applied
	 * @param applicative the value validator to apply when the condition is met
	 * @return this builder
	 * @since 0.11.0
	 * @see #constraintOnCondition(ConstraintCondition, Validator)
	 */
	public <R> ValidatorBuilder<T> constraintOnCondition(ConstraintCondition<T> condition,
			ValueValidator<T, R> applicative) {
		this.conditionalValidators.add(new Pair<>(condition, Validatable.from(applicative)));
		return this;
	}

	/**
	 * Adds a validator that is applied only when the given condition is met.
	 *
	 * <p>
	 * The condition is evaluated with the target object and the constraint context
	 * supplied to the validation call. When it returns {@code true}, the given validator
	 * validates the target and its violations are reported with the names of this
	 * builder's prefix and collection index.
	 * <p>
	 * Example: <pre>{@code
	 * Validator<Payment> validator = ValidatorBuilder.<Payment>of()
	 *     .constraint(Payment::getMethod, "method", c -> c.notBlank())
	 *     .constraintOnCondition(
	 *         payment -> "card".equals(payment.getMethod()),
	 *         ValidatorBuilder.<Payment>of()
	 *             .constraint(Payment::getCardNumber, "cardNumber", c -> c.notBlank().pattern("[0-9]{16}"))
	 *             .constraint(Payment::getCardExpiry, "cardExpiry", c -> c.notBlank().pattern("[0-9]{2}/[0-9]{2}"))
	 *             .build())
	 *     .build();
	 * }</pre>
	 * @param condition the condition that decides whether the validator is applied
	 * @param validator the validator to apply when the condition is met
	 * @return this builder
	 * @see ConstraintCondition
	 */
	public ValidatorBuilder<T> constraintOnCondition(ConstraintCondition<T> condition, Validator<T> validator) {
		this.conditionalValidators.add(new Pair<>(condition, validator));
		return this;
	}

	/**
	 * Adds a conditional validator defined by a builder converter.
	 *
	 * <p>
	 * A new builder is created, passed to {@code converter} and built into a validator,
	 * which is then registered like
	 * {@link #constraintOnCondition(ConstraintCondition, Validator)}.
	 * @param condition the condition that decides whether the validator is applied
	 * @param converter a function that configures a new builder with the conditional
	 * constraints
	 * @return this builder
	 * @see #constraintOnCondition(ConstraintCondition, Validator)
	 */
	public ValidatorBuilder<T> constraintOnCondition(ConstraintCondition<T> condition,
			ValidatorBuilderConverter<T> converter) {
		ValidatorBuilder<T> builder = converter.apply(new ValidatorBuilder<>(this.messageKeySeparator));
		Validator<T> validator = builder.build();
		return this.constraintOnCondition(condition, validator);
	}

	/**
	 * Adds a value validator that is applied only when the given constraint group is
	 * active.
	 *
	 * <p>
	 * This is equivalent to
	 * {@code constraintOnCondition(group.toCondition(), applicative)}: the validator is
	 * applied when the group passed to the validation call has the same name as
	 * {@code group}. The value produced by the value validator is discarded.
	 * @param <R> the type of the value produced by the value validator
	 * @param group the constraint group that activates the validator
	 * @param applicative the value validator to apply when the group is active
	 * @return this builder
	 * @see #constraintOnGroup(ConstraintGroup, Validator)
	 */
	public <R> ValidatorBuilder<T> constraintOnGroup(ConstraintGroup group, ValueValidator<T, R> applicative) {
		return this.constraintOnCondition(group.toCondition(), applicative);
	}

	/**
	 * Adds a validator that is applied only when the given constraint group is active.
	 *
	 * <p>
	 * This is equivalent to
	 * {@code constraintOnCondition(group.toCondition(), validator)}: the validator is
	 * applied when the group passed to the validation call has the same name as
	 * {@code group}. Groups are created with {@link ConstraintGroup#of(String)} or by
	 * implementing {@link ConstraintGroup} in an enum, and are passed to a validation
	 * method such as {@code validator.validate(target, group)}.
	 * <p>
	 * Example: <pre>{@code
	 * Validator<Person> validator = ValidatorBuilder.<Person>of()
	 *     .constraint(Person::getName, "name", c -> c.notBlank())
	 *     .constraintOnGroup(ConstraintGroup.of("CREATE"),
	 *         ValidatorBuilder.<Person>of()
	 *             .constraint(Person::getPassword, "password", c -> c.notBlank().greaterThanOrEqual(8))
	 *             .build())
	 *     .build();
	 *
	 * ConstraintViolations violations = validator.validate(person, ConstraintGroup.of("CREATE"));
	 * }</pre>
	 * @param group the constraint group that activates the validator
	 * @param validator the validator to apply when the group is active
	 * @return this builder
	 * @see ConstraintGroup
	 */
	public ValidatorBuilder<T> constraintOnGroup(ConstraintGroup group, Validator<T> validator) {
		return this.constraintOnCondition(group.toCondition(), validator);
	}

	/**
	 * Adds a group-specific validator defined by a builder converter.
	 *
	 * <p>
	 * This is equivalent to
	 * {@code constraintOnCondition(group.toCondition(), converter)}: a new builder is
	 * configured by {@code converter} and applied only when the group is active.
	 * @param group the constraint group that activates the validator
	 * @param converter a function that configures a new builder with the group-specific
	 * constraints
	 * @return this builder
	 * @see #constraintOnGroup(ConstraintGroup, Validator)
	 */
	public ValidatorBuilder<T> constraintOnGroup(ConstraintGroup group, ValidatorBuilderConverter<T> converter) {
		return this.constraintOnCondition(group.toCondition(), converter);
	}

	/**
	 * Adds an object constraint for a property of any type.
	 *
	 * <p>
	 * The checks configured by {@code c} are evaluated against the value extracted by
	 * {@code f}. Violations use the message ids of {@link ObjectConstraint}, for example
	 * {@code object.notNull}. A {@code null} value is considered valid unless the check
	 * rejects it.
	 * @param <E> the type of the property
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #_object(Function, String, Function)
	 */
	public <E> ValidatorBuilder<T> constraintOnObject(Function<T, @Nullable E> f, String name,
			Function<ObjectConstraint<T, E>, ObjectConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectConstraint::new);
	}

	/**
	 * Adds an object constraint for a property of any type.
	 *
	 * <p>
	 * This is an alias of {@link #constraintOnObject(Function, String, Function)}.
	 * @param <E> the type of the property
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.4.0
	 */
	public <E> ValidatorBuilder<T> _object(Function<T, @Nullable E> f, String name,
			Function<ObjectConstraint<T, E>, ObjectConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectConstraint::new);
	}

	/**
	 * Adds an object constraint for a property selected by a generated meta object.
	 * @param <E> the type of the property
	 * @param meta the generated metadata for the property
	 * @param c function that configures the constraint
	 * @return this builder
	 * @see #constraintOnObject(Function, String, Function)
	 */
	public <E> ValidatorBuilder<T> constraint(ObjectConstraintMeta<T, E> meta,
			Function<ObjectConstraint<T, E>, ObjectConstraint<T, E>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, ObjectConstraint::new);
	}

	/**
	 * Adds a predicate that validates the target object itself.
	 *
	 * <p>
	 * The predicate is evaluated against the target, not against a property. A violation
	 * is reported under {@code name} with the given message; the arguments returned by
	 * {@code violatedArguments} are inserted between the property name ({@code {0}}) and
	 * the target as the last argument.
	 * @param predicate the predicate that has to return {@code true} for a valid target
	 * @param name the name used in the violation
	 * @param violatedArguments supplies the message arguments for the violated target
	 * @param violationMessage the message id and default message of the violation
	 * @return this builder
	 */
	public ValidatorBuilder<T> constraintOnTarget(Predicate<T> predicate, String name,
			ViolatedArguments<T> violatedArguments, ViolationMessage violationMessage) {
		Deque<ConstraintPredicate<T>> predicates = new LinkedList<>();
		predicates.add(ConstraintPredicate.of(predicate, violationMessage, violatedArguments, NullAs.INVALID));
		this.predicatesList.add(new ConstraintPredicates<>(Function.identity(), name, predicates));
		return this;
	}

	/**
	 * Adds a predicate that validates the target object itself, without extra message
	 * arguments.
	 * @param predicate the predicate that has to return {@code true} for a valid target
	 * @param name the name used in the violation
	 * @param violationMessage the message id and default message of the violation
	 * @return this builder
	 * @see #constraintOnTarget(Predicate, String, ViolatedArguments, ViolationMessage)
	 */
	public ValidatorBuilder<T> constraintOnTarget(Predicate<T> predicate, String name,
			ViolationMessage violationMessage) {
		return this.constraintOnTarget(predicate, name, violatedValue -> CustomConstraint.EMPTY_ARRAY,
				violationMessage);
	}

	/**
	 * Adds a predicate that validates the target object itself, with a message key and
	 * default message.
	 * @param predicate the predicate that has to return {@code true} for a valid target
	 * @param name the name used in the violation
	 * @param messageKey the message id used for message lookup
	 * @param defaultMessage the default message format
	 * @return this builder
	 * @see #constraintOnTarget(Predicate, String, ViolationMessage)
	 */
	public ValidatorBuilder<T> constraintOnTarget(Predicate<T> predicate, String name, String messageKey,
			String defaultMessage) {
		return this.constraintOnTarget(predicate, name, ViolationMessage.of(messageKey, defaultMessage));
	}

	/**
	 * Adds a {@link CustomConstraint} that validates the target object itself. The custom
	 * constraint supplies the predicate, the message and the message arguments.
	 * @param customConstraint the custom constraint applied to the target
	 * @param name the name used in the violation
	 * @return this builder
	 * @see #constraintOnTarget(Predicate, String, ViolatedArguments, ViolationMessage)
	 */
	public ValidatorBuilder<T> constraintOnTarget(CustomConstraint<T> customConstraint, String name) {
		return this.constraintOnTarget(customConstraint, name, customConstraint, customConstraint);
	}

	/**
	 * Adds an object constraint that is applied to the target object itself.
	 *
	 * <p>
	 * This is equivalent to {@code constraintOnObject(Function.identity(), name, c)}: the
	 * checks configured by {@code c} are applied to the target. Violations use the
	 * message ids of {@link ObjectConstraint}, for example {@code object.notNull}.
	 * @param name the name used in violation names and messages
	 * @param c function that configures the constraint
	 * @return this builder
	 * @since 0.7.0
	 * @see #constraintOnObject(Function, String, Function)
	 */
	public ValidatorBuilder<T> constraintOnTarget(String name,
			Function<ObjectConstraint<T, T>, ObjectConstraint<T, T>> c) {
		return this.constraint(Function.identity(), name, c, ObjectConstraint::new);
	}

	/**
	 * Adds a validator for each element of a collection property.
	 *
	 * <p>
	 * The collection is extracted by {@code toCollection} each time a target is
	 * validated. Each element is validated with {@code validator}; violations are
	 * reported with indexed names such as {@code addresses[0].street}. A {@code null}
	 * collection property is reported as an {@code object.notNull} violation under
	 * {@code name}, and a {@code null} element is reported as an {@code object.notNull}
	 * violation under its indexed name.
	 * <p>
	 * When fail-fast mode is enabled, validation stops at the first violation. Otherwise
	 * the violations of all elements are collected.
	 * @param <L> the collection type of the property
	 * @param <E> the element type of the collection
	 * @param toCollection function that extracts the collection from the target object
	 * @param name the property name used in violation names and messages
	 * @param validator the validator applied to each element
	 * @return this builder
	 * @see #forEachIfPresent(ToCollection, String, Validator)
	 * @see #forEach(ToMap, String, Validator)
	 * @see #forEach(ToObjectArray, String, Validator)
	 */
	public <L extends Collection<E>, E> ValidatorBuilder<T> forEach(ToCollection<T, L, E> toCollection, String name,
			Validator<E> validator) {
		return this.forEach(toCollection, name, validator, NullAs.INVALID);
	}

	/**
	 * Adds an element validator defined by a builder converter for a collection property.
	 *
	 * <p>
	 * A new builder is created, passed to {@code converter} and built into the element
	 * validator, which is then registered like
	 * {@link #forEach(ToCollection, String, Validator)}.
	 * @param <L> the collection type of the property
	 * @param <E> the element type of the collection
	 * @param toCollection function that extracts the collection from the target object
	 * @param name the property name used in violation names and messages
	 * @param converter a function that configures a new builder with the element
	 * constraints
	 * @return this builder
	 * @see #forEach(ToCollection, String, Validator)
	 */
	public <L extends Collection<E>, E> ValidatorBuilder<T> forEach(ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEach(toCollection, name, converter, NullAs.INVALID);
	}

	/**
	 * Adds a validator for each value of a map property.
	 *
	 * <p>
	 * Only the map values are validated; keys are ignored. In all other respects this
	 * behaves like {@link #forEach(ToCollection, String, Validator)}.
	 * @param <K> the key type of the map
	 * @param <V> the value type of the map
	 * @param toMap function that extracts the map from the target object
	 * @param name the property name used in violation names and messages
	 * @param validator the validator applied to each value
	 * @return this builder
	 * @see #forEach(ToCollection, String, Validator)
	 */
	public <K, V> ValidatorBuilder<T> forEach(ToMap<T, K, V> toMap, String name, Validator<V> validator) {
		return this.forEach(this.toMapToCollection(toMap), name, validator);
	}

	/**
	 * Adds a value validator defined by a builder converter for a map property.
	 * @param <K> the key type of the map
	 * @param <V> the value type of the map
	 * @param toMap function that extracts the map from the target object
	 * @param name the property name used in violation names and messages
	 * @param converter a function that configures a new builder with the value
	 * constraints
	 * @return this builder
	 * @see #forEach(ToCollection, String, ValidatorBuilderConverter)
	 */
	public <K, V> ValidatorBuilder<T> forEach(ToMap<T, K, V> toMap, String name,
			ValidatorBuilderConverter<V> converter) {
		return this.forEach(this.toMapToCollection(toMap), name, converter);
	}

	/**
	 * Adds a validator for each element of an object array property.
	 *
	 * <p>
	 * The array is converted to a list and validated as described in
	 * {@link #forEach(ToCollection, String, Validator)}.
	 * @param <E> the element type of the array
	 * @param toObjectArray function that extracts the array from the target object
	 * @param name the property name used in violation names and messages
	 * @param validator the validator applied to each element
	 * @return this builder
	 * @see #forEach(ToCollection, String, Validator)
	 */
	public <E> ValidatorBuilder<T> forEach(ToObjectArray<T, E> toObjectArray, String name, Validator<E> validator) {
		return this.forEach(this.toObjectArrayToCollection(toObjectArray), name, validator);
	}

	/**
	 * Adds an element validator defined by a builder converter for an object array
	 * property.
	 * @param <E> the element type of the array
	 * @param toObjectArray function that extracts the array from the target object
	 * @param name the property name used in violation names and messages
	 * @param converter a function that configures a new builder with the element
	 * constraints
	 * @return this builder
	 * @see #forEach(ToCollection, String, ValidatorBuilderConverter)
	 */
	public <E> ValidatorBuilder<T> forEach(ToObjectArray<T, E> toObjectArray, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEach(this.toObjectArrayToCollection(toObjectArray), name, converter);
	}

	/**
	 * Adds a validator for each element of a collection property, skipping validation
	 * when the collection is {@code null}.
	 *
	 * <p>
	 * Unlike {@link #forEach(ToCollection, String, Validator)}, a {@code null} collection
	 * is considered valid and produces no violation. A {@code null} element inside a
	 * non-null collection is still reported as an {@code object.notNull} violation under
	 * its indexed name.
	 * @param <L> the collection type of the property
	 * @param <E> the element type of the collection
	 * @param toCollection function that extracts the collection from the target object
	 * @param name the property name used in violation names and messages
	 * @param validator the validator applied to each element
	 * @return this builder
	 */
	public <L extends Collection<E>, E> ValidatorBuilder<T> forEachIfPresent(ToCollection<T, L, E> toCollection,
			String name, Validator<E> validator) {
		return this.forEach(toCollection, name, validator, NullAs.VALID);
	}

	/**
	 * Adds an element validator defined by a builder converter for a collection property,
	 * skipping validation when the collection is {@code null}.
	 * @param <L> the collection type of the property
	 * @param <E> the element type of the collection
	 * @param toCollection function that extracts the collection from the target object
	 * @param name the property name used in violation names and messages
	 * @param converter a function that configures a new builder with the element
	 * constraints
	 * @return this builder
	 * @see #forEachIfPresent(ToCollection, String, Validator)
	 */
	public <L extends Collection<E>, E> ValidatorBuilder<T> forEachIfPresent(ToCollection<T, L, E> toCollection,
			String name, ValidatorBuilderConverter<E> converter) {
		return this.forEach(toCollection, name, converter, NullAs.VALID);
	}

	/**
	 * Adds a validator for each value of a map property, skipping validation when the map
	 * is {@code null}.
	 * @param <K> the key type of the map
	 * @param <V> the value type of the map
	 * @param toMap function that extracts the map from the target object
	 * @param name the property name used in violation names and messages
	 * @param validator the validator applied to each value
	 * @return this builder
	 * @see #forEachIfPresent(ToCollection, String, Validator)
	 */
	public <K, V> ValidatorBuilder<T> forEachIfPresent(ToMap<T, K, V> toMap, String name, Validator<V> validator) {
		return this.forEachIfPresent(this.toMapToCollection(toMap), name, validator);
	}

	/**
	 * Adds a value validator defined by a builder converter for a map property, skipping
	 * validation when the map is {@code null}.
	 * @param <K> the key type of the map
	 * @param <V> the value type of the map
	 * @param toMap function that extracts the map from the target object
	 * @param name the property name used in violation names and messages
	 * @param converter a function that configures a new builder with the value
	 * constraints
	 * @return this builder
	 * @see #forEachIfPresent(ToCollection, String, ValidatorBuilderConverter)
	 */
	public <K, V> ValidatorBuilder<T> forEachIfPresent(ToMap<T, K, V> toMap, String name,
			ValidatorBuilderConverter<V> converter) {
		return this.forEachIfPresent(this.toMapToCollection(toMap), name, converter);
	}

	/**
	 * Adds a validator for each element of an object array property, skipping validation
	 * when the array is {@code null}.
	 * @param <E> the element type of the array
	 * @param toObjectArray function that extracts the array from the target object
	 * @param name the property name used in violation names and messages
	 * @param validator the validator applied to each element
	 * @return this builder
	 * @see #forEachIfPresent(ToCollection, String, Validator)
	 */
	public <E> ValidatorBuilder<T> forEachIfPresent(ToObjectArray<T, E> toObjectArray, String name,
			Validator<E> validator) {
		return this.forEachIfPresent(this.toObjectArrayToCollection(toObjectArray), name, validator);
	}

	/**
	 * Adds an element validator defined by a builder converter for an object array
	 * property, skipping validation when the array is {@code null}.
	 * @param <E> the element type of the array
	 * @param toObjectArray function that extracts the array from the target object
	 * @param name the property name used in violation names and messages
	 * @param converter a function that configures a new builder with the element
	 * constraints
	 * @return this builder
	 * @see #forEachIfPresent(ToCollection, String, ValidatorBuilderConverter)
	 */
	public <E> ValidatorBuilder<T> forEachIfPresent(ToObjectArray<T, E> toObjectArray, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEachIfPresent(this.toObjectArrayToCollection(toObjectArray), name, converter);
	}

	/**
	 * Sets the message formatter used by the built validator.
	 *
	 * <p>
	 * The formatter resolves the message text for a violation from its message id and
	 * default message format. When none is set, {@link SimpleMessageFormatter} is used,
	 * which formats the default message without localization.
	 * <p>
	 * Example: <pre>{@code
	 * Validator<User> validator = ValidatorBuilder.<User>of()
	 *     .constraint(User::getName, "name", c -> c.notBlank())
	 *     .messageFormatter(new CustomMessageFormatter())
	 *     .build();
	 * }</pre>
	 * @param messageFormatter the formatter to use; {@code null} restores the default
	 * @return this builder
	 * @see MessageFormatter
	 */
	public ValidatorBuilder<T> messageFormatter(@Nullable MessageFormatter messageFormatter) {
		this.messageFormatter = messageFormatter == null ? SimpleMessageFormatter.getInstance() : messageFormatter;
		return this;
	}

	/**
	 * Sets whether the built validator operates in fail-fast mode.
	 *
	 * <p>
	 * In fail-fast mode, validation returns as soon as the first constraint violation is
	 * found, including in nested and collection validators. When disabled, which is the
	 * default, all violations are collected.
	 * @param failFast whether to enable fail-fast mode
	 * @return this builder
	 * @since 0.8.0
	 */
	public ValidatorBuilder<T> failFast(boolean failFast) {
		this.failFast = failFast;
		return this;
	}

	/**
	 * Sets how constraints with the same property name are handled.
	 *
	 * <p>
	 * The strategy is applied by {@link #build()} for each repeated name.
	 * {@link ConflictStrategy#NOOP} keeps all constraints, while
	 * {@link ConflictStrategy#OVERRIDE} removes the existing constraints and keeps the
	 * new one. The default is {@link ConflictStrategy#NOOP}.
	 * @param conflictStrategy the conflict strategy to use
	 * @return this builder
	 * @since 0.13.0
	 */
	public ValidatorBuilder<T> conflictStrategy(ConflictStrategy conflictStrategy) {
		this.conflictStrategy = conflictStrategy;
		return this;
	}

	/**
	 * Adds a nested validator for a property.
	 *
	 * <p>
	 * The constraints, conditional validators and collection validators of
	 * {@code validator} are appended to this builder and applied to the value extracted
	 * by {@code nested}. Their violation names are prefixed with {@code name} and the
	 * message key separator, for example {@code address.street}. If the nested property
	 * is {@code null}, an {@code object.notNull} violation is reported under {@code name}
	 * and the nested constraints are skipped.
	 * <p>
	 * Example: <pre>{@code
	 * Validator<Address> addressValidator = ValidatorBuilder.<Address>of()
	 *     .constraint(Address::getStreet, "street", c -> c.notBlank())
	 *     .constraint(Address::getCity, "city", c -> c.notBlank())
	 *     .build();
	 *
	 * Validator<Person> personValidator = ValidatorBuilder.<Person>of()
	 *     .constraint(Person::getName, "name", c -> c.notBlank())
	 *     .nest(Person::getAddress, "address", addressValidator)
	 *     .build();
	 * }</pre>
	 * @param <N> the type of the nested property
	 * @param nested function that extracts the nested property from the target object
	 * @param name the property name used in violation names and messages
	 * @param validator the validator whose constraints are applied to the nested property
	 * @return this builder
	 * @see #nestIfPresent(Function, String, Validator)
	 */
	public <N> ValidatorBuilder<T> nest(Function<T, @Nullable N> nested, String name, Validator<N> validator) {
		return this.nest(nested, name, validator, NullAs.INVALID);
	}

	/**
	 * Adds a nested validator for a property selected by a generated meta object. The
	 * property name and extractor are taken from {@code meta}.
	 * @param <N> the type of the nested property
	 * @param meta the generated metadata for the property
	 * @param validator the validator whose constraints are applied to the nested property
	 * @return this builder
	 * @see #nest(Function, String, Validator)
	 */
	public <N> ValidatorBuilder<T> nest(ObjectConstraintMeta<T, N> meta, Validator<N> validator) {
		return this.nest(target -> meta.toValue().apply(target), meta.name(), validator, NullAs.INVALID);
	}

	/**
	 * Adds a nested validator defined by a builder converter for a property.
	 * @param <N> the type of the nested property
	 * @param nested function that extracts the nested property from the target object
	 * @param name the property name used in violation names and messages
	 * @param converter a function that configures a new builder with the nested
	 * constraints
	 * @return this builder
	 * @see #nest(Function, String, Validator)
	 */
	public <N> ValidatorBuilder<T> nest(Function<T, @Nullable N> nested, String name,
			ValidatorBuilderConverter<N> converter) {
		return this.nest(nested, name, converter, NullAs.INVALID);
	}

	/**
	 * Adds a nested validator defined by a builder converter for a property selected by a
	 * generated meta object.
	 * @param <N> the type of the nested property
	 * @param meta the generated metadata for the property
	 * @param converter a function that configures a new builder with the nested
	 * constraints
	 * @return this builder
	 * @see #nest(Function, String, ValidatorBuilderConverter)
	 */
	public <N> ValidatorBuilder<T> nest(ObjectConstraintMeta<T, N> meta, ValidatorBuilderConverter<N> converter) {
		return this.nest(target -> meta.toValue().apply(target), meta.name(), converter, NullAs.INVALID);
	}

	/**
	 * Adds a nested validator for a property that is validated only when it is not
	 * {@code null}.
	 *
	 * <p>
	 * This works like {@link #nest(Function, String, Validator)} but a {@code null}
	 * property produces no violation and the nested constraints are skipped.
	 * <p>
	 * Example: <pre>{@code
	 * Validator<Person> personValidator = ValidatorBuilder.<Person>of()
	 *     .constraint(Person::getName, "name", c -> c.notBlank())
	 *     .nestIfPresent(Person::getAddress, "address", addressValidator)
	 *     .build();
	 * }</pre>
	 * @param <N> the type of the nested property
	 * @param nested function that extracts the nested property from the target object
	 * @param name the property name used in violation names and messages
	 * @param validator the validator whose constraints are applied to the nested property
	 * @return this builder
	 */
	public <N> ValidatorBuilder<T> nestIfPresent(Function<T, @Nullable N> nested, String name, Validator<N> validator) {
		return this.nest(nested, name, validator, NullAs.VALID);
	}

	/**
	 * Adds a nested validator for a property selected by a generated meta object,
	 * validating only when the property is not {@code null}.
	 * @param <N> the type of the nested property
	 * @param meta the generated metadata for the property
	 * @param validator the validator whose constraints are applied to the nested property
	 * @return this builder
	 * @see #nestIfPresent(Function, String, Validator)
	 */
	public <N> ValidatorBuilder<T> nestIfPresent(ObjectConstraintMeta<T, N> meta, Validator<N> validator) {
		return this.nest(target -> meta.toValue().apply(target), meta.name(), validator, NullAs.VALID);
	}

	/**
	 * Adds a nested validator defined by a builder converter for a property, validating
	 * only when the property is not {@code null}.
	 * @param <N> the type of the nested property
	 * @param nested function that extracts the nested property from the target object
	 * @param name the property name used in violation names and messages
	 * @param converter a function that configures a new builder with the nested
	 * constraints
	 * @return this builder
	 * @see #nestIfPresent(Function, String, Validator)
	 */
	public <N> ValidatorBuilder<T> nestIfPresent(Function<T, @Nullable N> nested, String name,
			ValidatorBuilderConverter<N> converter) {
		return this.nest(nested, name, converter, NullAs.VALID);
	}

	/**
	 * Adds a nested validator defined by a builder converter for a property selected by a
	 * generated meta object, validating only when the property is not {@code null}.
	 * @param <N> the type of the nested property
	 * @param meta the generated metadata for the property
	 * @param converter a function that configures a new builder with the nested
	 * constraints
	 * @return this builder
	 * @see #nestIfPresent(Function, String, ValidatorBuilderConverter)
	 */
	public <N> ValidatorBuilder<T> nestIfPresent(ObjectConstraintMeta<T, N> meta,
			ValidatorBuilderConverter<N> converter) {
		return this.nest(target -> meta.toValue().apply(target), meta.name(), converter, NullAs.VALID);
	}

	/**
	 * Adds the predicates configured by {@code c} on a new constraint created by
	 * {@code supplier}.
	 * @param <V> the type of the property value
	 * @param <C> the type of the constraint
	 * @param f function that extracts the property from the target object
	 * @param name the property name used in violation names and messages
	 * @param c function that configures the constraint
	 * @param supplier creates the constraint instance
	 * @return this builder
	 */
	protected final <V, C extends Constraint<T, V, C>> ValidatorBuilder<T> constraint(
			Function<T, ? extends @Nullable V> f, String name, Function<C, C> c, Supplier<C> supplier) {
		C constraint = supplier.get();
		Deque<ConstraintPredicate<V>> predicates = c.apply(constraint).predicates();
		this.predicatesList.add(new ConstraintPredicates<>(f, name, predicates));
		return this;
	}

	/**
	 * Adds an element validator defined by a builder converter for a collection property.
	 * @param <L> the collection type of the property
	 * @param <E> the element type of the collection
	 * @param toCollection function that extracts the collection from the target object
	 * @param name the property name used in violation names and messages
	 * @param converter a function that configures a new builder with the element
	 * constraints
	 * @param nullAs whether a {@code null} collection is skipped or rejected
	 * @return this builder
	 */
	protected <L extends Collection<E>, E> ValidatorBuilder<T> forEach(ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter, NullAs nullAs) {
		ValidatorBuilder<E> builder = converter.apply(new ValidatorBuilder<>(this.messageKeySeparator));
		return this.forEach(toCollection, name, builder.build(), nullAs);
	}

	/**
	 * Adds a validator for each element of a collection property.
	 * @param <L> the collection type of the property
	 * @param <E> the element type of the collection
	 * @param toCollection function that extracts the collection from the target object
	 * @param name the property name used in violation names and messages
	 * @param validator the validator applied to each element
	 * @param nullAs whether a {@code null} collection is skipped or rejected
	 * @return this builder
	 * @see #forEach(ToCollection, String, Validator)
	 */
	protected final <L extends Collection<E>, E> ValidatorBuilder<T> forEach(ToCollection<T, L, E> toCollection,
			String name, Validator<E> validator, NullAs nullAs) {
		if (!nullAs.skipNull()) {
			this.constraintOnObject(toCollection, name, Constraint::notNull);
		}
		this.collectionValidators.add(new CollectionValidator<>(toCollection, name, validator));
		return this;
	}

	/**
	 * Adds a nested validator defined by a builder converter for a property.
	 * @param <N> the type of the nested property
	 * @param nested function that extracts the nested property from the target object
	 * @param name the property name used in violation names and messages
	 * @param converter a function that configures a new builder with the nested
	 * constraints
	 * @param nullAs whether a {@code null} nested value is skipped or rejected
	 * @return this builder
	 */
	protected final <N> ValidatorBuilder<T> nest(Function<T, @Nullable N> nested, String name,
			ValidatorBuilderConverter<N> converter, NullAs nullAs) {
		if (!nullAs.skipNull()) {
			this.constraintOnObject(nested, name, Constraint::notNull);
		}
		ValidatorBuilder<N> builder = converter.apply(new ValidatorBuilder<>(this.messageKeySeparator));
		builder.predicatesList.forEach(this.appendNestedPredicates(nested, name, false));
		builder.conditionalValidators.forEach(this.appendNestedConditionalValidator(nested, name));
		builder.collectionValidators.forEach(appendNestedCollectionValidator(nested, name));
		return this;
	}

	/**
	 * Adds a nested validator for a property.
	 * @param <N> the type of the nested property
	 * @param nested function that extracts the nested property from the target object
	 * @param name the property name used in violation names and messages
	 * @param validator the validator whose constraints are applied to the nested property
	 * @param nullAs whether a {@code null} nested value is skipped or rejected
	 * @return this builder
	 * @see #nest(Function, String, Validator)
	 */
	protected final <N> ValidatorBuilder<T> nest(Function<T, @Nullable N> nested, String name, Validator<N> validator,
			NullAs nullAs) {
		if (!nullAs.skipNull()) {
			this.constraintOnObject(nested, name, Constraint::notNull);
		}
		validator.forEachPredicates(this.appendNestedPredicates(nested, name, validator.isFailFast()));
		validator.forEachConditionalValidator(this.appendNestedConditionalValidator(nested, name));
		validator.forEachCollectionValidator(appendNestedCollectionValidator(nested, name));
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <N> Consumer<ConstraintPredicates<N, ?>> appendNestedPredicates(Function<T, @Nullable N> nested,
			String name, boolean failFast) {
		return predicates -> {
			String nestedName = name + this.messageKeySeparator + predicates.name();
			ConstraintPredicates<T, ?> constraintPredicates = new NestedConstraintPredicates(
					this.toNestedValue(nested, predicates), nestedName,
					PathSegment.concat(PathSegment.of(name), predicates.path()), predicates.predicates(),
					this.toNestedFunction(nested, predicates), failFast);
			this.predicatesList.add(constraintPredicates);
		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <N> Function<T, @Nullable Object> toNestedFunction(Function<T, @Nullable N> nested,
			ConstraintPredicates<N, ?> predicates) {
		if (predicates instanceof NestedConstraintPredicates) {
			return target -> {
				N nestedValue = nested.apply(target);
				if (nestedValue == null) {
					return null;
				}
				return (N) ((NestedConstraintPredicates) predicates).nestedValue(nestedValue);
			};
		}
		return target -> nested.apply(target);
	}

	private <N> Consumer<Pair<ConstraintCondition<N>, Validatable<N>>> appendNestedConditionalValidator(
			Function<T, @Nullable N> nested, String name) {
		return conditionalValidator -> {
			final ConstraintCondition<T> condition = new NestedConstraintCondition<>(nested,
					conditionalValidator.first());
			final Validatable<N> validator = conditionalValidator.second();
			final String nestedPrefix = toNestedPrefix(name, validator);
			final List<PathSegment> nestedPrefixPath = (validator instanceof NestedValidator<?, ?>nestedValidator)
					? PathSegment.concat(PathSegment.of(name), nestedValidator.getPrefixPath()) : PathSegment.of(name);
			final Validatable<T> v = new NestedValidator<>(nested, validator, nestedPrefix, nestedPrefixPath);
			this.conditionalValidators.add(new Pair<>(condition, v));
		};
	}

	private <N> String toNestedPrefix(String name, Validatable<N> validator) {
		if (validator instanceof NestedValidator) {
			final NestedValidator<?, ?> nestedValidator = (NestedValidator<?, ?>) validator;
			return name + this.messageKeySeparator + nestedValidator.getPrefix();
		}
		return name + this.messageKeySeparator;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <N> Consumer<CollectionValidator<N, ?, ?>> appendNestedCollectionValidator(Function<T, @Nullable N> nested,
			String name) {
		return collectionValidator -> {
			String nestedName = name + this.messageKeySeparator + collectionValidator.name();
			CollectionValidator<T, ?, ?> validator = new NestedCollectionValidator(
					toNestedCollection(nested, collectionValidator), nestedName,
					PathSegment.concat(PathSegment.of(name), collectionValidator.path()),
					collectionValidator.validator(), nested);
			this.collectionValidators.add(validator);
		};
	}

	private <K, V> ToCollection<T, Collection<V>, V> toMapToCollection(ToMap<T, K, V> toMap) {
		return t -> {
			Map<K, V> map = toMap.apply(t);
			if (map == null) {
				return null;
			}
			return map.values();
		};
	}

	private <N> Function<T, @Nullable Object> toNestedValue(Function<T, @Nullable N> nested,
			ConstraintPredicates<N, ?> predicates) {
		return (T target) -> {
			N nestedValue = nested.apply(target);
			if (nestedValue == null) {
				return null;
			}
			return predicates.toValue().apply(nestedValue);
		};
	}

	private <N, C extends Collection<?>> Function<T, @Nullable C> toNestedCollection(Function<T, @Nullable N> nested,
			CollectionValidator<N, C, ?> validator) {
		return (T target) -> {
			N nestedCollection = nested.apply(target);
			if (nestedCollection == null) {
				return null;
			}
			return validator.toCollection().apply(nestedCollection);
		};
	}

	private <E> ToCollection<T, Collection<E>, E> toObjectArrayToCollection(ToObjectArray<T, E> toObjectArray) {
		return t -> {
			E[] array = toObjectArray.apply(t);
			if (array == null) {
				return null;
			}
			return Arrays.asList(array);
		};
	}

	/**
	 * Extracts a {@code BigDecimal} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToBigDecimal<T> extends Function<T, @Nullable BigDecimal> {

	}

	/**
	 * Extracts a {@code BigInteger} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToBigInteger<T> extends Function<T, @Nullable BigInteger> {

	}

	/**
	 * Extracts a {@code Boolean} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToBoolean<T> extends Function<T, @Nullable Boolean> {

	}

	/**
	 * Extracts a {@code boolean[]} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToBooleanArray<T> extends Function<T, boolean @Nullable []> {

	}

	/**
	 * Extracts a {@code Byte} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToByte<T> extends Function<T, @Nullable Byte> {

	}

	/**
	 * Extracts a {@code byte[]} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToByteArray<T> extends Function<T, byte @Nullable []> {

	}

	/**
	 * Extracts a {@code char[]} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToCharArray<T> extends Function<T, char @Nullable []> {

	}

	/**
	 * Extracts a {@code CharSequence} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @param <E> the {@code CharSequence} subtype of the property
	 * @since 0.1.0
	 */
	public interface ToCharSequence<T, E extends CharSequence> extends Function<T, @Nullable E> {

	}

	/**
	 * Extracts an enum property from a target object.
	 *
	 * <p>
	 * The name of this interface starts with a lower-case letter, unlike the other
	 * extraction interfaces, for compatibility with earlier releases.
	 *
	 * @param <T> the type of the target object
	 * @param <E> the enum type of the property
	 * @since 0.14.0
	 */
	public interface toEnum<T, E extends Enum> extends Function<T, @Nullable E> {

	}

	/**
	 * Extracts a {@code Character} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToCharacter<T> extends Function<T, @Nullable Character> {

	}

	/**
	 * Extracts a collection property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @param <L> the collection type of the property
	 * @param <E> the element type of the collection
	 * @since 0.1.0
	 */
	public interface ToCollection<T, L extends Collection<E>, E> extends Function<T, @Nullable L> {

	}

	/**
	 * Extracts a {@code Double} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToDouble<T> extends Function<T, @Nullable Double> {

	}

	/**
	 * Extracts a {@code double[]} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToDoubleArray<T> extends Function<T, double @Nullable []> {

	}

	/**
	 * Extracts a {@code Float} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToFloat<T> extends Function<T, @Nullable Float> {

	}

	/**
	 * Extracts a {@code float[]} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToFloatArray<T> extends Function<T, float @Nullable []> {

	}

	/**
	 * Extracts an {@code int[]} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToIntArray<T> extends Function<T, int @Nullable []> {

	}

	/**
	 * Extracts an {@code Integer} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToInteger<T> extends Function<T, @Nullable Integer> {

	}

	/**
	 * Extracts a {@code Long} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToLong<T> extends Function<T, @Nullable Long> {

	}

	/**
	 * Extracts a {@code long[]} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToLongArray<T> extends Function<T, long @Nullable []> {

	}

	/**
	 * Extracts a map property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @param <K> the key type of the map
	 * @param <V> the value type of the map
	 * @since 0.1.0
	 */
	public interface ToMap<T, K, V> extends Function<T, @Nullable Map<K, V>> {

	}

	/**
	 * Extracts an object array property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @param <E> the element type of the array
	 * @since 0.1.0
	 */
	public interface ToObjectArray<T, E> extends Function<T, E @Nullable []> {

	}

	/**
	 * Extracts a {@code Short} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToShort<T> extends Function<T, @Nullable Short> {

	}

	/**
	 * Extracts a {@code short[]} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.1.0
	 */
	public interface ToShortArray<T> extends Function<T, short @Nullable []> {

	}

	/**
	 * Extracts a {@code LocalTime} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.10.0
	 */
	public interface ToLocalTimeConstraint<T> extends Function<T, @Nullable LocalTime> {

	}

	/**
	 * Extracts a {@code ZonedDateTime} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.10.0
	 */
	public interface ToZonedDateTimeConstraint<T> extends Function<T, @Nullable ZonedDateTime> {

	}

	/**
	 * Extracts an {@code OffsetDateTime} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.10.0
	 */
	public interface ToOffsetDateTimeConstraint<T> extends Function<T, @Nullable OffsetDateTime> {

	}

	/**
	 * Extracts a {@code LocalDateTime} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.10.0
	 */
	public interface ToLocalDateTimeConstraint<T> extends Function<T, @Nullable LocalDateTime> {

	}

	/**
	 * Extracts a {@code LocalDate} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.10.0
	 */
	public interface ToLocalDateConstraint<T> extends Function<T, @Nullable LocalDate> {

	}

	/**
	 * Extracts an {@code Instant} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.10.0
	 */
	public interface ToInstantConstraint<T> extends Function<T, @Nullable Instant> {

	}

	/**
	 * Extracts a {@code Year} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.11.0
	 */
	public interface ToYearConstraint<T> extends Function<T, @Nullable Year> {

	}

	/**
	 * Extracts a {@code YearMonth} property from a target object.
	 *
	 * @param <T> the type of the target object
	 * @since 0.11.0
	 */
	public interface ToYearMonthConstraint<T> extends Function<T, @Nullable YearMonth> {

	}

	/**
	 * Configures a {@link ValidatorBuilder} and returns it.
	 *
	 * <p>
	 * This is used by methods that accept a callback instead of a built validator, such
	 * as {@link #constraintOnCondition(ConstraintCondition, ValidatorBuilderConverter)}
	 * and {@link #nest(Function, String, ValidatorBuilderConverter)}.
	 *
	 * @param <T> the type of the target object to validate
	 * @since 0.1.0
	 */
	public interface ValidatorBuilderConverter<T> extends Function<ValidatorBuilder<T>, ValidatorBuilder<T>> {

	}

	/**
	 * Manages the predicates of a builder and applies the conflict strategy when a name
	 * is repeated.
	 *
	 * <p>
	 * This is an internal helper of {@link #build()}.
	 *
	 * @param <T> the type of the target object to validate
	 * @since 0.13.0
	 */
	static final class PredicatesList<T> {

		private final ConflictStrategy conflictStrategy;

		private final Map<String, List<ConstraintPredicates<T, ?>>> predicatesListMap = new LinkedHashMap<>();

		/**
		 * Creates an empty list.
		 * @param conflictStrategy the strategy applied when a name is repeated
		 */
		public PredicatesList(ConflictStrategy conflictStrategy) {
			this.conflictStrategy = conflictStrategy;
		}

		/**
		 * Creates a list initialized with the given predicates, applying the conflict
		 * strategy as they are added.
		 * @param conflictStrategy the strategy applied when a name is repeated
		 * @param predicatesList the predicates to add
		 */
		public PredicatesList(ConflictStrategy conflictStrategy, List<ConstraintPredicates<T, ?>> predicatesList) {
			this(conflictStrategy);
			predicatesList.forEach(this::add);
		}

		/**
		 * Adds predicates, resolving a conflict with existing predicates of the same name
		 * using the conflict strategy.
		 * @param predicates the predicates to add
		 */
		public void add(ConstraintPredicates<T, ?> predicates) {
			final List<ConstraintPredicates<T, ?>> predicatesList = this.predicatesListMap
				.computeIfAbsent(predicates.name(), s -> new ArrayList<>());
			if (!predicatesList.isEmpty()) {
				this.conflictStrategy.resolveConflict(predicatesList, predicates);
			}
			predicatesList.add(predicates);
		}

		/**
		 * Returns all predicates in insertion order.
		 * @return the predicates grouped by name
		 */
		public List<ConstraintPredicates<T, ?>> toList() {
			final int length = predicatesListMap.values().stream().mapToInt(List::size).sum();
			final List<ConstraintPredicates<T, ?>> list = new ArrayList<>(length);
			for (List<ConstraintPredicates<T, ?>> predicates : predicatesListMap.values()) {
				list.addAll(predicates);
			}
			return list;
		}

	}

}
