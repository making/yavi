package am.ik.yavi.constraint.temporal;

import java.util.stream.Stream;

/**
 * An enum that represents all the important minute values. This enum will allow you to
 * have a more fluent way to specifying a given minute in validation. The following
 * minutes are stored.
 * <ul>
 * <li>{@link TemporalMinute#FULL_HOUR} - the full hour</li>
 * <li>{@link TemporalMinute#QUARTER_PAST} - 15 minutes</li>
 * <li>{@link TemporalMinute#HALF_PAST} - 30 minutes</li>
 * <li>{@link TemporalMinute#QUARTER_TO} - 45 minutes</li>
 * </ul>
 *
 * @author Diego Krupitza
 * @since 0.10.0
 */
public enum TemporalMinute {
	FULL_HOUR(0), QUARTER_PAST(15), HALF_PAST(30), QUARTER_TO(45);

	private final Integer minute;

	TemporalMinute(Integer minute) {
		this.minute = minute;
	}

	/**
	 * @return the integer value representing the temporal minute
	 * @since 0.10.0
	 */
	public Integer value() {
		return minute;
	}

	/**
	 * Gets the enum representing the given quarter of the hour. The {@code quarter} has
	 * to be between 0 and 3.
	 *
	 * @param quarter the quarter of the {@link TemporalMinute} we want to receive
	 * @return the {@link TemporalMinute} of the given quarter
	 * @throws IllegalArgumentException in case the quarter is not between 0 and 3
	 * @since 0.10.0
	 */
	public static TemporalMinute of(Integer quarter) {
		return Stream.of(TemporalMinute.values())
				.filter(item -> item.value().equals(quarter * 15)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(String.format(
						"Please specify a quarter from 0 and 3! The quarter you provided %d is not valid!",
						quarter)));
	}
}
