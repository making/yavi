package am.ik.yavi.core;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CollectionConstraint;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh432Test {

	@Test
	@SuppressWarnings("unchecked")
	void testDuplicatedElements() {
		Data u = new Data(Arrays.asList(1, 1, 1, 2, 3, 1, 1, 2, 3, 1, 4));
		Validator<Data> validator = ValidatorBuilder.of(Data.class)
			.constraint(Data::getList, "list", CollectionConstraint::unique)
			.build();
		ConstraintViolations violations = validator.validate(u);
		assertThat(violations.isValid()).isFalse();
		Object[] args = violations.get(0).args();
		assertThat(args).hasSize(2);
		assertThat(args[0]).isEqualTo("list");
		assertThat((Collection<Integer>) args[1]).containsExactly(1, 2, 3);
	}

	public static class Data {

		final List<Integer> list;

		public Data(List<Integer> list) {
			this.list = list;
		}

		public List<Integer> getList() {
			return list;
		}

	}

}
