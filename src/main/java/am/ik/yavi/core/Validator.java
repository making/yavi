package am.ik.yavi.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import am.ik.yavi.constraint.*;
import am.ik.yavi.constraint.array.*;
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;

public class Validator<T> {
	private static final String SEPARATOR = ".";
	private final MessageFormatter messageFormatter;
	private final List<ConstraintPredicates<T, ?>> predicatesList = new ArrayList<>();

	public Validator() {
		this(new SimpleMessageFormatter());
	}

	public Validator(MessageFormatter messageFormatter) {
		this.messageFormatter = messageFormatter;
	}

	public final <E extends CharSequence> Validator<T> constraint(ToCharSequence<T, E> f,
			String name,
			Function<CharSequenceConstraint<T, E>, CharSequenceConstraint<T, E>> c) {
		return this.constraint(f, name, c, CharSequenceConstraint::new);
	}

	public final Validator<T> constraint(ToByte<T> f, String name,
			Function<ByteConstraint<T>, ByteConstraint<T>> c) {
		return this.constraint(f, name, c, ByteConstraint::new);
	}

	public final Validator<T> constraint(ToShort<T> f, String name,
			Function<ShortConstraint<T>, ShortConstraint<T>> c) {
		return this.constraint(f, name, c, ShortConstraint::new);
	}

	public final Validator<T> constraint(ToInteger<T> f, String name,
			Function<IntegerConstraint<T>, IntegerConstraint<T>> c) {
		return this.constraint(f, name, c, IntegerConstraint::new);
	}

	public final Validator<T> constraint(ToLong<T> f, String name,
			Function<LongConstraint<T>, LongConstraint<T>> c) {
		return this.constraint(f, name, c, LongConstraint::new);
	}

	public final Validator<T> constraint(ToFloat<T> f, String name,
			Function<FloatConstraint<T>, FloatConstraint<T>> c) {
		return this.constraint(f, name, c, FloatConstraint::new);
	}

	public final Validator<T> constraint(ToDouble<T> f, String name,
			Function<DoubleConstraint<T>, DoubleConstraint<T>> c) {
		return this.constraint(f, name, c, DoubleConstraint::new);
	}

	public final Validator<T> constraint(ToBigInteger<T> f, String name,
			Function<BigIntegerConstraint<T>, BigIntegerConstraint<T>> c) {
		return this.constraint(f, name, c, BigIntegerConstraint::new);
	}

	public final Validator<T> constraint(ToBigDecimal<T> f, String name,
			Function<BigDecimalConstraint<T>, BigDecimalConstraint<T>> c) {
		return this.constraint(f, name, c, BigDecimalConstraint::new);
	}

	public final <E> Validator<T> constraint(ToCollection<T, E> f, String name,
			Function<CollectionConstraint<T, E>, CollectionConstraint<T, E>> c) {
		return this.constraint(f, name, c, CollectionConstraint::new);
	}

	public final <K, V> Validator<T> constraint(ToMap<T, K, V> f, String name,
			Function<MapConstraint<T, K, V>, MapConstraint<T, K, V>> c) {
		return this.constraint(f, name, c, MapConstraint::new);
	}

	public final <E> Validator<T> constraint(ToObjectArray<T, E> f, String name,
			Function<ObjectArrayConstraint<T, E>, ObjectArrayConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectArrayConstraint::new);
	}

	public final Validator<T> constraint(ToBooleanArray<T> f, String name,
			Function<BooleanArrayConstraint<T>, BooleanArrayConstraint<T>> c) {
		return this.constraint(f, name, c, BooleanArrayConstraint::new);
	}

	public final Validator<T> constraint(ToCharArray<T> f, String name,
			Function<CharArrayConstraint<T>, CharArrayConstraint<T>> c) {
		return this.constraint(f, name, c, CharArrayConstraint::new);
	}

	public final Validator<T> constraint(ToByteArray<T> f, String name,
			Function<ByteArrayConstraint<T>, ByteArrayConstraint<T>> c) {
		return this.constraint(f, name, c, ByteArrayConstraint::new);
	}

	public final Validator<T> constraint(ToShortArray<T> f, String name,
			Function<ShortArrayConstraint<T>, ShortArrayConstraint<T>> c) {
		return this.constraint(f, name, c, ShortArrayConstraint::new);
	}

	public final Validator<T> constraint(ToIntArray<T> f, String name,
			Function<IntArrayConstraint<T>, IntArrayConstraint<T>> c) {
		return this.constraint(f, name, c, IntArrayConstraint::new);
	}

	public final Validator<T> constraint(ToLongArray<T> f, String name,
			Function<LongArrayConstraint<T>, LongArrayConstraint<T>> c) {
		return this.constraint(f, name, c, LongArrayConstraint::new);
	}

	public final Validator<T> constraint(ToFloatArray<T> f, String name,
			Function<FloatArrayConstraint<T>, FloatArrayConstraint<T>> c) {
		return this.constraint(f, name, c, FloatArrayConstraint::new);
	}

	public final Validator<T> constraint(ToDoubleArray<T> f, String name,
			Function<DoubleArrayConstraint<T>, DoubleArrayConstraint<T>> c) {
		return this.constraint(f, name, c, DoubleArrayConstraint::new);
	}

	public final <E> Validator<T> constraintForObject(Function<T, E> f, String name,
			Function<ObjectConstraint<T, E>, ObjectConstraint<T, E>> c) {
		return this.constraint(f, name, c, ObjectConstraint::new);
	}

	public <N> Validator<T> constraint(Function<T, N> nested, String name,
			Validator<N> validator) {
		return this.constraint(nested, name, validator, NullValidity.NULL_IS_INVALID);
	}

	public <N> Validator<T> constraintIfNotNull(Function<T, N> nested, String name,
			Validator<N> validator) {
		return this.constraint(nested, name, validator, NullValidity.NULL_IS_VALID);
	}

	@SuppressWarnings("unchecked")
	private <N> Validator<T> constraint(Function<T, N> nested, String name,
			Validator<N> validator, NullValidity nullValidity) {
		if (!nullValidity.skipNull()) {
			this.constraintForObject(nested, name, Constraint::notNull);
		}
		validator.predicatesList.forEach(predicates -> {
			String nestedName = name + SEPARATOR + predicates.name();
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

	protected final <V, C extends Constraint<T, V, C>> Validator<T> constraint(
			Function<T, V> f, String name, Function<C, C> c, Supplier<C> supplier) {
		C constraint = supplier.get();
		List<ConstraintPredicate<V>> predicates = c.apply(constraint).predicates();
		this.predicatesList.add(new ConstraintPredicates<>(f, name, predicates));
		return this;
	}

	@SuppressWarnings("unchecked")
	public ConstraintViolations validate(T target) {
		ConstraintViolations violations = new ConstraintViolations();
		for (ConstraintPredicates<T, ?> predicates : this.predicatesList) {
			if (predicates instanceof NestedConstraintPredicates) {
				NestedConstraintPredicates<T, ?, ?> nested = (NestedConstraintPredicates<T, ?, ?>) predicates;
				Object nestedValue = nested.nestedValue(target);
				if (nestedValue == null) {
					continue;
				}
			}
			for (ConstraintPredicate<?> constraintPredicate : predicates.predicates()) {
				Object v = predicates.toValue().apply(target);
				Predicate<Object> predicate = (Predicate<Object>) constraintPredicate
						.predicate();
				if (v == null && constraintPredicate.nullValidity().skipNull()) {
					continue;
				}
				if (!predicate.test(v)) {
					String name = predicates.name();
					Object[] args = constraintPredicate.args().get();
					violations.add(new ConstraintViolation(name,
							constraintPredicate.messageKey(),
							constraintPredicate.defaultMessageFormat(),
							pad(name, args, v), v, this.messageFormatter));
				}
			}
		}
		return violations;
	}

	private Object[] pad(String name, Object[] args, Object value) {
		Object[] pad = new Object[args.length + 2];
		pad[0] = name;
		System.arraycopy(args, 0, pad, 1, args.length);
		pad[pad.length - 1] = value;
		return pad;
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

	public interface ToCollection<T, E> extends Function<T, Collection<E>> {
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
