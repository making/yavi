package am.ik.yavi;

import am.ik.yavi.core.Validator;

public class Country {
	private final String name;

	public Country(String name) {
		this.name = name;
	}

	public static Validator<Country> validator() {
		return Validator.<Country> builder()
				.constraint(Country::name, "name", c -> c.notBlank() //
						.greaterThanOrEquals(2))
				.build();
	}

	public String name() {
		return this.name;
	}
}
