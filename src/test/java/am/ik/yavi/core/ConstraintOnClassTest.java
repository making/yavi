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
	void testConstraintOnConditionClass(Validator<User> validator) {
		User validAdmin = new Admin("admin123", "admin@gmail", 27, "yavi123");
		User invalidAdmin = new Admin("Niraz", "niraz@gmail", 23, "user");

		assertThat(validator.validate(validAdmin).isValid()).isTrue();
		assertThat(validator.validate(invalidAdmin).isValid()).isFalse();
	}

	@ParameterizedTest
	@MethodSource("provideValidators")
	void testConstraintOnNonConditionClass(Validator<User> validator) {
		User validUser = new User("Rawad", "rawad@gmail", 25);
		User invalidUser = new User("Almog", "almog@gmail", 19);

		assertThat(validator.validate(validUser).isValid()).isTrue();
		assertThat(validator.validate(invalidUser).isValid()).isFalse();
	}

	static Stream<Arguments> provideValidators() {
		ValidatorBuilder<User> userValidatorBuilder = ValidatorBuilder.of(User.class)
				.constraint(User::getAge, "age", c -> c.greaterThan(20));
		Function<CharSequenceConstraint<Admin, String>, CharSequenceConstraint<Admin, String>> startsWithAdmin = (
				CharSequenceConstraint<Admin, String> c) -> c.startsWith("yavi");

		return Stream
				.of(Arguments.of(new ValidatorBuilder<>(userValidatorBuilder)
						.constraintOnClass(Admin.class,
								ValidatorBuilder.of(Admin.class)
										.constraint(Admin::getGroup, "group",
												startsWithAdmin)
										.build())
						.build()), Arguments
								.of(new ValidatorBuilder<>(userValidatorBuilder)
										.constraintOnClass(Admin.class,
												b -> b.constraint(Admin::getGroup,
														"group", startsWithAdmin))
										.build()));
	}

	private static class Admin extends User {
		private String group;

		public Admin(String name, String email, int age, String group) {
			super(name, email, age);
			this.group = group;
		}

		public String getGroup() {
			return group;
		}
	}
}
