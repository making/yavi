package am.ik.yavi.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.api.Test;
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

	static final StringValidator<String> v1 = StringValidatorBuilder
			.of("s1", c -> c.notBlank()).build();

	static final StringValidator<String> v2 = StringValidatorBuilder
			.of("s2", c -> c.notBlank()).build();

	static final StringValidator<String> v3 = StringValidatorBuilder
			.of("s3", c -> c.notBlank()).build();

	static final StringValidator<String> v4 = StringValidatorBuilder
			.of("s4", c -> c.notBlank()).build();

	static final StringValidator<String> v5 = StringValidatorBuilder
			.of("s5", c -> c.notBlank()).build();

	static final StringValidator<String> v6 = StringValidatorBuilder
			.of("s6", c -> c.notBlank()).build();

	static final StringValidator<String> v7 = StringValidatorBuilder
			.of("s7", c -> c.notBlank()).build();

	static final StringValidator<String> v8 = StringValidatorBuilder
			.of("s8", c -> c.notBlank()).build();

	static final StringValidator<String> v9 = StringValidatorBuilder
			.of("s9", c -> c.notBlank()).build();

	static final StringValidator<String> v10 = StringValidatorBuilder
			.of("s10", c -> c.notBlank()).build();

	static final Arguments10Validator<String, String, String, String, String, String, String, String, String, String, List<String>> arguments10Validator = v1
			.split(v2).split(v3).split(v4).split(v5).split(v6).split(v7).split(v8)
			.split(v9).split(v10).apply(Arrays::asList);

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

	@Test
	void arguments10Valid() {
		final Validated<List<String>> validated = arguments10Validator.validate("1", "2",
				"3", "4", "5", "6", "7", "8", "9", "10");
		assertThat(validated.isValid()).isTrue();
		final List<String> list = validated.value();
		assertThat(list).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9",
				"10");
	}

	@Test
	void arguments10Invalid() {

	}

	static Stream<Arguments3Validator<String, String, String, Address>> validators() {
		return Stream.of(
				ArgumentsValidators.split(countryValidator, streetValidator,
						phoneNumberValidator, Address::new),
				countryValidator.split(streetValidator).split(phoneNumberValidator)
						.apply(Address::new));
	}
}