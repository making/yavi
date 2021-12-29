/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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

import am.ik.yavi.constraint.*;
import am.ik.yavi.constraint.array.*;
import am.ik.yavi.constraint.time.LocalDateConstraint;
import am.ik.yavi.constraint.time.LocalDateTimeConstraint;
import am.ik.yavi.constraint.time.ZonedDateTimeConstraint;
import am.ik.yavi.core.*;
import am.ik.yavi.fn.Pair;
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;
import am.ik.yavi.meta.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ValidatorBuilder<T> implements Cloneable {
	private static final String DEFAULT_SEPARATOR = ".";

	final List<CollectionValidator<T, ?, ?>> collectionValidators = new ArrayList<>();

	final List<Pair<ConstraintCondition<T>, Validatable<T>>> conditionalValidators = new ArrayList<>();

	final String messageKeySeparator;

	final List<ConstraintPredicates<T, ?>> predicatesList = new ArrayList<>();

	MessageFormatter messageFormatter;

	boolean failFast = false;

	public ValidatorBuilder() {
		this(DEFAULT_SEPARATOR);
	}

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

	@SuppressWarnings("unchecked")
	public <S extends T> ValidatorBuilder<S> cast(Class<S> clazz) {
		return (ValidatorBuilder<S>) this;
	}

	@SuppressWarnings("unchecked")
	public <S extends T> ValidatorBuilder<S> cast() {
		return (ValidatorBuilder<S>) this;
	}

	/**
	 * @deprecated please use the copy constructor
	 * {@link #ValidatorBuilder(ValidatorBuilder)}
	 * @return the cloned builder
	 */
	@Deprecated
	public ValidatorBuilder<T> clone() {
		return new ValidatorBuilder<>(this);
	}

	/**
	 * Factory method for a {@code ValidatorBuilder} to build {@code Validator} instance.
	 * @param <X> the type of the instance to validate
	 * @return builder instance
	 */
	public static <X> ValidatorBuilder<X> of(Class<X> clazz) {
		return new ValidatorBuilder<>();
	}

	/**
	 * Factory method for a {@code ValidatorBuilder} to build {@code Validator} instance.
	 * @param <X> the type of the instance to validate
	 * @return builder instance
	 */
	public static <X> ValidatorBuilder<X> of() {
		return new ValidatorBuilder<>();
	}

	public Validator<T> build() {
		return new Validator<>(messageKeySeparator, this.predicatesList,
				this.collectionValidators, this.conditionalValidators,
				this.messageFormatter == null ? new SimpleMessageFormatter()
						: this.messageFormatter,
				this.failFast);
	}

	/**
	 * Create a <code>BiValidator</code> instance using the given constraints.
	 *
	 * In case of Spring Framework's Validator integration
	 *
	 * <pre>
	 * BiValidator&lt;CartItem, Errors&gt; validator = ValidatorBuilder
	 *   .&lt;CartItem&gt;of()
	 *   .constraint(...)
	 *   .build(Errors::rejectValue);
	 * </pre>
	 *
	 * @param errorHandler handler that handle if the validation fails
	 * @param <E> the type of the error object
	 * @return <code>BiValidator</code> instance
	 */
	public <E> BiValidator<T, E> build(BiValidator.ErrorHandler<E> errorHandler) {
		final Validator<T> validator = this.build();
		return new BiValidator<>(validator, errorHandler);
	}

	// region LocalDateConstraint
	public ValidatorBuilder<T> constraint(ToLocalDateConstraint<T> f, String name,
			Function<LocalDateConstraint<T>, LocalDateConstraint<T>> c) {
		return this.constraint(f, name, c, LocalDateConstraint::new);
	}

	public ValidatorBuilder<T> constraint(LocalDateConstraintMeta<T> meta,
			Function<LocalDateConstraint<T>, LocalDateConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, LocalDateConstraint::new);
	}

	public ValidatorBuilder<T> _localDate(ToLocalDateConstraint<T> f, String name,
			Function<LocalDateConstraint<T>, LocalDateConstraint<T>> c) {
		return this.constraint(f, name, c, LocalDateConstraint::new);
	}
	// endregion

	// region LocalDateTimeConstraint
	public ValidatorBuilder<T> constraint(ToLocalDateTimeConstraint<T> f, String name,
			Function<LocalDateTimeConstraint<T>, LocalDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, LocalDateTimeConstraint::new);
	}

	public ValidatorBuilder<T> constraint(LocalDateTimeConstraintMeta<T> meta,
			Function<LocalDateTimeConstraint<T>, LocalDateTimeConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c,
				LocalDateTimeConstraint::new);
	}

	public ValidatorBuilder<T> _localDateTime(ToLocalDateTimeConstraint<T> f, String name,
			Function<LocalDateTimeConstraint<T>, LocalDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, LocalDateTimeConstraint::new);
	}
	// endregion

	// region ZonedDateTimeConstraint
	public ValidatorBuilder<T> constraint(ToZonedDateTimeConstraint<T> f, String name,
			Function<ZonedDateTimeConstraint<T>, ZonedDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, ZonedDateTimeConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ZonedDateTimeConstraintMeta<T> meta,
			Function<ZonedDateTimeConstraint<T>, ZonedDateTimeConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c,
				ZonedDateTimeConstraint::new);
	}

	public ValidatorBuilder<T> _zonedDateTime(ToZonedDateTimeConstraint<T> f, String name,
			Function<ZonedDateTimeConstraint<T>, ZonedDateTimeConstraint<T>> c) {
		return this.constraint(f, name, c, ZonedDateTimeConstraint::new);
	}
	// endregion

	public ValidatorBuilder<T> constraint(ToCharSequence<T, String> f, String name,
			Function<CharSequenceConstraint<T, String>, CharSequenceConstraint<T, String>> c) {
		return this.constraint(f, name, c, CharSequenceConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(StringConstraintMeta<T> meta,
			Function<CharSequenceConstraint<T, String>, CharSequenceConstraint<T, String>> c) {
		return this.constraint(meta.toValue(), meta.name(), c,
				CharSequenceConstraint::new);
	}

	public <E extends CharSequence> ValidatorBuilder<T> _charSequence(
			ToCharSequence<T, E> f, String name,
			Function<CharSequenceConstraint<T, E>, CharSequenceConstraint<T, E>> c) {
		return this.constraint(f, name, c, CharSequenceConstraint::new);
	}

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

	public ValidatorBuilder<T> constraint(ToByte<T> f, String name,
			Function<ByteConstraint<T>, ByteConstraint<T>> c) {
		return this.constraint(f, name, c, ByteConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(ByteConstraintMeta<T> meta,
			Function<ByteConstraint<T>, ByteConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, ByteConstraint::new);
	}

	public ValidatorBuilder<T> _byte(ToByte<T> f, String name,
			Function<ByteConstraint<T>, ByteConstraint<T>> c) {
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

	public ValidatorBuilder<T> _short(ToShort<T> f, String name,
			Function<ShortConstraint<T>, ShortConstraint<T>> c) {
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

	public ValidatorBuilder<T> constraint(ToLong<T> f, String name,
			Function<LongConstraint<T>, LongConstraint<T>> c) {
		return this.constraint(f, name, c, LongConstraint::new);
	}

	/**
	 * @since 0.4.0
	 */
	public ValidatorBuilder<T> constraint(LongConstraintMeta<T> meta,
			Function<LongConstraint<T>, LongConstraint<T>> c) {
		return this.constraint(meta.toValue(), meta.name(), c, LongConstraint::new);
	}

	public ValidatorBuilder<T> _long(ToLong<T> f, String name,
			Function<LongConstraint<T>, LongConstraint<T>> c) {
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

	public ValidatorBuilder<T> _float(ToFloat<T> f, String name,
			Function<FloatConstraint<T>, FloatConstraint<T>> c) {
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

	public <L extends Collection<E>, E> ValidatorBuilder<T> constraint(
			ToCollection<T, L, E> f, String name,
			Function<CollectionConstraint<T, L, E>, CollectionConstraint<T, L, E>> c) {
		return this.constraint(f, name, c, CollectionConstraint::new);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> _collection(
			ToCollection<T, L, E> f, String name,
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

	public ValidatorBuilder<T> constraintOnCondition(ConstraintCondition<T> condition,
			Validator<T> validator) {
		this.conditionalValidators.add(new Pair<>(condition, validator));
		return this;
	}

	public ValidatorBuilder<T> constraintOnCondition(ConstraintCondition<T> condition,
			ValidatorBuilderConverter<T> converter) {
		ValidatorBuilder<T> builder = converter.apply(new ValidatorBuilder<>());
		Validator<T> validator = builder.build();
		return this.constraintOnCondition(condition, validator);
	}

	public ValidatorBuilder<T> constraintOnGroup(ConstraintGroup group,
			Validator<T> validator) {
		return this.constraintOnCondition(group.toCondition(), validator);
	}

	public ValidatorBuilder<T> constraintOnGroup(ConstraintGroup group,
			ValidatorBuilderConverter<T> converter) {
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
		predicates.add(ConstraintPredicate.of(predicate, violationMessage,
				violatedArguments, NullAs.INVALID));
		this.predicatesList
				.add(new ConstraintPredicates<>(Function.identity(), name, predicates));
		return this;
	}

	public ValidatorBuilder<T> constraintOnTarget(Predicate<T> predicate, String name,
			ViolationMessage violationMessage) {
		return this.constraintOnTarget(predicate, name,
				violatedValue -> CustomConstraint.EMPTY_ARRAY, violationMessage);
	}

	public ValidatorBuilder<T> constraintOnTarget(Predicate<T> predicate, String name,
			String messageKey, String defaultMessage) {
		return this.constraintOnTarget(predicate, name,
				ViolationMessage.of(messageKey, defaultMessage));
	}

	public ValidatorBuilder<T> constraintOnTarget(CustomConstraint<T> customConstraint,
			String name) {
		return this.constraintOnTarget(customConstraint, name, customConstraint,
				customConstraint);
	}

	/**
	 * @since 0.7.0
	 */
	public ValidatorBuilder<T> constraintOnTarget(String name,
			Function<ObjectConstraint<T, T>, ObjectConstraint<T, T>> c) {
		return this.constraint(Function.identity(), name, c, ObjectConstraint::new);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> forEach(
			ToCollection<T, L, E> toCollection, String name, Validator<E> validator) {
		return this.forEach(toCollection, name, validator, NullAs.INVALID);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> forEach(
			ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEach(toCollection, name, converter, NullAs.INVALID);
	}

	public <K, V> ValidatorBuilder<T> forEach(ToMap<T, K, V> toMap, String name,
			Validator<V> validator) {
		return this.forEach(this.toMapToCollection(toMap), name, validator);
	}

	public <K, V> ValidatorBuilder<T> forEach(ToMap<T, K, V> toMap, String name,
			ValidatorBuilderConverter<V> converter) {
		return this.forEach(this.toMapToCollection(toMap), name, converter);
	}

	public <E> ValidatorBuilder<T> forEach(ToObjectArray<T, E> toObjectArray, String name,
			Validator<E> validator) {
		return this.forEach(this.toObjectArrayToCollection(toObjectArray), name,
				validator);
	}

	public <E> ValidatorBuilder<T> forEach(ToObjectArray<T, E> toObjectArray, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEach(this.toObjectArrayToCollection(toObjectArray), name,
				converter);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> forEachIfPresent(
			ToCollection<T, L, E> toCollection, String name, Validator<E> validator) {
		return this.forEach(toCollection, name, validator, NullAs.VALID);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> forEachIfPresent(
			ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEach(toCollection, name, converter, NullAs.VALID);
	}

	public <K, V> ValidatorBuilder<T> forEachIfPresent(ToMap<T, K, V> toMap, String name,
			Validator<V> validator) {
		return this.forEachIfPresent(this.toMapToCollection(toMap), name, validator);
	}

	public <K, V> ValidatorBuilder<T> forEachIfPresent(ToMap<T, K, V> toMap, String name,
			ValidatorBuilderConverter<V> converter) {
		return this.forEachIfPresent(this.toMapToCollection(toMap), name, converter);
	}

	public <E> ValidatorBuilder<T> forEachIfPresent(ToObjectArray<T, E> toObjectArray,
			String name, Validator<E> validator) {
		return this.forEachIfPresent(this.toObjectArrayToCollection(toObjectArray), name,
				validator);
	}

	public <E> ValidatorBuilder<T> forEachIfPresent(ToObjectArray<T, E> toObjectArray,
			String name, ValidatorBuilderConverter<E> converter) {
		return this.forEachIfPresent(this.toObjectArrayToCollection(toObjectArray), name,
				converter);
	}

	public ValidatorBuilder<T> messageFormatter(MessageFormatter messageFormatter) {
		this.messageFormatter = messageFormatter;
		return this;
	}

	/**
	 * Set whether to enable fail fast mode. If enabled, Validator returns from the
	 * current validation as soon as the first constraint violation occurs.
	 *
	 * @param failFast whether to enable fail fast mode
	 * @since 0.8.0
	 */
	public ValidatorBuilder<T> failFast(boolean failFast) {
		this.failFast = failFast;
		return this;
	}

	public <N> ValidatorBuilder<T> nest(Function<T, N> nested, String name,
			Validator<N> validator) {
		return this.nest(nested, name, validator, NullAs.INVALID);
	}

	public <N> ValidatorBuilder<T> nest(ObjectConstraintMeta<T, N> meta,
			Validator<N> validator) {
		return this.nest(meta.toValue(), meta.name(), validator, NullAs.INVALID);
	}

	public <N> ValidatorBuilder<T> nest(Function<T, N> nested, String name,
			ValidatorBuilderConverter<N> converter) {
		return this.nest(nested, name, converter, NullAs.INVALID);
	}

	public <N> ValidatorBuilder<T> nest(ObjectConstraintMeta<T, N> meta,
			ValidatorBuilderConverter<N> converter) {
		return this.nest(meta.toValue(), meta.name(), converter, NullAs.INVALID);
	}

	public <N> ValidatorBuilder<T> nestIfPresent(Function<T, N> nested, String name,
			Validator<N> validator) {
		return this.nest(nested, name, validator, NullAs.VALID);
	}

	public <N> ValidatorBuilder<T> nestIfPresent(ObjectConstraintMeta<T, N> meta,
			Validator<N> validator) {
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

	protected final <V, C extends Constraint<T, V, C>> ValidatorBuilder<T> constraint(
			Function<T, V> f, String name, Function<C, C> c, Supplier<C> supplier) {
		C constraint = supplier.get();
		Deque<ConstraintPredicate<V>> predicates = c.apply(constraint).predicates();
		this.predicatesList.add(new ConstraintPredicates<>(f, name, predicates));
		return this;
	}

	protected <L extends Collection<E>, E> ValidatorBuilder<T> forEach(
			ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter, NullAs nullAs) {
		ValidatorBuilder<E> builder = converter.apply(new ValidatorBuilder<>());
		return this.forEach(toCollection, name, builder.build(), nullAs);
	}

	protected final <L extends Collection<E>, E> ValidatorBuilder<T> forEach(
			ToCollection<T, L, E> toCollection, String name, Validator<E> validator,
			NullAs nullAs) {
		if (!nullAs.skipNull()) {
			this.constraintOnObject(toCollection, name, Constraint::notNull);
		}
		this.collectionValidators
				.add(new CollectionValidator<>(toCollection, name, validator));
		return this;
	}

	protected final <N> ValidatorBuilder<T> nest(Function<T, N> nested, String name,
			ValidatorBuilderConverter<N> converter, NullAs nullAs) {
		if (!nullAs.skipNull()) {
			this.constraintOnObject(nested, name, Constraint::notNull);
		}
		ValidatorBuilder<N> builder = converter.apply(new ValidatorBuilder<>());
		builder.predicatesList.forEach(this.appendNestedPredicates(nested, name));
		builder.conditionalValidators
				.forEach(this.appendNestedConditionalValidator(nested, name));
		builder.collectionValidators
				.forEach(appendNestedCollectionValidator(nested, name));
		return this;
	}

	protected final <N> ValidatorBuilder<T> nest(Function<T, N> nested, String name,
			Validator<N> validator, NullAs nullAs) {
		if (!nullAs.skipNull()) {
			this.constraintOnObject(nested, name, Constraint::notNull);
		}
		validator.forEachPredicates(this.appendNestedPredicates(nested, name));
		validator.forEachConditionalValidator(
				this.appendNestedConditionalValidator(nested, name));
		validator.forEachCollectionValidator(
				appendNestedCollectionValidator(nested, name));
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <N> Consumer<ConstraintPredicates<N, ?>> appendNestedPredicates(
			Function<T, N> nested, String name) {
		return predicates -> {
			String nestedName = name + this.messageKeySeparator + predicates.name();
			ConstraintPredicates<T, ?> constraintPredicates = new NestedConstraintPredicates(
					this.toNestedValue(nested, predicates), nestedName,
					predicates.predicates(), this.toNestedFunction(nested, predicates));
			this.predicatesList.add(constraintPredicates);
		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <N> Function<T, ?> toNestedFunction(Function<T, N> nested,
			ConstraintPredicates<N, ?> predicates) {
		if (predicates instanceof NestedConstraintPredicates) {
			return target -> {
				N nestedValue = nested.apply(target);
				if (nestedValue == null) {
					return null;
				}
				return (N) ((NestedConstraintPredicates) predicates)
						.nestedValue(nestedValue);
			};
		}
		return nested;
	}

	private <N> Consumer<Pair<ConstraintCondition<N>, Validatable<N>>> appendNestedConditionalValidator(
			Function<T, N> nested, String name) {
		return conditionalValidator -> {
			final ConstraintCondition<T> condition = new NestedConstraintCondition<>(
					nested, conditionalValidator.first());
			final Validatable<N> validator = conditionalValidator.second();
			final String nestedPrefix = toNestedPrefix(name, validator);
			final Validatable<T> v = new NestedValidator<>(nested, validator,
					nestedPrefix);
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
	private <N> Consumer<CollectionValidator<N, ?, ?>> appendNestedCollectionValidator(
			Function<T, N> nested, String name) {
		return collectionValidator -> {
			String nestedName = name + this.messageKeySeparator
					+ collectionValidator.name();
			CollectionValidator<T, ?, ?> validator = new NestedCollectionValidator(
					toNestedCollection(nested, collectionValidator), nestedName,
					collectionValidator.validator(), nested);
			this.collectionValidators.add(validator);
		};
	}

	private <K, V> ToCollection<T, Collection<V>, V> toMapToCollection(
			ToMap<T, K, V> toMap) {
		return t -> {
			Map<K, V> map = toMap.apply(t);
			if (map == null) {
				return null;
			}
			return map.values();
		};
	}

	private <N> Function<T, Object> toNestedValue(Function<T, N> nested,
			ConstraintPredicates<N, ?> predicates) {
		return (T target) -> {
			N nestedValue = nested.apply(target);
			if (nestedValue == null) {
				return null;
			}
			return predicates.toValue().apply(nestedValue);
		};
	}

	private <N, C extends Collection<?>> Function<T, Object> toNestedCollection(
			Function<T, N> nested, CollectionValidator<N, C, ?> validator) {
		return (T target) -> {
			N nestedCollection = nested.apply(target);
			if (nestedCollection == null) {
				return null;
			}
			return validator.toCollection().apply(nestedCollection);
		};
	}

	private <E> ToCollection<T, Collection<E>, E> toObjectArrayToCollection(
			ToObjectArray<T, E> toObjectArray) {
		return t -> {
			E[] array = toObjectArray.apply(t);
			if (array == null) {
				return null;
			}
			return Arrays.asList(array);
		};
	}

	public interface ToZonedDateTimeConstraint<T> extends Function<T, ZonedDateTime> {
	}

	public interface ToLocalDateTimeConstraint<T> extends Function<T, LocalDateTime> {
	}

	public interface ToLocalDateConstraint<T> extends Function<T, LocalDate> {
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

	public interface ValidatorBuilderConverter<T>
			extends Function<ValidatorBuilder<T>, ValidatorBuilder<T>> {
	}
}
