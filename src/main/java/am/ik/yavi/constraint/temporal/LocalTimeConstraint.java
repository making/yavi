package am.ik.yavi.constraint.temporal;

import am.ik.yavi.core.ConstraintPredicate;

import java.time.LocalTime;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static am.ik.yavi.core.NullAs.VALID;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_HOUR;
import static am.ik.yavi.core.ViolationMessage.Default.TEMPORAL_MINUTE;

/**
 * This is the actual class for constraints on LocalTime.
 *
 * @author Diego Krupitza
 */
public class LocalTimeConstraint<T>
		extends ComparableTemporalConstraintBase<T, LocalTime, LocalTimeConstraint<T>> {

	/**
	 * Is the given LocalTime at the same hour as {@code other}
	 *
	 * @param other the supplier that provides the other localtime that wraps the hour
	 * @since 0.10.0
	 */
	public LocalTimeConstraint<T> hour(Supplier<LocalTime> other) {
		this.predicates()
				.add(ConstraintPredicate.of(x -> x.getHour() == other.get().getHour(),
						TEMPORAL_HOUR, () -> new Object[] { other.get() }, VALID));
		return cast();
	}

	/**
	 * Is the given LocalTime at the same hour as {@code other}
	 *
	 * @param other the other localtime that wraps the hour
	 * @since 0.10.0
	 */
	public LocalTimeConstraint<T> hour(LocalTime other) {
		return this.hour(() -> other);
	}

	/**
	 * Is the given LocalTime at the same hour as {@code hour}
	 *
	 * @param hour the hour the LocalTime should have
	 * @since 0.10.0
	 */
	public LocalTimeConstraint<T> hour(Integer hour) {
		if (hour < 0 || hour >= 23) {
			throw new IllegalArgumentException(
					"The parameter `hour` has to be between 0 and 23");
		}
		return this.hour(LocalTime.of(hour, 0));
	}

	/**
	 * Is the given LocalTime at the same minute as represented by the {@code quarter}
	 * <ul>
	 * <li>{@code quarter} = 0 -> XX:00</li>
	 * <li>{@code quarter} = 1 -> XX:15</li>
	 * <li>{@code quarter} = 2 -> XX:30</li>
	 * </ul>
	 *
	 * @param quarter the quarter the LocalTime should have
	 * @since 0.10.0
	 */
	public LocalTimeConstraint<T> quarter(Integer quarter) {
		return this.minute(TemporalMinute.of(quarter));
	}

	/**
	 * Is the given LocalTime at the same minute as {@code minute}
	 *
	 * @param minute the minute the LocalTime should have
	 * @since 0.10.0
	 */
	public LocalTimeConstraint<T> minute(Integer minute) {
		if (minute < 0 || minute >= 60) {
			throw new IllegalArgumentException(
					"The parameter `minute` has to be between 0 and 59");
		}
		return this.minute(LocalTime.of(1, minute));
	}

	/**
	 * Is the given LocalTime at the same minute as {@code hour}
	 *
	 * @param minute the minute the LocalTime should have
	 * @since 0.10.0
	 */
	public LocalTimeConstraint<T> minute(TemporalMinute minute) {
		return this.minute(LocalTime.of(1, minute.value()));
	}

	/**
	 * Is the given LocalTime at the same minute as {@code other}
	 *
	 * @param other the other localtime that wraps the minute
	 * @since 0.10.0
	 */
	public LocalTimeConstraint<T> minute(LocalTime other) {
		return this.minute(() -> other);
	}

	/**
	 * Is the given LocalTime at the same minute as {@code other}
	 *
	 * @param other the supplier that provides the other localtime that wraps the minute
	 * @since 0.10.0
	 */
	public LocalTimeConstraint<T> minute(Supplier<LocalTime> other) {
		this.predicates()
				.add(ConstraintPredicate.of(x -> x.getMinute() == other.get().getMinute(),
						TEMPORAL_MINUTE, () -> new Object[] { other.get().getMinute() },
						VALID));
		return cast();
	}

	@Override
	protected Predicate<LocalTime> isBetween(LocalTime rangeFrom, LocalTime rangeTo) {
		return x -> {
			if (rangeFrom.isAfter(rangeTo)) {
				throw new IllegalArgumentException(
						"Parameter 'rangeFrom' has to be before 'rangeTo'");
			}
			return rangeFrom.isBefore(x) && rangeTo.isAfter(x);
		};
	}

	@Override
	public LocalTimeConstraint<T> cast() {
		return this;
	}
}