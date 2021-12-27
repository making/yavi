package am.ik.yavi.constraint.time;

import am.ik.yavi.core.Constraint;
import am.ik.yavi.core.ConstraintPredicate;

import java.time.temporal.Temporal;
import java.util.function.Predicate;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.*;

/**
 * This is the base class for constraints on Temporal classes that can be compared with
 * others. Methods in the class require the {@link V} to extend Temporal and implement
 * Comparable.
 *
 * @author Diego Krupitza
 */
public abstract class ComparableTemporalConstraintBase<T, V extends Temporal & Comparable, C extends Constraint<T, V, C>>
		extends TemporalConstraintBase<T, V, C> {

	/**
	 * Is the given temporal before {@code other}
	 *
	 * @param other the other temporal that is after
	 */
	public C before(V other) {
		this.predicates().add(ConstraintPredicate.of(x -> x.compareTo(other) <= -1,
				DATE_BEFORE, () -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * Is the given temporal after {@code other}
	 *
	 * @param other the other temporal that is before
	 */
	public C after(V other) {
		this.predicates().add(ConstraintPredicate.of(x -> x.compareTo(other) >= 1,
				DATE_AFTER, () -> new Object[] {}, VALID));
		return cast();
	}

	/**
	 * Is the given temporal between {@code rangeFrom} and {@code rangeTo} The range is
	 * not inclusive. This means if the dates are equal (rangeFrom = x = rangeTo) it is
	 * invalid
	 *
	 * @param rangeFrom the start of the range the temporal has to be in
	 * @param rangeTo the ned of the range the temporal has to be in
	 */
	public C between(V rangeFrom, V rangeTo) {
		this.predicates().add(ConstraintPredicate.of(this.isBetween(rangeFrom, rangeTo),
				DATE_BETWEEN, () -> new Object[] { rangeFrom, rangeTo }, VALID));
		return cast();
	}

	protected abstract Predicate<V> isBetween(V rangeFrom, V rangeTo);
}
