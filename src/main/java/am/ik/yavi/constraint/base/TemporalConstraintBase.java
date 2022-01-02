/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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
		return this.before(() -> this.getNow(clock)).message(TEMPORAL_PAST);
	}

	public C pastOrPresent() {
		return this.pastOrPresent(Clock.systemDefaultZone());
	}

	public C pastOrPresent(Clock clock) {
		return this.beforeOrEqual(() -> this.getNow(clock))
				.message(TEMPORAL_PAST_OR_PRESENT);
	}

	public C future() {
		return this.future(Clock.systemDefaultZone());
	}

	public C future(Clock clock) {
		return this.after(() -> this.getNow(clock)).message(TEMPORAL_FUTURE);
	}

	public C futureOrPresent() {
		return this.futureOrPresent(Clock.systemDefaultZone());
	}

	public C futureOrPresent(Clock clock) {
		return this.afterOrEqual(() -> this.getNow(clock))
				.message(TEMPORAL_FUTURE_OR_PRESENT);
	}

	/**
	 * Is the given temporal before the supplied {@code other}
	 *
	 * @param other the supplier providing the other temporal that is before
	 */
	public C before(Supplier<V> other) {
		final Supplier<V> memoized = memoize(other);
		this.predicates()
				.add(ConstraintPredicate.of(x -> this.isBefore(x, memoized.get()),
						TEMPORAL_BEFORE, () -> new Object[] { memoized.get() }, VALID));
		return cast();
	}

	public C beforeOrEqual(Supplier<V> other) {
		final Supplier<V> memoized = memoize(other);
		this.predicates()
				.add(ConstraintPredicate.of(x -> !this.isAfter(x, memoized.get()),
						TEMPORAL_BEFORE_OR_EQUAL, () -> new Object[] { memoized.get() },
						VALID));
		return cast();
	}

	/**
	 * Is the given temporal after the supplied {@code other}
	 *
	 * @param other the supplier providing the other temporal that is before
	 */
	public C after(Supplier<V> other) {
		final Supplier<V> memoized = memoize(other);
		this.predicates().add(ConstraintPredicate.of(x -> this.isAfter(x, memoized.get()),
				TEMPORAL_AFTER, () -> new Object[] { memoized.get() }, VALID));
		return cast();
	}

	public C afterOrEqual(Supplier<V> other) {
		final Supplier<V> memoized = memoize(other);
		this.predicates()
				.add(ConstraintPredicate.of(x -> !this.isBefore(x, memoized.get()),
						TEMPORAL_AFTER_OR_EQUAL, () -> new Object[] { memoized.get() },
						VALID));
		return cast();
	}

	/**
	 * Is the given temporal between the supplied {@code rangeFrom} and {@code rangeTo}.
	 * The range is not inclusive. This means if the dates are equal (rangeFrom = x =
	 * rangeTo) it is invalid
	 *
	 * @param rangeFrom the supplier provide the start of the range the temporal has to be
	 *     in
	 * @param rangeTo the supplier provide the end of the range the temporal has to be in
	 */
	public C between(Supplier<V> rangeFrom, Supplier<V> rangeTo) {
		final Supplier<V> memoizedFrom = memoize(rangeFrom);
		final Supplier<V> memoizedTo = memoize(rangeTo);
		this.predicates().add(ConstraintPredicate.of(x -> {
			final V from = memoizedFrom.get();
			final V to = memoizedTo.get();
			if (this.isAfter(from, to)) {
				throw new IllegalArgumentException(
						"Parameter 'rangeFrom' has to be before 'rangeTo'");
			}
			return this.isBefore(from, x) && this.isAfter(to, x);
		}, TEMPORAL_BETWEEN, () -> new Object[] { memoizedFrom.get(), memoizedTo.get() },
				VALID));
		return cast();
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
