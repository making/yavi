package am.ik.yavi.fn;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Function2Test {

	@Test
	void curried() {
		Function2<Integer, Integer, Integer> add = (x, y) -> x + y;
		final Function1<Integer, Integer> add10 = add.curried().apply(10);
		final Integer result = add10.apply(2);
		assertThat(result).isEqualTo(12);
	}
}