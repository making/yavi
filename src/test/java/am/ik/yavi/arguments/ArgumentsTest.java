package am.ik.yavi.arguments;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArgumentsTest {

	@Test
	void arguments3() {
		Arguments3<Integer, String, Double> args = Arguments.of(1, "two", 3.0);
		assertThat(args.arg1()).isEqualTo(1);
		assertThat(args.arg2()).isEqualTo("two");
		assertThat(args.arg3()).isEqualTo(3.0);
		assertThat(args.first1()).isEqualTo(Arguments.of(1));
		assertThat(args.first2()).isEqualTo(Arguments.of(1, "two"));
		assertThat(args.last1()).isEqualTo(Arguments.of(3.0));
		assertThat(args.last2()).isEqualTo(Arguments.of("two", 3.0));
		assertThat(args.append(4.0f)).isEqualTo(Arguments.of(1, "two", 3.0, 4.0f));
		assertThat(args.prepend(0)).isEqualTo(Arguments.of(0, 1, "two", 3.0));
		assertThat(args.reverse()).isEqualTo(Arguments.of(3.0, "two", 1));
	}

	@Test
	void arguments4() {
		Arguments4<Integer, String, Double, Float> args = Arguments.of(1, "two", 3.0, 4.0f);
		assertThat(args.arg1()).isEqualTo(1);
		assertThat(args.arg2()).isEqualTo("two");
		assertThat(args.arg3()).isEqualTo(3.0);
		assertThat(args.arg4()).isEqualTo(4.0f);
		assertThat(args.first1()).isEqualTo(Arguments.of(1));
		assertThat(args.first2()).isEqualTo(Arguments.of(1, "two"));
		assertThat(args.first3()).isEqualTo(Arguments.of(1, "two", 3.0));
		assertThat(args.last1()).isEqualTo(Arguments.of(4.0f));
		assertThat(args.last2()).isEqualTo(Arguments.of(3.0, 4.0f));
		assertThat(args.last3()).isEqualTo(Arguments.of("two", 3.0, 4.0f));
		assertThat(args.append(5)).isEqualTo(Arguments.of(1, "two", 3.0, 4.0f, 5));
		assertThat(args.prepend(0)).isEqualTo(Arguments.of(0, 1, "two", 3.0, 4.0f));
	}

}