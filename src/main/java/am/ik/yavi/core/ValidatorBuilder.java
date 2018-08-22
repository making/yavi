package am.ik.yavi.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import am.ik.yavi.constraint.*;
import am.ik.yavi.constraint.array.*;
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;

public class ValidatorBuilder<T> {
	private static final String DEFAULT_SEPARATOR = ".";
	private final String messageKeySeparator;
	private MessageFormatter messageFormatter;
	private final List<ConstraintPredicates<T, ?>> predicatesList = new ArrayList<>();

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

	public <N> ValidatorBuilder<T> constraint(Function<T, N> nested, String name,
			Validator<N> validator) {
		return this.constraint(nested, name, validator, NullValidity.NULL_IS_INVALID);
	}

	public <N> ValidatorBuilder<T> constraintIfNotNull(Function<T, N> nested, String name,
			Validator<N> validator) {
		return this.constraint(nested, name, validator, NullValidity.NULL_IS_VALID);
	}

	@SuppressWarnings("unchecked")
	protected final <N> ValidatorBuilder<T> constraint(Function<T, N> nested, String name,
			Validator<N> validator, NullValidity nullValidity) {
		if (!nullValidity.skipNull()) {
			this.constraintForObject(nested, name, Constraint::notNull);
		}
		validator.predicatesList.forEach(predicates -> {
			String nestedName = name + this.messageKeySeparator + predicates.name();
			ConstraintPredicates constraintPredicates = new NestedConstraintPredicates(
					(Function<T, Object>) ((T target) -> {
						N nestedValue = nested.apply(target);
						if (nestedValue == null) {
							return null;
						}
						return predicates.toValue().apply(nestedValue);
					}), nestedName, predicates.predicates(), nested);
			this.predicatesList.add(constraintPredicates);
		});
		return this;
	}

	protected final <V, C extends Constraint<T, V, C>> ValidatorBuilder<T> constraint(
			Function<T, V> f, String name, Function<C, C> c, Supplier<C> supplier) {
		C constraint = supplier.get();
		List<ConstraintPredicate<V>> predicates = c.apply(constraint).predicates();
		this.predicatesList.add(new ConstraintPredicates<>(f, name, predicates));
		return this;
	}

	public Validator<T> build() {
		return new Validator<>(this.predicatesList,
				this.messageFormatter == null ? new SimpleMessageFormatter()
						: this.messageFormatter);
	}

	public interface ToCharSequence<T, E extends CharSequence> extends Function<T, E> {
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
}
