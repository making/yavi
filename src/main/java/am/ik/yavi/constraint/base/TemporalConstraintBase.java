/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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
 * Base class for constraints on temporal values.
 *
 * <p>
 * A {@code null} value is considered valid and the temporal checks are skipped. The
 * actual comparison is delegated to the abstract methods of the concrete temporal type.
 * </p>
 *
 * @param <T> the type of the object that owns the property
 * @param <V> the temporal type the constraints apply to
 * @param <C> the concrete constraint type returned for method chaining
 * @author Diego Krupitza
 * @author Toshiaki Maki
 * @since 0.10.0
 */
public abstract class TemporalConstraintBase<T, V extends TemporalAccessor, C extends Constraint<T, V, C>>
		extends ConstraintBase<T, V, C> {

	/**
	 * Creates a new constraint without any predicate.
	 */
	public TemporalConstraintBase() {
	}

	/**
	 * Tests whether {@code a} is after {@code b}.
	 * @param a the first temporal
	 * @param b the second temporal
	 * @return {@code true} if {@code a} is after {@code b}
	 */
	abstract protected boolean isAfter(V a, V b);

	/**
	 * Tests whether {@code a} is before {@code b}.
	 * @param a the first temporal
	 * @param b the second temporal
	 * @return {@code true} if {@code a} is before {@code b}
	 */
	abstract protected boolean isBefore(V a, V b);

	/**
	 * Returns the current temporal for the given clock.
	 * @param clock the clock to read the current time from
	 * @return the current temporal
	 */
	abstract protected V getNow(Clock clock);

	/**
	 * Requires the value to be in the past according to the system default time zone.
	 *
	 * <p>
	 * A violation is reported with the message key {@code temporal.past} and the default
	 * message {@code "{0}" must be a past date}.
	 * </p>
	 * @return this constraint
	 */
	public C past() {
		return this.past(Clock.systemDefaultZone());
	}

	/**
	 * Requires the value to be in the past according to the given clock.
	 * @param clock the clock to read the current time from
	 * @return this constraint
	 */
	public C past(Clock clock) {
		return this.before(() -> this.getNow(clock), false).message(TEMPORAL_PAST);
	}

	/**
	 * Requires the value to be in the past or in the present according to the system
	 * default time zone.
	 *
	 * <p>
	 * A violation is reported with the message key {@code temporal.pastOrPresent} and the
	 * default message {@code "{0}" must be a date in the past or in the present}.
	 * </p>
	 * @return this constraint
	 */
	public C pastOrPresent() {
		return this.pastOrPresent(Clock.systemDefaultZone());
	}

	/**
	 * Requires the value to be in the past or in the present according to the given
	 * clock.
	 * @param clock the clock to read the current time from
	 * @return this constraint
	 */
	public C pastOrPresent(Clock clock) {
		return this.beforeOrEqual(() -> this.getNow(clock), false).message(TEMPORAL_PAST_OR_PRESENT);
	}

	/**
	 * Requires the value to be in the future according to the system default time zone.
	 *
	 * <p>
	 * A violation is reported with the message key {@code temporal.future} and the
	 * default message {@code "{0}" must be a future date}.
	 * </p>
	 * @return this constraint
	 */
	public C future() {
		return this.future(Clock.systemDefaultZone());
	}

	/**
	 * Requires the value to be in the future according to the given clock.
	 * @param clock the clock to read the current time from
	 * @return this constraint
	 */
	public C future(Clock clock) {
		return this.after(() -> this.getNow(clock), false).message(TEMPORAL_FUTURE);
	}

	/**
	 * Requires the value to be in the present or in the future according to the system
	 * default time zone.
	 *
	 * <p>
	 * A violation is reported with the message key {@code temporal.futureOrPresent} and
	 * the default message {@code "{0}" must be a date in the present or in the future}.
	 * </p>
	 * @return this constraint
	 */
	public C futureOrPresent() {
		return this.futureOrPresent(Clock.systemDefaultZone());
	}

	/**
	 * Requires the value to be in the present or in the future according to the given
	 * clock.
	 * @param clock the clock to read the current time from
	 * @return this constraint
	 */
	public C futureOrPresent(Clock clock) {
		return this.afterOrEqual(() -> this.getNow(clock), false).message(TEMPORAL_FUTURE_OR_PRESENT);
	}

	/**
	 * Requires the value to be before the temporal supplied by {@code other}.
	 *
	 * <p>
	 * If {@code memoize} is {@code true}, the supplier is called at most once and the
	 * same value is used for the comparison and the violation message. If it is
	 * {@code false} and the supplier returns a different value on each call, the value
	 * used for the comparison and the one in the message may differ.
	 * </p>
	 *
	 * <p>
	 * A violation is reported with the message key {@code temporal.before} and the
	 * default message {@code "{0}" has to be before {1}}, where {@code {1}} is the
	 * supplied temporal.
	 * </p>
	 * @param other the supplier providing the temporal the value has to be before
	 * @param memoize whether to memoize the result of the supplier
	 * @return this constraint
	 * @since 0.11.1
	 */
	public C before(Supplier<V> other, boolean memoize) {
		final Supplier<V> supplier = memoize ? memoize(other) : other;
		this.predicates()
			.add(ConstraintPredicate.of(x -> this.isBefore(x, supplier.get()), TEMPORAL_BEFORE,
					() -> new Object[] { supplier.get() }, VALID));
		return cast();
	}

	/**
	 * Requires the value to be before the temporal supplied by {@code other}.
	 *
	 * <p>
	 * The result of the supplier is memoized: it is called at most once and the same
	 * value is used for the comparison and the violation message. To evaluate the
	 * supplier on every validation, use {@link #before(Supplier, boolean)} instead.
	 * </p>
	 * @param other the supplier providing the temporal the value has to be before
	 * @return this constraint
	 */
	public C before(Supplier<V> other) {
		return this.before(other, true);
	}

	/**
	 * Requires the value to be before or equal to the temporal supplied by {@code other}.
	 *
	 * <p>
	 * If {@code memoize} is {@code true}, the supplier is called at most once and the
	 * same value is used for the comparison and the violation message. If it is
	 * {@code false} and the supplier returns a different value on each call, the value
	 * used for the comparison and the one in the message may differ.
	 * </p>
	 *
	 * <p>
	 * A violation is reported with the message key {@code temporal.beforeOrEqual} and the
	 * default message {@code "{0}" has to be before or equals to {1}}, where {@code {1}}
	 * is the supplied temporal.
	 * </p>
	 * @param other the supplier providing the temporal the value has to be before or
	 * equal to
	 * @param memoize whether to memoize the result of the supplier
	 * @return this constraint
	 * @since 0.11.1
	 */
	public C beforeOrEqual(Supplier<V> other, boolean memoize) {
		final Supplier<V> supplier = memoize ? memoize(other) : other;
		this.predicates()
			.add(ConstraintPredicate.of(x -> !this.isAfter(x, supplier.get()), TEMPORAL_BEFORE_OR_EQUAL,
					() -> new Object[] { supplier.get() }, VALID));
		return cast();
	}

	/**
	 * Requires the value to be before or equal to the temporal supplied by {@code other}.
	 *
	 * <p>
	 * The result of the supplier is memoized: it is called at most once and the same
	 * value is used for the comparison and the violation message. To evaluate the
	 * supplier on every validation, use {@link #beforeOrEqual(Supplier, boolean)}
	 * instead.
	 * </p>
	 * @param other the supplier providing the temporal the value has to be before or
	 * equal to
	 * @return this constraint
	 */
	public C beforeOrEqual(Supplier<V> other) {
		return beforeOrEqual(other, true);
	}

	/**
	 * Requires the value to be after the temporal supplied by {@code other}.
	 *
	 * <p>
	 * If {@code memoize} is {@code true}, the supplier is called at most once and the
	 * same value is used for the comparison and the violation message. If it is
	 * {@code false} and the supplier returns a different value on each call, the value
	 * used for the comparison and the one in the message may differ.
	 * </p>
	 *
	 * <p>
	 * A violation is reported with the message key {@code temporal.after} and the default
	 * message {@code "{0}" has to be after {1}}, where {@code {1}} is the supplied
	 * temporal.
	 * </p>
	 * @param other the supplier providing the temporal the value has to be after
	 * @param memoize whether to memoize the result of the supplier
	 * @return this constraint
	 * @since 0.11.1
	 */
	public C after(Supplier<V> other, boolean memoize) {
		final Supplier<V> supplier = memoize ? memoize(other) : other;
		this.predicates()
			.add(ConstraintPredicate.of(x -> this.isAfter(x, supplier.get()), TEMPORAL_AFTER,
					() -> new Object[] { supplier.get() }, VALID));
		return cast();
	}

	/**
	 * Requires the value to be after the temporal supplied by {@code other}.
	 *
	 * <p>
	 * The result of the supplier is memoized: it is called at most once and the same
	 * value is used for the comparison and the violation message. To evaluate the
	 * supplier on every validation, use {@link #after(Supplier, boolean)} instead.
	 * </p>
	 * @param other the supplier providing the temporal the value has to be after
	 * @return this constraint
	 */
	public C after(Supplier<V> other) {
		return this.after(other, true);
	}

	/**
	 * Requires the value to be after or equal to the temporal supplied by {@code other}.
	 *
	 * <p>
	 * If {@code memoize} is {@code true}, the supplier is called at most once and the
	 * same value is used for the comparison and the violation message. If it is
	 * {@code false} and the supplier returns a different value on each call, the value
	 * used for the comparison and the one in the message may differ.
	 * </p>
	 *
	 * <p>
	 * A violation is reported with the message key {@code temporal.afterOrEqual} and the
	 * default message {@code "{0}" has to be after or equals to {1}}, where {@code {1}}
	 * is the supplied temporal.
	 * </p>
	 * @param other the supplier providing the temporal the value has to be after or equal
	 * to
	 * @param memoize whether to memoize the result of the supplier
	 * @return this constraint
	 * @since 0.11.1
	 */
	public C afterOrEqual(Supplier<V> other, boolean memoize) {
		final Supplier<V> supplier = memoize ? memoize(other) : other;
		this.predicates()
			.add(ConstraintPredicate.of(x -> !this.isBefore(x, supplier.get()), TEMPORAL_AFTER_OR_EQUAL,
					() -> new Object[] { supplier.get() }, VALID));
		return cast();
	}

	/**
	 * Requires the value to be after or equal to the temporal supplied by {@code other}.
	 *
	 * <p>
	 * The result of the supplier is memoized: it is called at most once and the same
	 * value is used for the comparison and the violation message. To evaluate the
	 * supplier on every validation, use {@link #afterOrEqual(Supplier, boolean)} instead.
	 * </p>
	 * @param other the supplier providing the temporal the value has to be after or equal
	 * to
	 * @return this constraint
	 */
	public C afterOrEqual(Supplier<V> other) {
		return afterOrEqual(other, true);
	}

	/**
	 * Requires the value to be between the temporals supplied by {@code rangeFrom} and
	 * {@code rangeTo}.
	 *
	 * <p>
	 * The range is exclusive on both ends: a value equal to either bound is invalid.
	 * </p>
	 *
	 * <p>
	 * If {@code memoize} is {@code true}, the suppliers are called at most once and the
	 * same values are used for the comparison and the violation message. If it is
	 * {@code false} and the suppliers return different values on each call, the values
	 * used for the comparison and the ones in the message may differ.
	 * </p>
	 *
	 * <p>
	 * A violation is reported with the message key {@code temporal.between} and the
	 * default message {@code "{0}" has to be between {1} and {2}}, where {@code {1}} and
	 * {@code {2}} are the supplied bounds.
	 * </p>
	 * @param rangeFrom the supplier providing the start of the range
	 * @param rangeTo the supplier providing the end of the range
	 * @param memoize whether to memoize the results of the suppliers
	 * @return this constraint
	 * @throws IllegalArgumentException if {@code rangeFrom} is after {@code rangeTo}; it
	 * is thrown when the constraint is evaluated
	 * @since 0.11.1
	 */
	public C between(Supplier<V> rangeFrom, Supplier<V> rangeTo, boolean memoize) {
		final Supplier<V> supplierFrom = memoize ? memoize(rangeFrom) : rangeFrom;
		final Supplier<V> supplierTo = memoize ? memoize(rangeTo) : rangeTo;
		this.predicates().add(ConstraintPredicate.of(x -> {
			final V from = supplierFrom.get();
			final V to = supplierTo.get();
			if (this.isAfter(from, to)) {
				throw new IllegalArgumentException("Parameter 'rangeFrom' has to be before 'rangeTo'");
			}
			return this.isBefore(from, x) && this.isAfter(to, x);
		}, TEMPORAL_BETWEEN, () -> new Object[] { supplierFrom.get(), supplierTo.get() }, VALID));
		return cast();
	}

	/**
	 * Requires the value to be between the temporals supplied by {@code rangeFrom} and
	 * {@code rangeTo}.
	 *
	 * <p>
	 * The range is exclusive on both ends. The results of the suppliers are memoized: the
	 * suppliers are called at most once and the same values are used for the comparison
	 * and the violation message. To evaluate the suppliers on every validation, use
	 * {@link #between(Supplier, Supplier, boolean)} instead.
	 * </p>
	 * @param rangeFrom the supplier providing the start of the range
	 * @param rangeTo the supplier providing the end of the range
	 * @return this constraint
	 * @throws IllegalArgumentException if {@code rangeFrom} is after {@code rangeTo}; it
	 * is thrown when the constraint is evaluated
	 */
	public C between(Supplier<V> rangeFrom, Supplier<V> rangeTo) {
		return this.between(rangeFrom, rangeTo, true);
	}

	/**
	 * Requires the given predicate to accept the value of the field of the temporal.
	 *
	 * <p>
	 * A violation is reported with the message key {@code temporal.field} and the default
	 * message {@code The {1} of "{0}" is invalid}, where {@code {1}} is {@code field}.
	 * </p>
	 * @param field the temporal field to read from the value
	 * @param predicate the predicate that has to accept the field value
	 * @return this constraint
	 */
	public C fieldPredicate(TemporalField field, LongPredicate predicate) {
		this.predicates()
			.add(ConstraintPredicate.of(x -> predicate.test(x.getLong(field)), TEMPORAL_FIELD,
					() -> new Object[] { field }, VALID));
		return cast();
	}

	/**
	 * Returns a supplier that calls the given supplier at most once and returns the
	 * cached result afterwards.
	 * @param delegate the supplier to memoize
	 * @param <T> the type of the supplied value
	 * @return the memoizing supplier
	 */
	static <T> Supplier<T> memoize(Supplier<T> delegate) {
		final AtomicReference<T> supplier = new AtomicReference<>();
		return () -> {
			final T value = supplier.get();
			if (value == null) {
				return supplier.updateAndGet(prev -> prev == null ? delegate.get() : prev);
			}
			return value;
		};
	}

}
