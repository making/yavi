package am.ik.yavi.core;

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh145Test {

	private static final Validator<Address> ADDRESS_VALIDATOR = ValidatorBuilder
			.of(Address.class)
			.constraint(Address::getCity, "city", city -> city.notBlank())
			.nestIfPresent(Address::getCountry, "country",
					countryValidatorBuilder -> countryValidatorBuilder
							.constraint(Country::getCode, "code",
									code -> code.lessThan(3))
							.constraintOnCondition(
									(country, group) -> country.getName() != null,
									conditionalCountryBuilder -> conditionalCountryBuilder
											.constraint(Country::getCode, "code",
													code -> code.notBlank())))
			.build();

	private static final Validator<Person> PERSON_VALIDATOR = ValidatorBuilder
			.of(Person.class).constraint(Person::getName, "name", name -> name.notBlank())
			.nestIfPresent(Person::getAddress, "address", ADDRESS_VALIDATOR).build();

	@Test
	void shouldBeValidAddressWithoutCountry() {
		Address address = new Address("Paris", null);

		ConstraintViolations violations = ADDRESS_VALIDATOR.validate(address);

		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void shouldBeValidPersonWithoutAddress() {
		Person person = new Person("Jack", null);

		ConstraintViolations violations = PERSON_VALIDATOR.validate(person);

		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void shouldBeInvalidWhenAddressHasCountryWithoutCode() {
		Address address = new Address("Paris", new Country(null, "France"));

		ConstraintViolations violations = ADDRESS_VALIDATOR.validate(address);

		assertThat(violations.isValid()).isFalse();
		assertThat(violations).extracting(ConstraintViolation::message)
				.containsOnly("\"country.code\" must not be blank");
	}

	@Test
	void shouldBeInvalidWhenPersonHasAddressWithoutCountryCode() {
		Person person = new Person("Jack",
				new Address("Paris", new Country(null, "France")));

		ConstraintViolations violations = PERSON_VALIDATOR.validate(person);

		assertThat(violations.isValid()).isFalse();
		assertThat(violations).extracting(ConstraintViolation::message)
				.containsOnly("\"address.country.code\" must not be blank");
	}

	@Test
	void shouldBeValidWhenPersonHasAddressWithCountryCodeAndName() {
		Person person = new Person("Jack",
				new Address("Paris", new Country("FR", "France")));

		ConstraintViolations violations = PERSON_VALIDATOR.validate(person);

		assertThat(violations.isValid()).isTrue();
	}

	private static class Person {
		private String name;
		private Address address;

		public Person(String name, Address address) {
			this.name = name;
			this.address = address;
		}

		public String getName() {
			return name;
		}

		public Address getAddress() {
			return address;
		}
	}

	private static class Address {
		private String city;
		private Country country;

		public Address(String city, Country country) {
			this.city = city;
			this.country = country;
		}

		public String getCity() {
			return city;
		}

		public Country getCountry() {
			return country;
		}
	}

	private static class Country {
		private String code;
		private String name;

		public Country(String code, String name) {
			this.code = code;
			this.name = name;
		}

		public String getCode() {
			return code;
		}

		public String getName() {
			return name;
		}
	}
}
