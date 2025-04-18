/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

/**
 * A builder class for creating {@link Validator} instances with fluent API.
 * <p>
 * ValidatorBuilder allows construction of validators with various constraints for
 * different types of properties. It supports validation for primitive types, collections,
 * arrays, nested objects, and conditional validation. The builder follows a fluent
 * pattern where constraints can be chained together to create complex validation rules.
 * <p>
 * The builder provides two types of methods:
 * <ul>
 * <li>Methods prefixed with "_" (like {@code _string}, {@code _integer}) - Concise
 * shorthand methods that explicitly specify the type. These methods are provided for
 * cases where type inference fails with the overloaded constraint methods.</li>
 * <li>Methods prefixed with "constraint" - More descriptive constraint methods with the
 * same functionality. These methods are recommended when type inference works
 * correctly.</li>
 * </ul>
 * <p>
 * Properties are referenced using method references or lambda expressions, along with a
 * name for error messages, and a function to define constraints on that property.
 * <p>
 * Example usage: <pre>{@code
 * Validator<Person> validator = ValidatorBuilder.<Person>of()
 *     // Using constraint method with successful type inference
 *     .constraint(Person::getName, "name", c -> c.notBlank().lessThan(20))
 *     // Using _integer method for explicit type declaration
 *     ._integer(Person::getAge, "age", c -> c.greaterThan(0).lessThan(120))
 *     .build();
 * }</pre>
 * <p>
 * Advanced features:
 * <ul>
 * <li>Nested validation for complex object graphs</li>
 * <li>Collection validation for elements in arrays, collections, and maps</li>
 * <li>Conditional validation based on predicate conditions</li>
 * <li>Group-based validation for differentiating validation contexts</li>
 * <li>Custom message formatting</li>
 * <li>Fail-fast capability to abort validation on first error</li>
 * </ul>
 *
 * @param <T> the type of the target object to validate
 * @author Toshiaki Maki
 */
public class ValidatorBuilder<T> implements Cloneable {

	private static final String DEFAULT_SEPARATOR = ".";

	final List<CollectionValidator<T, ?, ?>> collectionValidators = new ArrayList<>();

	final List<Pair<ConstraintCondition<T>, Validatable<T>>> conditionalValidators = new ArrayList<>();

	final String messageKeySeparator;

	final List<ConstraintPredicates<T, ?>> predicatesList = new ArrayList<>();

	MessageFormatter messageFormatter;

	boolean failFast = false;

	ConflictStrategy conflictStrategy = ConflictStrategy.NOOP;

	/**
	 * Constructs a new ValidatorBuilder with the default message key separator. The
	 * default separator is a dot (".").
	 */
	public ValidatorBuilder() {
		this(DEFAULT_SEPARATOR);
	}

	/**
	 * Constructs a new ValidatorBuilder with the specified message key separator.
	 * @param messageKeySeparator the separator used for constructing message keys for
	 * nested properties (e.g., "address.street")
	 */
	public ValidatorBuilder(String messageKeySeparator) {
		this.messageKeySeparator = messageKeySeparator;
	}

	/**
	 * The copy constructor
	 *
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
	 * Casts this ValidatorBuilder to a ValidatorBuilder for a subtype of T. This is
	 * useful when you want to reuse validators for a supertype when validating a subtype.
	 * @param <S> the subtype of T
	 * @param clazz the Class object of the subtype
	 * @return this ValidatorBuilder cast to ValidatorBuilder&lt;S&gt;
	 */
	@SuppressWarnings("unchecked")
	public <S extends T> ValidatorBuilder<S> cast(Class<S> clazz) {
		return (ValidatorBuilder<S>) this;
	}

	/**
	 * Casts this ValidatorBuilder to a ValidatorBuilder for a subtype of T. This method
	 * doesn't require the Class object parameter.
	 * @param <S> the subtype of T
	 * @return this ValidatorBuilder cast to ValidatorBuilder&lt;S&gt;
	 */
	@SuppressWarnings("unchecked")
	public <S extends T> ValidatorBuilder<S> cast() {
		return (ValidatorBuilder<S>) this;
	}

	/**
	 * @return the cloned builder
	 * @deprecated please use the copy constructor
	 * {@link #ValidatorBuilder(ValidatorBuilder)}
	 */
	@Deprecated
	public ValidatorBuilder<T> clone() {
		return new ValidatorBuilder<>(this);
	}

	/**
	 * Factory method for a {@code ValidatorBuilder} to build {@code Validator} instance.
	 * <p>
	 * This method accepts a Class parameter which can be useful for explicitly specifying
	 * the type to validate, although the type parameter can often be inferred from the
	 * context.
	 * <p>
	 * Example: <pre>{@code
	 * Validator<Person> validator = ValidatorBuilder.of(Person.class)
	 *     ._string(Person::getName, "name", c -> c.notBlank())
	 *     .build();
	 * }</pre>
	 * @param <X> the type of the instance to validate
	 * @param clazz the Class object of the type to validate
	 * @return a new ValidatorBuilder instance
	 */
	public static <X> ValidatorBuilder<X> of(Class<X> clazz) {
		return new ValidatorBuilder<>();
	}

	/**
	 * Factory method for a {@code ValidatorBuilder} to build {@code Validator} instance.
	 * <p>
	 * This method uses type inference to determine the type parameter, which makes for a
	 * more concise syntax in most cases.
	 * <p>
	 * Example: <pre>{@code
	 * Validator<Person> validator = ValidatorBuilder.<Person>of()
	 *     .constraint(Person::getName, "name", c -> c.notBlank())
	 *     .build();
	 * }</pre>
	 * @param <X> the type of the instance to validate
	 * @return a new ValidatorBuilder instance
	 */
	public static <X> ValidatorBuilder<X> of() {
		return new ValidatorBuilder<>();
	}

	/**
	 * Builds a Validator instance with all the constraints that have been added to this
	 * builder.
	 * <p>
	 * This is the final step in the builder chain, which creates the actual validator
	 * that can be used to validate objects of type T.
	 * @return a new Validator instance configured with all added constraints
	 */
	public Validator<T> build() {
		return new Validator<>(messageKeySeparator,
				new PredicatesList<>(this.conflictStrategy, this.predicatesList).toList(), this.collectionValidators,
				this.conditionalValidators,
				this.messageFormatter == null ? SimpleMessageFormatter.getInstance() : this.messageFormatter,
				this.failFast);
	}

	public ValidatorBuilder<T> constraint(ToCharSequence<T, String> f, String name,
			Function<CharSequenceConstraint<T, String>, CharSequenceConstraint<T, String>> c) {
		return this.constraint(f, name, c, CharSequenceConstraint::new);
	}

	/**
	 * @since 0.14.0
	 */
	public <E extends Enum<E>> ValidatorBuilder<T> constraint(toEnum<T, E> f, String name,
			Function<EnumConstraint<T, E>, EnumConstraint<T, E>> c) {
		return this.constraint(f, name, c, EnumConstraint::new);
	}

	/**
	 * @since 0.14.0
	 */
	public <E extends Enum<E>> ValidatorBuilder<T> _enum(toEnum<T, E> f, String name,
			Function<EnumConstraint<T, E>, EnumConstraint<T, E>> c) {
		return this.constraint(f, name, c);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(StringConstraintMeta<T> meta,
			Function<CharSequenceConstraint<T, String>, CharSequenceConstraint<T, String>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, CharSequenceConstraint::new);
	}

	public <E extends CharSequence> ValidatorBuilder<T> _charSequence(ToCharSequence<T, E> f, String name,
			Function<CharSequenceConstraint<T, E>, CharSequenceConstraint<T, E>> c) {
		return this.constraint(f, name, c, CharSequenceConstraint::new);
	}

	/**
	 * Adds a constraint for a String property of type T.
	 * <p>
	 * This is a shorthand method for validating String properties with a more concise
	 * method name. For example: <pre>{@code
	 * ValidatorBuilder<Person> builder = ValidatorBuilder.<Person>of()
	 *    ._string(Person::getName, "name", c -> c.notBlank().lessThan(100))
	 * }</pre>
	 * @param f a function to extract the String property from type T
	 * @param name the property name used in error messages
	 * @param c a function to configure constraints on the String property
	 * @return this builder instance for method chaining
	 */
	public ValidatorBuilder<T> _string(ToCharSequence<T, String> f, String name,
			Function<CharSequenceConstraint<T, String>, CharSequenceConstraint<T, String>> c) {
		return this.constraint(f, name, c, CharSequenceConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToBoolean<T> f, String name,
			Function<BooleanConstraint<T>, BooleanConstraint<T>> c) {
		return this.constraint(f, name, c, BooleanConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(BooleanConstraintMeta<T> meta,
			Function<BooleanConstraint<T>, BooleanConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, BooleanConstraint::new);
	}

	public ValidatorBuilder<T> _boolean(ToBoolean<T> f, String name,
			Function<BooleanConstraint<T>, BooleanConstraint<T>> c) {
		return this.constraint(f, name, c, BooleanConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToCharacter<T> f, String name,
			Function<CharacterConstraint<T>, CharacterConstraint<T>> c) {
		return this.constraint(f, name, c, CharacterConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(CharacterConstraintMeta<T> meta,
			Function<CharacterConstraint<T>, CharacterConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, CharacterConstraint::new);
	}

	public ValidatorBuilder<T> _character(ToCharacter<T> f, String name,
			Function<CharacterConstraint<T>, CharacterConstraint<T>> c) {
		return this.constraint(f, name, c, CharacterConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToByte<T> f, String name, Function<ByteConstraint<T>, ByteConstraint<T>> c) {
		return this.constraint(f, name, c, ByteConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(ByteConstraintMeta<T> meta,
			Function<ByteConstraint<T>, ByteConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, ByteConstraint::new);
	}

	public ValidatorBuilder<T> _byte(ToByte<T> f, String name, Function<ByteConstraint<T>, ByteConstraint<T>> c) {
		return this.constraint(f, name, c, ByteConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToShort<T> f, String name,
			Function<ShortConstraint<T>, ShortConstraint<T>> c) {
		return this.constraint(f, name, c, ShortConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(ShortConstraintMeta<T> meta,
			Function<ShortConstraint<T>, ShortConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, ShortConstraint::new);
	}

	public ValidatorBuilder<T> _short(ToShort<T> f, String name, Function<ShortConstraint<T>, ShortConstraint<T>> c) {
		return this.constraint(f, name, c, ShortConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToInteger<T> f, String name,
			Function<IntegerConstraint<T>, IntegerConstraint<T>> c) {
		return this.constraint(f, name, c, IntegerConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(IntegerConstraintMeta<T> meta,
			Function<IntegerConstraint<T>, IntegerConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, IntegerConstraint::new);
	}

	public ValidatorBuilder<T> _integer(ToInteger<T> f, String name,
			Function<IntegerConstraint<T>, IntegerConstraint<T>> c) {
		return this.constraint(f, name, c, IntegerConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToLong<T> f, String name, Function<LongConstraint<T>, LongConstraint<T>> c) {
		return this.constraint(f, name, c, LongConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(LongConstraintMeta<T> meta,
			Function<LongConstraint<T>, LongConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, LongConstraint::new);
	}

	public ValidatorBuilder<T> _long(ToLong<T> f, String name, Function<LongConstraint<T>, LongConstraint<T>> c) {
		return this.constraint(f, name, c, LongConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToFloat<T> f, String name,
			Function<FloatConstraint<T>, FloatConstraint<T>> c) {
		return this.constraint(f, name, c, FloatConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(FloatConstraintMeta<T> meta,
			Function<FloatConstraint<T>, FloatConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, FloatConstraint::new);
	}

	public ValidatorBuilder<T> _float(ToFloat<T> f, String name, Function<FloatConstraint<T>, FloatConstraint<T>> c) {
		return this.constraint(f, name, c, FloatConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToDouble<T> f, String name,
			Function<DoubleConstraint<T>, DoubleConstraint<T>> c) {
		return this.constraint(f, name, c, DoubleConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(DoubleConstraintMeta<T> meta,
			Function<DoubleConstraint<T>, DoubleConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, DoubleConstraint::new);
	}

	public ValidatorBuilder<T> _double(ToDouble<T> f, String name,
			Function<DoubleConstraint<T>, DoubleConstraint<T>> c) {
		return this.constraint(f, name, c, DoubleConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToBigInteger<T> f, String name,
			Function<BigIntegerConstraint<T>, BigIntegerConstraint<T>> c) {
		return this.constraint(f, name, c, BigIntegerConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(BigIntegerConstraintMeta<T> meta,
			Function<BigIntegerConstraint<T>, BigIntegerConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, BigIntegerConstraint::new);
	}

	public ValidatorBuilder<T> _bigInteger(ToBigInteger<T> f, String name,
			Function<BigIntegerConstraint<T>, BigIntegerConstraint<T>> c) {
		return this.constraint(f, name, c, BigIntegerConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToBigDecimal<T> f, String name,
			Function<BigDecimalConstraint<T>, BigDecimalConstraint<T>> c) {
		return this.constraint(f, name, c, BigDecimalConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(BigDecimalConstraintMeta<T> meta,
			Function<BigDecimalConstraint<T>, BigDecimalConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, BigDecimalConstraint::new);
	}

	public ValidatorBuilder<T> _bigDecimal(ToBigDecimal<T> f, String name,
			Function<BigDecimalConstraint<T>, BigDecimalConstraint<T>> c) {
		return this.constraint(f, name, c, BigDecimalConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> constraint(ToLocalTimeConstraint<T> f, String name,
			Function<LocalTimeConstraint<T>, LocalTimeConstraint<T>> c) {
		return this.constraint(f, name, c, LocalTimeConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> constraint(LocalTimeConstraintMeta<T> meta,
			Function<LocalTimeConstraint<T>, LocalTimeConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, LocalTimeConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> _localTime(ToLocalTimeConstraint<T> f, String name,
			Function<LocalTimeConstraint<T>, LocalTimeConstraint<T>> c) {
		return this.constraint(f, name, c, LocalTimeConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> constraint(ToLocalDateConstraint<T> f, String name,
			Function<LocalDateConstraint<T>, LocalDateConstraint<T>> c) {
		return this.constraint(f, name, c, LocalDateConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> constraint(LocalDateConstraintMeta<T> meta,
			Function<LocalDateConstraint<T>, LocalDateConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, LocalDateConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> _localDate(ToLocalDateConstraint<T> f, String name,
			Function<LocalDateConstraint<T>, LocalDateConstraint<T>> c) {
		return this.constraint(f, name, c, LocalDateConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> constraint(ToLocalDateTimeConstraint<T> f, String name,
			Function<LocalDateTimeConstraint<T>, LocalDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, LocalDateTimeConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> constraint(LocalDateTimeConstraintMeta<T> meta,
			Function<LocalDateTimeConstraint<T>, LocalDateTimeConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, LocalDateTimeConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> _localDateTime(ToLocalDateTimeConstraint<T> f, String name,
			Function<LocalDateTimeConstraint<T>, LocalDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, LocalDateTimeConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> constraint(ToZonedDateTimeConstraint<T> f, String name,
			Function<ZonedDateTimeConstraint<T>, ZonedDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, ZonedDateTimeConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> constraint(ZonedDateTimeConstraintMeta<T> meta,
			Function<ZonedDateTimeConstraint<T>, ZonedDateTimeConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, ZonedDateTimeConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> _zonedDateTime(ToZonedDateTimeConstraint<T> f, String name,
			Function<ZonedDateTimeConstraint<T>, ZonedDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, ZonedDateTimeConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> constraint(ToOffsetDateTimeConstraint<T> f, String name,
			Function<OffsetDateTimeConstraint<T>, OffsetDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, OffsetDateTimeConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> constraint(OffsetDateTimeConstraintMeta<T> meta,
			Function<OffsetDateTimeConstraint<T>, OffsetDateTimeConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, OffsetDateTimeConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> _offsetDateTime(ToOffsetDateTimeConstraint<T> f, String name,
			Function<OffsetDateTimeConstraint<T>, OffsetDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, OffsetDateTimeConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> constraint(ToInstantConstraint<T> f, String name,
			Function<InstantConstraint<T>, InstantConstraint<T>> c) {
		return this.constraint(f, name, c, InstantConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> constraint(InstantConstraintMeta<T> meta,
			Function<InstantConstraint<T>, InstantConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, InstantConstraint::new);
	}

	/**
	 * @since 0.10.0
	 */
	public ValidatorBuilder<T> _instant(ToInstantConstraint<T> f, String name,
			Function<InstantConstraint<T>, InstantConstraint<T>> c) {
		return this.constraint(f, name, c, InstantConstraint::new);
	}

	/**
	 * @since 0.11.0
	 */
	public ValidatorBuilder<T> constraint(ToYearMonthConstraint<T> f, String name,
			Function<YearMonthConstraint<T>, YearMonthConstraint<T>> c) {
		return this.constraint(f, name, c, YearMonthConstraint::new);
	}

	/**
	 * @since 0.11.0
	 */
	public ValidatorBuilder<T> constraint(YearMonthConstraintMeta<T> meta,
			Function<YearMonthConstraint<T>, YearMonthConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, YearMonthConstraint::new);
	}

	/**
	 * @since 0.11.0
	 */
	public ValidatorBuilder<T> _yearMonth(ToYearMonthConstraint<T> f, String name,
			Function<YearMonthConstraint<T>, YearMonthConstraint<T>> c) {
		return this.constraint(f, name, c, YearMonthConstraint::new);
	}

	/**
	 * @since 0.11.0
	 */
	public ValidatorBuilder<T> constraint(ToYearConstraint<T> f, String name,
			Function<YearConstraint<T>, YearConstraint<T>> c) {
		return this.constraint(f, name, c, YearConstraint::new);
	}

	/**
	 * @since 0.11.0
	 */
	public ValidatorBuilder<T> constraint(YearConstraintMeta<T> meta,
			Function<YearConstraint<T>, YearConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, YearConstraint::new);
	}

	/**
	 * @since 0.11.0
	 */
	public ValidatorBuilder<T> _year(ToYearConstraint<T> f, String name,
			Function<YearConstraint<T>, YearConstraint<T>> c) {
		return this.constraint(f, name, c, YearConstraint::new);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> constraint(ToCollection<T, L, E> f, String name,
			Function<CollectionConstraint<T, L, E>, CollectionConstraint<T, L, E>> c) {
		return this.constraint(f, name, c, CollectionConstraint::new);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> _collection(ToCollection<T, L, E> f, String name,
			Function<CollectionConstraint<T, L, E>, CollectionConstraint<T, L, E>> c) {
		return this.constraint(f, name, c, CollectionConstraint::new);
	}

	public <K, V> ValidatorBuilder<T> constraint(ToMap<T, K, V> f, String name,
			Function<MapConstraint<T, K, V>, MapConstraint<T, K, V>> c) {
		return this.constraint(f, name, c, MapConstraint::new);
	}

	public <K, V> ValidatorBuilder<T> _map(ToMap<T, K, V> f, String name,
			Function<MapConstraint<T, K, V>, MapConstraint<T, K, V>> c) {
		return this.constraint(f, name, c, MapConstraint::new);
	}

	public <E> ValidatorBuilder<T> constraint(ToObjectArray<T, E> f, String name,
			Function<ObjectArrayConstraint<T, E>, ObjectArrayConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectArrayConstraint::new);
	}

	public <E> ValidatorBuilder<T> _objectArray(ToObjectArray<T, E> f, String name,
			Function<ObjectArrayConstraint<T, E>, ObjectArrayConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToBooleanArray<T> f, String name,
			Function<BooleanArrayConstraint<T>, BooleanArrayConstraint<T>> c) {
		return this.constraint(f, name, c, BooleanArrayConstraint::new);
	}

	public ValidatorBuilder<T> _booleanArray(ToBooleanArray<T> f, String name,
			Function<BooleanArrayConstraint<T>, BooleanArrayConstraint<T>> c) {
		return this.constraint(f, name, c, BooleanArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToCharArray<T> f, String name,
			Function<CharArrayConstraint<T>, CharArrayConstraint<T>> c) {
		return this.constraint(f, name, c, CharArrayConstraint::new);
	}

	public ValidatorBuilder<T> _charArray(ToCharArray<T> f, String name,
			Function<CharArrayConstraint<T>, CharArrayConstraint<T>> c) {
		return this.constraint(f, name, c, CharArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToByteArray<T> f, String name,
			Function<ByteArrayConstraint<T>, ByteArrayConstraint<T>> c) {
		return this.constraint(f, name, c, ByteArrayConstraint::new);
	}

	public ValidatorBuilder<T> _byteArray(ToByteArray<T> f, String name,
			Function<ByteArrayConstraint<T>, ByteArrayConstraint<T>> c) {
		return this.constraint(f, name, c, ByteArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToShortArray<T> f, String name,
			Function<ShortArrayConstraint<T>, ShortArrayConstraint<T>> c) {
		return this.constraint(f, name, c, ShortArrayConstraint::new);
	}

	public ValidatorBuilder<T> _shortArray(ToShortArray<T> f, String name,
			Function<ShortArrayConstraint<T>, ShortArrayConstraint<T>> c) {
		return this.constraint(f, name, c, ShortArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToIntArray<T> f, String name,
			Function<IntArrayConstraint<T>, IntArrayConstraint<T>> c) {
		return this.constraint(f, name, c, IntArrayConstraint::new);
	}

	public ValidatorBuilder<T> _intArray(ToIntArray<T> f, String name,
			Function<IntArrayConstraint<T>, IntArrayConstraint<T>> c) {
		return this.constraint(f, name, c, IntArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToLongArray<T> f, String name,
			Function<LongArrayConstraint<T>, LongArrayConstraint<T>> c) {
		return this.constraint(f, name, c, LongArrayConstraint::new);
	}

	public ValidatorBuilder<T> _longArray(ToLongArray<T> f, String name,
			Function<LongArrayConstraint<T>, LongArrayConstraint<T>> c) {
		return this.constraint(f, name, c, LongArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToFloatArray<T> f, String name,
			Function<FloatArrayConstraint<T>, FloatArrayConstraint<T>> c) {
		return this.constraint(f, name, c, FloatArrayConstraint::new);
	}

	public ValidatorBuilder<T> _floatArray(ToFloatArray<T> f, String name,
			Function<FloatArrayConstraint<T>, FloatArrayConstraint<T>> c) {
		return this.constraint(f, name, c, FloatArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToDoubleArray<T> f, String name,
			Function<DoubleArrayConstraint<T>, DoubleArrayConstraint<T>> c) {
		return this.constraint(f, name, c, DoubleArrayConstraint::new);
	}

	public ValidatorBuilder<T> _doubleArray(ToDoubleArray<T> f, String name,
			Function<DoubleArrayConstraint<T>, DoubleArrayConstraint<T>> c) {
		return this.constraint(f, name, c, DoubleArrayConstraint::new);
	}

	/**
	 * Adds a conditional validator that is only applied when the specified condition is
	 * met.
	 * <p>
	 * This method allows you to apply validation rules conditionally based on the state
	 * of the object being validated. The validation will only be performed if the
	 * condition predicate returns true for the target object.
	 * @param <R> the return type of the validator
	 * @param condition the condition that determines whether validation should be applied
	 * @param applicative the validator to apply when the condition is met
	 * @return this builder instance for method chaining
	 * @since 0.11.0
	 */
	public <R> ValidatorBuilder<T> constraintOnCondition(ConstraintCondition<T> condition,
			ValueValidator<T, R> applicative) {
		this.conditionalValidators.add(new Pair<>(condition, Validatable.from(applicative)));
		return this;
	}

	/**
	 * Adds a conditional validator that is only applied when the specified condition is
	 * met.
	 * <p>
	 * This method allows you to apply a complete validator conditionally based on the
	 * state of the object being validated. The validation will only be performed if the
	 * condition predicate returns true for the target object.
	 * <p>
	 * Example: <pre>{@code
	 * // Only validate credit card if payment method is "card"
	 * ValidatorBuilder<Payment> builder = ValidatorBuilder.<Payment>of()
	 *     .constraint(Payment::getMethod, "method", c -> c.notBlank())
	 *     .constraintOnCondition(
	 *         payment -> "card".equals(payment.getMethod()),
	 *         ValidatorBuilder.<Payment>of()
	 *             .constraint(Payment::getCardNumber, "cardNumber", c -> c.notBlank().pattern("[0-9]{16}"))
	 *             .constraint(Payment::getCardExpiry, "cardExpiry", c -> c.notBlank().pattern("[0-9]{2}/[0-9]{2}"))
	 *             .build()
	 *     );
	 * }</pre>
	 * @param condition the condition that determines whether validation should be applied
	 * @param validator the validator to apply when the condition is met
	 * @return this builder instance for method chaining
	 */
	public ValidatorBuilder<T> constraintOnCondition(ConstraintCondition<T> condition, Validator<T> validator) {
		this.conditionalValidators.add(new Pair<>(condition, validator));
		return this;
	}

	/**
	 * Adds a conditional validator using a builder converter that is only applied when
	 * the specified condition is met.
	 * <p>
	 * This method allows you to define conditional validation using a builder converter
	 * function. The validation will only be performed if the condition predicate returns
	 * true for the target object.
	 * <p>
	 * Example: <pre>{@code
	 * ValidatorBuilder<Order> builder = ValidatorBuilder.<Order>of()
	 *     ._integer(Order::getAmount, "amount", c -> c.greaterThan(0))
	 *     .constraintOnCondition(
	 *         order -> order.getAmount() > 10000,
	 *         b -> b._string(Order::getApprovalCode, "approvalCode", c -> c.notBlank())
	 *     );
	 * }</pre>
	 * @param condition the condition that determines whether validation should be applied
	 * @param converter a function that accepts and returns a ValidatorBuilder to define
	 * additional constraints
	 * @return this builder instance for method chaining
	 */
	public ValidatorBuilder<T> constraintOnCondition(ConstraintCondition<T> condition,
			ValidatorBuilderConverter<T> converter) {
		ValidatorBuilder<T> builder = converter.apply(new ValidatorBuilder<>());
		Validator<T> validator = builder.build();
		return this.constraintOnCondition(condition, validator);
	}

	/**
	 * Adds a group-based validator that is applied only when the given constraint group
	 * is active.
	 * <p>
	 * This method provides a way to organize validation rules into logical groups that
	 * can be selectively applied during validation. For example, you might have different
	 * validation rules for creating vs. updating an entity.
	 * <p>
	 * The validation will only be performed if the constraint group is active during
	 * validation.
	 * @param <R> the return type of the validator
	 * @param group the constraint group that determines whether validation should be
	 * applied
	 * @param applicative the validator to apply when the group is active
	 * @return this builder instance for method chaining
	 */
	public <R> ValidatorBuilder<T> constraintOnGroup(ConstraintGroup group, ValueValidator<T, R> applicative) {
		return this.constraintOnCondition(group.toCondition(), applicative);
	}

	/**
	 * Adds a group-based validator that is applied only when the given constraint group
	 * is active.
	 * <p>
	 * This method provides a way to organize validation rules into logical groups that
	 * can be selectively applied during validation. For example, you might have different
	 * validation rules for creating vs. updating an entity.
	 * <p>
	 * Example: <pre>{@code
	 * // Define validation groups
	 * ConstraintGroup CREATE = new ConstraintGroup("CREATE");
	 * ConstraintGroup UPDATE = new ConstraintGroup("UPDATE");
	 * 
	 * // Create validator with group-specific constraints
	 * Validator<User> validator = ValidatorBuilder.<User>of()
	 *     // Common validation for all scenarios
	 *     .constraint(User::getUsername, "username", c -> c.notBlank())
	 *     
	 *     // Password validation only for user creation
	 *     .constraintOnGroup(CREATE, 
	 *         ValidatorBuilder.<User>of()
	 *             .constraint(User::getPassword, "password", c -> c.notBlank().greaterThanOrEqual(8))
	 *             .build())
	 *             
	 *     // ID validation only for user updates
	 *     .constraintOnGroup(UPDATE,
	 *         ValidatorBuilder.<User>of()
	 *             .constraint(User::getId, "id", c -> c.greaterThan(0))
	 *             .build())
	 *     .build();
	 *             
	 * // Validate for creation
	 * ConstraintViolations violations = validator.validate(user, CREATE);
	 * }</pre>
	 * @param group the constraint group that determines whether validation should be
	 * applied
	 * @param validator the validator to apply when the group is active
	 * @return this builder instance for method chaining
	 */
	public ValidatorBuilder<T> constraintOnGroup(ConstraintGroup group, Validator<T> validator) {
		return this.constraintOnCondition(group.toCondition(), validator);
	}

	/**
	 * Adds a group-based validator using a builder converter that is applied only when
	 * the given constraint group is active.
	 * <p>
	 * This method provides a way to organize validation rules into logical groups using a
	 * builder converter function. The validation will only be performed if the constraint
	 * group is active during validation.
	 * <p>
	 * Example: <pre>{@code
	 * // Define validation groups
	 * ConstraintGroup ADMIN = new ConstraintGroup("ADMIN");
	 * ConstraintGroup USER = new ConstraintGroup("USER");
	 * 
	 * // Create validator with group-specific constraints
	 * Validator<Document> validator = ValidatorBuilder.<Document>of()
	 *     // Common validation for all scenarios
	 *     .constraint(Document::getTitle, "title", c -> c.notBlank())
	 *     
	 *     // Admin-only validation using converter function
	 *     .constraintOnGroup(ADMIN, builder -> 
	 *         builder.constraint(Document::getSecurityLevel, "securityLevel", c -> c.notBlank())
	 *                .constraint(Document::isClassified, "classified", c -> c.isTrue())
	 *     )
	 *     .build();
	 * }</pre>
	 * @param group the constraint group that determines whether validation should be
	 * applied
	 * @param converter a function that accepts and returns a ValidatorBuilder to define
	 * additional constraints
	 * @return this builder instance for method chaining
	 */
	public ValidatorBuilder<T> constraintOnGroup(ConstraintGroup group, ValidatorBuilderConverter<T> converter) {
		return this.constraintOnCondition(group.toCondition(), converter);
	}

	public <E> ValidatorBuilder<T> constraintOnObject(Function<T, E> f, String name,
			Function<ObjectConstraint<T, E>, ObjectConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public <E> ValidatorBuilder<T> _object(Function<T, E> f, String name,
			Function<ObjectConstraint<T, E>, ObjectConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectConstraint::new);
	}

	public <E> ValidatorBuilder<T> constraint(ObjectConstraintMeta<T, E> meta,
			Function<ObjectConstraint<T, E>, ObjectConstraint<T, E>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, ObjectConstraint::new);
	}

	public ValidatorBuilder<T> constraintOnTarget(Predicate<T> predicate, String name,
			ViolatedArguments<T> violatedArguments, ViolationMessage violationMessage) {
		Deque<ConstraintPredicate<T>> predicates = new LinkedList<>();
		predicates.add(ConstraintPredicate.of(predicate, violationMessage, violatedArguments, NullAs.INVALID));
		this.predicatesList.add(new ConstraintPredicates<>(Function.identity(), name, predicates));
		return this;
	}

	public ValidatorBuilder<T> constraintOnTarget(Predicate<T> predicate, String name,
			ViolationMessage violationMessage) {
		return this.constraintOnTarget(predicate, name, violatedValue -> CustomConstraint.EMPTY_ARRAY,
				violationMessage);
	}

	public ValidatorBuilder<T> constraintOnTarget(Predicate<T> predicate, String name, String messageKey,
			String defaultMessage) {
		return this.constraintOnTarget(predicate, name, ViolationMessage.of(messageKey, defaultMessage));
	}

	public ValidatorBuilder<T> constraintOnTarget(CustomConstraint<T> customConstraint, String name) {
		return this.constraintOnTarget(customConstraint, name, customConstraint, customConstraint);
	}

	/**
	 * @since 0.7.0
	 */
	public ValidatorBuilder<T> constraintOnTarget(String name,
			Function<ObjectConstraint<T, T>, ObjectConstraint<T, T>> c) {
		return this.constraint(Function.identity(), name, c, ObjectConstraint::new);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> forEach(ToCollection<T, L, E> toCollection, String name,
			Validator<E> validator) {
		return this.forEach(toCollection, name, validator, NullAs.INVALID);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> forEach(ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEach(toCollection, name, converter, NullAs.INVALID);
	}

	public <K, V> ValidatorBuilder<T> forEach(ToMap<T, K, V> toMap, String name, Validator<V> validator) {
		return this.forEach(this.toMapToCollection(toMap), name, validator);
	}

	public <K, V> ValidatorBuilder<T> forEach(ToMap<T, K, V> toMap, String name,
			ValidatorBuilderConverter<V> converter) {
		return this.forEach(this.toMapToCollection(toMap), name, converter);
	}

	public <E> ValidatorBuilder<T> forEach(ToObjectArray<T, E> toObjectArray, String name, Validator<E> validator) {
		return this.forEach(this.toObjectArrayToCollection(toObjectArray), name, validator);
	}

	public <E> ValidatorBuilder<T> forEach(ToObjectArray<T, E> toObjectArray, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEach(this.toObjectArrayToCollection(toObjectArray), name, converter);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> forEachIfPresent(ToCollection<T, L, E> toCollection,
			String name, Validator<E> validator) {
		return this.forEach(toCollection, name, validator, NullAs.VALID);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> forEachIfPresent(ToCollection<T, L, E> toCollection,
			String name, ValidatorBuilderConverter<E> converter) {
		return this.forEach(toCollection, name, converter, NullAs.VALID);
	}

	public <K, V> ValidatorBuilder<T> forEachIfPresent(ToMap<T, K, V> toMap, String name, Validator<V> validator) {
		return this.forEachIfPresent(this.toMapToCollection(toMap), name, validator);
	}

	public <K, V> ValidatorBuilder<T> forEachIfPresent(ToMap<T, K, V> toMap, String name,
			ValidatorBuilderConverter<V> converter) {
		return this.forEachIfPresent(this.toMapToCollection(toMap), name, converter);
	}

	public <E> ValidatorBuilder<T> forEachIfPresent(ToObjectArray<T, E> toObjectArray, String name,
			Validator<E> validator) {
		return this.forEachIfPresent(this.toObjectArrayToCollection(toObjectArray), name, validator);
	}

	public <E> ValidatorBuilder<T> forEachIfPresent(ToObjectArray<T, E> toObjectArray, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEachIfPresent(this.toObjectArrayToCollection(toObjectArray), name, converter);
	}

	/**
	 * Sets a custom message formatter for the validator.
	 * <p>
	 * Message formatters are responsible for creating error messages when validation
	 * fails. By default, {@link SimpleMessageFormatter} is used, but you can provide your
	 * own implementation for custom error message formatting.
	 * <p>
	 * Example: <pre>{@code
	 * // Create a validator with a custom message formatter
	 * Validator<User> validator = ValidatorBuilder.<User>of()
	 *     .constraint(User::getName, "name", c -> c.notBlank())
	 *     .messageFormatter(new CustomMessageFormatter())
	 *     .build();
	 * }</pre>
	 * @param messageFormatter the custom message formatter to use
	 * @return this builder instance for method chaining
	 */
	public ValidatorBuilder<T> messageFormatter(MessageFormatter messageFormatter) {
		this.messageFormatter = messageFormatter;
		return this;
	}

	/**
	 * Set whether to enable fail fast mode. If enabled, Validator returns from the
	 * current validation as soon as the first constraint violation occurs.
	 * @param failFast whether to enable fail fast mode
	 * @since 0.8.0
	 */
	public ValidatorBuilder<T> failFast(boolean failFast) {
		this.failFast = failFast;
		return this;
	}

	/**
	 * Sets the {@link ConflictStrategy} that defines the behavior when a constraint name
	 * conflicts when adding a constraint. By default, {@link ConflictStrategy#NOOP} is
	 * used.
	 * @param conflictStrategy Conflict Strategy
	 * @return validator builder (self)
	 * @since 0.13.0
	 */
	public ValidatorBuilder<T> conflictStrategy(ConflictStrategy conflictStrategy) {
		this.conflictStrategy = conflictStrategy;
		return this;
	}

	/**
	 * Adds a nested validator for a property that requires complex validation.
	 * <p>
	 * This method allows validation of complex object graphs by applying a separate
	 * validator to a nested property of the target object. If the nested property is
	 * null, validation will fail with a "must not be null" violation unless nestIfPresent
	 * is used instead.
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
	 * @param nested a function to extract the nested property from type T
	 * @param name the property name used in error messages
	 * @param validator the validator to apply to the nested property
	 * @return this builder instance for method chaining
	 */
	public <N> ValidatorBuilder<T> nest(Function<T, N> nested, String name, Validator<N> validator) {
		return this.nest(nested, name, validator, NullAs.INVALID);
	}

	public <N> ValidatorBuilder<T> nest(ObjectConstraintMeta<T, N> meta, Validator<N> validator) {
		return this.nest(meta.toValue(), meta.name(), validator, NullAs.INVALID);
	}

	public <N> ValidatorBuilder<T> nest(Function<T, N> nested, String name, ValidatorBuilderConverter<N> converter) {
		return this.nest(nested, name, converter, NullAs.INVALID);
	}

	public <N> ValidatorBuilder<T> nest(ObjectConstraintMeta<T, N> meta, ValidatorBuilderConverter<N> converter) {
		return this.nest(meta.toValue(), meta.name(), converter, NullAs.INVALID);
	}

	/**
	 * Adds a nested validator for a property that requires complex validation, but only
	 * applies it if the property is non-null.
	 * <p>
	 * This method works like {@link #nest(Function, String, Validator)}, but does not
	 * generate a "must not be null" error if the nested property is null. Instead,
	 * validation is skipped for null properties.
	 * <p>
	 * Example: <pre>{@code
	 * // Address is optional, but if present, must be valid
	 * Validator<Person> personValidator = ValidatorBuilder.<Person>of()
	 *     .constraint(Person::getName, "name", c -> c.notBlank())
	 *     .nestIfPresent(Person::getAddress, "address", addressValidator)
	 *     .build();
	 * }</pre>
	 * @param <N> the type of the nested property
	 * @param nested a function to extract the nested property from type T
	 * @param name the property name used in error messages
	 * @param validator the validator to apply to the nested property if it is non-null
	 * @return this builder instance for method chaining
	 */
	public <N> ValidatorBuilder<T> nestIfPresent(Function<T, N> nested, String name, Validator<N> validator) {
		return this.nest(nested, name, validator, NullAs.VALID);
	}

	public <N> ValidatorBuilder<T> nestIfPresent(ObjectConstraintMeta<T, N> meta, Validator<N> validator) {
		return this.nest(meta.toValue(), meta.name(), validator, NullAs.VALID);
	}

	public <N> ValidatorBuilder<T> nestIfPresent(Function<T, N> nested, String name,
			ValidatorBuilderConverter<N> converter) {
		return this.nest(nested, name, converter, NullAs.VALID);
	}

	public <N> ValidatorBuilder<T> nestIfPresent(ObjectConstraintMeta<T, N> meta,
			ValidatorBuilderConverter<N> converter) {
		return this.nest(meta.toValue(), meta.name(), converter, NullAs.VALID);
	}

	protected final <V, C extends Constraint<T, V, C>> ValidatorBuilder<T> constraint(Function<T, V> f, String name,
			Function<C, C> c, Supplier<C> supplier) {
		C constraint = supplier.get();
		Deque<ConstraintPredicate<V>> predicates = c.apply(constraint).predicates();
		this.predicatesList.add(new ConstraintPredicates<>(f, name, predicates));
		return this;
	}

	protected <L extends Collection<E>, E> ValidatorBuilder<T> forEach(ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter, NullAs nullAs) {
		ValidatorBuilder<E> builder = converter.apply(new ValidatorBuilder<>());
		return this.forEach(toCollection, name, builder.build(), nullAs);
	}

	protected final <L extends Collection<E>, E> ValidatorBuilder<T> forEach(ToCollection<T, L, E> toCollection,
			String name, Validator<E> validator, NullAs nullAs) {
		if (!nullAs.skipNull()) {
			this.constraintOnObject(toCollection, name, Constraint::notNull);
		}
		this.collectionValidators.add(new CollectionValidator<>(toCollection, name, validator));
		return this;
	}

	protected final <N> ValidatorBuilder<T> nest(Function<T, N> nested, String name,
			ValidatorBuilderConverter<N> converter, NullAs nullAs) {
		if (!nullAs.skipNull()) {
			this.constraintOnObject(nested, name, Constraint::notNull);
		}
		ValidatorBuilder<N> builder = converter.apply(new ValidatorBuilder<>());
		builder.predicatesList.forEach(this.appendNestedPredicates(nested, name, false));
		builder.conditionalValidators.forEach(this.appendNestedConditionalValidator(nested, name));
		builder.collectionValidators.forEach(appendNestedCollectionValidator(nested, name));
		return this;
	}

	protected final <N> ValidatorBuilder<T> nest(Function<T, N> nested, String name, Validator<N> validator,
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
	private <N> Consumer<ConstraintPredicates<N, ?>> appendNestedPredicates(Function<T, N> nested, String name,
			boolean failFast) {
		return predicates -> {
			String nestedName = name + this.messageKeySeparator + predicates.name();
			ConstraintPredicates<T, ?> constraintPredicates = new NestedConstraintPredicates(
					this.toNestedValue(nested, predicates), nestedName, predicates.predicates(),
					this.toNestedFunction(nested, predicates), failFast);
			this.predicatesList.add(constraintPredicates);
		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <N> Function<T, ?> toNestedFunction(Function<T, N> nested, ConstraintPredicates<N, ?> predicates) {
		if (predicates instanceof NestedConstraintPredicates) {
			return target -> {
				N nestedValue = nested.apply(target);
				if (nestedValue == null) {
					return null;
				}
				return (N) ((NestedConstraintPredicates) predicates).nestedValue(nestedValue);
			};
		}
		return nested;
	}

	private <N> Consumer<Pair<ConstraintCondition<N>, Validatable<N>>> appendNestedConditionalValidator(
			Function<T, N> nested, String name) {
		return conditionalValidator -> {
			final ConstraintCondition<T> condition = new NestedConstraintCondition<>(nested,
					conditionalValidator.first());
			final Validatable<N> validator = conditionalValidator.second();
			final String nestedPrefix = toNestedPrefix(name, validator);
			final Validatable<T> v = new NestedValidator<>(nested, validator, nestedPrefix);
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
	private <N> Consumer<CollectionValidator<N, ?, ?>> appendNestedCollectionValidator(Function<T, N> nested,
			String name) {
		return collectionValidator -> {
			String nestedName = name + this.messageKeySeparator + collectionValidator.name();
			CollectionValidator<T, ?, ?> validator = new NestedCollectionValidator(
					toNestedCollection(nested, collectionValidator), nestedName, collectionValidator.validator(),
					nested);
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

	private <N> Function<T, Object> toNestedValue(Function<T, N> nested, ConstraintPredicates<N, ?> predicates) {
		return (T target) -> {
			N nestedValue = nested.apply(target);
			if (nestedValue == null) {
				return null;
			}
			return predicates.toValue().apply(nestedValue);
		};
	}

	private <N, C extends Collection<?>> Function<T, Object> toNestedCollection(Function<T, N> nested,
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

	public interface ToBigDecimal<T> extends Function<T, BigDecimal> {

	}

	public interface ToBigInteger<T> extends Function<T, BigInteger> {

	}

	public interface ToBoolean<T> extends Function<T, Boolean> {

	}

	public interface ToBooleanArray<T> extends Function<T, boolean[]> {

	}

	public interface ToByte<T> extends Function<T, Byte> {

	}

	public interface ToByteArray<T> extends Function<T, byte[]> {

	}

	public interface ToCharArray<T> extends Function<T, char[]> {

	}

	public interface ToCharSequence<T, E extends CharSequence> extends Function<T, E> {

	}

	public interface toEnum<T, E extends Enum> extends Function<T, E> {

	}

	public interface ToCharacter<T> extends Function<T, Character> {

	}

	public interface ToCollection<T, L extends Collection<E>, E> extends Function<T, L> {

	}

	public interface ToDouble<T> extends Function<T, Double> {

	}

	public interface ToDoubleArray<T> extends Function<T, double[]> {

	}

	public interface ToFloat<T> extends Function<T, Float> {

	}

	public interface ToFloatArray<T> extends Function<T, float[]> {

	}

	public interface ToIntArray<T> extends Function<T, int[]> {

	}

	public interface ToInteger<T> extends Function<T, Integer> {

	}

	public interface ToLong<T> extends Function<T, Long> {

	}

	public interface ToLongArray<T> extends Function<T, long[]> {

	}

	public interface ToMap<T, K, V> extends Function<T, Map<K, V>> {

	}

	public interface ToObjectArray<T, E> extends Function<T, E[]> {

	}

	public interface ToShort<T> extends Function<T, Short> {

	}

	public interface ToShortArray<T> extends Function<T, short[]> {

	}

	public interface ToLocalTimeConstraint<T> extends Function<T, LocalTime> {

	}

	public interface ToZonedDateTimeConstraint<T> extends Function<T, ZonedDateTime> {

	}

	public interface ToOffsetDateTimeConstraint<T> extends Function<T, OffsetDateTime> {

	}

	public interface ToLocalDateTimeConstraint<T> extends Function<T, LocalDateTime> {

	}

	public interface ToLocalDateConstraint<T> extends Function<T, LocalDate> {

	}

	public interface ToInstantConstraint<T> extends Function<T, Instant> {

	}

	public interface ToYearConstraint<T> extends Function<T, Year> {

	}

	public interface ToYearMonthConstraint<T> extends Function<T, YearMonth> {

	}

	public interface ValidatorBuilderConverter<T> extends Function<ValidatorBuilder<T>, ValidatorBuilder<T>> {

	}

	/**
	 * An internal class that manages predicate conflicts
	 *
	 * @param <T> target type
	 * @since 0.13.0
	 */
	static final class PredicatesList<T> {

		private final ConflictStrategy conflictStrategy;

		private final Map<String, List<ConstraintPredicates<T, ?>>> predicatesListMap = new LinkedHashMap<>();

		public PredicatesList(ConflictStrategy conflictStrategy) {
			this.conflictStrategy = conflictStrategy;
		}

		public PredicatesList(ConflictStrategy conflictStrategy, List<ConstraintPredicates<T, ?>> predicatesList) {
			this(conflictStrategy);
			predicatesList.forEach(this::add);
		}

		public void add(ConstraintPredicates<T, ?> predicates) {
			final List<ConstraintPredicates<T, ?>> predicatesList = this.predicatesListMap
				.computeIfAbsent(predicates.name(), s -> new ArrayList<>());
			if (!predicatesList.isEmpty()) {
				this.conflictStrategy.resolveConflict(predicatesList, predicates);
			}
			predicatesList.add(predicates);
		}

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
