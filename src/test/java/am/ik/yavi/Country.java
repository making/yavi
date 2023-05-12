/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ApplicativeValidator;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.Validator;

public class Country {
	private final String name;

	public Country(String name) {
		this.name = name;
	}

	static final ApplicativeValidator<Country> applicativeValidator = validator()
			.prefixed("country").applicative();

	public static Validator<Country> validator() {
		return ValidatorBuilder.<Country> of()
				.constraint(Country::name, "name", c -> c.notBlank() //
						.greaterThanOrEqual(2))
				.build();
	}

	public String name() {
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
