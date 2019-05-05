/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import am.ik.yavi.constraint.BigDecimalConstraint;
import am.ik.yavi.constraint.BigIntegerConstraint;
import am.ik.yavi.constraint.BooleanConstraint;
import am.ik.yavi.constraint.ByteConstraint;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.constraint.CharacterConstraint;
import am.ik.yavi.constraint.CollectionConstraint;
import am.ik.yavi.constraint.Constraint;
import am.ik.yavi.constraint.CustomConstraint;
import am.ik.yavi.constraint.DoubleConstraint;
import am.ik.yavi.constraint.FloatConstraint;
import am.ik.yavi.constraint.IntegerConstraint;
import am.ik.yavi.constraint.LongConstraint;
import am.ik.yavi.constraint.MapConstraint;
import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.constraint.ShortConstraint;
import am.ik.yavi.constraint.ViolationMessage;
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
import am.ik.yavi.core.ConstraintCondition;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.ConstraintPredicates;
import am.ik.yavi.core.NestedConstraintPredicates;
import am.ik.yavi.core.NullAs;
import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Pair;
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;

public class ValidatorBuilder<T> {
	private static final String DEFAULT_SEPARATOR = ".";
	private final String messageKeySeparator;
	private final List<ConstraintPredicates<T, ?>> predicatesList = new ArrayList<>();
	private final List<CollectionValidator<T, ?, ?>> collectionValidators = new ArrayList<>();
	private final List<Pair<ConstraintCondition<T>, Validator<T>>> conditionalValidators = new ArrayList<>();
	private MessageFormatter messageFormatter;

	public ValidatorBuilder() {
		this(DEFAULT_SEPARATOR);
	}

	public ValidatorBuilder(String messageKeySeparator) {
		this.messageKeySeparator = messageKeySeparator;
	}

	public ValidatorBuilder<T> messageFormatter(MessageFormatter messageFormatter) {
		this.messageFormatter = messageFormatter;
		return this;
	}

	public <E extends CharSequence> ValidatorBuilder<T> constraint(ToCharSequence<T, E> f,
			String name,
			Function<CharSequenceConstraint<T, E>, CharSequenceConstraint<T, E>> c) {
		return this.constraint(f, name, c, CharSequenceConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToBoolean<T> f, String name,
			Function<BooleanConstraint<T>, BooleanConstraint<T>> c) {
		return this.constraint(f, name, c, BooleanConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToCharacter<T> f, String name,
			Function<CharacterConstraint<T>, CharacterConstraint<T>> c) {
		return this.constraint(f, name, c, CharacterConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToByte<T> f, String name,
			Function<ByteConstraint<T>, ByteConstraint<T>> c) {
		return this.constraint(f, name, c, ByteConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToShort<T> f, String name,
			Function<ShortConstraint<T>, ShortConstraint<T>> c) {
		return this.constraint(f, name, c, ShortConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToInteger<T> f, String name,
			Function<IntegerConstraint<T>, IntegerConstraint<T>> c) {
		return this.constraint(f, name, c, IntegerConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToLong<T> f, String name,
			Function<LongConstraint<T>, LongConstraint<T>> c) {
		return this.constraint(f, name, c, LongConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToFloat<T> f, String name,
			Function<FloatConstraint<T>, FloatConstraint<T>> c) {
		return this.constraint(f, name, c, FloatConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToDouble<T> f, String name,
			Function<DoubleConstraint<T>, DoubleConstraint<T>> c) {
		return this.constraint(f, name, c, DoubleConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToBigInteger<T> f, String name,
			Function<BigIntegerConstraint<T>, BigIntegerConstraint<T>> c) {
		return this.constraint(f, name, c, BigIntegerConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToBigDecimal<T> f, String name,
			Function<BigDecimalConstraint<T>, BigDecimalConstraint<T>> c) {
		return this.constraint(f, name, c, BigDecimalConstraint::new);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> constraint(
			ToCollection<T, L, E> f, String name,
			Function<CollectionConstraint<T, L, E>, CollectionConstraint<T, L, E>> c) {
		return this.constraint(f, name, c, CollectionConstraint::new);
	}

	public <K, V> ValidatorBuilder<T> constraint(ToMap<T, K, V> f, String name,
			Function<MapConstraint<T, K, V>, MapConstraint<T, K, V>> c) {
		return this.constraint(f, name, c, MapConstraint::new);
	}

	public <E> ValidatorBuilder<T> constraint(ToObjectArray<T, E> f, String name,
			Function<ObjectArrayConstraint<T, E>, ObjectArrayConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToBooleanArray<T> f, String name,
			Function<BooleanArrayConstraint<T>, BooleanArrayConstraint<T>> c) {
		return this.constraint(f, name, c, BooleanArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToCharArray<T> f, String name,
			Function<CharArrayConstraint<T>, CharArrayConstraint<T>> c) {
		return this.constraint(f, name, c, CharArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToByteArray<T> f, String name,
			Function<ByteArrayConstraint<T>, ByteArrayConstraint<T>> c) {
		return this.constraint(f, name, c, ByteArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToShortArray<T> f, String name,
			Function<ShortArrayConstraint<T>, ShortArrayConstraint<T>> c) {
		return this.constraint(f, name, c, ShortArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToIntArray<T> f, String name,
			Function<IntArrayConstraint<T>, IntArrayConstraint<T>> c) {
		return this.constraint(f, name, c, IntArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToLongArray<T> f, String name,
			Function<LongArrayConstraint<T>, LongArrayConstraint<T>> c) {
		return this.constraint(f, name, c, LongArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToFloatArray<T> f, String name,
			Function<FloatArrayConstraint<T>, FloatArrayConstraint<T>> c) {
		return this.constraint(f, name, c, FloatArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraint(ToDoubleArray<T> f, String name,
			Function<DoubleArrayConstraint<T>, DoubleArrayConstraint<T>> c) {
		return this.constraint(f, name, c, DoubleArrayConstraint::new);
	}

	public ValidatorBuilder<T> constraintOnTarget(Predicate<T> predicate, String name,
			ViolationMessage violationMessage) {
		Deque<ConstraintPredicate<T>> predicates = new LinkedList<>();
		predicates.add(ConstraintPredicate.of(predicate, violationMessage,
				() -> new Object[] { name }, NullAs.INVALID));
		this.predicatesList
				.add(new ConstraintPredicates<>(Function.identity(), name, predicates));
		return this;
	}

	public ValidatorBuilder<T> constraintOnTarget(Predicate<T> predicate, String name,
			String messageKey, String defaultMessage) {
		return this.constraintOnTarget(predicate, name,
				ViolationMessage.of(messageKey, defaultMessage));
	}

	public ValidatorBuilder<T> constraintOnTarget(CustomConstraint<T> customConstraint,
			String name) {
		return this.constraintOnTarget(customConstraint, name, customConstraint);
	}

	public <E> ValidatorBuilder<T> constraintOnObject(Function<T, E> f, String name,
			Function<ObjectConstraint<T, E>, ObjectConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectConstraint::new);
	}

	public <N> ValidatorBuilder<T> nest(Function<T, N> nested, String name,
			Validator<N> validator) {
		return this.nest(nested, name, validator, NullAs.INVALID);
	}

	public <N> ValidatorBuilder<T> nestIfPresent(Function<T, N> nested, String name,
			Validator<N> validator) {
		return this.nest(nested, name, validator, NullAs.VALID);
	}

	@SuppressWarnings("unchecked")
	protected final <N> ValidatorBuilder<T> nest(Function<T, N> nested, String name,
			Validator<N> validator, NullAs nullAs) {
		if (!nullAs.skipNull()) {
			this.constraintOnObject(nested, name, Constraint::notNull);
		}
		validator.forEachPredicates(predicates -> {
			String nestedName = name + this.messageKeySeparator + predicates.name();
			ConstraintPredicates constraintPredicates = new NestedConstraintPredicates(
					this.toNestedValue(nested, predicates), nestedName,
					predicates.predicates(), nested);
			this.predicatesList.add(constraintPredicates);
		});
		return this;
	}

	public <N> ValidatorBuilder<T> nest(Function<T, N> nested, String name,
			ValidatorBuilderConverter<N> converter) {
		return this.nest(nested, name, converter, NullAs.INVALID);
	}

	public <N> ValidatorBuilder<T> nestIfPresent(Function<T, N> nested, String name,
			ValidatorBuilderConverter<N> converter) {
		return this.nest(nested, name, converter, NullAs.VALID);
	}

	@SuppressWarnings("unchecked")
	protected final <N> ValidatorBuilder<T> nest(Function<T, N> nested, String name,
			ValidatorBuilderConverter<N> converter, NullAs nullAs) {
		if (!nullAs.skipNull()) {
			this.constraintOnObject(nested, name, Constraint::notNull);
		}
		ValidatorBuilder<N> builder = converter.apply(new ValidatorBuilder<>());
		builder.predicatesList.forEach(predicates -> {
			ConstraintPredicates constraintPredicates = new NestedConstraintPredicates(
					this.toNestedValue(nested, predicates),
					name + ValidatorBuilder.this.messageKeySeparator + predicates.name(),
					predicates.predicates(), nested);
			this.predicatesList.add(constraintPredicates);
		});
		return this;
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> forEach(
			ToCollection<T, L, E> toCollection, String name, Validator<E> validator) {
		return this.forEach(toCollection, name, validator, NullAs.INVALID);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> forEachIfPresent(
			ToCollection<T, L, E> toCollection, String name, Validator<E> validator) {
		return this.forEach(toCollection, name, validator, NullAs.VALID);
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

	public <L extends Collection<E>, E> ValidatorBuilder<T> forEach(
			ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEach(toCollection, name, converter, NullAs.INVALID);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> forEachIfPresent(
			ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEach(toCollection, name, converter, NullAs.VALID);
	}

	protected <L extends Collection<E>, E> ValidatorBuilder<T> forEach(
			ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter, NullAs nullAs) {
		if (!nullAs.skipNull()) {
			this.constraintOnObject(toCollection, name, Constraint::notNull);
		}
		ValidatorBuilder<E> builder = converter.apply(new ValidatorBuilder<>());
		this.collectionValidators
				.add(new CollectionValidator<>(toCollection, name, builder.build()));
		return this;
	}

	public <K, V> ValidatorBuilder<T> forEach(ToMap<T, K, V> toMap, String name,
			Validator<V> validator) {
		return this.forEach(this.toMapToCollection(toMap), name, validator);
	}

	public <K, V> ValidatorBuilder<T> forEachIfPresent(ToMap<T, K, V> toMap, String name,
			Validator<V> validator) {
		return this.forEachIfPresent(this.toMapToCollection(toMap), name, validator);
	}

	public <K, V> ValidatorBuilder<T> forEach(ToMap<T, K, V> toMap, String name,
			ValidatorBuilderConverter<V> converter) {
		return this.forEach(this.toMapToCollection(toMap), name, converter);
	}

	public <K, V> ValidatorBuilder<T> forEachIfPresent(ToMap<T, K, V> toMap, String name,
			ValidatorBuilderConverter<V> converter) {
		return this.forEachIfPresent(this.toMapToCollection(toMap), name, converter);
	}

	public <E> ValidatorBuilder<T> forEach(ToObjectArray<T, E> toObjectArray, String name,
			Validator<E> validator) {
		return this.forEach(this.toObjectArrayToCollection(toObjectArray), name,
				validator);
	}

	public <E> ValidatorBuilder<T> forEachIfPresent(ToObjectArray<T, E> toObjectArray,
			String name, Validator<E> validator) {
		return this.forEachIfPresent(this.toObjectArrayToCollection(toObjectArray), name,
				validator);
	}

	public <E> ValidatorBuilder<T> forEach(ToObjectArray<T, E> toObjectArray, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEach(this.toObjectArrayToCollection(toObjectArray), name,
				converter);
	}

	public <E> ValidatorBuilder<T> forEachIfPresent(ToObjectArray<T, E> toObjectArray,
			String name, ValidatorBuilderConverter<E> converter) {
		return this.forEachIfPresent(this.toObjectArrayToCollection(toObjectArray), name,
				converter);
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

	protected final <V, C extends Constraint<T, V, C>> ValidatorBuilder<T> constraint(
			Function<T, V> f, String name, Function<C, C> c, Supplier<C> supplier) {
		C constraint = supplier.get();
		Deque<ConstraintPredicate<V>> predicates = c.apply(constraint).predicates();
		this.predicatesList.add(new ConstraintPredicates<>(f, name, predicates));
		return this;
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

	public Validator<T> build() {
		return new Validator<>(messageKeySeparator, this.predicatesList,
				this.collectionValidators, this.conditionalValidators,
				this.messageFormatter == null ? new SimpleMessageFormatter()
						: this.messageFormatter);
	}

	public interface ToCharSequence<T, E extends CharSequence> extends Function<T, E> {
	}

	public interface ToBoolean<T> extends Function<T, Boolean> {
	}

	public interface ToCharacter<T> extends Function<T, Character> {
	}

	public interface ToByte<T> extends Function<T, Byte> {
	}

	public interface ToShort<T> extends Function<T, Short> {
	}

	public interface ToInteger<T> extends Function<T, Integer> {
	}

	public interface ToLong<T> extends Function<T, Long> {
	}

	public interface ToFloat<T> extends Function<T, Float> {
	}

	public interface ToDouble<T> extends Function<T, Double> {
	}

	public interface ToBigInteger<T> extends Function<T, BigInteger> {
	}

	public interface ToBigDecimal<T> extends Function<T, BigDecimal> {
	}

	public interface ToCollection<T, L extends Collection<E>, E> extends Function<T, L> {
	}

	public interface ToMap<T, K, V> extends Function<T, Map<K, V>> {
	}

	public interface ToObjectArray<T, E> extends Function<T, E[]> {
	}

	public interface ToBooleanArray<T> extends Function<T, boolean[]> {
	}

	public interface ToCharArray<T> extends Function<T, char[]> {
	}

	public interface ToByteArray<T> extends Function<T, byte[]> {
	}

	public interface ToShortArray<T> extends Function<T, short[]> {
	}

	public interface ToIntArray<T> extends Function<T, int[]> {
	}

	public interface ToLongArray<T> extends Function<T, long[]> {
	}

	public interface ToFloatArray<T> extends Function<T, float[]> {
	}

	public interface ToDoubleArray<T> extends Function<T, double[]> {
	}

	public interface ValidatorBuilderConverter<T>
			extends Function<ValidatorBuilder<T>, ValidatorBuilder<T>> {
	}

	/**
	 * Deprecated in favor of {@link #constraintOnObject(Function, String, Function)}
	 */
	@Deprecated
	public <E> ValidatorBuilder<T> constraintForObject(Function<T, E> f, String name,
			Function<ObjectConstraint<T, E>, ObjectConstraint<T, E>> c) {
		return this.constraintOnObject(f, name, c);
	}

	/**
	 * Deprecated in favor of {@code nest}
	 */
	@Deprecated
	public <N> ValidatorBuilder<T> constraintForNested(Function<T, N> nested, String name,
			Validator<N> validator) {
		return this.nest(nested, name, validator);
	}

	/**
	 * Deprecated in favor of {@code nestIfPresent}
	 */
	@Deprecated
	public <N> ValidatorBuilder<T> constraintIfPresentForNested(Function<T, N> nested,
			String name, Validator<N> validator) {
		return this.nestIfPresent(nested, name, validator);
	}

	/**
	 * Deprecated in favor of {@code nest}
	 */
	@Deprecated
	public <N> ValidatorBuilder<T> constraintForNested(Function<T, N> nested, String name,
			ValidatorBuilderConverter<N> converter) {
		return this.nest(nested, name, converter);
	}

	/**
	 * Deprecated in favor of {@code nestIfPresent}
	 */
	@Deprecated
	public <N> ValidatorBuilder<T> constraintIfPresentForNested(Function<T, N> nested,
			String name, ValidatorBuilderConverter<N> converter) {
		return this.nestIfPresent(nested, name, converter);
	}

	/**
	 * Deprecated in favor of {@code forEach}
	 */
	@Deprecated
	public <L extends Collection<E>, E> ValidatorBuilder<T> constraintForEach(
			ToCollection<T, L, E> toCollection, String name, Validator<E> validator) {
		return this.forEach(toCollection, name, validator);
	}

	/**
	 * Deprecated in favor of {@code forEachIfPresent}
	 */
	@Deprecated
	public <L extends Collection<E>, E> ValidatorBuilder<T> constraintIfPresentForEach(
			ToCollection<T, L, E> toCollection, String name, Validator<E> validator) {
		return this.forEachIfPresent(toCollection, name, validator);
	}

	/**
	 * Deprecated in favor of {@code forEach}
	 */
	@Deprecated
	public <L extends Collection<E>, E> ValidatorBuilder<T> constraintForEach(
			ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEach(toCollection, name, converter);
	}

	/**
	 * Deprecated in favor of {@code forEachIfPresent}
	 */
	@Deprecated
	public <L extends Collection<E>, E> ValidatorBuilder<T> constraintIfPresentForEach(
			ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEachIfPresent(toCollection, name, converter);
	}

	/**
	 * Deprecated in favor of {@code forEach}
	 */
	@Deprecated
	public <K, V> ValidatorBuilder<T> constraintForEach(ToMap<T, K, V> toMap, String name,
			Validator<V> validator) {
		return this.forEach(toMap, name, validator);
	}

	/**
	 * Deprecated in favor of {@code forEachIfPresent}
	 */
	@Deprecated
	public <K, V> ValidatorBuilder<T> constraintIfPresentForEach(ToMap<T, K, V> toMap,
			String name, Validator<V> validator) {
		return this.forEachIfPresent(toMap, name, validator);
	}

	/**
	 * Deprecated in favor of {@code forEach}
	 */
	@Deprecated
	public <K, V> ValidatorBuilder<T> constraintForEach(ToMap<T, K, V> toMap, String name,
			ValidatorBuilderConverter<V> converter) {
		return this.forEach(toMap, name, converter);
	}

	/**
	 * Deprecated in favor of {@code forEachIfPresent}
	 */
	@Deprecated
	public <K, V> ValidatorBuilder<T> constraintIfPresentForEach(ToMap<T, K, V> toMap,
			String name, ValidatorBuilderConverter<V> converter) {
		return this.forEachIfPresent(toMap, name, converter);
	}

	/**
	 * Deprecated in favor of {@code forEach}
	 */
	@Deprecated
	public <E> ValidatorBuilder<T> constraintForEach(ToObjectArray<T, E> toObjectArray,
			String name, Validator<E> validator) {
		return this.forEach(toObjectArray, name, validator);
	}

	/**
	 * Deprecated in favor of {@code forEachIfPresent}
	 */
	@Deprecated
	public <E> ValidatorBuilder<T> constraintIfPresentForEach(
			ToObjectArray<T, E> toObjectArray, String name, Validator<E> validator) {
		return this.forEachIfPresent(toObjectArray, name, validator);
	}

	/**
	 * Deprecated in favor of {@code forEach}
	 */
	@Deprecated
	public <E> ValidatorBuilder<T> constraintForEach(ToObjectArray<T, E> toObjectArray,
			String name, ValidatorBuilderConverter<E> converter) {
		return this.forEach(toObjectArray, name, converter);
	}

	/**
	 * Deprecated in favor of {@code forEachIfPresent}
	 */
	@Deprecated
	public <E> ValidatorBuilder<T> constraintIfPresentForEach(
			ToObjectArray<T, E> toObjectArray, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.forEachIfPresent(toObjectArray, name, converter);
	}
}
