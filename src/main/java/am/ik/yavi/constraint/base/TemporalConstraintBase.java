/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.constraint.base;

import java.time.Clock;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongPredicate;
import java.util.function.Supplier;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintPredicate;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_AFTER;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_AFTER_OR_EQUAL;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_BEFORE;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_BEFORE_OR_EQUAL;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_BETWEEN;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_FIELD;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_FUTURE;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_FUTURE_OR_PRESENT;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_PAST;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_PAST_OR_PRESENT;

/**
 * This is the base class for constraints on Temporal classes. Methods in the class
 * require the {@link V} to extend Temporal.
 *
 * @author Diego Krupitza
 * @author Toshiaki Maki
 * @since 0.10.0
 */
public abstract class TemporalConstraintBase<T, V extends TemporalAccessor, C extends Constraint<T, V, C>>
		extends ConstraintBase<T, V, C> {
	abstract protected boolean isAfter(V a, V b);

	abstract protected boolean isBefore(V a, V b);

	abstract protected V getNow(Clock clock);

	public C past() {
		return this.past(Clock.systemDefaultZone());
	}

	public C past(Clock clock) {
		return this.before(() -> this.getNow(clock), false).message(TEMPORAL_PAST);
	}

	public C pastOrPresent() {
		return this.pastOrPresent(Clock.systemDefaultZone());
	}

	public C pastOrPresent(Clock clock) {
		return this.beforeOrEqual(() -> this.getNow(clock), false)
				.message(TEMPORAL_PAST_OR_PRESENT);
	}

	public C future() {
		return this.future(Clock.systemDefaultZone());
	}

	public C future(Clock clock) {
		return this.after(() -> this.getNow(clock), false).message(TEMPORAL_FUTURE);
	}

	public C futureOrPresent() {
		return this.futureOrPresent(Clock.systemDefaultZone());
	}

	public C futureOrPresent(Clock clock) {
		return this.afterOrEqual(() -> this.getNow(clock), false)
				.message(TEMPORAL_FUTURE_OR_PRESENT);
	}

	/**
	 * Check if the given temporal is before the supplied {@code other} <br>
	 * You can specify whether to memoize the result of the supplier by using the argument
	 * <code>memoize</code>. If you set <code>memoize</code> to <code>false</code> and the
	 * supplier return value changes each time, be aware that the value used to compare
	 * the input values and the value contained in the error message can be different.
	 *
	 * @param other the supplier providing the other temporal that is before
	 * @param memoize whether to memoize the result of supplier
	 * @since 0.11.1
	 */
	public C before(Supplier<V> other, boolean memoize) {
		final Supplier<V> supplier = memoize ? memoize(other) : other;
		this.predicates()
				.add(ConstraintPredicate.of(x -> this.isBefore(x, supplier.get()),
						TEMPORAL_BEFORE, () -> new Object[] { supplier.get() }, VALID));
		return cast();
	}

	/**
	 * Check if the given temporal if before the supplied {@code other}. <br>
	 * <strong>The result of the supplier is memoized</strong>. That means the supplier is
	 * cached to always return the same value and is not available if you want to
	 * dynamically return different values.<br>
	 * If you don't want to memoize, use {@link #before(Supplier, boolean)} instead.
	 *
	 * @param other the supplier providing the other temporal that is before
	 */
	public C before(Supplier<V> other) {
		return this.before(other, true);
	}

	/**
	 * Check if the given temporal is before or equals to the supplied {@code other}<br>
	 * You can specify whether to memoize the result of the supplier by using the argument
	 * <code>memoize</code>. If you set <code>memoize</code> to <code>false</code> and the
	 * supplier return value changes each time, be aware that the value used to compare
	 * the input values and the value contained in the error message can be different.
	 *
	 * @param other the supplier providing the other temporal that is before or equals to
	 * @param memoize whether to memoize the result of supplier
	 * @since 0.11.1
	 */
	public C beforeOrEqual(Supplier<V> other, boolean memoize) {
		final Supplier<V> supplier = memoize ? memoize(other) : other;
		this.predicates()
				.add(ConstraintPredicate.of(x -> !this.isAfter(x, supplier.get()),
						TEMPORAL_BEFORE_OR_EQUAL, () -> new Object[] { supplier.get() },
						VALID));
		return cast();
	}

	/**
	 * Check if the given temporal if before or equals to the supplied {@code other}. <br>
	 * <strong>The result of the supplier is memoized</strong>. That means the supplier is
	 * cached to always return the same value and is not available if you want to
	 * dynamically return different values.<br>
	 * If you don't want to memoize, use {@link #beforeOrEqual(Supplier, boolean)}
	 * instead.
	 *
	 * @param other the supplier providing the other temporal that is before or equals to
	 */
	public C beforeOrEqual(Supplier<V> other) {
		return beforeOrEqual(other, true);
	}

	/**
	 * Check if the given temporal is after the supplied {@code other}<br>
	 * You can specify whether to memoize the result of the supplier by using the argument
	 * <code>memoize</code>. If you set <code>memoize</code> to <code>false</code> and the
	 * supplier return value changes each time, be aware that the value used to compare
	 * the input values and the value contained in the error message can be different.
	 *
	 * @param other the supplier providing the other temporal that is after
	 * @param memoize whether to memoize the result of supplier
	 * @since 0.11.1
	 */
	public C after(Supplier<V> other, boolean memoize) {
		final Supplier<V> supplier = memoize ? memoize(other) : other;
		this.predicates().add(ConstraintPredicate.of(x -> this.isAfter(x, supplier.get()),
				TEMPORAL_AFTER, () -> new Object[] { supplier.get() }, VALID));
		return cast();
	}

	/**
	 * Check if the given temporal if after the supplied {@code other}. <br>
	 * <strong>The result of the supplier is memoized</strong>. That means the supplier is
	 * cached to always return the same value and is not available if you want to
	 * dynamically return different values.<br>
	 * If you don't want to memoize, use {@link #after(Supplier, boolean)} instead.
	 *
	 * @param other the supplier providing the other temporal that is after
	 */
	public C after(Supplier<V> other) {
		return this.after(other, true);
	}

	/**
	 * Check if the given temporal is after or equals to the supplied {@code other}<br>
	 * You can specify whether to memoize the result of the supplier by using the argument
	 * <code>memoize</code>. If you set <code>memoize</code> to <code>false</code> and the
	 * supplier return value changes each time, be aware that the value used to compare
	 * the input values and the value contained in the error message can be different.
	 *
	 * @param other the supplier providing the other temporal that is after or equals to
	 * @param memoize whether to memoize the result of supplier
	 * @since 0.11.1
	 */
	public C afterOrEqual(Supplier<V> other, boolean memoize) {
		final Supplier<V> supplier = memoize ? memoize(other) : other;
		this.predicates()
				.add(ConstraintPredicate.of(x -> !this.isBefore(x, supplier.get()),
						TEMPORAL_AFTER_OR_EQUAL, () -> new Object[] { supplier.get() },
						VALID));
		return cast();
	}

	/**
	 * Check if the given temporal if after or equals to the supplied {@code other}. <br>
	 * <strong>The result of the supplier is memoized</strong>. That means the supplier is
	 * cached to always return the same value and is not available if you want to
	 * dynamically return different values.<br>
	 * If you don't want to memoize, use {@link #afterOrEqual(Supplier, boolean)} instead.
	 *
	 * @param other the supplier providing the other temporal that is after or equals to
	 */
	public C afterOrEqual(Supplier<V> other) {
		return afterOrEqual(other, true);
	}

	/**
	 * Is the given temporal between the supplied {@code rangeFrom} and {@code rangeTo}.
	 * The range is not inclusive. This means if the dates are equal (rangeFrom = x =
	 * rangeTo) it is invalid<br>
	 * You can specify whether to memoize the result of the supplier by using the argument
	 * <code>memoize</code>. If you set <code>memoize</code> to <code>false</code> and the
	 * supplier return value changes each time, be aware that the value used to compare
	 * the input values and the value contained in the error message can be different.
	 *
	 * @param rangeFrom the supplier provide the start of the range the temporal has to be
	 *     in
	 * @param rangeTo the supplier provide the end of the range the temporal has to be in
	 * @param memoize whether to memoize the result of supplier
	 * @since 0.11.1
	 */
	public C between(Supplier<V> rangeFrom, Supplier<V> rangeTo, boolean memoize) {
		final Supplier<V> supplierFrom = memoize ? memoize(rangeFrom) : rangeFrom;
		final Supplier<V> supplierTo = memoize ? memoize(rangeTo) : rangeTo;
		this.predicates().add(ConstraintPredicate.of(x -> {
			final V from = supplierFrom.get();
			final V to = supplierTo.get();
			if (this.isAfter(from, to)) {
				throw new IllegalArgumentException(
						"Parameter 'rangeFrom' has to be before 'rangeTo'");
			}
			return this.isBefore(from, x) && this.isAfter(to, x);
		}, TEMPORAL_BETWEEN, () -> new Object[] { supplierFrom.get(), supplierTo.get() },
				VALID));
		return cast();
	}

	/**
	 * Is the given temporal between the supplied {@code rangeFrom} and {@code rangeTo}.
	 * The range is not inclusive. This means if the dates are equal (rangeFrom = x =
	 * rangeTo) it is invalid <strong>The result of the supplier is memoized</strong>.
	 * That means the supplier is cached to always return the same value and is not
	 * available if you want to dynamically return different values.<br>
	 * If you don't want to memoize, use {@link #between(Supplier, Supplier, boolean)}
	 * instead.
	 *
	 * @param rangeFrom the supplier provide the start of the range the temporal has to be
	 *     in
	 * @param rangeTo the supplier provide the end of the range the temporal has to be in
	 */
	public C between(Supplier<V> rangeFrom, Supplier<V> rangeTo) {
		return this.between(rangeFrom, rangeTo, true);
	}

	public C fieldPredicate(TemporalField field, LongPredicate predicate) {
		this.predicates()
				.add(ConstraintPredicate.of(x -> predicate.test(x.getLong(field)),
						TEMPORAL_FIELD, () -> new Object[] { field }, VALID));
		return cast();
	}

	static <T> Supplier<T> memoize(Supplier<T> delegate) {
		final AtomicReference<T> supplier = new AtomicReference<>();
		return () -> {
			final T value = supplier.get();
			if (value == null) {
				return supplier
						.updateAndGet(prev -> prev == null ? delegate.get() : prev);
			}
			return value;
		};
	}
}
