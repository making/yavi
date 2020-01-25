package am.ik.yavi.arguments;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import am.ik.yavi.Country;
import am.ik.yavi.Range;
import am.ik.yavi.User;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.ViolationMessage;
import am.ik.yavi.fn.Either;

public class ArgumentsValidatorTest {

	final Arguments1Validator<String, Country> arguments1Validator = ArgumentsValidatorBuilder
			.of(Country::new) //
			.builder(b -> b //
					.$string(Arguments1::arg1, "name", c -> c.greaterThanOrEqual(2)))
			.build();

	final Arguments2Validator<Integer, Integer, Range> arguments2Validator = ArgumentsValidatorBuilder
			.of(Range::new)
			.builder(b -> b
					.$integer(Arguments1::arg1, "from",
							c -> c.greaterThanOrEqual(0).lessThanOrEqual(9))
					.$integer(Arguments2::arg2, "to",
							c -> c.greaterThanOrEqual(0).lessThanOrEqual(9))
					.constraintOnTarget(a -> a.arg1() < a.arg2(), "range",
							ViolationMessage.of("to.isGreaterThanFrom",
									"\"to\" must be greater than \"from\".")))
			.build();

	final Arguments3Validator<String, String, Integer, User> arguments3Validator = ArgumentsValidatorBuilder
			.of(User::new)
			.builder(b -> b
					.$string(Arguments1::arg1, "name",
							c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(20))
					.$string(Arguments2::arg2, "email",
							c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50)
									.email())
					.$integer(Arguments3::arg3, "age",
							c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200)))
			.build();

	@Test
	void testArg2_allInvalid() {
		assertThatThrownBy(() -> arguments2Validator.validateArgsToEither(-1, -3)
				.rightOrElseThrow(ConstraintViolationsException::new))
						.isInstanceOfSatisfying(ConstraintViolationsException.class,
								e -> assertThat(e.getMessage())
										.isEqualTo("Constraint violations found!"
												+ System.lineSeparator()
												+ "* \"from\" must be greater than or equal to 0"
												+ System.lineSeparator()
												+ "* \"to\" must be greater than or equal to 0"
												+ System.lineSeparator()
												+ "* \"to\" must be greater than \"from\"."));
	}

	@Test
	void testArg2_valid() {
		final Range range = arguments2Validator.validateArgsToEither(3, 5)
				.rightOrElseThrow(ConstraintViolationsException::new);
		assertThat(range.getFrom()).isEqualTo(3);
		assertThat(range.getTo()).isEqualTo(5);
	}

	@Test
	void testArg3_allInvalid() {
		ConstraintViolations violations = arguments3Validator.validateArgs("",
				"example.com", 300);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"name\" must be greater than or equal to 1. The given size is 0");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).message())
				.isEqualTo("\"email\" must be a valid email address");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.email");
		assertThat(violations.get(2).message())
				.isEqualTo("\"age\" must be less than or equal to 200");
		assertThat(violations.get(2).messageKey()).isEqualTo("numeric.lessThanOrEqual");
	}

	@Test
	void testArg3_either_allInvalid() {
		final Either<ConstraintViolations, User> either = arguments3Validator
				.validateArgsToEither("", "example.com", 300);
		assertThat(either.isRight()).isFalse();
		final ConstraintViolations violations = either.left().get();
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"name\" must be greater than or equal to 1. The given size is 0");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).message())
				.isEqualTo("\"email\" must be a valid email address");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.email");
		assertThat(violations.get(2).message())
				.isEqualTo("\"age\" must be less than or equal to 200");
		assertThat(violations.get(2).messageKey()).isEqualTo("numeric.lessThanOrEqual");
	}

	@Test
	void testArg3_either_valid() {
		final Either<ConstraintViolations, User> either = arguments3Validator
				.validateArgsToEither("foo", "foo@example.com", 30);
		assertThat(either.isRight()).isTrue();
		final User user = either.right().get();
		assertThat(user.getName()).isEqualTo("foo");
		assertThat(user.getEmail()).isEqualTo("foo@example.com");
		assertThat(user.getAge()).isEqualTo(30);
	}

	@Test
	void testArg3_valid() {
		ConstraintViolations violations = arguments3Validator.validateArgs("foo",
				"foo@example.com", 30);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void testArg_invalid() {
		assertThatThrownBy(() -> arguments1Validator.validateArgsToEither("J")
				.rightOrElseThrow(ConstraintViolationsException::new))
						.isInstanceOfSatisfying(ConstraintViolationsException.class,
								e -> assertThat(e.getMessage())
										.isEqualTo("Constraint violations found!"
												+ System.lineSeparator()
												+ "* The size of \"name\" must be greater than or equal to 2. The given size is 1"));
	}

	@Test
	void testArg_valid() {
		final Country country = arguments1Validator.validateArgsToEither("JP")
				.rightOrElseThrow(ConstraintViolationsException::new);
		assertThat(country.name()).isEqualTo("JP");
	}
}
