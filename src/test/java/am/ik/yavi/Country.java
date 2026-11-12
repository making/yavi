/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

import org.jspecify.annotations.Nullable;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ApplicativeValidator;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.Validator;

public class Country {

	private final @Nullable String name;

	public Country(@Nullable String name) {
		this.name = name;
	}

	static final ApplicativeValidator<Country> applicativeValidator = validator().prefixed("country").applicative();

	public static Validator<Country> validator() {
		return ValidatorBuilder.<Country>of()
			.constraint(Country::name, "name", c -> c.notBlank() //
				.greaterThanOrEqual(2))
			.build();
	}

	public @Nullable String name() {
		return this.name;
	}

	public static Validated<Country> of(String name) {
		return applicativeValidator.validate(new Country(name));
	}

	@Override
	public String toString() {
		return "Country{" + "name='" + name + '\'' + '}';
	}

}
