package am.ik.yavi.core;

import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.*;

public class MultiNestedCollectionValidatorTest {
	Validator<Address> addressValidator = Validator.<Address> builder()
			.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
			.constraint(Address::country, "country", Country.validator())
			.constraintIfNotNull(Address::phoneNumber, "phoneNumber",
					PhoneNumber.validator())
			.build();
	Validator<FormWithCollection> formValidator = Validator
			.builder(FormWithCollection.class) //
			.constraintForEach(FormWithCollection::getAddresses, "addresses",
					addressValidator)
			.build();

	Validator<NestedFormWithCollection> validator = Validator
			.builder(NestedFormWithCollection.class)
			.constraintForEach(NestedFormWithCollection::getForms, "forms", formValidator)
			.build();

	@Test
	public void valid() {
		NestedFormWithCollection form = new NestedFormWithCollection(
				Arrays.asList(new FormWithCollection(Arrays.asList(
						new Address(new Country("JP"), "tokyo",
								new PhoneNumber("0123456789")),
						new Address(new Country("JP"), "osaka",
								new PhoneNumber("0123456788"))))));
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	public void invalid() {
		NestedFormWithCollection form = new NestedFormWithCollection(
				Arrays.asList(new FormWithCollection(Arrays.asList(
						new Address(new Country("JP"), "tokyo",
								new PhoneNumber("0123456")),
						new Address(new Country("JP"), "",
								new PhoneNumber("0123456788"))))));
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(2);
		assertThat(violations.get(0).name())
				.isEqualTo("forms[0].addresses[0].phoneNumber.value");
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"forms[0].addresses[0].phoneNumber.value\" must be greater than or equal to 8");
		assertThat(violations.get(1).name()).isEqualTo("forms[0].addresses[1].street");
		assertThat(violations.get(1).message())
				.isEqualTo("\"forms[0].addresses[1].street\" must not be blank");
	}
}
