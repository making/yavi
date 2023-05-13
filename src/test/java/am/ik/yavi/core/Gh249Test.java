package am.ik.yavi.core;

import java.util.Collections;
import java.util.List;

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static am.ik.yavi.core.ViolationMessage.Default.NUMERIC_GREATER_THAN;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class Gh249Test {

	Validator<B> bValidator = ValidatorBuilder.of(B.class)
			.constraint(B::x, "x", c -> c.greaterThan(0)) //
			.constraint(B::y, "y", c -> c.equalTo("y")) //
			.failFast(true) //
			.build();

	final B validB = new B(1, "y");

	final B invalidB = new B(0, "not y");

	final Validator<A> aValidator = ValidatorBuilder.of(A.class)
			.nest(A::b, "b", bValidator) //
			.forEach(A::bs, "bs", bValidator) //
			.build();

	@Test
	void failFastOfNestedValidator() {
		final A a = new A(invalidB, Collections.singletonList(validB));
		final List<String> violations = aValidator.validate(a).violations().stream()
				.map(ConstraintViolation::messageKey).collect(toList());
		assertThat(violations).hasSize(1);
		assertThat(violations).contains(NUMERIC_GREATER_THAN.messageKey());
	}

	@Test
	void failFastOfCollectionValidator() {
		final A a = new A(validB, Collections.singletonList(invalidB));
		final List<String> violations = aValidator.validate(a).violations().stream()
				.map(ConstraintViolation::messageKey).collect(toList());
		assertThat(violations).hasSize(1);
		assertThat(violations).contains(NUMERIC_GREATER_THAN.messageKey());
	}

	@Test
	void failFastOfConditionalValidator() {
		final Validator<A> validator = ValidatorBuilder.of(A.class)
				.constraintOnGroup(Group.UPDATE,
						builder -> builder.nest(A::b, "b", bValidator).failFast(true))
				.constraintOnGroup(Group.UPDATE,
						builder -> builder.forEach(A::bs, "bs",
								b -> b.constraint(B::x, "x", c -> c.greaterThan(0))
										.constraint(B::y, "y", c -> c.equalTo("y")))) //
				.build();
		final List<String> violations = validator
				.validate(new A(invalidB, Collections.singletonList(invalidB)),
						Group.UPDATE)
				.violations().stream().map(ConstraintViolation::messageKey)
				.collect(toList());
		assertThat(violations).hasSize(1);
		assertThat(violations).contains(NUMERIC_GREATER_THAN.messageKey());
	}

	static class B {
		private final int x;

		private final String y;

		B(int x, String y) {
			this.x = x;
			this.y = y;
		}

		public int x() {
			return x;
		}

		public String y() {
			return y;
		}

		@Override
		public String toString() {
			return "B{" + "x=" + x + ", y='" + y + '\'' + '}';
		}
	}

	static class A {
		private final B b;

		private final List<B> bs;

		A(B b, List<B> bs) {
			this.b = b;
			this.bs = bs;
		}

		public B b() {
			return b;
		}

		public List<B> bs() {
			return bs;
		}

		@Override
		public String toString() {
			return "A{" + "b=" + b + ", bs=" + bs + '}';
		}
	}
}
