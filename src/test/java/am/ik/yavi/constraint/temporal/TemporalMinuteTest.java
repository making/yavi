package am.ik.yavi.constraint.temporal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemporalMinuteTest {

	@ParameterizedTest
	@MethodSource("validTemporalOfValues")
	void validTemporalOfMatchesTest(Integer value, TemporalMinute expected) {
		assertThat(TemporalMinute.of(value)).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource("invalidTemporalOfValues")
	void invalidValuesThrowException(Integer value) {
		assertThatThrownBy(() -> TemporalMinute.of(value))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining(
						"Please specify a quarter from 0 and 3! The quarter you provided "
								+ value + " is not valid");
	}

	private static Stream<Arguments> validTemporalOfValues() {
		return Stream.of(TemporalMinute.values())
				.map(item -> Arguments.of(item.value() / 15, item));
	}

	private static Stream<Arguments> invalidTemporalOfValues() {
		return IntStream.rangeClosed(4, 10).boxed().map(Arguments::of);
	}
}
