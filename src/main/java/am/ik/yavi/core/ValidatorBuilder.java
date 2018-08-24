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
package am.ik.yavi.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import am.ik.yavi.constraint.*;
import am.ik.yavi.constraint.array.*;
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;

public class ValidatorBuilder<T> {
	private static final String DEFAULT_SEPARATOR = ".";
	private final String messageKeySeparator;
	private final List<ConstraintPredicates<T, ?>> predicatesList = new ArrayList<>();
	private final List<CollectionValidator<T, ?, ?>> collectionValidators = new ArrayList<>();
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

	public <E extends CharSequence> ValidatorBuilder<T> constraint(ToCharSequence<T, E> f,
			Normalizer.Form normalizerForm, String name,
			Function<CharSequenceConstraint<T, E>, CharSequenceConstraint<T, E>> c) {
		return this.constraint(f, name, c,
				() -> new CharSequenceConstraint<>(normalizerForm));
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

	public <E> ValidatorBuilder<T> constraintForObject(Function<T, E> f, String name,
			Function<ObjectConstraint<T, E>, ObjectConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectConstraint::new);
	}

	public <N> ValidatorBuilder<T> constraintForNested(Function<T, N> nested, String name,
			Validator<N> validator) {
		return this.constraintForNested(nested, name, validator,
				NullValidity.NULL_IS_INVALID);
	}

	public <N> ValidatorBuilder<T> constraintIfPresentForNested(Function<T, N> nested,
			String name, Validator<N> validator) {
		return this.constraintForNested(nested, name, validator,
				NullValidity.NULL_IS_VALID);
	}

	@SuppressWarnings("unchecked")
	protected final <N> ValidatorBuilder<T> constraintForNested(Function<T, N> nested,
			String name, Validator<N> validator, NullValidity nullValidity) {
		if (!nullValidity.skipNull()) {
			this.constraintForObject(nested, name, Constraint::notNull);
		}
		validator.predicatesList.forEach(predicates -> {
			String nestedName = name + this.messageKeySeparator + predicates.name();
			ConstraintPredicates constraintPredicates = new NestedConstraintPredicates(
					this.toNestedValue(nested, predicates), nestedName,
					predicates.predicates(), nested);
			this.predicatesList.add(constraintPredicates);
		});
		return this;
	}

	@SuppressWarnings("unchecked")
	public <N> ValidatorBuilder<T> constraintForNested(Function<T, N> nested, String name,
			ValidatorBuilderConverter<N> converter) {
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

	public <L extends Collection<E>, E> ValidatorBuilder<T> constraintForEach(
			ToCollection<T, L, E> toCollection, String name, Validator<E> validator) {
		return this.constraintForEach(toCollection, name, validator,
				NullValidity.NULL_IS_INVALID);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> constraintIfPresentForEach(
			ToCollection<T, L, E> toCollection, String name, Validator<E> validator) {
		return this.constraintForEach(toCollection, name, validator,
				NullValidity.NULL_IS_VALID);
	}

	protected final <L extends Collection<E>, E> ValidatorBuilder<T> constraintForEach(
			ToCollection<T, L, E> toCollection, String name, Validator<E> validator,
			NullValidity nullValidity) {
		if (!nullValidity.skipNull()) {
			this.constraintForObject(toCollection, name, Constraint::notNull);
		}
		this.collectionValidators
				.add(new CollectionValidator<>(toCollection, name, validator));
		return this;
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> constraintForEach(
			ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.constraintForEach(toCollection, name, converter,
				NullValidity.NULL_IS_INVALID);
	}

	public <L extends Collection<E>, E> ValidatorBuilder<T> constraintIfPresentForEach(
			ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.constraintForEach(toCollection, name, converter,
				NullValidity.NULL_IS_VALID);
	}

	protected <L extends Collection<E>, E> ValidatorBuilder<T> constraintForEach(
			ToCollection<T, L, E> toCollection, String name,
			ValidatorBuilderConverter<E> converter, NullValidity nullValidity) {
		if (!nullValidity.skipNull()) {
			this.constraintForObject(toCollection, name, Constraint::notNull);
		}
		ValidatorBuilder<E> builder = converter.apply(new ValidatorBuilder<>());
		this.collectionValidators
				.add(new CollectionValidator<>(toCollection, name, builder.build()));
		return this;
	}

	public <K, V> ValidatorBuilder<T> constraintForEach(ToMap<T, K, V> toMap, String name,
			Validator<V> validator) {
		return this.constraintForEach(this.toMapToCollection(toMap), name, validator);
	}

	public <K, V> ValidatorBuilder<T> constraintIfPresentForEach(ToMap<T, K, V> toMap,
			String name, Validator<V> validator) {
		return this.constraintIfPresentForEach(this.toMapToCollection(toMap), name,
				validator);
	}

	public <K, V> ValidatorBuilder<T> constraintForEach(ToMap<T, K, V> toMap, String name,
			ValidatorBuilderConverter<V> converter) {
		return this.constraintForEach(this.toMapToCollection(toMap), name, converter);
	}

	public <K, V> ValidatorBuilder<T> constraintIfPresentForEach(ToMap<T, K, V> toMap,
			String name, ValidatorBuilderConverter<V> converter) {
		return this.constraintIfPresentForEach(this.toMapToCollection(toMap), name,
				converter);
	}

	public <E> ValidatorBuilder<T> constraintForEach(ToObjectArray<T, E> toObjectArray,
			String name, Validator<E> validator) {
		return this.constraintForEach(this.toObjectArrayToCollection(toObjectArray), name,
				validator);
	}

	public <E> ValidatorBuilder<T> constraintIfPresentForEach(
			ToObjectArray<T, E> toObjectArray, String name, Validator<E> validator) {
		return this.constraintIfPresentForEach(
				this.toObjectArrayToCollection(toObjectArray), name, validator);
	}

	public <E> ValidatorBuilder<T> constraintForEach(ToObjectArray<T, E> toObjectArray,
			String name, ValidatorBuilderConverter<E> converter) {
		return this.constraintForEach(this.toObjectArrayToCollection(toObjectArray), name,
				converter);
	}

	public <E> ValidatorBuilder<T> constraintIfPresentForEach(
			ToObjectArray<T, E> toObjectArray, String name,
			ValidatorBuilderConverter<E> converter) {
		return this.constraintIfPresentForEach(
				this.toObjectArrayToCollection(toObjectArray), name, converter);
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
		List<ConstraintPredicate<V>> predicates = c.apply(constraint).predicates();
		this.predicatesList.add(new ConstraintPredicates<>(f, name, predicates));
		return this;
	}

	public Validator<T> build() {
		return new Validator<>(messageKeySeparator, this.predicatesList,
				this.collectionValidators,
				this.messageFormatter == null ? new SimpleMessageFormatter()
						: this.messageFormatter);
	}

	public interface ToCharSequence<T, E extends CharSequence> extends Function<T, E> {
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
}
