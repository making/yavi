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
package am.ik.yavi.arguments;

import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import am.ik.yavi.core.ConstraintViolationsException;

public class Product {
	private final String name;

	private final int price;

	static final Arguments2Validator<String, Integer, Product> validator = ArgumentsValidatorBuilder
			.of(Product::new) //
			.builder(b -> b //
					._string(Arguments1::arg1, "name", c -> c.notEmpty())
					._integer(Arguments2::arg2, "price", c -> c.greaterThan(0)))
			.build();

	public Product(String name, int price) {
		validator.lazy().validate(name, price)
				.throwIfInvalid(ConstraintViolationsException::new);
		this.name = name;
		this.price = price;
	}

	@Override
	public String toString() {
		return "Product{" + "name='" + name + '\'' + ", price=" + price + '}';
	}
}
