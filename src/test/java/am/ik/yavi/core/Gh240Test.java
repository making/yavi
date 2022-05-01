package am.ik.yavi.core;

import java.util.Arrays;
import java.util.List;

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh240Test {
	@Test
	void defaultMessage() {
		final Validator<List<String>> validator = ValidatorBuilder.<List<String>> of()
				._collection(l -> l, "list", l -> l.lessThanOrEqual(2)).build();
		final ConstraintViolation violation = validator
				.validate(Arrays.asList("x", "y", "z")).get(0);
		assertThat(violation.message()).isEqualTo(
				"The size of \"list\" must be less than or equal to 2. The given size is 3");
	}

	@Test
	void customMessage() {
		final Validator<List<String>> validator = ValidatorBuilder.<List<String>> of()
				._collection(l -> l, "list",
						l -> l.lessThanOrEqual(2)
								.message("{0} had {2} elements, max allowed is {1}"))
				.build();
		final ConstraintViolation violation = validator
				.validate(Arrays.asList("x", "y", "z")).get(0);
		assertThat(violation.message())
				.isEqualTo("list had 3 elements, max allowed is 2");
	}
}
