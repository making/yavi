package am.ik.yavi.fn;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionsTest {

	@Test
	void curry() {
		final Function2<Integer, Integer, Integer> add = (x, y) -> x + y;
		final Function1<Integer, Integer> add10 = Functions.curry(add).apply(10);
		final Integer result = add10.apply(2);
		assertThat(result).isEqualTo(12);
	}
}