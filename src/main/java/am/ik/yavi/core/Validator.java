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
import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;

public class Validator<T> {
	private static final String SEPARATOR = ".";
	private final MessageFormatter messageFormatter;
	private final List<ConstraintHolders<T, ?>> holdersList = new ArrayList<>();

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

	public final <E> Validator<T> constraint(ToArray<T, E> f, String name,
			Function<ArrayConstraint<T, E>, ArrayConstraint<T, E>> c) {
		return this.constraint(f, name, c, ArrayConstraint::new);
	}

	public final <E> Validator<T> constraint(ToCollection<T, E> f, String name,
			Function<CollectionConstraint<T, E>, CollectionConstraint<T, E>> c) {
		return this.constraint(f, name, c, CollectionConstraint::new);
	}

	public final <K, V> Validator<T> constraint(ToMap<T, K, V> f, String name,
			Function<MapConstraint<T, K, V>, MapConstraint<T, K, V>> c) {
		return this.constraint(f, name, c, MapConstraint::new);
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
		validator.holdersList.forEach(holders -> {
			String nestedName = name + SEPARATOR + holders.name();
			ConstraintHolders constraintHolders = new NestedConstraintHolders(
					(Function<T, Object>) ((T target) -> {
						N nestedValue = nested.apply(target);
						if (nestedValue == null) {
							return null;
						}
						return holders.toValue().apply(nestedValue);
					}), nestedName, holders.holders(), nested);
			this.holdersList.add(constraintHolders);
		});
		return this;
	}

	protected final <V, C extends Constraint<T, V, C>> Validator<T> constraint(
			Function<T, V> f, String name, Function<C, C> c, Supplier<C> supplier) {
		C constraint = supplier.get();
		List<ConstraintHolder<V>> holders = c.apply(constraint).holders();
		this.holdersList.add(new ConstraintHolders<>(f, name, holders));
		return this;
	}

	@SuppressWarnings("unchecked")
	public ConstraintViolations validate(T target) {
		ConstraintViolations violations = new ConstraintViolations();
		for (ConstraintHolders<T, ?> holders : this.holdersList) {
			if (holders instanceof NestedConstraintHolders) {
				NestedConstraintHolders<T, ?, ?> nested = (NestedConstraintHolders<T, ?, ?>) holders;
				Object nestedValue = nested.nestedValue(target);
				if (nestedValue == null) {
					continue;
				}
			}
			for (ConstraintHolder<?> holder : holders.holders()) {
				Object v = holders.toValue().apply(target);
				Predicate<Object> predicate = (Predicate<Object>) holder.predicate();
				if (v == null && holder.nullValidity().skipNull()) {
					continue;
				}
				if (!predicate.test(v)) {
					String name = holders.name();
					Object[] args = holder.args().get();
					violations.add(new ConstraintViolation(name, holder.messageKey(),
							holder.defaultMessageFormat(), pad(name, args, v), v,
							this.messageFormatter));
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

	public interface ToArray<T, E> extends Function<T, E[]> {
	}

	public interface ToCollection<T, E> extends Function<T, Collection<E>> {
	}

	public interface ToMap<T, K, V> extends Function<T, Map<K, V>> {
	}
}
