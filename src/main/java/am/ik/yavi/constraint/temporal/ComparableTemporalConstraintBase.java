package am.ik.yavi.constraint.temporal;

import am.ik.yavi.constraint.base.ConstraintBase;
import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintPredicate;

import java.time.temporal.Temporal;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.*;

/**
 * This is the base class for constraints on Temporal classes that can be compared with
 * others. Methods in the class require the {@link V} to extend Temporal and implement
 * Comparable.
 *
 * @author Diego Krupitza
 */
abstract class ComparableTemporalConstraintBase<T, V extends Temporal & Comparable, C extends Constraint<T, V, C>>
		extends ConstraintBase<T, V, C> {

	/**
	 * Is the given temporal before {@code other}
	 *
	 * @param other the other temporal that is after
	 */
	public C before(V other) {
		return this.before(() -> other);
	}

	/**
	 * Is the given temporal before the supplied {@code other}
	 *
	 * @param other the supplier providing the other temporal that is after
	 */
	public C before(Supplier<V> other) {
		this.predicates().add(ConstraintPredicate.of(x -> x.compareTo(other.get()) <= -1,
				TEMPORAL_BEFORE, () -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * Is the given temporal after {@code other}
	 *
	 * @param other the other temporal that is before
	 */
	public C after(V other) {
		return this.after(() -> other);
	}

	/**
	 * Is the given temporal after the supplied {@code other}
	 *
	 * @param other the supplier providing the other temporal that is before
	 */
	public C after(Supplier<V> other) {
		this.predicates().add(ConstraintPredicate.of(x -> x.compareTo(other.get()) >= 1,
				TEMPORAL_AFTER, () -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * Is the given temporal between {@code rangeFrom} and {@code rangeTo}. The range is
	 * not inclusive. This means if the dates are equal (rangeFrom = x = rangeTo) it is
	 * invalid
	 *
	 * @param rangeFrom the start of the range the temporal has to be in
	 * @param rangeTo the end of the range the temporal has to be in
	 */
	public C between(V rangeFrom, V rangeTo) {
		return this.between(() -> rangeFrom, () -> rangeTo);
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
		this.predicates()
				.add(ConstraintPredicate.of(
						this.isBetween(rangeFrom.get(), rangeTo.get()), TEMPORAL_BETWEEN,
						() -> new Object[] { rangeFrom, rangeTo }, VALID));
		return cast();
	}

	protected abstract Predicate<V> isBetween(V rangeFrom, V rangeTo);
}
