package am.ik.yavi.arguments;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArgumentsTest {

	@Test
	void firstNOfArguments3() {
		Arguments3<Integer, String, Double> args = Arguments.of(1, "two", 3.0);
		assertThat(args.arg1()).isEqualTo(1);
		assertThat(args.arg2()).isEqualTo("two");
		assertThat(args.arg3()).isEqualTo(3.0);
		assertThat(args.first1()).isEqualTo(Arguments.of(1));
		assertThat(args.first2()).isEqualTo(Arguments.of(1, "two"));
		System.out.println(args.first1());
		System.out.println(args.first2());
		System.out.println(args);

	}

}