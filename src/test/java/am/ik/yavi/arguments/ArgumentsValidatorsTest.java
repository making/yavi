package am.ik.yavi.arguments;

import java.util.stream.Stream;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class ArgumentsValidatorsTest {
	static final StringValidator<Country> countryValidator = StringValidatorBuilder
			.of("country", c -> c.notBlank().greaterThanOrEqual(2)).build(Country::new);

	static final StringValidator<String> streetValidator = StringValidatorBuilder
			.of("street", c -> c.notBlank()).build();

	static final StringValidator<PhoneNumber> phoneNumberValidator = StringValidatorBuilder
			.of("phoneNumber",
					c -> c.notBlank().greaterThanOrEqual(8).lessThanOrEqual(16))
			.build(PhoneNumber::new);

	@ParameterizedTest
	@MethodSource("validators")
	void applyValid(Arguments3Validator<String, String, String, Address> validator) {
		final Validated<Address> validated = validator.validate("JP", "XYZ Avenue",
				"123-456-789");
		assertThat(validated.isValid()).isTrue();
		final Address address = validated.value();
		assertThat(address.country().name()).isEqualTo("JP");
		assertThat(address.street()).isEqualTo("XYZ Avenue");
		assertThat(address.phoneNumber().value()).isEqualTo("123-456-789");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void applyInvalid(Arguments3Validator<String, String, String, Address> validator) {
		final Validated<Address> validated = validator.validate("J", " ", "1234567");
		assertThat(validated.isValid()).isFalse();
		final ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(3);
		assertThat(violations.get(0).name()).isEqualTo("country");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).name()).isEqualTo("street");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(2).name()).isEqualTo("phoneNumber");
		assertThat(violations.get(2).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
	}

	static Stream<Arguments3Validator<String, String, String, Address>> validators() {
		return Stream.of(
				ArgumentsValidators.apply(Address::new, countryValidator, streetValidator,
						phoneNumberValidator),
				ArgumentsValidators
						.combine(countryValidator, streetValidator, phoneNumberValidator)
						.apply(Address::new),
				countryValidator
						.combine(streetValidator)
						.combine(phoneNumberValidator)
						.apply(Address::new));
	}
}