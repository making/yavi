package am.ik.yavi.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.User;

public class ValidatorTest {
	Validator<User> validator() {
		return new Validator<User>() //
				.constraint(User::getName, "name", c -> c.notNull() //
						.greaterThanOrEquals(1) //
						.lessThanOrEquals(20)) //
				.constraint(User::getEmail, "email", c -> c.notNull() //
						.greaterThanOrEquals(1) //
						.lessThanOrEquals(50) //
						.email()) //
				.constraint(User::getAge, "age", c -> c.notNull() //
						.greaterThanOrEquals(0) //
						.lessThanOrEquals(200));
	}

	@Test
	public void valid() throws Exception {
		User user = new User("foo", "foo@example.com", 30);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	public void allInvalid() throws Exception {
		User user = new User("", "example.com", 300);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message())
				.isEqualTo("\"name\" must be greater than 1");
		assertThat(violations.get(1).message())
				.isEqualTo("\"email\" must be a valid email address");
		assertThat(violations.get(2).message())
				.isEqualTo("\"age\" must not be less than 200");
	}

	@Test
	public void multipleViolationOnOneProperty() throws Exception {
		User user = new User("foo", "", 200);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(2);
		assertThat(violations.get(0).message())
				.isEqualTo("\"email\" must be greater than 1");
		assertThat(violations.get(1).message())
				.isEqualTo("\"email\" must be a valid email address");
	}

	@Test
	public void nullValues() throws Exception {
		User user = new User(null, null, null);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message()).isEqualTo("\"name\" must not be null");
		assertThat(violations.get(1).message()).isEqualTo("\"email\" must not be null");
		assertThat(violations.get(2).message()).isEqualTo("\"age\" must not be null");
	}
}