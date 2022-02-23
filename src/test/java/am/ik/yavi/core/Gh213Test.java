package am.ik.yavi.core;

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static am.ik.yavi.core.ConstraintCondition.hasAttributeWithValue;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class Gh213Test {

	@Test
	void noContextInvalid() {
		final User user = new User(null, null, null);
		final ConstraintViolations violations = User.VALIDATOR.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"firstName\" must not be null");
	}

	@Test
	void noContextValid() {
		final User user = new User("foo", null, null);
		final ConstraintViolations violations = User.VALIDATOR.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void countryContextInvalid() {
		final User user = new User("foo", null, null);
		final ConstraintViolations violations = User.VALIDATOR.validate(user,
				ConstraintContext.from(singletonMap("country", "IT")));
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"lastName\" must not be null");
	}

	@Test
	void countryContextValid() {
		final User user = new User("foo", null, null);
		final ConstraintViolations violations = User.VALIDATOR.validate(user,
				ConstraintContext.from(singletonMap("country", "FR")));
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void booleanContextInvalid() {
		final User user = new User("foo", null, null);
		final ConstraintViolations violations = User.VALIDATOR.validate(user,
				ConstraintContext
						.from(singletonMap("enableValidationInvoiceCode", true)));
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).message()).isEqualTo("\"code\" must not be null");
	}

	@Test
	void booleanContextValid() {
		final User user = new User("foo", null, null);
		final ConstraintViolations violations = User.VALIDATOR.validate(user,
				ConstraintContext
						.from(singletonMap("enableValidationInvoiceCode", false)));
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void booleanContextInvalidNested() {
		final User user = new User("foo", null, new InvoiceCode(null));
		final ConstraintViolations violations = User.VALIDATOR.validate(user,
				ConstraintContext
						.from(singletonMap("enableValidationInvoiceCode", true)));
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"code.fiscalCode\" must not be null");
	}

	@Test
	void booleanContextValidNested() {
		final User user = new User("foo", null, new InvoiceCode("a"));
		final ConstraintViolations violations = User.VALIDATOR.validate(user,
				ConstraintContext
						.from(singletonMap("enableValidationInvoiceCode", true)));
		assertThat(violations.isValid()).isTrue();
	}

	public static class User {

		private String firstName;

		private String lastName;

		private InvoiceCode code;

		static final Validator<User> VALIDATOR = ValidatorBuilder.<User> of()
				.constraint(User::getFirstName, "firstName", Constraint::notNull)
				.constraintOnCondition(hasAttributeWithValue("country", "IT"),
						b -> b.constraint(User::getLastName, "lastName",
								Constraint::notNull))
				.constraintOnCondition(
						hasAttributeWithValue("enableValidationInvoiceCode", true),
						b -> b.nest(User::getCode, "code", InvoiceCode.VALIDATOR))
				.build();

		public User(String firstName, String lastName, InvoiceCode code) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.code = code;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public InvoiceCode getCode() {
			return code;
		}
	}

	public static class InvoiceCode {
		static final Validator<InvoiceCode> VALIDATOR = ValidatorBuilder
				.<InvoiceCode> of()
				.constraint(InvoiceCode::getFiscalCode, "fiscalCode", Constraint::notNull)
				.build();

		public String fiscalCode;

		public InvoiceCode(String fiscalCode) {
			this.fiscalCode = fiscalCode;
		}

		public String getFiscalCode() {
			return fiscalCode;
		}
	}

}
