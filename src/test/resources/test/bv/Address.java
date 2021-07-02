package test.bv;

import javax.validation.constraints.NotBlank;

public class Address {
	@NotBlank
	private String street;

	private Country country;

	private City city;

	public Address(String street, Country country, City city) {
		this.street = street;
		this.country = country;
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public static class Country {
		@NotBlank
		private String value;

		public Country(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public static class City {
		@NotBlank
		private String value;

		public City(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
