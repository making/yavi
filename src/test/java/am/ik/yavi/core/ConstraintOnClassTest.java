package am.ik.yavi.core;

import am.ik.yavi.User;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CharSequenceConstraint;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

public class ConstraintOnClassTest {

	@ParameterizedTest
	@MethodSource("provideValidators")
	void testConstraintOnConditionClass(Validator<Parent> validator) {
		Parent validChild = new Child(23, "drawing");
		Parent invalidChild = new Child(6, "drawing");

		assertThat(validator.validate(validChild).isValid()).isTrue();
		assertThat(validator.validate(invalidChild).isValid()).isFalse();
	}

	@ParameterizedTest
	@MethodSource("provideValidators")
	void testConstraintOnNonConditionClass(Validator<Parent> validator) {
		Parent validParent = new Parent(35);
		Parent invalidParent = new Parent(19);

		assertThat(validator.validate(validParent).isValid()).isTrue();
		assertThat(validator.validate(invalidParent).isValid()).isFalse();
	}

	static Stream<Arguments> provideValidators() {
		ValidatorBuilder<Parent> userValidatorBuilder = ValidatorBuilder.of(Parent.class)
				.constraint(Parent::getAge, "age", c -> c.greaterThan(20));
		Function<CharSequenceConstraint<Child, String>, CharSequenceConstraint<Child, String>> equalsToDrawing = (
				CharSequenceConstraint<Child, String> c) -> c.equalTo("drawing");

		return Stream
				.of(Arguments.of(new ValidatorBuilder<>(userValidatorBuilder)
						.constraintOnClass(Child.class,
								ValidatorBuilder.of(Child.class)
										.constraint(Child::getHobby, "hobby",
												equalsToDrawing)
										.build())
						.build()), Arguments
								.of(new ValidatorBuilder<>(userValidatorBuilder)
										.constraintOnClass(Child.class,
												b -> b.constraint(Child::getHobby,
														"hobby", equalsToDrawing))
										.build()));
	}

	private static class Child extends Parent {
		private String hobby;

		public Child(int age, String hobby) {
			super(age);
			this.hobby = hobby;
		}

		public String getHobby() {
			return hobby;
		}
	}

	private static class Parent {
		private final int age;

		public Parent(int age) {
			this.age = age;
		}

		public int getAge() {
			return age;
		}
	}
}
